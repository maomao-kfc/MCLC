package com.youlai.boot.support.mail.model.dto;

import lombok.Data;

/**
 * 邮件发送结果DTO
 *
 * 包含SMTP发送的详细信息：成功/失败、Message-ID、耗时、错误信息。
 * 由 MailService 返回，供 MailSendRecordService 记录到数据库。
 *
 * @author system
 * @since 2026/7/24
 */
@Data
public class MailSendResult {

    /** 是否发送成功 */
    private boolean success;

    /** SMTP返回的Message-ID（发送成功时有值，用于查重） */
    private String messageId;

    /** 发送耗时（毫秒） */
    private long consumeTime;

    /** 错误信息（发送失败时有值） */
    private String errorMessage;

    /**
     * 创建成功结果
     */
    public static MailSendResult success(String messageId, long consumeTime) {
        MailSendResult result = new MailSendResult();
        result.setSuccess(true);
        result.setMessageId(messageId);
        result.setConsumeTime(consumeTime);
        return result;
    }

    /**
     * 创建失败结果
     */
    public static MailSendResult fail(String errorMessage, long consumeTime) {
        MailSendResult result = new MailSendResult();
        result.setSuccess(false);
        result.setErrorMessage(errorMessage);
        result.setConsumeTime(consumeTime);
        return result;
    }
}