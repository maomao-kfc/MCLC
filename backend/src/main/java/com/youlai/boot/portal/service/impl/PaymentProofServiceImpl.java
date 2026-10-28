package com.youlai.boot.portal.service.impl;

import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.youlai.boot.module.file.model.FileInfo;
import com.youlai.boot.module.file.service.FileService;
import com.youlai.boot.portal.mapper.PaymentProofMapper;
import com.youlai.boot.portal.mapper.RegisterUserInformationMapper;
import com.youlai.boot.portal.model.entity.PaymentProof;
import com.youlai.boot.portal.model.entity.RegisterUserInformation;
import com.youlai.boot.portal.model.form.PaymentProofSubmitForm;
import com.youlai.boot.portal.model.form.ProofReviewForm;
import com.youlai.boot.portal.model.vo.PaymentProofPageVO;
import com.youlai.boot.portal.service.OcrService;
import com.youlai.boot.portal.service.PaymentProofService;
import com.youlai.boot.portal.util.ByteArrayMultipartFile;
import com.youlai.boot.portal.util.PaymentProofUtil;
import com.youlai.boot.security.util.SecurityUtils;
import com.youlai.boot.support.mail.enums.MailBusinessType;
import com.youlai.boot.support.mail.service.MailSendRecordService;
import com.youlai.boot.system.mapper.RegistrationTypeMapper;
import com.youlai.boot.system.model.entity.RegistrationType;
import com.youlai.boot.system.model.form.EmailTemplateForm;
import com.youlai.boot.system.service.EmailTemplateService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 支付凭证服务实现
 * <p>流程：用户上传截图 → 后端加水印 → MD5/pHash去重 → OCR初筛 → 落库待核验</p>
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class PaymentProofServiceImpl implements PaymentProofService {

    private final PaymentProofMapper paymentProofMapper;
    private final RegisterUserInformationMapper registerUserInformationMapper;
    private final RegistrationTypeMapper registrationTypeMapper;
    private final FileService fileService;
    private final OcrService ocrService;
    private final MailSendRecordService mailSendRecordService;
    private final EmailTemplateService emailTemplateService;

    /** dHash 汉明距离阈值，小于此值视为同一张图 */
    private static final int DHASH_DISTANCE_THRESHOLD = 5;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Map<String, Object> submitProof(MultipartFile file, PaymentProofSubmitForm form) {
        // 1. 校验注册用户
        RegisterUserInformation registerUser = registerUserInformationMapper.selectById(form.getRegistrationId());
        if (registerUser == null) {
            throw new RuntimeException("注册用户不存在");
        }
        // 已支付/已线下登记的不允许重复提交
        if ("paid".equals(registerUser.getProofStatus()) || "offline_paid".equals(registerUser.getProofStatus())) {
            throw new RuntimeException("该报名已确认支付，无需重复提交凭证");
        }

        // 1.1 补算应付金额（存量数据 payment_amount 为空时，根据注册类型和当前日期从 registration_type 计算）
        if (registerUser.getPaymentAmount() == null || registerUser.getPaymentAmount().isBlank()) {
            String calculatedAmount = calculatePaymentAmount(registerUser);
            if (calculatedAmount != null) {
                registerUser.setPaymentAmount(calculatedAmount);
                registerUserInformationMapper.updateById(registerUser);
                log.info("补算应付金额: registrationId={}, registrationFee={}, paymentAmount={}",
                        form.getRegistrationId(), registerUser.getRegistrationFee(), calculatedAmount);
            } else {
                log.warn("无法补算应付金额: registrationId={}, registrationFee={}",
                        form.getRegistrationId(), registerUser.getRegistrationFee());
            }
        }

        // 2. 校验文件格式
        String originalFilename = file.getOriginalFilename();
        if (originalFilename == null || !isImageFile(originalFilename)) {
            throw new RuntimeException("仅支持上传图片文件（jpg/png/jpeg）");
        }
        // 文件大小限制 10MB
        if (file.getSize() > 10 * 1024 * 1024) {
            throw new RuntimeException("图片大小不能超过10MB");
        }

        // 3. 读取图片（从已读字节数组解码，避免流重复读取问题）
        byte[] imageBytes;
        BufferedImage originalImage;
        try {
            imageBytes = file.getBytes();
            originalImage = ImageIO.read(new ByteArrayInputStream(imageBytes));
            if (originalImage == null) {
                // ImageIO 无法解码（可能为特殊格式/色彩空间，如CMYK JPEG、HEIC改扩展名等）
                // 记录文件头魔数便于诊断，降级处理：跳过水印和dHash，仍允许提交
                log.warn("ImageIO无法解码凭证图片，将跳过水印和感知哈希去重。文件名={}, 大小={}B, contentType={}, 魔数={}",
                        originalFilename, imageBytes.length, file.getContentType(), bytesToHex(imageBytes, 8));
            }
        } catch (Exception e) {
            log.error("读取凭证图片失败", e);
            throw new RuntimeException("读取图片失败: " + e.getMessage());
        }

        // 4. MD5 精确去重（排除已驳回的凭证：被驳回后允许重新提交）
        String md5 = PaymentProofUtil.calculateMd5(imageBytes);
        Long md5DuplicateCount = paymentProofMapper.selectCount(
                new LambdaQueryWrapper<PaymentProof>()
                        .eq(PaymentProof::getFileMd5, md5)
                        .ne(PaymentProof::getStatus, "rejected"));
        if (md5DuplicateCount > 0) {
            throw new RuntimeException("该截图已被上传过了，请勿重复使用同一张截图");
        }

        // 5. dHash 感知哈希去重（防裁剪/压缩重复；图片无法解码时跳过；排除已驳回的凭证）
        String dHash = null;
        if (originalImage != null) {
            dHash = PaymentProofUtil.calculateDHash(originalImage);
            List<PaymentProof> allProofs = paymentProofMapper.selectList(
                    new LambdaQueryWrapper<PaymentProof>()
                            .isNotNull(PaymentProof::getFilePhash)
                            .ne(PaymentProof::getStatus, "rejected"));
            for (PaymentProof existing : allProofs) {
                int distance = PaymentProofUtil.hammingDistance(dHash, existing.getFilePhash());
                if (distance < DHASH_DISTANCE_THRESHOLD) {
                    log.warn("感知哈希去重拦截: 新凭证dHash={}, 与凭证id={}的dHash={} 汉明距离={}",
                            dHash, existing.getId(), existing.getFilePhash(), distance);
                    throw new RuntimeException("该截图与已上传的凭证高度相似，请勿重复使用同一张截图");
                }
            }
        }

        // 6. 后端加水印（图片无法解码时跳过水印，直接上传原图）
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));
        Long userId = null;
        try {
            userId = SecurityUtils.getUserId();
        } catch (Exception ignored) {
            // 门户用户可能未登录系统，userId 留空
        }
        String watermarkText = StrUtil.format("MCLC-REG{}-u{}-{}", form.getRegistrationId(), userId != null ? userId : "anon", timestamp);
        String format = originalFilename.toLowerCase().endsWith(".png") ? "png" : "jpg";
        String watermarkedFilename = "proof_" + form.getRegistrationId() + "_" + timestamp + "." + format;
        byte[] uploadBytes;
        if (originalImage != null) {
            BufferedImage watermarkedImage = PaymentProofUtil.addWatermark(originalImage, watermarkText);
            try {
                uploadBytes = PaymentProofUtil.imageToBytes(watermarkedImage, format);
            } catch (Exception e) {
                log.error("水印图片转换失败", e);
                throw new RuntimeException("图片处理失败");
            }
        } else {
            // 无法解码图片，上传原始字节（无水印）
            uploadBytes = imageBytes;
        }
        MultipartFile watermarkedFile = new ByteArrayMultipartFile(
                "file1", watermarkedFilename, file.getContentType(), uploadBytes);
        FileInfo fileInfo = fileService.uploadFile(watermarkedFile);
        String fileUrl = fileInfo.getUrl();

        // 8. OCR 识别
        String ocrText = null;
        String ocrAmount = null;
        String ocrTradeNo = null;
        String ocrMerchantNo = null;
        int ocrStatus = 0;
        try {
            ocrText = ocrService.recognizeText(new ByteArrayInputStream(imageBytes), originalFilename);
            if (ocrText != null && !ocrText.isBlank()) {
                ocrStatus = 1;
                ocrAmount = PaymentProofUtil.extractAmountFromOcrText(ocrText);
                ocrTradeNo = PaymentProofUtil.extractTradeNoFromOcrText(ocrText);
                ocrMerchantNo = PaymentProofUtil.extractMerchantNoFromOcrText(ocrText);
            } else {
                ocrStatus = 2;
            }
        } catch (Throwable e) {
            // 捕获Throwable含Error：OCR为辅助功能，任何崩溃都不应阻断凭证提交
            ocrStatus = 2;
            log.warn("OCR识别异常（不影响提交）: {}", e.toString());
        }

        // 9. 金额比对（OCR金额 vs 应付金额）
        Integer amountMatch = null;
        if (ocrAmount != null && registerUser.getPaymentAmount() != null) {
            amountMatch = PaymentProofUtil.amountsMatch(ocrAmount, registerUser.getPaymentAmount()) ? 1 : 0;
        }

        // 10. 保存凭证记录
        PaymentProof proof = new PaymentProof();
        proof.setRegistrationId(form.getRegistrationId());
        proof.setUserId(userId);
        proof.setFileUrl(fileUrl);
        proof.setFileMd5(md5);
        proof.setFilePhash(dHash);
        proof.setDeclaredPayTime(form.getDeclaredPayTime());
        proof.setDeclaredAmount(form.getDeclaredAmount());
        proof.setDeclaredTradeNo(form.getDeclaredTradeNo());
        proof.setRemark(form.getRemark());
        proof.setOcrAmount(ocrAmount);
        proof.setOcrTradeNo(ocrTradeNo);
        proof.setOcrMerchantNo(ocrMerchantNo);
        proof.setOcrRawText(ocrText);
        proof.setOcrStatus(ocrStatus);
        proof.setAmountMatch(amountMatch);
        proof.setStatus("proof_submitted");
        proof.setCreateTime(LocalDateTime.now());
        proof.setUpdateTime(LocalDateTime.now());
        paymentProofMapper.insert(proof);

        // 11. 更新注册用户凭证状态
        registerUser.setProofStatus("proof_submitted");
        registerUser.setProofSubmitTime(LocalDateTime.now());
        registerUser.setProofRejectReason(null);
        registerUser.setDeclaredPayTime(form.getDeclaredPayTime());
        registerUserInformationMapper.updateById(registerUser);

        log.info("支付凭证提交成功: registrationId={}, proofId={}, md5={}, ocrAmount={}, amountMatch={}",
                form.getRegistrationId(), proof.getId(), md5, ocrAmount, amountMatch);

        // 12. 凭证提交成功 → 通过邮件中心发送"待核对"通知邮件
        sendProofSubmittedEmail(registerUser);

        Map<String, Object> result = new HashMap<>();
        result.put("proofId", proof.getId());
        result.put("status", "proof_submitted");
        result.put("ocrAmount", ocrAmount);
        result.put("amountMatch", amountMatch);
        if (amountMatch != null && amountMatch == 0) {
            result.put("warning", "系统识别到凭证金额与应付金额可能不一致，请确认截图正确");
        }
        return result;
    }

    @Override
    public Map<String, Object> getProofStatus(Long registrationId) {
        RegisterUserInformation registerUser = registerUserInformationMapper.selectById(registrationId);
        if (registerUser == null) {
            throw new RuntimeException("注册用户不存在");
        }
        Map<String, Object> result = new HashMap<>();
        result.put("proofStatus", registerUser.getProofStatus());
        result.put("proofSubmitTime", registerUser.getProofSubmitTime());
        result.put("proofReviewTime", registerUser.getProofReviewTime());
        result.put("proofRejectReason", registerUser.getProofRejectReason());

        // 查询最近一条凭证记录
        PaymentProof latestProof = paymentProofMapper.selectOne(
                new LambdaQueryWrapper<PaymentProof>()
                        .eq(PaymentProof::getRegistrationId, registrationId)
                        .orderByDesc(PaymentProof::getCreateTime)
                        .last("LIMIT 1"));
        if (latestProof != null) {
            result.put("proofId", latestProof.getId());
            result.put("fileUrl", latestProof.getFileUrl());
            // 驳回后重传时，前端可预填上次申报的金额
            result.put("lastDeclaredAmount", latestProof.getDeclaredAmount());
        }
        return result;
    }

    @Override
    public Page<PaymentProofPageVO> getProofPage(Integer pageNum, Integer pageSize, String status, String name) {
        // 第一步：查凭证表（按状态筛选）
        LambdaQueryWrapper<PaymentProof> proofWrapper = new LambdaQueryWrapper<>();
        if (StrUtil.isNotBlank(status)) {
            proofWrapper.eq(PaymentProof::getStatus, status);
        }
        proofWrapper.orderByDesc(PaymentProof::getCreateTime);
        Page<PaymentProof> proofPage = paymentProofMapper.selectPage(
                new Page<>(pageNum, pageSize), proofWrapper);

        // 第二步：批量查关联的注册用户
        List<Long> regIds = proofPage.getRecords().stream()
                .map(PaymentProof::getRegistrationId)
                .distinct()
                .collect(Collectors.toList());
        Map<Long, RegisterUserInformation> userMap = new HashMap<>();
        if (!regIds.isEmpty()) {
            List<RegisterUserInformation> users = registerUserInformationMapper.selectBatchIds(regIds);
            userMap = users.stream().collect(Collectors.toMap(RegisterUserInformation::getId, u -> u));
        }

        // 第三步：组装 VO（按姓名过滤）
        Map<Long, RegisterUserInformation> finalUserMap = userMap;
        List<PaymentProofPageVO> voList = proofPage.getRecords().stream()
                .map(proof -> {
                    RegisterUserInformation ru = finalUserMap.get(proof.getRegistrationId());
                    if (ru == null) {
                        return null;
                    }
                    // 姓名过滤
                    if (StrUtil.isNotBlank(name)) {
                        String fullName = (ru.getSurname() != null ? ru.getSurname() : "") + (ru.getName() != null ? ru.getName() : "");
                        if (!fullName.contains(name) && !String.valueOf(ru.getEmail()).contains(name)) {
                            return null;
                        }
                    }
                    return toPageVO(proof, ru);
                })
                .filter(Objects::nonNull)
                .collect(Collectors.toList());

        Page<PaymentProofPageVO> resultPage = new Page<>(proofPage.getCurrent(), proofPage.getSize(), proofPage.getTotal());
        resultPage.setRecords(voList);
        return resultPage;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void reviewProof(ProofReviewForm form) {
        PaymentProof proof = paymentProofMapper.selectById(form.getProofId());
        if (proof == null) {
            throw new RuntimeException("凭证不存在");
        }
        if (!"proof_submitted".equals(proof.getStatus())) {
            throw new RuntimeException("该凭证已被审核，不可重复操作");
        }

        RegisterUserInformation registerUser = registerUserInformationMapper.selectById(proof.getRegistrationId());
        if (registerUser == null) {
            throw new RuntimeException("关联的注册用户不存在");
        }

        Long reviewerId = SecurityUtils.getUserId();
        String reviewerName = SecurityUtils.getUsername();

        if ("pass".equals(form.getAction())) {
            // 审核通过 → 直接确认支付成功（凭证记录标记初审通过）
            proof.setStatus("review_passed");
            registerUser.setProofStatus("paid");
            registerUser.setPaymentStatus("paid");
        } else if ("reject".equals(form.getAction())) {
            if (StrUtil.isBlank(form.getRejectReason())) {
                throw new RuntimeException("驳回必须填写原因");
            }
            proof.setStatus("rejected");
            proof.setRejectReason(form.getRejectReason());
            registerUser.setProofStatus("rejected");
            registerUser.setProofRejectReason(form.getRejectReason());
        } else {
            throw new RuntimeException("无效的审核动作: " + form.getAction());
        }

        proof.setReviewTime(LocalDateTime.now());
        proof.setReviewerId(reviewerId);
        proof.setReviewerName(reviewerName);
        proof.setUpdateTime(LocalDateTime.now());
        paymentProofMapper.updateById(proof);

        registerUser.setProofReviewTime(LocalDateTime.now());
        registerUser.setProofReviewerId(reviewerId);
        registerUserInformationMapper.updateById(registerUser);

        // 审核通过 → 通过邮件中心发送"大会注册成功"通知邮件
        if ("pass".equals(form.getAction())) {
            sendRegistrationSuccessEmail(registerUser);
        }

        log.info("凭证审核完成: proofId={}, action={}, reviewer={}", form.getProofId(), form.getAction(), reviewerName);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void offlineRegister(Long registrationId, String remark) {
        RegisterUserInformation registerUser = registerUserInformationMapper.selectById(registrationId);
        if (registerUser == null) {
            throw new RuntimeException("注册用户不存在");
        }
        if ("paid".equals(registerUser.getProofStatus()) || "offline_paid".equals(registerUser.getProofStatus())) {
            throw new RuntimeException("该报名已确认支付，无需重复操作");
        }

        Long operatorId = SecurityUtils.getUserId();
        String operatorName = SecurityUtils.getUsername();

        registerUser.setProofStatus("offline_paid");
        registerUser.setPaymentStatus("paid");
        registerUser.setProofReviewTime(LocalDateTime.now());
        registerUser.setProofReviewerId(operatorId);
        if (StrUtil.isNotBlank(remark)) {
            registerUser.setProofRejectReason(null);
        }
        registerUserInformationMapper.updateById(registerUser);

        // 线下登记确认 → 通过邮件中心发送"大会注册成功"通知邮件
        sendRegistrationSuccessEmail(registerUser);

        log.info("线下支付登记: registrationId={}, operator={}, remark={}", registrationId, operatorName, remark);
    }

    /**
     * 发送"凭证已提交待核对"通知邮件（走邮件中心，可追踪发送状态）
     */
    private void sendProofSubmittedEmail(RegisterUserInformation registerUser) {
        if (StrUtil.isBlank(registerUser.getEmail())) return;
        try {
            EmailTemplateForm templateForm = emailTemplateService.getEmailTemplateForm("注册待核对模板");
            String content = templateForm.getEmailContent()
                    .replace("{{name1}}", registerUser.getName() + " " + registerUser.getSurname());
            mailSendRecordService.createAndSend(
                    MailBusinessType.USER_REGISTER,
                    registerUser.getId(),
                    registerUser.getEmail(),
                    registerUser.getName() + " " + registerUser.getSurname(),
                    "MCLC注册凭证已提交，待核对",
                    content
            );
            log.info("凭证提交待核对通知邮件已发送：registrationId={}, email={}", registerUser.getId(), registerUser.getEmail());
        } catch (Exception e) {
            log.error("发送凭证提交待核对邮件失败：registrationId={}, error={}", registerUser.getId(), e.getMessage());
        }
    }

    /**
     * 发送"大会注册成功"通知邮件（走邮件中心，可追踪发送状态）
     */
    private void sendRegistrationSuccessEmail(RegisterUserInformation registerUser) {
        if (StrUtil.isBlank(registerUser.getEmail())) return;
        try {
            EmailTemplateForm templateForm = emailTemplateService.getEmailTemplateForm("注册通过模板");
            String content = templateForm.getEmailContent()
                    .replace("{{name1}}", registerUser.getName() + " " + registerUser.getSurname());
            mailSendRecordService.createAndSend(
                    MailBusinessType.USER_REGISTER,
                    registerUser.getId(),
                    registerUser.getEmail(),
                    registerUser.getName() + " " + registerUser.getSurname(),
                    "MCLC大会注册成功",
                    content
            );
            log.info("注册成功通知邮件已发送：registrationId={}, email={}", registerUser.getId(), registerUser.getEmail());
        } catch (Exception e) {
            log.error("发送注册成功邮件失败：registrationId={}, error={}", registerUser.getId(), e.getMessage());
        }
    }

    private PaymentProofPageVO toPageVO(PaymentProof proof, RegisterUserInformation ru) {
        PaymentProofPageVO vo = new PaymentProofPageVO();
        vo.setId(proof.getId());
        vo.setRegistrationId(proof.getRegistrationId());
        vo.setName(ru.getName());
        vo.setSurname(ru.getSurname());
        vo.setEmail(ru.getEmail());
        vo.setMobilePhone(ru.getMobilePhone());
        vo.setRegisterLabel(ru.getRegisterLabel());
        vo.setPaymentAmount(ru.getPaymentAmount());
        vo.setActualPayment(ru.getActualPayment());
        vo.setFileUrl(proof.getFileUrl());
        vo.setDeclaredPayTime(proof.getDeclaredPayTime());
        vo.setDeclaredAmount(proof.getDeclaredAmount());
        vo.setDeclaredTradeNo(proof.getDeclaredTradeNo());
        vo.setRemark(proof.getRemark());
        vo.setOcrAmount(proof.getOcrAmount());
        vo.setOcrPayee(proof.getOcrPayee());
        vo.setOcrTradeNo(proof.getOcrTradeNo());
        vo.setOcrMerchantNo(proof.getOcrMerchantNo());
        vo.setAmountMatch(proof.getAmountMatch());
        vo.setStatus(proof.getStatus());
        vo.setReviewerName(proof.getReviewerName());
        vo.setRejectReason(proof.getRejectReason());
        vo.setCreateTime(proof.getCreateTime());
        vo.setReviewTime(proof.getReviewTime());
        return vo;
    }

    /**
     * 根据注册类型计算应付金额
     * <p>register_user_information.registration_fee 存的是 registration_type 表的主键ID；
     * 按当前日期与常规有效期比较：未过期取 normal_amount，超过常规有效期取 deadline_amount</p>
     *
     * @param registerUser 注册用户
     * @return 应付金额（已去千分位逗号，如"1000"），无法计算返回null
     */
    private String calculatePaymentAmount(RegisterUserInformation registerUser) {
        String typeId = registerUser.getRegistrationFee();
        if (typeId == null || typeId.isBlank()) {
            return null;
        }
        try {
            RegistrationType type = registrationTypeMapper.selectById(Long.parseLong(typeId.trim()));
            if (type == null) {
                return null;
            }
            LocalDateTime now = LocalDateTime.now();
            String amount;
            if (type.getNormalValidDate() != null && now.isAfter(type.getNormalValidDate())) {
                // 超过常规有效期，取截止金额
                amount = type.getDeadlineAmount();
            } else {
                // 常规期内，取常规金额
                amount = type.getNormalAmount();
            }
            if (amount == null || amount.isBlank()) {
                return null;
            }
            // 去千分位逗号（"1,000" → "1000"）
            return amount.replace(",", "").trim();
        } catch (NumberFormatException e) {
            log.warn("registration_fee 不是数字类型ID，无法计算应付金额: {}", typeId);
            return null;
        }
    }

    private boolean isImageFile(String filename) {
        String lower = filename.toLowerCase();
        return lower.endsWith(".jpg") || lower.endsWith(".jpeg") || lower.endsWith(".png");
    }

    /**
     * 字节数组转十六进制字符串（用于输出文件头魔数诊断图片实际格式）
     */
    private static String bytesToHex(byte[] bytes, int limit) {
        StringBuilder sb = new StringBuilder();
        int len = Math.min(bytes.length, limit);
        for (int i = 0; i < len; i++) {
            sb.append(String.format("%02X", bytes[i]));
            if (i < len - 1) sb.append(" ");
        }
        return sb.toString();
    }
}