package com.youlai.boot.support.mail.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * IMAP退信检测执行状态枚举
 *
 * 用于追踪退信检测定时任务的执行阶段，区分各种失败原因，
 * 避免扫描0封邮件时无法区分"真的没有新邮件"和"链路断裂"。
 *
 * 状态流转：
 * NOT_STARTED → IMAP_CONNECTED → ID_SENT → FOLDER_OPENED → MESSAGE_FETCHED → SUCCESS
 * 任意阶段失败 → FAILED_XXX
 *
 * @author system
 * @since 2026/8/6
 */
@Getter
@AllArgsConstructor
public enum ImapExecutionStatus {

    /** 未开始 */
    NOT_STARTED("NOT_STARTED", "未开始"),

    /** IMAP连接成功 */
    IMAP_CONNECTED("IMAP_CONNECTED", "IMAP连接成功"),

    /** IMAP ID命令发送成功 */
    ID_SENT("ID_SENT", "ID命令发送成功"),

    /** Folder打开成功 */
    FOLDER_OPENED("FOLDER_OPENED", "Folder打开成功"),

    /** 邮件获取成功 */
    MESSAGE_FETCHED("MESSAGE_FETCHED", "邮件获取成功"),

    /** 执行完成 */
    SUCCESS("SUCCESS", "执行完成"),

    /** IMAP连接失败 */
    FAILED_IMAP_CONNECT("FAILED_IMAP_CONNECT", "IMAP连接失败"),

    /** ID命令发送失败 */
    FAILED_ID_COMMAND("FAILED_ID_COMMAND", "ID命令发送失败"),

    /** Folder打开失败 */
    FAILED_FOLDER_OPEN("FAILED_FOLDER_OPEN", "Folder打开失败"),

    /** 邮件获取失败 */
    FAILED_MESSAGE_FETCH("FAILED_MESSAGE_FETCH", "邮件获取失败"),

    /** 认证失败 */
    FAILED_AUTH("FAILED_AUTH", "认证失败"),

    /** 其他异常 */
    FAILED_OTHER("FAILED_OTHER", "其他异常");

    private final String code;
    private final String label;
}