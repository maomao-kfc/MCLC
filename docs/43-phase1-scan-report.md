# MCLC 项目全量扫描报告（Phase 1）

> **扫描时间**: 2026-10-29  
> **数据来源**: 实际代码文件 + git 提交历史 + 数据库 SQL 备份  
> **用途**: 知识库建设基础，后续 Phase 2-7 均以本报告为事实基线  
> **原则**: 一切以实际代码为准，严禁编造；区分【已实现】【已验证】【曾经发生】【计划改进】

---

## 1. 项目定位与规模

| 维度 | 事实 |
|------|------|
| 项目名称 | MCLC 国际数学与计算语言学大会管理系统 |
| 系统构成 | 三端：后端 API(backend) + 管理端(frontend) + 门户端(webpage) |
| 后端规模 | Java 文件约 280+，portal 包 24 Controller / 26 Service / 27 Mapper |
| 管理端规模 | Vue3+TS，30+ 页面视图，25+ 通用组件，19 portal API + 13 system API |
| 门户端规模 | Vue3+JS，15 页面 + 9 步注册流程，i18n 中英双语(zh.js 47.4KB / en.js 53.8KB) |
| 数据库 | MySQL，37 张表（本地实测） |
| 代码生成 | 内置 codegen 模块，支持前后端代码一键生成 |

---

## 2. 技术栈全景（pom.xml / package.json 实测）

### 2.1 后端（backend/pom.xml）

| 类别 | 技术 | 版本 | 用途 |
|------|------|------|------|
| 框架 | Spring Boot | 4.0.1 | 主框架 |
| 语言 | Java | 17 | 编译级别 |
| ORM | MyBatis-Plus | 3.5.15 | 数据访问 |
| 数据库驱动 | MySQL Connector | 9.1.0 | MySQL 连接 |
| 连接池 | Druid | 1.2.28 | 数据库连接池 |
| 缓存 | Redisson | 4.1.0 | 分布式缓存/锁 |
| 本地缓存 | Caffeine | 2.9.3 | 二级缓存 |
| 定时任务 | XXL-Job | 3.2.0 | 退信轮询等 |
| 对象存储 | MinIO | 8.5.10 | 文件存储 |
| 云存储 | 阿里云 OSS | - | 备选文件存储 |
| OCR | Tess4j | 5.13.0 | 支付凭证 OCR |
| 二维码 | ZXing | 3.5.3 | 签到二维码 |
| 对象映射 | MapStruct | 1.6.3 | Entity↔VO/Form 转换 |
| API 文档 | Knife4j | 4.5.0 | OpenAPI 增强 |
| 工具库 | Hutool | 5.8.41 | 通用工具 |
| 线程上下文 | TransmittableThreadLocal | 2.14.5 | 异步线程上下文传递 |
| 监控 | Spring Boot Admin | 4.0.2 | 应用监控 |
| PDF | JODConverter + LibreOffice + OpenPDF | - | DOCX→PDF 转换 |
| 支付 | 支付宝 SDK / 微信支付 / Stripe | - | 多渠道支付 |

### 2.2 管理端（frontend/package.json）

| 技术 | 用途 |
|------|------|
| Vue 3 + TypeScript | 框架 |
| Element Plus | UI 组件库 |
| Pinia | 状态管理 |
| Vue Router | 路由 |
| Axios | HTTP 请求 |
| Vite | 构建工具 |
| UnoCSS | 原子化 CSS |
| ECharts | 图表 |
| WangEditor | 富文本编辑器 |
| SSE (EventSource) | 字典同步/在线人数实时推送 |

### 2.3 门户端（webpage/package.json）

| 技术 | 用途 |
|------|------|
| Vue 3 (JS) | 框架 |
| Vue Router | 路由 |
| Pinia | 状态管理 |
| Axios | HTTP 请求 |
| vue-i18n | 中英双语国际化 |
| Element Plus | UI 组件库（部分） |
| Vite | 构建工具 |

---

## 3. 后端架构与模块索引

```
com.youlai.boot
├── auth/                    # 认证模块（2 Controller）
│   ├── AuthController           # 账号密码登录/验证码/登出
│   └── WechatMiniappAuthController  # 微信小程序登录
├── common/                  # 公共基础
│   ├── annotation/              # @DataPermission @Log @RepeatSubmit @ValidField
│   ├── base/                    # BaseEntity BaseQuery IBaseEnum
│   ├── constant/                # JwtClaimConstants RedisConstants SecurityConstants
│   ├── enums/                   # CaptchaTypeEnum DataScopeEnum EnvEnum LogModuleEnum
│   ├── model/                   # KeyValue Option
│   └── util/                    # DateUtils ExcelUtils IPUtils QRCodeUtils
├── config/                  # 配置类（23 个）
│   ├── SecurityConfig            # Spring Security 配置（8.9KB）
│   ├── WebSocketConfig           # WebSocket + SSE 推送（11.5KB）
│   ├── WechatPayConfig           # 微信支付配置（10.3KB）
│   ├── MyDataPermissionHandler   # 数据权限处理器（10.4KB）
│   ├── MailConfig                # 邮件配置（2.6KB）
│   ├── RedisConfig / RedisCacheConfig  # Redis + 缓存配置
│   ├── CaffeineConfig            # 本地缓存
│   ├── XxlJobConfig              # 定时任务
│   ├── AsyncConfig               # 线程池
│   ├── CorsConfig                # 跨域
│   ├── DruidConfig               # 数据源
│   └── property/ (11 个)         # Alipay/AliyunSms/BankTransfer/Captcha/Codegen/Imap/Mail/Security/Stripe/WechatPay/WxMa
├── core/                    # 核心机制
│   ├── aspect/                   # LogAspect(10KB) RepeatSubmitAspect
│   ├── exception/                # BusinessException GlobalExceptionHandler(11.1KB)
│   ├── filter/                   # RateLimiterFilter RequestLogFilter
│   ├── validator/                # FieldValidator
│   └── web/                      # Result PageResult ResultCode WebResponseWriter
├── module/
│   ├── codegen/              # 代码生成器（16.9KB ServiceImpl）
│   └── file/                 # 文件服务（Local/MinIO/Aliyun 三实现）
├── portal/                  # 门户业务（核心，24 Controller / 26 Service / 27 Mapper）
│   ├── controller/ (24 个)       # 详见 §3.1
│   ├── service/ (26 个)          # 详见 §3.2
│   ├── mapper/ (27 个)           # MyBatis-Plus Mapper
│   ├── converter/ (13 个)        # MapStruct 转换器
│   └── enums/                    # PaymentMethodEnum PaymentStatusEnum
├── support/
│   ├── mail/                # 邮件中心（重点模块，详见 §3.3）
│   ├── sms/                 # 阿里云短信
│   └── websocket/           # WebSocket + SSE 实时推送
└── system/                  # 系统管理（11 Controller / 16 Service）
    ├── controller/ (11 个)       # Config/Dept/Dict/EmailTemplate/Log/Menu/Notice/RegistrationType/Role/Statistics/User
    └── service/ (16 个)          # 含 UserServiceImpl(22.1KB) MenuServiceImpl(17.9KB) RoleServiceImpl(13.4KB)
```

### 3.1 Portal Controller 清单（24 个）

| Controller | 大小 | 职责 |
|-----------|------|------|
| PaperSubmissionController | 17.5KB | 论文投稿（含多文件上传/状态流转/邮件通知） |
| PaperSubmitController | 4.7KB | 论文提交（门户端简化接口） |
| PaymentProofController | 3.8KB | 支付凭证（上传/审核/列表） |
| PaymentReconciliationController | 3.8KB | 对账（批次创建/匹配/确认） |
| PaymentController | 2.8KB | 支付下单（支付宝/微信/Stripe） |
| RegisterUserController | 3.9KB | 注册用户管理 |
| PortalController | 5.6KB | 门户聚合数据 |
| PortalAuthController | 2.0KB | 门户端认证 |
| MeetingScheduleController | 5.1KB | 会议日程 |
| MeetingSettingController | 3.3KB | 会议设置 |
| MeetDateController | 2.9KB | 会议日期 |
| GuestController | 2.9KB | 特邀嘉宾 |
| HotelController | 2.9KB | 住宿酒店 |
| SpeechController | 3.4KB | 演讲信息 |
| ArchitectureController | 3.2KB | 组委会架构 |
| ArrangeController | 3.0KB | 会议安排 |
| ContactController | 3.0KB | 联系方式 |
| MomentsController | 3.0KB | 精彩瞬间 |
| QuestionController | 3.2KB | 常见问题 |
| DiscountConfigurationController | 1.4KB | 优惠配置 |
| UserCheckInController | 1.8KB | 签到 |
| AlipayCallbackController | 1.8KB | 支付宝回调 |
| WechatCallbackController | 2.2KB | 微信支付回调 |
| StripeWebhookController | 838B | Stripe Webhook |

### 3.2 Portal Service 清单（26 个，按业务域分组）

**支付域（方案B核心）**：
| Service | 大小 | 核心职责 |
|---------|------|----------|
| PaymentProofServiceImpl | 25.7KB | 凭证上传(OCR+水印+去重)/审核通过(直接确认paid)/两封邮件机制 |
| PaymentReconciliationServiceImpl | 18.1KB | 对账批次/银行流水匹配/确认支付/线下登记 |
| PaymentServiceImpl | 13.1KB | 多渠道下单(支付宝/微信/Stripe)/回调处理 |

**注册域**：
| Service | 大小 | 核心职责 |
|---------|------|----------|
| RegisterUserServiceImpl | 28.1KB | 注册全流程/状态管理/凭证状态/我的注册查询 |

**论文投稿域**：
| Service | 大小 | 核心职责 |
|---------|------|----------|
| PaperSubmissionServiceImpl | 40.6KB | 投稿提交/多文件管理/审批流转/邮件通知/PDF生成 |
| PaperPdfGeneratorServiceImpl | 11.3KB | DOCX→PDF(JODConverter+LibreOffice) |
| DocxToPdfServiceImpl | 7.3KB | DOCX→PDF 底层转换 |
| TesseractOcrServiceImpl | 4.1KB | Tess4j OCR 识别支付凭证 |

**会议管理域**：
| Service | 大小 | 核心职责 |
|---------|------|----------|
| MeetingInfoServiceImpl | 21.2KB | 会议信息 CRUD |
| MeetingDateServiceImpl | 13.6KB | 会议日期管理 |
| MeetingSettingServiceImpl | 6.0KB | 会议设置 |
| MeetingAgendaItemServiceImpl | 806B | 议程项 |
| MeetingSessionServiceImpl | 650B | 会议场次 |
| MeetingSessionParticipantServiceImpl | 750B | 场次参与者 |
| MeetingSpeakerServiceImpl | 644B | 演讲者 |

**门户内容域**：
| Service | 大小 |
|---------|------|
| GuestServiceImpl | 5.0KB |
| HotelServiceImpl | 4.9KB |
| ArchitectureServiceImpl | 5.4KB |
| ArrangeServiceImpl | 5.2KB |
| ContactServiceImpl | 5.2KB |
| MomentsServiceImpl | 5.0KB |
| QuestionServiceImpl | 5.1KB |
| SpeechServiceImpl | 4.8KB |
| SpeechServiceV1Impl | 2.6KB |
| DiscountConfigurationServiceImpl | 1.5KB |
| UserCheckInServiceImpl | 2.4KB |

### 3.3 邮件中心（support/mail/）——重点模块

| 组件 | 大小 | 职责 |
|------|------|------|
| MailSendRecordServiceImpl | 39.3KB | 邮件发送记录全生命周期：创建/发送/重试/状态机(9态)/父子重试模型 |
| BounceEmailJob | 46.2KB | XXL-Job 定时轮询 IMAP 收件箱，解析退信 |
| BounceParser | 26.6KB | 退信邮件解析（DSN/状态码/退信类型判定） |
| MailServiceImpl | 7.7KB | SMTP 实际发送 |
| MailBounceBlacklistServiceImpl | 5.0KB | 退信黑名单管理 |
| MailBounceRecordServiceImpl | 6.3KB | 退信记录管理 |
| MailImapCheckpointServiceImpl | 3.6KB | IMAP 游标检查点 |

**枚举体系**：
- `MailSendStatus`：9 态状态机（PENDING/SENDING/SENT/DELIVERED/BOUNCED/FAILED/CANCELLED/RETRY_PENDING/SKIPPED）
- `BounceType`：退信类型分类（硬退信/软退信/延迟/自动回复等）
- `DeliveryStatus`：投递状态
- `MailBusinessType`：业务类型（用户注册/论文通过等）
- `ImapExecutionStatus`：IMAP 执行状态

**数据模型**：
- `mail_send_record`：发送记录（含 record_type ORIGINAL/RETRY + retry_sequence + root_record_id 父子重试模型）
- `mail_bounce_record`：退信记录
- `mail_bounce_blacklist`：退信黑名单
- `mail_imap_checkpoint`：IMAP 游标（断点续传）

---

## 4. 管理端架构（frontend/）

### 4.1 页面视图（src/views/）

| 目录 | 页面 | 说明 |
|------|------|------|
| dashboard/ | index.vue | 仪表盘 |
| portal/paymentProof/ | index.vue (11.6KB) | 支付凭证审核【新增】 |
| portal/reconciliation/ | index.vue (12.5KB) | 对账管理【新增】 |
| portal/registerUser → system/registration | index.vue (10.3KB) | 注册管理 |
| mailRecord/ | index.vue (28.9KB) + blacklist.vue (5.6KB) | 邮件中心 |
| paperSubmission/ | index.vue (9.3KB) | 论文投稿管理 |
| portal/meeting/ | index.vue (33.1KB) | 会议管理 |
| portal/setting/ | index.vue (28.9KB) | 门户设置 |
| portal/guest/ | index.vue (17.3KB) | 嘉宾管理 |
| portal/speech/ | index.vue (25.1KB) | 演讲管理 |
| portal/home/ | architecture/moments/speech | 首页内容 |
| portal/hotel/ | index.vue (15.8KB) | 酒店管理 |
| portal/arrange/ | index.vue (11.9KB) | 安排管理 |
| portal/contact/ | index.vue (9.4KB) | 联系方式 |
| portal/answer/ | index.vue (11.3KB) | FAQ 管理 |
| system/ | user/role/menu/dept/dict/config/notice/tenant | RBAC 系统 |
| emailConfiguration/ | index.vue (14.8KB) | 邮件配置 |
| discountConfiguration/ | index.vue (4.2KB) | 优惠配置 |
| checkIn/ | index.vue (5.8KB) | 签到管理 |
| hotelRecords/ | index.vue (8.5KB) | 住宿记录 |
| contribution/ | index.vue (25.7KB) | 投稿管理 |
| payMoney/ | index.vue (5.2KB) | 付费管理 |
| userNum/ | index.vue (9.6KB) | 用户统计 |
| report/ | index.vue (5.1KB) | 报表 |
| profile/ | index.vue (23.8KB) + notice/ | 个人中心 |

### 4.2 API 层（src/api/）

- `api/portal/`（19 个）：paymentProof / reconciliation / registerUser / paperSubmission / mailSendRecord / mailBounceRecord / mailBlacklist / meeting / setting / guest / speech / hotel / architecture / arrange / contact / moments / answer / discountConfiguration / userCheckIn
- `api/system/`（13 个）：user / role / menu / dept / dict / config / notice / log / emailTemplate / registrationType / statistics / tenant / tenant-plan
- `api/hotel/`、`api/auth.ts`、`api/codegen.ts`、`api/file.ts`

### 4.3 通用组件（src/components/ 25+）

CURD 模板（PageContent/PageModal/PageSearch/usePage）、Upload（File/MultiImage/SingleImage）、WangEditor、ECharts、DictSelect/DictTag、SSE（useSse/useDictSync/useOnlineCount）、CommandPalette、TableSelect、IconSelect 等。

---

## 5. 门户端架构（webpage/）

### 5.1 页面（src/pages/ 15 个）

| 页面 | 大小 | 说明 |
|------|------|------|
| Home.vue | 13.1KB | 首页（倒计时/嘉宾/日程预览） |
| Register.vue | 7.3KB | 注册入口（9 步流程导航） |
| register/Signup.vue | 10.3KB | 步骤1：账号信息 |
| register/Contact.vue | 19.2KB | 步骤2：联系方式 |
| register/Survey.vue | 16.5KB | 步骤3：问卷 |
| register/Skills.vue | 11.5KB | 步骤4：技能 |
| register/Enroll.vue | 6.1KB | 步骤5：报名小会 |
| register/Expert.vue | 7.2KB | 步骤6：专家信息 |
| register/Consent.vue | 4.9KB | 步骤7：同意书 |
| register/Confirm.vue | 27.0KB | 步骤8：确认页（含支付/凭证上传/状态展示） |
| register/Final.vue | 4.9KB | 步骤9：完成 |
| Payment.vue | 27.9KB | 支付页（多渠道） |
| MyRegistration.vue | 11.2KB | 我的注册【新增】 |
| Submit.vue | 29.0KB | 论文投稿 |
| FirstDraft.vue | 31.8KB | 初稿提交 |
| ReviewFile.vue | 6.9KB | 审稿文件 |
| Schedule.vue | 32.7KB | 会议日程 |
| Guests.vue | 2.3KB | 特邀嘉宾 |
| Hotel.vue | 2.5KB | 住宿 |
| Faq.vue | 2.9KB | FAQ |
| ArticleList/ArticleDetail | - | 文章 |
| EmailConfiguration | 9.6KB | 邮件配置 |
| PreMeeting | 5.9KB | 会前 |

### 5.2 导航（nav.js 数据驱动）

```
navItems = [home, guests, schedule1, hotel, faq, myRegistration]
```
NavBar.vue v-for 渲染，schedule1 受 showSchedule 控制。

### 5.3 状态管理（stores/）

- `user.js`：门户用户登录态
- `register.js`：9 步注册流程数据
- `lang.js`：语言切换
- `meeting.js`：会议信息
- `guest.js`：嘉宾数据

### 5.4 i18n（locales/）

- zh.js（47.4KB）/ en.js（53.8KB），vue-i18n
- 关键命名空间：nav / register / myRegistration / payment / submit 等

---

## 6. 数据库全景（37 张表，本地实测）

### 6.1 业务核心表（9 张）

| 表名 | 说明 | 关键字段 |
|------|------|----------|
| register_user_information | 注册用户 | proof_status(凭证状态)/payment_status(支付状态)/registration_type |
| payment_orders | 支付订单 | order_no/pay_method/alipay/wechat/stripe |
| payment_proof | 支付凭证【新增】 | proof_file_url/transaction_no/amount/status/watermarked_file_url |
| reconciliation_batch | 对账批次【新增】 | batch_no/status/total_count/matched_count |
| reconciliation_record | 对账记录【新增】 | batch_id/register_user_id/match_status/confirm_status |
| paper_submission | 论文投稿 | title/abstract/status/review_status |
| paper_submission_email | 投稿邮件记录 | submission_id/email_type/send_status |
| questionnaire_survey | 问卷 | register_user_id/answers(JSON) |
| user_check_in | 签到 | register_user_id/check_in_time/qr_code |

### 6.2 门户内容表（16 张）

portal_architecture / portal_arrange / portal_contact / portal_guest / portal_hotel / portal_meeting_date / portal_meeting_period / portal_meeting_setting / portal_moments / portal_question / portal_speech / meeting_info / meeting_session / meeting_session_participant / meeting_speaker / meeting_agenda_item

### 6.3 邮件中心表（4 张）

| 表名 | 说明 |
|------|------|
| mail_send_record | 发送记录（record_type/retry_sequence/root_record_id 父子重试模型） |
| mail_bounce_record | 退信记录 |
| mail_bounce_blacklist | 退信黑名单 |
| mail_imap_checkpoint | IMAP 游标（断点续传） |

### 6.4 系统管理表（14 张）

sys_user / sys_role / sys_menu / sys_dept / sys_dict / sys_dict_item / sys_config / sys_log / sys_notice / sys_user_role / sys_role_menu / sys_role_dept / sys_user_notice / sys_user_social

### 6.5 其他表（4 张）

sys_email_template（邮件模板）/ registration_type（注册类型）/ discount_configuration（优惠配置）/ gen_table + gen_table_column（代码生成）

---

## 7. 核心业务调用链

### 7.1 注册 → 支付 → 凭证 → 审核全链路【方案B核心】

```
门户端注册(9步)
  → RegisterUserServiceImpl.submit()
  → 注册成功 → 确认页(Confirm.vue)
  → 用户支付(支付宝/微信/Stripe) → PaymentController → PaymentServiceImpl
  → 支付回调(AlipayCallback/WechatCallback/StripeWebhook)
  → 或：上传支付凭证 → PaymentProofController.submitProof()
      → TesseractOcrServiceImpl.ocr() [OCR识别金额/交易号]
      → 水印处理 + MD5+dHash去重
      → 发第一封邮件"注册待核对"(sendProofSubmittedEmail)
  → 管理端审核 → PaymentProofController.reviewProof()
      → pass: proof_status=paid + payment_status=paid
      → 发第二封邮件"注册成功"(sendRegistrationSuccessEmail)
      → 或 reject: proof_status=rejected
  → 或：管理端对账 → PaymentReconciliationController.confirmMatch()
      → proof_status=paid → 发第二封邮件
  → 或：线下登记 → offlineRegister() → proof_status=offline_paid → 发第二封邮件
  → 用户"我的注册"页面查询状态(MyRegistration.vue)
```

**凭证状态流转链**：null → proof_submitted(待核验) → paid(审核通过/对账确认) / rejected(驳回) / offline_paid(线下登记)

### 7.2 邮件发送全链路

```
业务触发(注册/投稿/审核)
  → MailSendRecordService.createAndSend(businessType, businessId, receiver, subject, content)
  → 创建 mail_send_record(status=PENDING)
  → MailServiceImpl.send() → SMTP发送
  → 成功: status=SENT
  → 失败: status=FAILED / RETRY_PENDING
  → 重试: 创建子记录(record_type=RETRY, retry_sequence=1/2, root_record_id=父记录ID)
  → XXL-Job定时: BounceEmailJob轮询IMAP收件箱
  → BounceParser解析退信(DSN/状态码)
  → 匹配发送记录 → status=BOUNCED
  → 硬退信 → 加入mail_bounce_blacklist
  → 后续发送跳过黑名单: status=SKIPPED
```

### 7.3 论文投稿全链路

```
门户端Submit.vue → PaperSubmitController
  → PaperSubmissionServiceImpl.submit()
  → 文件上传(MinIO) → DOCX→PDF(JODConverter+LibreOffice)
  → 管理端审批 → status流转
  → 审批通过 → 发邮件通知(paper_submission_email记录)
```

---

## 8. 2026-08-14 后变更清单（git log 实测，21 个 commit）

| # | Commit | 内容 |
|---|--------|------|
| 1 | cdf387b | 基线（docs 生成日） |
| 2-5 | 方案B系列 | 支付凭证上传+手动对账全栈实现 |
| 6-8 | OCR系列 | Tess4j集成/水印/MD5+dHash去重/应付金额补算/交易号提取 |
| 9-10 | 错误处理 | 前端错误消息透传/400修复 |
| 11-12 | 权限/文案 | 权限SQL修复/Redis缓存/文案修改 |
| 13 | 组织非必填 | 注册表单组织字段改为非必填 |
| 14 | 论文投稿 | 测试数据SQL |
| 15 | 审批502 | 论文审批502修复 |
| 16 | 审批拦截BUG | 修复 |
| 17 | 三端打包 | 打包部署 |
| 18 | 邮件排查 | 服务器论文通过邮件未发送排查修复 |
| 19 | 步骤2.1/4.1 | 清理逻辑修复+自愈机制 |
| 20 | 注册流程重构 | 确认页面流程重构(156f454) |
| 21 | i18n+回访机制 | i18n补全+注册成功判定重构(9e75b76) |
| 22 | 导航+两封邮件 | 我的注册入口+待核对邮件(2d8da08) |
| 23 | 邮件中心修复 | record_type缺列+审核通过即成功(d2caa39) |

**新增功能（docs 未覆盖）**：
- 方案B支付凭证系统（payment_proof + reconciliation_batch + reconciliation_record 三表）
- OCR 识别（TesseractOcrServiceImpl）
- 水印 + 去重（MD5 + dHash）
- 两封邮件机制（待核对 + 注册成功）
- 我的注册页面（MyRegistration.vue）
- 注册确认页流程重构（Confirm.vue 区分审核中/已完成/驳回状态）
- 凭证审核通过即确认支付成功（三种触发方式）
- 邮件中心父子重试模型（record_type/retry_sequence/root_record_id）
- 导航栏"我的注册"入口

---

## 9. 现有文档差距分析

### 9.1 现有 docs 目录（20 个文件，2026-08-14 生成）

> **注**：本表记录的是扫描时点（2026-10-29）的 docs 目录状态。扫描完成后 docs 已完成整理重组：
> - 旧文件已按新编号重命名（如 05-email-subsystem.md → 14-email-subsystem.md）
> - 子目录已拍平到根目录（features/ → 10-12, case-studies/ → 20-24, interview/ → 30-32, career/ → 33, learning/ → 40）
> - 重复文件已删除（01-project-overview/phase1-analysis-report/email-case-study/fault-cases/interview-questions/旧版learning-guide/旧版resume-materials）
> - 当前目录结构见 [00-project-overview.md](00-project-overview.md)

| 文件 | 状态 | 需要更新 |
|------|------|----------|
| 00/01-project-overview.md | 过时 | 需补充方案B/OCR/两封邮件 |
| 02-architecture.md | 过时 | 需补充支付凭证/对账模块 |
| 03-database-schema.md | 过时 | 缺 payment_proof/reconciliation 三表 |
| 03-directory-structure.md | 过时 | 缺新增 Service/Controller |
| 04-i18n-strategy.md | 过时 | 缺 myRegistration 命名空间 |
| 05-email-subsystem.md | 过时 | 缺父子重试模型/两封邮件机制 |
| 06-payment-subsystem.md | 严重过时 | 缺方案B全链路 |
| 07-registration-workflow.md | 严重过时 | 缺凭证审核/我的注册/状态重构 |
| 08-rbac-permission.md | 基本准确 | 小幅更新 |
| 09-conference-agenda.md | 基本准确 | 无需更新 |
| 10-paper-submission.md | 部分过时 | 补充审批502修复/邮件排查 |
| api-endpoint-catalog.md | 过时 | 缺凭证/对账 API |
| codebase-map.md | 过时 | 缺新增文件索引 |
| email-case-study.md | 过时 | 缺退信/重试/黑名单案例 |
| fault-cases.md | 严重过时 | 需大幅补充 2026-08-14 后故障 |
| interview-questions.md | 需重建 | 缺方案B/OCR/邮件中心题目 |
| known-issues.md | 需更新 | 补充已知问题 |
| learning-guide.md | 需重建 | 按四阶段重构 |
| phase1-analysis-report.md | 已被本报告替代 | 归档 |
| resume-materials.md | 需重建 | 按新规范重写 |

### 9.2 需新建文档

| 目录 | 文件 | 用途 |
|------|------|------|
| docs/features/ | payment-proof-flow.md | 支付凭证全链路 |
| docs/features/ | reconciliation-flow.md | 对账全链路 |
| docs/features/ | email-lifecycle.md | 邮件9态状态机+重试+退信 |
| docs/features/ | registration-status.md | 注册状态流转图 |
| docs/features/ | ocr-watermark-dedup.md | OCR+水印+去重 |
| docs/features/ | paper-submission-flow.md | 投稿全链路 |
| docs/features/ | two-email-mechanism.md | 两封邮件机制 |
| docs/features/ | i18n-bilingual.md | 中英双语实现 |
| docs/features/ | rbac-data-permission.md | RBAC+数据权限 |
| docs/case-studies/ | 5+ 文件 | 真实故障案例（统一模板A-H） |
| docs/career/ | 4 文件 | 简历素材（区分已实现/已验证/曾经发生/计划改进） |
| docs/interview/ | 13 文件 | MCLC 专属面试题库 |
| docs/learning/ | 多文件 | 四阶段学习路线 |
| docs/ | glossary.md | 术语表 |

---

## 10. 证据缺口与待确认清单

### 10.1 个人贡献确认（简历用，需用户确认）

| # | 问题 | 影响 |
|---|------|------|
| 1 | 项目代码主要由 AI 辅助生成，用户承担什么角色？ | 简历不能默认写"独立开发" |
| 2 | 用户是否参与了需求分析/架构设计/技术选型？ | 决定简历定位（技术负责人/全栈开发/需求分析+测试） |
| 3 | 用户是否亲自排查过线上故障？哪些？ | 故障案例的"我做了什么"部分 |
| 4 | 用户是否与甲方/产品有过沟通？ | 可体现沟通能力 |
| 5 | 用户对部署运维（Docker/服务器）的参与度？ | 决定是否写 DevOps 经验 |

### 10.2 技术细节待验证（Phase 2 深度复盘时确认）

| # | 待验证项 | 验证方式 |
|---|----------|----------|
| 1 | BounceEmailJob 的 IMAP 轮询频率和断点续传机制 | 读代码 46.2KB |
| 2 | MailSendRecordServiceImpl 的 9 态状态机完整流转 | 读代码 39.3KB |
| 3 | PaymentReconciliationServiceImpl 对账匹配算法 | 读代码 18.1KB |
| 4 | TesseractOcrServiceImpl OCR 识别准确率和降级策略 | 读代码 4.1KB |
| 5 | PaperSubmissionServiceImpl 审批流转完整状态 | 读代码 40.6KB |
| 6 | SecurityConfig 白名单/权限控制细节 | 读代码 8.9KB |
| 7 | WebSocketConfig SSE 推送实现 | 读代码 11.5KB |
| 8 | MyDataPermissionHandler 数据权限 SQL 注入机制 | 读代码 10.4KB |

### 10.3 已知问题（从对话历史和代码中提取）

| # | 问题 | 状态 | 证据 |
|---|------|------|------|
| 1 | 邮件中心 mail_send_record 表曾缺 record_type 等三列 | 已修复(ALTER TABLE) | 对话历史 2026-10 |
| 2 | 凭证审核通过后未自动确认支付成功 | 已修复(reviewProof直接设paid) | PaymentProofServiceImpl |
| 3 | 论文通过邮件服务器未发送 | 已修复 | 对话历史 |
| 4 | 论文审批 502 | 已修复 | 对话历史 |
| 5 | 权限菜单 type=B 才能挂 perm | 已确认(设计约束) | project_perm_design.md |
| 6 | Redis 后端用 db 12 而非默认 db 0 | 已确认(环境配置) | project_local_env.md |
| 7 | SMTP 发送成功≠邮件最终送达 | 已确认(AGENTS.md 规范) | AGENTS.md |

---

## 11. Phase 2-7 执行计划

| Phase | 内容 | 交付物 |
|-------|------|--------|
| 2 | 核心模块深度复盘 | 逐个读取大文件，产出 features/ 链路文档 |
| 3 | 故障案例库 | docs/case-studies/ 5+ 文件（统一模板） |
| 4 | 简历素材 | docs/career/ 4 文件（待用户确认贡献后定稿） |
| 5 | 面试题库 | docs/interview/ 13 文件 |
| 6 | 学习路线 | docs/learning/ 四阶段课程 |
| 7 | 一致性检查 | 术语表 + 交叉引用校验 + README 更新 |

---

*本报告为 Phase 1 交付物，后续所有文档均以本报告为事实基线。如有与代码不符之处，以代码为准。*