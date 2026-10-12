# MCLC 项目简历素材

> 基于项目实际代码生成，所有描述均有代码证据支撑
> 更新时间：2026-10-10
> 使用原则：选择与目标岗位匹配的bullet，不编造不夸大

---

## 1. 项目描述（Project Description）

### 简短版（1-2句）
学术会议全生命周期管理平台，覆盖会议日程、参会注册、论文审稿、邮件系统、四轨支付等核心业务，基于Spring Boot 4.0.1+Vue3单体架构。

### 详细版（3-5句）
面向国际学术会议的全生命周期管理平台，服务会议组织者的注册、论文投稿审稿、日程编排、邮件通知、支付结算等全流程。后端Spring Boot 4.0.1单体架构，MyBatis-Plus ORM，Redis+Redisson缓存与分布式锁，Spring Security 4种认证Provider+JWT/Redis-Token双模式。前端双轨架构：管理端Vue3+TypeScript，参会者端Vue3+JavaScript，均Element Plus+Pinia。集成Stripe/微信/支付宝/银行转账四轨支付、IMAP退信检测、JODConverter+LibreOffice文档转换。

---

## 2. 技术栈Bullet（Technology Stack）

### 后端
- Java 17 + Spring Boot 4.0.1 + Spring Security 6.x
- MyBatis-Plus 3.5.15 + MySQL 8.0.45 + Druid连接池
- Redis + Redisson 4.1.0（Token存储/验证码/分布式锁/缓存）
- MapStruct 1.6.3（编译期对象映射）
- Caffeine 3.1.8（本地缓存）
- XXL-Job 3.2.0（定时任务调度）
- JODConverter 4.4.6 + LibreOffice（DOCX→PDF转换）
- OpenPDF（论文PDF生成）
- ZXing 3.5.3（签到二维码）
- Spring Boot Admin + Actuator（应用监控）

### 前端
- Vue 3 + TypeScript/JavaScript + Vite
- Element Plus + Pinia + Vue Router
- vue-i18n（中英日三语言）
- UnoCSS + Axios

### 第三方集成
- Stripe SDK（信用卡支付）
- 微信支付SDK V3（Native+H5）
- 支付宝SDK（扫码+H5+RSA2验签）
- 阿里云SMS（短信验证码）
- WxJava 4.6.0（微信小程序登录）
- MinIO / 阿里云OSS（文件存储策略模式）
- JavaMailSender + IMAP（邮件发送+退信检测）

---

## 3. 核心贡献Bullet（Core Contributions）

### 按技术领域分类

#### 邮件系统（最核心亮点）
- 设计并实现邮件9态状态机（PENDING→SENDING→SENT→DELIVERED/BOUNCED），双状态设计(send_status+delivery_status)精确跟踪SMTP发送阶段与最终投递结果
- 基于IMAP协议实现退信自动检测，采用UID增量扫描+UIDVALIDITY变化检测+首次回溯+降级全量扫描4层容错机制，解决163邮箱"Unsafe Login"问题（RFC 2971 IMAP ID命令反射调用）
- 实现BounceParser通用退信解析器，15+正则模式覆盖DSN(RFC 3464)/SMTP响应码/DNS查询/正文匹配4级分类优先级，区分永久退信(自动加黑名单)与临时退信(自动重试最多5次)
- 设计5级退信匹配策略（MESSAGE_ID→IN_REPLY_TO→RECEIVER_TIME_WINDOW→UNMATCHED），TransactionTemplate分离事务内DB操作与事务外IO操作，保证幂等性
- Redisson分布式锁保证退信检测任务单实例执行，IMAP检查点(mail_imap_checkpoint)持久化增量扫描进度

#### 支付系统
- 基于策略模式设计四轨支付架构（PaymentRouter+PaymentProvider接口），Spring自动注入4种Provider实现（Stripe/微信/支付宝/银行转账），运行时按PaymentMethodEnum动态路由
- 实现Stripe PaymentIntent API集成+Webhook签名验证(HMAC-SHA256)，支付宝扫码(Precreate)+H5(WAP)+RSA2手动验签，微信Native+H5+V3回调NotificationParser自动验签
- 设计支付订单持久化(payment_orders表)，统一回调处理流程：验签→更新订单状态→同步注册用户支付状态

#### 安全认证
- 实现JWT/Redis-Token双模式认证，通过@ConditionalOnProperty条件装配切换，JwtTokenManager 5层校验（验签+过期+alg白名单+tokenVersion+jti黑名单）
- 设计4种AuthenticationProvider（Dao用户名密码/Sms短信验证码/Email邮箱验证码/WechatMini微信小程序），ProviderManager自动路由
- SecurityFilterChain 3层过滤：RateLimiterFilter(Redis限流)→CaptchaValidationFilter(图形验证码)→TokenAuthenticationFilter(Token解析)
- JWT撤销双机制：jti黑名单(单Token撤销，TTL=剩余有效期)+tokenVersion递增(用户级批量失效)

#### 业务模块
- 设计会议日程三级层次结构（MeetingInfo→MeetingSession→MeetingAgendaItem+MeetingSessionParticipant），9种条目类型中英文自动映射
- 实现论文投稿审稿全流程：DOCX→PDF转换(JODConverter+LibreOffice)→审稿邮件模板替换→专家审核(通过率≥50%生成8位抵扣码)→投稿者验证码鉴权查询
- 注册6步向导式流程：邮箱唯一校验(LOWER)→审核→抵扣码验证→QR码生成(ZXing)→审核通过邮件(含QR码base64)→签到
- 双轨i18n策略：portal_*表行级i18n(country_type ZH/EN)+meeting_*表列级i18n(_en后缀字段)

#### 架构与工程
- Spring Boot 4.0.1单体架构，8大顶层包(auth/common/config/core/module/portal/security/support/system)，261个API端点，45张数据库表
- 文件存储策略模式3实现(MinIO/AliyunOSS/本地)，@ConditionalOnProperty部署时切换
- WebSocket(STOMP)实时推送：在线用户计数+字典变更通知+点对点消息，UserSessionRegistry双Map结构支持多设备
- Docker部署：Eclipse Temurin 17 JRE+LibreOffice+中文字体，ENTRYPOINT清理JODConverter锁文件防止容器重启后端口锁定

---

## 4. 项目亮点Bullet（Highlights for Resume）

### 适合后端开发岗位
1. **邮件9态状态机+退信自动检测**：设计SMTP发送9态+投递4态双状态模型，IMAP增量扫描+4级退信分类+5级匹配策略，永久退信自动黑名单+临时退信自动重试(最多5次)
2. **四轨支付策略模式**：PaymentRouter运行时路由+4种Provider实现，Stripe Webhook/支付宝RSA2/微信V3三种签名验证机制
3. **JWT 5层安全校验**：验签+过期+alg白名单防降级+tokenVersion批量失效+jti黑名单单Token撤销

### 适合全栈岗位
1. **双前端架构**：管理端Vue3+TypeScript+Element Plus，参会者端Vue3+JavaScript+Element Plus，vue-i18n中英日三语言
2. **注册6步向导**：前端9步组件+Pinia状态管理+后端分步校验+QR码生成+审核邮件

### 适合架构设计岗位
1. **策略模式双应用**：支付运行时路由(PaymentRouter)+文件存储部署时切换(@ConditionalOnProperty)
2. **双模式认证**：JWT无状态/Redis-Token有状态，@ConditionalOnProperty切换，同一TokenManager接口
3. **事务边界设计**：TransactionTemplate分离事务内DB操作与事务外IO操作(退信处理)

---

## 5. 项目数据（Quantifiable Metrics）

| 指标 | 数值 | 代码证据 |
|------|------|----------|
| 后端Java文件 | 250+ | 8大顶层包 |
| API端点 | 261 | 32+ Controller |
| 数据库表 | 45 | MySQL 8.0.45 |
| Spring Bean | 113 | @Service+@Component+@Configuration |
| 邮件状态 | 9态+4态 | MailStatusEnum+DeliveryStatusEnum |
| 退信正则模式 | 15+ | BounceParser.java |
| 支付方式 | 4轨 | PaymentRouter+4 Provider |
| 认证Provider | 4种 | SecurityConfig |
| 前端页面 | 54+ | Admin 40+ / Webpage 14+ |
| 国际化语言 | 3种 | 中/英/日 |

---

## 6. 已知问题与改进（Known Issues & Improvements）

### 可在面试中展示"发现问题+分析原因+提出方案"能力

| 编号 | 问题 | 分析 | 建议方案 |
|------|------|------|----------|
| SEC-001 | SMTP密码明文 | 配置文件泄露风险 | Jasypt加密或环境变量注入 |
| CODE-015 | Math.random验证码 | 安全随机性不足 | 改用SecureRandom |
| PAY-002 | 退款仅Stripe | 其他支付方式无法退款 | refund()应使用PaymentRouter路由 |
| CODE-012 | 8位随机抵扣码 | 可能重复 | UUID或数据库唯一约束 |
| SEC-012 | expertReview无认证 | 任何人可调用审核接口 | 添加@PreAuthorize |
| ARCH-007 | 团队折扣仅前端 | 后端未校验折扣逻辑 | 后端增加折扣计算校验 |
| CODE-001 | BounceEmailJob 46KB | 职责过多 | 拆分为BounceDetector+BounceClassifier+BounceActionHandler+ImapScanner |

---

## 7. 面试话术模板（Interview Scripts）

### 30秒项目介绍
"我参与开发的MCLC是一个学术会议管理系统，核心亮点是邮件系统的9态状态机和IMAP退信自动检测——设计了4级退信分类和5级匹配策略，能区分永久退信自动加黑名单和临时退信自动重试。支付系统用了策略模式支持Stripe、微信、支付宝、银行转账四种方式。认证方面实现了JWT和Redis-Token双模式，JWT有5层安全校验包括alg白名单防降级攻击和tokenVersion批量失效机制。"

### 1分钟技术深度
"邮件系统是我负责的最复杂的模块。发送阶段有9个状态，投递阶段有4个状态，双状态设计是因为SMTP发送成功不等于收件人收到——中间可能退信。退信检测用IMAP增量扫描，通过UID checkpoint记录上次扫描位置，UIDVALIDITY检测邮箱重建。退信解析器BounceParser有4级分类优先级：DSN状态码最高，然后是DNS查询、SMTP响应码、正文正则匹配。匹配策略有5级：Message-ID最精确，然后是In-Reply-To、收件人+时间窗口，最后是未匹配。永久退信自动加黑名单禁止重发，临时退信自动创建重试记录最多5次。事务设计上用TransactionTemplate把DB操作放在事务内，IO操作放在事务外，保证一致性又不阻塞事务。"

### 被问"遇到什么难题"
"163邮箱IMAP连接时报'Unsafe Login'错误，查了RFC 2971发现163要求客户端发送IMAP ID命令标识自身。但JavaMail的IMAPStore.id()方法不在公开API中，最后通过反射调用解决了。另外Docker容器重启后JODConverter连接LibreOffice失败，排查发现是/tmp下残留锁文件导致端口锁定，在Dockerfile ENTRYPOINT里加了rm -rf清理命令解决。"