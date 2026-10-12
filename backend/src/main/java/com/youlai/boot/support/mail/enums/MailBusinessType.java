package com.youlai.boot.support.mail.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.youlai.boot.common.base.IBaseEnum;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 邮件业务类型枚举
 *
 * 用于标识邮件发送记录关联的业务场景，便于按类型统计和查询。
 * 扩展新业务类型时只需在此枚举中添加即可。
 *
 * @author system
 * @since 2026/7/24
 */
@Getter
@AllArgsConstructor
public enum MailBusinessType implements IBaseEnum<String> {

    /** 论文投稿审稿邮件（发给专家） */
    PAPER_SUBMIT("PAPER_SUBMIT", "论文审稿通知"),

    /** 论文审核通过通知（发给投稿者） */
    PAPER_PASS("PAPER_PASS", "论文审核通过"),

    /** 论文审核拒绝通知（发给投稿者） */
    PAPER_REJECT("PAPER_REJECT", "论文审核拒绝"),

    /** 注册成功通知 */
    USER_REGISTER("USER_REGISTER", "注册成功通知"),

    /** 密码重置 */
    RESET_PASSWORD("RESET_PASSWORD", "密码重置"),

    /** 会议提醒 */
    MEETING_NOTICE("MEETING_NOTICE", "会议提醒"),

    /** 管理员错误通知（系统内部） */
    ADMIN_ERROR_NOTIFY("ADMIN_ERROR_NOTIFY", "管理员错误通知");

    /** 数据库存储值 */
    @EnumValue
    private final String value;

    /** 中文描述 */
    private final String label;

    /**
     * 根据值获取枚举
     */
    public static MailBusinessType fromValue(String value) {
        return IBaseEnum.getEnumByValue(value, MailBusinessType.class);
    }
}