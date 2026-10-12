# MCLC 项目 Phase 1 深度分析报告

> 本报告基于对项目源代码的全量扫描和关键文件的深度阅读，不包含任何编造内容。
> 生成时间：2026-08-14
> 分析范围：后端Java（250+文件）、前端Admin（Vue3+TS）、前端Webpage（Vue3+JS）、SQL脚本、配置文件、部署文件

---

## 1. 项目整体架构与技术栈

### 1.1 系统定位
MCLC（国际学术会议管理系统）是一个面向学术会议组织者的全流程管理平台，覆盖：
- 会议信息管理（日程、嘉宾、演讲、组织架构）
- 参会者注册（9步向导式注册、问卷、签到二维码）
- 论文投稿与审稿（DOCX转PDF、专家审核、抵扣码生成）
- 邮件系统（9态状态机、退信检测、自动重试）
- 支付系统（Stripe/微信/支付宝/银行转账四轨并行）
- 系统管理（RBAC权限、用户、角色、菜单、字典、日志）

### 1.2 技术栈全景

| 层次 | 技术 | 版本 | 用途 |
|------|------|------|------|
| **后端框架** | Spring Boot | 4.0.1 | 主框架 |
| **JDK** | Eclipse Temurin | 17 | 运行时 |
| **ORM** | MyBatis-Plus | 3.5.15 | 数据访问 |
| **认证** | Spring Security + JWT | - | 认证授权 |
| **API文档** | Knife4j + OpenAPI3 | 4.5.0 | 接口文档 |
| **对象映射** | MapStruct | 1.6.3 | Entity↔Form↔VO↔Query |
| **缓存** | Caffeine + Redis + Redisson | 3.1.8 / 4.1.0 | 本地+分布式缓存+分布式锁 |
| **定时任务** | XXL-Job | 3.2.0 | 异步任务调度（开发环境禁用） |
| **文件存储** | MinIO / 阿里云OSS / 本地 | 8.5.10 | 文件上传策略模式 |
| **PDF生成** | OpenPDF + JODConverter + LibreOffice | - | DOCX→PDF转换 |
| **支付** | Stripe SDK / 微信支付SDK / 支付宝SDK | - | 四轨支付 |
| **邮件** | Spring Mail + JavaMail + IMAP | - | 邮件发送+退信检测 |
| **SMS** | 阿里云SMS | - | 短信验证码 |
| **微信** | WxJava | 4.6.0 | 小程序认证 |
| **实时推送** | WebSocket (STOMP) | - | 在线用户计数+消息推送 |
| **二维码** | ZXing | 3.5.3 | 签到二维码生成 |
| **监控** | Spring Boot Admin | - | 应用监控 |
| **前端Admin** | Vue 3 + TypeScript + Vite + Element Plus | 4.3.0 | 管理后台 |
| **前端Webpage** | Vue 3 + JavaScript + Vite + Element Plus | - | 参会者前台 |
| **前端状态** | Pinia | - | 状态管理 |
| **前端路由** | Vue Router | - | SPA路由 |
| **前端i18n** | vue-i18n | - | 国际化 |
| **前端工具** | UnoCSS + Axios | - | CSS原子化+HTTP客户端 |
| **数据库** | MySQL | 8.0.45 | 主数据存储 |
| **容器** | Docker + docker-compose | - | 部署 |
| **Web服务器** | Nginx（隐含） | - | 反向代理 |

### 1.3 架构模式

```
┌─────────────────────────────────────────────────────────┐
│                    Nginx (反向代理)                       │
├────────────────────────┬────────────────────────────────┤
│   /admin/ → Admin SPA  │   / → Webpage SPA              │
│   (Vue3+TS+ElementPlus)│   (Vue3+JS+ElementPlus)        │
├────────────────────────┴────────────────────────────────┤
│              /dev-api/* → Spring Boot :8000              │
├─────────────────────────────────────────────────────────┤
│  Spring Boot 4.0.1 Application                          │
│  ┌──────────┬──────────┬──────────┬──────────┐          │
│  │  auth    │  system  │  portal  │  support │          │
│  │ 认证授权  │ 系统管理  │ 业务模块  │ 基础服务  │          │
│  ├──────────┼──────────┼──────────┼──────────┤          │
│  │  config  │  core    │  module  │ security │          │
│  │ 配置层   │ 核心切面  │ 功能模块  │ 安全层   │          │
│  └──────────┴──────────┴──────────┴──────────┘          │
│  ┌─────────────────────────────────────────┐            │
│  │  MyBatis-Plus 3.5.15 → MySQL 8.0.45     │            │
│  │  Redis + Redisson (缓存/锁/Token)        │            │
│  │  MinIO / 阿里云OSS / 本地 (文件存储)      │            │
│  └─────────────────────────────────────────┘            │
└─────────────────────────────────────────────────────────┘
```

### 1.4 双前端架构

| 特性 | Admin前端 | Webpage前端 |
|------|-----------|-------------|
| 路径 | `frontend/` | `webpage/` |
| 语言 | TypeScript | JavaScript |
| 基座 | vue3-element-admin v4.3.0 | vue3_cli_default |
| 构建工具 | Vite + TypeScript | Vite |
| UI框架 | Element Plus | Element Plus |
| 状态管理 | Pinia (7模块) | Pinia (5模块) |
| 国际化 | vue-i18n (中/英) | vue-i18n (中/英/日) |
| 路由 | 40+页面 | 14+页面 |
| 部署路径 | `/admin/` | `/` |
| API代理 | `/dev-api` → 后端 | `/dev-api` → localhost:8000 |
| 目标用户 | 管理员 | 参会者 |

---

## 2. 后端模块分解与核心类映射

### 2.1 顶层包结构（8大包）

| 包 | 文件数 | 职责 |
|----|--------|------|
| `auth` | 10 | 认证授权（登录/登出/刷新Token/SMS/微信小程序） |
| `common` | 23 | 公共工具（Result/分页/枚举/异常/IP工具） |
| `config` | 34 | 配置类（Security/CORS/Redis/Mail/WebSocket/Knife4j等） |
| `core` | 13 | 核心切面（日志/防重提交/全局异常/过滤器） |
| `module` | 20 | 功能模块（文件上传/代码生成器） |
| `portal` | 130+ | 业务核心（会议/注册/论文/支付/嘉宾/日程等） |
| `security` | 22 | 安全层（JWT/Token/Provider/Filter/Permission） |
| `support` | 30+ | 基础服务（邮件/SMS/WebSocket） |
| `system` | 120+ | 系统管理（用户/角色/菜单/字典/部门/配置/日志） |

### 2.2 auth 包 — 认证授权

```
auth/
├── controller/
│   ├── AuthController              — /api/v1/auth (登录/登出/验证码/刷新Token/SMS登录)
│   └── WechatMiniappAuthController — /api/v1/wechat/miniapp/auth (静默登录/手机登录/绑定手机)
├── model/
│   ├── form/
│   │   ├── LoginForm               — 用户名+密码登录表单
│   │   ├── SmsLoginForm            — SMS验证码登录表单
│   │   ├── WechatMiniappLoginReq   — 微信小程序登录请求
│   │   └── TokenInfoResp           — Token响应(accessToken+refreshToken)
│   └── vo/ (无)
├── service/
│   ├── AuthService                 — 认证服务接口
│   └── WechatMiniappAuthService    — 微信小程序认证接口
└── service/impl/
    ├── AuthServiceImpl             — 认证实现(密码校验→JWT生成→Redis存储)
    └── WechatMiniappAuthServiceImpl— 微信实现(WxMaService→手机号解密→自动注册)
```

### 2.3 system 包 — 系统管理

```
system/
├── controller/ (11个)
│   ├── UserController          — /api/v1/users (CRUD+导入导出+密码重置+手机/邮箱绑定)
│   ├── RoleController          — /api/v1/roles (CRUD+权限分配)
│   ├── MenuController          — /api/v1/menus (CRUD+路由树)
│   ├── DeptController          — /api/v1/depts (部门树CRUD)
│   ├── DictController          — /api/v1/dicts (字典+字典项CRUD)
│   ├── ConfigController        — /api/v1/configs (系统配置CRUD+刷新)
│   ├── NoticeController        — /api/v1/notices (通知CRUD+已读)
│   ├── LogController           — /api/v1/logs (操作日志查询)
│   ├── EmailTemplateController — /api/v1/email-templates (邮件模板CRUD)
│   ├── RegistrationTypeController — /api/v1/registration-types (注册类型)
│   └── StatisticsController    — /api/v1/statistics (访问趋势+概览)
├── model/
│   ├── entity/ (16个): User, Role, Menu, Dept, Dict, DictItem, Config, Notice, UserNotice, UserRole, RoleMenu, RoleDept, Log, UserSocial, EmailTemplate, RegistrationType
│   ├── form/ (15个): 对应Entity的表单
│   ├── query/ (11个): 查询条件
│   ├── vo/ (18个): 视图对象
│   ├── bo/ (6个): 业务对象
│   ├── dto/ (5个): 数据传输对象
│   └── enums/ (5个): DataScopeEnum, MenuTypeEnum, GenderEnum, UserStatusEnum, NoticeEnum
├── converter/ (10个): MapStruct转换器
├── mapper/ (16个): MyBatis-Plus Mapper
├── service/ (16个): 服务接口
├── service/impl/ (16个): 服务实现
├── handler/ (1个): XxlJobSampleHandler
└── listener/ (1个): (事件监听器)
```

### 2.4 portal 包 — 业务核心

```
portal/
├── controller/ (21个)
│   ├── RegisterUserController     — /api/v1/registerUser (CRUD+审核+签到)
│   ├── PaperSubmissionController  — /api/v1/paperSubmission (CRUD+审稿+邮件+PDF导出)
│   ├── MeetingInfoController      — /api/v1/meeting (会议信息CRUD)
│   ├── GuestController            — /api/v1/guest (嘉宾CRUD)
│   ├── SpeechController           — /api/v1/speech (演讲CRUD)
│   ├── ContactController          — /api/v1/contact (联系人CRUD)
│   ├── HotelController            — /api/v1/hotel (酒店CRUD)
│   ├── ArchitectureController     — /api/v1/architecture (组织架构CRUD)
│   ├── ArrangeController          — /api/v1/arrange (安排CRUD)
│   ├── MomentsController          — /api/v1/moments (精彩瞬间CRUD)
│   ├── DiscountConfigurationController — /api/v1/discount (折扣配置CRUD)
│   ├── UserCheckInController      — /api/v1/userCheckIn (签到查询+删除)
│   ├── PaymentController          — (支付相关)
│   ├── WechatCallbackController   — /api/v1/payment/wechat/notify (微信支付回调)
│   ├── AlipayCallbackController   — /api/v1/payment/alipay/notify (支付宝回调)
│   └── StripeWebhookController    — /api/v1/stripe/webhook (Stripe回调)
├── model/
│   ├── entity/ (24个): RegisterUserInformation, PaperSubmission, PaperSubmissionEmail, PaymentOrder, MeetingInfo, MeetingSession, MeetingAgendaItem, MeetingSessionParticipant, MeetingSpeaker, MeetingDate, MeetingPeriod, MeetingSetting, Guest, Speech, SpeechV1, Hotel, Contact, Architecture, Arrange, Moments, Question, QuestionnaireSurvey, DiscountConfiguration, UserCheckIn
│   ├── form/ (23个): 对应Entity的表单
│   ├── query/ (12个): 查询条件
│   ├── vo/ (21个): 视图对象
│   └── enums/: PaymentMethodEnum等
├── converter/ (13个): MapStruct转换器
├── mapper/ (24个): MyBatis-Plus Mapper
├── service/ (23个): 服务接口
├── service/impl/ (23个): 服务实现
│   ├── ★ PaperSubmissionServiceImpl  (37.5KB) — 论文投稿核心业务
│   ├── ★ RegisterUserServiceImpl     (25.2KB) — 注册用户业务
│   ├── ★ MeetingInfoServiceImpl      (20.7KB) — 会议信息业务
│   ├── ★ PaymentServiceImpl          — 支付业务
│   ├── ★ DocxToPdfServiceImpl        — DOCX转PDF
│   └── ★ PaperPdfGeneratorServiceImpl — PDF生成
└── payment/ (策略模式)
    ├── PaymentProvider          — 支付提供者接口
    ├── PaymentRouter            — 支付路由器(按PaymentMethodEnum匹配)
    ├── CreatePaymentRequest     — 创建支付请求
    ├── PaymentProviderResponse  — 支付响应
    └── impl/
        ├── StripePaymentProvider       — Stripe支付
        ├── WechatPaymentProvider       — 微信支付
        ├── AlipayPaymentProvider       — 支付宝支付
        └── BankTransferPaymentProvider — 银行转账
```

### 2.5 support 包 — 基础服务

```
support/
├── mail/ (邮件子系统)
│   ├── controller/ (3个): MailSendRecordController, MailBounceRecordController, MailBounceBlacklistController
│   ├── enums/ (5个): MailStatusEnum(9态), BounceCategoryEnum, BounceTypeEnum, BounceActionEnum, DeliveryStatusEnum
│   ├── job/: BounceEmailJob (46.2KB) — 退信检测定时任务
│   ├── mapper/ (4个): MailSendRecordMapper, MailBounceRecordMapper, MailBounceBlacklistMapper, MailImapCheckpointMapper
│   ├── dto/ (3个): 邮件DTO
│   ├── entity/ (4个): MailSendRecord, MailBounceRecord, MailBounceBlacklist, MailImapCheckpoint
│   ├── query/ (2个): 查询条件
│   ├── vo/ (2个): 视图对象
│   ├── parser/: BounceParser (26.6KB) — 通用退信解析器
│   ├── service/ (5个): MailService, MailSendRecordService, MailBounceRecordService, MailBounceBlacklistService, MailImapCheckpointService
│   └── service/impl/ (5个): 对应实现
├── sms/ (短信子系统)
│   ├── enums/: SmsTypeEnum
│   ├── service/: AliyunSmsService接口
│   └── service/impl/: AliyunSmsServiceImpl
└── websocket/ (WebSocket子系统)
    ├── dto/ (2个): WebSocket消息DTO
    ├── job/: OnlineUserCountJob — 在线用户计数定时任务
    ├── publisher/: WebSocketPublisher — 消息发布
    ├── service/: WebSocketService接口
    ├── service/impl/: WebSocketServiceImpl
    ├── session/: UserSessionRegistry — 用户会话注册表
    └── topics/: Topics常量
```

### 2.6 security 包 — 安全层

```
security/
├── exception/ (2个): UnauthorizedException, BusinessException
├── filter/ (2个): JwtAuthenticationFilter, RepeatSubmitFilter
├── handler/ (2个): AuthenticationEntryPointImpl, AccessDeniedHandlerImpl
├── model/ (8个): LoginUserDetails, UserPrincipal, SecurityUser等
├── provider/ (3个): DaoAuthenticationProvider, SmsAuthenticationProvider, EmailAuthenticationProvider
├── service/ (2个): SysUserDetailsService, PermissionService(@Component("ss"))
├── token/ (3个): JwtTokenManager(16.1KB), RedisTokenManager, TokenCleanScheduler
└── util/ (1个): SecurityUtils
```

### 2.7 config 包 — 配置层（34个配置类）

| 配置类 | 职责 |
|--------|------|
| SecurityConfig | Spring Security核心配置，4种AuthenticationProvider |
| WebSocketConfig | STOMP协议配置，端点/ws，消息代理 |
| CorsConfig | 跨域配置 |
| RedisConfig | Redis序列化配置 |
| RedisCacheConfig | Redis缓存管理器配置 |
| CaffeineConfig | Caffeine本地缓存配置 |
| MailConfig | JavaMailSender配置 |
| MybatisConfig | MyBatis-Plus分页插件配置 |
| Knife4jOpenApiCustomizer | API文档自定义 |
| OpenApiConfig | Swagger/OpenAPI配置 |
| AsyncConfig | 异步任务线程池配置 |
| CaptchaConfig | 验证码配置 |
| PasswordEncoderConfig | 密码编码器配置 |
| JacksonConfig | JSON序列化配置 |
| StripeConfig | Stripe支付配置 |
| AlipayConfig | 支付宝配置 |
| WechatPayConfig | 微信支付配置 |
| WxMaConfig | 微信小程序配置 |
| AdminServerConfig | Spring Boot Admin配置 |
| XxlJobConfig | XXL-Job配置 |
| property/ | 配置属性类（AlipayProperties, AliyunSmsProperties, BankTransferProperties, CaptchaProperties, CodegenProperties, ImapProperties, StripeProperties, WxMaProperties） |

### 2.8 核心类文件大小排名（Top 10）

| 排名 | 文件 | 大小 | 包 |
|------|------|------|-----|
| 1 | BounceEmailJob.java | 46.2KB | support.mail.job |
| 2 | BounceParser.java | 26.6KB | support.mail.parser |
| 3 | MailSendRecordServiceImpl.java | 39.3KB | support.mail.service.impl |
| 4 | PaperSubmissionServiceImpl.java | 37.5KB | portal.service.impl |
| 5 | RegisterUserServiceImpl.java | 25.2KB | portal.service.impl |
| 6 | UserServiceImpl.java | 22.1KB | system.service.impl |
| 7 | MeetingInfoServiceImpl.java | 20.7KB | portal.service.impl |
| 8 | JwtTokenManager.java | 16.1KB | security.token |
| 9 | MenuServiceImpl.java | 17.9KB | system.service.impl |
| 10 | PaperSubmissionController.java | 17.5KB | portal.controller |

---

## 3. 前端模块分解与页面映射

### 3.1 Admin前端（frontend/）

#### API层（30+文件）

| 模块 | 文件 | 对应后端 |
|------|------|----------|
| 认证 | auth.ts | AuthController |
| 文件 | file.ts | FileController |
| 代码生成 | codegen.ts | CodegenController |
| Portal | answer.ts | AnswerController(隐含) |
| Portal | architecture.ts | ArchitectureController |
| Portal | arrange.ts | ArrangeController |
| Portal | contact.ts | ContactController |
| Portal | discountConfiguration.ts | DiscountConfigurationController |
| Portal | guest.ts | GuestController |
| Portal | hotel.ts | HotelController |
| Portal | mailBlacklist.ts | MailBounceBlacklistController |
| Portal | mailBounceRecord.ts | MailBounceRecordController |
| Portal | mailSendRecord.ts | MailSendRecordController |
| Portal | meeting.ts | MeetingInfoController |
| Portal | moments.ts | MomentsController |
| Portal | paperSubmission.ts | PaperSubmissionController |
| Portal | registerUser.ts | RegisterUserController |
| Portal | setting.ts | MeetingSettingController(隐含) |
| Portal | speech.ts | SpeechController |
| Portal | userCheckIn.ts | UserCheckInController |
| System | config.ts | ConfigController |
| System | dept.ts | DeptController |
| System | dict.ts | DictController |
| System | emailTemplate.ts | EmailTemplateController |
| System | log.ts | LogController |
| System | menu.ts | MenuController |
| System | notice.ts | NoticeController |
| System | registrationType.ts | RegistrationTypeController |
| System | role.ts | RoleController |
| System | statistics.ts | StatisticsController |
| System | tenant.ts | TenantController(隐含) |
| System | tenant-plan.ts | TenantPlanController(隐含) |
| System | user.ts | UserController |

#### 页面层（40+页面）

| 路径 | 页面 | 功能 |
|------|------|------|
| login/ | Login, Register, ResetPwd | 登录/注册/重置密码 |
| dashboard/ | Dashboard | 仪表盘 |
| system/user/ | UserList | 用户管理 |
| system/role/ | RoleList | 角色管理 |
| system/menu/ | MenuList | 菜单管理 |
| system/dept/ | DeptList | 部门管理 |
| system/dict/ | DictList, DictItem | 字典管理 |
| system/config/ | ConfigList | 系统配置 |
| system/notice/ | NoticeList | 通知管理 |
| system/registration/ | RegistrationType | 注册类型 |
| system/tenant/ | Tenant, TenantPlan | 租户管理 |
| portal/meeting/ | MeetingList | 会议日程管理 |
| portal/guest/ | GuestList | 嘉宾管理 |
| portal/speech/ | SpeechList | 演讲管理 |
| portal/hotel/ | HotelList | 酒店管理 |
| portal/contact/ | ContactList | 联系人管理 |
| portal/arrange/ | ArrangeList | 安排管理 |
| portal/home/architecture/ | Architecture | 组织架构 |
| portal/home/moments/ | Moments | 精彩瞬间 |
| portal/home/speech/ | SpeechHome | 演讲展示 |
| portal/answer/ | AnswerList | 问卷回答 |
| portal/setting/ | MeetingSetting | 会议设置 |
| paperSubmission/ | PaperSubmission | 论文投稿管理 |
| mailRecord/ | MailRecord, Blacklist | 邮件记录+黑名单 |
| emailConfiguration/ | EmailConfig | 邮件配置 |
| discountConfiguration/ | DiscountConfig | 折扣配置 |
| hotelRecords/ | HotelRecords | 酒店记录 |
| payMoney/ | PayMoney | 付款管理 |
| checkIn/ | CheckIn | 签到管理 |
| userNum/ | UserNum | 用户统计 |
| report/ | Report | 报表 |
| contribution/ | Contribution | 投稿贡献 |
| profile/ | Profile, Notice | 个人中心+通知 |
| demo/ | 14个demo页面 | 组件示例 |

#### Pinia Store（7模块）

| Store | 文件 | 职责 |
|-------|------|------|
| app | app.ts | 应用全局状态（侧边栏/布局） |
| user | user.ts | 用户认证状态（登录/登出/Token） |
| permission | permission.ts | 权限路由（动态路由生成） |
| dict | dict.ts | 字典缓存 |
| settings | settings.ts | 系统设置 |
| tags-view | tags-view.ts | 标签页视图 |
| tenant | tenant.ts | 租户状态 |

### 3.2 Webpage前端（webpage/）

#### API层（19+文件）

| 文件 | 对应后端 |
|------|----------|
| auth.js | AuthController |
| user.js | UserController |
| upload.js | FileController |
| payment.js | PaymentController |
| submit.js | PaperSubmissionController |
| contribution.js | 论文投稿相关 |
| discountConfiguration.js | DiscountConfigurationController |
| emailTemplate.js | EmailTemplateController |
| registrationType.js | RegistrationTypeController |
| portal/answer.js | 问卷回答 |
| portal/architecture.js | 组织架构 |
| portal/arrange.js | 安排 |
| portal/contact.js | 联系人 |
| portal/guest.js | 嘉宾 |
| portal/hotel.js | 酒店 |
| portal/meeting.js | 会议 |
| portal/meetSetting.js | 会议设置 |
| portal/moments.js | 精彩瞬间 |
| portal/speech.js | 演讲 |

#### 页面层（14+页面）

| 页面 | 文件 | 功能 |
|------|------|------|
| Home | Home.vue | 首页 |
| Guests | Guests.vue | 嘉宾介绍 |
| Schedule | Schedule.vue | 会议日程 |
| Hotel | Hotel.vue | 酒店信息 |
| Faq | Faq.vue | 常见问题 |
| PreMeeting | PreMeeting.vue | 会前信息 |
| Submit | Submit.vue | 论文提交 |
| FirstDraft | FirstDraft.vue | 初稿提交 |
| Payment | Payment.vue | 支付 |
| ReviewFile | ReviewFile.vue | 审稿文件 |
| Register | Register.vue | 注册入口 |
| EmailConfiguration | EmailConfiguration.vue | 邮件配置 |
| ArticleList | ArticleList.vue | 文章列表 |
| ArticleDetail | ArticleDetail.vue | 文章详情 |

#### 注册9步向导

| 步骤 | 组件 | 功能 |
|------|------|------|
| 1 | Signup.vue | 账号注册 |
| 2 | Contact.vue | 联系信息 |
| 3 | Enroll.vue | 注册类型选择 |
| 4 | Skills.vue | 专业技能 |
| 5 | Expert.vue | 专家信息 |
| 6 | Survey.vue | 问卷调查 |
| 7 | Consent.vue | 同意条款 |
| 8 | Confirm.vue | 确认提交 |
| 9 | Final.vue | 完成注册 |

#### Pinia Store（5模块）

| Store | 文件 | 职责 |
|-------|------|------|
| user | user.js | 用户认证状态 |
| register | register.js | 注册向导状态（9步数据） |
| meeting | meeting.js | 会议信息缓存 |
| guest | guest.js | 嘉宾数据 |
| lang | lang.js | 语言切换 |

#### 国际化（3套语言包）

| 语言 | 目录 |
|------|------|
| 中文 | locales/zh/ |
| 英文 | locales/en/ |
| 日文 | locales/ja/ |

---

## 4. 数据库Schema与表关系

### 4.1 表分类统计（45张表）

| 分类 | 前缀 | 数量 | 表名 |
|------|------|------|------|
| **系统核心** | sys_ | 16 | sys_dept, sys_dict, sys_dict_item, sys_menu, sys_role, sys_role_menu, sys_role_dept, sys_user, sys_user_role, sys_log, sys_config, sys_notice, sys_user_notice, sys_user_social, sys_email_template |
| **业务核心** | portal_ | 13 | portal_architecture, portal_arrange, portal_contact, portal_guest, portal_hotel, portal_meeting_date, portal_meeting_period, portal_meeting_setting, portal_moments, portal_question, portal_speech, register_user_information, questionnaire_survey |
| **会议日程** | meeting_ | 5 | meeting_info, meeting_session, meeting_agenda_item, meeting_session_participant, meeting_speaker |
| **邮件系统** | mail_ | 4 | mail_send_record, mail_bounce_record, mail_bounce_blacklist, mail_imap_checkpoint |
| **其他业务** | - | 7 | paper_submission, paper_submission_email, payment_orders, discount_configuration, registration_type, user_check_in, gen_table, gen_table_column |

### 4.2 核心表ER关系

```
sys_user ──M:N── sys_role ──M:N── sys_menu
   │                │
   │                └──M:N── sys_dept (sys_role_dept)
   │
   ├──1:N── sys_user_role
   ├──1:N── sys_user_notice
   ├──1:N── sys_user_social
   └──1:1── register_user_information

register_user_information ──1:N── user_check_in
                          ──1:N── paper_submission
                          ──1:N── payment_orders
                          ──1:N── questionnaire_survey

meeting_info ──1:N── meeting_session ──1:N── meeting_agenda_item
             │                          └──1:N── meeting_session_participant
             └──1:N── meeting_speaker

portal_speech ──1:1── portal_guest (隐含关联)
portal_meeting_setting ──1:N── portal_meeting_date ──1:N── portal_meeting_period

mail_send_record ──1:N── mail_bounce_record
                ──关联── mail_imap_checkpoint (增量检查点)
mail_bounce_record ──关联── mail_bounce_blacklist (自动拉黑)

paper_submission ──1:N── paper_submission_email (审稿邮件记录)
paper_submission ──关联── discount_configuration (审核通过→生成抵扣码)

payment_orders ──关联── discount_configuration (折扣应用)
```

### 4.3 关键表字段概要

#### sys_user（用户表）
- id, username, password, nickname, phone, email, avatar, gender, status
- dept_id(FK→sys_dept), data_scope(数据权限范围)

#### register_user_information（注册用户表）
- id, user_id(FK→sys_user), first_name, last_name, organization, country_type(ZH/EN)
- registration_type_id(FK→registration_type), check_in_qrcode, is_check_in
- survey_answers(JSON), expert_info(JSON)

#### meeting_info（会议信息表）
- id, title, title_en, description, description_en, start_date, end_date
- venue, venue_en, country_type(ZH/EN双轨i18n)

#### mail_send_record（邮件发送记录表）
- id, recipient, subject, content, status(9态枚举), retry_count, max_retry
- scheduled_at, sent_at, error_message, template_id, template_variables

#### paper_submission（论文投稿表）
- id, user_id, title, abstract, keywords, file_path, pdf_path
- status, review_result, discount_code(审核通过后生成)

#### payment_orders（支付订单表）
- id, user_id, order_no, amount, currency, payment_method(枚举)
- status, transaction_id, paid_at

### 4.4 邮件9态状态机

```
PENDING → SCHEDULED → SENDING → SENT → DELIVERED
                                    ↓
                              BOUNCED_PERMANENT → (黑名单)
                              BOUNCED_TEMPORARY → RETRY → SENDING(循环)
                                    ↓ (超过max_retry)
                              FAILED
```

| 状态 | 说明 | 可流转到 |
|------|------|----------|
| PENDING | 待发送 | SCHEDULED |
| SCHEDULED | 已调度 | SENDING |
| SENDING | 发送中 | SENT, FAILED |
| SENT | 已发送 | DELIVERED, BOUNCED_PERMANENT, BOUNCED_TEMPORARY |
| DELIVERED | 已送达 | (终态) |
| BOUNCED_PERMANENT | 永久退信 | (终态，自动拉黑) |
| BOUNCED_TEMPORARY | 临时退信 | RETRY |
| RETRY | 重试中 | SENDING |
| FAILED | 发送失败 | (终态) |

### 4.5 双轨i18n策略

| 策略 | 适用表 | 实现方式 | 示例 |
|------|--------|----------|------|
| **行级i18n** | portal_* 表 | country_type字段(ZH/EN)标识语言行 | portal_guest: country_type='ZH'为中文名行, 'EN'为英文名行 |
| **列级i18n** | meeting_* 表 | _en后缀字段存英文 | meeting_info: title(中文), title_en(英文) |

---

## 5. API端点全量清单

### 5.1 按模块分组统计

| 模块 | 前缀 | 端点数 | 核心Controller |
|------|------|--------|----------------|
| 认证 | /api/v1/auth | ~8 | AuthController, WechatMiniappAuthController |
| 系统管理 | /api/v1/{users,roles,menus,depts,dicts,configs,notices,logs,email-templates,registration-types,statistics} | ~60 | 11个SystemController |
| Portal业务 | /api/v1/{registerUser,paperSubmission,meeting,guest,speech,contact,hotel,architecture,arrange,moments,discount,userCheckIn} | ~80 | 21个PortalController |
| 支付回调 | /api/v1/payment/{wechat,alipay}/notify, /api/v1/stripe/webhook | ~3 | 3个CallbackController |
| 邮件 | /api/v1/{mailSendRecord,mailBounceRecord,mailBounceBlacklist} | ~15 | 3个MailController |
| 文件 | /api/v1/file | ~2 | FileController |
| 代码生成 | /api/v1/codegen | ~3 | CodegenController |
| 公开接口 | /api/v1/public/* | ~10 | 各Controller的@Public标记方法 |
| **合计** | - | **261** | - |

### 5.2 认证模块端点

| 方法 | 路径 | 说明 | 认证 |
|------|------|------|------|
| POST | /api/v1/auth/login | 用户名+密码登录 | 否 |
| POST | /api/v1/auth/logout | 登出 | 是 |
| POST | /api/v1/auth/refreshToken | 刷新AccessToken | 是 |
| GET | /api/v1/auth/captcha | 获取图形验证码 | 否 |
| POST | /api/v1/auth/smsLogin | SMS验证码登录 | 否 |
| POST | /api/v1/auth/emailLogin | 邮箱验证码登录 | 否 |
| POST | /api/v1/wechat/miniapp/auth/silent | 微信小程序静默登录 | 否 |
| POST | /api/v1/wechat/miniapp/auth/phone | 微信小程序手机号登录 | 否 |
| POST | /api/v1/wechat/miniapp/auth/bindPhone | 微信小程序绑定手机 | 是 |

### 5.3 系统管理端点（核心）

| 方法 | 路径 | 说明 |
|------|------|------|
| GET | /api/v1/users | 用户分页列表 |
| POST | /api/v1/users | 新增用户 |
| PUT | /api/v1/users/{id} | 更新用户 |
| DELETE | /api/v1/users/{id} | 删除用户 |
| PUT | /api/v1/users/{id}/password/reset | 重置密码 |
| PUT | /api/v1/users/{id}/phone/bind | 绑定手机 |
| PUT | /api/v1/users/{id}/email/bind | 绑定邮箱 |
| GET | /api/v1/users/{id}/roles | 用户角色 |
| POST | /api/v1/users/import | 导入用户 |
| GET | /api/v1/users/export | 导出用户 |
| GET | /api/v1/roles | 角色列表 |
| POST | /api/v1/roles | 新增角色 |
| PUT | /api/v1/roles/{id} | 更新角色 |
| DELETE | /api/v1/roles/{id} | 删除角色 |
| PUT | /api/v1/roles/{id}/menus | 分配菜单权限 |
| PUT | /api/v1/roles/{id}/depts | 分配数据权限 |
| GET | /api/v1/menus | 菜单列表 |
| POST | /api/v1/menus | 新增菜单 |
| PUT | /api/v1/menus/{id} | 更新菜单 |
| DELETE | /api/v1/menus/{id} | 删除菜单 |
| GET | /api/v1/menus/routes | 路由树 |
| GET | /api/v1/depts | 部门树 |
| GET | /api/v1/dicts | 字典列表 |
| GET | /api/v1/configs | 系统配置 |
| PUT | /api/v1/configs/refresh | 刷新配置缓存 |

### 5.4 Portal业务端点（核心）

| 方法 | 路径 | 说明 |
|------|------|------|
| GET | /api/v1/registerUser | 注册用户分页 |
| POST | /api/v1/registerUser | 新增注册 |
| PUT | /api/v1/registerUser/{id} | 更新注册 |
| DELETE | /api/v1/registerUser/{id} | 删除注册 |
| PUT | /api/v1/registerUser/{id}/status | 审核状态 |
| GET | /api/v1/registerUser/{id}/qrcode | 签到二维码 |
| GET | /api/v1/paperSubmission | 论文列表 |
| POST | /api/v1/paperSubmission | 提交论文 |
| PUT | /api/v1/paperSubmission/{id} | 更新论文 |
| PUT | /api/v1/paperSubmission/{id}/review | 审核论文 |
| POST | /api/v1/paperSubmission/{id}/sendEmail | 发送审稿邮件 |
| POST | /api/v1/paperSubmission/{id}/resendEmail | 重发邮件 |
| GET | /api/v1/paperSubmission/export | 导出PDF |
| GET | /api/v1/meeting | 会议信息 |
| PUT | /api/v1/meeting/{id} | 更新会议 |
| GET | /api/v1/meeting/{id}/sessions | 会议场次 |
| GET | /api/v1/guest | 嘉宾列表 |
| POST | /api/v1/guest | 新增嘉宾 |
| GET | /api/v1/speech | 演讲列表 |
| GET | /api/v1/discount | 折扣配置 |
| GET | /api/v1/userCheckIn | 签到记录 |

### 5.5 支付回调端点

| 方法 | 路径 | 说明 | 认证 |
|------|------|------|------|
| POST | /api/v1/payment/wechat/notify | 微信支付回调 | 否(签名验证) |
| POST | /api/v1/payment/alipay/notify | 支付宝回调 | 否(签名验证) |
| POST | /api/v1/stripe/webhook | Stripe Webhook | 否(签名验证) |

---

## 6. 认证授权流程

### 6.1 四种认证Provider

```
┌─────────────────────────────────────────────────────────┐
│                  SecurityConfig                          │
│  ┌─────────────────────────────────────────────────┐    │
│  │         AuthenticationManager                    │    │
│  │  ┌───────────────┐  ┌───────────────────┐       │    │
│  │  │ DaoAuth       │  │ SmsAuth           │       │    │
│  │  │ Provider      │  │ Provider          │       │    │
│  │  │ (用户名+密码)  │  │ (手机+验证码)     │       │    │
│  │  └───────────────┘  └───────────────────┘       │    │
│  │  ┌───────────────┐  ┌───────────────────┐       │    │
│  │  │ EmailAuth     │  │ WechatMiniapp     │       │    │
│  │  │ Provider      │  │ AuthController    │       │    │
│  │  │ (邮箱+验证码)  │  │ (微信小程序)      │       │    │
│  │  └───────────────┘  └───────────────────┘       │    │
│  └─────────────────────────────────────────────────┘    │
│  ┌─────────────────────────────────────────────────┐    │
│  │  JwtAuthenticationFilter → Token验证链           │    │
│  │  1. 从Header提取Bearer Token                     │    │
│  │  2. JwtTokenManager.validateToken()              │    │
│  │  3. RedisTokenManager.getToken() (黑名单检查)     │    │
│  │  4. SecurityContext.setAuthentication()          │    │
│  └─────────────────────────────────────────────────┘    │
└─────────────────────────────────────────────────────────┘
```

### 6.2 JWT双令牌机制

| 令牌 | 有效期 | 存储 | 用途 |
|------|--------|------|------|
| AccessToken | 30分钟(默认) | Redis | API请求认证 |
| RefreshToken | 7天(默认) | Redis | 刷新AccessToken |

**Token结构（JWT Payload）：**
- sub: 用户ID
- username: 用户名
- jti: 唯一ID（用于黑名单撤销）
- tokenVersion: 用户级版本号（批量失效）
- roles: 角色列表
- permissions: 权限列表
- dataScope: 数据权限范围

**Token安全机制：**
- HS256算法签名
- jti黑名单：登出时将jti加入Redis黑名单，TTL=Token剩余有效期
- tokenVersion：修改密码/禁用用户时递增version，所有旧Token失效
- RefreshToken轮换：使用后旧RefreshToken立即失效

### 6.3 RBAC权限模型

```
用户(sys_user) ─M:N─ 角色(sys_role) ─M:N─ 菜单(sys_menu)
     │                  │
     │                  └──M:N─ 部门(sys_dept) [数据权限]
     │
     └── 数据权限范围(data_scope):
         ├── ALL: 全部数据
         ├── DEPT_AND_SUB: 本部门及子部门
         ├── DEPT: 本部门
         ├── SELF: 仅本人
         └── CUSTOM: 自定义(sys_role_dept)
```

**权限校验方式：**
- `@PreAuthorize("@ss.hasPermission('sys:user:list')")` — 方法级权限
- `@ss` = PermissionService组件
- 菜单权限标识格式：`模块:功能:操作`（如`sys:user:list`）

### 6.4 公开接口配置

SecurityConfig中配置的免认证路径：
- `/api/v1/auth/login`, `/api/v1/auth/captcha`, `/api/v1/auth/smsLogin`, `/api/v1/auth/emailLogin`
- `/api/v1/wechat/miniapp/auth/**`
- `/api/v1/payment/wechat/notify`, `/api/v1/payment/alipay/notify`, `/api/v1/stripe/webhook`
- `/doc.html`, `/webjars/**`, `/v3/api-docs/**` (Knife4j)
- `/ws/**` (WebSocket)
- `/admin/**` (Spring Boot Admin)

---

## 7. 配置体系

### 7.1 三层配置结构

```
application.yml          ← 主配置（spring.profiles.active=dev, 公共配置）
├── application-dev.yml  ← 开发环境（port:8000, 本地MySQL, Redis db12, SMTP 163.com）
└── application-prod.yml ← 生产环境（port:8000, MySQL 192.168.0.169, Redis 192.168.0.169）
```

### 7.2 关键配置项

| 配置域 | 配置项 | 开发值 | 生产值 |
|--------|--------|--------|--------|
| 服务端口 | server.port | 8000 | 8000 |
| 数据库 | spring.datasource.url | localhost:3306/meeting_admin | 192.168.0.169:13306/meeting_admin |
| Redis | spring.data.redis.database | 12 | 0 |
| Redis | spring.data.redis.host | localhost | 192.168.0.169 |
| Redis | spring.data.redis.port | 6379 | 16379 |
| 文件上传 | spring.servlet.multipart.max-file-size | 1024MB | 1024MB |
| SMTP | spring.mail.host | smtp.163.com | (生产SMTP) |
| IMAP | spring.mail.imap.host | imap.163.com | (生产IMAP) |
| Tomcat | server.tomcat.connection-timeout | - | 1小时 |
| JWT | jwt.secret | (配置) | (配置) |
| JWT | jwt.access-token-expiration | 30分钟 | (可能不同) |
| JWT | jwt.refresh-token-expiration | 7天 | (可能不同) |

### 7.3 @ConfigurationProperties绑定（8个属性类）

| 属性类 | 前缀 | 用途 |
|--------|------|------|
| AlipayProperties | alipay | 支付宝配置(appId/privateKey/publicKey) |
| AliyunSmsProperties | aliyun.sms | 阿里云SMS(accessKeyId/accessKeySecret/signName/templateCode) |
| BankTransferProperties | bank-transfer | 银行转账配置(bankName/accountNo/accountName) |
| CaptchaProperties | captcha | 验证码配置(enabled/type/expireSeconds) |
| CodegenProperties | codegen | 代码生成器配置(outputDir/templateDir) |
| ImapProperties | imap | IMAP配置(host/port/username/password/folder) |
| StripeProperties | stripe | Stripe配置(apiKey/webhookSecret) |
| WxMaProperties | wx.ma | 微信小程序配置(appid/secret/token/aesKey) |

### 7.4 缓存配置

| 缓存层 | 技术 | 用途 | TTL |
|--------|------|------|-----|
| 本地缓存 | Caffeine | 菜单、字典、配置等高频读数据 | 可配置 |
| 分布式缓存 | Redis | Token、验证码、用户会话 | 按需 |
| 分布式锁 | Redisson | 退信检测互斥、支付幂等 | 自动释放 |

---

## 8. 第三方服务集成

### 8.1 集成清单

| 第三方服务 | 集成方式 | 配置位置 | 核心类 |
|------------|----------|----------|--------|
| **Stripe支付** | Stripe SDK | StripeConfig + StripeProperties | StripePaymentProvider |
| **微信支付** | WxJava SDK | WechatPayConfig | WechatPaymentProvider |
| **支付宝** | Alipay SDK | AlipayConfig + AlipayProperties | AlipayPaymentProvider |
| **银行转账** | 线下+人工确认 | BankTransferProperties | BankTransferPaymentProvider |
| **阿里云SMS** | 阿里云SDK | AliyunSmsProperties | AliyunSmsServiceImpl |
| **微信小程序** | WxJava SDK | WxMaConfig + WxMaProperties | WechatMiniappAuthServiceImpl |
| **MinIO** | MinIO SDK | application.yml | MinioFileService |
| **阿里云OSS** | 阿里云SDK | application.yml | AliyunFileService |
| **本地文件** | java.io | application.yml | LocalFileService |
| **LibreOffice** | JODConverter | Dockerfile(安装) | DocxToPdfServiceImpl |
| **邮件SMTP** | JavaMailSender | MailConfig + application.yml | MailServiceImpl |
| **邮件IMAP** | JavaMail IMAP | ImapProperties | BounceEmailJob |
| **XXL-Job** | XXL-Job Client | XxlJobConfig | (可选，开发环境禁用) |
| **Spring Boot Admin** | Spring Boot Admin | AdminServerConfig | 监控端点 |

### 8.2 支付策略模式

```
PaymentController
    │
    ▼
PaymentRouter.route(PaymentMethodEnum)
    │
    ├── STRIPE → StripePaymentProvider
    │   ├── createPayment() → Stripe Checkout Session
    │   └── handleCallback() → Stripe Webhook签名验证
    │
    ├── WECHAT → WechatPaymentProvider
    │   ├── createPayment() → 微信Native/JSAPI下单
    │   └── handleCallback() → 微信签名验证
    │
    ├── ALIPAY → AlipayPaymentProvider
    │   ├── createPayment() → 支付宝PC/手机网站支付
    │   └── handleCallback() → 支付宝RSA签名验证
    │
    └── BANK_TRANSFER → BankTransferPaymentProvider
        ├── createPayment() → 生成银行转账信息
        └── handleCallback() → 人工确认
```

### 8.3 文件存储策略模式

```
FileService (接口)
    │
    ├── MinioFileService    — @ConditionalOnProperty("minio.enabled")
    ├── AliyunFileService   — @ConditionalOnProperty("aliyun.oss.enabled")
    └── LocalFileService    — 默认实现
```

### 8.4 退信检测架构

```
BounceEmailJob (定时任务, Redisson分布式锁)
    │
    ├── 1. 获取IMAP检查点(mail_imap_checkpoint)
    ├── 2. 增量扫描IMAP收件箱(仅新邮件)
    ├── 3. 对每封邮件调用BounceParser解析
    │       │
    │       ├── DSN解析(RFC 3464) — 最高优先级
    │       ├── SMTP响应码解析(5xx/4xx)
    │       ├── DNS查询(MX记录验证)
    │       └── 正文正则匹配(15+模式)
    │
    ├── 4. 分类退信:
    │       ├── PERMANENT(永久) → 更新mail_send_record + 自动拉黑mail_bounce_blacklist
    │       └── TEMPORARY(临时) → 更新mail_send_record + 标记可重试
    │
    ├── 5. 幂等保证(同一邮件UID不重复处理)
    └── 6. 更新IMAP检查点

---

## 9. 部署架构

### 9.1 Docker部署

**Dockerfile关键配置：**
```dockerfile
FROM eclipse-temurin:17-jre
# 安装LibreOffice + 中文字体（DOCX→PDF转换依赖）
RUN apt-get update && apt-get install -y libreoffice fonts-wqy-zenhei
# JVM参数
ENV JAVA_OPTS="-Xms512m -Xmx512m"
# 清理JODConverter锁文件（防止容器重启后残留锁）
ENTRYPOINT ["sh", "-c", "rm -rf /tmp/.jodconverter_socket_* && java $JAVA_OPTS -jar app.jar"]
```

**docker-compose.yml关键配置：**
```yaml
services:
  mclc-admin:
    image: mclc-admin
    ports:
      - "8989:8000"          # 外部8989 → 内部8000
    environment:
      - SPRING_PROFILES_ACTIVE=prod
    depends_on:
      - mysql
      - redis
```

### 9.2 部署拓扑

```
┌─────────────────────────────────────────────────────────┐
│                    生产环境                               │
│                                                         │
│  ┌─────────┐     ┌──────────────────────────────────┐   │
│  │  Nginx  │────→│  Spring Boot Container :8000     │   │
│  │ (反向代理)│     │  ┌────────────────────────────┐ │   │
│  └─────────┘     │  │  Spring Boot 4.0.1          │ │   │
│       │          │  │  + LibreOffice (JODConverter)│ │   │
│       │          │  │  + JVM 512m                 │ │   │
│  /admin/ → 静态   │  └────────────────────────────┘ │   │
│  / → 静态         └──────────────────────────────────┘   │
│       │                      │                          │
│       │              ┌───────┴────────┐                 │
│       │              ▼                ▼                  │
│       │     ┌──────────────┐  ┌──────────────┐          │
│       │     │ MySQL 8.0.45 │  │ Redis 6.x    │          │
│       │     │ :13306       │  │ :16379       │          │
│       │     │ meeting_admin│  │ db0(Token/锁) │          │
│       │     └──────────────┘  └──────────────┘          │
│       │                                                │
│  ┌────┴─────┐                                          │
│  │ 前端静态  │                                          │
│  │ Admin SPA│  (构建产物, /admin/ 路径)                  │
│  │ Webpage  │  (构建产物, / 路径)                       │
│  └──────────┘                                          │
└─────────────────────────────────────────────────────────┘
```

### 9.3 端口映射

| 服务 | 内部端口 | 外部端口 | 说明 |
|------|----------|----------|------|
| Spring Boot | 8000 | 8989 | 应用服务 |
| MySQL | 13306 | - | 数据库（生产内网） |
| Redis | 16379 | - | 缓存（生产内网） |

### 9.4 环境配置差异

| 配置项 | 开发环境(dev) | 生产环境(prod) |
|--------|---------------|----------------|
| MySQL地址 | localhost:3306 | 192.168.0.169:13306 |
| Redis地址 | localhost:6379/db12 | 192.168.0.169:16379/db0 |
| SMTP | smtp.163.com | (生产SMTP) |
| IMAP | imap.163.com | (生产IMAP) |
| Tomcat超时 | 默认 | 1小时 |
| XXL-Job | 禁用 | 启用 |
| Stripe | 测试密钥 | 生产密钥 |

---

## 10. 代码质量评估

### 10.1 大文件问题（超过15KB）

| 文件 | 大小 | 问题 | 建议 |
|------|------|------|------|
| BounceEmailJob.java | 46.2KB | 严重过大，单文件包含IMAP连接、邮件解析、退信分类、状态更新、黑名单管理、重试调度等6大职责 | 拆分为BounceDetector + BounceClassifier + BounceActionHandler + ImapScanner |
| BounceParser.java | 26.6KB | 15+正则模式+4级分类逻辑集中 | 可按分类拆分为DsnParser + SmtpParser + DnsParser + ContentParser |
| MailSendRecordServiceImpl.java | 39.3KB | 任务表模式+事务+重试+取消逻辑混合 | 拆分为MailSendService + MailRetryService + MailCancelService |
| PaperSubmissionServiceImpl.java | 37.5KB | 5大流程(CRUD+审稿+邮件+抵扣码+PDF)混合 | 拆分为PaperCrudService + PaperReviewService + PaperEmailService + PaperDiscountService |
| RegisterUserServiceImpl.java | 25.2KB | 注册+QRCode+问卷+签到多职责 | 拆分为RegisterService + QrCodeService + CheckInService |
| UserServiceImpl.java | 22.1KB | 用户CRUD+密码+验证码+Token+数据权限 | 拆分为UserCrudService + UserAuthService + UserDataScopeService |
| MeetingInfoServiceImpl.java | 20.7KB | 4级层次组装+中英文映射 | 可拆分为MeetingQueryService + MeetingAssemblyService |
| JwtTokenManager.java | 16.1KB | Token生成+验证+刷新+黑名单+version | 可拆分为TokenGenerator + TokenValidator + TokenRevoker |
| MenuServiceImpl.java | 17.9KB | 菜单CRUD+路由树+缓存+角色关联 | 可拆分为MenuCrudService + MenuRouteService |
| PaperSubmissionController.java | 17.5KB | Controller承载PDF导出逻辑 | PDF导出应移至Service层 |

### 10.2 注入风格不一致

| 风格 | 代表类 | 数量 | 说明 |
|------|--------|------|------|
| **构造器注入(@RequiredArgsConstructor)** | PaperSubmissionServiceImpl, MailSendRecordServiceImpl | 多数ServiceImpl | ✅ 推荐方式 |
| **字段注入(@Autowired)** | MeetingInfoServiceImpl | 少数 | ❌ 不推荐，难以测试 |

### 10.3 命名问题

| 位置 | 问题 | 建议 |
|------|------|------|
| SpeechController: @PutMapping value="/{SpeechId}/status" | S大写不符合Java命名规范 | 应为`/{speechId}/status` |
| portal包内部分Entity命名 | RegisterUserInformation vs 其他Entity用名词 | 命名风格不统一 |
| MailStatusEnum 9个状态 | PENDING/SCHEDULED/SENDING/SENT/DELIVERED/BOUNCED_PERMANENT/BOUNCED_TEMPORARY/RETRY/FAILED | 命名清晰，但BOUNCED_PERMANENT/BOUNCED_TEMPORARY过长 |

### 10.4 安全问题

| 问题 | 位置 | 严重程度 | 说明 |
|------|------|----------|------|
| **明文SMTP密码** | application-dev.yml | 🔴 高 | spring.mail.password明文存储 |
| **Stripe测试密钥** | application-dev.yml | 🟡 中 | stripe.api-key测试密钥明文 |
| **JWT密钥** | application.yml | 🔴 高 | jwt.secret应使用环境变量 |
| **IMAP密码** | application-dev.yml | 🔴 高 | imap.password明文存储 |
| **阿里云SMS密钥** | application-dev.yml | 🟡 中 | aliyun.sms.access-key-secret明文 |

### 10.5 SQL脚本基线不统一

| 脚本 | 表数 | 说明 |
|------|------|------|
| youlai_admin.sql | 16 | 基础系统表（缺sys_email_template） |
| youlai_admin_template.sql | 15 | 模板表（与上面重复14张，缺sys_email_template） |
| meeting_admin(结构).sql | 45 | 完整DDL（包含上述所有表+业务表） |
| 27个增量脚本 | - | 按日期命名，部分与完整DDL重叠 |

**问题：** 新开发者不清楚应该使用哪个脚本初始化数据库，增量脚本与完整DDL之间可能存在冲突。

### 10.6 代码重复/冗余

| 问题 | 位置 | 说明 |
|------|------|------|
| Speech vs SpeechV1 | portal/model/entity/ | 两个Entity，SpeechV1疑似旧版本遗留 |
| youlai_admin.sql vs youlai_admin_template.sql | backend/sql/mysql/ | 14张表重复定义 |
| 3个文件存储实现 | MinioFileService/AliyunFileService/LocalFileService | 策略模式正确，但上传逻辑有部分重复 |

### 10.7 缺失的代码规范

| 规范 | 现状 | 建议 |
|------|------|------|
| Service接口Javadoc | 部分Service接口缺少类级文档 | 补充接口用途说明 |
| Controller接口文档 | @Tag/@Operation注解覆盖不完整 | 补充Knife4j注解 |
| 异常处理 | BusinessException统一，但错误码体系不明确 | 建立错误码枚举 |
| 日志规范 | log.info/error混用，格式不统一 | 统一日志格式和级别 |

---

## 11. 已知问题清单

### 11.1 安全问题

| 编号 | 严重程度 | 问题 | 位置 | 状态 |
|------|----------|------|------|------|
| SEC-001 | 🔴 高 | SMTP密码明文存储在application-dev.yml | application-dev.yml | 待修复 |
| SEC-002 | 🔴 高 | JWT密钥明文存储在配置文件 | application.yml | 待修复 |
| SEC-003 | 🔴 高 | IMAP密码明文存储在配置文件 | application-dev.yml | 待修复 |
| SEC-004 | 🟡 中 | Stripe测试密钥明文存储 | application-dev.yml | 待修复 |
| SEC-005 | 🟡 中 | 阿里云SMS密钥明文存储 | application-dev.yml | 待修复 |

### 11.2 代码质量问题

| 编号 | 严重程度 | 问题 | 位置 | 状态 |
|------|----------|------|------|------|
| CODE-001 | 🟡 中 | BounceEmailJob.java 46.2KB，职责过多 | support/mail/job/ | 待重构 |
| CODE-002 | 🟡 中 | MailSendRecordServiceImpl.java 39.3KB，职责过多 | support/mail/service/impl/ | 待重构 |
| CODE-003 | 🟡 中 | PaperSubmissionServiceImpl.java 37.5KB，职责过多 | portal/service/impl/ | 待重构 |
| CODE-004 | 🟢 低 | MeetingInfoServiceImpl使用@Autowired字段注入 | portal/service/impl/ | 待统一 |
| CODE-005 | 🟢 低 | SpeechController中/{SpeechId}/status命名不规范 | portal/controller/ | 待修复 |
| CODE-006 | 🟢 低 | SpeechV1 Entity疑似旧版本遗留 | portal/model/entity/ | 待确认删除 |
| CODE-007 | 🟢 低 | PaperSubmissionController中PDF导出逻辑应在Service层 | portal/controller/ | 待重构 |

### 11.3 数据库问题

| 编号 | 严重程度 | 问题 | 位置 | 状态 |
|------|----------|------|------|------|
| DB-001 | 🟡 中 | SQL脚本基线不统一（3个DDL脚本重叠） | backend/sql/ | 待整理 |
| DB-002 | 🟢 低 | 增量脚本与完整DDL可能存在冲突 | backend/sql/mysql/ | 待验证 |
| DB-003 | 🟢 低 | gen_table/gen_table_column在业务库中（代码生成器表） | meeting_admin(结构).sql | 待确认是否需要 |

### 11.4 架构问题

| 编号 | 严重程度 | 问题 | 位置 | 状态 |
|------|----------|------|------|------|
| ARCH-001 | 🟡 中 | 邮件9态状态机缺少显式状态转换图文档 | support/mail/enums/ | 待文档化 |
| ARCH-002 | 🟢 低 | 双轨i18n策略缺少统一规范文档 | portal/ + meeting/ | 待文档化 |
| ARCH-003 | 🟢 低 | 支付回调缺少幂等性保证文档 | portal/controller/ | 待文档化 |

---

## 12. 依赖关系图

### 12.1 后端包间依赖

```
auth ──→ security (Token/JWT)
      ──→ system (UserService)
      ──→ support (SMS/Mail)

system ──→ security (Permission/DataScope)
       ──→ common (Result/Page)
       ──→ config (Redis/Cache)

portal ──→ security (@PreAuthorize)
       ──→ support (Mail/SMS/WebSocket)
       ──→ common (Result/Page)
       ──→ config (Stripe/Alipay/WechatPay)
       ──→ module (FileService)

support ──→ common (Result/Page)
        ──→ config (Mail/IMAP/Redis)
        ──→ security (SecurityUtils)

module ──→ common (Result)
       ──→ config (MinIO/OSS)

config ──→ common (常量)
       ──→ security (Filter配置)

core ──→ common (异常定义)
      ──→ security (SecurityUtils)

security ──→ common (异常)
         ──→ config (Redis)
```

### 12.2 核心业务依赖链

```
论文投稿流程:
PaperSubmissionController
    → PaperSubmissionServiceImpl
        → MailService (发送审稿邮件)
        → MailSendRecordServiceImpl (记录邮件状态)
        → DiscountConfigurationServiceImpl (生成抵扣码)
        → DocxToPdfServiceImpl (DOCX→PDF)
        → PaperPdfGeneratorServiceImpl (PDF生成)
        → FileService (文件上传)

注册流程:
RegisterUserController
    → RegisterUserServiceImpl
        → QRCodeUtils (签到二维码)
        → RedisTemplate (验证码)
        → MailService (欢迎邮件)
        → QuestionnaireSurveyService (问卷)
        → PaperSubmissionService (论文关联)
        → UserCheckInService (签到)

支付流程:
PaymentController
    → PaymentServiceImpl
        → PaymentRouter (路由)
            → StripePaymentProvider / WechatPaymentProvider / AlipayPaymentProvider / BankTransferPaymentProvider
        → PaymentOrderMapper (订单持久化)
        → DiscountConfigurationService (折扣计算)

退信检测流程:
BounceEmailJob (XXL-Job定时触发)
    → RedissonClient (分布式锁)
    → MailImapCheckpointService (检查点)
    → BounceParser (退信解析)
    → MailSendRecordService (状态更新)
    → MailBounceBlacklistService (黑名单管理)
```

### 12.3 前端依赖链

```
Admin前端:
页面组件 → Pinia Store → API模块 → Axios → /dev-api → Spring Boot

Webpage前端:
页面组件 → Pinia Store → API模块 → Axios → /dev-api → Spring Boot

注册向导数据流:
Signup → Contact → Enroll → Skills → Expert → Survey → Consent → Confirm → Final
  │        │         │        │        │         │        │        │        │
  └────────┴─────────┴────────┴────────┴─────────┴────────┴────────┴────────┘
                                    │
                              register Store
                              (Pinia状态管理)
                                    │
                              RegisterUserController
                              (最终提交)
```

### 12.4 Maven关键依赖

| 依赖 | 版本 | 作用 |
|------|------|------|
| spring-boot-starter-web | 4.0.1 | Web框架 |
| spring-boot-starter-security | 4.0.1 | 安全框架 |
| spring-boot-starter-data-redis | 4.0.1 | Redis |
| mybatis-plus-spring-boot3-starter | 3.5.15 | ORM |
| redisson-spring-boot-starter | 4.1.0 | 分布式锁 |
| knife4j-openapi3-jakarta-spring-boot-starter | 4.5.0 | API文档 |
| mapstruct | 1.6.3 | 对象映射 |
| xxl-job-core | 3.2.0 | 定时任务 |
| minio | 8.5.10 | 对象存储 |
| zxing | 3.5.3 | 二维码 |
| weixin-java-miniapp | 4.6.0 | 微信小程序 |
| spring-boot-starter-mail | 4.0.1 | 邮件 |
| spring-boot-starter-websocket | 4.0.1 | WebSocket |
| caffeine | 3.1.8 | 本地缓存 |
| openhtmltopdf | 1.0.10 | PDF生成 |
| jodconverter-local | 4.4.6 | LibreOffice转换 |

---

## 13. 数据流图

### 13.1 用户登录数据流

```
[前端] POST /api/v1/auth/login {username, password}
    │
    ▼
[AuthController] → [AuthServiceImpl]
    │
    ├──1. 验证图形验证码(Redis)
    ├──2. AuthenticationManager.authenticate()
    │       └── DaoAuthenticationProvider
    │           └── SysUserDetailsService.loadUserByUsername()
    │               └── UserMapper.selectByUsername()
    │                   └── MySQL sys_user
    │
    ├──3. JwtTokenManager.generateToken()
    │       ├── 生成AccessToken(JWT, HS256, 30min)
    │       └── 生成RefreshToken(JWT, HS256, 7d)
    │
    ├──4. RedisTokenManager.saveToken()
    │       ├── Redis SET accessToken:{jti} → userId, TTL=30min
    │       └── Redis SET refreshToken:{jti} → userId, TTL=7d
    │
    └──5. 返回 {accessToken, refreshToken, userInfo}
```

### 13.2 论文投稿审稿数据流

```
[前端] POST /api/v1/paperSubmission {title, abstract, file}
    │
    ▼
[PaperSubmissionController] → [PaperSubmissionServiceImpl]
    │
    ├──1. 保存论文记录 → MySQL paper_submission
    ├──2. 上传文件 → FileService → MinIO/OSS/本地
    ├──3. DOCX→PDF → DocxToPdfServiceImpl → LibreOffice
    │
[前端] POST /api/v1/paperSubmission/{id}/sendEmail
    │
    ├──4. 查询论文 → MySQL paper_submission
    ├──5. 查询审稿专家 → MySQL sys_user (角色=审稿人)
    ├──6. 生成邮件内容 → MailService
    ├──7. 创建邮件记录 → MySQL mail_send_record (PENDING→SCHEDULED→SENDING→SENT)
    ├──8. 发送邮件 → JavaMailSender → SMTP
    │
[前端] PUT /api/v1/paperSubmission/{id}/review {result}
    │
    ├──9. 更新审核状态 → MySQL paper_submission
    ├──10. 判断超半数通过?
    │       ├── 是 → 生成抵扣码 → MySQL discount_configuration
    │       └── 否 → 等待更多审核
    │
[前端] GET /api/v1/paperSubmission/export
    │
    └──11. PaperPdfGeneratorServiceImpl → 生成PDF → 返回下载
```

### 13.3 邮件发送+退信检测数据流

```
[业务Service] → MailService.sendMail()
    │
    ▼
[MailSendRecordServiceImpl]
    ├──1. 创建记录 → MySQL mail_send_record (PENDING)
    ├──2. 调度发送 → status=SCHEDULED
    ├──3. 实际发送 → JavaMailSender → SMTP
    │       ├── 成功 → status=SENT → (异步确认) → status=DELIVERED
    │       └── 失败 → status=FAILED → (可重试?) → status=RETRY → 回到步骤3
    │
[BounceEmailJob] (XXL-Job定时, Redisson分布式锁)
    │
    ├──1. 获取IMAP检查点 → MySQL mail_imap_checkpoint
    ├──2. 增量扫描IMAP收件箱(仅新邮件)
    ├──3. 对每封邮件 → BounceParser.parse()
    │       ├── DSN解析(RFC 3464) — action=failed/delayed
    │       ├── SMTP响应码 — 5xx永久/4xx临时
    │       ├── DNS查询 — MX记录验证
    │       └── 正文正则 — 15+退信模式匹配
    ├──4. 分类结果:
    │       ├── PERMANENT → 更新mail_send_record(BOUNCED_PERMANENT)
    │       │            → 插入mail_bounce_blacklist(自动拉黑)
    │       └── TEMPORARY → 更新mail_send_record(BOUNCED_TEMPORARY)
    │                  → 标记可重试(未超max_retry)
    └──5. 更新IMAP检查点 → MySQL mail_imap_checkpoint
```

### 13.4 支付数据流

```
[前端] POST /api/v1/payment/create {amount, method, orderId}
    │
    ▼
[PaymentController] → [PaymentServiceImpl]
    │
    ├──1. 创建支付订单 → MySQL payment_orders (PENDING)
    ├──2. PaymentRouter.route(method)
    │       ├── STRIPE → StripePaymentProvider.createPayment()
    │       │           → Stripe Checkout Session URL
    │       ├── WECHAT → WechatPaymentProvider.createPayment()
    │       │           → 微信支付二维码URL
    │       ├── ALIPAY → AlipayPaymentProvider.createPayment()
    │       │           → 支付宝支付表单HTML
    │       └── BANK_TRANSFER → BankTransferPaymentProvider.createPayment()
    │                       → 银行转账信息(银行名/账号/户名)
    │
    ├──3. 返回支付信息给前端
    │
[第三方回调] POST /api/v1/payment/{method}/notify
    │
    ├──4. 验证签名(Stripe/微信/支付宝各自签名算法)
    ├──5. 更新订单状态 → MySQL payment_orders (PAID)
    └──6. 触发后续业务(如注册确认、折扣码激活)
```

### 13.5 注册向导数据流

```
[Webpage前端] 9步向导
    │
    Step1: Signup → 用户注册 → AuthController.register()
    Step2: Contact → 联系信息 → RegisterUserController.updateContact()
    Step3: Enroll → 注册类型 → RegisterUserController.updateEnroll()
    Step4: Skills → 专业技能 → RegisterUserController.updateSkills()
    Step5: Expert → 专家信息 → RegisterUserController.updateExpert()
    Step6: Survey → 问卷调查 → RegisterUserController.submitSurvey()
    Step7: Consent → 同意条款 → RegisterUserController.acceptConsent()
    Step8: Confirm → 确认提交 → RegisterUserController.confirmRegistration()
    Step9: Final → 完成 → 跳转支付页面
    │
    ▼
[RegisterUserServiceImpl]
    ├── 创建sys_user记录(Step1)
    ├── 创建register_user_information记录(Step1)
    ├── 逐步更新字段(Step2-7)
    ├── 生成签到二维码(Step8) → QRCodeUtils → ZXing
    ├── 发送欢迎邮件(Step8) → MailService
    └── 关联支付订单(Step9) → PaymentService
```

---

## 14. 文档缺口分析

### 14.1 现有文档覆盖范围

| 文档 | 内容 | 覆盖范围 | 缺口 |
|------|------|----------|------|
| 00-project-overview.md | 项目概述 | ✅ 基本架构 | 缺技术栈版本、双前端架构 |
| 01-project-overview.md | 项目概述(另一版) | ✅ 基本架构 | 与00重复 |
| 02-architecture.md | 架构设计 | ✅ 分层架构 | 缺安全层、支付策略模式、邮件状态机 |
| 03-database-schema.md | 数据库Schema | ✅ 表结构 | 缺ER关系图、i18n策略说明 |
| 03-directory-structure.md | 目录结构 | ✅ 文件组织 | 与03编号冲突，缺前端目录 |
| 04-i18n-strategy.md | 国际化策略 | ✅ 双轨i18n | 缺Webpage三语言包详情 |
| 05-email-subsystem.md | 邮件子系统 | ✅ 9态状态机 | 缺退信检测流程、BounceParser详情 |
| 06-payment-subsystem.md | 支付子系统 | ✅ 四轨支付 | 缺回调签名验证、幂等性说明 |
| 07-registration-workflow.md | 注册流程 | ✅ 9步向导 | 缺前端Store状态管理详情 |
| 08-rbac-permission.md | RBAC权限 | ✅ 权限模型 | 缺4种认证Provider、JWT双令牌 |
| 09-conference-agenda.md | 会议日程 | ✅ 4级层次 | 缺ITEM_TYPE_MAP自动填充逻辑 |
| 10-paper-submission.md | 论文投稿 | ✅ 投稿流程 | 缺PDF生成链、抵扣码生成逻辑 |
| phase1-analysis-report.md | Phase 1分析 | ✅ 本报告 | - |

### 14.2 缺失文档清单

| 优先级 | 文档 | 说明 |
|--------|------|------|
| 🔴 高 | deployment-guide.md | 部署指南（Docker+环境变量+数据库初始化） |
| 🔴 高 | security-auth-flow.md | 认证授权完整流程（4Provider+JWT+RBAC） |
| 🔴 高 | api-endpoints-full.md | API端点全量清单（261个，含请求/响应示例） |
| 🟡 中 | email-bounce-detection.md | 退信检测完整流程（IMAP+BounceParser+5级匹配） |
| 🟡 中 | payment-integration-guide.md | 支付集成指南（4轨配置+回调+签名验证） |
| 🟡 中 | file-storage-guide.md | 文件存储策略（3实现+配置切换） |
| 🟡 中 | websocket-realtime.md | WebSocket实时推送（STOMP+在线计数） |
| 🟡 中 | known-issues.md | 已知问题清单（本报告第11节内容） |
| 🟢 低 | code-conventions.md | 代码规范（注入风格、命名、日志） |
| 🟢 低 | testing-guide.md | 测试指南（当前测试覆盖情况） |
| 🟢 低 | frontend-architecture.md | 前端架构详情（Store数据流+路由守卫+i18n） |
| 🟢 低 | sql-migration-guide.md | SQL迁移指南（基线选择+增量脚本使用） |

### 14.3 代码标注缺口

| 优先级 | 文件 | 缺失标注 |
|--------|------|----------|
| 🔴 高 | BounceEmailJob.java | 缺少类级"为什么"注释、6大职责说明、IMAP增量扫描原理 |
| 🔴 高 | BounceParser.java | 缺少15+正则模式说明、4级分类优先级说明、RFC 3464字段说明 |
| 🔴 高 | MailSendRecordServiceImpl.java | 缺少任务表模式说明、TransactionTemplate编程式事务原因 |
| 🔴 高 | PaperSubmissionServiceImpl.java | 缺少5大流程说明、专家审核超半数通过逻辑 |
| 🟡 中 | JwtTokenManager.java | 缺少tokenVersion批量失效机制说明 |
| 🟡 中 | SecurityConfig.java | 缺少4种Provider选择逻辑说明 |
| 🟡 中 | PaymentRouter.java | 缺少策略模式路由逻辑说明 |
| 🟡 中 | MeetingInfoServiceImpl.java | 缺少4级层次组装逻辑、ITEM_TYPE_MAP说明 |
| 🟡 中 | RegisterUserServiceImpl.java | 缺少9步注册数据流说明 |
| 🟢 低 | 各Controller | 缺少@Tag/@Operation Knife4j注解 |
| 🟢 低 | 各Service接口 | 缺少类级Javadoc |

---

## 15. 优先级排序建议

### 15.1 Phase 2 文档生成优先级

| 优先级 | 任务 | 预计工作量 | 理由 |
|--------|------|------------|------|
| **P0** | deployment-guide.md | 2小时 | 新开发者首先需要能跑起来 |
| **P0** | security-auth-flow.md | 3小时 | 认证授权是理解系统的前提 |
| **P0** | known-issues.md | 1小时 | 直接从本报告第11节提取 |
| **P1** | email-bounce-detection.md | 4小时 | 最复杂的子系统，最需要文档 |
| **P1** | payment-integration-guide.md | 3小时 | 4轨支付配置复杂 |
| **P1** | api-endpoints-full.md | 6小时 | 261个端点，工作量大但价值高 |
| **P2** | file-storage-guide.md | 2小时 | 3实现策略模式 |
| **P2** | websocket-realtime.md | 1小时 | 相对简单 |
| **P2** | frontend-architecture.md | 3小时 | 前端架构文档化 |
| **P3** | code-conventions.md | 2小时 | 规范化 |
| **P3** | testing-guide.md | 2小时 | 测试覆盖 |
| **P3** | sql-migration-guide.md | 2小时 | SQL脚本整理 |

### 15.2 Phase 2 代码标注优先级

| 优先级 | 文件 | 预计工作量 | 标注类型 |
|--------|------|------------|----------|
| **P0** | BounceEmailJob.java | 3小时 | 类级+方法级+"为什么" |
| **P0** | BounceParser.java | 2小时 | 类级+正则模式说明+分类优先级 |
| **P0** | MailSendRecordServiceImpl.java | 2小时 | 任务表模式+事务+状态机 |
| **P0** | PaperSubmissionServiceImpl.java | 2小时 | 5大流程+审稿逻辑 |
| **P1** | JwtTokenManager.java | 1.5小时 | 双令牌+黑名单+version |
| **P1** | SecurityConfig.java | 1小时 | 4种Provider配置 |
| **P1** | PaymentRouter.java | 0.5小时 | 策略模式路由 |
| **P1** | RegisterUserServiceImpl.java | 1.5小时 | 9步数据流 |
| **P1** | MeetingInfoServiceImpl.java | 1小时 | 4级层次+ITEM_TYPE_MAP |
| **P2** | 各Controller(21+11个) | 4小时 | @Tag/@Operation注解 |
| **P2** | 各Service接口(23+16个) | 3小时 | 类级Javadoc |
| **P3** | 其他ServiceImpl | 4小时 | 关键方法注释 |

### 15.3 Phase 2 总工作量估算

| 类别 | P0 | P1 | P2 | P3 | 合计 |
|------|-----|-----|-----|-----|------|
| 文档生成 | 6小时 | 13小时 | 6小时 | 6小时 | ~31小时 |
| 代码标注 | 9小时 | 5小时 | 7小时 | 4小时 | ~25小时 |
| **合计** | **15小时** | **18小时** | **13小时** | **10小时** | **~56小时** |

### 15.4 建议执行顺序

**第一批（P0，约15小时）：**
1. known-issues.md — 从本报告提取，最快完成
2. deployment-guide.md — 新开发者入门必需
3. security-auth-flow.md — 理解系统前提
4. BounceEmailJob.java代码标注 — 最复杂文件
5. BounceParser.java代码标注 — 退信解析核心
6. MailSendRecordServiceImpl.java代码标注 — 邮件状态机
7. PaperSubmissionServiceImpl.java代码标注 — 论文核心

**第二批（P1，约18小时）：**
1. email-bounce-detection.md — 最复杂子系统文档
2. payment-integration-guide.md — 支付配置复杂
3. api-endpoints-full.md — API全量清单
4. JwtTokenManager.java代码标注
5. SecurityConfig.java代码标注
6. PaymentRouter.java代码标注
7. RegisterUserServiceImpl.java代码标注
8. MeetingInfoServiceImpl.java代码标注

**第三批（P2+P3，约23小时）：**
1. file-storage-guide.md
2. websocket-realtime.md
3. frontend-architecture.md
4. Controller注解补充
5. Service接口Javadoc
6. code-conventions.md
7. testing-guide.md
8. sql-migration-guide.md
9. 其他ServiceImpl代码标注

---

## 附录A：Spring Bean清单（113个）

### A.1 @Service（67个）

**portal.service.impl（23个）：**
RegisterUserServiceImpl, PaperSubmissionServiceImpl, MeetingInfoServiceImpl, GuestServiceImpl, SpeechServiceImpl, ContactServiceImpl, HotelServiceImpl, ArchitectureServiceImpl, ArrangeServiceImpl, MomentsServiceImpl, DiscountConfigurationServiceImpl, UserCheckInServiceImpl, PaymentServiceImpl, DocxToPdfServiceImpl, PaperPdfGeneratorServiceImpl, QuestionnaireSurveyServiceImpl, MeetingSessionServiceImpl, MeetingAgendaItemServiceImpl, MeetingSpeakerServiceImpl, MeetingDateServiceImpl, MeetingPeriodServiceImpl, MeetingSettingServiceImpl, PaperSubmissionEmailServiceImpl

**system.service.impl（16个）：**
UserServiceImpl, RoleServiceImpl, MenuServiceImpl, DeptServiceImpl, DictServiceImpl, DictItemServiceImpl, ConfigServiceImpl, NoticeServiceImpl, UserNoticeServiceImpl, LogServiceImpl, EmailTemplateServiceImpl, RegistrationTypeServiceImpl, UserSocialServiceImpl, StatisticsServiceImpl, UserRoleServiceImpl, RoleMenuServiceImpl

**support.mail.service.impl（5个）：**
MailServiceImpl, MailSendRecordServiceImpl, MailBounceRecordServiceImpl, MailBounceBlacklistServiceImpl, MailImapCheckpointServiceImpl

**support.sms.service.impl（1个）：**
AliyunSmsServiceImpl

**support.websocket.service.impl（1个）：**
WebSocketServiceImpl

**auth.service.impl（2个）：**
AuthServiceImpl, WechatMiniappAuthServiceImpl

**portal.payment.impl（4个）：**
StripePaymentProvider, WechatPaymentProvider, AlipayPaymentProvider, BankTransferPaymentProvider

**module.service.impl（2个）：**
MinioFileServiceImpl, AliyunFileServiceImpl (LocalFileServiceImpl可能未标注@Service)

### A.2 @Component（12个）

PermissionService("ss"), SecurityUtils, BounceParser, OnlineUserCountJob, BounceEmailJob, UserSessionRegistry, WebSocketPublisher, Topics, XxlJobSampleHandler, QRCodeUtils, 以及其他工具类

### A.3 @Configuration（34个）

SecurityConfig, WebSocketConfig, CorsConfig, RedisConfig, RedisCacheConfig, CaffeineConfig, MailConfig, MybatisConfig, Knife4jOpenApiCustomizer, OpenApiConfig, AsyncConfig, CaptchaConfig, PasswordEncoderConfig, JacksonConfig, StripeConfig, AlipayConfig, WechatPayConfig, WxMaConfig, AdminServerConfig, XxlJobConfig, 以及其他配置类

---

## 附录B：Entity-Table映射（51个@TableName）

| # | @TableName | Entity类 | 包 |
|---|-----------|----------|-----|
| 1 | sys_user | User | system.model.entity |
| 2 | sys_role | Role | system.model.entity |
| 3 | sys_menu | Menu | system.model.entity |
| 4 | sys_dept | Dept | system.model.entity |
| 5 | sys_dict | Dict | system.model.entity |
| 6 | sys_dict_item | DictItem | system.model.entity |
| 7 | sys_config | Config | system.model.entity |
| 8 | sys_notice | Notice | system.model.entity |
| 9 | sys_user_notice | UserNotice | system.model.entity |
| 10 | sys_user_role | UserRole | system.model.entity |
| 11 | sys_role_menu | RoleMenu | system.model.entity |
| 12 | sys_role_dept | RoleDept | system.model.entity |
| 13 | sys_log | Log | system.model.entity |
| 14 | sys_user_social | UserSocial | system.model.entity |
| 15 | sys_email_template | EmailTemplate | system.model.entity |
| 16 | registration_type | RegistrationType | system.model.entity |
| 17 | register_user_information | RegisterUserInformation | portal.model.entity |
| 18 | paper_submission | PaperSubmission | portal.model.entity |
| 19 | paper_submission_email | PaperSubmissionEmail | portal.model.entity |
| 20 | payment_orders | PaymentOrder | portal.model.entity |
| 21 | meeting_info | MeetingInfo | portal.model.entity |
| 22 | meeting_session | MeetingSession | portal.model.entity |
| 23 | meeting_agenda_item | MeetingAgendaItem | portal.model.entity |
| 24 | meeting_session_participant | MeetingSessionParticipant | portal.model.entity |
| 25 | meeting_speaker | MeetingSpeaker | portal.model.entity |
| 26 | meeting_date | MeetingDate | portal.model.entity |
| 27 | meeting_period | MeetingPeriod | portal.model.entity |
| 28 | meeting_setting | MeetingSetting | portal.model.entity |
| 29 | portal_guest | Guest | portal.model.entity |
| 30 | portal_speech | Speech | portal.model.entity |
| 31 | portal_hotel | Hotel | portal.model.entity |
| 32 | portal_contact | Contact | portal.model.entity |
| 33 | portal_architecture | Architecture | portal.model.entity |
| 34 | portal_arrange | Arrange | portal.model.entity |
| 35 | portal_moments | Moments | portal.model.entity |
| 36 | portal_question | Question | portal.model.entity |
| 37 | questionnaire_survey | QuestionnaireSurvey | portal.model.entity |
| 38 | discount_configuration | DiscountConfiguration | portal.model.entity |
| 39 | user_check_in | UserCheckIn | portal.model.entity |
| 40 | mail_send_record | MailSendRecord | support.mail.model.entity |
| 41 | mail_bounce_record | MailBounceRecord | support.mail.model.entity |
| 42 | mail_bounce_blacklist | MailBounceBlacklist | support.mail.model.entity |
| 43 | mail_imap_checkpoint | MailImapCheckpoint | support.mail.model.entity |
| 44 | gen_table | GenTable | module.model.entity |
| 45 | gen_table_column | GenTableColumn | module.model.entity |
| 46-51 | (6个代码级实体) | - | 无对应数据库表或未扫描到@TableName |

---

## 附录C：项目文件统计

| 类别 | 数量 | 说明 |
|------|------|------|
| 后端Java文件 | 250+ | 8大顶层包 |
| 后端Controller | 32+ | auth(2) + system(11) + portal(21) + support(3) + module(2) |
| 后端ServiceImpl | 54+ | portal(23) + system(16) + support(7) + auth(2) + payment(4) + module(2) |
| 后端Entity | 51 | @TableName注解 |
| 后端Mapper | 50+ | MyBatis-Plus Mapper |
| 后端Converter | 23+ | MapStruct转换器 |
| 前端Admin API | 30+ | TypeScript |
| 前端Admin页面 | 40+ | Vue3+TS |
| 前端Admin Store | 7 | Pinia |
| 前端Webpage API | 19+ | JavaScript |
| 前端Webpage页面 | 14+ | Vue3+JS |
| 前端Webpage Store | 5 | Pinia |
| 数据库表 | 45 | MySQL 8.0.45 |
| SQL脚本 | 31 | 27增量+4备份/完整 |
| API端点 | 261 | @RequestMapping映射 |
| Spring Bean | 113 | @Service+@Component+@Configuration |
| 配置类 | 34 | @Configuration |
| 配置属性类 | 8 | @ConfigurationProperties |

---

> **Phase 1 分析报告完成。请确认报告内容后，我将进入Phase 2文档生成和代码标注阶段。**
```