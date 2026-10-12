# MCLC 项目面试题库

> 基于项目实际代码生成，所有答案均有代码证据支撑
> 更新时间：2026-10-10
> 层次划分：A(项目概述) → B(架构设计) → C(核心模块) → D(邮件系统) → E(支付系统) → F(安全认证) → G(故障排查)

---

## A. 项目概述层（1分钟电梯演讲）

### A1: 请用1分钟介绍这个项目

**考察目的**：项目概括能力、技术栈理解

**初级回答**：
MCLC是一个学术会议管理系统，用Spring Boot+Vue3开发，有注册、论文投稿、支付、邮件等功能。

**深入回答**：
MCLC是面向国际学术会议的全生命周期管理平台，基于Spring Boot 4.0.1单体架构，技术栈包括MyBatis-Plus 3.5.15、Redis+Redisson 4.1.0、Spring Security 6.x。前端双轨架构：管理端Vue3+TypeScript+Element Plus，参会者端Vue3+JavaScript+Element Plus。核心业务覆盖6大模块：会议日程管理（三级层次结构）、参会者注册（6步向导）、论文投稿审稿（DOCX转PDF+专家审核+抵扣码）、邮件系统（9态状态机+IMAP退信检测+自动重试）、四轨支付（Stripe/微信/支付宝/银行转账策略模式）、RBAC权限管理（4种认证Provider+JWT/Redis-Token双模式）。

**代码证据**：
- 架构：[`SecurityConfig.java`](backend/src/main/java/com/youlai/boot/config/SecurityConfig.java:1) — 4种AuthenticationProvider
- 支付策略：[`PaymentRouter.java`](backend/src/main/java/com/youlai/boot/portal/payment/PaymentRouter.java:1) — 策略模式路由
- 邮件状态机：[`MailStatusEnum`](backend/src/main/java/com/youlai/boot/support/mail/enums/MailStatusEnum.java) — 9态枚举

**追问**：为什么选择单体架构而不是微服务？
→ 项目规模适中（45张表、261个API端点），团队规模小，单体架构开发部署效率高，避免微服务的分布式复杂度（网络延迟、数据一致性、运维成本）。

---

### A2: 项目的技术选型理由是什么？

**考察目的**：技术决策能力

**深入回答**：
| 技术 | 选型理由 | 代码证据 |
|------|----------|----------|
| Spring Boot 4.0.1 | 企业级成熟框架，生态丰富，Security/Mail/Redis集成开箱即用 | `pom.xml` |
| MyBatis-Plus 3.5.15 | 简化CRUD，支持Lambda查询，分页插件内置 | 所有Mapper继承BaseMapper |
| Redis+Redisson | Token存储+验证码+分布式锁（退信检测互斥） | `RedissonClient`在[`BounceEmailJob.java`](backend/src/main/java/com/youlai/boot/support/mail/job/BounceEmailJob.java:1) |
| MapStruct 1.6.3 | 编译期对象映射，无反射开销，类型安全 | 23+Converter类 |
| Caffeine | 本地缓存菜单/字典/配置，减少Redis网络开销 | `CaffeineConfig` |
| JODConverter+LibreOffice | 服务端DOCX转PDF，不依赖客户端软件 | [`DocxToPdfServiceImpl`](backend/src/main/java/com/youlai/boot/portal/service/impl/DocxToPdfServiceImpl.java) |

---

## B. 架构设计层（系统设计思维）

### B1: 项目的分层架构是怎样的？

**考察目的**：架构理解深度

**深入回答**：
```
Frontend(Vue3) → Controller → Service → Mapper → MySQL
                    ↓
              SecurityFilterChain
              RateLimiterFilter → CaptchaValidationFilter → TokenAuthenticationFilter
```

关键设计点：
1. **Controller层**：@RestController + @PreAuthorize权限注解 + @Log操作日志 + @RepeatSubmit防重
2. **Service层**：业务逻辑 + 事务管理 + 状态机 + 策略路由，MapStruct转换Entity↔Form↔VO↔Query
3. **Mapper层**：BaseMapper<Entity> + 自定义XML，@DataPermission数据权限拦截
4. **横切关注点**：LogAspect(操作日志) + RepeatSubmitAspect(防重) + RateLimiterFilter(Redis限流) + GlobalExceptionHandler

**代码证据**：[`SecurityConfig.java`](backend/src/main/java/com/youlai/boot/config/SecurityConfig.java:19) — SecurityFilterChain定义

---

### B2: 策略模式在项目中的应用？

**考察目的**：设计模式实战

**深入回答**：
项目中有两处策略模式应用：

**1. 支付策略模式**（[`PaymentRouter.java`](backend/src/main/java/com/youlai/boot/portal/payment/PaymentRouter.java:1)）：
```java
// Spring自动注入所有PaymentProvider实现
@Autowired
private List<PaymentProvider> providers;

public PaymentProvider route(PaymentMethodEnum method) {
    return providers.stream()
        .filter(p -> p.supportedMethod() == method)
        .findFirst()
        .orElseThrow(() -> new BusinessException("不支持的支付方式"));
}
```
4种Provider：StripePaymentProvider(credit_card) / WechatPaymentProvider(wechat) / AlipayPaymentProvider(alipay) / BankTransferPaymentProvider(bank_transfer)

**2. 文件存储策略模式**（`FileService`接口）：
- MinioFileService — @ConditionalOnProperty("minio.enabled")
- AliyunFileService — @ConditionalOnProperty("oss.type", havingValue="aliyun")
- LocalFileService — 默认实现

**追问**：为什么支付用路由器模式而文件存储用条件装配？
→ 支付方式需要运行时动态切换（用户选择），文件存储是部署时确定的（环境配置），不需要运行时切换。

---

### B3: 双轨i18n策略是如何实现的？

**考察目的**：国际化方案设计

**深入回答**：
| 策略 | 适用表 | 实现方式 | 示例 |
|------|--------|----------|------|
| **行级i18n** | portal_*表 | country_type字段(ZH/EN)标识语言行 | portal_guest: country_type='ZH'为中文名行 |
| **列级i18n** | meeting_*表 | _en后缀字段存英文 | meeting_info: title(中文), title_en(英文) |

行级i18n的查询：WHERE country_type = 'ZH' 或 WHERE country_type = 'EN'
列级i18n的查询：前端根据语言选择title或title_en字段

**代码证据**：[`MeetingInfoServiceImpl.java`](backend/src/main/java/com/youlai/boot/portal/service/impl/MeetingInfoServiceImpl.java:1) — ITEM_TYPE_MAP中英文映射

---

## C. 核心模块层（业务深度）

### C1: 注册流程的6步向导是如何设计的？

**考察目的**：复杂业务流程设计

**深入回答**：
前端9步组件（Signup→Contact→Enroll→Skills→Expert→Survey→Consent→Confirm→Final），后端对应[`RegisterUserServiceImpl`](backend/src/main/java/com/youlai/boot/portal/service/impl/RegisterUserServiceImpl.java:1)的核心方法：

1. **saveRegisterUser()**：邮箱唯一校验(LOWER)→新建(设置paymentStatus=pending+注册日期+发送注册成功邮件+处理团队成员)→更新(删除旧团队成员→重新创建)
2. **auditRegisterUser()**：审核→验证抵扣码→生成QR码(QRCodeUtils)→发送审核通过邮件(含QR码base64图片)
3. **checkIn()**：校验用户→创建/更新UserCheckIn记录
4. **sendLoginEmailCode()**：校验邮箱已注册→Math.random生成6位码→Redis 5分钟

**已知问题**：
- CODE-008: team_id字段exist=false，团队成员关联功能暂不可用
- CODE-015: 验证码使用Math.random而非SecureRandom
- ARCH-007: 团队折扣仅前端计算

---

### C2: 论文投稿审稿的完整流程？

**考察目的**：业务链路理解

**深入回答**：
[`PaperSubmissionServiceImpl`](backend/src/main/java/com/youlai/boot/portal/service/impl/PaperSubmissionServiceImpl.java:1) 5大流程：

1. **CRUD**：savePaperSubmission / deletePaperSubmissions / getPaperSubmissionPage
2. **发送审稿邮件**：updatePaperSubmissionStatus() — 有附件模板替换{{{name1}}},{{name2}},{{论文稿件}},{{审批论文稿件}}} / 无附件正则替换<a>标签→createAndSend
3. **专家审核**：expertReview() — 更新reviewResult→全部审核完→通过率≥50%→生成8位随机抵扣码+paperStatus=2
4. **重发审稿邮件**：resendReviewEmail() — 基于已有emailRecord重新发送
5. **投稿者公开接口**：sendQueryCode/queryByEmail/getDetailByEmail/updateBySubmitter — 验证码鉴权(Redis 5分钟)+LOWER邮箱匹配+脱敏VO+修改限制(仅待审核)

**已知问题**：
- CODE-012: 8位随机抵扣码可能重复（应使用UUID或数据库唯一约束）
- SEC-012: expertReview接口无认证保护

---

### C3: 会议日程的三级结构设计？

**考察目的**：层次数据建模

**深入回答**：
三级结构：MeetingInfo(日期/会场) → MeetingSession(板块/时段) → MeetingAgendaItem(条目/讲者) + MeetingSessionParticipant(参与人)

[`MeetingInfoServiceImpl.java`](backend/src/main/java/com/youlai/boot/portal/service/impl/MeetingInfoServiceImpl.java:1) 核心方法：
- getAllSchedules()：查启用会议→查板块→查条目(withSpeaker)→查参与人→组装VO(日期格式M/D+中英文星期+中英文会场)
- createSchedule()：创建板块+条目+参与人，fillTypeFromItemType自动填充中英文类型名
- ITEM_TYPE_MAP：9种类型映射(OPENING/KEYNOTE/INVITED_SPEECH/GENERAL_SPEECH/DISCUSSION/BREAK/LUNCH/CLOSING/OTHER)

---

## D. 邮件系统层（最核心面试案例）

### D1: 邮件9态状态机是如何设计的？

**考察目的**：状态机设计、业务建模

**深入回答**：
[`MailStatusEnum`](backend/src/main/java/com/youlai/boot/support/mail/enums/MailStatusEnum.java) 定义9种状态：

```
PENDING → SCHEDULED → SENDING → SENT → DELIVERED
                                    ↓
                              BOUNCED_PERMANENT → (黑名单)
                              BOUNCED_TEMPORARY → RETRY → SENDING(循环)
                                    ↓ (超过max_retry=5)
                              FAILED
```

**双状态设计**：
- send_status：SMTP阶段9态（PENDING/SCHEDULED/SENDING/SUCCESS/FAILED/RETRYING/RETRY_SUCCESS/RETRY_FAILED/BOUNCE_DETECTED）
- delivery_status：最终投递阶段4态（PENDING/DELIVERED/BOUNCED/UNKNOWN）

**为什么需要双状态？**
→ SMTP发送成功(SENT)不等于收件人收到(DELIVERED)，中间可能退信(BOUNCED)。send_status跟踪发送动作，delivery_status跟踪最终投递结果。

**代码证据**：[`MailSendRecordServiceImpl.java`](backend/src/main/java/com/youlai/boot/support/mail/service/impl/MailSendRecordServiceImpl.java:1) — createAndSend()/retry()/handleBounceEnhanced()

---

### D2: 退信检测的5级匹配策略是什么？

**考察目的**：复杂业务规则设计、幂等性

**深入回答**：
[`MailSendRecordServiceImpl.handleBounceEnhanced()`](backend/src/main/java/com/youlai/boot/support/mail/service/impl/MailSendRecordServiceImpl.java:1) 实现5级匹配：

| 优先级 | 匹配策略 | 匹配字段 | 说明 |
|--------|----------|----------|------|
| 1 | MESSAGE_ID | bounceMessageId = originalMessageId | 最精确，RFC 2822 Message-ID唯一 |
| 2 | IN_REPLY_TO | bounceInReplyTo = originalMessageId | 退信回复原始邮件 |
| 3 | RECEIVER_TIME_WINDOW | 收件人+时间窗口(±24h) | 退信无Message-ID时的降级策略 |
| 4 | UNMATCHED | 无法匹配 | 记录unmatched退信，人工处理 |

**幂等保证**：同一bounceMessageId不会重复处理（先查mail_bounce_record是否已存在）

**事务设计**：TransactionTemplate分离事务内DB操作（写bounce_record+更新状态+黑名单+重试记录）和事务外IO操作（告警+发送）

---

### D3: BounceParser的4级分类逻辑？

**考察目的**：退信解析算法

**深入回答**：
[`BounceParser.java`](backend/src/main/java/com/youlai/boot/support/mail/parser/BounceParser.java:1) 4级分类优先级：

1. **DSN状态码**（RFC 3464）— 最高优先级
   - 5.1.1→USER_NOT_FOUND / 5.4.4→HOST_NOT_FOUND / 5.2.1→MAILBOX_DISABLED / 5.2.2→MAILBOX_FULL / 5.7.1→REJECTED
2. **DNS查询** — MX记录验证
   - DNS_PERMANENT(永久) / DNS_TEMPORARY(临时) / DNS_ERROR(默认临时)
3. **SMTP响应码** — 5xx永久/4xx临时
   - 550→7个子模式(USER_NOT_FOUND/MAILBOX_UNAVAILABLE/SPAM_REJECTED/RELAY_DENIED) / 552→MAILBOX_FULL / RATE_LIMIT / NETWORK_TEMPORARY
4. **通用匹配** — 默认分类
   - 554→REJECTED / OTHER

**收件人提取优先级**：DSN Final-Recipient > DSN Original-Recipient > Body Fallback

**15+正则模式**：覆盖550/552/554/421/450等SMTP响应码及常见退信正文模式

---

### D4: IMAP增量扫描如何实现？

**考察目的**：增量数据处理、容错设计

**深入回答**：
[`BounceEmailJob.java`](backend/src/main/java/com/youlai/boot/support/mail/job/BounceEmailJob.java:1) 增量扫描机制：

1. **UID checkpoint**：记录上次扫描的最大UID到mail_imap_checkpoint表
2. **UIDVALIDITY检测**：邮箱重建后UIDVALIDITY变化，触发全量扫描
3. **首次回溯**：首次运行回溯N天的邮件
4. **降级全量扫描**：checkpoint异常时降级为全量扫描

**163邮箱IMAP ID命令**：
```java
// 反射调用IMAPStore.id()解决"Unsafe Login"问题
// RFC 2971: IMAP ID extension
Method idMethod = store.getClass().getMethod("id", Map.class);
idMethod.invoke(store, idMap);
```
→ 需要在163邮箱后台配置授权IP白名单

**分布式锁**：Redisson分布式锁保证同一时间只有一个实例执行退信检测

---

### D5: 永久退信vs临时退信的处理差异？

**考察目的**：业务规则理解

**深入回答**：
| 维度 | 永久退信(PERMANENT) | 临时退信(TEMPORARY) |
|------|---------------------|---------------------|
| **原因** | 用户不存在/邮箱不可用/被拒收 | 邮箱满/速率限制/网络临时故障 |
| **DSN** | 5.1.1/5.4.4/5.2.1/5.7.1 | 4.x.x |
| **SMTP** | 550(部分)/554 | 421/450/552 |
| **处理** | 更新状态BOUNCED_PERMANENT | 更新状态BOUNCED_TEMPORARY |
| **黑名单** | 自动加入mail_bounce_blacklist | 不加黑名单 |
| **重试** | 不重试，永久禁止发送 | 自动创建重试记录，最多5次 |
| **后续** | createAndSend()/retry()发送前检查黑名单 | retry()执行重试发送 |

---

## E. 支付系统层

### E1: 四轨支付的策略模式如何实现？

**考察目的**：策略模式+工厂模式实战

**深入回答**：
[`PaymentRouter.java`](backend/src/main/java/com/youlai/boot/portal/payment/PaymentRouter.java:1) 路由器 + [`PaymentProvider.java`](backend/src/main/java/com/youlai/boot/portal/payment/PaymentProvider.java:1) 接口：

```java
public interface PaymentProvider {
    PaymentMethodEnum supportedMethod();  // 声明支持的支付方式
    PaymentProviderResponse createPayment(CreatePaymentRequest request);
    String handleCallback(Map<String, String> params);  // 回调处理
    void refund(PaymentOrder order);  // 退款
}
```

Spring自动注入所有PaymentProvider实现到List<PaymentProvider> providers，route()方法按PaymentMethodEnum匹配。

**回调差异**：
- Stripe：Webhook验签→payment_intent.succeeded→从metadata取registration_id
- 支付宝：RSA2手动验签→TRADE_SUCCESS→更新payment_order
- 微信：V3 NotificationParser.parse→TRADE_STATE.SUCCESS→更新payment_order
- 银行转账：无回调，confirmBankTransfer()人工确认

---

### E2: 支付回调的签名验证机制？

**考察目的**：支付安全

**深入回答**：
| 支付方式 | 签名算法 | 验证方式 | 代码位置 |
|----------|----------|----------|----------|
| Stripe | HMAC-SHA256 | Webhook.constructEvent(payload, sigHeader, webhookSecret) | [`StripePaymentProvider`](backend/src/main/java/com/youlai/boot/portal/payment/impl/StripePaymentProvider.java:1) |
| 支付宝 | RSA2 | AlipaySignature.rsaCheckV1(params, publicKey, charset) | [`AlipayPaymentProvider`](backend/src/main/java/com/youlai/boot/portal/payment/impl/AlipayPaymentProvider.java:1) |
| 微信 | RSA-SHA256 | NotificationParser.parse(request) 自动验签 | [`WechatPaymentProvider`](backend/src/main/java/com/youlai/boot/portal/payment/impl/WechatPaymentProvider.java:1) |

**已知问题PAY-002**：退款仅检查stripePaymentIntentId，未路由到其他Provider → 退款功能不完整

---

## F. 安全认证层

### F1: JWT/Redis-Token双模式认证如何切换？

**考察目的**：ConditionalOnProperty条件装配

**深入回答**：
通过`security.session.type`配置项切换：
- `jwt` → [`JwtTokenManager.java`](backend/src/main/java/com/youlai/boot/security/token/JwtTokenManager.java:1) @ConditionalOnProperty(havingValue="jwt")
- `redis-token` → [`RedisTokenManager.java`](backend/src/main/java/com/youlai/boot/security/token/RedisTokenManager.java:1) @ConditionalOnProperty(havingValue="redis-token")

| 维度 | JWT模式 | Redis-Token模式 |
|------|---------|-----------------|
| Token生成 | HS256签名JWT | UUID生成Token |
| Token验证 | 验签+过期+alg白名单+tokenVersion+jti黑名单 | Redis.hasKey检查 |
| 用户信息 | 从JWT Payload解析 | 从Redis读UserSession |
| 撤销机制 | jti黑名单(TTL=剩余有效期)+tokenVersion递增 | 删除Redis Key |
| 多设备控制 | 无（无状态） | 单/多设备登录控制 |
| 性能 | 无网络IO | 每次请求查Redis |

---

### F2: JWT的5层安全校验是什么？

**考察目的**：JWT安全深度

**深入回答**：
[`JwtTokenManager.validateToken()`](backend/src/main/java/com/youlai/boot/security/token/JwtTokenManager.java:1) 5层校验：

1. **签名验证**：HS256算法验签，防止Token被篡改
2. **过期检查**：exp字段检查，Token自然过期
3. **alg白名单**：仅允许HS256，防止alg:none攻击或算法降级攻击
4. **tokenVersion校验**：从Redis读用户tokenVersion，递增后所有旧Token失效（用户级会话失效）
5. **jti黑名单校验**：登出时将jti加入Redis黑名单，TTL=Token剩余有效期（单Token撤销）

**追问**：为什么不直接用Redis黑名单而需要tokenVersion？
→ jti黑名单适合单Token撤销（登出），tokenVersion适合批量撤销（修改密码/禁用用户），两者配合使用覆盖所有场景。

---

### F3: 4种AuthenticationProvider的设计？

**考察目的**：Spring Security扩展

**深入回答**：
[`SecurityConfig.java`](backend/src/main/java/com/youlai/boot/config/SecurityConfig.java:1) 配置4种Provider：

1. **DaoAuthenticationProvider**：用户名+密码登录，SysUserDetailsService.loadUserByUsername()
2. **SmsAuthenticationProvider**：手机+短信验证码登录，Redis验证码校验
3. **EmailAuthenticationProvider**：邮箱+验证码登录，Redis验证码校验
4. **WechatMiniAuthenticationProvider**：微信小程序登录，WxMaService.jsCode2Session()

AuthenticationManager = ProviderManager组合4种Provider，根据Authentication类型自动路由。

**SecurityFilterChain**：RateLimiterFilter(Redis限流) → CaptchaValidationFilter(图形验证码) → TokenAuthenticationFilter(JWT/Redis-Token解析) → Authorization(@PreAuthorize)

---

## G. 故障排查层（实战经验）

### G1: 163邮箱IMAP "Unsafe Login"如何解决？

**考察目的**：第三方服务集成问题排查

**深入回答**：
**问题**：使用JavaMail连接163邮箱IMAP时报"Unsafe Login"错误，无法读取收件箱。

**根因**：163邮箱安全策略要求IMAP客户端发送ID命令（RFC 2971）标识自身，否则拒绝访问。

**解决方案**：[`BounceEmailJob.sendImapId()`](backend/src/main/java/com/youlai/boot/support/mail/job/BounceEmailJob.java:1) 通过反射调用IMAPStore.id()方法：
```java
Method idMethod = store.getClass().getMethod("id", Map.class);
Map<String, String> idMap = Map.of("name", "MCLC-BounceDetector", "version", "1.0");
idMethod.invoke(store, idMap);
```

**前置条件**：需要在163邮箱后台配置授权IP白名单。

---

### G2: 退信检测的幂等性如何保证？

**考察目的**：分布式系统幂等设计

**深入回答**：
[`MailSendRecordServiceImpl.handleBounceEnhanced()`](backend/src/main/java/com/youlai/boot/support/mail/service/impl/MailSendRecordServiceImpl.java:1) 幂等保证：

1. **退信记录幂等**：处理前先查mail_bounce_record是否已存在相同bounceMessageId，存在则跳过
2. **状态更新幂等**：只有当前状态允许转换时才更新（如SENT→BOUNCED_PERMANENT）
3. **黑名单幂等**：INSERT IGNORE或先查后插
4. **重试记录幂等**：基于parentRecordId+retrySequence唯一标识

**分布式锁**：Redisson分布式锁保证同一时间只有一个BounceEmailJob实例执行

---

### G3: 邮件重试机制的设计？

**考察目的**：可靠性设计

**深入回答**：
[`MailSendRecordServiceImpl.retry()`](backend/src/main/java/com/youlai/boot/support/mail/service/impl/MailSendRecordServiceImpl.java:1) 重试机制：

1. **最大重试次数**：MAX_RETRY_LIMIT=5
2. **重试前检查**：黑名单检查→如果收件人在黑名单中则不重试
3. **状态流转**：FAILED/BOUNCED_TEMPORARY → RETRYING → SENDING → SUCCESS/FAILED
4. **重试记录**：创建新的mail_send_record，parentRecordId指向原始记录，retrySequence递增
5. **根记录追踪**：rootRecordId始终指向最初的发送记录，方便追溯完整重试链

**临时退信自动重试**：handleBounceEnhanced()判断临时退信后，自动创建重试记录

---

### G4: DOCX转PDF的线上修复案例？

**考察目的**：生产环境问题排查

**深入回答**：
**问题**：Docker容器中JODConverter连接LibreOffice失败，DOCX转PDF超时。

**根因**：容器重启后JODConverter残留锁文件(/tmp/.jodconverter_socket_*)，导致LibreOffice端口锁定。

**解决方案**：Dockerfile ENTRYPOINT中添加清理命令：
```dockerfile
ENTRYPOINT ["sh", "-c", "rm -rf /tmp/.jodconverter_socket_* && java $JAVA_OPTS -jar app.jar"]
```

**代码证据**：[`Dockerfile`](backend/Dockerfile:1)

**追问**：还有哪些可能的原因？
→ LibreOffice进程崩溃残留、端口耗尽、中文字体缺失（需安装fonts-wqy-zenhei）、JVM内存不足（默认512MB）

---

### G5: 已知的安全问题有哪些？

**考察目的**：安全意识

**深入回答**：
| 编号 | 严重程度 | 问题 | 位置 | 建议 |
|------|----------|------|------|------|
| SEC-001 | 🔴高 | SMTP密码明文存储 | application-dev.yml | Jasypt加密或环境变量 |
| SEC-002 | 🔴高 | JWT密钥明文存储 | application.yml | Vault/K8s Secret |
| SEC-003 | 🔴高 | IMAP密码明文存储 | application-dev.yml | 同SEC-001 |
| SEC-004 | 🟡中 | Stripe密钥明文存储 | application-dev.yml | 环境变量注入 |
| SEC-005 | 🟡中 | 阿里云SMS密钥明文 | application-dev.yml | 环境变量注入 |
| SEC-012 | 🟡中 | expertReview接口无认证 | PaperSubmissionController | 添加@PreAuthorize |
| CODE-015 | 🟡中 | Math.random生成验证码 | RegisterUserServiceImpl | 改用SecureRandom |

---

## 常见错误回答

| 问题 | ❌错误回答 | ✅正确回答 |
|------|-----------|-----------|
| 邮件状态 | "邮件只有发送成功和失败两种状态" | 9态状态机+双状态设计(send_status+delivery_status) |
| 退信检测 | "用POP3接收退信" | IMAP增量扫描+UID checkpoint+UIDVALIDITY检测 |
| 支付回调 | "所有支付都用Webhook" | Stripe用Webhook，支付宝/微信用同步回调+签名验证，银行转账无回调 |
| JWT撤销 | "JWT无法撤销" | jti黑名单+tokenVersion双重撤销机制 |
| 文件存储 | "只用MinIO" | 策略模式3实现：MinIO/AliyunOSS/本地，@ConditionalOnProperty切换 |
| 验证码 | "用UUID生成" | Math.random生成6位数字码（CODE-015：应改用SecureRandom） |

---

## 真实性标记说明

| 标记 | 含义 |
|------|------|
| ✅ 已实现 | 代码中已完成，可正常运行 |
| ⚠️ 已知问题 | 代码中存在，但有已知缺陷 |
| 🔧 计划改进 | 代码注释中标记的改进点 |
| 📋 待验证 | 需要实际运行验证 |