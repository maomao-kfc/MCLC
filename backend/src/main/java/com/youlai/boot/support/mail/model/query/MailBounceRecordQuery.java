package com.youlai.boot.support.mail.model.query;

import com.youlai.boot.common.base.BaseQuery;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 邮件退信记录查询条件
 *
 * 支持按退信类型、永久/临时、收件人、匹配策略、处理状态等条件筛选。
 *
 * @author system
 * @since 2026/8/3
 */
@Schema(description = "邮件退信记录查询条件")
@Data
@EqualsAndHashCode(callSuper = true)
public class MailBounceRecordQuery extends BaseQuery {

    @Schema(description = "退信收件人地址（模糊搜索）")
    private String bouncedRecipient;

    @Schema(description = "退信类型：DNS_ERROR/USER_NOT_FOUND/MAILBOX_UNAVAILABLE/MAILBOX_FULL/SPAM_REJECTED/RELAY_DENIED/OTHER")
    private String bounceType;

    @Schema(description = "是否永久性退信：true-永久退信，false-临时退信")
    private Boolean bouncePermanent;

    @Schema(description = "匹配策略：MESSAGE_ID/IN_REPLY_TO/RECEIVER_TIME_WINDOW/UNMATCHED")
    private String matchStrategy;

    @Schema(description = "是否已处理：true-已处理，false-未处理")
    private Boolean processed;

    @Schema(description = "匹配到的发送记录ID")
    private Long matchedRecordId;

    @Schema(description = "开始时间（退信日期范围）")
    private String startTime;

    @Schema(description = "结束时间（退信日期范围）")
    private String endTime;
}