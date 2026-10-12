package com.youlai.boot.support.mail.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.youlai.boot.common.base.IBaseEnum;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 退信类型枚举
 *
 * 分类标准基于 RFC 3463 (Enhanced Mail System Status Codes) 和常见退信场景。
 * 每种类型明确标记 permanent（永久退信）或临时退信，用于决定黑名单和自动重发策略。
 *
 * 永久退信（permanent=true）：
 * - USER_NOT_FOUND: 收件人不存在（550 User not found / 5.1.1）
 * - DOMAIN_NOT_FOUND: 域名不存在（NXDOMAIN / DNS query error: domain does not exist）
 * - MAILBOX_UNAVAILABLE: 邮箱不可用（550 Mailbox unavailable / 5.2.1）
 * - RELAY_DENIED: 中继拒绝（550 Relay denied）
 * - SPAM_REJECTED: 被识别为垃圾邮件（550 Spam rejected / 5.7.1）
 *
 * 临时退信（permanent=false）：
 * - DNS_TEMPORARY_FAILURE: DNS临时故障（SERVFAIL / DNS timeout）
 * - MAILBOX_FULL: 邮箱已满（552 Mailbox full / 4.2.2 / 5.2.2）
 * - RATE_LIMIT: 频率限制（450/552 rate limit）
 * - NETWORK_TEMPORARY_FAILURE: 网络临时故障
 * - DNS_ERROR: 其他DNS异常（无法确定永久/临时，默认临时）
 * - OTHER: 其他未分类退信（默认临时）
 *
 * @author system
 * @since 2026/8/3
 */
@Getter
@AllArgsConstructor
public enum BounceType implements IBaseEnum<String> {

    /** 收件人不存在（550 User not found / 5.1.1）— 永久退信 */
    USER_NOT_FOUND("USER_NOT_FOUND", "收件人不存在", true),

    /** 域名不存在（NXDOMAIN / domain not found / does not exist）— 永久退信 */
    DOMAIN_NOT_FOUND("DOMAIN_NOT_FOUND", "域名不存在", true),

    /** 邮箱不可用（550 Mailbox unavailable / 5.2.1）— 永久退信 */
    MAILBOX_UNAVAILABLE("MAILBOX_UNAVAILABLE", "邮箱不可用", true),

    /** 中继拒绝（550 Relay denied）— 永久退信 */
    RELAY_DENIED("RELAY_DENIED", "中继拒绝", true),

    /** 被识别为垃圾邮件（550 Spam rejected / 5.7.1）— 永久退信 */
    SPAM_REJECTED("SPAM_REJECTED", "垃圾邮件拒绝", true),

    /** DNS临时故障（SERVFAIL / DNS timeout / temporary failure）— 临时退信 */
    DNS_TEMPORARY_FAILURE("DNS_TEMPORARY_FAILURE", "DNS临时故障", false),

    /** 邮箱已满（552 Mailbox full / 4.2.2 / 5.2.2）— 临时退信 */
    MAILBOX_FULL("MAILBOX_FULL", "邮箱已满", false),

    /** 频率限制（450/552 rate limit）— 临时退信 */
    RATE_LIMIT("RATE_LIMIT", "频率限制", false),

    /** 网络临时故障 — 临时退信 */
    NETWORK_TEMPORARY_FAILURE("NETWORK_TEMPORARY_FAILURE", "网络临时故障", false),

    /** 其他DNS异常（无法确定永久/临时，默认临时）— 临时退信 */
    DNS_ERROR("DNS_ERROR", "DNS解析失败", false),

    /** 其他未分类退信（默认临时）— 临时退信 */
    OTHER("OTHER", "其他退信", false);

    /** 数据库存储值 */
    @EnumValue
    private final String value;

    /** 中文描述 */
    private final String label;

    /** 是否永久性退信 */
    private final boolean permanent;

    /**
     * 判断该退信类型是否为永久性退信
     *
     * 永久性退信：USER_NOT_FOUND、DOMAIN_NOT_FOUND、MAILBOX_UNAVAILABLE、SPAM_REJECTED、RELAY_DENIED
     * 临时性退信：DNS_TEMPORARY_FAILURE、MAILBOX_FULL、RATE_LIMIT、NETWORK_TEMPORARY_FAILURE、DNS_ERROR、OTHER
     */
    public boolean isPermanent() {
        return permanent;
    }

    /**
     * 根据值获取枚举
     */
    public static BounceType fromValue(String value) {
        return IBaseEnum.getEnumByValue(value, BounceType.class);
    }
}