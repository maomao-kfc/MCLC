package com.youlai.boot.support.mail.model.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.youlai.boot.support.mail.enums.BounceType;
import com.youlai.boot.support.mail.enums.DeliveryStatus;
import com.youlai.boot.support.mail.enums.MailBusinessType;
import com.youlai.boot.support.mail.enums.MailSendStatus;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 邮件发送记录实体（任务表设计）
 *
 * 核心理念：
 * 1. 日志永远不覆盖，永远新增 —— 保留完整发送历史
 * 2. 同时承担发送队列职责 —— status=待发送的记录可被定时任务扫描发送
 * 3. 每次发送都生成新记录，便于追溯和统计
 *
 * 双状态设计：
 * - send_status: SMTP阶段状态（0待发送→1成功/2失败→3重试中→1成功/2失败）
 * - delivery_status: 最终投递状态（0等待投递→1已投递/2已退信）
 *   SMTP成功(1)不代表最终投递成功，需要IMAP退信检测确认
 *
 * 状态流转：
 * send_status: 待发送(0) → 成功(1) / 失败(2) → 重试中(3) → 成功(1) / 失败(2)
 *              成功(1) → 退信-投递失败(5)（IMAP检测到退信后回退）
 * delivery_status: 等待投递(0) → 已退信(2)（IMAP检测到退信）
 *
 * @author system
 * @since 2026/7/24
 */
@TableName("mail_send_record")
@Getter
@Setter
public class MailSendRecord implements Serializable {

    /** 主键ID */
    @TableId(type = IdType.AUTO)
    private Long id;

    /** 业务类型（PAPER_SUBMIT/PAPER_PASS/PAPER_REJECT/USER_REGISTER等） */
    private MailBusinessType businessType;

    /** 业务ID（如paper_submission.id） */
    private Long businessId;

    /** 收件邮箱 */
    private String receiver;

    /** 收件人姓名 */
    private String receiverName;

    /** 邮件标题 */
    private String subject;

    /** 邮件正文（保留完整内容，便于追溯） */
    private String content;

    /** 发送状态：0-待发送, 1-成功, 2-失败, 3-重试中, 4-已取消, 5-退信-投递失败 */
    private MailSendStatus sendStatus;

    /** 投递状态：0-等待投递, 1-已投递, 2-已退信, 3-投递中 */
    private DeliveryStatus deliveryStatus;

    /** 重试次数 */
    private Integer retryCount;

    /** 错误信息（SMTP异常详情） */
    private String errorMessage;

    /** 退信原因（IMAP检测到退信时记录退信原因） */
    private String bounceReason;

    /** 退信代码（标准化退信分类码，如550、5.1.1、DNS_ERROR等） */
    private String bounceCode;

    /** 退信邮件的Message-ID（用于幂等去重，同一退信不重复处理） */
    private String bounceMessageId;

    /** IMAP退信检测扫描次数 */
    private Integer imapCheckCount;

    /** 最近一次IMAP退信检测时间 */
    private LocalDateTime lastImapCheckTime;

    /** 最近一次投递检查状态摘要 */
    private String lastDeliveryCheckStatus;

    /** 父记录ID（重发邮件关联原始退信记录，形成追踪链） */
    private Long parentRecordId;

    /** 记录类型：ORIGINAL-原始发送, RETRY-重试发送 */
    private String recordType;

    /** 重试序号：1=第一次重试, 2=第二次重试, NULL=原始记录 */
    private Integer retrySequence;

    /** 根记录ID（始终指向原始邮件记录，重试记录必填，原始记录等于自身ID） */
    private Long rootRecordId;

    /** SMTP返回的Message-ID（用于查重和退信匹配） */
    private String messageId;

    /** 发送耗时（毫秒） */
    private Long consumeTime;

    /** 发送账号（如notice@mclc.com） */
    private String sender;

    /** 实际发送成功时间 */
    private LocalDateTime sendTime;

    /** 投递成功时间（最终送达收件人邮箱的时间） */
    private LocalDateTime deliveryTime;

    /** 退信检测时间（IMAP检测到退信的时间） */
    private LocalDateTime bounceTime;

    /** 创建时间 */
    private LocalDateTime createTime;

    /** 更新时间 */
    private LocalDateTime updateTime;
}