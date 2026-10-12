package com.youlai.boot.config.property;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * IMAP 邮件配置类，用于退信检测定时任务连接收件箱。
 *
 * 配置前缀：spring.mail.imap
 * 需要在 application-dev.yml / application-prod.yml 中配置。
 *
 * @author system
 * @since 2026/7/31
 */
@ConfigurationProperties(prefix = "spring.mail.imap")
@Data
public class ImapProperties {

    /**
     * 是否启用退信检测定时任务。
     * 生产环境建议启用，开发环境可关闭。
     */
    private boolean enabled = false;

    /**
     * IMAP 服务器主机名。
     * 例如：imap.163.com
     */
    private String host;

    /**
     * IMAP 服务器端口号。
     * SSL 通常为 993，非加密为 143。
     */
    private int port = 993;

    /**
     * 登录用户名（通常与 SMTP 用户名相同）。
     */
    private String username;

    /**
     * 登录密码（通常与 SMTP 密码相同）。
     */
    private String password;

    /**
     * 是否使用 SSL 加密连接。
     * 推荐生产环境启用。
     */
    private boolean sslEnable = true;

    /**
     * 定时任务 cron 表达式。
     * 默认每5分钟执行一次。
     */
    private String cron = "0 */5 * * * ?";

    /**
     * 每次扫描最多处理的退信邮件数量。
     * 防止一次性处理过多邮件导致性能问题。
     */
    private int maxProcessCount = 50;

    /**
     * 退信检测的收件箱文件夹名称。
     * 不同邮件服务商可能有不同的退信文件夹：
     * - 163邮箱：INBOX（退信会进入收件箱）
     * - QQ邮箱：INBOX
     * - Gmail：INBOX
     */
    private String folderName = "INBOX";

    /**
     * 退信检测后是否自动重发邮件。
     */
    private boolean autoResend = true;

    /**
     * 退信自动重发最大次数（超过此次数不再自动重发，仅告警）。
     */
    private int autoResendMaxCount = 2;

    /**
     * 管理员告警邮箱地址（退信检测到时发送告警通知）。
     * 多个邮箱用逗号分隔。
     */
    private String adminAlertEmails;

    /**
     * 是否启用IMAP增量扫描（基于UID checkpoint）。
     * 启用后只扫描新增邮件，避免每次全量遍历INBOX。
     * 默认启用。
     */
    private boolean checkpointEnabled = true;

    /**
     * 退信检测分布式锁等待时间（秒）。
     * 防止多实例同时执行退信检测任务。
     * 默认0秒（不等待，获取不到锁直接跳过）。
     */
    private int lockWaitTime = 0;

    /**
     * 退信检测分布式锁持有时间（秒）。
     * 锁自动过期时间，防止任务异常退出后锁不释放。
     * 默认300秒（5分钟）。
     */
    private int lockLeaseTime = 300;

    /**
     * 退信匹配的时间窗口（天）。
     * 当无法通过Message-ID匹配退信时，按收件人+时间窗口匹配。
     * 默认7天。
     */
    private int matchTimeWindowDays = 7;

    /**
     * 退信检测首次扫描的回溯天数。
     * 首次启动增量扫描时，回溯N天内的邮件。
     * 默认30天。
     */
    private int firstScanLookbackDays = 30;

    /**
     * 永久性退信是否禁止自动重发。
     * 默认true（永久性退信如DNS错误、用户不存在等不应重发）。
     */
    private boolean permanentBounceBlockResend = true;

    /**
     * 退信告警防重复时间窗口（小时）。
     * 同一收件人在N小时内只发送一次告警，避免频繁告警。
     * 默认24小时。
     */
    private int alertDedupHours = 24;
}