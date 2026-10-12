package com.youlai.boot.support.mail.model.vo;

import com.youlai.boot.support.mail.enums.DeliveryStatus;
import com.youlai.boot.support.mail.enums.MailBusinessType;
import com.youlai.boot.support.mail.enums.MailSendStatus;
import com.youlai.boot.support.mail.model.entity.MailSendRecord;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 邮件发送记录详情VO
 *
 * 将实体中的枚举字段转为前端友好的 Integer/String 类型，
 * 避免枚举序列化为复杂对象导致前端条件判断失败。
 *
 * @author system
 * @since 2026/7/27
 */
@Schema(description = "邮件发送记录详情")
@Data
public class MailSendRecordDetailVO {

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

    @Schema(description = "邮件正文")
    private String content;

    @Schema(description = "发送状态：0-待发送, 1-成功, 2-失败, 3-重试中, 4-已取消, 5-退信-投递失败, 6-待重试, 7-重试成功, 8-重试失败")
    private Integer sendStatus;

    @Schema(description = "发送状态描述")
    private String sendStatusDesc;

    @Schema(description = "重试次数")
    private Integer retryCount;

    @Schema(description = "错误信息")
    private String errorMessage;

    @Schema(description = "退信原因")
    private String bounceReason;

    @Schema(description = "投递状态：0-等待投递, 1-已投递, 2-已退信, 3-投递中")
    private Integer deliveryStatus;

    @Schema(description = "投递状态描述")
    private String deliveryStatusDesc;

    @Schema(description = "退信代码（标准化退信分类码，如550、5.1.1、DNS_ERROR等）")
    private String bounceCode;

    @Schema(description = "退信邮件Message-ID（用于幂等去重）")
    private String bounceMessageId;

    @Schema(description = "IMAP检查次数")
    private Integer imapCheckCount;

    @Schema(description = "最近IMAP检查时间")
    private LocalDateTime lastImapCheckTime;

    @Schema(description = "父记录ID（重发邮件关联原始退信记录）")
    private Long parentRecordId;

    @Schema(description = "记录类型：ORIGINAL-原始邮件, RETRY-重试邮件")
    private String recordType;

    @Schema(description = "重试序号：1/2标识第几次重试，NULL为原始邮件")
    private Integer retrySequence;

    @Schema(description = "原始记录ID：始终指向原始邮件记录ID（扁平关联）")
    private Long rootRecordId;

    @Schema(description = "投递成功时间")
    private LocalDateTime deliveryTime;

    @Schema(description = "退信检测时间")
    private LocalDateTime bounceTime;

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

    @Schema(description = "更新时间")
    private LocalDateTime updateTime;

    /**
     * 从实体转换为详情VO
     */
    public static MailSendRecordDetailVO fromEntity(MailSendRecord record) {
        if (record == null) {
            return null;
        }
        MailSendRecordDetailVO vo = new MailSendRecordDetailVO();
        vo.setId(record.getId());
        vo.setBusinessType(record.getBusinessType() != null ? record.getBusinessType().getValue() : null);
        vo.setBusinessTypeDesc(record.getBusinessType() != null ? record.getBusinessType().getLabel() : null);
        vo.setBusinessId(record.getBusinessId());
        vo.setReceiver(record.getReceiver());
        vo.setReceiverName(record.getReceiverName());
        vo.setSubject(record.getSubject());
        vo.setContent(record.getContent());
        vo.setSendStatus(record.getSendStatus() != null ? record.getSendStatus().getValue() : null);
        vo.setSendStatusDesc(record.getSendStatus() != null ? record.getSendStatus().getLabel() : null);
        vo.setRetryCount(record.getRetryCount());
        vo.setErrorMessage(record.getErrorMessage());
        vo.setBounceReason(record.getBounceReason());
        vo.setDeliveryStatus(record.getDeliveryStatus() != null ? record.getDeliveryStatus().getValue() : null);
        vo.setDeliveryStatusDesc(record.getDeliveryStatus() != null ? record.getDeliveryStatus().getLabel() : null);
        vo.setBounceCode(record.getBounceCode());
        vo.setBounceMessageId(record.getBounceMessageId());
        vo.setImapCheckCount(record.getImapCheckCount());
        vo.setLastImapCheckTime(record.getLastImapCheckTime());
        vo.setParentRecordId(record.getParentRecordId());
        vo.setRecordType(record.getRecordType());
        vo.setRetrySequence(record.getRetrySequence());
        vo.setRootRecordId(record.getRootRecordId());
        vo.setDeliveryTime(record.getDeliveryTime());
        vo.setBounceTime(record.getBounceTime());
        vo.setMessageId(record.getMessageId());
        vo.setConsumeTime(record.getConsumeTime());
        vo.setSender(record.getSender());
        vo.setSendTime(record.getSendTime());
        vo.setCreateTime(record.getCreateTime());
        vo.setUpdateTime(record.getUpdateTime());
        return vo;
    }
}