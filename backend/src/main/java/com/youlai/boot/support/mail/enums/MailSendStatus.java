package com.youlai.boot.support.mail.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.youlai.boot.common.base.IBaseEnum;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 邮件发送状态枚举
 *
 * 状态流转：
 * 待发送(0) → 成功(1)
 * 待发送(0) → 失败(2) → 重试中(3) → 成功(1) / 失败(2)
 * 待发送(0) → 已取消(4)
 * 成功(1) → 退信-投递失败(5)（IMAP定时任务检测到退信后回退）
 * 退信后自动重试：待重试(6) → 重试成功(7) / 重试失败(8)
 *
 * @author system
 * @since 2026/7/24
 */
@Getter
@AllArgsConstructor
public enum MailSendStatus implements IBaseEnum<Integer> {

    /** 待发送（已创建记录，尚未调用SMTP） */
    PENDING(0, "待发送"),

    /** 已提交至邮件服务器（SMTP返回250 OK，但不代表最终投递成功） */
    SUCCESS(1, "已提交至邮件服务器"),

    /** 发送失败（SMTP异常或超时） */
    FAILED(2, "发送失败"),

    /** 重试中（失败后正在重试） */
    RETRYING(3, "重试中"),

    /** 已取消（管理员手动取消） */
    CANCELLED(4, "已取消"),

    /** 退信检测到（IMAP检测到退信，投递失败） */
    BOUNCE_DETECTED(5, "退信-投递失败"),

    /** 待重试（退信后自动重试，等待发送） */
    RETRY_PENDING(6, "待重试"),

    /** 重试成功（重试邮件SMTP发送成功，但不代表最终投递成功） */
    RETRY_SUCCESS(7, "重试成功"),

    /** 重试失败（重试邮件SMTP发送失败） */
    RETRY_FAILED(8, "重试失败");

    /** 数据库存储值 */
    @EnumValue
    private final Integer value;

    /** 中文描述 */
    private final String label;

    /**
     * 根据值获取枚举
     */
    public static MailSendStatus fromValue(Integer value) {
        return IBaseEnum.getEnumByValue(value, MailSendStatus.class);
    }
}