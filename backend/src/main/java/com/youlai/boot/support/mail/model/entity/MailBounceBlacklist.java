package com.youlai.boot.support.mail.model.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 邮件退信黑名单实体
 *
 * 记录永久无效的收件地址，发送前校验，避免无效重发浪费资源。
 * 来源分为两种：
 * - AUTO：IMAP退信检测自动识别永久性失败（如 550 User not found）
 * - MANUAL：管理员手动添加
 *
 * @author system
 * @since 2026/8/3
 */
@TableName("mail_bounce_blacklist")
@Getter
@Setter
public class MailBounceBlacklist implements Serializable {

    /** 主键ID */
    @TableId(type = IdType.AUTO)
    private Long id;

    /** 黑名单邮箱地址 */
    private String email;

    /** 加入黑名单原因（退信原因摘要） */
    private String reason;

    /** 来源：AUTO-自动检测, MANUAL-手动添加 */
    private String source;

    /** 累计退信次数 */
    private Integer bounceCount;

    /** 最近一次退信时间 */
    private LocalDateTime lastBounceTime;

    /** 关联的邮件发送记录ID */
    private Long relatedRecordId;

    /** 创建时间 */
    private LocalDateTime createTime;

    /** 更新时间 */
    private LocalDateTime updateTime;
}