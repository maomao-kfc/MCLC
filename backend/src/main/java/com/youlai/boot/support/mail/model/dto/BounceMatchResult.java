package com.youlai.boot.support.mail.model.dto;

import com.youlai.boot.support.mail.model.entity.MailSendRecord;
import lombok.Data;

/**
 * 退信匹配结果
 *
 * 记录退信与原始邮件发送记录的匹配结果，包括匹配策略、置信度和匹配到的记录。
 *
 * 匹配策略优先级（置信度从高到低）：
 * 1. MESSAGE_ID (100) - 原始Message-ID精确匹配
 * 2. IN_REPLY_TO (95) - In-Reply-To精确匹配
 * 3. REFERENCES (90) - References精确匹配
 * 4. FINAL_RECIPIENT_TIME_WINDOW (80) - DSN Final-Recipient + 时间窗口
 * 5. ORIGINAL_RECIPIENT_TIME_WINDOW (70) - DSN Original-Recipient + 时间窗口
 * 6. RECEIVER_TIME_WINDOW (50) - 退信收件人 + 时间窗口
 * 7. UNMATCHED (0) - 未匹配
 *
 * @author system
 * @since 2026/8/3
 */
@Data
public class BounceMatchResult {

    /** 是否匹配成功 */
    private boolean matched;

    /** 匹配到的原始邮件发送记录 */
    private MailSendRecord record;

    /** 匹配策略名称：MESSAGE_ID / IN_REPLY_TO / REFERENCES / FINAL_RECIPIENT_TIME_WINDOW / ORIGINAL_RECIPIENT_TIME_WINDOW / RECEIVER_TIME_WINDOW / UNMATCHED */
    private String strategy;

    /** 匹配置信度（0-100），数值越高表示匹配越可靠 */
    private Integer confidence;

    /** 匹配使用的Message-ID（MESSAGE_ID/IN_REPLY_TO/REFERENCES策略时有值） */
    private String matchedMessageId;

    /** 匹配使用的收件人地址（RECEIVER_TIME_WINDOW等策略时有值） */
    private String matchedRecipient;

    /**
     * 创建未匹配结果
     */
    public static BounceMatchResult unmatched() {
        BounceMatchResult result = new BounceMatchResult();
        result.setMatched(false);
        result.setStrategy("UNMATCHED");
        result.setConfidence(0);
        return result;
    }

    /**
     * 创建匹配成功结果
     */
    public static BounceMatchResult matched(MailSendRecord record, String strategy, int confidence,
                                             String matchedMessageId, String matchedRecipient) {
        BounceMatchResult result = new BounceMatchResult();
        result.setMatched(true);
        result.setRecord(record);
        result.setStrategy(strategy);
        result.setConfidence(confidence);
        result.setMatchedMessageId(matchedMessageId);
        result.setMatchedRecipient(matchedRecipient);
        return result;
    }
}