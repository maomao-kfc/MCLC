# MCLC 面试题库：邮件系统

> **基于实际代码，严禁编造**。每道题标注考察点和代码出处。

---

## Q1: 你的邮件系统为什么要设计 9 个状态？直接用"成功/失败"不够吗？

**考察点**: 状态机设计、邮件投递链路理解

**参考答案**:

不够。核心原因是 **SMTP 发送成功≠邮件最终投递成功**。

简单二态（成功/失败）的问题：
1. SMTP 返回 250 OK 只代表邮件服务器接受了邮件，之后还要经过 DNS 查询 MX 记录 → 收件方 SMTP → 垃圾邮件过滤等环节，任何环节都可能失败
2. 退信是**异步**到达的（可能几分钟后甚至几天后），无法在发送时同步得知
3. 退信后需要重试，重试本身也是一次发送，有自己的成功/失败状态

MCLC 的 9 态设计（MailSendStatus.java）：
```
PENDING(0) → SUCCESS(1) / FAILED(2) / CANCELLED(4)
FAILED(2) → RETRYING(3) → SUCCESS(1) / FAILED(2)
SUCCESS(1) → BOUNCE_DETECTED(5)  [IMAP退信检测]
BOUNCE_DETECTED(5) → RETRY_PENDING(6) → RETRY_SUCCESS(7) / RETRY_FAILED(8)
```

关键区分：
- SUCCESS vs BOUNCE_DETECTED：区分"服务器接受"和"最终投递失败"
- FAILED vs RETRY_FAILED：区分"首次发送失败"和"重试发送失败"
- RETRYING vs RETRY_PENDING：区分"正在重试"和"退信后等待重试"

**追问方向**: 退信检测怎么实现的？（IMAP 轮询 + BounceParser 解析 DSN）

**代码出处**: MailSendStatus.java / MailSendRecordServiceImpl.java

---

## Q2: 退信检测是怎么实现的？怎么区分永久退信和临时退信？

**考察点**: 邮件协议（DSN/RFC 3463）、定时任务设计

**参考答案**:

**实现链路**（BounceEmailJob.java 46.2KB）：
1. XXL-Job 定时触发 → 连接发件邮箱的 IMAP 收件箱
2. 从 mail_imap_checkpoint 表读取游标（断点续传，避免重复处理）
3. 拉取新邮件 → BounceParser 解析退信内容
4. 提取 DSN（Delivery Status Notification）和增强状态码（如 5.1.1）
5. 匹配 mail_send_record（收件人+主题+时间）→ 更新状态为 BOUNCE_DETECTED
6. 永久退信 → 写入 mail_bounce_blacklist
7. 更新 IMAP 游标

**永久 vs 临时区分**（BounceType.java，基于 RFC 3463）：

| 分类 | 判定依据 | 处理策略 |
|------|----------|----------|
| 永久退信(permanent=true) | 550 5.1.1 用户不存在 / NXDOMAIN 域名不存在 / 550 5.7.1 垃圾邮件拒绝 | 加入黑名单，不再发送 |
| 临时退信(permanent=false) | 552 4.2.2 邮箱已满 / 450 频率限制 / DNS SERVFAIL | 可重试 |

5 种永久退信：USER_NOT_FOUND / DOMAIN_NOT_FOUND / MAILBOX_UNAVAILABLE / RELAY_DENIED / SPAM_REJECTED
6 种临时退信：DNS_TEMPORARY_FAILURE / MAILBOX_FULL / RATE_LIMIT / NETWORK_TEMPORARY_FAILURE / DNS_ERROR / OTHER

**追问方向**: 游标断点续传怎么保证不丢不重？（UID 单调递增 + checkpoint 落库）

**代码出处**: BounceEmailJob.java / BounceParser.java / BounceType.java

---

## Q3: 邮件重试为什么不直接更新原记录，而是创建子记录？

**考察点**: 数据建模、审计思维

**参考答案**:

MCLC 采用**父子重试模型**（record_type + retry_sequence + root_record_id）：

- 原始记录：record_type=ORIGINAL
- 重试记录：record_type=RETRY, retry_sequence=1/2/..., root_record_id=指向原始记录
- 唯一约束：uk_root_retry(root_record_id, retry_sequence)

**不覆盖原记录的理由**：
1. **审计需要**：每次发送都是独立事件，有自己的时间戳/SMTP 响应/错误信息，覆盖后无法追溯
2. **树形展示**：管理端邮件中心列表只显示 ORIGINAL，展开可看重试链（children），信息层次清晰
3. **防重**：唯一约束保证同一原始记录的第 N 次重试只有一条，避免并发重试产生重复
4. **统计准确**：重试次数 = 子记录数，不需要维护计数器字段（避免并发更新）

**分页查询实现**：一级查 ORIGINAL，批量查 RETRY 子记录按 rootRecordId 分组组装 children。

**追问方向**: 如果重试次数很多会不会影响性能？（分页只查一级，子记录批量查，有索引）

**代码出处**: MailSendRecordServiceImpl.java getMailSendRecordPage() / MailSendRecordMapper.xml

---

## Q4: 黑名单机制是怎么设计的？什么时候拦截？

**考察点**: 防御性设计、发送信誉保护

**参考答案**:

**写入时机**：BounceEmailJob 检测到永久退信 → 写入 mail_bounce_blacklist

**拦截时机**（两处）：
1. createAndSend 步骤 2.5：发送前检查，黑名单中 → status=FAILED + "收件地址在退信黑名单中，已拦截发送"
2. retry 重试前：黑名单中 → status=FAILED + 拦截重发

**为什么需要黑名单**：
1. **保护发件域名信誉**：向已知无效地址反复发送，会被邮件服务商降权，影响所有邮件投递
2. **避免无效重试**：用户不存在（550 5.1.1）是永久性的，重试永远不会成功
3. **节省资源**：SMTP 连接+发送是 IO 操作，拦截无效发送

**管理**：管理端 blacklist.vue 可查看/手动移除（误判场景，如用户后来注册了邮箱）

**追问方向**: 手动移除黑名单后怎么保证不会被再次加入？（移除后如果再次退信会重新加入，这是正确行为）

**代码出处**: MailBounceBlacklistServiceImpl.java / MailSendRecordServiceImpl.java

---

## Q5: 邮件发送失败会不会导致业务回滚？怎么处理的？

**考察点**: 事务边界设计、业务解耦

**参考答案**:

**不会回滚**。所有业务邮件发送都包裹在 try-catch 中，失败仅 log.error：

```java
private void sendRegistrationSuccessEmail(RegisterUserInformation registerUser) {
    try {
        mailSendRecordService.createAndSend(USER_REGISTER, ...);
    } catch (Exception e) {
        log.error("发送注册成功邮件失败：{}", e.getMessage());
        // 不抛出，不回滚业务
    }
}
```

**设计理由**：
1. **邮件是通知手段，不是业务核心**：凭证审核通过/对账确认是核心操作，邮件失败不应导致这些操作回滚
2. **任务表模式兜底**：邮件中心先落库（PENDING），即使发送失败记录也在，可人工重试
3. **用户有替代查询途径**："我的注册"页面可查状态，不完全依赖邮件

**如果邮件必须成功呢**（如密码重置）：
- 密码重置邮件失败时用户无法完成操作，此时前端应提示用户"邮件发送失败，请重试"
- 但仍然不回滚业务（重置 token 已生成），用户可重新触发发送

**追问方向**: 如果邮件发送很慢（SMTP 超时），会不会阻塞主线程？（可以考虑异步发送+消息队列，当前是同步但可接受）

**代码出处**: PaymentProofServiceImpl.java / PaymentReconciliationServiceImpl.java

---

## Q6: 你提到"任务表模式"，具体是什么？和直接发送有什么区别？

**考察点**: 可靠性设计模式

**参考答案**:

**任务表模式（Outbox Pattern 变体）**：先落库再发送。

```
直接发送: 业务 → SMTP.send() → 成功/失败（失败则记录丢失）
任务表:   业务 → INSERT mail_send_record(PENDING) → SMTP.send() → UPDATE status
```

**优势**：
1. **记录不丢失**：即使 SMTP 发送过程崩溃（OOM/断电），PENDING 记录已在数据库，可人工处理
2. **可追踪**：每封邮件有完整生命周期记录（创建时间/发送时间/耗时/结果/错误信息）
3. **可重试**：失败记录可手动/自动重试
4. **可审计**：谁在什么时间给谁发了什么邮件，全量可查

**MCLC 的实现**：
- createAndSend()：创建 PENDING → save() → 黑名单检查 → SMTP 发送 → 更新状态
- 注意：save() 和后续 updateById() 不在同一个事务中（各是独立操作），避免长事务持有数据库连接

**追问方向**: 能不能用消息队列替代？（可以，RabbitMQ/Kafka 更标准，但当前规模任务表已足够）

**代码出处**: MailSendRecordServiceImpl.java createAndSend()