package com.youlai.boot.support.mail.model.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.youlai.boot.support.mail.enums.BounceType;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 邮件退信记录实体
 *
 * 独立记录每封退信邮件的完整信息，与 mail_send_record 通过原始 Message-ID 关联。
 * 核心设计原则：退信通知邮件与原始邮件信息严格分离。
 *
 * 退信通知邮件信息：
 * - bounceMessageId: 退信通知邮件自身的Message-ID
 * - bounceSender: 退信通知发送者（如 Postmaster@163.com）
 * - bounceRecipient: 退信通知收件人（如 registrationmclc@163.com）
 * - bounceSubject: 退信通知主题
 *
 * 原始邮件信息（真正发生投递失败的邮件）：
 * - originalMessageId: 原始邮件的Message-ID
 * - originalSender: 原始邮件发件人
 * - originalRecipient: 原始邮件真正的目标收件人（如 maojiangjun@faxmai.com）
 * - originalSubject: 原始邮件主题
 *
 * DSN标准字段：
 * - dsnStatus, dsnDiagnosticCode, dsnAction
 * - finalRecipient, originalRecipientDsn
 *
 * 匹配信息：
 * - matchedRecordId: 匹配到的mail_send_record记录ID
 * - matchStrategy: 匹配策略
 * - matchConfidence: 匹配置信度
 * - recipientSource: 原始收件人信息来源
 *
 * @author system
 * @since 2026/8/3
 */
@TableName("mail_bounce_record")
@Getter
@Setter
public class MailBounceRecord implements Serializable {

    /** 主键ID */
    @TableId(type = IdType.AUTO)
    private Long id;

    // ========== 退信通知邮件信息 ==========

    /** 退信邮件的Message-ID（唯一标识一封退信，幂等去重） */
    private String bounceMessageId;

    /** 退信通知发送者（如 Postmaster@163.com） */
    private String bounceSender;

    /** 退信通知收件人（如 registrationmclc@163.com，即谁收到退信通知） */
    private String bounceRecipient;

    /** 退信邮件主题 */
    private String bounceSubject;

    // ========== 原始邮件信息（真正发生投递失败的邮件） ==========

    /** 原始发送邮件的Message-ID（退信引用的原始邮件ID） */
    private String originalMessageId;

    /** 原始邮件发件人 */
    private String originalSender;

    /** 原始邮件真正的目标收件人（如 maojiangjun@faxmai.com） */
    private String originalRecipient;

    /** 原始邮件主题（如 "论文审稿邀请"） */
    private String originalSubject;

    // ========== DSN标准字段（RFC 3464） ==========

    /** DSN状态码（如5.1.1、5.4.4等，来自delivery-status部分） */
    private String dsnStatus;

    /** DSN诊断码（来自delivery-status部分的Diagnostic-Code字段） */
    private String dsnDiagnosticCode;

    /** DSN Action字段（如 failed） */
    private String dsnAction;

    /** DSN Final-Recipient字段（如 rfc822;maojiangjun@faxmai.com） */
    private String finalRecipient;

    /** DSN Original-Recipient字段 */
    private String originalRecipientDsn;

    // ========== 匹配信息 ==========

    /** 匹配到的mail_send_record记录ID（NULL表示未匹配） */
    private Long matchedRecordId;

    /** 匹配策略：MESSAGE_ID/IN_REPLY_TO/REFERENCES/FINAL_RECIPIENT_TIME_WINDOW/ORIGINAL_RECIPIENT_TIME_WINDOW/RECEIVER_TIME_WINDOW/UNMATCHED */
    private String matchStrategy;

    /** 匹配置信度（0-100），数值越高表示匹配越可靠 */
    private Integer matchConfidence;

    /** 原始收件人信息来源：DSN_FINAL_RECIPIENT / DSN_ORIGINAL_RECIPIENT / HEADER / BODY_FALLBACK */
    private String recipientSource;

    // ========== 退信分类信息 ==========

    /** 退信类型 */
    private BounceType bounceType;

    /** 是否永久性退信：true-永久退信（禁止重发），false-临时退信（可重发） */
    private Boolean bouncePermanent;

    /** 退信代码（如550、5.1.1、DNS_ERROR等标准化分类码） */
    private String bounceCode;

    /** 退信原因（从退信邮件中提取的完整原因文本） */
    private String bounceReason;

    // ========== Header辅助字段 ==========

    /** In-Reply-To头（引用原始邮件Message-ID） */
    private String inReplyTo;

    /** References头 */
    @TableField(value = "references_header")
    private String referencesHeader;

    // ========== 时间和处理状态 ==========

    /** 退信邮件日期 */
    private LocalDateTime bounceDate;

    /** 是否已处理：false-未处理，true-已处理 */
    private Boolean processed;

    /** 处理时间 */
    private LocalDateTime processTime;

    /** 处理结果摘要 */
    private String processResult;

    /** 创建时间 */
    private LocalDateTime createTime;

    /** 更新时间 */
    private LocalDateTime updateTime;

    // ========== 兼容旧字段（保留向后兼容） ==========

    /**
     * @deprecated 使用 originalRecipient 替代，该字段仅为向后兼容保留
     */
    private String bouncedRecipient;
}