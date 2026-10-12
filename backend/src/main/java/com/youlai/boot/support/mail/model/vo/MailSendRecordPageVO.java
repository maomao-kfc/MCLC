package com.youlai.boot.support.mail.model.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 邮件发送记录分页VO
 *
 * @author system
 * @since 2026/7/24
 */
@Schema(description = "邮件发送记录分页对象")
@Data
public class MailSendRecordPageVO {

    @Schema(description = "ID")
    private Long id;

    @Schema(description = "业务类型")
    private String businessType;

    @Schema(description = "业务类型描述")
    private String businessTypeDesc;

    @Schema(description = "业务ID")
    private Long businessId;

    @Schema(description = "收件邮箱")
    private String receiver;

    @Schema(description = "收件人姓名")
    private String receiverName;

    @Schema(description = "邮件标题")
    private String subject;

    @Schema(description = "发送状态")
    private Integer sendStatus;

    @Schema(description = "发送状态描述")
    private String sendStatusDesc;

    @Schema(description = "重试次数")
    private Integer retryCount;

    @Schema(description = "错误信息")
    private String errorMessage;

    @Schema(description = "退信原因（IMAP检测到退信时记录）")
    private String bounceReason;

    @Schema(description = "投递状态：0-等待投递, 1-已投递, 2-已退信, 3-投递中")
    private Integer deliveryStatus;

    @Schema(description = "投递状态描述")
    private String deliveryStatusDesc;

    @Schema(description = "退信代码（标准化退信分类码）")
    private String bounceCode;

    @Schema(description = "退信类型（DNS_ERROR/USER_NOT_FOUND等）")
    private String bounceType;

    @Schema(description = "是否永久性退信：true-永久退信，false-临时退信")
    private Boolean bouncePermanent;

    @Schema(description = "父记录ID（重发邮件关联原始退信记录）")
    private Long parentRecordId;

    @Schema(description = "记录类型：ORIGINAL-原始发送, RETRY-重试发送")
    private String recordType;

    @Schema(description = "重试序号：1=第一次重试, 2=第二次重试, NULL=原始记录")
    private Integer retrySequence;

    @Schema(description = "根记录ID（始终指向原始邮件记录）")
    private Long rootRecordId;

    @Schema(description = "SMTP Message-ID")
    private String messageId;

    @Schema(description = "发送耗时(ms)")
    private Long consumeTime;

    @Schema(description = "发送账号")
    private String sender;

    @Schema(description = "发送时间")
    private LocalDateTime sendTime;

    @Schema(description = "创建时间")
    private LocalDateTime createTime;

    @Schema(description = "子记录列表（重试记录）")
    private List<MailSendRecordPageVO> children;
}