package com.youlai.boot.support.mail.model.dto;

import com.youlai.boot.support.mail.enums.BounceType;
import lombok.Data;

/**
 * 退信解析结果
 *
 * 核心设计原则：退信通知邮件与原始邮件信息严格分离。
 *
 * 退信通知邮件信息：
 * - bounceSender: 退信通知的发送者（如 Postmaster@163.com）
 * - bounceRecipient: 退信通知的收件人（如 registrationmclc@163.com）
 * - bounceSubject: 退信通知的主题
 * - bounceMessageId: 退信通知邮件自身的Message-ID
 *
 * 原始邮件信息（真正发生投递失败的邮件）：
 * - originalRecipient: 原始邮件真正的目标收件人（如 maojiangjun@faxmai.com）
 * - originalSender: 原始邮件发件人
 * - originalSubject: 原始邮件主题（如 "论文审稿邀请"）
 * - originalMessageId: 原始邮件的Message-ID
 *
 * DSN标准字段（RFC 3464）：
 * - dsnStatus: Status字段（如 5.4.4）
 * - dsnDiagnosticCode: Diagnostic-Code字段（如 SMTP; DNS query error: faxmai.com）
 * - dsnAction: Action字段（如 failed）
 * - finalRecipient: Final-Recipient字段（如 rfc822;maojiangjun@faxmai.com）
 * - originalRecipientDsn: Original-Recipient字段
 *
 * 匹配辅助字段：
 * - inReplyTo: 退信邮件的In-Reply-To头（引用原始邮件Message-ID）
 * - referencesHeader: 退信邮件的References头
 *
 * @author system
 * @since 2026/8/3
 */
@Data
public class BounceParseResult {

    // ========== 退信通知邮件信息 ==========

    /** 退信通知发送者（如 Postmaster@163.com） */
    private String bounceSender;

    /** 退信通知收件人（如 registrationmclc@163.com，即谁收到退信通知） */
    private String bounceRecipient;

    /** 退信通知邮件主题 */
    private String bounceSubject;

    /** 退信通知邮件自身的Message-ID */
    private String bounceMessageId;

    // ========== 原始邮件信息（真正发生投递失败的邮件） ==========

    /** 原始邮件真正的目标收件人（如 maojiangjun@faxmai.com） */
    private String originalRecipient;

    /** 原始邮件发件人 */
    private String originalSender;

    /** 原始邮件主题（如 "论文审稿邀请"） */
    private String originalSubject;

    /** 原始邮件的Message-ID */
    private String originalMessageId;

    // ========== DSN标准字段（RFC 3464） ==========

    /** DSN Status字段（如 5.4.4） */
    private String dsnStatus;

    /** DSN Diagnostic-Code字段（如 SMTP; DNS query error: faxmai.com） */
    private String dsnDiagnosticCode;

    /** DSN Action字段（如 failed） */
    private String dsnAction;

    /** DSN Final-Recipient字段（如 rfc822;maojiangjun@faxmai.com） */
    private String finalRecipient;

    /** DSN Original-Recipient字段 */
    private String originalRecipientDsn;

    // ========== 匹配辅助字段 ==========

    /** In-Reply-To头（引用原始邮件Message-ID） */
    private String inReplyTo;

    /** References头 */
    private String referencesHeader;

    // ========== 退信分类结果 ==========

    /** 退信类型 */
    private BounceType bounceType;

    /** 退信代码（标准化分类码，如 5.4.4、550_USER_NOT_FOUND、DNS_ERROR） */
    private String bounceCode;

    /** 退信原因（从退信邮件中提取的完整原因文本） */
    private String bounceReason;

    /** 原始收件人信息来源：DSN_FINAL_RECIPIENT / DSN_ORIGINAL_RECIPIENT / IN_REPLY_TO / BODY_FALLBACK */
    private String recipientSource;

    /**
     * 是否永久性退信（由BounceType.isPermanent()决定）
     */
    public boolean isPermanent() {
        return bounceType != null && bounceType.isPermanent();
    }

    /**
     * 获取退信收件人（向后兼容方法）
     *
     * @deprecated 使用 {@link #getOriginalRecipient()} 替代。
     * 旧bouncedRecipient语义等同于originalRecipient（真正投递失败的目标地址）
     */
    @Deprecated
    public String getBouncedRecipient() {
        return originalRecipient;
    }

    /**
     * 设置退信收件人（向后兼容方法）
     *
     * @deprecated 使用 {@link #setOriginalRecipient(String)} 替代
     */
    @Deprecated
    public void setBouncedRecipient(String bouncedRecipient) {
        this.originalRecipient = bouncedRecipient;
    }
}