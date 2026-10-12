package com.youlai.boot.common.enums;

import com.fasterxml.jackson.annotation.JsonValue;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;

/**
 * 日志模块枚举
 *
 * @author Ray
 * @since 2.10.0
 */
@Schema(enumAsRef = true)
@Getter
public enum LogModuleEnum {

    EXCEPTION("异常"),
    LOGIN("登录"),
    USER("用户"),
    DEPT("部门"),
    ROLE("角色"),
    MENU("菜单"),
    DICT("字典"),
    SETTING("系统配置"),
    OTHER("其他"),

    GUEST("特邀嘉宾"),
    HOTEL("酒店信息"),
    QUESTION("问题解答"),
    CONTACT("联系人"),
    ARRANGE("会前安排"),
    MOMENTS("精彩瞬间"),
    MEETINGSETTING("会议设置"),
    SPEECH("演讲"),
    REGISTERUSER("注册用户"),
    PAPER_SUBMISSION("论文投稿"),
    EMAIL_TEMPLATE("邮箱模板"),
    CHECK_IN("签到记录"),
    ARCHITECTURE("组织架构");

    @JsonValue
    private final String moduleName;

    LogModuleEnum(String moduleName) {
        this.moduleName = moduleName;
    }
}
