package com.youlai.boot.support.mail.job;

import com.youlai.boot.config.property.ImapProperties;
import com.youlai.boot.support.mail.enums.BounceType;
import com.youlai.boot.support.mail.enums.ImapExecutionStatus;
import com.youlai.boot.support.mail.model.dto.BounceParseResult;
import com.youlai.boot.support.mail.model.entity.MailImapCheckpoint;
import com.youlai.boot.support.mail.parser.BounceParser;
import com.youlai.boot.support.mail.service.MailImapCheckpointService;
import com.youlai.boot.support.mail.service.MailSendRecordService;
import jakarta.annotation.PostConstruct;
import jakarta.mail.*;
import jakarta.mail.search.ComparisonTerm;
import jakarta.mail.search.FlagTerm;
import jakarta.mail.search.ReceivedDateTerm;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.lang.reflect.Method;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 退信检测定时任务（增强版）
 *
 * 通过 IMAP 协议连接发件邮箱收件箱，增量扫描退信邮件，
 * 使用BounceParser统一退信分类，调用handleBounceEnhanced实现5级匹配，
 * 更新delivery_status+send_status双状态，永久退信加黑名单，临时退信自动重发。
 *
 * 增强特性：
 * 1. 增量扫描：基于UID checkpoint，只扫描新增邮件，避免重复处理
 * 2. 分布式锁：Redisson防止多实例同时执行退信检测
 * 3. BounceParser：统一退信分类（15+正则模式，DSN>SMTP>DNS>正文四级分类）
 * 4. 5级匹配：MESSAGE_ID→IN_REPLY_TO→RECEIVER_TIME_WINDOW→UNMATCHED
 * 5. 双状态更新：delivery_status=BOUNCED + send_status=BOUNCE_DETECTED
 * 6. 永久/临时区分：永久退信加黑名单禁止重发，临时退信自动重发+parentRecordId追踪
 * 7. 幂等保证：bounce_message_id唯一索引+saveIfAbsent双重检查
 *
 * 退信邮件识别策略：
 * 1. 邮件主题包含常见退信关键词（failure notification、undelivered、returned mail 等）
 * 2. 邮件发送者为邮件系统自动回复地址（mailer-daemon、postmaster 等）
 * 3. Content-Type 为 multipart/report 且 report-type 为 delivery-status
 *
 * @author system
 * @since 2026/7/31
 */
@Component
@Slf4j
@RequiredArgsConstructor
public class BounceEmailJob {

    private final MailSendRecordService mailSendRecordService;
    private final ImapProperties imapProperties;
    private final MailImapCheckpointService mailImapCheckpointService;
    private final RedissonClient redissonClient;

    /** 分布式锁名称 */
    private static final String LOCK_KEY = "mail:bounce-check";

    /**
     * 启动时输出退信检测配置信息，便于确认定时任务是否已注册
     */
    @PostConstruct
    public void init() {
        if (imapProperties.isEnabled()) {
            log.info("====== 退信检测定时任务已注册 ======");
            log.info("IMAP配置: host={}, port={}, username={}, folder={}, ssl={}",
                    imapProperties.getHost(), imapProperties.getPort(),
                    imapProperties.getUsername(), imapProperties.getFolderName(),
                    imapProperties.isSslEnable());
            log.info("调度配置: cron={}, maxProcessCount={}, checkpointEnabled={}",
                    imapProperties.getCron(), imapProperties.getMaxProcessCount(),
                    imapProperties.isCheckpointEnabled());
            log.info("退信策略: autoResend={}, autoResendMaxCount={}, permanentBounceBlockResend={}, matchTimeWindowDays={}天",
                    imapProperties.isAutoResend(), imapProperties.getAutoResendMaxCount(),
                    imapProperties.isPermanentBounceBlockResend(), imapProperties.getMatchTimeWindowDays());
            log.info("首次扫描回溯: {}天, 告警邮箱: {}", imapProperties.getFirstScanLookbackDays(), imapProperties.getAdminAlertEmails());
            log.info("=======================================");
        } else {
            log.info("退信检测定时任务已注册但未启用（spring.mail.imap.enabled=false）");
        }
    }

    /** 退信关键词模式（不区分大小写，覆盖国内外常见邮箱退信主题） */
    private static final Pattern BOUNCE_SUBJECT_PATTERN = Pattern.compile(
            "(failure notice|undelivered|returned mail|delivery failure|mail delivery|delivery status notification" +
            "|退信|投递失败|无法投递|被退回|发送失败通知|邮件退回|投递错误|无法送达|地址不存在|收件人不存在|用户不存在|邮箱不存在)",
            Pattern.CASE_INSENSITIVE
    );

    /** 自动回复发送者模式 */
    private static final Pattern BOUNCE_SENDER_PATTERN = Pattern.compile(
            "(mailer-daemon|postmaster|mail subsystem|mail delivery subsystem|noreply|no-reply)",
            Pattern.CASE_INSENSITIVE
    );

    /** Message-ID 提取模式（匹配 <...> 格式） */
    private static final Pattern MESSAGE_ID_PATTERN = Pattern.compile(
            "<([^>]+)>"
    );

    /** 退信收件人提取模式（适配国内外邮箱退信格式，包括网易550 User not found格式） */
    private static final Pattern BOUNCED_RECIPIENT_PATTERN = Pattern.compile(
            "(?:failed|undeliverable|refused|not found|no such user|unknown user|doesn't exist|does not exist|said\\s+550|RCPT TO)[^\\n]*?([a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,})",
            Pattern.CASE_INSENSITIVE
    );

    /**
     * 定时扫描退信邮件
     *
     * 改造要点：
     * 1. 分布式锁：防止多实例同时执行退信检测
     * 2. 增量扫描：基于UID checkpoint，只扫描新增邮件
     * 3. BounceParser：统一退信分类逻辑
     * 4. handleBounceEnhanced：5级匹配+delivery_status+永久/临时区分重发
     *
     * cron 表达式由 spring.mail.imap.cron 配置。
     */
    @Scheduled(cron = "${spring.mail.imap.cron:0 */5 * * * ?}")
    public void detectBounceEmails() {
        if (!imapProperties.isEnabled()) {
            return;
        }

        // 分布式锁：防止多实例同时执行退信检测
        RLock lock = redissonClient.getLock(LOCK_KEY);
        boolean locked = false;
        try {
            locked = lock.tryLock(imapProperties.getLockWaitTime(), imapProperties.getLockLeaseTime(), java.util.concurrent.TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            log.warn("退信检测：获取分布式锁被中断");
            return;
        }
        if (!locked) {
            log.info("退信检测：未获取到分布式锁，跳过本次执行（可能其他实例正在执行）");
            return;
        }

        log.info("退信检测定时任务开始执行...");
        long startTime = System.currentTimeMillis();
        ImapExecutionStatus executionStatus = ImapExecutionStatus.NOT_STARTED;

        Store store = null;
        Folder folder = null;
        int processedCount = 0;
        int bounceCount = 0;

        try {
            // 1. 建立 IMAP 连接
            store = connectImap();
            if (store == null) {
                executionStatus = ImapExecutionStatus.FAILED_IMAP_CONNECT;
                log.error("退信检测：IMAP连接失败，跳过本次执行 [executionStatus={}]", executionStatus);
                return;
            }
            executionStatus = ImapExecutionStatus.IMAP_CONNECTED;

            // 2. 发送IMAP ID命令（163/126邮箱要求，否则报"Unsafe Login"）
            //    必须在打开文件夹之前发送，163要求ID命令在SELECT之前
            //    ID失败直接抛出MessagingException，阻止后续folder.open()产生误导性日志
            sendImapId(store);
            executionStatus = ImapExecutionStatus.ID_SENT;

            // 3. 打开收件箱（读写模式，需要标记已读）
            folder = store.getFolder(imapProperties.getFolderName());
            folder.open(Folder.READ_WRITE);
            executionStatus = ImapExecutionStatus.FOLDER_OPENED;

            // 3. 增量扫描：基于UID checkpoint
            Message[] messages;
            long currentUidValidity = 0;
            long lastUid = 0;

            if (imapProperties.isCheckpointEnabled() && folder instanceof UIDFolder uidFolder) {
                // 增量扫描模式
                currentUidValidity = uidFolder.getUIDValidity();

                // 获取检查点
                MailImapCheckpoint checkpoint =
                        mailImapCheckpointService.getCheckpoint(imapProperties.getFolderName(), imapProperties.getUsername());

                if (checkpoint != null) {
                    // 检查UIDVALIDITY是否变化（邮箱UID重置）
                    if (checkpoint.getUidValidity() != null && !checkpoint.getUidValidity().equals(currentUidValidity)) {
                        log.warn("退信检测：UIDVALIDITY变化（{}→{}），重置检查点，将全量扫描",
                                checkpoint.getUidValidity(), currentUidValidity);
                        mailImapCheckpointService.resetCheckpoint(imapProperties.getFolderName(), imapProperties.getUsername());
                        lastUid = 0;
                    } else {
                        lastUid = checkpoint.getLastUid() != null ? checkpoint.getLastUid() : 0;
                    }
                } else {
                    // 首次扫描：回溯N天
                    int lookbackDays = imapProperties.getFirstScanLookbackDays();
                    log.info("退信检测：首次扫描，回溯{}天内的邮件", lookbackDays);
                    lastUid = 0; // 标记为首次扫描
                }

                // 获取UID > lastUid的邮件
                if (lastUid > 0) {
                    // 增量：只扫描新邮件（UID > lastUid）
                    messages = uidFolder.getMessagesByUID(lastUid + 1, UIDFolder.LASTUID);
                    // 过滤掉null元素（某些UID可能不存在）
                    messages = java.util.Arrays.stream(messages)
                            .filter(java.util.Objects::nonNull)
                            .toArray(Message[]::new);
                    log.info("退信检测：增量扫描，lastUid={}，新邮件数={}", lastUid, messages.length);
                } else {
                    // 首次扫描或UIDVALIDITY变化后：使用ReceivedDateTerm按时间过滤，避免扫描整个收件箱
                    int lookbackDays = imapProperties.getFirstScanLookbackDays();
                    Date lookbackDate = Date.from(LocalDate.now().minusDays(lookbackDays)
                            .atStartOfDay(ZoneId.systemDefault()).toInstant());
                    messages = folder.search(new ReceivedDateTerm(ComparisonTerm.GE, lookbackDate));
                    log.info("退信检测：首次扫描，回溯{}天（{}），邮件数={}", lookbackDays, lookbackDate, messages.length);
                }
            } else {
                // 降级：全量扫描未读邮件（兼容不支持UID的邮箱或禁用checkpoint）
                messages = folder.search(new FlagTerm(new Flags(Flags.Flag.SEEN), false));
                log.info("退信检测：全量扫描未读邮件，邮件数={}", messages.length);
            }
            executionStatus = ImapExecutionStatus.MESSAGE_FETCHED;

            if (messages.length == 0) {
                executionStatus = ImapExecutionStatus.SUCCESS;
                log.info("退信检测：无新邮件，跳过 [executionStatus=SUCCESS，链路正常]");
                // 更新检查点（即使没有新邮件也要更新时间）
                if (imapProperties.isCheckpointEnabled() && folder instanceof UIDFolder uidFolder) {
                    updateCheckpointAfterScan(uidFolder, messages, currentUidValidity, processedCount, bounceCount);
                }
                return;
            }

            // 4. 遍历邮件，识别退信
            int maxProcess = imapProperties.getMaxProcessCount();

            for (int i = messages.length - 1; i >= 0 && processedCount < maxProcess; i--) {
                try {
                    Message message = messages[i];
                    if (isBounceEmail(message)) {
                        String subject = null;
                        try { subject = message.getSubject(); } catch (Exception ignored) {}
                        log.info("退信检测：识别到退信邮件，索引={}，主题={}", i, subject);
                        bounceCount += processBounceEmail(message);
                    }
                    processedCount++;
                } catch (Exception e) {
                    log.error("退信检测：处理第{}封邮件异常", i, e);
                    processedCount++;
                }
            }

            // 5. 更新检查点
            if (imapProperties.isCheckpointEnabled() && folder instanceof UIDFolder uidFolder) {
                updateCheckpointAfterScan(uidFolder, messages, currentUidValidity, processedCount, bounceCount);
            }
            executionStatus = ImapExecutionStatus.SUCCESS;

        } catch (AuthenticationFailedException e) {
            executionStatus = ImapExecutionStatus.FAILED_AUTH;
            log.error("退信检测：IMAP认证失败，请检查用户名和密码配置 [executionStatus={}]", executionStatus, e);
        } catch (FolderClosedException e) {
            executionStatus = ImapExecutionStatus.FAILED_FOLDER_OPEN;
            log.error("退信检测：邮件文件夹已关闭 [executionStatus={}]", executionStatus, e);
        } catch (MessagingException e) {
            // 根据当前执行阶段判断失败原因
            if (executionStatus == ImapExecutionStatus.ID_SENT || executionStatus == ImapExecutionStatus.IMAP_CONNECTED) {
                executionStatus = ImapExecutionStatus.FAILED_FOLDER_OPEN;
            } else if (executionStatus == ImapExecutionStatus.IMAP_CONNECTED) {
                // sendImapId()抛出MessagingException时，executionStatus仍为IMAP_CONNECTED
                executionStatus = ImapExecutionStatus.FAILED_ID_COMMAND;
            } else if (executionStatus == ImapExecutionStatus.FOLDER_OPENED) {
                executionStatus = ImapExecutionStatus.FAILED_MESSAGE_FETCH;
            } else {
                executionStatus = ImapExecutionStatus.FAILED_OTHER;
            }
            // 针对163/126邮箱"Unsafe Login"错误提供详细排查指引
            String msg = e.getMessage();
            if (msg != null && msg.contains("Unsafe Login")) {
                executionStatus = ImapExecutionStatus.FAILED_FOLDER_OPEN;
                log.error("====== 163邮箱IMAP \"Unsafe Login\" 错误 [executionStatus={}] ======", executionStatus);
                log.error("错误原因：163/126邮箱要求IMAP ID命令（RFC 2971）标识客户端身份");
                log.error("代码已内置ID命令发送逻辑，如仍报此错误，请逐项检查：");
                log.error("  1. IMAP服务是否在163网页端开启（设置 → POP3/SMTP/IMAP → 开启IMAP）");
                log.error("  2. 授权密码（客户端授权码）是否正确且仍有效（非邮箱登录密码）");
                log.error("  3. 【关键】服务器公网IP是否添加到163邮箱「客户端授权IP白名单」");
                log.error("     操作路径：163网页端 → 设置 → POP3/SMTP/IMAP → 客户端授权密码 → 授权IP管理 → 添加服务器公网IP");
                log.error("  4. 如使用Docker部署，需添加宿主机公网IP（非容器内网IP）");
                log.error("  参考：https://help.mail.163.com/faqDetail/0/197");
                log.error("========================================");
            } else if (executionStatus == ImapExecutionStatus.FAILED_ID_COMMAND) {
                log.error("====== IMAP ID命令发送失败 [executionStatus={}] ======", executionStatus);
                log.error("ID命令失败意味着后续folder.open()也会失败（163邮箱报Unsafe Login）");
                log.error("请检查：");
                log.error("  1. IMAP服务是否在163网页端开启（设置 → POP3/SMTP/IMAP）");
                log.error("  2. 授权密码（客户端授权码）是否正确且仍有效");
                log.error("  3. 服务器公网IP是否添加到163邮箱「客户端授权IP白名单」");
                log.error("     163网页端 → 设置 → POP3/SMTP/IMAP → 客户端授权密码 → 授权IP管理");
                log.error("  参考：https://help.mail.163.com/faqDetail/0/197");
                log.error("========================================");
            } else {
                log.error("退信检测：执行异常 [executionStatus={}]", executionStatus, e);
            }
        } finally {
            // 6. 关闭连接
            closeQuietly(folder, store);
            // 7. 释放分布式锁
            if (lock.isHeldByCurrentThread()) {
                lock.unlock();
            }
        }

        long elapsed = System.currentTimeMillis() - startTime;
        log.info("退信检测定时任务执行完成：扫描邮件数={}, 退信处理数={}, 耗时={}ms, executionStatus={}",
                processedCount, bounceCount, elapsed, executionStatus);
    }

    /**
     * 扫描完成后更新检查点
     */
    private void updateCheckpointAfterScan(UIDFolder uidFolder, Message[] messages,
                                            long uidValidity, int checkCount, int bounceCount) {
        try {
            long maxUid = 0;
            for (Message msg : messages) {
                try {
                    long uid = uidFolder.getUID(msg);
                    if (uid > maxUid) {
                        maxUid = uid;
                    }
                } catch (Exception ignored) {}
            }
            if (maxUid > 0) {
                mailImapCheckpointService.updateCheckpoint(
                        imapProperties.getFolderName(), imapProperties.getUsername(),
                        uidValidity, maxUid, checkCount, bounceCount);
            }
        } catch (Exception e) {
            log.error("退信检测：更新检查点异常", e);
        }
    }

    /**
     * 建立 IMAP 连接
     */
    private Store connectImap() {
        try {
            Properties props = new Properties();
            props.put("mail.store.protocol", "imaps");
            props.put("mail.imap.ssl.enable", String.valueOf(imapProperties.isSslEnable()));
            props.put("mail.imap.ssl.trust", "*");
            props.put("mail.imap.timeout", "10000");
            props.put("mail.imap.connectiontimeout", "10000");

            Session session = Session.getInstance(props);
            session.setDebug(false);

            Store store = session.getStore("imaps");
            store.connect(
                    imapProperties.getHost(),
                    imapProperties.getPort(),
                    imapProperties.getUsername(),
                    imapProperties.getPassword()
            );

            log.info("退信检测：IMAP连接成功，host={}", imapProperties.getHost());
            return store;
        } catch (jakarta.mail.AuthenticationFailedException e) {
            String errorMsg = e.getMessage();
            log.error("退信检测：IMAP认证失败，host={}, username={}", imapProperties.getHost(), imapProperties.getUsername());
            // 163/126邮箱"Unsafe Login"错误：网易邮箱要求IMAP ID命令（RFC 2971）
            if (errorMsg != null && errorMsg.contains("Unsafe Login")) {
                log.error("====== 163邮箱IMAP \"Unsafe Login\" 错误 ======");
                log.error("错误原因：163/126邮箱不遵循标准IMAP规范，要求客户端发送IMAP ID命令（RFC 2971）标识身份");
                log.error("代码已内置ID命令发送逻辑，如仍报此错误，请检查：");
                log.error("  1. IMAP服务是否在163网页端开启（设置 → POP3/SMTP/IMAP）");
                log.error("  2. 授权密码（客户端授权码）是否正确且仍有效");
                log.error("  3. 服务器公网IP是否添加到163邮箱客户端授权IP白名单");
                log.error("  参考：https://help.mail.163.com/faqDetail/0/197");
                log.error("========================================");
            } else if (errorMsg != null && (errorMsg.contains("LOGIN") || errorMsg.contains("AUTHENTICATE") || errorMsg.contains("credential"))) {
                log.error("====== IMAP用户名或密码错误 ======");
                log.error("请检查application-prod.yml中spring.mail.imap.username和password是否正确");
                log.error("注意：163/126邮箱IMAP需使用「授权密码」（客户端授权码），不是邮箱登录密码");
                log.error("========================================");
            } else {
                log.error("IMAP认证失败详情：{}", errorMsg, e);
            }
            return null;
        } catch (Exception e) {
            log.error("退信检测：IMAP连接失败，host={}, port={}", imapProperties.getHost(), imapProperties.getPort(), e);
            return null;
        }
    }

    /**
     * 发送IMAP ID命令（RFC 2971）
     *
     * 163/126邮箱不遵循标准IMAP规范，要求客户端在连接后、打开文件夹前发送ID命令标识自身，
     * 否则执行任何文件夹操作（SELECT）时会返回"A3 NO SELECT Unsafe Login"错误。
     * 这是网易邮箱的安全策略，用于识别客户端类型。
     *
     * 必须在Store级别发送ID命令（IMAPStore.id()），而不是Folder级别。
     * IMAPFolder.id()要求文件夹已打开（checkOpened），但163要求ID在SELECT之前发送，
     * 形成鸡生蛋问题。Store级别的id()在连接建立后即可发送，无此限制。
     *
     * 使用反射调用IMAPStore.id()方法，避免对angus-mail的编译时依赖（其scope为runtime）。
     * 注意：IMAPSSLStore继承自IMAPStore，id()方法定义在父类IMAPStore中，
     * 因此需要遍历类层次查找方法，不能只查当前class。
     *
     * 163邮箱ID命令要求：
     * 1. 必须在SELECT/EXAMINE命令之前发送
     * 2. ID参数需包含name、version、vendor等字段（163会校验客户端合法性）
     * 3. 服务器公网IP需添加到163邮箱"客户端授权IP白名单"（设置→POP3/SMTP/IMAP→客户端授权密码）
     *
     * @param store IMAP Store连接
     * @throws MessagingException ID命令发送失败时抛出，阻止后续folder.open()产生误导性日志
     */

private void sendImapId(Store store) throws MessagingException {
    try {
        Class<?> storeClass = store.getClass();
        String storeClassName = storeClass.getName();
        log.info("退信检测：IMAP Store实现类={}", storeClassName);

        // 遍历类层次查找 id(Map) 方法（不再限制类名必须包含"IMAPStore"）
        Method idMethod = null;
        Class<?> currentClass = storeClass;
        while (currentClass != null && currentClass != Object.class) {
            try {
                idMethod = currentClass.getDeclaredMethod("id", Map.class);
                break;
            } catch (NoSuchMethodException e) {
                currentClass = currentClass.getSuperclass();
            }
        }

        if (idMethod == null) {
            String msg = "IMAP Store类层次中未找到id(Map)方法，无法发送ID命令，163邮箱将报Unsafe Login";
            log.error("退信检测：{}", msg);
            throw new MessagingException(msg);  
        }

        idMethod.setAccessible(true);
        Map<String, String> clientParams = new HashMap<>();
        clientParams.put("name", "MCLC-BounceChecker");
        clientParams.put("version", "1.0.0");
        clientParams.put("vendor", "MCLC");
        clientParams.put("contact", imapProperties.getUsername());
        clientParams.put("os", "Linux");
        clientParams.put("language", "zh-CN");
        Object result = idMethod.invoke(store, clientParams);
        log.info("退信检测：已发送IMAP ID命令（RFC 2971），host={}，响应={}", imapProperties.getHost(), result);
    } catch (MessagingException e) {
        throw e;
    } catch (Exception e) {
        String msg = "发送IMAP ID命令失败，host=" + imapProperties.getHost() + "，163邮箱将报Unsafe Login";
        log.error("退信检测：{}，异常：{}", msg, e.getMessage(), e);
        throw new MessagingException(msg, e);
    }
}


    // private void sendImapId(Store store) throws MessagingException {
    //     try {
    //         // 反射获取IMAPStore的id(Map)方法，避免编译时依赖angus-mail
    //         Class<?> storeClass = store.getClass();
    //         String storeClassName = storeClass.getName();
    //         log.info("退信检测：IMAP Store实现类={}", storeClassName);

    //         // angus-mail的IMAPStore类名：org.eclipse.angus.mail.imap.IMAPStore
    //         // IMAPSSLStore继承自IMAPStore，类名也包含"IMAPStore"
    //         if (!storeClassName.contains("IMAPStore")) {
    //             String msg = "当前Store非IMAPStore实现（" + storeClassName + "），无法发送ID命令，163邮箱将报Unsafe Login";
    //             log.error("退信检测：{}", msg);
    //             throw new MessagingException(msg);
    //         }

    //         // 遍历类层次查找id(Map)方法（IMAPSSLStore的id()定义在父类IMAPStore中）
    //         Method idMethod = null;
    //         Class<?> currentClass = storeClass;
    //         while (currentClass != null && currentClass != Object.class) {
    //             try {
    //                 idMethod = currentClass.getDeclaredMethod("id", Map.class);
    //                 log.debug("退信检测：在类{}中找到id(Map)方法", currentClass.getName());
    //                 break;
    //             } catch (NoSuchMethodException e) {
    //                 // 当前类没有id()方法，继续查找父类
    //                 log.debug("退信检测：类{}中没有id(Map)方法，继续查找父类", currentClass.getName());
    //                 currentClass = currentClass.getSuperclass();
    //             }
    //         }

    //         if (idMethod == null) {
    //             String msg = "IMAP Store类层次中未找到id(Map)方法（已遍历" + storeClassName + "至Object），163邮箱将报Unsafe Login";
    //             log.error("退信检测：{}", msg);
    //             throw new MessagingException(msg);
    //         }

    //         idMethod.setAccessible(true);
    //         Map<String, String> clientParams = new HashMap<>();
    //         // 163邮箱要求的ID字段：name、version、vendor、contact、os、language
    //         // 参考：https://help.mail.163.com/faqDetail/0/197
    //         clientParams.put("name", "MCLC-BounceChecker");
    //         clientParams.put("version", "1.0.0");
    //         clientParams.put("vendor", "MCLC");
    //         clientParams.put("contact", imapProperties.getUsername());
    //         clientParams.put("os", "Linux");
    //         clientParams.put("language", "zh-CN");
    //         Object result = idMethod.invoke(store, clientParams);
    //         log.info("退信检测：已发送IMAP ID命令（RFC 2971），host={}，响应={}", imapProperties.getHost(), result);
    //     } catch (MessagingException e) {
    //         // 重新抛出MessagingException，阻止后续folder.open()
    //         throw e;
    //     } catch (Exception e) {
    //         String msg = "发送IMAP ID命令失败，host=" + imapProperties.getHost() + "，163邮箱将报Unsafe Login";
    //         log.error("退信检测：{}，异常：{}", msg, e.getMessage(), e);
    //         throw new MessagingException(msg, e);
    //     }
    // }

    /**
     * 判断是否为退信邮件
     *
     * 识别策略：
     * 1. 发送者匹配自动回复模式
     * 2. 主题匹配退信关键词
     * 3. Content-Type 为 multipart/report（RFC 3462 DSN 格式）
     */
    private boolean isBounceEmail(Message message) throws MessagingException {
        // 策略1：检查发送者
        Address[] fromAddresses = message.getFrom();
        if (fromAddresses != null && fromAddresses.length > 0) {
            String from = fromAddresses[0].toString();
            if (BOUNCE_SENDER_PATTERN.matcher(from).find()) {
                log.debug("退信识别[策略1-发送者匹配]：from={}", from);
                return true;
            }
        }

        // 策略2：检查主题
        String subject = message.getSubject();
        if (subject != null && BOUNCE_SUBJECT_PATTERN.matcher(subject).find()) {
            log.debug("退信识别[策略2-主题匹配]：subject={}", subject);
            return true;
        }

        // 策略3：检查 Content-Type（multipart/report 是 DSN 标准格式）
        try {
            String contentType = message.getContentType();
            if (contentType != null && contentType.toLowerCase().contains("multipart/report")) {
                log.debug("退信识别[策略3-ContentType匹配]：contentType={}", contentType);
                return true;
            }
        } catch (MessagingException e) {
            log.debug("退信检测：无法读取Content-Type", e);
        }

        return false;
    }

    /**
     * 处理单封退信邮件（增强版）
     *
     * 改造要点：
     * 1. 使用BounceParser统一退信分类（替代硬编码正则）
     * 2. 提取完整退信信息（subject/sender/contentType/textContent）
     * 3. 提取退信邮件自身的Message-ID（用于幂等检查）
     * 4. 调用handleBounceEnhanced()（替代旧handleBounce）
     *
     * @return 成功匹配并处理的退信数量（0或1）
     */
    private int processBounceEmail(Message message) {
        String traceId = "BOUNCE-" + System.currentTimeMillis() + "-" + Integer.toHexString(message.hashCode());
        try {
            log.info("[{}] 退信检测：开始处理退信邮件", traceId);

            // 1. 提取退信邮件自身的Message-ID（用于幂等检查）
            String bounceMessageId = extractBounceMessageId(message);
            log.info("[{}] 退信检测：步骤1-提取bounceMessageId={}", traceId, bounceMessageId);

            // 2. 提取原始 Message-ID（用于匹配发送记录，含In-Reply-To/References/正文提取）
            String originalMessageId = extractOriginalMessageId(message);
            log.info("[{}] 退信检测：步骤2-提取originalMessageId={}", traceId, originalMessageId);

            // 3. 提取退信邮件的完整信息（供BounceParser使用）
            String subject = message.getSubject();
            String sender = null;
            Address[] fromAddresses = message.getFrom();
            if (fromAddresses != null && fromAddresses.length > 0) {
                sender = fromAddresses[0].toString();
            }
            String contentType = null;
            try {
                contentType = message.getContentType();
            } catch (MessagingException e) {
                log.debug("[{}] 退信检测：无法读取Content-Type", traceId, e);
            }
            log.info("[{}] 退信检测：步骤3-提取邮件信息 subject={}, sender={}, contentType={}", traceId, subject, sender, contentType);

            // 4. 提取邮件正文（纯文本+DSN内容）
            String textContent = null;
            try {
                Object content = message.getContent();
                textContent = extractTextFromPart(content);
            } catch (Exception e) {
                log.debug("[{}] 退信检测：无法读取邮件正文", traceId, e);
            }
            log.debug("[{}] 退信检测：步骤4-提取正文 长度={}", traceId, textContent != null ? textContent.length() : 0);

            // 5. 提取退信收件人（作为兜底，BounceParser也会尝试提取）
            String bouncedRecipient = extractBouncedRecipient(message);
            log.info("[{}] 退信检测：步骤5-提取退信收件人 bouncedRecipient={}", traceId, bouncedRecipient);

            // 6. 使用BounceParser统一解析退信分类
            BounceParseResult parseResult =
                    BounceParser.parse(subject, sender, contentType, textContent);

            // 7. 确定bounceType：优先使用BounceParser解析结果，降级为OTHER
            BounceType bounceType = parseResult.getBounceType();
            if (bounceType == null) {
                bounceType = BounceType.OTHER;
            }

            // 8. 确定退信原因：优先使用BounceParser解析结果，兜底使用extractBounceReason
            String bounceReason = parseResult.getBounceReason();
            if (bounceReason == null || bounceReason.isBlank()) {
                bounceReason = extractBounceReason(message);
            }

            // 9. 确定退信收件人：优先使用BounceParser解析结果，兜底使用extractBouncedRecipient
            String finalBouncedRecipient = parseResult.getBouncedRecipient();
            if (finalBouncedRecipient == null || finalBouncedRecipient.isBlank()) {
                finalBouncedRecipient = bouncedRecipient;
            }

            log.info("[{}] 退信检测：步骤6-BounceParser解析结果 bounceType={}, bounceCode={}, isPermanent={}, bouncedRecipient={}",
                    traceId, bounceType, parseResult.getBounceCode(), parseResult.isPermanent(), finalBouncedRecipient);

            // 10. 调用增强版退信处理（5级匹配+delivery_status+永久/临时区分重发）
            boolean handled = mailSendRecordService.handleBounceEnhanced(
                    bounceMessageId,               // messageId - 退信邮件自身Message-ID（幂等检查）
                    originalMessageId,             // originalMessageId - 原始邮件Message-ID（MESSAGE_ID级别匹配）
                    bounceReason,                  // bounceReason - 退信原因
                    finalBouncedRecipient,         // bouncedRecipient - 退信收件人
                    bounceType.name(),             // bounceType - 退信类型（String）
                    parseResult.getBounceCode(),   // bounceCode - 退信代码
                    parseResult.getDsnStatus(),    // dsnStatus - DSN状态码
                    parseResult.getDsnDiagnosticCode(), // dsnDiagnosticCode - DSN诊断码
                    null,                          // matchStrategy - 匹配策略（null=自动检测）
                    subject,                       // bounceSubject - 退信邮件主题
                    sender                         // bounceSender - 退信邮件发送者
            );

            log.info("[{}] 退信检测：步骤7-handleBounceEnhanced结果 handled={}", traceId, handled);
            return handled ? 1 : 0;

        } catch (Exception e) {
            String subject = "unknown";
            try { subject = message.getSubject(); } catch (Exception ignored) {}
            log.error("[{}] 退信检测：处理退信邮件异常，subject={}", traceId, subject, e);
            return 0;
        }
    }

    /**
     * 提取退信邮件自身的Message-ID（用于幂等检查）
     * 这是退信通知邮件的Message-ID，不是原始邮件的Message-ID
     */
    private String extractBounceMessageId(Message message) throws MessagingException {
        String[] messageIdHeader = message.getHeader("Message-ID");
        if (messageIdHeader != null && messageIdHeader.length > 0) {
            return extractMessageIdFromString(messageIdHeader[0]);
        }
        return null;
    }

    /**
     * 从退信邮件中提取原始 Message-ID
     *
     * 优先从 In-Reply-To / References 头部提取，
     * 其次从邮件正文中提取。
     */
    private String extractOriginalMessageId(Message message) throws MessagingException {
        // 策略1：从 In-Reply-To 头部提取
        String[] inReplyTo = message.getHeader("In-Reply-To");
        if (inReplyTo != null && inReplyTo.length > 0) {
            String messageId = extractMessageIdFromString(inReplyTo[0]);
            if (messageId != null) {
                return messageId;
            }
        }

        // 策略2：从 References 头部提取（取最后一个，即原始邮件ID）
        // References头格式：<msg1> <msg2> <msg3>，最后一个是最早的原始邮件
        String[] references = message.getHeader("References");
        if (references != null && references.length > 0) {
            // References可能有多个值（每个值包含空格分隔的多个Message-ID）
            // 合并所有值后取最后一个Message-ID
            StringBuilder allRefs = new StringBuilder();
            for (String ref : references) {
                allRefs.append(ref).append(" ");
            }
            String refStr = allRefs.toString().trim();
            String messageId = extractLastMessageIdFromString(refStr);
            if (messageId != null) {
                log.debug("退信检测：从References头部提取原始Message-ID={}（References={}）", messageId, refStr);
                return messageId;
            }
        }

        // 策略3：从邮件正文中提取
        try {
            Object content = message.getContent();
            String textContent = extractTextFromPart(content);
            if (textContent != null) {
                return extractMessageIdFromText(textContent);
            }
        } catch (IOException | MessagingException e) {
            log.debug("退信检测：无法读取邮件正文提取Message-ID", e);
        }

        return null;
    }

    /**
     * 从字符串中提取 Message-ID（<...> 格式）
     */
    private String extractMessageIdFromString(String headerValue) {
        if (headerValue == null || headerValue.isBlank()) {
            return null;
        }
        Matcher matcher = MESSAGE_ID_PATTERN.matcher(headerValue);
        if (matcher.find()) {
            return matcher.group(1);
        }
        return null;
    }

    /**
     * 从字符串中提取最后一个 Message-ID（<...> 格式）
     * 用于References头部，最后一个引用的是原始邮件ID
     */
    private String extractLastMessageIdFromString(String headerValue) {
        if (headerValue == null || headerValue.isBlank()) {
            return null;
        }
        Matcher matcher = MESSAGE_ID_PATTERN.matcher(headerValue);
        String lastMatch = null;
        while (matcher.find()) {
            lastMatch = matcher.group(1);
        }
        return lastMatch;
    }

    /**
     * 从邮件正文中提取 Message-ID
     */
    private String extractMessageIdFromText(String text) {
        if (text == null || text.isBlank()) {
            return null;
        }
        // 匹配 Message-ID: <xxx@xxx> 格式
        Pattern msgIdHeaderPattern = Pattern.compile(
                "Message-Id:\\s*<([^>]+)>",
                Pattern.CASE_INSENSITIVE
        );
        Matcher matcher = msgIdHeaderPattern.matcher(text);
        if (matcher.find()) {
            return matcher.group(1);
        }
        return null;
    }

    /**
     * 从邮件内容中提取纯文本（增强版）
     *
     * 增强点：
     * 1. 保留 message/delivery-status 部分内容（DSN格式，RFC 3464）
     * 2. 保留 message/rfc822 嵌套邮件内容
     * 3. 对非文本内容尝试toString()兜底
     */
    private String extractTextFromPart(Object content) throws MessagingException, IOException {
        if (content instanceof String text) {
            return text;
        } else if (content instanceof Multipart multipart) {
            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < multipart.getCount(); i++) {
                BodyPart part = multipart.getBodyPart(i);
                try {
                    String contentType = part.getContentType();
                    Object partContent = part.getContent();

                    // 优先处理DSN delivery-status部分（RFC 3464）
                    if (contentType != null && contentType.toLowerCase().contains("delivery-status")) {
                        String dsnText = extractDeliveryStatusContent(partContent);
                        if (dsnText != null && !dsnText.isBlank()) {
                            sb.append("\n[DSN-Delivery-Status]\n").append(dsnText).append("\n");
                        }
                        continue;
                    }

                    // 递归处理Multipart嵌套
                    String partText = extractTextFromPart(partContent);
                    if (partText != null) {
                        sb.append(partText);
                    }
                } catch (Exception e) {
                    log.debug("退信检测：无法读取MIME部分", e);
                }
            }
            return sb.toString();
        }
        // 兜底：对非String非Multipart内容尝试toString()
        if (content != null) {
            return content.toString();
        }
        return null;
    }

    /**
     * 提取DSN delivery-status部分内容
     * DSN格式通常是InputStream或嵌套Multipart
     */
    private String extractDeliveryStatusContent(Object partContent) {
        if (partContent == null) {
            return null;
        }
        if (partContent instanceof String text) {
            return text;
        }
        if (partContent instanceof java.io.InputStream inputStream) {
            try {
                return new String(inputStream.readAllBytes(), java.nio.charset.StandardCharsets.UTF_8);
            } catch (Exception e) {
                log.debug("退信检测：无法读取DSN InputStream", e);
                return null;
            }
        }
        // 兜底toString()
        return partContent.toString();
    }

    /**
     * 提取退信原因
     *
     * 优先从 DSN（Delivery Status Notification）部分提取，
     * 其次使用邮件主题作为退信原因。
     */
    private String extractBounceReason(Message message) throws MessagingException {
        try {
            Object content = message.getContent();
            if (content instanceof Multipart multipart) {
                for (int i = 0; i < multipart.getCount(); i++) {
                    BodyPart part = multipart.getBodyPart(i);
                    String contentType = part.getContentType();
                    if (contentType != null && contentType.toLowerCase().contains("delivery-status")) {
                        Object partContent = part.getContent();
                        if (partContent instanceof String text) {
                            // 截取前500字符作为退信原因
                            return text.length() > 500 ? text.substring(0, 500) : text;
                        }
                    }
                }
                // 尝试从文本部分提取诊断信息
                for (int i = 0; i < multipart.getCount(); i++) {
                    BodyPart part = multipart.getBodyPart(i);
                    String contentType = part.getContentType();
                    if (contentType != null && contentType.toLowerCase().contains("text/plain")) {
                        Object partContent = part.getContent();
                        if (partContent instanceof String text) {
                            // 截取前300字符
                            return text.length() > 300 ? text.substring(0, 300) : text;
                        }
                    }
                }
            }
        } catch (IOException e) {
            log.debug("退信检测：无法读取邮件内容提取退信原因", e);
        }

        // 兜底：使用邮件主题
        String subject = message.getSubject();
        return subject != null ? subject : "未知退信原因";
    }

    /**
     * 提取退信收件人地址
     */
    private String extractBouncedRecipient(Message message) throws MessagingException {
        // 从邮件正文中提取
        try {
            Object content = message.getContent();
            String textContent = extractTextFromPart(content);
            if (textContent != null) {
                Matcher matcher = BOUNCED_RECIPIENT_PATTERN.matcher(textContent);
                if (matcher.find()) {
                    return matcher.group(1);
                }
            }
        } catch (IOException e) {
            log.debug("退信检测：无法读取邮件内容提取退信收件人", e);
        }

        // 兜底：尝试从退信通知的收件人头部提取
        Address[] recipients = message.getRecipients(Message.RecipientType.TO);
        if (recipients != null && recipients.length > 0) {
            return recipients[0].toString();
        }

        return "unknown";
    }

    /**
     * 安全关闭 IMAP 连接
     */
    private void closeQuietly(Folder folder, Store store) {
        if (folder != null) {
            try {
                if (folder.isOpen()) {
                    folder.close(false);
                }
            } catch (Exception e) {
                log.debug("退信检测：关闭Folder异常", e);
            }
        }
        if (store != null) {
            try {
                if (store.isConnected()) {
                    store.close();
                }
            } catch (Exception e) {
                log.debug("退信检测：关闭Store异常", e);
            }
        }
    }
}