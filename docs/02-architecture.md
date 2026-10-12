# MCLC 系统架构文档

## 1. 分层架构总览

```
┌────────────────────────────────────────────────────────────────────────┐
│                         Frontend (Vue3)                                │
│  ┌──────────┐  ┌──────────┐  ┌──────────┐  ┌──────────┐              │
│  │  Views    │  │  Stores  │  │   APIs   │  │  Utils   │              │
│  │ (Pages)  │→│ (Pinia)  │→│ (Axios)  │→│ (Helper) │              │
│  └──────────┘  └──────────┘  └──────────┘  └──────────┘              │
└──────────────────────────┬─────────────────────────────────────────────┘
                           │ HTTP/HTTPS (JSON)
┌──────────────────────────▼─────────────────────────────────────────────┐
│                      Backend (Spring Boot)                             │
│                                                                        │
│  ┌─────────────────────────────────────────────────────────────────┐  │
│  │                    Security Filter Chain                         │  │
│  │  RateLimiterFilter → CaptchaValidationFilter → TokenAuthFilter  │  │
│  └─────────────────────────────────────────────────────────────────┘  │
│                                    │                                   │
│  ┌─────────────────────────────────▼───────────────────────────────┐  │
│  │                    Controller Layer                              │  │
│  │  @RestController + @RequestMapping("/api/v1/{module}")          │  │
│  │  权限: @PreAuthorize + @Log + @RepeatSubmit                     │  │
│  └─────────────────────────────────┬───────────────────────────────┘  │
│                                    │                                   │
│  ┌─────────────────────────────────▼───────────────────────────────┐  │
│  │                    Service Layer                                 │  │
│  │  业务逻辑 + 事务管理 + 状态机 + 策略路由                         │  │
│  │  转换: MapStruct (Entity↔Form↔VO↔Query)                        │  │
│  └─────────────────────────────────┬───────────────────────────────┘  │
│                                    │                                   │
│  ┌─────────────────────────────────▼───────────────────────────────┐  │
│  │                    Mapper Layer (MyBatis-Plus)                   │  │
│  │  BaseMapper<Entity> + 自定义XML (mapper/*.xml)                  │  │
│  │  数据权限: @DataPermission → MyDataPermissionHandler            │  │
│  └─────────────────────────────────┬───────────────────────────────┘  │
│                                    │                                   │
│  ┌─────────────────────────────────▼───────────────────────────────┐  │
│  │                    Database (MySQL 8.0)                          │  │
│  │  meeting_admin (45 tables, InnoDB + MyISAM for sys_log)         │  │
│  └─────────────────────────────────────────────────────────────────┘  │
│                                                                        │
│  ┌─────────────────────────── 横切关注点 ──────────────────────────┐  │
│  │  AOP: LogAspect(操作日志) + RepeatSubmitAspect(防重)            │  │
│  │  过滤器: RateLimiterFilter(Redis限流)                           │  │
│  │  异常: GlobalExceptionHandler → Result.failed()                 │  │
│  │  WebSocket: STOMP /ws → /topic, /queue                         │  │
│  └─────────────────────────────────────────────────────────────────┘  │
└────────────────────────────────────────────────────────────────────────┘
```

## 2. 请求流转全链路

以"论文投稿分页查询"为例：

```
用户操作 → 前端Vue页面
  ↓
store/portal/paperSubmission.ts → dispatch('getPaperSubmissionPage')
  ↓
api/portal/paperSubmission.ts → GET /api/v1/paperSubmission?pageNum=1&pageSize=10
  ↓ (Axios拦截器自动附加Authorization: Bearer {token})
  ↓
[Security Filter Chain]
  RateLimiterFilter → 检查Redis限流计数
  CaptchaValidationFilter → 跳过(非登录接口)
  TokenAuthenticationFilter → 解析JWT, 设置SecurityContext
  ↓
PaperSubmissionController.getPaperSubmissionPage(PaperSubmissionQuery)
  @Log(value="论文投稿分页列表", module=LogModuleEnum.PAPER_SUBMISSION)
  @PreAuthorize(...)  → 检查按钮权限
  ↓
PaperSubmissionServiceImpl.getPaperSubmissionPage(queryParams)
  → PaperSubmissionConverter.query2Entity(queryParams)  // MapStruct
  → PaperSubmissionMapper.selectPage(page, wrapper)      // MyBatis-Plus
  → [MyDataPermissionHandler] → 注入数据权限SQL条件
  → PaperSubmissionConverter.entity2PageVO(entity)       // MapStruct
  ↓
PageResult<PaperSubmissionPageVO> → Result.success(data)
  ↓
[LogAspect] → 异步保存sys_log
  ↓
JSON响应 → 前端Axios响应拦截器 → store更新 → 页面渲染
```

## 3. 安全架构

### 3.1 认证方式（4种Provider）

| Provider | 类 | 认证方式 | 适用场景 |
|----------|-----|---------|---------|
| DaoAuthenticationProvider | Spring内置 | 用户名+密码 | 管理后台登录 |
| SmsAuthenticationProvider | 自定义 | 手机号+短信验证码 | 移动端/快捷登录 |
| EmailAuthenticationProvider | 自定义 | 邮箱+验证码 | 注册用户登录 |
| WechatMiniAuthenticationProvider | 自定义 | 微信小程序code | 微信小程序 |

### 3.2 Security Filter Chain

```
HTTP Request
  → RateLimiterFilter (Redis令牌桶限流)
  → CaptchaValidationFilter (验证码校验，仅/login接口)
  → TokenAuthenticationFilter (JWT解析，设置SecurityContext)
  → Spring Security Authorization (URL权限 + @PreAuthorize方法权限)
  → Controller
```

### 3.3 Token管理

- **Access Token**: JWT，存储用户ID、角色、权限，Redis缓存
- **Refresh Token**: 长有效期，用于刷新Access Token
- **TokenManager**: 统一管理Token的生成、验证、刷新、销毁

### 3.4 数据权限

通过 `@DataPermission` 注解 + `MyDataPermissionHandler` 实现：

| 数据范围 | SQL条件 | 说明 |
|---------|---------|------|
| ALL (1) | 无过滤 | 查看所有数据 |
| DEPT_AND_CHILD (2) | dept_id IN (本部门+子部门ID) | 部门及子部门 |
| DEPT_ONLY (3) | dept_id = 本部门ID | 仅本部门 |
| SELF (4) | create_by = 当前用户ID | 仅本人 |
| CUSTOM (5) | dept_id IN (自定义部门ID列表) | 自定义部门 |

**多角色合并策略**: 如果任一角色拥有ALL权限，则跳过过滤；否则用OR连接各角色条件（并集）。

## 4. 核心设计模式

### 4.1 策略模式 — 支付网关

```
PaymentProvider (接口)
  ├── createPayment(request)   → 创建支付
  ├── queryPayment(orderNo)    → 查询支付
  ├── handleCallback(params)   → 处理回调
  ├── refund(orderNo)          → 退款
  └── supportedMethod()        → 返回支持的支付方式

PaymentRouter (路由器)
  ├── route(paymentMethod)     → 按支付方式路由到Provider
  └── routeByCallback(type)    → 按回调类型路由到Provider

实现类:
  ├── StripePaymentProvider    → credit_card (Stripe API)
  ├── WechatPaymentProvider   → wechat (微信支付V3 API)
  ├── AlipayPaymentProvider   → alipay (支付宝API)
  └── BankTransferProvider    → bank_transfer (线下转账)
```

### 4.2 观察者模式 — WebSocket实时推送

```
WebSocketConfig (配置)
  ├── 端点: /ws (SockJS fallback)
  ├── 消息代理: /topic (广播), /queue (点对点)
  ├── 应用前缀: /app
  └── 用户前缀: /user

WebSocketService (服务)
  ├── sendMessageToUser(userId, destination, payload)  → 发送给指定用户
  ├── sendMessageToAll(destination, payload)           → 广播给所有用户
  └── sendNoticeToUser(userId, notice)                 → 发送通知

WebSocketPublisher (发布者)
  └── 在业务事件(如: 新通知、支付成功)时调用WebSocketService推送

UserSessionRegistry (会话注册)
  └── 管理在线用户的WebSocket Session映射

WebSocketCleanupJob (定时任务)
  └── 清理过期/断开的Session
```

### 4.3 模板方法模式 — 代码生成

```
CodegenServiceImpl
  ├── generateCode(tableId) → 主入口
  │     ├── 读取gen_table配置
  │     ├── 读取gen_table_column列配置
  │     ├── 构建Velocity上下文
  │     └── 遍历模板生成代码
  │
  └── Velocity模板 (backend/src/main/resources/templates/codegen/)
       ├── 后端模板 (10个):
       │     Entity.java.vm, Mapper.java.vm, Mapper.xml.vm,
       │     Service.java.vm, ServiceImpl.java.vm, Controller.java.vm,
       │     Form.java.vm, Query.java.vm, VO.java.vm, Converter.java.vm
       │
       └── 前端模板 (7个):
             api.ts.vm, index.vue.vm, components/Form.vue.vm,
             components/Dialog.vue.vm, hooks.ts.vm, types.ts.vm, router.ts.vm
```

### 4.4 状态机模式 — 邮件发送

详见 [docs/05-email-subsystem.md](05-email-subsystem.md)

```
MailSendStatus (9状态):
  PENDING(0) → SUBMITTED(1) → [SUCCESS] / FAIL(2) / BOUNCED(5)
  FAIL(2) → WAIT_RETRY(6) → RETRYING(3) → RETRY_SUCCESS(7) / RETRY_FAIL(8)
  BOUNCED(5) → WAIT_RETRY(6) → RETRYING(3) → RETRY_SUCCESS(7) / RETRY_FAIL(8)
  CANCELLED(4) ← 任何非终态
```

### 4.5 MapStruct转换模式

所有模块统一使用MapStruct进行对象转换：

```
Entity ↔ Form   (入库/更新)
Entity ↔ VO     (出库展示)
Entity ↔ Query  (查询条件)

转换器位置:
  portal/converter/   → 13个转换器 (Portal*Converter)
  system/converter/   → 10个转换器 (Sys*Converter)
  module/codegen/     → 1个转换器 (CodegenConverter)
```

## 5. 包结构规范

```
com.youlai.boot/
├── auth/                    # 认证模块
│   ├── controller/          # AuthController, WechatMiniappAuthController
│   ├── service/             # AuthService, AuthServiceImpl
│   └── model/               # LoginRequest, CaptchaVO, AuthenticationToken
│
├── common/                  # 公共模块
│   ├── annotation/          # @DataPermission, @Log, @RepeatSubmit, @ValidField
│   ├── base/                # BaseEntity (id, createTime, updateTime)
│   ├── constant/            # SystemConstants, SecurityConstants
│   ├── enums/               # LogModuleEnum, DataScopeEnum, GenderEnum
│   └── util/                # IPUtils, FileUtils
│
├── config/                  # 配置类 (23个)
│   ├── SecurityConfig       # Spring Security过滤链 + 4种认证Provider
│   ├── WebSocketConfig      # STOMP WebSocket配置
│   ├── MyDataPermissionHandler  # 数据权限SQL注入
│   ├── MybatisPlusConfig    # 分页插件 + 数据权限拦截器
│   ├── RedisConfig          # Redis序列化配置
│   ├── SwaggerConfig        # Knife4j API文档
│   ├── CorsConfig           # 跨域配置
│   ├── WechatPayConfig      # 微信支付证书配置
│   └── property/            # ImapProperties, SecurityProperties (11个)
│
├── core/                    # 核心模块
│   ├── aspect/              # LogAspect, RepeatSubmitAspect
│   ├── exception/           # GlobalExceptionHandler, BizException
│   ├── filter/              # RateLimiterFilter
│   └── web/                 # Result<T>, PageResult<T>, ResultCode
│
├── module/                  # 功能模块
│   ├── codegen/             # 代码生成
│   └── file/                # 文件上传 (Aliyun/Local/Minio)
│
├── portal/                  # 门户业务（核心）
│   ├── controller/          # 21个Controller
│   ├── service/             # 23个Service + 实现
│   ├── mapper/              # 24个Mapper
│   ├── model/               # Entity(24) + Form(23) + Query(12) + VO(21)
│   ├── converter/           # 13个MapStruct转换器
│   ├── enums/               # PaymentMethodEnum, PaymentStatusEnum
│   └── payment/             # PaymentProvider + 4实现 + PaymentRouter
│
├── support/                 # 支撑服务
│   ├── mail/                # 邮件子系统 (5 Service, 1 Job, 1 Parser)
│   ├── sms/                 # 短信服务 (AliyunSmsService)
│   └── websocket/           # WebSocket (Service, Publisher, Registry, Job)
│
├── system/                  # 系统管理
│   ├── controller/          # 11个Controller
│   ├── service/             # 16个Service + 实现
│   ├── mapper/              # 16个Mapper
│   ├── model/               # Entity(16) + Form(15) + Query(11) + VO(18) + BO(6)
│   ├── converter/           # 10个MapStruct转换器
│   ├── handler/             # DataPermissionHandler
│   └── listener/            # UserImportListener (EasyExcel导入)
│
└── security/                # 安全模块 (被config引用)
    ├── filter/              # TokenAuthenticationFilter, CaptchaValidationFilter
    ├── handler/             # MyAccessDeniedHandler, MyAuthenticationEntryPoint
    ├── provider/            # SmsAuthenticationProvider, EmailAuthenticationProvider
    ├── token/               # TokenManager (JWT生成/验证/刷新)
    ├── service/             # SysUserDetailsService
    ├── util/                # SecurityUtils (获取当前用户)
    └── model/               # SysUserDetails, AuthenticationToken, RoleDataScope
```

## 6. 包间依赖关系

### 6.1 依赖矩阵（源码验证）

基于实际import语句分析，包间依赖关系如下（→表示"依赖"）：

```
                    ┌─────────┐
                    │ common  │ ← 被所有包依赖（注解/基类/枚举/工具）
                    └─────────┘
                         ↑
┌──────┐  ┌───────┐  ┌───┴───┐  ┌────────┐  ┌───────┐  ┌────────┐
│ core │→│common │  │config │→│security│  │ auth  │  │module │
└──┬───┘  └───────┘  └───┬───┘  └───┬────┘  └──┬────┘  └───────┘
   │                    │          │           │
   │  core→system(LogService)    │     auth→system(UserService)
   │  core→security(SecurityUtils)     auth→security(TokenManager)
   │                    │          │     auth→support.sms(SmsService)
   │              config→security(4个类)  auth→config(CaptchaProperties)
   │              config→portal(RegisterUserService)
   │              config→system(UserService,ConfigService)
   │              config→support.websocket(WebSocketService)
   ↓                    ↓          ↓           ↓
┌──────────────────────────────────────────────────────────┐
│                     system（系统管理）                      │
│  → common(注解/基类/枚举/工具)                              │
│  → support.mail(MailService)   UserService发送邮件         │
│  → support.sms(SmsService)     UserService发送短信         │
│  → support.websocket(WebSocketService)  NoticeService推送  │
│  → security(无直接依赖，通过Spring Security间接使用)          │
└──────────────────────────────────────────────────────────┘
                         ↓
┌──────────────────────────────────────────────────────────┐
│                     portal（门户业务）                      │
│  → common(注解/基类/枚举/常量/工具)                          │
│  → system.service(EmailTemplateService)  论文/注册发邮件模板  │
│  → system.model(UserBO,DictItem,UserPageVO)  Contact转换   │
│  → support.mail(MailService,MailSendRecordService)        │
│  → support.mail.enums(MailBusinessType)  论文/注册邮件业务类型│
│  → support.mail.model(MailSendRecord)  邮件记录实体         │
└──────────────────────────────────────────────────────────┘
                         ↓
┌──────────────────────────────────────────────────────────┐
│                   support（支撑服务）                       │
│  → common(基类/枚举)  邮件/短信/WebSocket                   │
│  不依赖 system/portal/auth/config（完全独立）                │
└──────────────────────────────────────────────────────────┘
```

### 6.2 关键依赖说明

| 依赖路径 | 用途 | 涉及文件 |
|---------|------|---------|
| portal → system.service | 论文/注册使用EmailTemplateService | PaperSubmissionServiceImpl, RegisterUserServiceImpl |
| portal → system.model | ContactConverter使用UserBO/DictItem | ContactConverter |
| portal → support.mail | 论文审稿邮件/注册确认邮件 | PaperSubmissionServiceImpl, RegisterUserServiceImpl |
| system → support.mail | 用户管理发送邮件 | UserServiceImpl |
| system → support.sms | 用户管理发送短信 | UserServiceImpl |
| system → support.websocket | 通知推送 | NoticeServiceImpl, DictController |
| auth → system.service | 认证查询用户/社交账号 | AuthServiceImpl, WechatMiniappAuthServiceImpl |
| auth → support.sms | 短信登录发送验证码 | AuthServiceImpl |
| auth → security | Token管理/用户详情 | AuthServiceImpl, WechatMiniappAuthServiceImpl |
| config → security | SecurityConfig组装过滤链/Provider | SecurityConfig, WebSocketConfig |
| config → portal | SecurityConfig注入RegisterUserService | SecurityConfig |
| config → system | SecurityConfig注入UserService/ConfigService | SecurityConfig |
| core → system | LogAspect保存日志/RateLimiter读配置 | LogAspect, RateLimiterFilter |
| core → security | LogAspect获取当前用户 | LogAspect |

### 6.3 依赖规则与风险

- **support包完全独立**：不依赖system/portal/auth，可被任何包安全引用
- **portal不依赖auth**：门户业务不直接依赖认证模块，通过Spring Security间接使用
- **system不依赖portal**：系统管理不依赖门户业务，单向依赖
- **⚠ 循环风险**：config→portal(RegisterUserService) + portal不依赖config，无循环
- **⚠ 跨层依赖**：portal直接依赖system.service(EmailTemplateService)而非通过接口解耦

## 7. 核心数据流图

### 7.1 用户注册数据流

```
Webpage前端                    后端                              数据库
───────────                   ────                            ──────
注册6步向导
  │
  ├─Step1:Enroll ─────────→ RegisterUserController.enroll()
  │                         → RegisterUserServiceImpl.enroll()
  │                           → register_user INSERT(状态=ENROLLED)
  │
  ├─Step2:Consent ────────→ PUT /register/consent
  │                         → 更新consent_status
  │
  ├─Step3:Contact ────────→ PUT /register/contact
  │                         → 更新contact_email/mobile
  │
  ├─Step4:Signup ─────────→ POST /register/signup
  │                         → 计算费用+折扣
  │                         → register_user UPDATE(状态=SIGNED_UP)
  │
  ├─Step5:Confirm ────────→ POST /register/confirm
  │                         → 生成QR码→上传文件服务→回写URL
  │                         → register_user UPDATE(状态=CONFIRMED)
  │
  └─Step6:Final ──────────→ GET /register/final
                            → 查询最终状态+支付信息
                            → 返回FinalVO
```

### 7.2 论文投稿审稿数据流

```
管理端Admin                     后端                              外部系统
──────────                     ────                            ────────
1.新增投稿 ──────────→ POST /paperSubmission
                       → PaperSubmissionServiceImpl.save()
                       → paper_submission INSERT

2.发送审稿邮件 ───────→ PUT /{id}/review
                       → updatePaperSubmissionStatus()
                       → 读取paper_submission_email(专家列表)
                       → 构建邮件内容(有附件→模板/无附件→HTML表格)
                       → MailService.send() ──────────→ SMTP服务器
                       → paper_submission_email UPDATE(send_status)

3.专家审核 ──────────→ POST /expertReview (公开接口)
                       → expertReview()
                       → 更新review_result(1=通过/2=拒绝)
                       → 全部审核完→计算通过率
                       → ≥50%通过→paperStatus=2+生成discount_code
                       → <50%通过→paperStatus=3

4.投稿者查询 ────────→ POST /query/sendCode (Webpage)
                       → sendQueryCode()
                       → 生成6位验证码→Redis(TTL=5min)
                       → 发送验证码邮件 ──────────→ SMTP服务器

5.投稿者验证查询 ────→ GET /query/byEmail?email=xxx&code=xxx
                       → queryByEmail()
                       → 校验验证码→LOWER邮箱匹配
                       → 返回脱敏PaperSubmissionQueryVO
```

### 7.3 支付数据流

```
Webpage/Admin                  后端                              外部系统
──────────────                 ────                            ────────
1.创建支付 ──────────→ POST /payment/create
                       → PaymentServiceImpl.createPayment()
                       → PaymentRouter.route(method) ──→ 对应Provider
                       → Provider.createPayment() ──────→ Stripe/微信/支付宝API
                       → payment_order INSERT(状态=PENDING)
                       ← 返回支付参数(client_secret/prepay_id)

2.支付回调 ──────────→ POST /payment/callback/{type} (公开接口)
                       → PaymentRouter.routeByCallback(type)
                       → Provider.handleCallback() ──────→ 验签+解析
                       → PaymentServiceImpl.handleCallbackResult()
                       → payment_order UPDATE(状态=PAID)
                       → register_user UPDATE(支付状态)
                       → WebSocket推送支付成功通知

3.退款 ──────────────→ POST /payment/refund
                       → PaymentServiceImpl.refund()
                       → 检查stripePaymentIntentId
                       → StripePaymentProvider.refund() ──→ Stripe API
                       → payment_order UPDATE(退款状态)
```

### 7.4 邮件发送数据流

```
业务Service                    MailService                      外部系统
───────────                    ──────────                      ────────
调用send() ──────────→ MailServiceImpl.send()
                       → 构建MimeMessage
                       → mail_send_record INSERT(状态=SUBMITTED)
                       → JavaMailSender.send() ──────────→ SMTP服务器
                       → 成功: UPDATE(状态=SUCCESS)
                       → 失败: UPDATE(状态=FAIL)

退信检测(定时):
  BounceEmailJob ─────→ MailServiceImpl.checkBouncedEmails()
                       → IMAP连接退信邮箱 ──────────────→ IMAP服务器
                       → 解析RFC3464 DSN
                       → 匹配Message-ID
                       → mail_send_record UPDATE(状态=BOUNCED)
                       → autoResend=true → 重新发送
```

## 8. API规范

### 8.1 URL规范

```
基础路径: /api/v1/{module}

认证:     /api/v1/auth/**
系统:     /api/v1/system/**
门户:     /api/v1/portal/**
论文:     /api/v1/paperSubmission/**
支付:     /api/v1/payment/**
邮件:     /api/v1/mail/**
文件:     /api/v1/file/**
代码生成: /api/v1/codegen/**
```

### 8.2 统一响应结构

```json
{
  "code": "00000",        // ResultCode (00000=成功, 其他=错误码)
  "msg": "操作成功",       // 消息
  "data": {}              // 业务数据 (泛型T)
}
```

分页响应:
```json
{
  "code": "00000",
  "msg": "操作成功",
  "data": {
    "records": [],        // 数据列表
    "total": 100,         // 总记录数
    "pageNum": 1,         // 当前页
    "pageSize": 10        // 每页大小
  }
}
```

### 8.3 认证Header

```
Authorization: Bearer {accessToken}
```

### 8.4 公开接口（无需认证）

配置在 `SecurityProperties.ignoreUrls`:
- `/api/v1/auth/captcha` — 验证码
- `/api/v1/auth/login` — 登录
- `/api/v1/auth/login/sms` — 短信登录
- `/api/v1/auth/sms/code` — 发送短信验证码
- `/api/v1/paperSubmission/query/**` — 投稿者查询接口
- `/api/v1/register/**` — 注册接口
- `/api/v1/payment/callback/**` — 支付回调
- `/api/v1/paperSubmission/expertReview` — 专家审核
- `/ws/**` — WebSocket端点

## 9. 前端架构

### 9.1 Admin前端 (Vue3 + TypeScript)

```
frontend/src/
├── api/                    # API请求层
│   ├── auth.ts             # 认证API
│   ├── portal/             # 门户业务API (17个文件)
│   └── system/             # 系统管理API (13个文件)
│
├── views/                  # 页面组件
│   ├── login/              # 登录页
│   ├── dashboard/          # 首页仪表盘
│   ├── portal/             # 门户管理页面 (10个子目录)
│   ├── system/             # 系统管理页面 (8个子目录)
│   ├── paperSubmission/    # 论文投稿管理
│   ├── mailRecord/         # 邮件记录
│   ├── payMoney/           # 支付管理
│   └── ...                 # 其他页面
│
├── stores/                 # Pinia状态管理
│   ├── auth.ts             # 认证状态
│   ├── permission.ts       # 权限/菜单状态
│   └── app.ts              # 应用全局状态
│
├── router/                 # 路由配置
│   └── index.ts            # 动态路由 (基于后端菜单权限)
│
├── composables/            # 组合式函数
├── utils/                  # 工具函数
├── components/             # 公共组件
└── locales/                # 国际化 (中/英)
```

### 9.2 Webpage前端 (Vue3 + JavaScript)

```
webpage/src/
├── api/                    # API请求 (9个文件)
├── views/                  # 页面 (14个)
├── stores/                 # Pinia状态 (5个)
├── locales/                # 国际化 (3个)
├── data/                   # 静态数据 (5个)
└── utils/                  # 工具函数 (5个)
```

### 9.3 前端-后端映射

| 前端API文件 | 后端Controller | 业务域 |
|------------|---------------|--------|
| api/portal/paperSubmission.ts | PaperSubmissionController | 论文投稿 |
| api/portal/register.ts | RegisterUserController | 注册管理 |
| api/portal/payment.ts | PaymentController | 支付 |
| api/portal/meeting*.ts | MeetingInfo/Session/AgendaItem/SpeakerController | 会议日程 |
| api/system/user.ts | UserController | 用户管理 |
| api/system/role.ts | RoleController | 角色管理 |
| api/system/menu.ts | MenuController | 菜单管理 |
| api/system/dict.ts | DictController | 字典管理 |
| api/system/dept.ts | DeptController | 部门管理 |
| api/system/notice.ts | NoticeController | 通知公告 |
| api/system/log.ts | LogController | 操作日志 |
| api/system/config.ts | ConfigController | 系统配置 |
| api/system/emailTemplate.ts | EmailTemplateController | 邮件模板 |
| api/auth.ts | AuthController | 认证 |

## 10. 配置架构

### 10.1 多环境配置

```
application.yml          → 基础配置 (应用名, active profile)
application-dev.yml      → 开发环境 (port:8000, 本地MySQL/Redis)
application-prod.yml     → 生产环境 (生产数据库/Redis配置)
```

### 10.2 关键配置项

| 配置域 | 配置类 | 关键属性 |
|-------|--------|---------|
| 安全 | SecurityProperties | ignoreUrls, unsecuredUrls, tokenSecret, tokenExpire |
| IMAP | ImapProperties | host, port, username, password, cron, maxProcessCount, autoResend, maxCount |
| 微信支付 | WechatPayConfig | appId, mchId, apiV3Key, certPath |
| 文件上传 | - | type(aliyun/local/minio), endpoint, bucket |
| 邮件 | spring.mail | host(smtp.163.com), port(465), username, password |

## 11. 异常处理

```
GlobalExceptionHandler
  ├── BizException          → Result.failed(bizCode, message)
  ├── MethodArgumentNotValidException → 参数校验失败
  ├── ConstraintViolationException    → 约束违反
  ├── AccessDeniedException           → 无权限访问
  ├── AuthenticationException        → 未认证
  └── Exception                       → Result.failed("系统异常")
```

## 12. AOP切面

### 12.1 @Log — 操作日志

```java
@Log(value = "操作描述", module = LogModuleEnum.XXX)
public Result<?> someMethod() { ... }
```

- **LogAspect**: Around通知，记录请求参数、响应内容、执行时间、IP、浏览器
- **异步保存**: 保存到sys_log表 (MyISAM引擎，无事务开销)

### 12.2 @RepeatSubmit — 防重提交

```java
@RepeatSubmit(expire = 5)  // 5秒内防重
public Result<?> someMethod() { ... }
```

- **RepeatSubmitAspect**: 基于Redis分布式锁，相同请求参数+用户在expire秒内拒绝

### 12.3 @DataPermission — 数据权限

```java
@DataPermission
public IPage<User> getUserPage() { ... }
```

- **MyDataPermissionHandler**: 解析当前用户角色数据范围，注入SQL WHERE条件

## 13. 文件上传策略

```
FileService (接口)
  ├── AliyunOssService   → 阿里云OSS
  ├── LocalFileService   → 本地磁盘
  └── MinioFileService   → MinIO对象存储

选择策略: application.yml中 file.type = aliyun/local/minio
```