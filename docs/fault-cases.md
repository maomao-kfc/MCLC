# MCLC 真实故障案例库

> 基于代码注释、已知问题清单和实际设计决策整理
> 每个案例含：现象→排查→根因→修复→预防
> 标记：[已修复] / [代码已内置防护] / [已知风险] / [设计讨论]
> 更新时间：2026-10-10

---

## 案例1: 163邮箱IMAP "Unsafe Login" [代码已内置防护]

### 现象
JavaMail连接163邮箱IMAP服务器(imap.163.com:993)时报错：
```
javax.mail.AuthenticationFailedException: Unsafe Login
```
退信检测定时任务BounceEmailJob完全无法工作。

### 排查过程
1. **确认凭证正确**：用户名/授权码在SMTP发送时正常工作，排除密码错误
2. **搜索163邮箱文档**：发现163安全策略要求IMAP客户端发送ID命令(RFC 2971)
3. **查看JavaMail API**：IMAPStore有id()方法，但不在公开API中（com.sun.mail.imap.IMAPStore）
4. **确认RFC 2971**：IMAP ID extension要求客户端发送name/version/os等标识信息

### 根因
163邮箱安全策略：IMAP连接必须发送ID命令标识客户端身份，否则拒绝登录。JavaMail的IMAPStore.id()是内部方法，不在javax.mail公开API中。

### 修复
代码已内置反射调用方案：
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

**前置条件**：需在163邮箱后台→设置→POP3/IMAP/SMTP→授权IP白名单中添加服务器IP。

### 预防
- 部署新环境时，必须先配置163邮箱授权IP白名单
- 监控BounceEmailJob日志，确认IMAP连接成功

### 面试价值
展示第三方服务集成问题的完整排查链路：错误信息→官方文档→RFC标准→API限制→反射workaround。

---

## 案例2: Docker JODConverter锁文件残留 [已修复]

### 现象
Docker容器重启后，Word转PDF功能(JODConverter+LibreOffice)报错：
```
OfficeProcessManager: office process died; restarting
```
或LibreOffice无法启动，端口8100被占用。

### 排查过程
1. **查看容器日志**：JODConverter尝试连接LibreOffice端口8100失败
2. **进入容器检查**：发现`/tmp/.~lock.*`文件残留
3. **确认根因**：JODConverter在非正常关闭时不会清理锁文件，下次启动时检测到锁文件认为LibreOffice已在运行

### 根因
JODConverter使用LibreOffice的socket模式，通过`/tmp`目录下的锁文件协调进程。容器被强制停止（kill/OOM）时，锁文件未被清理。

### 修复
Dockerfile ENTRYPOINT添加清理命令：
```dockerfile
ENTRYPOINT ["sh", "-c", "rm -rf /tmp/.~lock.* && java -jar app.jar"]
```

### 预防
- 每次容器启动时自动清理锁文件
- 考虑配置JODConverter的taskExecutionTimeout和maxTasksPerProcess

### 面试价值
展示Docker容器化环境下的进程管理问题，以及ENTRYPOINT清理脚本的最佳实践。

---

## 案例3: 退信误判——永久退信vs临时退信 [代码已内置防护]

### 现象
部分550响应码的退信被错误分类为永久退信，导致收件人被加入黑名单，后续正常邮件无法发送。

### 排查过程
1. **查看mail_bounce_blacklist表**：发现部分被加入黑名单的邮箱实际是有效的
2. **分析BounceParser分类逻辑**：550响应码有7种子模式，部分是临时性的
3. **具体案例**：
   - `550 Rate limit exceeded` → 临时限流，不应加黑名单
   - `550 Spam rejected` → 被拒收，可能是临时策略调整
   - `550 User not found` → 永久退信，应加黑名单

### 根因
SMTP 550响应码语义不唯一，需要根据正文内容进一步区分永久/临时退信。BounceParser最初对550采用简单分类，未充分考虑子模式差异。

### 修复
BounceParser已实现7种550子模式分类：
```
550_USER_NOT_FOUND → PERMANENT (用户不存在)
550_MAILBOX_UNAVAILABLE → PERMANENT (邮箱不可用)
550_SPAM_REJECTED → PERMANENT (被拒收)
550_RELAY_DENIED → PERMANENT (中继被拒)
552_MAILBOX_FULL → TEMPORARY (邮箱满)
RATE_LIMIT → TEMPORARY (速率限制)
NETWORK_TEMPORARY → TEMPORARY (网络临时故障)
```

### 预防
- 分类优先级：DSN状态码 > DNS查询 > SMTP响应码 > 通用匹配
- 对不确定的退信默认为TEMPORARY，避免误加黑名单

### 面试价值
展示邮件协议的复杂性——SMTP响应码语义不唯一，需要多维度分类。

---

## 案例4: 支付回调幂等性 [已知风险]

### 现象
Stripe/支付宝/微信支付回调可能重复发送，如果处理不当会导致：
- 订单状态重复更新
- 重复发放注册权益
- 日志数据混乱

### 排查过程
1. **阅读PaymentServiceImpl**：发现回调处理逻辑中缺少显式幂等检查
2. **确认三方支付回调机制**：Stripe/支付宝/微信都可能在网络超时后重发回调
3. **确认数据库约束**：payment_order表有唯一约束，但状态更新无乐观锁

### 根因
支付回调处理未实现显式幂等检查。虽然数据库层面有唯一约束保护，但状态流转（如PENDING→PAID）可能被重复执行。

### 当前状态
- Stripe Webhook有签名验证，可防止伪造回调
- 但回调重复发送场景下，缺少显式幂等检查
- 已知问题标记：ARCH-003（支付回调缺幂等文档）

### 建议修复
```java
// PaymentServiceImpl.handleCallback()
PaymentOrder order = paymentOrderMapper.selectById(orderId);
if (order.getStatus() == PaymentStatus.PAID) {
    log.info("回调重复，订单已支付: {}", orderId);
    return; // 幂等返回
}
// 继续处理...
```

### 面试价值
展示支付系统的经典问题——回调幂等性，以及如何通过状态检查实现幂等。

---

## 案例5: JWT alg降级攻击防护 [代码已内置防护]

### 现象
JWT算法降级攻击(Algorithm Confusion Attack)：攻击者将RS256签名的Token改为HS256，用公钥作为HMAC密钥伪造Token。

### 排查过程
1. **阅读JwtTokenManager.validateToken()**：发现alg白名单校验
2. **确认防护机制**：仅允许HS256算法，其他算法直接拒绝

### 根因
JWT规范允许alg字段指定签名算法，如果服务端不校验alg，攻击者可以：
1. 获取服务器公钥（RS256公钥是公开的）
2. 将Token的alg改为HS256
3. 用公钥作为HMAC密钥签名Token
4. 服务端用公钥验证HS256 Token → 通过

### 修复
JwtTokenManager已内置alg白名单：
```java
// JwtTokenManager.validateToken()
String alg = header.getAlgorithm();
if (!"HS256".equals(alg)) {
    throw new JwtValidationException("不支持的算法: " + alg);
}
```

### 颢防
- 永远不要信任Token中的alg字段
- 服务端硬编码允许的算法列表
- 使用JWT库的严格验证模式

### 面试价值
展示JWT安全最佳实践——alg白名单是防止算法降级攻击的关键。

---

## 案例6: tokenVersion批量会话失效 [代码已内置防护]

### 现象
管理员修改用户权限后，用户旧Token仍然有效，权限未即时更新。

### 排查过程
1. **阅读JwtTokenManager**：发现tokenVersion机制
2. **确认流程**：用户登录时Token包含tokenVersion→每次验证时与Redis中的tokenVersion比对

### 根因
JWT是无状态的，Token签发后无法撤销。权限变更后，旧Token仍然有效。

### 修复
JwtTokenManager已实现tokenVersion机制：
```java
// 签发Token时
claims.claim("tokenVersion", user.getTokenVersion());

// 验证Token时
Integer tokenVersion = claims.get("tokenVersion", Integer.class);
Integer currentVersion = redisTemplate.opsForValue().get("token:version:" + userId);
if (!tokenVersion.equals(currentVersion)) {
    throw new JwtValidationException("Token版本过期");
}

// 批量失效
public void invalidateUserSessions(Long userId) {
    redisTemplate.opsForValue().increment("token:version:" + userId);
    // 所有旧Token的tokenVersion与Redis不一致 → 失效
}
```

### 面试价值
展示JWT无状态认证的局限性和解决方案——tokenVersion机制实现用户级会话失效。

---

## 案例7: Math.random验证码安全风险 [已知风险]

### 现象
验证码生成使用Math.random()而非SecureRandom，存在可预测性风险。

### 排查过程
1. **代码审查发现**：CODE-015标记
2. **Math.random问题**：使用伪随机数生成器(PRNG)，种子可预测
3. **攻击场景**：攻击者可预测验证码序列，绕过验证

### 根因
Math.random()底层使用java.util.Random，种子基于系统时间，可被预测。SecureRandom使用操作系统熵源，不可预测。

### 建议修复
```java
// 替换
String code = String.valueOf((int)(Math.random() * 900000) + 100000);
// 为
SecureRandom secureRandom = new SecureRandom();
String code = String.valueOf(secureRandom.nextInt(900000) + 100000);
```

### 面试价值
展示安全编码意识——验证码等安全敏感场景必须使用SecureRandom。

---

## 案例8: 抵扣码唯一性风险 [已知风险]

### 现象
论文审核通过后生成8位随机抵扣码，Random生成可能重复。

### 排查过程
1. **代码审查发现**：CODE-012标记
2. **计算碰撞概率**：8位数字=10^8=1亿种可能，Birthday Paradox约1万次后碰撞概率约0.05%
3. **实际风险**：学术会议论文数量通常<1000，碰撞概率极低但非零

### 根因
使用Random生成抵扣码，无唯一性校验。

### 建议修复
```java
// 方案1：数据库唯一索引 + 重试
// 方案2：UUID前8位
// 方案3：雪花算法
```

### 面试价值
展示对Birthday Paradox的理解和唯一性保证的多种方案。

---

## 案例9: StripeProvider注入未使用的Bean [已知问题]

### 现象
StripePaymentProvider注入了BankTransferProperties，但未使用。

### 排查过程
1. **代码审查发现**：PAY-001标记
2. **确认StripePaymentProvider代码**：确实未使用BankTransferProperties

### 根因
代码复制粘贴时未清理依赖注入。

### 建议修复
移除StripePaymentProvider中的BankTransferProperties注入。

### 面试价值
展示代码审查的细致程度——发现未使用的依赖注入。

---

## 案例10: 团队折扣仅前端计算 [已知风险]

### 现象
团队注册折扣金额仅在前端计算，后端未校验，存在价格篡改风险。

### 排查过程
1. **代码审查发现**：ARCH-007标记
2. **确认前端计算逻辑**：前端根据团队人数计算折扣
3. **确认后端支付逻辑**：PaymentServiceImpl直接使用前端传入的金额

### 根因
后端缺少折扣计算和校验逻辑，信任前端传入的价格。

### 建议修复
后端应独立计算折扣金额，与前端传入金额比对，不一致则拒绝。

### 面试价值
展示安全意识——永远不要信任前端传入的价格，后端必须独立计算。

---

## 附录: 故障案例面试使用指南

### 如何在面试中引用案例

1. **被问"遇到过什么问题"时**：选案例1或3，展示排查链路
2. **被问"如何保证幂等性"时**：选案例4，展示支付回调幂等
3. **被问"JWT有什么安全问题"时**：选案例5+6，展示alg降级+tokenVersion
4. **被问"代码审查关注什么"时**：选案例7+8+9，展示安全编码意识
5. **被问"Docker部署踩过什么坑"时**：选案例2，展示容器化经验

### 真实性标记说明

| 标记 | 含义 | 面试中如何说 |
|------|------|-------------|
| [已修复] | 代码已修复，可安全引用 | "我们遇到过这个问题，修复方案是..." |
| [代码已内置防护] | 设计时已考虑，代码中有防护 | "我们设计时考虑了这个风险，方案是..." |
| [已知风险] | 已识别但未修复 | "代码审查时发现了这个风险，建议修复方案是..." |
| [设计讨论] | 架构层面的讨论 | "我们讨论过这个设计决策，权衡了..." |