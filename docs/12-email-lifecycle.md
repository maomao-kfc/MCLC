# 邮件全生命周期（9态状态机 + 退信检测 + 父子重试）

> **状态**: 【已实现】【已验证】  
> **核心文件**: MailSendRecordServiceImpl.java (39.3KB) / BounceEmailJob.java (46.2KB) / BounceParser.java (26.6KB)  
> **数据库表**: mail_send_record / mail_bounce_record / mail_bounce_blacklist / mail_imap_checkpoint  
> **最后验证**: 2026-10-29 本地环境

---

## 1. 架构概览

```
业务触发 ──→ createAndSend() ──→ SMTP发送 ──→ 状态更新
                                      │
              XXL-Job定时 ◄───────────┘
              BounceEmailJob轮询IMAP收件箱
                    │
              BounceParser解析退信
                    │
              匹配发送记录 → BOUNCE_DETECTED
                    │
              永久退信 → 加入黑名单
              临时退信 → 可重试
```

### 核心文件清单

| 文件 | 大小 | 职责 |
|------|------|------|
| MailSendRecordServiceImpl.java | 39.3KB | 邮件发送记录服务：任务表模式、发送、重试、退信处理 |
| BounceEmailJob.java | 46.2KB | 退信检测定时任务：IMAP连接、增量扫描、退信识别、处理 |
| BounceParser.java | 26.6KB | 通用退信解析器：DSN/SMTP/DNS/正文4级分类 |
| MailServiceImpl.java | ~4KB | 邮件发送：JavaMailSender封装 |
| MailSendStatus.java | 1.9KB | 邮件9态枚举 |
| BounceType.java | 3.7KB | 退信类型枚举（5永久+6临时） |
| MailBusinessType.java | 1.6KB | 业务类型枚举（7种） |

## 2. 9 态状态机【代码实测】

**文件**: MailSendStatus.java

```
PENDING(0, "待发送")
  → SUCCESS(1, "已提交至邮件服务器")        [SMTP 250 OK]
  → FAILED(2, "发送失败")                   [SMTP异常/超时]
  → CANCELLED(4, "已取消")                  [管理员手动取消]

FAILED(2)
  → RETRYING(3, "重试中")                   [手动/自动重试]
  → SUCCESS(1) / FAILED(2)

SUCCESS(1)
  → BOUNCE_DETECTED(5, "退信-投递失败")      [IMAP检测到退信]

BOUNCE_DETECTED(5)
  → RETRY_PENDING(6, "待重试")              [退信后自动重试]
  → RETRY_SUCCESS(7, "重试成功")            [重试邮件SMTP成功]
  → RETRY_FAILED(8, "重试失败")             [重试邮件SMTP失败]
```

**关键认知**：`SUCCESS` 表示 SMTP 服务器接受（250 OK），**不代表最终投递成功**。最终投递状态需 IMAP 退信检测确认。

## 3. 发送流程（createAndSend）

**文件**: MailSendRecordServiceImpl.java

```
步骤1: 创建MailSendRecord, status=PENDING
步骤2: 插入数据库（先落库，确保记录不丢失）
步骤2.5: 黑名单检查
  → 收件人在mail_bounce_blacklist中 → status=FAILED, 拦截发送
步骤3: 调用MailService.sendHtmlMailWithResult() (SMTP)
步骤4: 根据结果更新状态/SUCCESS或FAILED
```

**任务表模式**：先落库再发送，即使发送过程崩溃，记录也不会丢失，可人工重试。

## 4. 重试机制

### 4.1 手动重试（retry）

```
校验: 状态必须为 FAILED / BOUNCE_DETECTED / RETRY_FAILED
校验: retryCount < MAX_RETRY_LIMIT
检查: 黑名单拦截
执行: status=RETRYING → SMTP重发 → 递增retryCount → 更新状态
```

### 4.2 父子重试模型【代码实测】

**数据库字段**: record_type / retry_sequence / root_record_id

| 字段 | 说明 |
|------|------|
| record_type | ORIGINAL（原始记录）/ RETRY（重试子记录） |
| retry_sequence | 重试序号（1, 2, ...） |
| root_record_id | 指向原始记录 ID（RETRY 记录才有） |
| uk_root_retry | 唯一约束（root_record_id + retry_sequence） |

**分页查询**：一级列表只查 ORIGINAL 记录，批量查询其 RETRY 子记录组装树形结构（children）。

**设计理由**：重试不覆盖原始记录，保留完整发送历史链，便于审计和故障排查。

## 5. 退信检测（BounceEmailJob）【已实现】

**文件**: BounceEmailJob.java (46.2KB) — XXL-Job 定时任务

### 5.1 流程

```
XXL-Job触发 → 连接IMAP收件箱
  → 从mail_imap_checkpoint读取游标（断点续传）
  → 拉取新邮件
  → BounceParser解析（DSN/状态码/退信类型）
  → 匹配mail_send_record（通过收件人地址+主题+时间）
  → 更新status=BOUNCE_DETECTED
  → 写入mail_bounce_record
  → 永久退信 → 写入mail_bounce_blacklist
  → 更新mail_imap_checkpoint游标
```

### 5.2 断点续传（IMAP游标机制）

`mail_imap_checkpoint` 表记录 IMAP UID 游标（lastUid / uidValidity / lastScanTime），避免重复处理已解析邮件。Job 崩溃重启后从游标继续。

### 5.3 IMAP增量扫描策略【代码实测】

| 场景 | 策略 |
|------|------|
| UIDVALIDITY 变化 | 邮箱已重建，UID 可能失效 → 全量扫描 |
| 首次运行（无 checkpoint） | 回溯 N 天邮件 |
| 有 checkpoint | 仅扫描 Uid > lastUid 的邮件 |
| checkpoint 异常 | 降级为全量扫描 |

### 5.4 退信识别（isBounceEmail 三策略）

| 策略 | 匹配目标 | 示例 |
|------|----------|------|
| 策略1: 发送者匹配 | MAILER-DAEMON / postmaster | 自动退信发送者 |
| 策略2: 主题匹配 | failure / undelivered / returned | 退信主题关键词 |
| 策略3: ContentType 匹配 | multipart/report | DSN 结构化退信 |

### 5.5 BounceParser 4级分类【代码实测】

```
BounceParser.parse(bounceMessage)
  ├──1. Header匹配: X-Failed-Recipients / Failure-Report
  ├──2. DSN提取(RFC 3464): Final-Recipient / Diagnostic-Code(smtp;5.x.x) / Action
  ├──3. 收件人提取: DSN Final-Recipient > Original-Recipient > Body正则
  └──4. 分类决策:
        ├── DSN状态码 → 5.1.1/5.4.4/5.2.1/5.2.2/5.7.1
        ├── DNS查询 → MX记录验证 → PERMANENT/TEMPORARY
        ├── SMTP响应码 → 550(7子模式)/552/554/421/450
        └── 通用匹配 → 554→REJECTED / OTHER
```

### 5.6 5级匹配策略（handleBounceEnhanced）【代码实测】

| 优先级 | 匹配方式 | 说明 |
|--------|----------|------|
| 0 | 幂等检查 | 查 mail_bounce_record 是否已存在相同 bounceMessageId |
| 1 | MESSAGE_ID | bounceMessageId = mail_send_record.messageId（RFC 2822 全局唯一） |
| 2 | IN_REPLY_TO | 退信邮件的 In-Reply-To 头指向原始邮件 |
| 3 | RECEIVER_TIME_WINDOW | 收件人匹配 + 发送时间 ±24h（降级方案） |
| 4 | UNMATCHED | 无法匹配，记录 unmatched 退信，人工处理 |

**匹配成功后事务内操作**：插入 bounce_record → 更新 send_record 状态 → 永久退信插入黑名单 → 临时退信创建重试记录。

### 5.7 163邮箱 Unsafe Login 问题【实际排查】

**现象**：JavaMail 连接 163 邮箱 IMAP 时报 "Unsafe Login" 错误。

**排查链路**：错误信息 → 163邮箱帮助文档（要求IMAP客户端发送ID命令）→ RFC 2971（IMAP ID extension）→ JavaMail API检查（IMAPStore.id() 不在公开API中）→ 反射调用解决方案。

**代码实现**：
```java
// BounceEmailJob.sendImapId()
Method idMethod = store.getClass().getMethod("id", Map.class);
Map<String, String> idMap = Map.of(
    "name", "MCLC-BounceDetector",
    "version", "1.0",
    "os", System.getProperty("os.name")
);
idMethod.invoke(store, idMap);
```

**前置条件**：163邮箱后台 → 设置 → POP3/IMAP/SMTP → 授权IP白名单中添加服务器IP。

### 5.8 TransactionTemplate 事务分离设计【代码实测】

退信处理涉及两类操作，用 TransactionTemplate 分离：

| 类别 | 操作 | 位置 |
|------|------|------|
| 事务内（DB） | 插入 bounce_record + 更新 send_record + 插入黑名单 + 创建重试记录 | transactionTemplate.execute() |
| 事务外（IO） | 发送重试邮件 + 发送告警通知 | execute() 之后 |

**设计理由**：IO 操作（发送邮件）耗时长，若放在事务内会导致数据库连接长时间占用；IO 失败不应回滚已正确的 DB 操作。

## 6. 退信分类（BounceType）【代码实测】

**文件**: BounceParser.java + BounceType.java

**分类标准**: RFC 3463 (Enhanced Mail System Status Codes)

### 永久退信（permanent=true）→ 加入黑名单

| 类型 | 触发条件 | 典型状态码 |
|------|----------|-----------|
| USER_NOT_FOUND | 收件人不存在 | 550 5.1.1 |
| DOMAIN_NOT_FOUND | 域名不存在 | NXDOMAIN |
| MAILBOX_UNAVAILABLE | 邮箱不可用 | 550 5.2.1 |
| RELAY_DENIED | 中继拒绝 | 550 |
| SPAM_REJECTED | 垃圾邮件拒绝 | 550 5.7.1 |

### 临时退信（permanent=false）→ 可重试

| 类型 | 触发条件 | 典型状态码 |
|------|----------|-----------|
| DNS_TEMPORARY_FAILURE | DNS临时故障 | SERVFAIL |
| MAILBOX_FULL | 邮箱已满 | 552 4.2.2 |
| RATE_LIMIT | 频率限制 | 450/552 |
| NETWORK_TEMPORARY_FAILURE | 网络临时故障 | - |
| DNS_ERROR | 其他DNS异常 | 默认临时 |
| OTHER | 未分类 | 默认临时 |

## 7. 黑名单机制【已实现】

**文件**: MailBounceBlacklistServiceImpl.java

- **写入时机**: BounceEmailJob 检测到永久退信
- **拦截时机**: createAndSend 步骤2.5 / retry 重试前
- **拦截行为**: status=FAILED + errorMessage="收件地址在退信黑名单中，已拦截发送"
- **管理**: 管理端 mailRecord/blacklist.vue 可查看/移除黑名单

## 8. 业务类型（MailBusinessType）

| 类型 | 说明 |
|------|------|
| PAPER_SUBMIT | 论文审稿通知（发给专家） |
| PAPER_PASS | 论文审核通过（发给投稿者） |
| PAPER_REJECT | 论文审核拒绝（发给投稿者） |
| USER_REGISTER | 注册成功通知（含待核对+注册成功两封） |
| RESET_PASSWORD | 密码重置 |
| MEETING_NOTICE | 会议提醒 |
| ADMIN_ERROR_NOTIFY | 管理员错误通知（系统内部） |

## 9. 邮件模板

**表**: sys_email_template

| ID | 模板名 | 用途 | 变量 |
|----|--------|------|------|
| 6 | 注册通过模板 | 第二封邮件 | {{name1}} |
| 7 | 注册待核对模板 | 第一封邮件 | {{name1}} |

## 10. 关键设计决策

| 决策 | 理由 |
|------|------|
| 任务表模式（先落库再发送） | 发送崩溃不丢记录，可人工重试 |
| SUCCESS≠最终投递成功 | SMTP 250 只代表服务器接受，退信检测才确认投递 |
| 父子重试模型 | 保留完整历史链，不覆盖原始记录 |
| 永久/临时退信分类 | 永久退信加黑名单避免反复发送，临时退信可重试 |
| IMAP 游标断点续传 | Job 崩溃不重复处理 |
| 黑名单拦截在发送前 | 避免向已知无效地址反复发送，保护发件域名信誉 |
| 邮件发送 try-catch 不阻断业务 | 邮件是通知手段，失败不应导致业务回滚 |

## 11. 曾经发生的故障【曾经发生】

| 故障 | 原因 | 修复 |
|------|------|------|
| mail_send_record 表缺 record_type 等三列 | 代码升级重试模型但数据库未同步 | ALTER TABLE 补列+索引 |
| 论文通过邮件服务器未发送 | 发送逻辑条件判断问题 | 修复发送条件 |
| SMTP 发送成功但用户未收到 | SMTP 成功≠最终送达，退信检测才发现 | IMAP 退信检测机制 |