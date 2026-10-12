package com.youlai.boot.portal.service.impl;

import cn.hutool.core.lang.Assert;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.youlai.boot.common.constant.RedisConstants;
import com.youlai.boot.core.exception.BusinessException;
import com.youlai.boot.portal.converter.PaperSubmissionConverter;
import com.youlai.boot.portal.mapper.PaperSubmissionEmailMapper;
import com.youlai.boot.portal.mapper.PaperSubmissionMapper;
import com.youlai.boot.portal.model.entity.PaperSubmission;
import com.youlai.boot.portal.model.entity.PaperSubmissionEmail;
import com.youlai.boot.portal.model.form.PaperSubmissionForm;
import com.youlai.boot.portal.model.form.PaperSubmissionReviewForm;
import com.youlai.boot.portal.model.form.PaperSubmissionUpdateForm;
import com.youlai.boot.portal.model.query.PaperSubmissionQuery;
import com.youlai.boot.portal.model.vo.EmailSendResultVO;
import com.youlai.boot.portal.model.vo.PaperSubmissionPageVO;
import com.youlai.boot.portal.model.vo.PaperSubmissionQueryVO;
import com.youlai.boot.portal.service.PaperSubmissionService;
import com.youlai.boot.support.mail.enums.MailBusinessType;
import com.youlai.boot.support.mail.model.entity.MailSendRecord;
import com.youlai.boot.support.mail.service.MailSendRecordService;
import com.youlai.boot.support.mail.service.MailService;
import com.youlai.boot.system.model.form.EmailTemplateForm;
import com.youlai.boot.system.service.EmailTemplateService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;
import java.util.concurrent.TimeUnit;

/**
 * 论文投稿业务实现类（797行）
 *
 * 核心业务流程：
 * 1. 论文投稿CRUD（新增、删除、分页查询、列表查询）
 * 2. 发送审稿邮件 — updatePaperSubmissionStatus():182
 *    有附件→模板替换({{name1}},{{name2}},{{论文稿件}},{{审批论文稿件}})
 *    无附件→HTML表格+正则替换&lt;a&gt;标签为纯文本
 * 3. 专家审核 — expertReview():312
 *    更新review_result → 全部审核完 → 计算通过率 → ≥50%通过→paperStatus=2+生成8位抵扣码
 * 4. 重发审稿邮件 — resendReviewEmail():467（基于已有邮件记录重新发送）
 * 5. 投稿者公开接口 — sendQueryCode():635 / queryByEmail():673 / getDetailByEmail():717 / updateBySubmitter():761
 *    邮箱验证码鉴权(Redis 5分钟) + LOWER邮箱匹配 + 脱敏VO + 修改限制
 * 6. PDF导出在Controller层处理，Service层提供PaperPdfGeneratorService
 *
 * 依赖注入(7个)：
 * - paperSubmissionConverter, mailService, paperSubmissionEmailMapper
 * - emailTemplateService, mailSendRecordService, redisTemplate
 * - confirmReview(@Value("${confirm-review.url}"))
 *
 * 已知问题：CODE-012(抵扣码唯一性) / CODE-015(验证码Math.random) / SEC-012(审核接口无认证)
 *
 * 技术文档详见：docs/10-paper-submission.md
 *
 * @author 243269112
 * @since 2026/5/6
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class PaperSubmissionServiceImpl extends ServiceImpl<PaperSubmissionMapper, PaperSubmission> implements PaperSubmissionService {

    /** 论文投稿对象转换器，用于 Form ↔ Entity ↔ VO 之间的转换 */
    private final PaperSubmissionConverter paperSubmissionConverter;

    /** 邮件发送服务，提供同步/异步发送HTML邮件的能力 */
    private final MailService mailService;

    /** 论文投稿邮件记录Mapper，操作 paper_submission_email 表 */
    private final PaperSubmissionEmailMapper paperSubmissionEmailMapper;

    /** 邮件模板服务，根据模板类型获取邮件模板内容 */
    private final EmailTemplateService emailTemplateService;

    /** 邮件发送记录服务，任务表模式：先落库再发送，记录完整发送历史 */
    private final MailSendRecordService mailSendRecordService;

    /** Redis模板，用于验证码存取 */
    private final RedisTemplate<String, Object> redisTemplate;

    /**
     * 管理员邮箱，用于接收邮件发送失败等异常通知
     */
    private static final String ADMIN_EMAIL = "maojiangjun@foxmail.com";

    /**
     * 专家审批链接前缀，配置在 application-dev.yml / application-prod.yml
     * 例如：https://myopiacontactlens.com/review-file?id=
     * 拼接邮件记录ID后形成完整审批链接
     */
    @Value("${confirm-review}")
    private String confirmReview;

    /**
     * 【功能1】论文投稿分页查询
     *
     * 流程：前端传入查询参数 → 调用Mapper自定义SQL分页查询 → 返回分页结果
     *
     * @param queryParams 查询参数（分页、筛选条件等）
     * @return 分页结果
     */
    @Override
    public Page<PaperSubmissionPageVO> getPaperSubmissionPage(PaperSubmissionQuery queryParams) {
        int pageNum = queryParams.getPageNum();
        int pageSize = queryParams.getPageSize();

        Page<PaperSubmissionPageVO> page = new Page<>(pageNum, pageSize);
        return baseMapper.selectPaperSubmissionPage(page, queryParams);
    }

    /**
     * 【功能2】新增论文投稿
     *
     * 流程：前端提交表单 → Form转Entity → 保存到数据库
     *
     * @param paperSubmissionForm 论文投稿表单
     * @return 是否保存成功
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean savePaperSubmission(PaperSubmissionForm paperSubmissionForm) {
        PaperSubmission paperSubmission = paperSubmissionConverter.toEntity(paperSubmissionForm);
        return this.save(paperSubmission);
    }

    /**
     * 【功能3】删除论文投稿（支持批量删除）
     *
     * 流程：前端传入逗号分隔的ID字符串 → 逐个校验存在性 → 逐个删除
     *
     * @param ids 逗号分隔的论文投稿ID，如 "1,2,3"
     */
    @Override
    public void deletePaperSubmissions(String ids) {
        Assert.isTrue(StrUtil.isNotBlank(ids), "删除的论文投稿ID不能为空");
        List<Long> idList = Arrays.stream(ids.split(","))
            .map(Long::parseLong)
            .toList();

        for (Long id : idList) {
            PaperSubmission paperSubmission = this.getById(id);
            Assert.isTrue(paperSubmission != null, "论文投稿不存在");
            this.removeById(id);
        }
    }

    /**
     * 【功能4】获取论文投稿列表（不分页）
     *
     * @param queryParams 查询参数
     * @return 论文投稿VO列表
     */
    @Override
    public List<PaperSubmissionPageVO> getPaperSubmissionList(PaperSubmissionQuery queryParams) {
        List<PaperSubmission> list = this.list(
            new LambdaQueryWrapper<PaperSubmission>()
                .orderByDesc(PaperSubmission::getId)
        );

        return paperSubmissionConverter.toPageVo(list);
    }

    /**
     * 【功能5】发送审稿邮件（核心业务）
     *
     * 完整流程：
     * 1. 校验论文投稿是否存在
     * 2. 检查是否已发送过邮件（防止重复发送）
     * 3. 更新论文投稿的专家链接和邮件状态
     * 4. 遍历专家列表，为每个专家：
     *    a. 创建邮件发送记录（paper_submission_email表）
     *    b. 构建邮件内容：
     *       - 有附件：使用邮件模板替换占位符（{{name1}}、{{name2}}、{{论文稿件}}、{{审批论文稿件}}）
     *       - 无附件：直接构建完整邮件HTML（问候语 + 投稿者信息表格 + 审批链接）
     *    c. 同步发送HTML邮件
     *    d. 更新邮件发送状态（1-发送中 → 2-成功 / 3-失败）
     * 5. 更新论文投稿的邮件发送状态为"已发送"
     *
     * @param id   论文投稿ID
     * @param form 审核表单（包含专家列表、附件链接、发送人姓名）
     * @return 每个专家的发送结果列表
     */
    @Override
    public List<EmailSendResultVO> updatePaperSubmissionStatus(Long id, PaperSubmissionReviewForm form) {
        // 步骤1：校验论文投稿是否存在
        PaperSubmission paperSubmission = this.getById(id);
        if (paperSubmission == null) {
            throw new BusinessException("论文投稿不存在");
        }

        // 步骤2：检查是否已有邮件记录，防止重复发送
        long existingCount = paperSubmissionEmailMapper.selectCount(
            new LambdaQueryWrapper<PaperSubmissionEmail>()
                .eq(PaperSubmissionEmail::getSubmissionId, id)
        );
        if (existingCount > 0) {
            throw new BusinessException("该投稿已发送过审稿邮件，如需重新发送请使用重发邮件功能");
        }

        // 步骤3：更新论文投稿的专家链接和邮件状态
        paperSubmission.setExpertUrl(form.getUrl());
        paperSubmission.setEmailStatus(0);  // 0-未发送
        boolean result = this.updateById(paperSubmission);

        List<EmailSendResultVO> sendResults = new ArrayList<>();

        if (result) {
            // 步骤4：获取专家列表和邮件模板
            List<PaperSubmissionReviewForm.ExpertEmailItem> emailsList = form.getEmailsList();
            String subject = "论文投稿评审通知";

            // 获取邮件模板（仅在有附件时使用）
            EmailTemplateForm templateForm = emailTemplateService.getEmailTemplateForm("专家审核模板");
            String templateContent = templateForm.getEmailContent();

            // 遍历每个专家，逐个发送邮件
            for (PaperSubmissionReviewForm.ExpertEmailItem item : emailsList) {
                String expertName = item.getName();
                String expertEmail = item.getEmail();

                // 步骤4a：创建邮件发送记录
                PaperSubmissionEmail emailRecord = new PaperSubmissionEmail();
                emailRecord.setSubmissionId(id);
                emailRecord.setExpertName(expertName);
                emailRecord.setExpertEmail(expertEmail);
                emailRecord.setUrl(form.getUrl());           // 附件链接（可为空）
                emailRecord.setSenderName(form.getSenderName());
                emailRecord.setSendStatus(1);                 // 1-发送中
                emailRecord.setSendTime(LocalDateTime.now());
                paperSubmissionEmailMapper.insert(emailRecord);

                // 步骤4b：构建邮件内容
                String content;
                if (StrUtil.isNotBlank(form.getUrl())) {
                    // 有附件 → 使用模板，{{论文稿件}}替换为附件URL
                    content = templateContent
                            .replace("{{name1}}", expertName)
                            .replace("{{name2}}", form.getSenderName())
                            .replace("{{论文稿件}}", form.getUrl())
                            .replace("{{审批论文稿件}}", confirmReview + emailRecord.getId());
                } else {
                    // 无附件 → 使用模板，但将{{论文稿件}}所在的<a>标签替换为投稿者信息表格
                    String submissionInfoHtml = buildSubmissionInfoTable(paperSubmission);
                    content = templateContent
                            .replace("{{name1}}", expertName)
                            .replace("{{name2}}", form.getSenderName())
                            // 正则替换：把包含{{论文稿件}}的<a>标签整体替换为信息表格
                            .replaceAll("<a[^>]*\\{\\{论文稿件\\}\\}[^>]*>[^<]*</a>", submissionInfoHtml)
                            // 兜底：如果模板中{{论文稿件}}不在<a>标签内，直接替换
                            .replace("{{论文稿件}}", submissionInfoHtml)
                            .replace("{{审批论文稿件}}", confirmReview + emailRecord.getId());
                }

                // 步骤4c：通过MailSendRecordService发送邮件（任务表模式：先落库再发送）
                EmailSendResultVO sendResult = new EmailSendResultVO();
                sendResult.setExpertName(expertName);
                sendResult.setExpertEmail(expertEmail);

                MailSendRecord mailRecord = mailSendRecordService.createAndSend(
                        MailBusinessType.PAPER_SUBMIT, id,
                        expertEmail, expertName, subject, content);
                sendResult.setSuccess(mailRecord.getSendStatus() == com.youlai.boot.support.mail.enums.MailSendStatus.SUCCESS);
                sendResults.add(sendResult);

                // 步骤4d：更新paper_submission_email的发送状态
                if (mailRecord.getSendStatus() == com.youlai.boot.support.mail.enums.MailSendStatus.SUCCESS) {
                    emailRecord.setSendStatus(2);  // 2-发送成功
                } else {
                    emailRecord.setSendStatus(3);  // 3-发送失败
                    emailRecord.setSendErrorMsg(mailRecord.getErrorMessage());
                    // 异步通知管理员邮件发送失败
                    sendAdminErrorNotification("审稿邮件发送失败",
                            "投稿ID=" + id + "，专家=" + expertName + "，邮箱=" + expertEmail
                                    + "，错误=" + mailRecord.getErrorMessage());
                }
                paperSubmissionEmailMapper.updateById(emailRecord);
            }

            // 步骤5：根据实际发送结果更新论文投稿的邮件状态
            // 检查该投稿下所有邮件记录的发送状态
            boolean anyFailed = sendResults.stream().anyMatch(r -> !r.isSuccess());
            boolean allSuccess = sendResults.stream().allMatch(EmailSendResultVO::isSuccess);
            if (allSuccess) {
                paperSubmission.setEmailStatus(2);  // 2-全部发送成功
            } else if (anyFailed) {
                paperSubmission.setEmailStatus(1);  // 1-部分或全部发送失败
            }
            this.updateById(paperSubmission);
        }

        return sendResults;
    }

    /**
     * 【功能6】专家审核
     *
     * 完整流程：
     * 1. 校验邮件记录是否存在
     * 2. 防止重复审核（reviewResult != 0 表示已审核）
     * 3. 更新审核结果和审核时间
     * 4. 检查该投稿下所有专家是否都已审核完毕
     * 5. 如果全部审核完毕：
     *    a. 计算通过率（通过数/总数 >= 50% → 审核通过，否则拒绝）
     *    b. 审核通过：生成8位随机抵扣码，设置paperStatus=2
     *    c. 审核拒绝：设置paperStatus=3
     *    d. 通知投稿人审核结果（通过/未通过邮件模板）
     *
     * @param id           邮件发送记录ID（paper_submission_email表的主键）
     * @param reviewResult 审核结果（1-通过 2-未通过）
     * @param rejectReason 拒绝原因（未通过时填写）
     * @return true
     */
    @Override
    public boolean expertReview(Long id, Integer reviewResult, String rejectReason) {
        // 步骤1：校验邮件记录
        PaperSubmissionEmail emailRecord = paperSubmissionEmailMapper.selectById(id);
        if (emailRecord == null) {
            throw new BusinessException("未找到该发送记录");
        }

        // 步骤2：防止重复审核
        if (emailRecord.getReviewResult() != null && emailRecord.getReviewResult() != 0) {
            throw new BusinessException("该记录已审核，不允许重复操作。审核结果：" +
                    (emailRecord.getReviewResult() == 1 ? "已通过" : "未通过"));
        }

        // 步骤3：更新审核结果
        Long submissionId = emailRecord.getSubmissionId();
        emailRecord.setReviewResult(reviewResult);
        emailRecord.setRejectReason(rejectReason);
        emailRecord.setReviewTime(LocalDateTime.now());
        paperSubmissionEmailMapper.updateById(emailRecord);

        // 步骤4：检查该投稿下所有专家是否都已审核
        List<PaperSubmissionEmail> allRecords = paperSubmissionEmailMapper.selectList(
            new LambdaQueryWrapper<PaperSubmissionEmail>()
                .eq(PaperSubmissionEmail::getSubmissionId, submissionId)
        );

        boolean allReviewed = allRecords.stream()
            .allMatch(r -> r.getReviewResult() != null && r.getReviewResult() != 0);

        // 步骤5：全部审核完毕 → 计算结果并通知投稿人
        if (allReviewed) {
            // 计算通过数
            long passedCount = allRecords.stream()
                .filter(r -> r.getReviewResult() == 1)
                .count();
            int total = allRecords.size();
            PaperSubmission paperSubmission = this.getById(submissionId);

            if (passedCount * 1.0 / total >= 0.5) {
                // 审核通过：生成8位随机抵扣码
                paperSubmission.setPaperStatus(2);  // 2-审核通过
                String chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
                Random random = new Random();
                StringBuilder code = new StringBuilder(8);
                for (int j = 0; j < 8; j++) {
                    code.append(chars.charAt(random.nextInt(chars.length())));
                }
                paperSubmission.setDiscountCode(code.toString());
            } else {
                // 审核拒绝
                paperSubmission.setPaperStatus(3);  // 3-审核拒绝
            }
            this.updateById(paperSubmission);

            // 通知投稿人审核结果（通过MailSendRecordService发送，记录完整发送历史）
            if (StrUtil.isNotBlank(paperSubmission.getContactEmail())) {
                Integer newStatus = paperSubmission.getPaperStatus();
                if (newStatus != null && newStatus != 1) {
                    try {
                        String subject;
                        String emailType;
                        MailBusinessType businessType;
                        if (newStatus == 2) {
                            subject = "论文投稿审核通过通知";
                            emailType = "论文审核通过模板";
                            businessType = MailBusinessType.PAPER_PASS;
                        } else {
                            subject = "论文投稿审核未通过通知";
                            emailType = "论文审核未通过模板";
                            businessType = MailBusinessType.PAPER_REJECT;
                        }
                        EmailTemplateForm templateForm = emailTemplateService.getEmailTemplateForm(emailType);
                        String content = templateForm.getEmailContent()
                                .replace("{{name1}}", paperSubmission.getName())
                                .replace("{{name2}}", paperSubmission.getPaperTitle())
                                .replace("{{code}}", paperSubmission.getDiscountCode() != null ? paperSubmission.getDiscountCode() : "");

                        // 通过MailSendRecordService发送，自动记录到mail_send_record表
                        MailSendRecord mailRecord = mailSendRecordService.createAndSend(
                                businessType, submissionId,
                                paperSubmission.getContactEmail(), paperSubmission.getName(),
                                subject, content);

                        if (mailRecord.getSendStatus() == com.youlai.boot.support.mail.enums.MailSendStatus.SUCCESS) {
                            log.info("审核结果通知邮件已发送：投稿ID={}, 状态={}, 收件人={}", submissionId, newStatus == 2 ? "通过" : "未通过", paperSubmission.getContactEmail());
                        } else {
                            log.error("审核结果通知邮件发送失败：投稿ID={}, 收件人={}, 错误={}", submissionId, paperSubmission.getContactEmail(), mailRecord.getErrorMessage());
                            // 异步通知管理员审核结果邮件发送失败
                            sendAdminErrorNotification("审核结果通知邮件发送失败",
                                    "投稿ID=" + submissionId + "，收件人=" + paperSubmission.getContactEmail()
                                            + "，审核结果=" + (newStatus == 2 ? "通过" : "未通过")
                                            + "，错误=" + mailRecord.getErrorMessage());
                        }
                    } catch (Exception e) {
                        // 模板获取等异常不影响审核结果，只记录日志
                        log.error("审核结果通知邮件发送失败：投稿ID={}, 收件人={}, 错误={}", submissionId, paperSubmission.getContactEmail(), e.getMessage());
                        sendAdminErrorNotification("审核结果通知邮件发送失败",
                                "投稿ID=" + submissionId + "，收件人=" + paperSubmission.getContactEmail()
                                        + "，错误=" + e.getMessage());
                    }
                }
            }
        }

        return true;
    }

    /**
     * 【功能7】查询同投稿的所有专家审核记录
     *
     * @param id 论文投稿ID
     * @return 该投稿下所有邮件发送记录列表
     */
    @Override
    public List<PaperSubmissionEmail> getExpertReviewList(Long id) {
        return paperSubmissionEmailMapper.selectList(
            new LambdaQueryWrapper<PaperSubmissionEmail>()
                .eq(PaperSubmissionEmail::getSubmissionId, id)
        );
    }

    /**
     * 【功能8】查询单条邮件发送记录
     *
     * @param emailId 邮件记录ID
     * @return 邮件发送记录
     */
    @Override
    public PaperSubmissionEmail getEmailRecord(Long emailId) {
        PaperSubmissionEmail record = paperSubmissionEmailMapper.selectById(emailId);
        if (record == null) {
            throw new BusinessException("未找到该邮件发送记录");
        }
        return record;
    }

    /**
     * 【功能9】重发审稿邮件
     *
     * 完整流程：
     * 1. 校验邮件记录是否存在
     * 2. 获取关联的论文投稿信息
     * 3. 构建邮件内容（与首次发送逻辑相同：有附件走模板，无附件直接构建）
     * 4. 同步发送邮件
     * 5. 更新邮件发送状态和时间
     * 6. 同步更新论文投稿的邮件状态（取所有邮件记录中最差的状态）
     *
     * 与首次发送的区别：
     * - 不创建新的邮件记录，只更新已有记录
     * - 不检查重复发送
     *
     * @param emailId 邮件记录ID
     * @return 发送结果
     */
    @Override
    public EmailSendResultVO resendReviewEmail(Long emailId) {
        // 步骤1：校验邮件记录
        PaperSubmissionEmail emailRecord = paperSubmissionEmailMapper.selectById(emailId);
        if (emailRecord == null) {
            throw new BusinessException("未找到该邮件发送记录");
        }

        // 步骤2：获取关联的论文投稿信息（用于无附件时构建邮件内容）
        PaperSubmission paperSubmission = this.getById(emailRecord.getSubmissionId());
        if (paperSubmission == null) {
            throw new BusinessException("关联的论文投稿不存在");
        }

        // 步骤3：构建邮件内容
        String subject = "论文投稿评审通知";
        EmailTemplateForm templateForm = emailTemplateService.getEmailTemplateForm("专家审核模板");

        // 构建邮件内容：有附件时用模板替换，无附件时用模板+替换<a>标签
        String content;
        if (StrUtil.isNotBlank(emailRecord.getUrl())) {
            // 有附件 → 使用模板
            content = templateForm.getEmailContent()
                    .replace("{{name1}}", emailRecord.getExpertName())
                    .replace("{{name2}}", emailRecord.getSenderName())
                    .replace("{{论文稿件}}", emailRecord.getUrl())
                    .replace("{{审批论文稿件}}", confirmReview + emailRecord.getId());
        } else {
            // 无附件 → 使用模板，但将{{论文稿件}}所在的<a>标签替换为投稿者信息表格
            String submissionInfoHtml = buildSubmissionInfoTable(paperSubmission);
            content = templateForm.getEmailContent()
                    .replace("{{name1}}", emailRecord.getExpertName())
                    .replace("{{name2}}", emailRecord.getSenderName())
                    // 正则替换：把包含{{论文稿件}}的<a>标签整体替换为信息表格
                    .replaceAll("<a[^>]*\\{\\{论文稿件\\}\\}[^>]*>[^<]*</a>", submissionInfoHtml)
                    // 兜底：如果模板中{{论文稿件}}不在<a>标签内，直接替换
                    .replace("{{论文稿件}}", submissionInfoHtml)
                    .replace("{{审批论文稿件}}", confirmReview + emailRecord.getId());
        }

        // 步骤4：通过MailSendRecordService发送邮件（任务表模式）
        MailSendRecord mailRecord = mailSendRecordService.createAndSend(
                MailBusinessType.PAPER_SUBMIT, emailRecord.getSubmissionId(),
                emailRecord.getExpertEmail(), emailRecord.getExpertName(), subject, content);

        // 步骤5：更新paper_submission_email的发送状态和时间
        emailRecord.setSendTime(LocalDateTime.now());
        if (mailRecord.getSendStatus() == com.youlai.boot.support.mail.enums.MailSendStatus.SUCCESS) {
            emailRecord.setSendStatus(2);  // 2-发送成功
            emailRecord.setSendErrorMsg(null);  // 清除之前的错误信息
        } else {
            emailRecord.setSendStatus(3);  // 3-发送失败
            emailRecord.setSendErrorMsg(mailRecord.getErrorMessage());
            // 异步通知管理员邮件发送失败
            sendAdminErrorNotification("重发审稿邮件失败",
                    "邮件记录ID=" + emailId + "，专家=" + emailRecord.getExpertName()
                            + "，邮箱=" + emailRecord.getExpertEmail()
                            + "，错误=" + mailRecord.getErrorMessage());
        }
        paperSubmissionEmailMapper.updateById(emailRecord);

        // 步骤6：同步更新论文投稿的邮件状态
        // 检查该投稿下所有邮件记录的发送状态，取最差的状态
        List<PaperSubmissionEmail> allRecords = paperSubmissionEmailMapper.selectList(
            new LambdaQueryWrapper<PaperSubmissionEmail>()
                .eq(PaperSubmissionEmail::getSubmissionId, emailRecord.getSubmissionId())
        );
        boolean anyFailed = allRecords.stream().anyMatch(r -> r.getSendStatus() == 3);
        boolean allSuccess = allRecords.stream().allMatch(r -> r.getSendStatus() == 2);
        if (allSuccess) {
            paperSubmission.setEmailStatus(2);  // 全部发送成功
        } else if (anyFailed) {
            paperSubmission.setEmailStatus(1);  // 有发送失败
        }
        this.updateById(paperSubmission);

        EmailSendResultVO result = new EmailSendResultVO();
        result.setExpertName(emailRecord.getExpertName());
        result.setExpertEmail(emailRecord.getExpertEmail());
        result.setSuccess(mailRecord.getSendStatus() == com.youlai.boot.support.mail.enums.MailSendStatus.SUCCESS);
        return result;
    }

    /**
     * 【私有方法】构建投稿者信息表格HTML（用于替换模板中的{{论文稿件}}占位符）
     *
     * 当论文没有附件时，用投稿者信息表格替换模板中{{论文稿件}}所在的<a>标签。
     * 表格包含：投稿人姓名、论文标题、论文摘要、联系电话、联系邮箱。
     *
     * @param paper 论文投稿实体
     * @return 信息表格HTML字符串
     */
    private String buildSubmissionInfoTable(PaperSubmission paper) {
        StringBuilder sb = new StringBuilder();
        sb.append("<table style=\"border-collapse:collapse;width:100%;margin:10px 0;\">");
        sb.append(buildTableRow("投稿人姓名", paper.getName()));
        sb.append(buildTableRow("论文标题", paper.getPaperTitle()));
        sb.append(buildTableRow("论文摘要", paper.getPaperAbstract()));
        sb.append(buildTableRow("联系电话", paper.getContactNumber()));
        sb.append(buildTableRow("联系邮箱", paper.getContactEmail()));
        sb.append("</table>");
        return sb.toString();
    }

    /**
     * 【私有方法】构建信息表格行
     *
     * 生成格式：<tr><td>标签</td><td>值</td></tr>
     * 标签列：灰色背景、加粗、居中、宽120px
     * 值列：正常显示
     *
     * @param label 标签名（如"投稿人姓名"）
     * @param value 值（如"张三"），null时显示空字符串
     * @return HTML表格行字符串
     */
    private String buildTableRow(String label, String value) {
        return "<tr>"
                + "<td style=\"border:1px solid #ddd;padding:10px;background:#f5f5f5;font-weight:bold;width:120px;\">" + label + "</td>"
                + "<td style=\"border:1px solid #ddd;padding:10px;\">" + (value != null ? value : "") + "</td>"
                + "</tr>";
    }

    /**
     * 【私有方法】异步发送错误通知邮件给管理员
     *
     * 当邮件发送失败时，通过此方法异步通知管理员，便于及时发现问题。
     * 通知邮件发送到 ADMIN_EMAIL（maojiangjun@foxmail.com）。
     * 即使通知邮件本身发送失败也不影响主流程，仅记录日志。
     *
     * @param errorType 错误类型（如"审稿邮件发送失败"、"审核结果通知邮件发送失败"）
     * @param detail    错误详情（包含投稿ID、专家信息、收件人等）
     */
    private void sendAdminErrorNotification(String errorType, String detail) {
        try {
            String htmlContent = "<div style=\"font-family:Arial,sans-serif;padding:20px;\">"
                    + "<h2 style=\"color:#e74c3c;\">⚠️ 邮件发送异常通知</h2>"
                    + "<table style=\"border-collapse:collapse;width:100%;margin:10px 0;\">"
                    + "<tr><td style=\"border:1px solid #ddd;padding:10px;background:#f5f5f5;font-weight:bold;width:120px;\">异常类型</td>"
                    + "<td style=\"border:1px solid #ddd;padding:10px;color:#e74c3c;\">" + errorType + "</td></tr>"
                    + "<tr><td style=\"border:1px solid #ddd;padding:10px;background:#f5f5f5;font-weight:bold;\">详细信息</td>"
                    + "<td style=\"border:1px solid #ddd;padding:10px;\">" + detail + "</td></tr>"
                    + "<tr><td style=\"border:1px solid #ddd;padding:10px;background:#f5f5f5;font-weight:bold;\">发生时间</td>"
                    + "<td style=\"border:1px solid #ddd;padding:10px;\">" + LocalDateTime.now() + "</td></tr>"
                    + "</table>"
                    + "<p style=\"color:#999;font-size:12px;\">此邮件由系统自动发送，请及时处理。</p>"
                    + "</div>";
            mailService.sendHtmlMail(ADMIN_EMAIL, "【MCLC系统】" + errorType, htmlContent);
            log.info("管理员错误通知邮件已发送：{}", errorType);
        } catch (Exception ex) {
            // 通知邮件发送失败不影响主流程，仅记录日志
            log.error("管理员错误通知邮件发送失败：{}", ex.getMessage());
        }
    }

    // ==================== 投稿者公开接口实现（无需登录，邮箱验证码鉴权） ====================

    /**
     * 【公开接口1】发送投稿查询验证码
     *
     * 流程：
     * 1. 校验邮箱格式
     * 2. 检查该邮箱是否有投稿记录
     * 3. 生成6位随机验证码
     * 4. 发送验证码邮件
     * 5. 存入Redis（5分钟有效）
     *
     * @param email 投稿者邮箱
     */
    @Override
    public void sendQueryCode(String email) {
        String emailTrimmed = email.trim().toLowerCase();
        Assert.isTrue(StrUtil.isNotBlank(emailTrimmed), "邮箱不能为空");

        // 检查该邮箱是否有投稿记录（邮箱不区分大小写）
        long count = this.count(new LambdaQueryWrapper<PaperSubmission>()
                .apply("LOWER(contact_email) = {0}", emailTrimmed));
        if (count == 0) {
                        throw new BusinessException("未找到您的投稿记录，请检查邮箱是否输入正确");
        }

        // 生成6位随机验证码
        String code = String.valueOf((int) ((Math.random() * 9 + 1) * 100000));

        // 发送验证码邮件
        mailService.sendMail(emailTrimmed, "投稿查询验证码",
                "您的投稿查询验证码为：" + code + "，请在5分钟内使用。如非本人操作，请忽略此邮件。");

        // 存入Redis，5分钟有效
        String redisCacheKey = StrUtil.format(RedisConstants.Captcha.PAPER_QUERY_CODE, emailTrimmed);
        redisTemplate.opsForValue().set(redisCacheKey, code, 5, TimeUnit.MINUTES);

        log.info("投稿查询验证码已发送至邮箱：{}", emailTrimmed);
    }

    /**
     * 【公开接口2】根据邮箱查询投稿列表
     *
     * 流程：
     * 1. 校验验证码
     * 2. 根据邮箱查询所有投稿记录
     * 3. 转换为脱敏VO返回
     *
     * @param email 投稿者邮箱
     * @param code  验证码
     * @return 该邮箱下的所有投稿记录
     */
    @Override
    public List<PaperSubmissionQueryVO> queryByEmail(String email, String code) {
        String emailTrimmed = email.trim().toLowerCase();

        // 校验验证码
        String redisCacheKey = StrUtil.format(RedisConstants.Captcha.PAPER_QUERY_CODE, emailTrimmed);
        Object cachedCode = redisTemplate.opsForValue().get(redisCacheKey);
        if (cachedCode == null || !cachedCode.toString().equals(code)) {
            throw new BusinessException("验证码错误或已过期，请重新获取");
        }

        // 验证通过后删除验证码（一次性使用）
        redisTemplate.delete(redisCacheKey);

        // 查询该邮箱下的所有投稿记录，按ID倒序（不区分大小写匹配）
        List<PaperSubmission> list = this.list(new LambdaQueryWrapper<PaperSubmission>()
                .apply("LOWER(contact_email) = {0}", emailTrimmed)
                .orderByDesc(PaperSubmission::getId));

        // 转换为脱敏VO
        return list.stream().map(entity -> {
            PaperSubmissionQueryVO vo = new PaperSubmissionQueryVO();
            vo.setId(entity.getId());
            vo.setName(entity.getName());
            vo.setCompany(entity.getCompany());
            vo.setPaperTitle(entity.getPaperTitle());
            vo.setFirstAuthor(entity.getFirstAuthor());
            vo.setContactNumber(entity.getContactNumber());
            vo.setContactEmail(entity.getContactEmail());
            vo.setPaperAbstract(entity.getPaperAbstract());
            vo.setKeywords(entity.getKeywords());
            vo.setAppendix(entity.getAppendix());
            vo.setPaperStatus(entity.getPaperStatus());
            vo.setRejectReason(entity.getRejectReason());
            return vo;
        }).toList();
    }

    /**
     * 【公开接口3】根据ID查询投稿详情（需验证邮箱归属）
     *
     * @param id    投稿记录ID
     * @param email 投稿者邮箱
     * @return 投稿详情
     */
    @Override
    public PaperSubmissionQueryVO getDetailByEmail(Long id, String email) {
        String emailTrimmed = email.trim().toLowerCase();

        PaperSubmission entity = this.getById(id);
        if (entity == null) {
            throw new BusinessException("投稿记录不存在");
        }

        // 验证邮箱归属：只有投稿者本人可以查看详情
        if (!emailTrimmed.equals(entity.getContactEmail().trim().toLowerCase())) {
            throw new BusinessException("无权查看此投稿记录");
        }

        PaperSubmissionQueryVO vo = new PaperSubmissionQueryVO();
        vo.setId(entity.getId());
        vo.setName(entity.getName());
        vo.setCompany(entity.getCompany());
        vo.setPaperTitle(entity.getPaperTitle());
        vo.setFirstAuthor(entity.getFirstAuthor());
        vo.setContactNumber(entity.getContactNumber());
        vo.setContactEmail(entity.getContactEmail());
        vo.setPaperAbstract(entity.getPaperAbstract());
        vo.setKeywords(entity.getKeywords());
        vo.setAppendix(entity.getAppendix());
        vo.setPaperStatus(entity.getPaperStatus());
        vo.setRejectReason(entity.getRejectReason());
        return vo;
    }

    /**
     * 【公开接口4】投稿者修改投稿信息（仅待审核状态可修改）
     *
     * 流程：
     * 1. 根据ID查询投稿记录
     * 2. 验证邮箱归属
     * 3. 校验投稿状态（仅待审核 paperStatus=1 可修改）
     * 4. 更新允许修改的字段
     *
     * 允许修改的字段：paperTitle、firstAuthor、contactNumber、paperAbstract、keywords、appendix
     * 不可修改的字段：name、surname、contactEmail（作为身份标识）
     *
     * @param updateForm 修改表单
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateBySubmitter(PaperSubmissionUpdateForm updateForm) {
        String emailTrimmed = updateForm.getContactEmail().trim().toLowerCase();

        // 查询投稿记录
        PaperSubmission entity = this.getById(updateForm.getId());
        if (entity == null) {
            throw new BusinessException("投稿记录不存在");
        }

        // 验证邮箱归属
        if (!emailTrimmed.equals(entity.getContactEmail().trim().toLowerCase())) {
            throw new BusinessException("无权修改此投稿记录");
        }

        // 校验投稿状态：仅待审核(paperStatus=1)可修改
        if (entity.getPaperStatus() != null && entity.getPaperStatus() != 1) {
            String statusMsg = switch (entity.getPaperStatus()) {
                case 2 -> "审核通过";
                case 3 -> "审核拒绝";
                default -> "当前状态";
            };
            throw new BusinessException("投稿状态为「" + statusMsg + "」，不可修改。仅待审核状态可修改。");
        }

        // 更新允许修改的字段
        entity.setPaperTitle(updateForm.getPaperTitle());
        entity.setFirstAuthor(updateForm.getFirstAuthor());
        entity.setContactNumber(updateForm.getContactNumber());
        entity.setCompany(updateForm.getCompany());
        entity.setPaperAbstract(updateForm.getPaperAbstract());
        entity.setKeywords(updateForm.getKeywords());
        entity.setAppendix(updateForm.getAppendix());

        this.updateById(entity);
        log.info("投稿者修改投稿信息成功，id={}，email={}", updateForm.getId(), emailTrimmed);
    }

}