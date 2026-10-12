package com.youlai.boot.support.mail.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.youlai.boot.common.base.IBaseEnum;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 邮件投递状态枚举（最终投递阶段）
 *
 * 与 send_status 的区别：
 * - send_status 表示 SMTP 阶段状态（邮件是否成功提交到邮件服务器）
 * - delivery_status 表示最终投递状态（邮件是否成功送达收件人邮箱）
 *
 * 状态流转：
 * SMTP成功(send_status=1) → 未知/等待投递(0)
 * 未知/等待投递(0) → 已退信(2)（IMAP检测到退信）
 * 未知/等待投递(0) → 已投递(1)（投递确认，目前暂不实现主动确认）
 *
 * @author system
 * @since 2026/8/3
 */
@Getter
@AllArgsConstructor
public enum DeliveryStatus implements IBaseEnum<Integer> {

    /** 未知/等待投递（SMTP成功但尚未确认最终投递结果） */
    UNKNOWN(0, "等待投递"),

    /** 已投递（邮件已成功送达收件人邮箱，目前暂不实现主动确认） */
    DELIVERED(1, "已投递"),

    /** 已退信（IMAP检测到退信，投递失败） */
    BOUNCED(2, "已退信"),

    /** 投递中（已开始投递流程，等待最终结果） */
    DELIVERING(3, "投递中");

    /** 数据库存储值 */
    @EnumValue
    private final Integer value;

    /** 中文描述 */
    private final String label;

    /**
     * 根据值获取枚举
     */
    public static DeliveryStatus fromValue(Integer value) {
        return IBaseEnum.getEnumByValue(value, DeliveryStatus.class);
    }
}