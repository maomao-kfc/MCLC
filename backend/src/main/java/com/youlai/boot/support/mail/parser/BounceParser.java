package com.youlai.boot.support.mail.parser;

import com.youlai.boot.support.mail.enums.BounceType;
import com.youlai.boot.support.mail.model.dto.BounceParseResult;
import lombok.extern.slf4j.Slf4j;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 通用退信解析器
 *
 * 从退信邮件中提取结构化信息，严格分离退信通知邮件与原始邮件的信息。
 *
 * 核心设计原则：
 * 1. 退信通知邮件 vs 原始邮件严格分离
 *    - bounceSender/bounceRecipient/bounceSubject = 退信通知本身的信息
 *    - originalRecipient/originalSender/originalSubject = 原始投递失败邮件的信息
 * 2. DSN标准字段优先（RFC 3464）
 *    - Final-Recipient / Original-Recipient 是获取真正失败收件人的最可靠来源
 *    - Status / Diagnostic-Code / Action 是分类和原因的最可靠来源
 * 3. 收件人提取优先级：DSN Final-Recipient > DSN Original-Recipient > Body Fallback
 *    - 每种来源标记recipientSource，用于匹配策略权重
 * 4. DNS分类细化：DOMAIN_NOT_FOUND(永久) vs DNS_TEMPORARY_FAILURE(临时) vs DNS_ERROR(默认临时)
 *
 * 支持的真实案例：
 * - 网易退信：Postmaster@163.com → registrationmclc@163.com，原始收件人 maojiangjun@faxmai.com
 * - DNS query error: faxmai.com（DNS解析失败）
 * - 550 User not found: yuanwenyi0239@126.com（收件人不存在）
 *
 * @author system
 * @since 2026/8/3
 */
@Slf4j
public class BounceParser {

    // ========== SMTP响应码模式 ==========
    /** 550 + 用户不存在关键词 */
    private static final Pattern SMTP_550_USER_NOT_FOUND = Pattern.compile(
            "550\\s+(?:user not found|no such user|unknown user|invalid recipient|recipient(?:\\s+address)? rejected|does(?:n't| not) exist)",
            Pattern.CASE_INSENSITIVE
    );

    /** 550 + 邮箱不可用关键词 */
    private static final Pattern SMTP_550_MAILBOX_UNAVAILABLE = Pattern.compile(
            "550\\s+(?:mailbox(?:\\s+is)? unavailable|account(?:\\s+is)? (?:unavailable|disabled|locked|inactive))",
            Pattern.CASE_INSENSITIVE
    );

    /** 550 + 垃圾邮件拒绝 */
    private static final Pattern SMTP_550_SPAM_REJECTED = Pattern.compile(
            "550\\s+(?:spam|rejected(?:\\s+by)?(?:\\s+policy)?|blocked|content\\s+filter|mail\\s+rejected)",
            Pattern.CASE_INSENSITIVE
    );

    /** 550 + 中继拒绝 */
    private static final Pattern SMTP_550_RELAY_DENIED = Pattern.compile(
            "550\\s+(?:relay(?:\\s+is)? denied|not\\s+allowed|access\\s+denied|relaying\\s+denied)",
            Pattern.CASE_INSENSITIVE
    );

    /** 552 + 邮箱已满 */
    private static final Pattern SMTP_552_MAILBOX_FULL = Pattern.compile(
            "552\\s+(?:mailbox\\s+full|quota\\s+(?:exceeded|overflow)|over\\s+quota|insufficient\\s+system\\s+storage)",
            Pattern.CASE_INSENSITIVE
    );

    /** 450/552 + 频率限制 */
    private static final Pattern SMTP_RATE_LIMIT = Pattern.compile(
            "(?:450|552)\\s+(?:too\\s+many\\s+(?:recipients|messages)|rate\\s+limit|throttl|sending\\s+(?:rate|speed|frequency)\\s+(?:exceeded|too\\s+(?:high|fast)))",
            Pattern.CASE_INSENSITIVE
    );

    /** 4xx + 网络临时故障 */
    private static final Pattern SMTP_NETWORK_TEMPORARY = Pattern.compile(
            "4[0-9]{2}\\s+(?:connection\\s+(?:refused|timed\\s+out|failed|reset)|network\\s+(?:error|failure|unreachable)|host\\s+unreachable|could\\s+not\\s+connect)",
            Pattern.CASE_INSENSITIVE
    );

    // ========== DSN状态码模式 (RFC 3463) ==========
    /** 5.1.1 - Bad destination mailbox address */
    private static final Pattern DSN_5_1_1 = Pattern.compile("5\\.1\\.1");

    /** 5.4.4 - Unable to route (DNS解析失败) */
    private static final Pattern DSN_5_4_4 = Pattern.compile("5\\.4\\.4");

    /** 5.2.1 - Mailbox disabled / not accepting mail */
    private static final Pattern DSN_5_2_1 = Pattern.compile("5\\.2\\.1");

    /** 5.2.2 - Mailbox full */
    private static final Pattern DSN_5_2_2 = Pattern.compile("4\\.2\\.2|5\\.2\\.2");

    /** 5.7.1 - Delivery not authorized / spam rejected */
    private static final Pattern DSN_5_7_1 = Pattern.compile("5\\.7\\.1");

    // ========== DNS错误模式 ==========
    /** DNS永久性错误（域名不存在，含 DNS query error / Host not found） */
    private static final Pattern DNS_PERMANENT_PATTERN = Pattern.compile(
            "(domain\\s+(?:not\\s+found|does\\s+not\\s+exist|name\\s+not\\s+found)|" +
            "no\\s+such\\s+domain|" +
            "nxdomain|" +
            "host\\s+(?:or\\s+domain\\s+)?(?:not\\s+found|name\\s+not\\s+found)|" +
            "dns\\s+query\\s+error|" +
            "5\\.4\\.4)",
            Pattern.CASE_INSENSITIVE
    );

    /** DNS临时性错误（SERVFAIL / timeout / temporary） */
    private static final Pattern DNS_TEMPORARY_PATTERN = Pattern.compile(
            "(dns\\s+(?:server\\s+)?(?:fail|timeout|temporary|refused|servfail)|" +
            "temporary\\s+(?:dns|name\\s+server)\\s+(?:failure|error)|" +
            "dns\\s+lookup\\s+(?:failed|timed?\\s?out)|" +
            "could\\s+not\\s+resolve\\s+domain|" +
            "name\\s+server\\s+(?:error|failure|unreachable)|" +
            "4\\.4\\.4|" +
            "4\\.5\\.3)",
            Pattern.CASE_INSENSITIVE
    );

    /** DNS通用错误（无法确定永久/临时，默认临时；dns query error 和 host not found 已归入永久模式） */
    private static final Pattern DNS_ERROR_PATTERN = Pattern.compile(
            "(dns\\s+error|" +
            "host\\s+(?:unreachable|unknown)|" +
            "couldn't\\s+find\\s+host|" +
            "resolution\\s+failed)",
            Pattern.CASE_INSENSITIVE
    );

    // ========== DSN字段提取模式 (RFC 3464) ==========
    /** Final-Recipient: rfc822; user@domain.com */
    private static final Pattern FINAL_RECIPIENT_PATTERN = Pattern.compile(
            "Final-Recipient:\\s*(?:rfc822;)?\\s*<?([a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,})>?",
            Pattern.CASE_INSENSITIVE
    );

    /** Original-Recipient: rfc822; user@domain.com */
    private static final Pattern ORIGINAL_RECIPIENT_DSN_PATTERN = Pattern.compile(
            "Original-Recipient:\\s*(?:rfc822;)?\\s*<?([a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,})>?",
            Pattern.CASE_INSENSITIVE
    );

    /** Action: failed / delayed / delivered / relayed / expanded */
    private static final Pattern ACTION_PATTERN = Pattern.compile(
            "Action:\\s*(failed|delayed|delivered|relayed|expanded)",
            Pattern.CASE_INSENSITIVE
    );

    /** Status: 5.x.x / 4.x.x */
    private static final Pattern STATUS_PATTERN = Pattern.compile(
            "Status:\\s*([2-5]\\.\\d+\\.\\d+)",
            Pattern.CASE_INSENSITIVE
    );

    /** Diagnostic-Code: smtp; 550 5.1.1 ... */
    private static final Pattern DIAGNOSTIC_CODE_PATTERN = Pattern.compile(
            "Diagnostic-Code:\\s*smtp;\\s*(\\d{3}\\s+[\\s\\S]*?)(?:\\r?\\n[^\\s]|$)",
            Pattern.CASE_INSENSITIVE
    );

    // ========== 正文提取模式 ==========
    /** 退信收件人地址提取（通用，适配多种退信格式） */
    private static final Pattern RECIPIENT_PATTERN = Pattern.compile(
            "(?:to\\s*[:=]?\\s*|rcpt\\s+to\\s*[:=]?\\s*|recipient\\s*[:=]?\\s*|for\\s+|addressed\\s+to\\s+)?" +
            "<?([a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,})>?",
            Pattern.CASE_INSENSITIVE
    );

    /** 网易550格式收件人提取 */
    private static final Pattern NETEASE_550_PATTERN = Pattern.compile(
            "550\\s+(?:user not found|no such user)[^\\n]*?([a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,})",
            Pattern.CASE_INSENSITIVE
    );

    /** 正文中的Message-ID提取 */
    private static final Pattern ORIGINAL_MESSAGE_ID_PATTERN = Pattern.compile(
            "(?:Message-Id|Message-ID|Original-Message-ID):\\s*<([^>]+)>",
            Pattern.CASE_INSENSITIVE
    );

    /** 正文中的原始主题提取 */
    private static final Pattern ORIGINAL_SUBJECT_PATTERN = Pattern.compile(
            "(?:Subject|Original-Subject):\\s*(.+?)(?:\\r?\\n(?:[ \\t]*\\r?\\n|[^ \\t])|$)",
            Pattern.CASE_INSENSITIVE
    );

    /** 收件人来源标记 */
    private static final String SOURCE_DSN_FINAL = "DSN_FINAL_RECIPIENT";
    private static final String SOURCE_DSN_ORIGINAL = "DSN_ORIGINAL_RECIPIENT";
    private static final String SOURCE_BODY_FALLBACK = "BODY_FALLBACK";

    /**
     * 解析退信邮件内容（完整版）
     *
     * 退信通知邮件与原始邮件信息严格分离：
     * - bounceSender/bounceRecipient/bounceSubject = 退信通知本身的信息
     * - originalRecipient = DSN Final-Recipient / Original-Recipient / Body提取的真正失败收件人
     *
     * @param subject         退信通知邮件主题
     * @param sender          退信通知邮件发送者（如 Postmaster@163.com）
     * @param bounceRecipient 退信通知邮件的收件人（如 registrationmclc@163.com）
     * @param contentType     Content-Type
     * @param textContent     退信邮件文本内容（包含DSN部分和正文）
     * @param inReplyTo       退信邮件的In-Reply-To头
     * @param referencesHeader 退信邮件的References头
     * @param bounceMessageId 退信邮件自身的Message-ID
     * @return BounceParseResult 解析结果
     */
    public static BounceParseResult parse(String subject, String sender, String bounceRecipient,
                                          String contentType, String textContent,
                                          String inReplyTo, String referencesHeader,
                                          String bounceMessageId) {
        BounceParseResult result = new BounceParseResult();

        // 1. 填充退信通知邮件信息
        result.setBounceSubject(subject);
        result.setBounceSender(sender);
        result.setBounceRecipient(bounceRecipient);
        result.setBounceMessageId(bounceMessageId);

        // 2. 填充Header匹配辅助字段
        result.setInReplyTo(inReplyTo);
        result.setReferencesHeader(referencesHeader);

        // 3. 从In-Reply-To提取原始Message-ID
        if (inReplyTo != null && !inReplyTo.isBlank()) {
            String extractedId = extractMessageIdFromHeader(inReplyTo);
            if (extractedId != null) {
                result.setOriginalMessageId(extractedId);
            }
        }

        // 4. 从References提取原始Message-ID（如果In-Reply-To未提取到）
        if (result.getOriginalMessageId() == null && referencesHeader != null && !referencesHeader.isBlank()) {
            String extractedId = extractLastMessageIdFromReferences(referencesHeader);
            if (extractedId != null) {
                result.setOriginalMessageId(extractedId);
            }
        }

        if (textContent == null || textContent.isBlank()) {
            result.setBounceType(BounceType.OTHER);
            result.setBounceCode("NO_CONTENT");
            result.setBounceReason(subject != null ? subject : "退信邮件内容为空");
            return result;
        }

        // 5. 提取DSN信息（Status, Diagnostic-Code, Action, Final-Recipient, Original-Recipient）
        extractDsnInfo(textContent, result);

        // 6. 提取原始收件人（优先级：DSN Final-Recipient > DSN Original-Recipient > Body Fallback）
        extractRecipient(textContent, result);

        // 7. 从正文提取原始Message-ID（如果Header未提取到）
        if (result.getOriginalMessageId() == null) {
            String msgId = extractOriginalMessageIdFromBody(textContent);
            if (msgId != null) {
                result.setOriginalMessageId(msgId);
            }
        }

        // 8. 从正文提取原始主题
        if (result.getOriginalSubject() == null) {
            String origSubject = extractOriginalSubjectFromBody(textContent);
            if (origSubject != null) {
                result.setOriginalSubject(origSubject);
            }
        }

        // 9. 分类退信类型（优先级：DSN状态码 > SMTP响应码 > DNS关键词 > 正文关键词）
        classifyBounce(textContent, result);

        // 10. 提取退信原因（截取关键片段）
        extractReason(textContent, result);

        return result;
    }

    /**
     * 解析退信邮件内容（兼容旧接口）
     *
     * @deprecated 建议使用完整版 {@link #parse(String, String, String, String, String, String, String, String)}
     *             以提供退信通知与原始邮件的严格分离信息
     */
    public static BounceParseResult parse(String subject, String sender, String contentType, String textContent) {
        return parse(subject, sender, null, contentType, textContent, null, null, null);
    }

    // ========== DSN信息提取 ==========

    /**
     * 提取DSN标准字段（RFC 3464）
     *
     * 提取字段：Status, Diagnostic-Code, Action, Final-Recipient, Original-Recipient
     */
    private static void extractDsnInfo(String textContent, BounceParseResult result) {
        // 提取DSN Status
        Matcher statusMatcher = STATUS_PATTERN.matcher(textContent);
        if (statusMatcher.find()) {
            result.setDsnStatus(statusMatcher.group(1).trim());
        }

        // 提取Diagnostic-Code
        Matcher diagnosticMatcher = DIAGNOSTIC_CODE_PATTERN.matcher(textContent);
        if (diagnosticMatcher.find()) {
            String diagnosticCode = diagnosticMatcher.group(1).trim();
            result.setDsnDiagnosticCode(diagnosticCode.length() > 500 ? diagnosticCode.substring(0, 500) : diagnosticCode);
        }

        // 提取Action
        Matcher actionMatcher = ACTION_PATTERN.matcher(textContent);
        if (actionMatcher.find()) {
            result.setDsnAction(actionMatcher.group(1).trim().toLowerCase());
        }

        // 提取Final-Recipient（DSN标准字段，最可靠的原始收件人来源）
        Matcher finalRecipientMatcher = FINAL_RECIPIENT_PATTERN.matcher(textContent);
        if (finalRecipientMatcher.find()) {
            result.setFinalRecipient(finalRecipientMatcher.group(1).trim());
        }

        // 提取Original-Recipient（DSN标准字段）
        Matcher originalRecipientDsnMatcher = ORIGINAL_RECIPIENT_DSN_PATTERN.matcher(textContent);
        if (originalRecipientDsnMatcher.find()) {
            result.setOriginalRecipientDsn(originalRecipientDsnMatcher.group(1).trim());
        }
    }

    // ========== 收件人提取（优先级：DSN > Body Fallback） ==========

    /**
     * 提取原始收件人地址
     *
     * 优先级（严格按DSN标准字段优先）：
     * 1. DSN Final-Recipient — 最可靠，RFC 3464标准字段
     * 2. DSN Original-Recipient — 次可靠
     * 3. Body Fallback — 最后兜底，标记recipientSource=BODY_FALLBACK
     *
     * 重要：退信通知邮件的To地址（bounceRecipient）是收到退信通知的人，
     *       不是真正投递失败的目标收件人。绝不能将bounceRecipient当作originalRecipient。
     */
    private static void extractRecipient(String textContent, BounceParseResult result) {
        // 优先级1：DSN Final-Recipient
        if (result.getFinalRecipient() != null && !result.getFinalRecipient().isBlank()) {
            result.setOriginalRecipient(result.getFinalRecipient());
            result.setRecipientSource(SOURCE_DSN_FINAL);
            log.debug("退信解析：从DSN Final-Recipient提取原始收件人: {}", result.getFinalRecipient());
            return;
        }

        // 优先级2：DSN Original-Recipient
        if (result.getOriginalRecipientDsn() != null && !result.getOriginalRecipientDsn().isBlank()) {
            result.setOriginalRecipient(result.getOriginalRecipientDsn());
            result.setRecipientSource(SOURCE_DSN_ORIGINAL);
            log.debug("退信解析：从DSN Original-Recipient提取原始收件人: {}", result.getOriginalRecipientDsn());
            return;
        }

        // 优先级3：Body Fallback（不可靠，标记来源）
        String bodyRecipient = extractRecipientFromBody(textContent);
        if (bodyRecipient != null) {
            result.setOriginalRecipient(bodyRecipient);
            result.setRecipientSource(SOURCE_BODY_FALLBACK);
            log.debug("退信解析：从正文提取原始收件人(Body Fallback): {}", bodyRecipient);
        }
    }

    /**
     * 从邮件正文提取收件人地址（Body Fallback）
     *
     * 优先尝试网易550格式，其次通用格式
     */
    private static String extractRecipientFromBody(String textContent) {
        // 优先尝试网易550格式
        Matcher neteaseMatcher = NETEASE_550_PATTERN.matcher(textContent);
        if (neteaseMatcher.find()) {
            return neteaseMatcher.group(1);
        }

        // 通用收件人提取
        Matcher recipientMatcher = RECIPIENT_PATTERN.matcher(textContent);
        if (recipientMatcher.find()) {
            return recipientMatcher.group(1);
        }

        return null;
    }

    // ========== 从正文提取原始邮件信息 ==========

    /**
     * 从邮件正文提取原始Message-ID
     *
     * 匹配格式：Message-ID: <xxx@xxx> 或 Original-Message-ID: <xxx@xxx>
     */
    private static String extractOriginalMessageIdFromBody(String textContent) {
        Matcher matcher = ORIGINAL_MESSAGE_ID_PATTERN.matcher(textContent);
        if (matcher.find()) {
            return matcher.group(1).trim();
        }
        return null;
    }

    /**
     * 从邮件正文提取原始主题
     *
     * 匹配格式：Subject: xxx 或 Original-Subject: xxx
     */
    private static String extractOriginalSubjectFromBody(String textContent) {
        Matcher matcher = ORIGINAL_SUBJECT_PATTERN.matcher(textContent);
        if (matcher.find()) {
            String subject = matcher.group(1).trim();
            // 截取前200字符
            return subject.length() > 200 ? subject.substring(0, 200) : subject;
        }
        return null;
    }

    // ========== Header辅助提取 ==========

    /**
     * 从In-Reply-To头值提取Message-ID
     */
    private static String extractMessageIdFromHeader(String headerValue) {
        if (headerValue == null || headerValue.isBlank()) {
            return null;
        }
        // 匹配 <xxx@xxx> 格式
        Pattern msgIdPattern = Pattern.compile("<([^>]+)>");
        Matcher matcher = msgIdPattern.matcher(headerValue);
        if (matcher.find()) {
            return matcher.group(1).trim();
        }
        return null;
    }

    /**
     * 从References头提取最后一个Message-ID（即原始邮件ID）
     *
     * References头格式：<id1@x> <id2@x> ... <originalId@x>
     * 最后一个通常是原始邮件的Message-ID
     */
    private static String extractLastMessageIdFromReferences(String references) {
        if (references == null || references.isBlank()) {
            return null;
        }
        Pattern msgIdPattern = Pattern.compile("<([^>]+)>");
        Matcher matcher = msgIdPattern.matcher(references);
        String lastId = null;
        while (matcher.find()) {
            lastId = matcher.group(1).trim();
        }
        return lastId;
    }

    // ========== 退信分类 ==========

    /**
     * 分类退信类型
     *
     * 优先级：DSN状态码 > SMTP响应码 > DNS关键词（永久>临时>通用） > 正文关键词 > OTHER
     *
     * DNS分类细化：
     * - DOMAIN_NOT_FOUND(永久)：域名不存在（NXDOMAIN / domain not found / does not exist）
     * - DNS_TEMPORARY_FAILURE(临时)：DNS临时故障（SERVFAIL / timeout / temporary）
     * - DNS_ERROR(默认临时)：其他DNS异常（无法确定永久/临时）
     */
    private static void classifyBounce(String textContent, BounceParseResult result) {
        String dsnStatus = result.getDsnStatus();
        String diagnosticCode = result.getDsnDiagnosticCode();

        // 组合诊断码和正文用于模式匹配
        String matchText = (diagnosticCode != null ? diagnosticCode : "") + " " + textContent;

        // 1. 优先按DSN状态码分类
        if (dsnStatus != null) {
            if (DSN_5_1_1.matcher(dsnStatus).find()) {
                result.setBounceType(BounceType.USER_NOT_FOUND);
                result.setBounceCode("5.1.1");
                return;
            }
            if (DSN_5_4_4.matcher(dsnStatus).find()) {
                // DSN 5.4.4 = Unable to route，明确是域名不存在
                result.setBounceType(BounceType.DOMAIN_NOT_FOUND);
                result.setBounceCode("5.4.4");
                return;
            }
            if (DSN_5_2_1.matcher(dsnStatus).find()) {
                result.setBounceType(BounceType.MAILBOX_UNAVAILABLE);
                result.setBounceCode("5.2.1");
                return;
            }
            if (DSN_5_2_2.matcher(dsnStatus).find()) {
                result.setBounceType(BounceType.MAILBOX_FULL);
                result.setBounceCode("5.2.2");
                return;
            }
            if (DSN_5_7_1.matcher(dsnStatus).find()) {
                result.setBounceType(BounceType.SPAM_REJECTED);
                result.setBounceCode("5.7.1");
                return;
            }
        }

        // 2. 按DNS关键词分类（细化：永久 > 临时 > 通用）
        if (DNS_PERMANENT_PATTERN.matcher(matchText).find()) {
            result.setBounceType(BounceType.DOMAIN_NOT_FOUND);
            result.setBounceCode("DOMAIN_NOT_FOUND");
            return;
        }
        if (DNS_TEMPORARY_PATTERN.matcher(matchText).find()) {
            result.setBounceType(BounceType.DNS_TEMPORARY_FAILURE);
            result.setBounceCode("DNS_TEMPORARY_FAILURE");
            return;
        }
        if (DNS_ERROR_PATTERN.matcher(matchText).find()) {
            // DNS通用错误（dns query error 和 host not found 已归入永久模式），无法确定永久/临时，默认临时
            result.setBounceType(BounceType.DNS_ERROR);
            result.setBounceCode("DNS_ERROR");
            return;
        }

        // 3. 按SMTP响应码分类
        if (SMTP_550_USER_NOT_FOUND.matcher(matchText).find()) {
            result.setBounceType(BounceType.USER_NOT_FOUND);
            result.setBounceCode("550_USER_NOT_FOUND");
            return;
        }
        if (SMTP_550_MAILBOX_UNAVAILABLE.matcher(matchText).find()) {
            result.setBounceType(BounceType.MAILBOX_UNAVAILABLE);
            result.setBounceCode("550_MAILBOX_UNAVAILABLE");
            return;
        }
        if (SMTP_550_SPAM_REJECTED.matcher(matchText).find()) {
            result.setBounceType(BounceType.SPAM_REJECTED);
            result.setBounceCode("550_SPAM_REJECTED");
            return;
        }
        if (SMTP_550_RELAY_DENIED.matcher(matchText).find()) {
            result.setBounceType(BounceType.RELAY_DENIED);
            result.setBounceCode("550_RELAY_DENIED");
            return;
        }
        if (SMTP_552_MAILBOX_FULL.matcher(matchText).find()) {
            result.setBounceType(BounceType.MAILBOX_FULL);
            result.setBounceCode("552_MAILBOX_FULL");
            return;
        }
        if (SMTP_RATE_LIMIT.matcher(matchText).find()) {
            result.setBounceType(BounceType.RATE_LIMIT);
            result.setBounceCode("RATE_LIMIT");
            return;
        }
        if (SMTP_NETWORK_TEMPORARY.matcher(matchText).find()) {
            result.setBounceType(BounceType.NETWORK_TEMPORARY_FAILURE);
            result.setBounceCode("NETWORK_TEMPORARY_FAILURE");
            return;
        }

        // 4. 通用550/554模式（未匹配到具体子类型）
        if (matchText.contains("550") || matchText.contains("554")) {
            result.setBounceType(BounceType.OTHER);
            result.setBounceCode("550_OTHER");
            return;
        }

        // 5. 未匹配到任何已知模式
        result.setBounceType(BounceType.OTHER);
        result.setBounceCode("UNKNOWN");
    }

    // ========== 退信原因提取 ==========

    /**
     * 提取退信原因（截取关键片段）
     *
     * 优先级：Diagnostic-Code > DSN delivery-status部分 > 含错误码的行 > 兜底截取
     */
    private static void extractReason(String textContent, BounceParseResult result) {
        // 如果有Diagnostic-Code，优先使用
        if (result.getDsnDiagnosticCode() != null && !result.getDsnDiagnosticCode().isBlank()) {
            String reason = result.getDsnDiagnosticCode().trim();
            result.setBounceReason(reason.length() > 500 ? reason.substring(0, 500) : reason);
            return;
        }

        // 尝试从DSN delivery-status部分提取
        int dsnStart = textContent.toLowerCase().indexOf("delivery-status");
        if (dsnStart >= 0) {
            String dsnPart = textContent.substring(dsnStart);
            int boundaryIdx = dsnPart.indexOf("--");
            if (boundaryIdx > 0) {
                dsnPart = dsnPart.substring(0, Math.min(boundaryIdx, 500));
            } else {
                dsnPart = dsnPart.substring(0, Math.min(dsnPart.length(), 500));
            }
            result.setBounceReason(dsnPart.trim());
            return;
        }

        // 尝试提取包含错误码的行
        String[] lines = textContent.split("\\r?\\n");
        StringBuilder reasonBuilder = new StringBuilder();
        for (String line : lines) {
            String trimmed = line.trim();
            if (trimmed.matches(".*\\b(5[0-9]{2}|[45]\\.\\d\\.\\d)\\b.*") ||
                trimmed.toLowerCase().contains("error") ||
                trimmed.toLowerCase().contains("fail") ||
                trimmed.toLowerCase().contains("reject") ||
                trimmed.toLowerCase().contains("bounce")) {
                reasonBuilder.append(trimmed).append("; ");
                if (reasonBuilder.length() > 500) break;
            }
        }
        if (reasonBuilder.length() > 0) {
            String reason = reasonBuilder.toString().trim();
            result.setBounceReason(reason.length() > 500 ? reason.substring(0, 500) : reason);
            return;
        }

        // 兜底：截取前300字符
        result.setBounceReason(textContent.length() > 300 ? textContent.substring(0, 300) : textContent);
    }
}