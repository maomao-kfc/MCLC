# MCLC 项目学习指南

> 4阶段渐进式学习路线，从入门到面试实战
> 基于实际代码深度阅读生成，所有路径有代码证据
> 更新时间：2026-10-10

---

## 阶段1: 入门（1-2天）— 建立全局认知

### 目标
理解项目做什么、用什么技术、怎么组织的。

### 1.1 项目概述
- 阅读 [`docs/00-project-overview.md`](docs/00-project-overview.md) — 项目背景和目标
- 阅读 [`docs/01-project-overview.md`](docs/01-project-overview.md) — 技术栈概览
- 阅读 [`docs/02-architecture.md`](docs/02-architecture.md) — 架构设计

### 1.2 技术栈确认
- 阅读 [`backend/pom.xml`](backend/pom.xml) — 确认所有依赖版本
- 重点依赖：Spring Boot 4.0.1 / MyBatis-Plus 3.5.15 / Redisson 4.1.0 / JavaMail / Spring Security

### 1.3 目录结构
- 阅读 [`docs/03-directory-structure.md`](docs/03-directory-structure.md)
- 后端8大顶层包：auth / system / portal / support / security / config / common / module
- 前端双轨：Admin(Vue3+TS) + Webpage(Vue3+JS)

### 1.4 数据库概览
- 阅读 [`docs/03-database-schema.md`](docs/03-database-schema.md)
- 45张表，按模块分组：system(10) / portal(22) / support(8) / module(5)
- 重点表：mail_send_record / mail_bounce_record / payment_order / register_user_information

### 验收标准
- [ ] 能用1分钟介绍MCLC项目是什么、做什么
- [ ] 能说出后端8大包的名称和职责
- [ ] 能说出5个以上核心依赖及版本

---

## 阶段2: 进阶（3-5天）— 核心模块代码阅读

### 目标
能独立定位代码，理解核心业务流程。

### 2.1 邮件系统（最高优先级，2天）

#### 阅读路线
| 步骤 | 文件 | 重点 | 时间 |
|------|------|------|------|
| 1 | [`MailStatusEnum.java`](backend/src/main/java/com/youlai/boot/support/mail/enums/MailStatusEnum.java) | 9态枚举 | 5min |
| 2 | [`DeliveryStatusEnum.java`](backend/src/main/java/com/youlai/boot/support/mail/enums/DeliveryStatusEnum.java) | 4态枚举 | 3min |
| 3 | [`BounceCategoryEnum.java`](backend/src/main/java/com/youlai/boot/support/mail/enums/BounceCategoryEnum.java) | 退信分类 | 3min |
| 4 | [`MailSendRecordServiceImpl.java`](backend/src/main/java/com/youlai/boot/support/mail/service/impl/MailSendRecordServiceImpl.java:1) | createAndSend/retry/handleBounceEnhanced | 60min |
| 5 | [`BounceParser.java`](backend/src/main/java/com/youlai/boot/support/mail/parser/BounceParser.java:1) | parse+15正则 | 45min |
| 6 | [`BounceEmailJob.java`](backend/src/main/java/com/youlai/boot/support/mail/job/BounceEmailJob.java:1) | IMAP增量扫描+退信识别 | 60min |

#### 关键概念
- 9态状态机：PENDING→SCHEDULED→SENDING→SUCCESS/FAILED→RETRYING→RETRY_FAILED + BOUNCE_DETECTED
- 双状态设计：send_status(SMTP阶段) + delivery_status(投递阶段)
- 5级匹配：MESSAGE_ID→IN_REPLY_TO→RECEIVER_TIME_WINDOW→UNMATCHED
- 4级分类：DSN→DNS→SMTP→正文
- 事务分离：TransactionTemplate分离DB操作和IO操作

### 2.2 支付系统（1天）

#### 阅读路线
| 步骤 | 文件 | 重点 | 时间 |
|------|------|------|------|
| 1 | [`PaymentRouter.java`](backend/src/main/java/com/youlai/boot/portal/payment/PaymentRouter.java) | 策略模式路由 | 10min |
| 2 | [`PaymentProvider.java`](backend/src/main/java/com/youlai/boot/portal/payment/PaymentProvider.java) | 接口定义 | 5min |
| 3 | [`StripePaymentProvider.java`](backend/src/main/java/com/youlai/boot/portal/payment/impl/StripePaymentProvider.java) | Stripe实现 | 20min |
| 4 | [`AlipayPaymentProvider.java`](backend/src/main/java/com/youlai/boot/portal/payment/impl/AlipayPaymentProvider.java) | 支付宝实现 | 30min |
| 5 | [`WechatPaymentProvider.java`](backend/src/main/java/com/youlai/boot/portal/payment/impl/WechatPaymentProvider.java) | 微信支付实现 | 30min |
| 6 | [`BankTransferPaymentProvider.java`](backend/src/main/java/com/youlai/boot/portal/payment/impl/BankTransferPaymentProvider.java) | 银行转账实现 | 15min |
| 7 | [`PaymentServiceImpl.java`](backend/src/main/java/com/youlai/boot/portal/service/impl/PaymentServiceImpl.java) | 业务编排 | 30min |

#### 关键概念
- 策略模式：PaymentRouter→PaymentProvider→4实现
- 支付宝双模式：扫码付(Precreate) + H5付(WAP)
- 微信V3：NativePayService + H5Service + NotificationParser
- 回调签名验证：Stripe Webhook签名 / 支付宝RSA2 / 微信V3证书

### 2.3 安全认证（1天）

#### 阅读路线
| 步骤 | 文件 | 重点 | 时间 |
|------|------|------|------|
| 1 | [`SecurityConfig.java`](backend/src/main/java/com/youlai/boot/config/SecurityConfig.java) | FilterChain+4Provider | 20min |
| 2 | [`JwtTokenManager.java`](backend/src/main/java/com/youlai/boot/security/token/JwtTokenManager.java) | JWT双令牌+5层校验 | 40min |
| 3 | [`RedisTokenManager.java`](backend/src/main/java/com/youlai/boot/security/token/RedisTokenManager.java) | Redis有状态认证 | 30min |
| 4 | [`TokenAuthenticationFilter.java`](backend/src/main/java/com/youlai/boot/security/filter/TokenAuthenticationFilter.java) | 过滤器链 | 10min |

#### 关键概念
- 双模式切换：ConditionalOnProperty(auth.token-type=jwt/redis)
- JWT 5层校验：验签→过期→alg白名单→tokenVersion→jti黑名单
- 4种Provider：Dao(用户名密码)/Sms(短信)/Email(邮箱)/WechatMini(微信)
- FilterChain：RateLimiter→CaptchaValidation→TokenAuthentication→Authorization

### 2.4 注册与论文（1天）

#### 阅读路线
| 步骤 | 文件 | 重点 | 时间 |
|------|------|------|------|
| 1 | [`RegisterUserServiceImpl.java`](backend/src/main/java/com/youlai/boot/portal/service/impl/RegisterUserServiceImpl.java) | 6步向导 | 40min |
| 2 | [`PaperSubmissionServiceImpl.java`](backend/src/main/java/com/youlai/boot/portal/service/impl/PaperSubmissionServiceImpl.java) | 投稿+审稿 | 50min |
| 3 | [`MeetingInfoServiceImpl.java`](backend/src/main/java/com/youlai/boot/portal/service/impl/MeetingInfoServiceImpl.java) | 日程三级结构 | 40min |

### 验收标准
- [ ] 能画出邮件9态状态机流转图
- [ ] 能说出支付策略模式的4个Provider及路由逻辑
- [ ] 能解释JWT 5层安全校验
- [ ] 能说出注册6步向导的步骤名称

---

## 阶段3: 深入（2-3天）— 专项突破

### 目标
能应对面试深度追问，讲清设计权衡。

### 3.1 邮件系统专项

- 精读 [`docs/email-case-study.md`](docs/email-case-study.md) — 完整面试案例
- 重点练习：
  - 9态状态机：为什么需要双状态？SMTP成功≠投递成功
  - 5级匹配：每级的匹配字段、优先级、降级策略
  - BounceParser：DSN/SMTP/DNS/正文4级分类优先级
  - IMAP增量扫描：UID checkpoint + UIDVALIDITY + 首次回溯 + 降级全量
  - 事务分离：TransactionTemplate分离DB和IO的设计考量
  - 163邮箱Unsafe Login：完整排查链路

### 3.2 故障案例专项

- 精读 [`docs/fault-cases.md`](docs/fault-cases.md) — 10个真实案例
- 重点练习：
  - 案例1: 163邮箱IMAP Unsafe Login — 第三方服务集成排查
  - 案例3: 退信误判 — SMTP响应码语义不唯一
  - 案例4: 支付回调幂等性 — 经典支付问题
  - 案例5: JWT alg降级攻击 — 安全最佳实践
  - 案例6: tokenVersion批量失效 — JWT局限性解决方案

### 3.3 已知问题专项

- 精读 [`docs/known-issues.md`](docs/known-issues.md) — 38个已知问题
- 分类记忆：
  - SEC类(安全)：密码/密钥明文存储、验证码Math.random
  - CODE类(代码)：大文件、字段注入、命名不规范
  - ARCH类(架构)：邮件状态机缺文档、团队折扣仅前端
  - PAY类(支付)：Stripe注入未用Bean、退款未路由
  - DB类(数据库)：SQL脚本基线不统一

### 验收标准
- [ ] 能完整讲述163邮箱Unsafe Login的排查过程
- [ ] 能解释永久退信vs临时退信的区分标准
- [ ] 能说出5个以上已知问题及建议修复方案
- [ ] 能解释TransactionTemplate分离事务内外的原因

---

## 阶段4: 实战（持续）— 面试模拟

### 目标
面试时能流畅、自信地讲述项目，应对追问。

### 4.1 项目介绍练习

使用 [`docs/resume-materials.md`](docs/resume-materials.md) 中的话术模板：

**30秒版本**：
> MCLC是一个学术会议管理系统，支持注册、支付、论文投稿、邮件通知等全流程。后端Spring Boot+MyBatis-Plus，前端Vue3双轨架构。我负责了邮件退信检测系统、四轨支付策略模式、JWT/Redis双模式认证等核心模块。

**1分钟技术深度版**：
> MCLC学术会议管理系统，后端Spring Boot 4 + MyBatis-Plus，前端Vue3双轨架构(Admin+Webpage)。我负责的邮件系统实现了9态状态机和IMAP退信自动检测，支持5级匹配和4级分类，区分永久/临时退信自动处理。支付系统用策略模式支持4种支付方式(Stripe/支付宝/微信/银行转账)，通过PaymentRouter路由。安全认证支持JWT/Redis-Token双模式切换，JWT有5层校验包括alg白名单和tokenVersion批量失效。

### 4.2 面试题库练习

使用 [`docs/interview-questions.md`](docs/interview-questions.md) — 7层26题：

| 层次 | 题数 | 重点 |
|------|------|------|
| A 项目概述 | 2 | 1分钟介绍、技术选型理由 |
| B 架构设计 | 3 | 分层架构、策略模式、双轨i18n |
| C 核心模块 | 3 | 注册向导、论文投稿、会议日程 |
| D 邮件系统 | 5 | 9态状态机、5级匹配、4级分类、IMAP、永久vs临时 |
| E 支付系统 | 2 | 策略模式、回调签名验证 |
| F 安全认证 | 3 | 双模式、5层校验、4种Provider |
| G 故障排查 | 5 | Unsafe Login、幂等性、重试、PDF修复、安全问题 |

### 4.3 模拟面试场景

**场景1: 被问"遇到过什么技术难题？"**
→ 选163邮箱Unsafe Login（案例1），展示完整排查链路

**场景2: 被问"如何保证系统可靠性？"**
→ 选邮件任务表模式+退信检测+重试机制+幂等性

**场景3: 被问"如何设计可扩展架构？"**
→ 选支付策略模式+文件存储策略模式+认证双模式

**场景4: 被问"有什么安全意识？"**
→ 选JWT alg白名单+tokenVersion+Math.random风险+团队折扣前端计算

**场景5: 被问"代码质量关注什么？"**
→ 选TransactionTemplate事务分离+BounceParser分类优先级+已知问题清单

### 4.4 持续提升

1. **代码走读**：每周重读一个核心文件，加深理解
2. **画图练习**：能默画出邮件9态状态机、支付策略模式UML、安全FilterChain
3. **追问准备**：每个回答准备3层追问（是什么→为什么→如果...怎么办）
4. **对比学习**：MCLC方案 vs 业界最佳实践的对比

---

## 附录: 文档索引

| 文档 | 用途 | 何时阅读 |
|------|------|----------|
| [`interview-questions.md`](docs/interview-questions.md) | 面试题库7层26题 | 阶段4面试前 |
| [`resume-materials.md`](docs/resume-materials.md) | 简历素材7节 | 更新简历时 |
| [`email-case-study.md`](docs/email-case-study.md) | 邮件系统面试案例 | 阶段3专项 |
| [`fault-cases.md`](docs/fault-cases.md) | 10个故障案例 | 阶段3专项 |
| [`known-issues.md`](docs/known-issues.md) | 38个已知问题 | 阶段3专项 |
| [`phase1-analysis-report.md`](docs/phase1-analysis-report.md) | Phase 1深度分析报告 | 阶段1全局认知 |
| [`codebase-map.md`](docs/codebase-map.md) | 代码地图 | 定位代码时 |
| [`api-endpoint-catalog.md`](docs/api-endpoint-catalog.md) | API端点目录 | 查找API时 |
| [`05-email-subsystem.md`](docs/05-email-subsystem.md) | 邮件子系统文档 | 阶段2邮件系统 |
| [`06-payment-subsystem.md`](docs/06-payment-subsystem.md) | 支付子系统文档 | 阶段2支付系统 |
| [`08-rbac-permission.md`](docs/08-rbac-permission.md) | RBAC权限文档 | 阶段2安全认证 |