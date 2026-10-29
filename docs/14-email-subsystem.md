# MCLC 邮件子系统技术文档

> 最复杂的子系统，包含9状态状态机、IMAP退信检测、RFC3464 DSN解析、自动重试链、退信黑名单
>
> **最后更新**: 2026-10-29

## 更新日志（2026-08-14 后变更）

| 变更 | 说明 | 关联文档 |
|------|------|----------|
| 父子重试模型 | mail_send_record 新增 record_type(ORIGINAL/RETRY) / retry_sequence / root_record_id 三列 + uk_root_retry 唯一约束 | [12-email-lifecycle.md](12-email-lifecycle.md) |
| 步骤2.5黑名单检查 | createAndSend 流程新增步骤2.5：发送前检查收件人是否在退信黑名单中，拦截发送 | - |
| 步骤4.1自愈机制 | 发送失败时的自愈处理逻辑 | - |
| 两封邮件机制 | 凭证提交→第一封"待核对"(模板id=7)；审核通过/对账确认/线下登记→第二封"注册通过"(模板id=6) | [12-email-lifecycle.md](12-email-lifecycle.md) |
| 数据库缺列故障 | 代码升级重试模型但数据库未同步，ALTER TABLE 补齐三列修复 | [20-case-email-record-type-missing.md](20-case-email-record-type-missing.md) |
| 论文通过邮件修复 | 服务器上论文审核通过后邮件未发送，修复发送条件判断 | [16-paper-submission.md](16-paper-submission.md) |

## 1. 架构总览

```
┌─────────────────────────────────────────────────────────────────────────┐
│                         邮件子系统架构                                    │
│                                                                         │
│  ┌──────────────┐    ┌──────────────────┐    ┌────────────────────┐     │
│  │ 业务调用方    │───→│ MailSendRecord   │───→│ MailService        │     │
│  │ (论文/注册等) │    │ ServiceImpl      │    │ (JavaMailSender)   │     │
│  └──────────────┘    └────────┬─────────┘    └────────────────────┘     │
│                               │                                        │
│                     创建记录+发送+更新状态                                 │
│                               │                                        │
│  ┌──────────────┐    ┌────────▼─────────┐    ┌────────────────────┐     │
│  │ BounceEmailJob│───→│ mail_send_record │───→│ mail_bounce_record │     │
│  │ (定时IMAP扫描) │    │ (9状态状态机)     │    │ (退信解析结果)      │     │
│  └──────────────┘    └────────┬─────────┘    └────────────────────┘     │
│        │                      │                                        │
│        │            永久退信→黑名单                                      │
│        │                      │                                        │
│  ┌─────▼──────┐    ┌──────────▼─────────┐                              │
│  │BounceParser│    │mail_bounce_blacklist│                              │
│  │(RFC3464解析)│    │ (退信黑名单)        │                              │
│  └────────────┘    └────────────────────┘                              │
│                                                                         │
│  ┌──────────────────┐                                                  │
│  │mail_imap_checkpoint│ ← IMAP增量扫描位置                               │
│  └──────────────────┘                                                  │
└─────────────────────────────────────────────────────────────────────────┘
```

## 2. 核心文件清单

| 文件 | 路径 | 大小 | 职责 |
|------|------|------|------|
| MailSendRecordServiceImpl | `support/mail/service/impl/` | 39.3KB | 邮件发送+状态机+重试链+退信处理 |
| BounceEmailJob | `support/mail/job/` | 46.2KB | IMAP退信检测定时任务(最大文件) |
| BounceParser | `support/mail/parser/` | 26.6KB | RFC3464 DSN退信解析器 |
| MailSendRecordController | `support/mail/controller/` | 4KB | 邮件记录管理API |
| MailBounceRecordController | `support/mail/controller/` | 2.9KB | 退信记录管理API |
| MailBounceBlacklistController | `support/mail/controller/` | 2.4KB | 黑名单管理API |
| MailBounceBlacklistServiceImpl | `support/mail/service/impl/` | 5KB | 黑名单增删查+黑名单检查 |
| MailBounceRecordServiceImpl | `support/mail/service/impl/` | 6.3KB | 退信记录CRUD |
| MailImapCheckpointServiceImpl | `support/mail/service/impl/` | 3.6KB | IMAP检查点CRUD |
| MailServiceImpl | `support/mail/service/impl/` | 7.2KB | SMTP发送封装 |
| MailSendRecordMapper.xml | `resources/mapper/support/` | 6.7KB | 自定义SQL(分页+重试子记录) |

## 3. 状态机详解

### 3.1 MailSendStatus — 9状态流转

```
                    ┌──────────────────────────────────────────────┐
                    │                                              │
                    ▼                                              │
  ┌─────────┐  SMTP   ┌─────────┐  成功   ┌──────────┐           │
  │ PENDING │───────→│ SUCCESS │───────→│(终态)    │           │
  │  (0)    │        │  (1)    │        │          │           │
  └────┬────┘        └────┬────┘        └──────────┘           │
       │                  │                                    │
       │ 手动取消          │ IMAP检测到退信                      │
       ▼                  ▼                                    │
  ┌──────────┐     ┌──────────────┐                           │
  │CANCELLED │     │BOUNCE_DETECTED│                           │
  │  (4)     │     │    (5)       │                           │
  │(终态)    │     └──────┬───────┘                           │
  └──────────┘            │                                   │
       │                  │ 自动/手动重试                       │
       │ SMTP失败          ▼                                   │
       │            ┌──────────────┐                          │
       ├──────────→│ RETRY_PENDING │◄─────────────────┐       │
       │            │    (6)       │                  │       │
       │            └──────┬───────┘                  │       │
       │                   │                          │       │
       │                   ▼                          │       │
       │            ┌──────────┐   SMTP成功    ┌──────────────┐
       │            │ RETRYING │──────────→│RETRY_SUCCESS │
       │            │   (3)    │           │    (7)       │
       │            └────┬─────┘           └──────────────┘
       │                 │                       (终态)
       │                 │ SMTP失败
       │                 ▼
       │          ┌──────────────┐
       │          │ RETRY_FAILED │
       ├────────→│    (8)       │
       │          └──────┬───────┘
       │                 │ 可继续重试(未超maxRetryCount)
       │                 └──────────────────────────→ RETRY_PENDING(6)
       │
       ▼
  ┌─────────┐
  │ FAILED  │
  │  (2)    │───→ 可重试 ──→ RETRY_PENDING(6)
  └─────────┘
```

### 3.2 状态说明

| 状态 | 值 | 中文 | 说明 | 可转换到 |
|------|-----|------|------|---------|
| PENDING | 0 | 待发送 | 已创建记录，尚未调用SMTP | SUCCESS(1), FAILED(2), CANCELLED(4) |
| SUCCESS | 1 | 已提交至邮件服务器 | SMTP返回250 OK（不代表最终投递成功） | BOUNCE_DETECTED(5) |
| FAILED | 2 | 发送失败 | SMTP异常或超时 | RETRY_PENDING(6) |
| RETRYING | 3 | 重试中 | 失败后正在重试 | RETRY_SUCCESS(7), RETRY_FAILED(8) |
| CANCELLED | 4 | 已取消 | 管理员手动取消（终态） | - |
| BOUNCE_DETECTED | 5 | 退信-投递失败 | IMAP检测到退信 | RETRY_PENDING(6) |
| RETRY_PENDING | 6 | 待重试 | 退信/失败后等待重试 | RETRYING(3) |
| RETRY_SUCCESS | 7 | 重试成功 | 重试邮件SMTP发送成功（终态） | BOUNCE_DETECTED(5) |
| RETRY_FAILED | 8 | 重试失败 | 重试邮件SMTP发送失败 | RETRY_PENDING(6) |

### 3.3 完整状态转换矩阵

> 来源：[`MailSendRecordServiceImpl`](backend/src/main/java/com/youlai/boot/support/mail/service/impl/MailSendRecordServiceImpl.java) + [`BounceEmailJob`](backend/src/main/java/com/youlai/boot/support/mail/job/BounceEmailJob.java)

| 从状态 | 到状态 | 触发方法 | 前置条件 | 副作用 |
|--------|--------|----------|----------|--------|
| PENDING(0) | SUCCESS(1) | [`createAndSend()`](backend/src/main/java/com/youlai/boot/support/mail/service/impl/MailSendRecordServiceImpl.java:72) | SMTP返回成功 | 记录messageId, consumeTime, sendTime |
| PENDING(0) | FAILED(2) | [`createAndSend()`](backend/src/main/java/com/youlai/boot/support/mail/service/impl/MailSendRecordServiceImpl.java:72) | SMTP异常/超时 **或** 收件人在黑名单中 | 记录errorMessage, consumeTime |
| PENDING(0) | CANCELLED(4) | [`cancel()`](backend/src/main/java/com/youlai/boot/support/mail/service/impl/MailSendRecordServiceImpl.java:168) | 仅PENDING状态可取消 | - |
| SUCCESS(1) | BOUNCE_DETECTED(5) | [`handleBounceEnhanced()`](backend/src/main/java/com/youlai/boot/support/mail/service/impl/MailSendRecordServiceImpl.java:475) | IMAP检测到退信+5级匹配成功 | delivery_status→BOUNCED, 退信记录, 黑名单(永久), 自动重试(临时) |
| RETRY_SUCCESS(7) | BOUNCE_DETECTED(5) | [`handleBounceEnhanced()`](backend/src/main/java/com/youlai/boot/support/mail/service/impl/MailSendRecordServiceImpl.java:475) | 重试邮件也被退信 | 同上 |
| FAILED(2) | RETRY_PENDING(6) | 自动重试创建 | 退信后临时退信自动重发 | 创建子记录(record_type=RETRY) |
| BOUNCE_DETECTED(5) | RETRY_PENDING(6) | 自动重试创建 | 临时退信+autoResend=true+未超限 | 创建子记录(record_type=RETRY) |
| RETRY_FAILED(8) | RETRY_PENDING(6) | 自动重试创建 | 同上 | 创建子记录 |
| RETRY_PENDING(6) | RETRYING(3) | [`retry()`](backend/src/main/java/com/youlai/boot/support/mail/service/impl/MailSendRecordServiceImpl.java:117) | 手动重试触发 | retryCount+1 |
| RETRYING(3) | RETRY_SUCCESS(7) | [`retry()`](backend/src/main/java/com/youlai/boot/support/mail/service/impl/MailSendRecordServiceImpl.java:117) | SMTP返回成功 | 记录messageId, consumeTime, sendTime |
| RETRYING(3) | RETRY_FAILED(8) | [`retry()`](backend/src/main/java/com/youlai/boot/support/mail/service/impl/MailSendRecordServiceImpl.java:117) | SMTP异常/超时 | 记录errorMessage |

**手动重试入口**（[`retry()`](backend/src/main/java/com/youlai/boot/support/mail/service/impl/MailSendRecordServiceImpl.java:117)）：
- 允许重试的状态：FAILED(2), BOUNCE_DETECTED(5), RETRY_FAILED(8)
- 最大重试次数：`MAX_RETRY_LIMIT = 5`
- 黑名单拦截：收件人在黑名单中直接标记FAILED，不实际发送

**终态说明**：
- CANCELLED(4)：管理员手动取消，不可重试
- RETRY_SUCCESS(7)：重试发送成功，但IMAP仍可能检测到退信→转为BOUNCE_DETECTED(5)

### 3.4 DeliveryStatus — 投递状态（与send_status双状态）

> 来源：[`DeliveryStatus`](backend/src/main/java/com/youlai/boot/support/mail/enums/DeliveryStatus.java)

| 状态 | 值 | 中文 | 说明 |
|------|-----|------|------|
| UNKNOWN | 0 | 等待投递 | SMTP成功但尚未确认最终投递结果 |
| DELIVERED | 1 | 已投递 | 邮件已成功送达收件人邮箱（暂不实现主动确认） |
| BOUNCED | 2 | 已退信 | IMAP检测到退信，投递失败 |
| DELIVERING | 3 | 投递中 | 已开始投递流程，等待最终结果 |

**双状态关系**：
- `send_status` 表示SMTP阶段状态（邮件是否成功提交到邮件服务器）
- `delivery_status` 表示最终投递状态（邮件是否成功送达收件人邮箱）
- 典型组合：send_status=SUCCESS(1) + delivery_status=UNKNOWN(0) → IMAP退信后 → send_status=BOUNCE_DETECTED(5) + delivery_status=BOUNCED(2)

## 4. 邮件发送流程

> 源码：[`MailSendRecordServiceImpl`](backend/src/main/java/com/youlai/boot/support/mail/service/impl/MailSendRecordServiceImpl.java)

### 4.1 createAndSend — 创建并发送

```
MailSendRecordServiceImpl.createAndSend(businessType, businessId, receiver, receiverName, subject, content)
  │
  ├── 1. 创建MailSendRecord，status=PENDING(0), retryCount=0, recordType=ORIGINAL
  ├── 2. 插入数据库（先落库，确保记录不丢失）
  ├── 3. 黑名单检查
  │     └── isBlacklisted(receiver)?
  │           ├── YES → 标记FAILED(2)，记录"收件地址在退信黑名单中，已拦截发送"，返回
  │           └── NO  → 继续
  ├── 4. 调用MailService.sendHtmlMailWithResult(receiver, subject, content)
  │     └── 返回MailSendResult(success, messageId, errorMessage, consumeTime)
  └── 5. updateRecordAfterSend(record, result)
        ├── 成功 → status=SUCCESS(1), 记录messageId, consumeTime, sendTime
        └── 失败 → status=FAILED(2), 记录errorMessage, consumeTime
```

### 4.2 retry — 手动重试

```
MailSendRecordServiceImpl.retry(id)
  │
  ├── 1. 校验记录存在
  ├── 2. 校验状态：只有FAILED(2)/BOUNCE_DETECTED(5)/RETRY_FAILED(8)可重试
  │     └── 其他状态 → 抛出BusinessException
  ├── 3. 校验重试次数：retryCount < MAX_RETRY_LIMIT(5)
  │     └── 超限 → 抛出BusinessException
  ├── 4. 黑名单检查
  │     └── isBlacklisted(receiver)?
  │           ├── YES → 标记FAILED(2)，retryCount+1，返回
  │           └── NO  → 继续
  ├── 5. 更新status=RETRYING(3)
  ├── 6. 调用MailService重新发送
  └── 7. updateRecordAfterSend
        ├── 成功 → status=RETRY_SUCCESS(7), retryCount+1
        └── 失败 → status=RETRY_FAILED(8), retryCount+1
```

**注意**：手动retry()与自动重试的区别：
- 手动retry()：在原记录上更新状态，retryCount+1
- 自动重试（handleBounceEnhanced）：创建新的子记录(record_type=RETRY)，原记录状态不变

### 4.3 cancel — 取消发送

```
MailSendRecordServiceImpl.cancel(id)
  │
  ├── 1. 校验记录存在
  ├── 2. 校验状态：只有PENDING(0)可取消
  │     └── 其他状态 → 抛出BusinessException("只有待发送的邮件才能取消")
  └── 3. 更新status=CANCELLED(4)
```

### 4.4 updateRecordAfterSend — 发送结果更新

> 源码：[MailSendRecordServiceImpl:235](backend/src/main/java/com/youlai/boot/support/mail/service/impl/MailSendRecordServiceImpl.java:235)

```
updateRecordAfterSend(record, result)
  │
  ├── result.isSuccess() == true
  │     ├── record.sendStatus = SUCCESS(1) 或 RETRY_SUCCESS(7)
  │     ├── record.messageId = result.getMessageId()  ← 关键：用于退信匹配
  │     ├── record.consumeTime = result.getConsumeTime()
  │     └── record.sendTime = LocalDateTime.now()
  │
  └── result.isSuccess() == false
        ├── record.sendStatus = FAILED(2) 或 RETRY_FAILED(8)
        ├── record.errorMessage = result.getErrorMessage()
        └── record.consumeTime = result.getConsumeTime()
```

**messageId的重要性**：SMTP成功后记录的messageId是退信匹配的关键依据（MESSAGE_ID匹配策略），如果messageId为空则退信只能依赖RECEIVER_TIME_WINDOW低置信度匹配。

## 5. IMAP退信检测

### 5.1 BounceEmailJob — 定时任务

> 源码：[`BounceEmailJob`](backend/src/main/java/com/youlai/boot/support/mail/job/BounceEmailJob.java) (948行，项目最大文件)

**配置**: `spring.mail.imap.cron` (默认 `0 */5 * * * ?`，每5分钟)

```
BounceEmailJob.detectBounceEmails()
  │
  ├── 1. 检查IMAP是否启用 (imapProperties.isEnabled())
  ├── 2. 获取分布式锁 (Redisson: "mail:bounce-check")
  │     └── 获取失败 → 跳过（防止多实例重复执行）
  ├── 3. 连接IMAP服务器 (imap.163.com:993 SSL)
  │     └── 失败 → executionStatus=FAILED_IMAP_CONNECT
  ├── 4. 发送ID命令 (网易IMAP需要ID命令，RFC 2971)
  │     └── 失败 → executionStatus=FAILED_ID_COMMAND
  ├── 5. 打开INBOX文件夹 (READ_WRITE模式)
  │     └── 失败 → executionStatus=FAILED_FOLDER_OPEN
  ├── 6. 增量扫描
  │     ├── checkpoint存在 + UIDVALIDITY未变化 → 增量: UID > lastUid
  │     ├── checkpoint存在 + UIDVALIDITY变化 → 重置checkpoint, 按时间回溯
  │     ├── 首次扫描 → 按firstScanLookbackDays(默认30天)回溯
  │     └── checkpoint禁用 → 降级: 全量扫描未读邮件
  ├── 7. 逐封处理 (从新到旧，限制maxProcessCount)
  │     ├── isBounceEmail(message) → 3策略退信识别
  │     ├── processBounceEmail(message) → 10步处理流程
  │     └── 更新checkpoint.lastUid
  ├── 8. 更新checkpoint (uidValidity, maxUid, checkCount, bounceCount)
  └── 9. 释放锁 + 关闭连接 (finally块)
```

### 5.2 BounceEmailJob方法级分解

| 方法 | 行号 | 职责 | 关键实现 |
|------|------|------|----------|
| [`detectBounceEmails()`](backend/src/main/java/com/youlai/boot/support/mail/job/BounceEmailJob.java:131) | 131-327 | 定时任务入口 | 分布式锁+IMAP连接+增量扫描+逐封处理 |
| [`init()`](backend/src/main/java/com/youlai/boot/support/mail/job/BounceEmailJob.java:75) | 75-93 | @PostConstruct启动日志 | 输出IMAP配置+调度配置+退信策略 |
| [`connectImap()`](backend/src/main/java/com/youlai/boot/support/mail/job/BounceEmailJob.java:357) | 357-405 | 建立IMAP连接 | SSL连接+认证失败详细排查指引 |
| [`sendImapId()`](backend/src/main/java/com/youlai/boot/support/mail/job/BounceEmailJob.java:431) | 431-472 | 发送IMAP ID命令 | 反射调用IMAPStore.id(Map)+遍历类层次查找方法 |
| [`isBounceEmail()`](backend/src/main/java/com/youlai/boot/support/mail/job/BounceEmailJob.java:541) | 541-571 | 退信邮件识别 | 3策略：发送者→主题→Content-Type |
| [`processBounceEmail()`](backend/src/main/java/com/youlai/boot/support/mail/job/BounceEmailJob.java:584) | 584-675 | 处理单封退信 | 10步流程（提取→解析→匹配→处理） |
| [`extractBounceMessageId()`](backend/src/main/java/com/youlai/boot/support/mail/job/BounceEmailJob.java:681) | 681-687 | 提取退信自身Message-ID | 用于幂等检查 |
| [`extractOriginalMessageId()`](backend/src/main/java/com/youlai/boot/support/mail/job/BounceEmailJob.java:695) | 695-735 | 提取原始邮件Message-ID | 3级：In-Reply-To→References→正文 |
| [`extractMessageIdFromString()`](backend/src/main/java/com/youlai/boot/support/mail/job/BounceEmailJob.java:740) | 740-749 | 从字符串提取`<...>`格式ID | 正则匹配 |
| [`extractLastMessageIdFromString()`](backend/src/main/java/com/youlai/boot/support/mail/job/BounceEmailJob.java:755) | 755-765 | 提取最后一个Message-ID | 用于References头部 |
| [`extractMessageIdFromText()`](backend/src/main/java/com/youlai/boot/support/mail/job/BounceEmailJob.java:770) | 770-784 | 从正文提取Message-Id头 | 正则匹配`Message-Id:\s*<...>` |
| [`extractTextFromPart()`](backend/src/main/java/com/youlai/boot/support/mail/job/BounceEmailJob.java:794) | 794-830 | 提取邮件正文(增强版) | 递归Multipart+保留DSN+保留rfc822 |
| [`extractDeliveryStatusContent()`](backend/src/main/java/com/youlai/boot/support/mail/job/BounceEmailJob.java:836) | 836-853 | 提取DSN delivery-status内容 | InputStream/String/toString三级处理 |
| [`extractBounceReason()`](backend/src/main/java/com/youlai/boot/support/mail/job/BounceEmailJob.java:861) | 861-896 | 提取退信原因 | DSN部分→text/plain部分→主题兜底 |
| [`extractBouncedRecipient()`](backend/src/main/java/com/youlai/boot/support/mail/job/BounceEmailJob.java:901) | 901-923 | 提取退信收件人 | 正文正则→TO头部兜底 |
| [`closeQuietly()`](backend/src/main/java/com/youlai/boot/support/mail/job/BounceEmailJob.java:928) | 928-947 | 安全关闭IMAP连接 | Folder+Store分别关闭 |

### 5.3 processBounceEmail — 10步处理流程

```
processBounceEmail(message)
  │
  ├── 步骤1: extractBounceMessageId(message)
  │     └── 提取退信邮件自身的Message-ID（用于幂等检查）
  │
  ├── 步骤2: extractOriginalMessageId(message)
  │     └── 提取原始邮件Message-ID（3级提取：In-Reply-To→References→正文）
  │
  ├── 步骤3: 提取邮件元信息
  │     └── subject, sender(from), contentType
  │
  ├── 步骤4: extractTextFromPart(content)
  │     └── 提取邮件正文（递归Multipart，保留DSN和rfc822部分）
  │
  ├── 步骤5: extractBouncedRecipient(message)
  │     └── 提取退信收件人（正文正则→TO头部兜底）
  │
  ├── 步骤6: BounceParser.parse(subject, sender, contentType, textContent)
  │     └── 统一退信分类（DSN>SMTP>DNS>正文四级优先级）
  │
  ├── 步骤7: 确定bounceType（优先BounceParser结果，降级OTHER）
  ├── 步骤8: 确定bounceReason（优先BounceParser结果，兜底extractBounceReason）
  ├── 步骤9: 确定bouncedRecipient（优先BounceParser结果，兜底步骤5结果）
  │
  └── 步骤10: mailSendRecordService.handleBounceEnhanced(...)
        └── 3级匹配+退信记录+状态更新+黑名单+自动重试
```

### 5.4 退信识别策略

| 策略 | 匹配规则 | 示例 |
|------|---------|------|
| 发送者匹配 | `mailer-daemon\|postmaster\|mail subsystem\|mail delivery subsystem\|noreply\|no-reply` | Postmaster@163.com |
| 主题关键词 | `failure notice\|undelivered\|returned mail\|delivery failure\|mail delivery\|delivery status notification\|退信\|投递失败\|无法投递\|被退回\|发送失败通知\|邮件退回\|投递错误\|无法送达\|地址不存在\|收件人不存在\|用户不存在\|邮箱不存在` | "Undelivered Mail Returned to Sender" |
| Content-Type | `multipart/report`（含`report-type=delivery-status`） | RFC3464 DSN格式 |

> 识别顺序：发送者→主题→Content-Type，任一匹配即判定为退信邮件

### 5.5 IMAP ID命令（RFC 2971）— 网易邮箱特殊要求

163/126邮箱不遵循标准IMAP规范，要求客户端在连接后、打开文件夹前发送ID命令标识自身，
否则执行任何文件夹操作（SELECT）时会返回"A3 NO SELECT Unsafe Login"错误。

**实现要点**：
- 使用反射调用`IMAPStore.id(Map)`方法，避免对angus-mail的编译时依赖
- 必须遍历类层次查找`id(Map)`方法（IMAPSSLStore继承自IMAPStore，方法定义在父类）
- ID命令必须在SELECT/EXAMINE命令之前发送
- ID参数：name=MCLC-BounceChecker, version=1.0.0, vendor=MCLC, contact=邮箱账号, os=Linux, language=zh-CN

**"Unsafe Login"排查清单**：
1. IMAP服务是否在163网页端开启（设置 → POP3/SMTP/IMAP → 开启IMAP）
2. 授权密码（客户端授权码）是否正确且仍有效（非邮箱登录密码）
3. **关键**：服务器公网IP是否添加到163邮箱「客户端授权IP白名单」
   - 操作路径：163网页端 → 设置 → POP3/SMTP/IMAP → 客户端授权密码 → 授权IP管理 → 添加服务器公网IP
4. 如使用Docker部署，需添加宿主机公网IP（非容器内网IP）
5. 参考：https://help.mail.163.com/faqDetail/0/197

### 5.6 ImapExecutionStatus — 执行状态追踪

> 来源：[`ImapExecutionStatus`](backend/src/main/java/com/youlai/boot/support/mail/enums/ImapExecutionStatus.java)

| 状态 | 说明 | 触发条件 |
|------|------|----------|
| NOT_STARTED | 未开始 | 初始状态 |
| IMAP_CONNECTED | IMAP连接成功 | connectImap()返回非null |
| ID_SENT | ID命令发送成功 | sendImapId()成功（网易需要） |
| FOLDER_OPENED | INBOX文件夹打开成功 | folder.open(READ_WRITE)成功 |
| MESSAGE_FETCHED | 邮件获取成功 | 搜索/获取邮件成功 |
| SUCCESS | 执行完成 | 全流程成功 |
| FAILED_IMAP_CONNECT | IMAP连接失败 | connectImap()返回null |
| FAILED_AUTH | IMAP认证失败 | AuthenticationFailedException |
| FAILED_ID_COMMAND | ID命令发送失败 | sendImapId()抛出MessagingException |
| FAILED_FOLDER_OPEN | 文件夹打开失败 | FolderClosedException或SELECT失败 |
| FAILED_MESSAGE_FETCH | 邮件获取失败 | MESSAGE_FETCHED之前的MessagingException |
| FAILED_OTHER | 其他异常 | 未分类异常 |

**状态推进顺序**：NOT_STARTED → IMAP_CONNECTED → ID_SENT → FOLDER_OPENED → MESSAGE_FETCHED → SUCCESS
**异常分支**：任何阶段失败 → 对应FAILED_*状态，用于checkpoint.executionStatus记录

### 5.3 退信匹配策略（3级提取 + 3级匹配）

> 来源：[`BounceEmailJob.extractOriginalMessageId()`](backend/src/main/java/com/youlai/boot/support/mail/job/BounceEmailJob.java:695) + [`MailSendRecordServiceImpl.handleBounceEnhanced()`](backend/src/main/java/com/youlai/boot/support/mail/service/impl/MailSendRecordServiceImpl.java:475)

#### 5.3.1 第一阶段：BounceEmailJob提取原始Message-ID（3级提取）

```
extractOriginalMessageId(message)
  │
  ├── 提取1: In-Reply-To头部
  │     └── message.getHeader("In-Reply-To") → 提取<...>中的Message-ID
  │           优先级最高：In-Reply-To直接引用原始邮件
  │
  ├── 提取2: References头部
  │     └── message.getHeader("References") → 取最后一个<...>（原始邮件ID）
  │           References格式：<msg1> <msg2> <msg3>，最后一个是最早的原始邮件
  │
  └── 提取3: 邮件正文
        └── extractMessageIdFromText(textContent) → 匹配 "Message-Id: <xxx@xxx>"
              兜底策略：从退信正文中提取
```

#### 5.3.2 第二阶段：handleBounceEnhanced匹配发送记录（3级匹配）

```
handleBounceEnhanced(bounceMessageId, originalMessageId, bouncedRecipient, ...)
  │
  ├── 匹配1: MESSAGE_ID（置信度最高）
  │     └── originalMessageId == mail_send_record.messageId
  │           → findByMessageId(originalMessageId)
  │           → usedMatchStrategy = "MESSAGE_ID"
  │
  ├── 匹配2: IN_REPLY_TO（退信自身Message-ID匹配）
  │     └── bounceMessageId == mail_send_record.messageId
  │           → findByMessageId(messageId)
  │           → usedMatchStrategy = "IN_REPLY_TO"
  │           说明：某些退信通知的Message-ID直接引用原始邮件
  │
  ├── 匹配3: RECEIVER_TIME_WINDOW（收件人+时间窗口）
  │     └── bouncedRecipient == mail_send_record.receiver
  │         AND 发送时间在matchTimeWindowDays天内
  │           → findByReceiverAndTimeWindow(bouncedRecipient, timeWindowDays)
  │           → 取最近一条候选记录
  │           → usedMatchStrategy = "RECEIVER_TIME_WINDOW"
  │
  └── UNMATCHED（未匹配）
        └── 无法匹配到任何发送记录
              → usedMatchStrategy = "UNMATCHED"
              → 仅保存退信记录，不更新send_record状态
```

**注意**：文档早期版本描述为"5级匹配"，实际代码中`handleBounceEnhanced`为3级匹配。In-Reply-To和References是`extractOriginalMessageId`的提取策略，不是独立的匹配策略。

## 6. BounceParser — RFC3464 DSN解析

### 6.1 解析优先级

```
BounceParser.parse(mimeMessage)
  │
  ├── 1. DSN字段解析（最可靠）
  │     ├── Final-Recipient → 退信收件人
  │     ├── Original-Recipient → 原始收件人
  │     ├── Action → failed/delayed/delivered/relayed/expanded
  │     ├── Status → DSN状态码 (5.1.1, 5.4.4等)
  │     └── Diagnostic-Code → 诊断码
  │
  ├── 2. SMTP响应码分类（次可靠）
  │     ├── 550 + User not found → USER_NOT_FOUND (永久)
  │     ├── 550 + Mailbox unavailable → MAILBOX_UNAVAILABLE (永久)
  │     ├── 550 + Spam rejected → SPAM_REJECTED (永久)
  │     ├── 550 + Relay denied → RELAY_DENIED (永久)
  │     ├── 552 + Mailbox full → MAILBOX_FULL (临时)
  │     ├── 450/552 + Rate limit → RATE_LIMIT (临时)
  │     └── 4xx + Network error → NETWORK_TEMPORARY_FAILURE (临时)
  │
  ├── 3. DNS错误分类
  │     ├── domain not found/does not exist → DOMAIN_NOT_FOUND (永久)
  │     ├── SERVFAIL/DNS timeout → DNS_TEMPORARY_FAILURE (临时)
  │     └── 其他DNS异常 → DNS_ERROR (默认临时)
  │
  └── 4. 正文Fallback（最低优先级）
        └── 正则提取收件人和错误信息
```

### 6.2 BounceType分类

| 类型 | permanent | 说明 | 典型SMTP/DSN码 |
|------|-----------|------|---------------|
| USER_NOT_FOUND | true | 收件人不存在 | 550 5.1.1 |
| DOMAIN_NOT_FOUND | true | 域名不存在 | NXDOMAIN |
| MAILBOX_UNAVAILABLE | true | 邮箱不可用 | 550 5.2.1 |
| RELAY_DENIED | true | 中继拒绝 | 550 Relay denied |
| SPAM_REJECTED | true | 垃圾邮件拒绝 | 550 5.7.1 |
| DNS_TEMPORARY_FAILURE | false | DNS临时故障 | SERVFAIL |
| MAILBOX_FULL | false | 邮箱已满 | 552 5.2.2 |
| RATE_LIMIT | false | 频率限制 | 450/552 |
| NETWORK_TEMPORARY_FAILURE | false | 网络临时故障 | 4xx |
| DNS_ERROR | false | 其他DNS异常 | - |
| OTHER | false | 未分类退信 | - |

## 7. 退信处理流程

> 源码：[`MailSendRecordServiceImpl.handleBounceEnhanced()`](backend/src/main/java/com/youlai/boot/support/mail/service/impl/MailSendRecordServiceImpl.java:475)

### 7.1 handleBounceEnhanced — 增强版退信处理

**方法签名**：
```java
public boolean handleBounceEnhanced(
    String messageId,          // 退信邮件自身Message-ID（幂等检查）
    String originalMessageId,  // 原始邮件Message-ID（MESSAGE_ID级别匹配）
    String bounceReason,       // 退信原因
    String bouncedRecipient,   // 退信收件人
    String bounceType,         // 退信类型（BounceType枚举名称）
    String bounceCode,         // 退信代码
    String dsnStatus,          // DSN状态码
    String dsnDiagnosticCode,  // DSN诊断码
    String matchStrategy,      // 匹配策略（null=自动检测）
    String bounceSubject,      // 退信邮件主题
    String bounceSender        // 退信邮件发送者
)
```

**事务边界**：
- **事务内**（`transactionTemplate.executeWithoutResult`）：写bounce_record + 更新send_record状态 + 黑名单 + 创建重试记录
- **事务外**：管理员告警邮件发送 + 重试邮件SMTP发送（避免IO长事务）

```
handleBounceEnhanced(messageId, originalMessageId, bounceReason, ...)
  │
  ├── 【幂等检查】
  │     └── findByBounceMessageId(messageId) → 已存在则跳过
  │
  ├── 【3级匹配策略】查找对应的mail_send_record
  │     ├── 匹配1: MESSAGE_ID → findByMessageId(originalMessageId)
  │     ├── 匹配2: IN_REPLY_TO → findByMessageId(messageId)
  │     ├── 匹配3: RECEIVER_TIME_WINDOW → findByReceiverAndTimeWindow(bouncedRecipient, timeWindowDays)
  │     └── UNMATCHED → 仅保存退信记录
  │
  ├── 【判断永久/临时】
  │     ├── BounceType.valueOf(bounceType) → type.isPermanent()
  │     └── 未指定类型 → BounceParser.parse()重新解析
  │
  ├── 【判断原始/重试记录】
  │     ├── isOriginalRecord = "ORIGINAL".equals(recordType) || recordType == null
  │     └── 只有原始记录才触发自动重试和管理员告警
  │
  ├── ══════════════ 事务内 ══════════════
  │
  │   ├── 3. 写入mail_bounce_record退信记录
  │   │     ├── bounce_message_id, original_message_id, matched_record_id
  │   │     ├── match_strategy, bounce_code, bounce_reason
  │   │     ├── bounced_recipient, bounce_type, bounce_permanent
  │   │     ├── dsn_status, dsn_diagnostic_code
  │   │     ├── bounce_subject, bounce_sender
  │   │     ├── process_result（匹配结果摘要）
  │   │     └── saveIfAbsent(bounceRecord) → 幂等双重检查
  │   │
  │   ├── 4. 更新mail_send_record状态（仅匹配成功+状态为SUCCESS/RETRY_SUCCESS时）
  │   │     ├── delivery_status = BOUNCED(2)
  │   │     ├── send_status = BOUNCE_DETECTED(5)（原始记录）
  │   │     │              或 RETRY_FAILED(8)（重试记录退信）
  │   │     ├── bounce_reason, bounce_code, bounce_message_id
  │   │     └── 注：FAILED/PENDING等状态不更新（避免覆盖发送失败信息）
  │   │
  │   ├── 5. 永久性退信加入黑名单
  │   │     └── isPermanent && permanentBounceBlockResend=true
  │   │           └── mailBounceBlacklistService.autoAdd(bouncedRecipient, ...)
  │   │
  │   ├── 6. 标记管理员告警（仅原始记录第一次退信）
  │   │     └── shouldSendAlert[0] = true
  │   │
  │   └── 7. 临时性退信自动重试（仅原始记录+非永久+autoResend+未超限）
  │         ├── 查询已有重试次数: count(rootRecordId=原始ID)
  │         ├── 未超autoResendMaxCount(默认2) → 创建重试记录
  │         │     ├── record_type = "RETRY"
  │         │     ├── root_record_id = 原始记录ID
  │         │     ├── parent_record_id = 原始记录ID
  │         │     ├── retry_sequence = existingRetryCount + 1
  │         │     ├── send_status = RETRY_PENDING(6)
  │         │     └── retryRecordHolder[0] = retryRecord（事务外发送）
  │         └── 超限 → 不再自动重试
  │
  ├── ══════════════ 事务外 ══════════════
  │
  ├── 6. 发送管理员告警邮件
  │     └── sendBounceAlert(matchedRecord, bounceReason, bouncedRecipient)
  │
  └── 7. 执行自动重试发送
        └── retryRecordHolder[0] != null
              ├── mailService.sendHtmlMailWithResult(receiver, subject, content)
              ├── 成功 → RETRY_SUCCESS(7), 记录messageId, consumeTime, sendTime
              └── 失败 → RETRY_FAILED(8), 记录errorMessage
```

### 7.2 关键设计决策

| 决策 | 原因 | 代码位置 |
|------|------|----------|
| 事务内外分离 | SMTP发送是IO操作，长事务会锁表 | [`handleBounceEnhanced`](backend/src/main/java/com/youlai/boot/support/mail/service/impl/MailSendRecordServiceImpl.java:572) 事务内 + [703-750](backend/src/main/java/com/youlai/boot/support/mail/service/impl/MailSendRecordServiceImpl.java:703) 事务外 |
| 重试记录退信→RETRY_FAILED | 重试记录退信不应再标记BOUNCE_DETECTED | [618-622](backend/src/main/java/com/youlai/boot/support/mail/service/impl/MailSendRecordServiceImpl.java:618) |
| 仅SUCCESS/RETRY_SUCCESS更新delivery_status | 避免覆盖FAILED/PENDING等状态的错误信息 | [610-634](backend/src/main/java/com/youlai/boot/support/mail/service/impl/MailSendRecordServiceImpl.java:610) |
| 非原始记录不触发自动重试 | 防止重试链无限延伸 | [698-700](backend/src/main/java/com/youlai/boot/support/mail/service/impl/MailSendRecordServiceImpl.java:698) |
| saveIfAbsent双重幂等 | bounce_message_id唯一索引 + 代码检查双重保障 | [602](backend/src/main/java/com/youlai/boot/support/mail/service/impl/MailSendRecordServiceImpl.java:602) |
| 管理员告警去重 | 仅原始记录第一次退信发送告警 | [554-556](backend/src/main/java/com/youlai/boot/support/mail/service/impl/MailSendRecordServiceImpl.java:554) |

## 8. 重试链设计

### 8.1 两种重试机制对比

| 特性 | 手动重试 [`retry()`](backend/src/main/java/com/youlai/boot/support/mail/service/impl/MailSendRecordServiceImpl.java:117) | 自动重试 [`handleBounceEnhanced()`](backend/src/main/java/com/youlai/boot/support/mail/service/impl/MailSendRecordServiceImpl.java:652) |
|------|------|------|
| 触发方式 | 管理员手动调用API | IMAP检测到临时退信自动触发 |
| 记录模式 | 在原记录上更新状态 | 创建新的子记录(record_type=RETRY) |
| 状态变更 | FAILED→RETRYING→RETRY_SUCCESS/RETRY_FAILED | 原记录→BOUNCE_DETECTED, 新记录→RETRY_PENDING→RETRY_SUCCESS/RETRY_FAILED |
| 重试次数限制 | MAX_RETRY_LIMIT=5 | autoResendMaxCount=2(默认) |
| 黑名单检查 | 是 | 是（创建子记录前检查） |
| 适用状态 | FAILED(2), BOUNCE_DETECTED(5), RETRY_FAILED(8) | 仅临时退信(isPermanent=false) |

### 8.2 自动重试链式关系

```
原始记录 (record_type=ORIGINAL, root_record_id=NULL, parent_record_id=NULL)
  │
  ├── 重试1 (record_type=RETRY, root_record_id=原始ID, parent_record_id=原始ID, retry_sequence=1)
  │     └── 由handleBounceEnhanced创建，send_status=RETRY_PENDING→自动发送→RETRY_SUCCESS/RETRY_FAILED
  │
  ├── 重试2 (record_type=RETRY, root_record_id=原始ID, parent_record_id=原始ID, retry_sequence=2)
  │     └── 第2次临时退信后自动创建（如果autoResendMaxCount≥2）
  │
  └── ...（最多autoResendMaxCount次）
```

**注意**：自动重试的`parent_record_id`始终指向原始记录ID（而非上一次重试），`root_record_id`也始终指向原始记录ID。
这与文档早期版本描述的"链式parent"不同，实际代码中`parent_record_id`是兼容旧字段。

### 8.3 查询展示

分页查询仅返回原始记录(record_type=ORIGINAL)，重试子记录作为children嵌套返回：
```java
// MailSendRecordMapper.xml
// 1. getMailSendRecordPage → 查询原始记录分页
// 2. getRetryRecordsByRootIds → 批量查询重试子记录
// 3. 按rootRecordId分组，设置为原始记录的children
```

### 8.4 重试次数计算

- **手动重试**：`record.retryCount`字段，每次retry()调用+1，上限`MAX_RETRY_LIMIT=5`
- **自动重试**：`count(root_record_id=原始ID)`，即统计该原始记录下的所有重试子记录数，上限`autoResendMaxCount=2`（默认）
- 两种重试的计数独立，互不影响

## 9. IMAP检查点

> 源码：[`MailImapCheckpoint`](backend/src/main/java/com/youlai/boot/support/mail/model/entity/MailImapCheckpoint.java) + [`BounceEmailJob.detectBounceEmails()`](backend/src/main/java/com/youlai/boot/support/mail/job/BounceEmailJob.java:186)

### 9.1 增量扫描机制

```
mail_imap_checkpoint表字段:
  ├── folder_name = "INBOX"
  ├── username = "registrationMCLC@163.com"
  ├── uid_validity = IMAP文件夹UID有效性标识
  ├── last_uid = 上次处理的最大UID
  ├── last_check_time = 上次检查时间
  ├── last_check_count = 上次检查邮件数
  ├── last_bounce_count = 上次退信数
  └── execution_status = 执行状态（ImapExecutionStatus枚举）
```

### 9.2 扫描模式决策树

```
detectBounceEmails() → 增量扫描决策:
  │
  ├── checkpointEnabled=false 或 folder不支持UIDFolder
  │     └── 降级模式: 全量扫描未读邮件 (FlagTerm SEEN=false)
  │
  └── checkpointEnabled=true 且 folder支持UIDFolder
        │
        ├── checkpoint不存在（首次扫描）
        │     └── 按时间回溯: ReceivedDateTerm ≥ (now - firstScanLookbackDays)
        │           默认回溯30天
        │
        ├── checkpoint存在 + UIDVALIDITY未变化
        │     └── 增量扫描: getMessagesByUID(lastUid+1, LASTUID)
        │           只处理新邮件，避免重复
        │
        └── checkpoint存在 + UIDVALIDITY变化
              └── 重置checkpoint + 按时间回溯
                    UIDVALIDITY变化意味着邮箱UID重置（如邮箱重建）
```

### 9.3 检查点更新

```
updateCheckpointAfterScan(uidFolder, messages, uidValidity, checkCount, bounceCount)
  │
  ├── 遍历所有已处理邮件，获取最大UID
  ├── maxUid > 0 → 更新checkpoint
  │     ├── uid_validity = currentUidValidity
  │     ├── last_uid = maxUid
  │     ├── last_check_time = now()
  │     ├── last_check_count = checkCount
  │     └── last_bounce_count = bounceCount
  └── maxUid = 0 → 不更新（无有效邮件）
```

### 9.4 配置参数

| 参数 | 默认值 | 说明 |
|------|--------|------|
| `spring.mail.imap.checkpoint-enabled` | true | 启用增量扫描 |
| `spring.mail.imap.max-process-count` | 50 | 每次最多处理邮件数 |
| `spring.mail.imap.first-scan-lookback-days` | 30 | 首次扫描回溯天数 |
| `spring.mail.imap.match-time-window-days` | 7 | 退信匹配时间窗口(天) |
| `spring.mail.imap.lock-wait-time` | - | 分布式锁等待时间(秒) |
| `spring.mail.imap.lock-lease-time` | - | 分布式锁租约时间(秒) |

## 10. MailBusinessType — 业务类型

| 类型 | 值 | 说明 | 触发场景 |
|------|-----|------|---------|
| PAPER_SUBMIT | PAPER_SUBMIT | 论文审稿通知 | 管理员发送审稿邀请给专家 |
| PAPER_PASS | PAPER_PASS | 论文审核通过 | 专家通过审核后通知投稿者 |
| PAPER_REJECT | PAPER_REJECT | 论文审核拒绝 | 专家拒绝审核后通知投稿者 |
| USER_REGISTER | USER_REGISTER | 注册成功通知 | 新用户注册成功 |
| RESET_PASSWORD | RESET_PASSWORD | 密码重置 | 用户请求重置密码 |
| MEETING_NOTICE | MEETING_NOTICE | 会议提醒 | 会议相关通知 |
| ADMIN_ERROR_NOTIFY | ADMIN_ERROR_NOTIFY | 管理员错误通知 | 系统内部错误通知管理员 |

## 11. 配置参考

```yaml
# application-dev.yml
spring:
  mail:
    host: smtp.163.com
    port: 465
    username: registrationMCLC@163.com
    password: ${MAIL_PASSWORD}
    imap:
      enabled: true
      host: imap.163.com
      port: 993
      username: registrationMCLC@163.com
      password: ${MAIL_PASSWORD}
      folder-name: INBOX
      ssl-enable: true
      cron: "0 */5 * * * ?"         # 每5分钟扫描
      max-process-count: 50          # 每次最多处理50封
      checkpoint-enabled: true       # 启用增量扫描
      auto-resend: true              # 临时退信自动重发
      auto-resend-max-count: 2       # 最大自动重发次数
      permanent-bounce-block-resend: true  # 永久退信阻止重发
      match-time-window-days: 7      # 匹配时间窗口(天)
      first-scan-lookback-days: 30   # 首次扫描回溯天数
      admin-alert-emails: ""         # 告警邮箱(逗号分隔)
```

## 12. API接口

| 方法 | URL | 说明 |
|------|-----|------|
| GET | /api/v1/mail/records | 邮件发送记录分页(树形) |
| GET | /api/v1/mail/records/{id} | 邮件记录详情 |
| POST | /api/v1/mail/records/{id}/retry | 重试发送 |
| POST | /api/v1/mail/records/{id}/cancel | 取消发送 |
| GET | /api/v1/mail/bounce/records | 退信记录分页 |
| GET | /api/v1/mail/bounce/blacklist | 黑名单列表 |
| POST | /api/v1/mail/bounce/blacklist | 添加黑名单 |
| DELETE | /api/v1/mail/bounce/blacklist/{id} | 删除黑名单 |
| POST | /api/v1/mail/bounce/blacklist/{id}/remove | 移出黑名单(允许重发) |

## 13. 常见问题排查

### 13.1 邮件发送失败

1. 检查 `mail_send_record.send_status` 和 `error_message`
2. 检查SMTP配置: `spring.mail.host/port/username/password`
3. 检查黑名单: `SELECT * FROM mail_bounce_blacklist WHERE email = 'xxx'`
4. 检查重试次数: `retry_count` 是否已达上限(5次)

### 13.2 退信未检测到

1. 检查IMAP配置: `spring.mail.imap.enabled = true`
2. 检查checkpoint: `SELECT * FROM mail_imap_checkpoint`
3. 检查execution_status: 是否为FAILED_*
4. 检查IMAP连接: 确认imap.163.com:993可达
5. 检查定时任务: 确认cron表达式正确

### 13.3 退信匹配不上

1. 检查 `mail_bounce_record.match_strategy`: 如果为UNMATCHED，说明5级匹配都失败
2. 检查 `mail_send_record.message_id`: 是否为空（发送时未获取到Message-ID）
3. 检查 `match-time-window-days`: 时间窗口是否太小
4. 检查 `mail_bounce_record.bounced_recipient`: 是否与 `mail_send_record.receiver` 一致

### 13.4 临时退信未自动重发

1. 检查 `auto-resend = true`
2. 检查 `auto-resend-max-count`: 是否已达上限
3. 检查黑名单: 临时退信不应加入黑名单（只有永久退信才加）
4. 检查 `mail_send_record.retry_count`: 是否超过限制

## 14. 2026-08-14 后新增功能

> 详细链路请参考 [12-email-lifecycle.md](12-email-lifecycle.md)

### 14.1 父子重试模型

原始设计重试时直接覆盖原始记录的 retry_count，无法保留完整发送历史。升级为父子重试模型：

| 字段 | 说明 |
|------|------|
| record_type | ORIGINAL（原始记录）/ RETRY（重试子记录） |
| retry_sequence | 重试序号（1, 2, ...） |
| root_record_id | 指向原始记录 ID（RETRY 记录才有） |
| uk_root_retry | 唯一约束（root_record_id + retry_sequence），防止重复重试 |

**分页查询**：一级列表只查 ORIGINAL 记录，批量查询其 RETRY 子记录组装树形结构（children）。

**故障记录**：2026-10 部署时数据库未同步三列，导致邮件中心报错，通过 ALTER TABLE 补齐修复（详见 [20-case-email-record-type-missing.md](20-case-email-record-type-missing.md)）。

### 14.2 两封邮件机制

注册流程中的邮件通知从单封升级为两封：

| 邮件 | 触发时机 | 模板 | 变量 |
|------|----------|------|------|
| 第一封：待核对 | 用户提交支付凭证后 | 注册待核对模板(id=7) | {{name1}} |
| 第二封：注册通过 | 凭证审核通过 / 对账确认匹配 / 线下登记 | 注册通过模板(id=6) | {{name1}} |

**发送方式**：通过 `MailSendRecordServiceImpl.createAndSend()` 统一入口，business_type=USER_REGISTER。

### 14.3 黑名单拦截（步骤2.5）

createAndSend 流程在"插入数据库"（步骤2）和"SMTP发送"（步骤3）之间新增步骤2.5：

```
步骤2.5: 查询 mail_bounce_blacklist
  → 收件人在黑名单中 → status=FAILED, errorMessage="收件地址在退信黑名单中，已拦截发送"
  → 不在黑名单 → 继续步骤3
```

**设计理由**：避免向已知无效地址反复发送，保护发件域名信誉，减少SMTP资源浪费。