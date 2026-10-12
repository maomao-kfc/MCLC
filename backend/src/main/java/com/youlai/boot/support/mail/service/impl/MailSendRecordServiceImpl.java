package com.youlai.boot.support.mail.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.youlai.boot.config.property.ImapProperties;
import com.youlai.boot.config.property.MailProperties;
import com.youlai.boot.core.exception.BusinessException;
import com.youlai.boot.support.mail.enums.BounceType;
import com.youlai.boot.support.mail.enums.DeliveryStatus;
import com.youlai.boot.support.mail.enums.MailBusinessType;
import com.youlai.boot.support.mail.enums.MailSendStatus;
import com.youlai.boot.support.mail.mapper.MailSendRecordMapper;
import com.youlai.boot.support.mail.model.dto.BounceParseResult;
import com.youlai.boot.support.mail.model.dto.MailSendResult;
import com.youlai.boot.support.mail.model.entity.MailBounceRecord;
import com.youlai.boot.support.mail.model.entity.MailSendRecord;
import com.youlai.boot.support.mail.model.query.MailSendRecordQuery;
import com.youlai.boot.support.mail.model.vo.MailSendRecordPageVO;
import com.youlai.boot.support.mail.parser.BounceParser;
import com.youlai.boot.support.mail.service.MailBounceRecordService;
import com.youlai.boot.support.mail.service.MailSendRecordService;
import com.youlai.boot.support.mail.service.MailService;
import com.youlai.boot.support.mail.service.MailBounceBlacklistService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionTemplate;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;


/**
 * 邮件发送记录服务实现类
 *
 * 核心流程（任务表模式）：
 * 1. createAndSend: 插入记录(待发送) → 发送 → 更新状态(成功/失败)
 * 2. retry: 校验状态(失败) → 更新为重试中 → 发送 → 更新状态
 * 3. cancel: 校验状态(待发送) → 更新为已取消
 *
 * @author system
 * @since 2026/7/24
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class MailSendRecordServiceImpl extends ServiceImpl<MailSendRecordMapper, MailSendRecord>
        implements MailSendRecordService {

    private final MailService mailService;
    private final MailProperties mailProperties;
    private final ImapProperties imapProperties;
    private final MailBounceBlacklistService mailBounceBlacklistService;
    private final MailBounceRecordService mailBounceRecordService;
    private final TransactionTemplate transactionTemplate;



    /**
     * 创建邮件记录并同步发送（任务表模式）
     *
     * 步骤：
     * 1. 创建MailSendRecord，status=待发送
     * 2. 插入数据库（确保记录不丢失）
     * 3. 调用MailService发送邮件（带详细结果）
     * 4. 根据发送结果更新状态、Message-ID、耗时、错误信息
     */
    @Override
    public MailSendRecord createAndSend(MailBusinessType businessType, Long businessId,
                                         String receiver, String receiverName,
                                         String subject, String content) {
        // 步骤1：创建记录
        MailSendRecord record = new MailSendRecord();
        record.setBusinessType(businessType);
        record.setBusinessId(businessId);
        record.setReceiver(receiver);
        record.setReceiverName(receiverName);
        record.setSubject(subject);
        record.setContent(content);
        record.setSendStatus(MailSendStatus.PENDING);
        record.setRetryCount(0);
        record.setSender(mailProperties.getFrom());
        record.setCreateTime(LocalDateTime.now());
        record.setUpdateTime(LocalDateTime.now());

        // 步骤2：插入数据库（先落库，确保记录不丢失）
        this.save(record);

        // 步骤2.5：黑名单检查——收件人在黑名单中则直接标记失败，不实际发送
        if (mailBounceBlacklistService.isBlacklisted(receiver)) {
            log.warn("发送拦截：收件人{}在退信黑名单中，跳过发送", receiver);
            record.setSendStatus(MailSendStatus.FAILED);
            record.setErrorMessage("收件地址在退信黑名单中，已拦截发送");
            record.setUpdateTime(LocalDateTime.now());
            this.updateById(record);
            return record;
        }

        // 步骤3：发送邮件
        MailSendResult result = mailService.sendHtmlMailWithResult(receiver, subject, content);

        // 步骤4：更新状态
        updateRecordAfterSend(record, result);

        return record;
    }

    /**
     * 重试发送失败的邮件
     *
     * 校验：状态必须为失败，且重试次数不超过5次
     */
    @Override
    public MailSendRecord retry(Long id) {
        MailSendRecord record = this.getById(id);
        if (record == null) {
            throw new BusinessException("邮件记录不存在");
        }

        // 校验状态：只有失败或退信的记录才能重试
        if (record.getSendStatus() != MailSendStatus.FAILED
                && record.getSendStatus() != MailSendStatus.BOUNCE_DETECTED
                && record.getSendStatus() != MailSendStatus.RETRY_FAILED) {
            throw new BusinessException("只有发送失败、退信或重试失败的邮件才能重试，当前状态：" + record.getSendStatus().getLabel());
        }

        // 校验重试次数
        if (record.getRetryCount() != null && record.getRetryCount() >= MAX_RETRY_LIMIT) {
            throw new BusinessException("已超过最大重试次数(" + MAX_RETRY_LIMIT + "次)，请检查邮件配置");
        }

        // 黑名单检查——收件人在黑名单中则直接标记失败，不实际重发
        if (mailBounceBlacklistService.isBlacklisted(record.getReceiver())) {
            log.warn("重试拦截：收件人{}在退信黑名单中，跳过重发", record.getReceiver());
            record.setSendStatus(MailSendStatus.FAILED);
            record.setErrorMessage("收件地址在退信黑名单中，已拦截重发");
            record.setRetryCount(record.getRetryCount() == null ? 1 : record.getRetryCount() + 1);
            record.setUpdateTime(LocalDateTime.now());
            this.updateById(record);
            return record;
        }

        // 更新为重试中
        record.setSendStatus(MailSendStatus.RETRYING);
        record.setUpdateTime(LocalDateTime.now());
        this.updateById(record);

        // 重新发送
        MailSendResult result = mailService.sendHtmlMailWithResult(
                record.getReceiver(), record.getSubject(), record.getContent());

        // 递增重试次数
        record.setRetryCount(record.getRetryCount() == null ? 1 : record.getRetryCount() + 1);

        // 更新状态
        updateRecordAfterSend(record, result);

        return record;
    }

    /**
     * 取消待发送的邮件
     */
    @Override
    public void cancel(Long id) {
        MailSendRecord record = this.getById(id);
        if (record == null) {
            throw new BusinessException("邮件记录不存在");
        }

        if (record.getSendStatus() != MailSendStatus.PENDING) {
            throw new BusinessException("只有待发送的邮件才能取消，当前状态：" + record.getSendStatus().getLabel());
        }

        record.setSendStatus(MailSendStatus.CANCELLED);
        record.setUpdateTime(LocalDateTime.now());
        this.updateById(record);
    }

    /**
     * 分页查询邮件发送记录（树形结构）
     *
     * 分页仅查询原始记录（record_type=ORIGINAL），然后批量查询其重试子记录组装树形结构。
     * 重试记录不会出现在一级列表中，而是作为原始记录的children返回。
     */
    @Override
    public Page<MailSendRecordPageVO> getMailSendRecordPage(MailSendRecordQuery query) {
        Page<MailSendRecordPageVO> page = this.baseMapper.getMailSendRecordPage(
                new Page<>(query.getPageNum(), query.getPageSize()), query);

        // 获取分页中的原始记录ID，批量查询重试子记录
        List<MailSendRecordPageVO> records = page.getRecords();
        if (records != null && !records.isEmpty()) {
            List<Long> rootIds = records.stream()
                    .map(MailSendRecordPageVO::getId)
                    .collect(Collectors.toList());

            // 批量查询所有重试子记录
            List<MailSendRecordPageVO> retryRecords = this.baseMapper.getRetryRecordsByRootIds(rootIds);

            // 按 rootRecordId 分组
            Map<Long, List<MailSendRecordPageVO>> childrenMap = retryRecords.stream()
                    .collect(Collectors.groupingBy(MailSendRecordPageVO::getRootRecordId));

            // 为每个原始记录设置children
            for (MailSendRecordPageVO record : records) {
                List<MailSendRecordPageVO> children = childrenMap.get(record.getId());
                record.setChildren(children != null ? children : Collections.emptyList());
            }
        }

        return page;
    }

    /**
     * 根据业务类型和业务ID查询邮件记录列表
     */
    @Override
    public List<MailSendRecord> listByBusiness(MailBusinessType businessType, Long businessId) {
        return this.list(new LambdaQueryWrapper<MailSendRecord>()
                .eq(MailSendRecord::getBusinessType, businessType)
                .eq(MailSendRecord::getBusinessId, businessId)
                .orderByDesc(MailSendRecord::getCreateTime));
    }

    /**
     * 根据发送结果更新记录
     *
     * 成功：status=成功，记录messageId、consumeTime、sendTime
     * 失败：status=失败，记录errorMessage、consumeTime
     */
    private void updateRecordAfterSend(MailSendRecord record, MailSendResult result) {
        if (result.isSuccess()) {
            record.setSendStatus(MailSendStatus.SUCCESS);
            record.setMessageId(result.getMessageId());
            record.setConsumeTime(result.getConsumeTime());
            record.setSendTime(LocalDateTime.now());
            record.setErrorMessage(null);
            log.info("邮件发送成功：id={}, businessType={}, businessId={}, receiver={}, messageId={}, 耗时={}ms",
                    record.getId(), record.getBusinessType(), record.getBusinessId(),
                    record.getReceiver(), result.getMessageId(), result.getConsumeTime());
        } else {
            record.setSendStatus(MailSendStatus.FAILED);
            record.setErrorMessage(result.getErrorMessage());
            record.setConsumeTime(result.getConsumeTime());
            log.error("邮件发送失败：id={}, businessType={}, businessId={}, receiver={}, error={}",
                    record.getId(), record.getBusinessType(), record.getBusinessId(),
                    record.getReceiver(), result.getErrorMessage());
        }
        record.setUpdateTime(LocalDateTime.now());
        this.updateById(record);
    }

    /**
     * 根据 Message-ID 查找邮件发送记录
     */
    @Override
    public MailSendRecord findByMessageId(String messageId) {
        if (messageId == null || messageId.isBlank()) {
            return null;
        }
        return this.getOne(new LambdaQueryWrapper<MailSendRecord>()
                .eq(MailSendRecord::getMessageId, messageId)
                .last("LIMIT 1"));
    }

    /**
     * 处理退信（兼容旧接口）：委托给handleBounceEnhanced处理
     *
     * 旧接口仅提供messageId/bounceReason/bouncedRecipient，
     * 通过BounceParser补充解析退信类型和代码后，委托给增强版处理。
     *
     * @deprecated 建议使用 {@link #handleBounceEnhanced} 替代，提供更完整的退信信息
     */
    @Override
    public boolean handleBounce(String messageId, String bounceReason, String bouncedRecipient) {
        // 使用BounceParser解析退信原因，补充退信类型和代码
        BounceParseResult parseResult = BounceParser.parse(null, null, null, bounceReason);

        // 委托给增强版处理：messageId同时作为退信Message-ID和原始Message-ID尝试匹配
        return handleBounceEnhanced(
                messageId,           // 退信邮件Message-ID
                messageId,           // 尝试作为原始Message-ID匹配
                bounceReason,        // 退信原因
                bouncedRecipient,    // 退信收件人
                parseResult.getBounceType().name(),  // 退信类型
                parseResult.getBounceCode(),          // 退信代码
                parseResult.getDsnStatus(),           // DSN状态码
                parseResult.getDsnDiagnosticCode(),   // DSN诊断码
                "MESSAGE_ID",        // 默认匹配策略
                null,                // 退信邮件主题（旧接口无此信息）
                null                 // 退信邮件发送者（旧接口无此信息）
        );
    }

    /**
     * 管理员在邮件中心手动标记退信，用于IMAP自动检测遗漏的退信场景。
     * 允许标记状态为"已提交至邮件服务器(1)"或"重试成功(7)"的记录。
     */
    @Override
    public MailSendRecord markBounce(Long id, String bounceReason) {
        MailSendRecord record = this.getById(id);
        if (record == null) {
            throw new BusinessException("邮件记录不存在");
        }

        // 允许标记"已提交至邮件服务器"或"重试成功"状态的记录
        if (record.getSendStatus() != MailSendStatus.SUCCESS
                && record.getSendStatus() != MailSendStatus.RETRY_SUCCESS) {
            throw new BusinessException("只能标记已提交至邮件服务器或重试成功的邮件为退信，当前状态：" + record.getSendStatus().getLabel());
        }

        // 判断记录类型，选择正确的退信状态
        boolean isRetryRecord = "RETRY".equals(record.getRecordType());
        MailSendStatus bounceStatus = isRetryRecord ? MailSendStatus.RETRY_FAILED : MailSendStatus.BOUNCE_DETECTED;

        // 更新状态为退信
        record.setSendStatus(bounceStatus);
        record.setBounceReason("[手动标记] " + bounceReason);
        record.setUpdateTime(LocalDateTime.now());
        this.updateById(record);

        // 同步更新delivery_status=BOUNCED
        updateDeliveryStatus(record.getId(), DeliveryStatus.BOUNCED, null, "[手动标记] " + bounceReason, null);

        log.info("手动标记退信：邮件记录id={}，收件人={}，原因={}，状态={}", record.getId(), record.getReceiver(), bounceReason, bounceStatus.getLabel());

        // 手动标记退信时，若匹配永久性退信模式，也加入黑名单
        String fullReason = "[手动标记] " + bounceReason;
        if (isPermanentBounce(fullReason) && record.getReceiver() != null && !record.getReceiver().isBlank()) {
            log.info("手动标记退信：检测到永久性退信，将收件人{}加入黑名单", record.getReceiver());
            mailBounceBlacklistService.autoAdd(record.getReceiver(), fullReason, record.getId());
        }

        // 发送管理员告警邮件（仅原始记录手动标记时通知）
        if (!isRetryRecord) {
            sendBounceAlert(record, "[手动标记] " + bounceReason, record.getReceiver());
        }

        return record;
    }

    /**
     * 发送退信告警邮件给管理员
     */
    private void sendBounceAlert(MailSendRecord record, String bounceReason, String bouncedRecipient) {
        String adminEmails = imapProperties.getAdminAlertEmails();
        if (adminEmails == null || adminEmails.isBlank()) {
            log.warn("退信告警：未配置管理员告警邮箱，跳过告警发送");
            return;
        }

        String subject = "【退信告警】邮件投递失败 - " + bouncedRecipient;
        String htmlContent = buildBounceAlertHtml(record, bounceReason, bouncedRecipient);

        Arrays.stream(adminEmails.split(","))
                .map(String::trim)
                .filter(email -> !email.isEmpty())
                .forEach(adminEmail -> {
                    try {
                        mailService.sendHtmlMail(adminEmail, subject, htmlContent);
                        log.info("退信告警邮件已发送：{}", adminEmail);
                    } catch (Exception e) {
                        log.error("退信告警邮件发送失败：{}", adminEmail, e);
                    }
                });
    }

    /**
     * 构建退信告警邮件HTML内容
     */
        private String buildBounceAlertHtml(MailSendRecord record, String bounceReason, String bouncedRecipient) {
        return String.format("""
                <div style="font-family: Arial, sans-serif; max-width: 600px; margin: 0 auto; padding: 20px;">
                    <h2 style="color: #e74c3c;">⚠️ 邮件投递失败告警</h2>
                    <table style="border-collapse: collapse; width: 100%%; margin-top: 15px;">
                        <tr><td style="padding: 8px; border: 1px solid #ddd; font-weight: bold; width: 120px;">退信收件人</td><td style="padding: 8px; border: 1px solid #ddd;">%s</td></tr>
                        <tr><td style="padding: 8px; border: 1px solid #ddd; font-weight: bold;">邮件主题</td><td style="padding: 8px; border: 1px solid #ddd;">%s</td></tr>
                        <tr><td style="padding: 8px; border: 1px solid #ddd; font-weight: bold;">业务类型</td><td style="padding: 8px; border: 1px solid #ddd;">%s</td></tr>
                        <tr><td style="padding: 8px; border: 1px solid #ddd; font-weight: bold;">业务ID</td><td style="padding: 8px; border: 1px solid #ddd;">%s</td></tr>
                        <tr><td style="padding: 8px; border: 1px solid #ddd; font-weight: bold;">退信原因</td><td style="padding: 8px; border: 1px solid #ddd; color: #e74c3c;">%s</td></tr>
                        <tr><td style="padding: 8px; border: 1px solid #ddd; font-weight: bold;">记录ID</td><td style="padding: 8px; border: 1px solid #ddd;">%s</td></tr>
                        <tr><td style="padding: 8px; border: 1px solid #ddd; font-weight: bold;">原始Message-ID</td><td style="padding: 8px; border: 1px solid #ddd;">%s</td></tr>
                        <tr><td style="padding: 8px; border: 1px solid #ddd; font-weight: bold;">发送时间</td><td style="padding: 8px; border: 1px solid #ddd;">%s</td></tr>
                    </table>
                    <p style="margin-top: 15px; color: #666;">此邮件由系统自动发送，请及时处理。</p>
                </div>
                """,
                bouncedRecipient,
                record.getSubject(),
                record.getBusinessType(),
                record.getBusinessId(),
                bounceReason,
                record.getId(),
                record.getMessageId(),
                record.getSendTime()
        );
    }

    /**
     * 根据收件人和时间窗口查找发送记录
     * 用于退信匹配的兜底策略：当Message-ID等无法匹配时，按收件人+时间窗口查找
     */
    @Override
    public List<MailSendRecord> findByReceiverAndTimeWindow(String receiver, int days) {
        if (receiver == null || receiver.isBlank() || days <= 0) {
            return Collections.emptyList();
        }
        LocalDateTime cutoffTime = LocalDateTime.now().minusDays(days);
        return this.list(new LambdaQueryWrapper<MailSendRecord>()
                .eq(MailSendRecord::getReceiver, receiver)
                .eq(MailSendRecord::getSendStatus, MailSendStatus.SUCCESS)
                .ge(MailSendRecord::getSendTime, cutoffTime)
                .orderByDesc(MailSendRecord::getSendTime));
    }

    /**
     * 更新投递状态
     * 独立于send_status，记录邮件从SMTP成功到最终投递的状态变化
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateDeliveryStatus(Long id, DeliveryStatus deliveryStatus,
                                      String bounceCode, String bounceReason, String bounceMessageId) {
        if (id == null) {
            return;
        }
        MailSendRecord record = this.getById(id);
        if (record == null) {
            return;
        }
        MailSendRecord update = new MailSendRecord();
        update.setId(id);
        update.setDeliveryStatus(deliveryStatus);
        if (bounceCode != null && !bounceCode.isBlank()) {
            update.setBounceCode(bounceCode);
        }
        if (bounceReason != null && !bounceReason.isBlank()) {
            update.setBounceReason(bounceReason);
        }
        if (bounceMessageId != null && !bounceMessageId.isBlank()) {
            update.setBounceMessageId(bounceMessageId);
        }
        // 投递状态为退信时，记录退信时间
        if (DeliveryStatus.BOUNCED.equals(deliveryStatus)) {
            update.setBounceTime(LocalDateTime.now());
        }
        // 投递状态为已投递时，记录投递时间
        if (DeliveryStatus.DELIVERED.equals(deliveryStatus)) {
            update.setDeliveryTime(LocalDateTime.now());
        }
        this.updateById(update);
    }

    /**
     * 处理退信（增强版）：解析退信信息、写入mail_bounce_record、更新delivery_status
     *
     * 完整流程：
     * 1. 幂等检查（bounceMessageId唯一索引）
     * 2. 5级匹配策略查找对应的mail_send_record
     * 3. 【事务内】写入mail_bounce_record退信记录 + 更新delivery_status + 黑名单 + 重试记录创建
     * 4. 【事务外】发送管理员告警（仅原始记录第一次退信）+ 自动重发邮件（仅原始记录+临时退信）
     *
     * 重试机制设计：
     * - 只有record_type=ORIGINAL的记录才触发自动重试，重试记录(RETRY)不会再触发重试
     * - root_record_id始终指向原始邮件记录ID（扁平关联，非递归）
     * - retry_sequence标识重试序号(1/2)，最多重试2次
     * - 唯一约束(root_record_id, retry_sequence)保证幂等
     * - 管理员告警仅在同一原始邮件第一次退信时发送一次
     */
    @Override
    public boolean handleBounceEnhanced(String messageId, String originalMessageId,
                                         String bounceReason, String bouncedRecipient,
                                         String bounceType, String bounceCode,
                                         String dsnStatus, String dsnDiagnosticCode,
                                         String matchStrategy,
                                         String bounceSubject, String bounceSender) {
        // 1. 幂等检查：同一退信邮件不重复处理
        if (messageId != null && !messageId.isBlank()) {
            MailBounceRecord existingRecord = mailBounceRecordService.findByBounceMessageId(messageId);
            if (existingRecord != null) {
                log.info("退信增强处理：退信邮件Message-ID={}已处理过，跳过（幂等）", messageId);
                return true;
            }
        }

        // 2. 5级匹配策略查找对应的mail_send_record
        MailSendRecord matchedRecord = null;
        String usedMatchStrategy = matchStrategy;

        // 优先级1：通过原始Message-ID直接匹配
        if (originalMessageId != null && !originalMessageId.isBlank()) {
            matchedRecord = findByMessageId(originalMessageId);
            if (matchedRecord != null) {
                usedMatchStrategy = "MESSAGE_ID";
                log.info("退信匹配成功[MESSAGE_ID]：originalMessageId={} → recordId={}", originalMessageId, matchedRecord.getId());
            }
        }

        // 优先级2：通过退信邮件Message-ID尝试匹配（某些退信直接引用自身）
        if (matchedRecord == null && messageId != null && !messageId.isBlank()) {
            matchedRecord = findByMessageId(messageId);
            if (matchedRecord != null) {
                usedMatchStrategy = "IN_REPLY_TO";
                log.info("退信匹配成功[IN_REPLY_TO]：messageId={} → recordId={}", messageId, matchedRecord.getId());
            }
        }

        // 优先级3：通过收件人+时间窗口匹配
        if (matchedRecord == null && bouncedRecipient != null && !bouncedRecipient.isBlank()) {
            int timeWindowDays = imapProperties.getMatchTimeWindowDays();
            List<MailSendRecord> candidates = findByReceiverAndTimeWindow(bouncedRecipient, timeWindowDays);
            if (!candidates.isEmpty()) {
                matchedRecord = candidates.get(0); // 取最近一条
                usedMatchStrategy = "RECEIVER_TIME_WINDOW";
                log.info("退信匹配成功[RECEIVER_TIME_WINDOW]：recipient={}，时间窗口={}天 → recordId={}，共{}条候选",
                        bouncedRecipient, timeWindowDays, matchedRecord.getId(), candidates.size());
            }
        }

        // 未匹配
        if (matchedRecord == null) {
            usedMatchStrategy = "UNMATCHED";
            log.warn("退信匹配失败[UNMATCHED]：originalMessageId={}，messageId={}，bouncedRecipient={}，无法找到对应发送记录",
                    originalMessageId, messageId, bouncedRecipient);
        }

        // 解析退信类型
        BounceType type = null;
        if (bounceType != null && !bounceType.isBlank()) {
            try {
                type = BounceType.valueOf(bounceType);
            } catch (IllegalArgumentException e) {
                log.warn("退信增强处理：无法解析退信类型bounceType={}，按OTHER处理", bounceType);
                type = BounceType.OTHER;
            }
        } else {
            // 未指定类型时，通过BounceParser解析
            BounceParseResult parseResult = BounceParser.parse(bounceSubject, bounceSender, null, bounceReason);
            type = parseResult.getBounceType();
            if (bounceCode == null || bounceCode.isBlank()) {
                bounceCode = parseResult.getBounceCode();
            }
        }
        boolean isPermanent = type != null && type.isPermanent();

        // 判断是否为原始记录（只有原始记录才触发重试和管理员告警）
        boolean isOriginalRecord = matchedRecord != null
                && ("ORIGINAL".equals(matchedRecord.getRecordType()) || matchedRecord.getRecordType() == null);

        // 判断是否为该原始邮件的第一次退信（用于管理员告警去重）
        // 如果matchedRecord本身就是重试记录，说明原始邮件之前已经退信过，不需要再发告警
        boolean isFirstBounceOfOriginal = isOriginalRecord;

        // ===== 【事务内】核心退信处理：写bounce_record + 更新send_record状态 + 黑名单 + 创建重试记录 =====
        final String finalBounceCode = bounceCode;
        final BounceType finalType = type;
        final String finalUsedMatchStrategy = usedMatchStrategy;
        final MailSendRecord finalMatchedRecord = matchedRecord;
        final boolean finalIsPermanent = isPermanent;
        final boolean finalIsOriginalRecord = isOriginalRecord;
        final boolean finalIsFirstBounceOfOriginal = isFirstBounceOfOriginal;

        // 需要在事务外执行的操作的参数
        final MailSendRecord[] retryRecordHolder = new MailSendRecord[1];
        final boolean[] shouldSendAlert = {false};
        final MailSendRecord[] alertRecordHolder = new MailSendRecord[1];

        transactionTemplate.executeWithoutResult(status -> {
            // 3. 写入mail_bounce_record退信记录
            MailBounceRecord bounceRecord = new MailBounceRecord();
            bounceRecord.setBounceMessageId(messageId);
            bounceRecord.setOriginalMessageId(originalMessageId);
            bounceRecord.setMatchedRecordId(finalMatchedRecord != null ? finalMatchedRecord.getId() : null);
            bounceRecord.setMatchStrategy(finalUsedMatchStrategy);
            bounceRecord.setBounceCode(finalBounceCode);
            bounceRecord.setBounceReason(bounceReason);
            bounceRecord.setBouncedRecipient(bouncedRecipient);
            bounceRecord.setDsnStatus(dsnStatus);
            bounceRecord.setDsnDiagnosticCode(dsnDiagnosticCode);
            bounceRecord.setBounceSubject(bounceSubject);
            bounceRecord.setBounceSender(bounceSender);
            bounceRecord.setBounceDate(LocalDateTime.now());
            bounceRecord.setProcessed(true);
            bounceRecord.setProcessTime(LocalDateTime.now());
            bounceRecord.setCreateTime(LocalDateTime.now());
            bounceRecord.setUpdateTime(LocalDateTime.now());

            bounceRecord.setBounceType(finalType);
            bounceRecord.setBouncePermanent(finalIsPermanent);

            // 设置处理结果摘要
            String processResult = finalMatchedRecord != null
                    ? String.format("匹配成功[%s]→recordId=%d，%s", finalUsedMatchStrategy, finalMatchedRecord.getId(),
                    finalIsPermanent ? "永久退信-禁止重发" : "临时退信-可重发")
                    : String.format("未匹配，%s", finalIsPermanent ? "永久退信" : "临时退信");
            bounceRecord.setProcessResult(processResult);

            boolean saved = mailBounceRecordService.saveIfAbsent(bounceRecord);
            if (!saved) {
                log.info("退信增强处理：退信记录已存在（幂等），messageId={}", messageId);
                return;
            }

            // 4. 更新mail_send_record的delivery_status=BOUNCED + send_status=BOUNCE_DETECTED（仅匹配成功时）
            if (finalMatchedRecord != null) {
                if (finalMatchedRecord.getSendStatus() == MailSendStatus.SUCCESS
                        || finalMatchedRecord.getSendStatus() == MailSendStatus.RETRY_SUCCESS) {
                    updateDeliveryStatus(finalMatchedRecord.getId(), DeliveryStatus.BOUNCED, finalBounceCode, bounceReason, messageId);

                    // 同步更新send_status
                    MailSendRecord statusUpdate = new MailSendRecord();
                    statusUpdate.setId(finalMatchedRecord.getId());
                    // 重试记录退信 → RETRY_FAILED，原始记录退信 → BOUNCE_DETECTED
                    if ("RETRY".equals(finalMatchedRecord.getRecordType())) {
                        statusUpdate.setSendStatus(MailSendStatus.RETRY_FAILED);
                    } else {
                        statusUpdate.setSendStatus(MailSendStatus.BOUNCE_DETECTED);
                    }
                    statusUpdate.setBounceReason(bounceReason);
                    statusUpdate.setBounceCode(finalBounceCode);
                    statusUpdate.setBounceMessageId(messageId);
                    statusUpdate.setUpdateTime(LocalDateTime.now());
                    this.updateById(statusUpdate);

                    log.info("退信增强处理：已更新recordId={}的delivery_status=BOUNCED, send_status={}",
                            finalMatchedRecord.getId(), statusUpdate.getSendStatus().getLabel());
                } else {
                    log.info("退信增强处理：recordId={}的send_status={}，跳过状态更新",
                            finalMatchedRecord.getId(), finalMatchedRecord.getSendStatus().getLabel());
                }
            }

            // 5. 永久性退信加入黑名单
            if (finalIsPermanent && bouncedRecipient != null && !bouncedRecipient.isBlank()) {
                if (imapProperties.isPermanentBounceBlockResend()) {
                    log.info("退信增强处理：永久性退信[{}]，将收件人{}加入黑名单", finalType, bouncedRecipient);
                    mailBounceBlacklistService.autoAdd(bouncedRecipient, bounceReason,
                            finalMatchedRecord != null ? finalMatchedRecord.getId() : null);
                }
            }

            // 6. 标记管理员告警（仅原始记录第一次退信）
            if (finalIsFirstBounceOfOriginal && finalMatchedRecord != null) {
                shouldSendAlert[0] = true;
                alertRecordHolder[0] = finalMatchedRecord;
            }

            // 7. 临时性退信自动重试（仅原始记录+非永久退信+启用自动重发+未超最大重试次数）
            if (finalIsOriginalRecord && !finalIsPermanent && imapProperties.isAutoResend() && finalMatchedRecord != null) {
                // 查询该原始记录已有的重试次数
                int maxResendCount = imapProperties.getAutoResendMaxCount();
                long existingRetryCount = this.count(new LambdaQueryWrapper<MailSendRecord>()
                        .eq(MailSendRecord::getRootRecordId, finalMatchedRecord.getId()));

                if (existingRetryCount < maxResendCount) {
                    int nextRetrySequence = (int) existingRetryCount + 1;
                    log.info("退信增强处理：临时性退信自动重试，rootRecordId={}，重试序号={}/{}",
                            finalMatchedRecord.getId(), nextRetrySequence, maxResendCount);

                    // 创建重试记录（状态=RETRY_PENDING，实际发送在事务外执行）
                    MailSendRecord retryRecord = new MailSendRecord();
                    retryRecord.setBusinessType(finalMatchedRecord.getBusinessType());
                    retryRecord.setBusinessId(finalMatchedRecord.getBusinessId());
                    retryRecord.setReceiver(finalMatchedRecord.getReceiver());
                    retryRecord.setReceiverName(finalMatchedRecord.getReceiverName());
                    retryRecord.setSubject(finalMatchedRecord.getSubject());
                    retryRecord.setContent(finalMatchedRecord.getContent());
                    retryRecord.setSender(finalMatchedRecord.getSender());
                    retryRecord.setRecordType("RETRY");
                    retryRecord.setRetrySequence(nextRetrySequence);
                    retryRecord.setRootRecordId(finalMatchedRecord.getId()); // 始终指向原始记录
                    retryRecord.setParentRecordId(finalMatchedRecord.getId()); // 兼容旧字段
                    retryRecord.setSendStatus(MailSendStatus.RETRY_PENDING);
                    retryRecord.setRetryCount(nextRetrySequence);
                    retryRecord.setCreateTime(LocalDateTime.now());
                    retryRecord.setUpdateTime(LocalDateTime.now());
                    this.save(retryRecord);

                    retryRecordHolder[0] = retryRecord;
                    log.info("退信增强处理：已创建重试记录retryRecordId={}，rootRecordId={}，retrySequence={}",
                            retryRecord.getId(), finalMatchedRecord.getId(), nextRetrySequence);
                } else {
                    log.warn("退信增强处理：rootRecordId={}已超过最大重试次数({}/{})，不再自动重试",
                            finalMatchedRecord.getId(), existingRetryCount, maxResendCount);
                }
            }

            // 永久性退信不自动重试
            if (finalIsOriginalRecord && finalIsPermanent && finalMatchedRecord != null) {
                log.info("退信增强处理：永久性退信[{}]，rootRecordId={}禁止自动重试", finalType, finalMatchedRecord.getId());
            }

            // 非原始记录（重试记录退信）不触发重试
            if (!finalIsOriginalRecord && finalMatchedRecord != null) {
                log.info("退信增强处理：recordId={}为重试记录(RETRY)，不再触发自动重试", finalMatchedRecord.getId());
            }
        });

        // ===== 【事务外】执行IO操作：管理员告警 + 邮件发送 =====

        // 6. 发送管理员告警（仅原始记录第一次退信）
        if (shouldSendAlert[0] && alertRecordHolder[0] != null) {
            try {
                sendBounceAlert(alertRecordHolder[0], bounceReason, bouncedRecipient);
            } catch (Exception e) {
                log.error("退信增强处理：管理员告警发送失败，recordId={}", alertRecordHolder[0].getId(), e);
            }
        }

        // 7. 执行自动重试发送（事务外，避免IO长事务）
        if (retryRecordHolder[0] != null) {
            MailSendRecord retryRecord = retryRecordHolder[0];
            try {
                MailSendResult result = mailService.sendHtmlMailWithResult(
                        retryRecord.getReceiver(), retryRecord.getSubject(), retryRecord.getContent());

                // 更新重试记录状态
                MailSendRecord update = new MailSendRecord();
                update.setId(retryRecord.getId());
                update.setUpdateTime(LocalDateTime.now());
                if (result.isSuccess()) {
                    update.setSendStatus(MailSendStatus.RETRY_SUCCESS);
                    update.setMessageId(result.getMessageId());
                    update.setConsumeTime(result.getConsumeTime());
                    update.setSendTime(LocalDateTime.now());
                    log.info("退信增强处理：自动重试发送成功，retryRecordId={}，messageId={}",
                            retryRecord.getId(), result.getMessageId());
                } else {
                    update.setSendStatus(MailSendStatus.RETRY_FAILED);
                    update.setErrorMessage(result.getErrorMessage());
                    update.setConsumeTime(result.getConsumeTime());
                    log.error("退信增强处理：自动重试发送失败，retryRecordId={}，错误={}",
                            retryRecord.getId(), result.getErrorMessage());
                }
                this.updateById(update);
            } catch (Exception e) {
                log.error("退信增强处理：自动重试发送异常，retryRecordId={}", retryRecord.getId(), e);
                // 发送异常时标记为重试失败
                MailSendRecord update = new MailSendRecord();
                update.setId(retryRecord.getId());
                update.setSendStatus(MailSendStatus.RETRY_FAILED);
                update.setErrorMessage("重试发送异常: " + e.getMessage());
                update.setUpdateTime(LocalDateTime.now());
                this.updateById(update);
            }
        }

        return true;
    }

    /**
     * 判断是否为永久性退信（基于BounceParser解析结果）
     *
     * 永久性退信：DNS_ERROR、USER_NOT_FOUND、MAILBOX_UNAVAILABLE、SPAM_REJECTED、RELAY_DENIED
     * 临时性退信：MAILBOX_FULL（邮箱满可能恢复）、OTHER（未知类型默认按临时处理）
     *
     * @param bounceReason 退信原因文本
     * @return true=永久性退信，false=临时性退信
     */
    private boolean isPermanentBounce(String bounceReason) {
        if (bounceReason == null || bounceReason.isBlank()) {
            return false;
        }
        // 使用BounceParser解析退信原因，统一由BounceType.isPermanent()判断
        BounceParseResult result = BounceParser.parse(null, null, null, bounceReason);
        return result.isPermanent();
    }
}
