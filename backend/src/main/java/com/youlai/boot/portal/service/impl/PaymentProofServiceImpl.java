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
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
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
    private final FileService fileService;
    private final OcrService ocrService;

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

        // 2. 校验文件格式
        String originalFilename = file.getOriginalFilename();
        if (originalFilename == null || !isImageFile(originalFilename)) {
            throw new RuntimeException("仅支持上传图片文件（jpg/png/jpeg）");
        }
        // 文件大小限制 10MB
        if (file.getSize() > 10 * 1024 * 1024) {
            throw new RuntimeException("图片大小不能超过10MB");
        }

        // 3. 读取图片
        byte[] imageBytes;
        BufferedImage originalImage;
        try {
            imageBytes = file.getBytes();
            originalImage = ImageIO.read(file.getInputStream());
            if (originalImage == null) {
                throw new RuntimeException("图片文件损坏或格式不支持");
            }
        } catch (Exception e) {
            log.error("读取凭证图片失败", e);
            throw new RuntimeException("读取图片失败: " + e.getMessage());
        }

        // 4. MD5 精确去重
        String md5 = PaymentProofUtil.calculateMd5(imageBytes);
        Long md5DuplicateCount = paymentProofMapper.selectCount(
                new LambdaQueryWrapper<PaymentProof>().eq(PaymentProof::getFileMd5, md5));
        if (md5DuplicateCount > 0) {
            throw new RuntimeException("该支付凭证已被提交，请勿重复上传同一张截图");
        }

        // 5. dHash 感知哈希去重（防裁剪/压缩重复）
        String dHash = PaymentProofUtil.calculateDHash(originalImage);
        List<PaymentProof> allProofs = paymentProofMapper.selectList(
                new LambdaQueryWrapper<PaymentProof>().isNotNull(PaymentProof::getFilePhash));
        for (PaymentProof existing : allProofs) {
            int distance = PaymentProofUtil.hammingDistance(dHash, existing.getFilePhash());
            if (distance < DHASH_DISTANCE_THRESHOLD) {
                log.warn("感知哈希去重拦截: 新凭证dHash={}, 与凭证id={}的dHash={} 汉明距离={}",
                        dHash, existing.getId(), existing.getFilePhash(), distance);
                throw new RuntimeException("该支付凭证与已提交的凭证高度相似，请勿重复使用同一张截图");
            }
        }

        // 6. 后端加水印
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));
        Long userId = null;
        try {
            userId = SecurityUtils.getUserId();
        } catch (Exception ignored) {
            // 门户用户可能未登录系统，userId 留空
        }
        String watermarkText = StrUtil.format("MCLC-REG{}-u{}-{}", form.getRegistrationId(), userId != null ? userId : "anon", timestamp);
        BufferedImage watermarkedImage = PaymentProofUtil.addWatermark(originalImage, watermarkText);

        // 7. 上传带水印图片
        String format = originalFilename.toLowerCase().endsWith(".png") ? "png" : "jpg";
        byte[] watermarkedBytes;
        try {
            watermarkedBytes = PaymentProofUtil.imageToBytes(watermarkedImage, format);
        } catch (Exception e) {
            log.error("水印图片转换失败", e);
            throw new RuntimeException("图片处理失败");
        }
        String watermarkedFilename = "proof_" + form.getRegistrationId() + "_" + timestamp + "." + format;
        MultipartFile watermarkedFile = new ByteArrayMultipartFile(
                "file1", watermarkedFilename, file.getContentType(), watermarkedBytes);
        FileInfo fileInfo = fileService.uploadFile(watermarkedFile);
        String fileUrl = fileInfo.getUrl();

        // 8. OCR 识别
        String ocrText = null;
        String ocrAmount = null;
        int ocrStatus = 0;
        try {
            ocrText = ocrService.recognizeText(file.getInputStream(), originalFilename);
            if (ocrText != null && !ocrText.isBlank()) {
                ocrStatus = 1;
                ocrAmount = PaymentProofUtil.extractAmountFromOcrText(ocrText);
            } else {
                ocrStatus = 2;
            }
        } catch (Exception e) {
            ocrStatus = 2;
            log.warn("OCR识别异常（不影响提交）: {}", e.getMessage());
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
            // 初审通过 → 待对账确认
            proof.setStatus("review_passed");
            registerUser.setProofStatus("review_passed");
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

        log.info("线下支付登记: registrationId={}, operator={}, remark={}", registrationId, operatorName, remark);
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
        vo.setAmountMatch(proof.getAmountMatch());
        vo.setStatus(proof.getStatus());
        vo.setReviewerName(proof.getReviewerName());
        vo.setRejectReason(proof.getRejectReason());
        vo.setCreateTime(proof.getCreateTime());
        vo.setReviewTime(proof.getReviewTime());
        return vo;
    }

    private boolean isImageFile(String filename) {
        String lower = filename.toLowerCase();
        return lower.endsWith(".jpg") || lower.endsWith(".jpeg") || lower.endsWith(".png");
    }
}