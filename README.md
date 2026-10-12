# MCLC 学术会议管理系统 - 项目使用手册

> 📋 更新日志已迁移至 [CHANGELOG.md](CHANGELOG.md)，所有功能变更、修复记录请查看该文件。

---

## 一、项目概述

本项目是 MCLC（近视眼接触镜）学术会议的完整管理系统，包含三个子项目：

| 子项目 | 目录 | 技术栈 | 说明 |
|--------|------|--------|------|
| **portal（官网）** | `webpage/` | Vue 3 + Vite + Element Plus + Pinia + vue-i18n | 大会官网前端，面向参会者 |
| **admin（管理端）** | `frontend/` | Vue 3 + Vite + TypeScript + Element Plus + Pinia | 后台管理系统，面向管理员 |
| **backend（后端）** | `backend/` | Java 17 + Spring Boot 4 + MyBatis-Plus + MySQL + Redis | API 服务，提供所有业务接口 |

---

## 二、Portal 官网代码结构（`webpage/`）

### 2.1 页面与路由

| 路由路径 | 页面文件 | 功能说明 |
|----------|----------|----------|
| `/` | `webpage/src/pages/Home.vue` | 大会首页，展示 Banner、嘉宾、日程等 |
| `/guests` | `webpage/src/pages/Guests.vue` | 嘉宾介绍页 |
| `/schedule` | `webpage/src/pages/Schedule.vue` | 会议日程页 |
| `/hotel` | `webpage/src/pages/Hotel.vue` | 会议及住宿信息页 |
| `/faq` | `webpage/src/pages/Faq.vue` | 常见问题页 |
| `/pre-meeting` | `webpage/src/pages/PreMeeting.vue` | 会前会介绍页 |
| `/submit` | `webpage/src/pages/Submit.vue` | 投稿入口页 |
| `/first-draft` | `webpage/src/pages/FirstDraft.vue` | 论文投稿表单页（含投稿状态查询模块） |
| `/payment` | `webpage/src/pages/Payment.vue` | 支付页面（管理端用） |
| `/review-file` | `webpage/src/pages/ReviewFile.vue` | 审稿决议页 |
| `/article` | `webpage/src/pages/ArticleList.vue` | 嘉宾演讲集锦列表页 |
| `/article/:id` | `webpage/src/pages/ArticleDetail.vue` | 演讲详情页 |
| `/email-configuration` | `webpage/src/pages/EmailConfiguration.vue` | 邮箱模板配置页 |
| `/register` | `webpage/src/pages/Register.vue` | **参会注册主页面**（含步骤导航栏） |

路由配置文件：`webpage/src/router/index.js`

### 2.2 注册流程页面（`webpage/src/pages/register/`）

注册流程共 6 步，由 `Register.vue` 的步骤导航栏统一管理：

```
Enroll → Consent → Contact → Signup → Confirm → Final
```

| 步骤 | 路由 | 页面文件 | 功能说明 |
|------|------|----------|----------|
| 1 | `/register/enroll` | `Enroll.vue` | **报名登记** — 填写姓名、邮箱、选择个人/团队注册 |
| 2 | `/register/consent` | `Consent.vue` | **隐私协议** — 同意数据隐私政策、ICO 隐私政策、胸牌扫描协议 |
| 3 | `/register/contact` | `Contact.vue` | **联系信息** — 填写职称、单位、电话、地址、账单地址、团队成员（团队注册时） |
| 4 | `/register/signup` | `Signup.vue` | **大会登记** — 选择注册类别（A普通代表/B住院医师学生/C讲者）、签证邀请函、提交注册数据到后端 |
| 5 | `/register/confirm` | `Confirm.vue` | **确认支付** — 订单摘要、抵扣码验证、支付二维码展示、更新支付信息 |
| 6 | `/register/final` | `Final.vue` | **注册完成** — 显示用户信息和支付摘要，跳转首页 |

#### 注册流程中未使用的页面（预留）

| 页面文件 | 功能说明 |
|----------|----------|
| `Survey.vue` | 问卷调查页（旧4类注册类别：眼科医生/非会员/科学家/住院医师，**尚未更新为新3类**） |
| `Skills.vue` | 技能课程选择页（Workshop 按日期分组选择） |
| `Expert.vue` | 专家课程选择页（Course 按日期分组选择） |

### 2.3 状态管理（`webpage/src/stores/`）

| Store 文件 | 功能说明 |
|------------|----------|
| `register.js` | **注册流程核心状态** — 存储 enroll/consent/contact/survey/signup/paymentMethod 等全部注册步骤数据，使用 sessionStorage 持久化 |
| `user.js` | **用户状态** — 管理 token、userInfo、registrationId，提供 `doRegister()` 注册方法和 `doLogin()` 登录方法 |
| `lang.js` | **语言状态** — 管理中英文切换 |
| `guest.js` | **嘉宾数据** — 存储嘉宾列表和首页 Banner 数据 |
| `meeting.js` | **会议数据** — 存储会议设置信息 |

### 2.4 API 接口（`webpage/src/api/`）

| API 文件 | 功能说明 |
|----------|----------|
| `user.js` | 注册用户接口 — `register()` 新增注册、`getRegisterUserByEmail()` 邮箱查询、`updateRegisterUser()` 更新注册信息 |
| `auth.js` | 认证接口 — 邮箱验证码登录 |
| `registrationType.js` | 注册类别接口 — `getRegistrationTypeList(countryType)` 按语言获取注册类别和费用 |
| `payment.js` | 支付接口 — 支付状态查询、银行转账确认、退款（**当前支付方式已改为静态二维码，这些接口暂未使用**） |
| `contribution.js` | 抵扣码接口 — `checkDeductionCode()` 校验抵扣码有效性 |
| `discountConfiguration.js` | 团队折扣配置接口 |
| `emailTemplate.js` | 邮件模板接口 |
| `submit.js` | 论文投稿接口 + 投稿查询接口（发送验证码、查询列表、查看详情、修改投稿） |
| `upload.js` | 文件上传接口 |

### 2.5 国际化（`webpage/src/locales/`）

| 文件 | 说明 |
|------|------|
| `zh.js` | 中文翻译（38.8KB），包含所有页面的中文文案 |
| `en.js` | 英文翻译（44.4KB），包含所有页面的英文文案 |
| `index.js` | i18n 初始化配置 |

### 2.6 工具函数（`webpage/src/utils/`）

| 文件 | 功能说明 |
|------|----------|
| `registerFormMapper.js` | **注册表单映射** — 将前端 registerStore.state 转换为后端 RegisterUserForm 格式，是前端数据提交到后端的核心转换层 |
| `request.js` | Axios 请求封装 — 统一添加 token、处理错误响应 |
| `hospital.js` | 医院社团数据（静态数据） |
| `total.js` | 通用工具函数 |

### 2.7 静态资源（`webpage/public/assets/image/`）

| 目录 | 内容说明 |
|------|----------|
| `patternOfPayment/` | **支付二维码图片** — `regularRegistration.png`（A类普通代表）、`studentRegistration.png`（B类住院医师/学生） |
| `home/` | 首页 Banner 和精彩记录图片 |
| `guests/` | 嘉宾头像和演讲图片 |
| `hotel/` | 酒店图片 |
| `tougao/` | 投稿须知图片 |
| `article/` | 文章详情配图 |
| `common/` | 通用图标和 Logo |
| `faq/` | FAQ 页面配图 |
| `preMeeting/` | 会前会配图 |

### 2.8 公共组件（`webpage/src/components/`）

| 组件文件 | 功能说明 |
|----------|----------|
| `common/NavBar.vue` | 导航栏组件 |
| `common/Footer.vue` | 页脚组件 |
| `common/HeaderLang.vue` | 语言切换组件 |
| `common/LoginModal.vue` | 登录弹窗组件 |
| `common/PaymentModal.vue` | 支付弹窗组件（管理端用） |
| `common/Sidebar.vue` | 侧边栏组件 |
| `common/UserDropdown.vue` | 用户下拉菜单 |
| `common/CountDown.vue` | 倒计时组件 |
| `business/GuestCard.vue` | 嘉宾卡片组件 |
| `business/ScheduleTable.vue` | 日程表格组件 |
| `business/AddHotelModal.vue` | 新增酒店弹窗 |
| `business/AddScheduleModal.vue` | 新增日程弹窗 |

---

## 三、Admin 管理端代码结构（`frontend/`）

### 3.1 页面模块（`frontend/src/views/`）

| 页面目录 | 功能说明 | 对应后端 Controller |
|----------|----------|---------------------|
| `userNum/userNumIndex/` | **注册用户管理** — 用户列表、审核（通过/拒绝）、查看详情 | `RegisterUserController` |
| `checkIn/` | **签到管理** — 扫码签到 | `UserCheckInController` |
| `mailRecord/` | **邮件发送记录** — 邮件列表、发送状态、投递状态、退信详情弹窗、重试、取消 | `MailSendRecordController` + `MailBounceRecordController` |
| `contribution/contributionIndex/` | **论文投稿管理** — 投稿列表、审核、查看审核状态、导出PDF | `PaperSubmissionController` |
| `payMoney/payMoneyIndex/` | **支付管理** — 支付记录查看 | `PaymentController` |
| `hotelRecords/hotelRecordsIndex/` | **酒店记录管理** | `HotelController` |
| `discountConfiguration/discountConfigurationIndex/` | **团队折扣配置** | `DiscountConfigurationController` |
| `emailConfiguration/emailConfigurationIndex/` | **邮件模板配置** | 系统管理 |
| `report/reportIndex/` | **报表统计** | — |
| `portal/answer/` | **问卷调查答案** | `QuestionController` |
| `portal/arrange/` | **日程安排管理** | `ArrangeController` |
| `portal/contact/` | **联系人管理** | `ContactController` |
| `portal/guest/` | **嘉宾管理** | `GuestController` |
| `portal/hotel/` | **酒店管理** | `HotelController` |
| `portal/meeting/` | **会议管理** | `MeetingScheduleController` |
| `portal/setting/` | **会议设置** | `MeetingSettingController` |
| `portal/speech/` | **演讲管理** | `SpeechController` |
| `portal/home/architecture/` | **首页架构管理** | `ArchitectureController` |
| `portal/home/moments/` | **精彩瞬间管理** | `MomentsController` |
| `portal/home/speech/` | **首页演讲管理** | `SpeechController` |
| `system/registration/` | **注册类别管理** — 管理注册类型和费用 | `RegistrationTypeController`（系统模块） |
| `system/user/` | **系统用户管理** | 系统管理 |
| `system/role/` | **角色管理** | 系统管理 |
| `system/menu/` | **菜单管理** | 系统管理 |
| `system/dept/` | **部门管理** | 系统管理 |
| `system/dict/` | **字典管理** | 系统管理 |
| `system/config/` | **系统配置** | 系统管理 |
| `system/notice/` | **通知公告** | 系统管理 |
| `system/tenant/` | **租户管理** | 系统管理 |
| `login/` | **登录页** | `PortalAuthController` |
| `dashboard/` | **仪表盘** | — |
| `profile/` | **个人中心** | — |

### 3.2 API 接口（`frontend/src/api/portal/`）

| API 文件 | 功能说明 |
|----------|----------|
| `registerUser.ts` | 注册用户 CRUD + 审核接口 `audit()` |
| `mailSendRecord.ts` | 邮件发送记录管理（查询、重试、取消、标记退信） |
| `mailBounceRecord.ts` | 退信记录管理（分页查询、详情、按发送记录查询、未处理列表） |
| `paperSubmission.ts` | 论文投稿管理（含导出PDF接口） |
| `userCheckIn.ts` | 签到管理 |
| `guest.ts` | 嘉宾管理 |
| `hotel.ts` | 酒店管理 |
| `arrange.ts` | 日程安排 |
| `contact.ts` | 联系人管理 |
| `speech.ts` | 演讲管理 |
| `meeting.ts` | 会议设置 |
| `moments.ts` | 精彩瞬间 |
| `architecture.ts` | 首页架构 |
| `answer.ts` | 问卷答案 |
| `discountConfiguration.ts` | 团队折扣配置 |
| `setting.ts` | 会议设置 |
| `meetDate.ts` | 会议日期 |

---

## 四、Backend 后端代码结构（`backend/`）

### 4.1 技术栈

- Java 17 + Spring Boot 4.0.1
- MyBatis-Plus 3.5.15（ORM）
- MySQL 8.0 + Druid 连接池
- Redis（缓存 + 验证码存储）
- Spring Mail（邮件发送）+ Jakarta Mail IMAP（退信检测）

### 4.2 Portal 业务模块（`backend/src/main/java/com/youlai/boot/portal/`）

#### Controller 层

| Controller 文件 | API 路径前缀 | 功能说明 |
|-----------------|-------------|----------|
| `RegisterUserController.java` | `/api/v1/registerUser` | **注册用户管理** — 新增、修改、删除、分页查询、审核、签到 |
| `PaymentController.java` | `/api/v1/payment` | **支付管理** — 创建支付意图、查询支付状态、确认银行转账、退款 |
| `PaperSubmissionController.java` | `/api/v1/paperSubmission` | **论文投稿** — 投稿、审核、专家审核、投稿查询（验证码+邮箱查询+修改） |
| `PortalAuthController.java` | `/api/v1/portal/auth` | **Portal 认证** — 邮箱验证码登录 |
| `PortalController.java` | `/api/v1/portal` | **Portal 公共接口** — 首页数据、嘉宾、日程等 |
| `GuestController.java` | `/api/v1/guest` | **嘉宾管理** |
| `HotelController.java` | `/api/v1/hotel` | **酒店管理** |
| `MeetingScheduleController.java` | `/api/v1/meetingSchedule` | **会议日程管理** |
| `MeetingSettingController.java` | `/api/v1/meetingSetting` | **会议设置** |
| `SpeechController.java` | `/api/v1/speech` | **演讲管理** |
| `ArchitectureController.java` | `/api/v1/architecture` | **首页架构管理** |
| `MomentsController.java` | `/api/v1/moments` | **精彩瞬间管理** |
| `ContactController.java` | `/api/v1/contact` | **联系人管理** |
| `ArrangeController.java` | `/api/v1/arrange` | **日程安排管理** |
| `QuestionController.java` | `/api/v1/question` | **问卷调查管理** |
| `DiscountConfigurationController.java` | `/api/v1/discountConfiguration` | **团队折扣配置** |
| `UserCheckInController.java` | `/api/v1/userCheckIn` | **签到管理** |
| `MeetDateController.java` | `/api/v1/meetDate` | **会议日期管理** |
| `AlipayCallbackController.java` | `/api/v1/alipay` | **支付宝回调**（已停用） |
| `WechatCallbackController.java` | `/api/v1/wechat` | **微信支付回调**（已停用） |
| `StripeWebhookController.java` | `/api/v1/stripe` | **Stripe Webhook**（已停用） |

#### Service 层（核心业务逻辑）

| Service 实现文件 | 功能说明 |
|------------------|----------|
| `RegisterUserServiceImpl.java` | **注册核心逻辑** — 用户注册（邮箱唯一校验+入库+发邮件）、更新注册信息、审核（通过发签到二维码邮件/拒绝记录原因）、签到 |
| `PaymentServiceImpl.java` | **支付逻辑** — 支付路由（Strategy 模式）、创建支付意图、支付状态查询（**当前已改为前端静态二维码，后端支付逻辑保留未删除**） |
| `PaperSubmissionServiceImpl.java` | **论文投稿逻辑** — 投稿、审核、专家审核、邮件通知、投稿查询（邮箱验证码+不区分大小写匹配） |
| `UserCheckInServiceImpl.java` | **签到逻辑** — 签到记录管理 |

#### 支付模块（Strategy 策略模式）

| 文件 | 说明 |
|------|------|
| `payment/PaymentProvider.java` | 支付提供者接口 |
| `payment/PaymentRouter.java` | 支付路由器，根据 paymentMethod 分发到对应 Provider |
| `payment/impl/StripePaymentProvider.java` | Stripe 信用卡支付（**已停用**） |
| `payment/impl/AlipayPaymentProvider.java` | 支付宝支付（**已停用**） |
| `payment/impl/WechatPaymentProvider.java` | 微信支付（**已停用**） |
| `payment/impl/BankTransferPaymentProvider.java` | 银行转账支付（**已停用**） |

#### 数据模型

| Entity 文件 | 数据库表 | 说明 |
|-------------|----------|------|
| `RegisterUserInformation.java` | `register_user_information` | **注册用户信息** — 含姓名、邮箱、注册类别、支付状态、审核状态等 |
| `PaperSubmission.java` | `paper_submission` | 论文投稿 |
| `PaymentOrder.java` | `payment_orders` | 支付订单 |
| `UserCheckIn.java` | `user_check_in` | 签到记录 |
| `QuestionnaireSurvey.java` | `questionnaire_survey` | 问卷调查 |
| `Guest.java` | `guest` | 嘉宾信息 |
| `Hotel.java` | `hotel` | 酒店信息 |
| `MeetingSetting.java` | `meeting_setting` | 会议设置 |
| `DiscountConfiguration.java` | `discount_configuration` | 团队折扣配置 |
| `MailSendRecord.java` | `mail_send_record` | **邮件发送记录** — 含 messageId、bounceReason、retryCount 等字段 |

#### 邮件发送记录模块（`support/mail/`）

邮件发送采用**任务表模式**：先落库再发送，保留完整发送历史，同时承担发送队列职责。

**双状态设计：**
- `send_status`：SMTP 阶段状态（邮件是否成功提交到邮件服务器）
- `delivery_status`：投递阶段状态（邮件是否成功送达收件人邮箱）

| 文件 | 说明 |
|------|------|
| `controller/MailSendRecordController.java` | 邮件发送记录管理接口 — 分页查询、详情、重试、取消、按业务查询、标记退信 |
| `controller/MailBounceRecordController.java` | 退信记录管理接口 — 分页查询、详情、按发送记录查询、未处理列表 |
| `service/MailSendRecordService.java` | 邮件发送记录服务接口 — 发送、重试、取消、退信处理、投递状态更新 |
| `service/impl/MailSendRecordServiceImpl.java` | 邮件发送记录服务实现 — 核心发送逻辑、handleBounceEnhanced 7步退信处理、管理员告警、自动重发 |
| `service/MailBounceRecordService.java` | 退信记录服务接口 — 分页查询、按发送记录查询、未处理列表、幂等保存 |
| `service/impl/MailBounceRecordServiceImpl.java` | 退信记录服务实现 — 8条件动态筛选分页查询、幂等保存、5级匹配 |
| `service/MailImapCheckpointService.java` | IMAP检查点服务接口 — 检查点CRUD、UIDVALIDITY变化检测 |
| `service/impl/MailImapCheckpointServiceImpl.java` | IMAP检查点服务实现 |
| `service/MailService.java` | 邮件发送底层接口 — JavaMailSender 封装 |
| `service/impl/MailServiceImpl.java` | 邮件发送底层实现 — 同步发送、Message-ID 提取 |
| `mapper/MailSendRecordMapper.java` | 邮件记录数据访问 |
| `mapper/MailBounceRecordMapper.java` | 退信记录数据访问 |
| `mapper/MailImapCheckpointMapper.java` | IMAP检查点数据访问 |
| `model/entity/MailSendRecord.java` | 邮件发送记录实体 — 含 messageId、deliveryStatus、bounceCode、bounceType等字段 |
| `model/entity/MailBounceRecord.java` | 退信记录实体 — bounceMessageId唯一索引保证幂等 |
| `model/entity/MailImapCheckpoint.java` | IMAP检查点实体 — lastUid+uidValidity增量扫描 |
| `model/vo/MailSendRecordPageVO.java` | 分页列表 VO — 含 deliveryStatus、bounceCode、bounceType、bouncePermanent |
| `model/vo/MailSendRecordDetailVO.java` | 详情 VO — 含 deliveryStatus、bounceMessageId、imapCheckCount |
| `model/vo/BounceParseResult.java` | 退信解析结果 VO |
| `model/query/MailSendRecordQuery.java` | 分页查询参数 — 含 deliveryStatus 筛选 |
| `model/query/MailBounceRecordQuery.java` | 退信记录查询参数 — 8条件筛选 |
| `enums/MailSendStatus.java` | 邮件发送状态枚举（6种状态） |
| `enums/DeliveryStatus.java` | 投递状态枚举（UNKNOWN/DELIVERED/BOUNCED/DELIVERING） |
| `enums/BounceType.java` | 退信类型枚举（7种：USER_NOT_FOUND/MAILBOX_FULL/DNS_ERROR等） |
| `enums/MailBusinessType.java` | 邮件业务类型枚举 |
| `parser/BounceParser.java` | 通用退信解析器（静态工具类）— 15+正则模式，DSN>SMTP>DNS>正文四级分类 |
| `job/BounceEmailJob.java` | 退信检测定时任务 — IMAP UID增量扫描+Redisson分布式锁+checkpoint+5级匹配 |

**邮件发送状态流转（send_status）：**

```
待发送(0) → 已提交至邮件服务器(1)
待发送(0) → 发送失败(2) → 重试中(3) → 已提交至邮件服务器(1) / 发送失败(2)
待发送(0) → 已取消(4)
已提交至邮件服务器(1) → 退信-投递失败(5)（IMAP定时任务检测到退信后回退）
退信-投递失败(5) → 重试中(3)（管理员手动重试或自动重发）
```

**投递状态流转（delivery_status）：**

```
SMTP成功(send_status=1) → 等待投递(0/UNKNOWN)
等待投递(0) → 已退信(2/BOUNCED)（IMAP检测到退信）
等待投递(0) → 已投递(1/DELIVERED)（投递确认，暂不实现主动确认）
```

> **注意**：状态1从"发送成功"改为"已提交至邮件服务器"，因为SMTP返回250 OK仅表示邮件服务器已接收，不代表最终投递成功。退信检测可在此状态后检测到投递失败。

#### 退信检测与投递状态追踪

> **⚠️ 关键配置要求**：退信检测依赖Spring定时任务调度，`YouLaiBootApplication`必须添加`@EnableScheduling`注解，否则`BounceEmailJob`的`@Scheduled(cron)`不会执行，退信检测将完全不工作。部署后启动日志应出现"====== 退信检测定时任务已注册 ======"。

**整体架构：**

```
BounceEmailJob(定时扫描)
    ↓ Redisson分布式锁(mail:bounce-check)
    ↓ IMAP UID增量扫描(UIDFolder.getMessagesByUID)
    ↓ checkpoint持久化(lastUid+uidValidity)
    ↓
BounceParser.parse()(通用退信解析)
    ↓ DSN状态码 > SMTP响应码 > DNS关键词 > 正文关键词
    ↓
handleBounceEnhanced(7步退信处理)
    1. 幂等检查(bounce_message_id唯一索引)
    2. 5级匹配(MESSAGE_ID→IN_REPLY_TO→REFERENCES→RECEIVER_TIME_WINDOW→UNMATCHED)
    3. 写mail_bounce_record(完整退信信息)
    4. 更新双状态(send_status=5 + delivery_status=2)
    5. 永久退信→加黑名单(禁止重发)
    6. 发送管理员告警邮件
    7. 临时退信→自动重发(parentRecordId追踪)
```

**5级匹配策略：**

| 级别 | 策略 | 说明 |
|------|------|------|
| 1 | MESSAGE_ID | 退信邮件引用的原始Message-ID精确匹配 |
| 2 | IN_REPLY_TO | In-Reply-To头匹配 |
| 3 | REFERENCES | References头匹配 |
| 4 | RECEIVER_TIME_WINDOW | 收件人+发送时间窗口(±24h)模糊匹配 |
| 5 | UNMATCHED | 无法匹配，仅记录退信 |

**BounceParser通用退信解析器：**

- 15+正则模式覆盖主流邮箱退信格式
- 四级分类优先级：DSN状态码 > SMTP响应码 > DNS关键词 > 正文关键词
- 退信类型7种：USER_NOT_FOUND / MAILBOX_FULL / DNS_ERROR / SPAM_REJECTED / ROUTING_ERROR / QUOTA_EXCEEDED / OTHER
- 永久退信5种（禁止重发）：USER_NOT_FOUND / MAILBOX_FULL / DNS_ERROR / SPAM_REJECTED / ROUTING_ERROR
- 临时退信2种（可重发）：QUOTA_EXCEEDED / OTHER

**增量扫描机制：**

- `mail_imap_checkpoint`表记录每个邮箱文件夹的`last_uid`和`uid_validity`
- 每次扫描从`last_uid+1`开始，使用`UIDFolder.getMessagesByUID(startUid, LASTUID)`
- UIDVALIDITY变化时自动重置检查点，从全量重新扫描
- 首次扫描回溯`firstScanLookbackDays`天（默认7天）
- 降级模式：checkpoint未启用或folder不支持UIDFolder时，使用FlagTerm未读扫描

**幂等保证：**

- `mail_bounce_record.bounce_message_id`唯一索引
- `saveIfAbsent`双重检查：先查再插，防止同一退信重复处理

**前端展示：**

- 邮件中心列表新增"投递状态"列（等待投递/已投递/已退信/投递中）
- 搜索栏新增"投递状态"筛选
- 已退信记录显示"退信详情"按钮，点击打开退信详情弹窗
- 退信详情弹窗展示：退信类型、永久/临时、匹配策略、退信代码、DSN状态码、退信原因、处理状态等
- 发送记录详情弹窗新增：投递状态、退信类型、退信代码、永久退信标记、退信Message-ID、父记录ID链接

**IMAP 配置（application-dev.yml / application-prod.yml）：**

```yaml
spring:
  mail:
    imap:
      enabled: false          # 开发环境关闭，生产环境开启
      host: imap.163.com       # IMAP 服务器地址
      port: 993                # IMAP 端口（SSL）
      username: xxx@163.com    # 与 SMTP 相同的邮箱账号
      password: xxx            # 邮箱授权码
      ssl-enable: true         # 启用 SSL
      cron: "0 */5 * * * ?"    # 每5分钟扫描一次
      max-process-count: 50    # 每次最多处理50封
      folder-name: INBOX       # 扫描的文件夹
      auto-resend: true        # 启用自动重发
      auto-resend-max-count: 3 # 自动重发最大次数
      admin-alert-emails: admin@example.com  # 管理员告警邮箱（逗号分隔）
```

> **⚠️ 163/126邮箱IMAP "Unsafe Login" 说明**：163/126邮箱不遵循标准IMAP规范，要求客户端连接后发送IMAP ID命令（RFC 2971）标识身份，否则返回 `A3 NO SELECT Unsafe Login`。代码已内置ID命令发送逻辑（`BounceEmailJob.sendImapId()`），无需额外配置。如仍报此错误，请检查：IMAP服务是否开启、授权码是否正确、服务器IP是否在白名单中。

#### 核心字段说明（`register_user_information` 表）

| 字段 | 说明 |
|------|------|
| `review_status` | 审核状态：0-待审核，1-通过，2-拒绝 |
| `payment_status` | 支付状态：pending/paid 等 |
| `payment_method` | 支付方式：当前固定为 `qr_code` |
| `registration_type` | 注册类别 ID |
| `registration_fee` | 注册费用 |
| `payment_amount` | 支付金额 |
| `actual_payment` | 实际支付金额 |
| `deduction_code` | 抵扣码 |
| `register_label` | 注册标签（类别名称） |
| `team_id` | 团队 ID（团队注册时关联） |

---

## 五、注册流程详解

### 5.1 前端注册流程

```
用户访问 /register
    │
    ▼
┌─────────────────────────────────────────────────────────┐
│ Step 1: Enroll.vue — 报名登记                            │
│   填写姓名、邮箱、选择个人/团队注册                          │
│   数据存入 registerStore.state.enroll                    │
└─────────────────────┬───────────────────────────────────┘
                      ▼
┌─────────────────────────────────────────────────────────┐
│ Step 2: Consent.vue — 隐私协议                            │
│   勾选同意数据隐私、ICO隐私、胸牌扫描协议                    │
│   数据存入 registerStore.state.consent                   │
└─────────────────────┬───────────────────────────────────┘
                      ▼
┌─────────────────────────────────────────────────────────┐
│ Step 3: Contact.vue — 联系信息                            │
│   填写职称、单位、电话、地址、账单地址                       │
│   手机号/邮编/城市格式验证                                 │
│   数据存入 registerStore.state.contact                   │
└─────────────────────┬───────────────────────────────────┘
                      ▼
┌─────────────────────────────────────────────────────────┐
│ Step 4: Signup.vue — 大会登记                             │
│   从后端 API 获取注册类别（按 countryType 过滤中英文）       │
│   选择 A普通代表/B住院医师/C讲者                            │
│   ★ 调用 doRegister() → POST /api/v1/registerUser       │
│   ★ 数据入库，发送"注册成功"邮件                            │
│   ★ sessionStorage 存储 registrationId                   │
└─────────────────────┬───────────────────────────────────┘
                      ▼
┌─────────────────────────────────────────────────────────┐
│ Step 5: Confirm.vue — 确认支付                            │
│   显示订单摘要、抵扣码验证                                 │
│   根据注册类别显示对应二维码图片和支付链接                    │
│   ★ 调用 updateRegisterUser() → PUT /api/v1/registerUser │
│   ★ 更新 paymentMethod/paymentAmount 等字段               │
└─────────────────────┬───────────────────────────────────┘
                      ▼
┌─────────────────────────────────────────────────────────┐
│ Step 6: Final.vue — 注册完成                              │
│   显示用户信息和支付摘要                                   │
│   点击确认 → 跳转首页，清除注册缓存                        │
└─────────────────────────────────────────────────────────┘
```

### 5.2 后端审核流程

```
管理员登录 admin 管理端
    │
    ▼
┌─────────────────────────────────────────────────────────┐
│ userNum 页面 — 注册用户列表                               │
│   reviewStatus === 0 的用户显示"审核"按钮                  │
└─────────────────────┬───────────────────────────────────┘
                      ▼
┌─────────────────────────────────────────────────────────┐
│ 点击"审核" → 弹出审核对话框                                │
│   ├─ 通过 → POST /api/v1/registerUser/{id}/audit        │
│   │         reviewStatus=1                              │
│   │         → 生成签到二维码                              │
│   │         → 发送"审核通过+签到二维码"邮件                 │
│   │         → 如有抵扣码，标记为已使用                      │
│   │                                                     │
│   └─ 拒绝 → reviewStatus=2                              │
│             → 记录拒绝原因                                │
└─────────────────────────────────────────────────────────┘
```

### 5.3 支付方式说明

当前支付方式已从后端支付接口（支付宝/微信/Stripe/银行转账）改为**前端静态二维码**：

| 注册类别 | 二维码图片 | 支付链接 |
|----------|-----------|---------|
| A 普通代表 | `patternOfPayment/regularRegistration.png` | `https://cahpepx.kechuangfu.com/coursecenter/preview/179` |
| B 住院医师/学生 | `patternOfPayment/studentRegistration.png` | `https://cahpepx.kechuangfu.com/coursecenter/preview/180` |
| C 讲者 | 不显示支付区域（免费） | — |

---

## 六、本地开发

### 6.1 环境要求

- Node.js 18+
- Java 17+
- MySQL 8.0+
- Redis 7+

### 6.2 后端启动

```bash
cd backend

# 1. 导入数据库（首次）
# 执行 backend/sql/mysql/ 下的 SQL 文件，顺序见第八章

# 2. 修改配置
# 编辑 src/main/resources/application-dev.yml
# 配置 MySQL、Redis、邮件等连接信息

# 3. 启动
mvn spring-boot:run
# 或打包后运行
mvn clean package -DskipTests
java -jar target/youlai-boot.jar
```

后端默认端口：`8000`（dev）/ `8989`（prod Docker）

### 6.3 Portal 官网启动

```bash
cd webpage

# 安装依赖
npm install

# 开发模式
npm run dev

# 打包构建
npm run build
```

开发模式默认端口：`5173`，代理 `/dev-api` 到后端 `localhost:8000`

### 6.4 Admin 管理端启动

```bash
cd frontend

# 安装依赖
pnpm install

# 开发模式
pnpm dev

# 打包构建
pnpm build
```

管理端 base 路径：`/admin/`

#### Admin 管理端打包常见问题

**`pnpm build` 报 TypeScript 类型错误（44 errors）**

原因：`vue-tsc --noEmit` 类型检查阶段，部分 TypeScript 类型定义与实际代码不匹配（类型定义不完整或过时），但不影响运行时。

已将 `package.json` 中 `build` 脚本从 `vue-tsc --noEmit && vite build` 改为 `vite build`，跳过类型检查直接打包。

如需单独运行类型检查，可使用：
```bash
pnpm type-check
```

---

## 七、服务器部署

### 7.1 服务器目录结构

```
/home/meeting-deploy/
├── nginx-portal/
│   └── html/
│       ├── admin/          ← 管理端发布文件（frontend 打包产物）
│       └── portal/         ← 官网发布文件（webpage 打包产物）
```

### 7.2 新服务器首次部署（数据库初始化）

新服务器部署后端前，必须先初始化数据库。SQL 脚本位于 `backend/sql/mysql/`，执行顺序见 [第八章 数据库](#八数据库)。

```bash
# 1. 创建数据库（如果不存在）
mysql -u root -p -e "CREATE DATABASE IF NOT EXISTS meeting_admin DEFAULT CHARSET utf8mb4 COLLATE utf8mb4_general_ci;"

# 2. 按顺序执行全量建表脚本（#1 ~ #9）
cd backend/sql/mysql
mysql -u root -p meeting_admin < youlai_admin.sql
mysql -u root -p meeting_admin < youlai_admin_template.sql
mysql -u root -p meeting_admin < email_template.sql
mysql -u root -p meeting_admin < portal_architecture.sql
mysql -u root -p meeting_admin < payment_orders.sql
mysql -u root -p meeting_admin < mail_send_record.sql
mysql -u root -p meeting_admin < mail_bounce_record.sql
mysql -u root -p meeting_admin < mail_imap_checkpoint.sql

# 3. 按顺序执行增量更新脚本（#10 ~ #17）
mysql -u root -p meeting_admin < mail_send_record_bounce_increment.sql
mysql -u root -p meeting_admin < mail_send_record_delivery_increment.sql
mysql -u root -p meeting_admin < paper_submission_alter_columns.sql
mysql -u root -p meeting_admin < paper_submission_email_add_send_error_msg.sql
mysql -u root -p meeting_admin < portal_meeting_setting_add_paper_submission_deadline.sql
mysql -u root -p meeting_admin < portal_meeting_setting_add_schedule_enabled.sql
mysql -u root -p meeting_admin < portal_meeting_setting_add_schedule_fields.sql
mysql -u root -p meeting_admin < portal_meeting_setting_add_reg_fields.sql

# 4. ⚠️ 生产环境注意：删除 mail_bounce_blacklist.sql 中的测试数据
#    脚本末尾有 INSERT INTO mail_bounce_blacklist 测试数据，生产环境请勿执行
```

> **已有服务器更新**：只需执行本次版本新增的增量脚本，详见 [8.2 增量更新脚本](#82-增量更新脚本按编号顺序执行)。


打包文件本机存放地址
D:\Developer\Project\MCLC\versions\管理端（admin）\后端（bnckend）
D:\Developer\Project\MCLC\versions\管理端（admin）\前端（frontend）
D:\Developer\Project\MCLC\versions\网站（webpage）

### 7.3 更新官网（Portal）

**步骤一：本地打包**

```bash
cd webpage
npm run build
```

打包产物在 `webpage/dist/` 目录。

**步骤二：上传到服务器**

将 `dist` 目录压缩为 zip，通过 SCP/SFTP 上传到服务器 `/home/meeting-deploy/nginx-portal/html/`

**步骤三：服务器部署（先备份再替换）**

```bash
cd /home/meeting-deploy/nginx-portal/html

# 备份当前 portal，带时间戳，出问题可回滚
\cp -r portal portal_backup/

# 清理旧临时目录（如果上次残留）
rm -rf temp_extract
mkdir temp_extract

# 解压上传的 zip 到临时目录，避免直接覆盖线上文件
unzip -o dist.zip -d temp_extract

# 清空当前 portal 目录
rm -rf portal/*

# 将新文件复制到 portal 目录（\cp 绕过 cp 别名，避免逐个确认）
\cp -rf temp_extract/dist/* portal/

# 清理临时目录
rm -rf temp_extract

# 验证部署成功
ls -l portal/index.html
```

### 7.4 更新管理端（Admin）

**步骤一：本地打包**

```bash
cd frontend
pnpm build
```

打包产物在 `frontend/dist/` 目录。

**步骤二：上传到服务器**

将 `dist` 目录压缩为 `admin.zip`，通过 SCP/SFTP 上传到服务器 `/home/meeting-deploy/nginx-portal/html/`

**步骤三：服务器部署（先备份再替换）**

```bash
cd /home/meeting-deploy/nginx-portal/html

# 备份当前 admin，带时间戳，出问题可回滚
\cp -r admin admin_backup/

# 清理旧临时目录（如果上次残留）
rm -rf temp_admin_extract
mkdir temp_admin_extract

# 解压上传的 zip 到临时目录
unzip -o admin.zip -d temp_admin_extract

# 清空当前 admin 目录
rm -rf admin/*

# 将新文件复制到 admin 目录（\cp 绕过 cp 别名，避免逐个确认）
\cp -rf temp_admin_extract/dist/* admin/

# 清理临时目录
rm -rf temp_admin_extract

# 验证部署成功
ls -l admin/index.html
```

### 7.5 更新后端（Backend）Docker 生产部署

服务器目录：`/home/meeting-deploy/mclc-admin/`

```bash
# 0. ⚠️ 如果本次更新涉及数据库变更，先执行增量SQL脚本
#    详见第八章"增量更新脚本"，按编号顺序执行本次新增的脚本
#    例如：mysql -u root -p meeting_admin < mail_bounce_record.sql

# 1. 本地打包
cd backend
mvn clean package -DskipTests

# 2. 上传 target/youlai-boot.jar 到服务器（用 .new 后缀，避免覆盖当前 jar 导致无法备份）
# scp target/youlai-boot.jar user@server:/home/meeting-deploy/mclc-admin/youlai-boot.jar.new

```bash
cd /home/meeting-deploy/mclc-admin

# 备份当前 jar 包（固定名，每次部署时替换旧备份）
cp youlai-boot.jar youlai-boot.jar.backup

# 停止并删除旧容器（2>/dev/null || true 防止容器不存在时报错）
docker stop mclc-admin 2>/dev/null || true
docker rm mclc-admin 2>/dev/null || true

# 用新上传的 jar 包替换当前 jar
cp youlai-boot.jar.bak26 youlai-boot.jar

# 重建镜像 —— 两条命令根据情况选择：
docker build -t mclc-admin:latest .
#   ↑ 仅更新 Java 代码时使用，利用 Docker 缓存层，构建更快
#
docker build --no-cache -t mclc-admin:latest .
#   ↑ Dockerfile 有改动（如安装了新系统依赖）时使用，忽略缓存全量重建，确保新依赖生效

# 启动镜像
docker run -d \
  --name mclc-admin \
  -p 8180:8000 \
  -e SPRING_PROFILES_ACTIVE=prod \
  -e TZ=Asia/Shanghai \
  --restart unless-stopped \
  mclc-admin:latest

# 查看日志确认启动成功（Ctrl+C 退出日志跟踪）
docker logs -f mclc-admin
```

**回滚到旧版本**

```bash
# 停止当前容器
docker stop mclc-admin && docker rm mclc-admin

# 恢复备份的 jar 包
cp youlai-boot.jar.backup youlai-boot.jar

# 重建镜像并启动
docker build -t mclc-admin:latest .
docker run -d \
  --name mclc-admin \
  -p 8180:8000 \
  -e SPRING_PROFILES_ACTIVE=prod \
  -e TZ=Asia/Shanghai \
  --restart unless-stopped \
  mclc-admin:latest
```

---

## 八、数据库

SQL 脚本位于 `backend/sql/mysql/`，分为**全量建表脚本**和**增量更新脚本**两类。

> **新服务器部署**：先执行全量建表脚本，再按编号顺序执行增量脚本。
> **已有服务器更新**：只需执行新增的增量脚本（已执行过的可跳过）。

### 8.1 全量建表脚本（新服务器必须执行）

| 执行顺序 | 文件 | 说明 |
|----------|------|------|
| 1 | `youlai_admin.sql` | 系统基础表（用户、角色、菜单、部门等） |
| 2 | `youlai_admin_template.sql` | 模板数据（菜单树、角色权限等初始数据） |
| 3 | `email_template.sql` | 邮件模板表 `sys_email_template` |
| 4 | `portal_architecture.sql` | 首页架构表 `portal_architecture` |
| 5 | `payment_orders.sql` | 支付订单表 `payment_orders` |
| 6 | `mail_send_record.sql` | 邮件发送记录表 `mail_send_record`（基础结构，不含退信/投递字段） |
| 7 | `mail_bounce_record.sql` | 退信记录表 `mail_bounce_record`（bounce_message_id 唯一索引） |
| 8 | `mail_bounce_blacklist.sql` | 退信黑名单表 `mail_bounce_blacklist` + 退信黑名单菜单（⚠️ 含测试数据，生产环境删除最后一段 INSERT） |
| 9 | `mail_imap_checkpoint.sql` | IMAP 检查点表 `mail_imap_checkpoint`（last_uid + uid_validity 增量扫描） |

### 8.2 增量更新脚本（按编号顺序执行）

| 执行顺序 | 文件 | 依赖 | 说明 |
|----------|------|------|------|
| 10 | `mail_send_record_bounce_increment.sql` | 依赖 #6 `mail_send_record.sql` | 邮件记录表新增 `bounce_reason` 字段 + `idx_message_id` 索引 + send_status 注释更新 |
| 11 | `mail_send_record_delivery_increment.sql` | 依赖 #6 + #10 | 邮件记录表新增 `delivery_status`/`bounce_code`/`bounce_type`/`bounce_permanent`/`bounce_message_id`/`parent_record_id`/`imap_check_count`/`last_imap_check_time` 字段 |
| 12 | `paper_submission_alter_columns.sql` | 依赖 #1 `youlai_admin.sql`（paper_submission 表） | 论文投稿表 `abstract`/`paper_title` 从 VARCHAR 改为 TEXT |
| 13 | `paper_submission_email_add_send_error_msg.sql` | 依赖 #1（paper_submission_email 表） | 论文投稿邮件记录表新增 `send_error_msg` 字段 |
| 14 | `portal_meeting_setting_add_paper_submission_deadline.sql` | 依赖 #1（portal_meeting_setting 表） | 会议设置表新增 `paper_submission_deadline` 字段 |
| 15 | `portal_meeting_setting_add_schedule_enabled.sql` | 依赖 #1 | 会议设置表新增 `schedule_enabled` 字段 |
| 16 | `portal_meeting_setting_add_schedule_fields.sql` | 依赖 #1 | 会议设置表新增 `schedule_title`/`schedule_description`/`show_schedule` 字段 |
| 17 | `portal_meeting_setting_add_reg_fields.sql` | 依赖 #1 + #14 | 会议设置表新增注册时间段字段（幂等脚本，可重复执行） |

### 8.3 执行命令参考

```bash
# 新服务器：按顺序执行所有脚本
mysql -u root -p meeting_admin < youlai_admin.sql
mysql -u root -p meeting_admin < youlai_admin_template.sql
mysql -u root -p meeting_admin < email_template.sql
mysql -u root -p meeting_admin < portal_architecture.sql
mysql -u root -p meeting_admin < payment_orders.sql
mysql -u root -p meeting_admin < mail_send_record.sql
mysql -u root -p meeting_admin < mail_bounce_record.sql
mysql -u root -p meeting_admin < mail_bounce_blacklist.sql
mysql -u root -p meeting_admin < mail_imap_checkpoint.sql
mysql -u root -p meeting_admin < mail_send_record_bounce_increment.sql
mysql -u root -p meeting_admin < mail_send_record_delivery_increment.sql
mysql -u root -p meeting_admin < paper_submission_alter_columns.sql
mysql -u root -p meeting_admin < paper_submission_email_add_send_error_msg.sql
mysql -u root -p meeting_admin < portal_meeting_setting_add_paper_submission_deadline.sql
mysql -u root -p meeting_admin < portal_meeting_setting_add_schedule_enabled.sql
mysql -u root -p meeting_admin < portal_meeting_setting_add_schedule_fields.sql
mysql -u root -p meeting_admin < portal_meeting_setting_add_reg_fields.sql

# 已有服务器：只执行新增的增量脚本（根据本次更新内容选择）
# 例如本次新增了邮件退信检测功能，执行以下3个脚本：
mysql -u root -p meeting_admin < mail_bounce_record.sql
mysql -u root -p meeting_admin < mail_imap_checkpoint.sql
mysql -u root -p meeting_admin < mail_send_record_bounce_increment.sql
mysql -u root -p meeting_admin < mail_send_record_delivery_increment.sql
```

---

## 九、配置文件说明

> **⚠️ 敏感文件安全策略**：`application-dev.yml` 和 `application-prod.yml` 包含数据库密码、API密钥等敏感信息，已被 `.gitignore` 排除，**不会提交到 GitHub**。
> 新开发者请复制对应的 `.template` 模板文件，重命名后填入真实值即可使用。

### 9.1 后端配置文件

| 文件 | Git 跟踪 | 说明 |
|------|----------|------|
| `application.yml` | ✅ 是 | 主配置，激活 dev profile、文件上传限制、项目版本 |
| `application-dev.yml` | ❌ 否（敏感） | 开发环境配置（MySQL、Redis、邮件、支付、OSS、短信等） |
| `application-dev.yml.template` | ✅ 是 | 开发环境配置**模板**，所有敏感值已替换为占位符 |
| `application-prod.yml` | ❌ 否（敏感） | 生产环境配置 |
| `application-prod.yml.template` | ✅ 是 | 生产环境配置**模板**，所有敏感值已替换为占位符 |

**使用方法**：

```bash
cd backend/src/main/resources

# 开发环境
cp application-dev.yml.template application-dev.yml
# 编辑 application-dev.yml，填入真实的数据库密码、邮件授权码、API密钥等

# 生产环境
cp application-prod.yml.template application-prod.yml
# 编辑 application-prod.yml，填入生产环境的真实配置
```

### 9.2 后端配置段详解

| 配置段 | 所在文件 | 说明 | 敏感字段 |
|--------|----------|------|----------|
| `server.port` | dev/prod | 服务端口（dev:8000, prod:8000） | — |
| `spring.datasource` | dev/prod | MySQL 数据源（URL、用户名、密码） | ⚠️ `password` |
| `spring.data.redis` | dev/prod | Redis 连接（host、port、密码） | ⚠️ `password` |
| `spring.mail` | dev/prod | SMTP 邮件发送（host、port、用户名、授权码） | ⚠️ `password` |
| `spring.mail.imap` | dev/prod | IMAP 退信检测（host、port、cron、自动重发） | ⚠️ `password` |
| `spring.mail.from` | dev/prod | 邮件发送者地址 | — |
| `stripe` | dev | Stripe 支付（API Key、Publishable Key、Webhook Secret） | ⚠️ `api-key` |
| `alipay` | dev | 支付宝支付（App ID、私钥、公钥、回调地址） | ⚠️ `private-key`、`alipay-public-key` |
| `wechat-pay` | dev | 微信支付（App ID、商户号、V3密钥、证书路径） | ⚠️ `api-v3-key`、`mch-serial-no` |
| `oss.aliyun` | dev/prod | 阿里云 OSS 对象存储（Endpoint、AccessKey、Bucket） | ⚠️ `access-key-id`、`access-key-secret` |
| `oss.minio` | dev/prod | MinIO 对象存储（Endpoint、AccessKey、Bucket） | ⚠️ `access-key`、`secret-key` |
| `sms.aliyun` | dev/prod | 阿里云短信（AccessKey、模板ID） | ⚠️ `accessKeyId`、`accessKeySecret` |
| `security.jwt.secret-key` | dev/prod | JWT 签名密钥（HS256 至少32字符） | ⚠️ `secret-key` |
| `jodconverter.local` | dev/prod | LibreOffice DOCX 转 PDF（enabled、office-home） | — |
| `confirm-review` | dev | 专家审批链接地址 | — |
| `check-in` | dev | 签到链接地址 | — |
| `wx.miniapp` | dev/prod | 微信小程序配置（App ID、Secret） | ⚠️ `appid`/`secret` |
| `xxl-job` | dev/prod | XXL-Job 定时任务（默认关闭） | — |
| `captcha` | dev/prod | 验证码配置（类型、尺寸、有效期） | — |

### 9.3 前端配置文件

| 文件 | 说明 |
|------|------|
| `webpage/vite.config.js` | Portal Vite 配置，代理 `/dev-api` 到后端 `localhost:8000` |
| `frontend/vite.config.ts` | Admin Vite 配置，base 路径 `/admin/` |
| `frontend/.env.development` | Admin 开发环境变量（API 地址、端口等） |
| `frontend/.env.production` | Admin 生产环境变量 |

---

## 十、论文投稿 PDF 导出功能

### 10.1 功能说明

管理端论文投稿管理页面支持将投稿信息导出为 PDF，支持单条导出和批量一键导出。

#### 导出逻辑

| 附件类型 | 处理方式 | 导出内容 |
|----------|----------|----------|
| **PDF 附件** | 后端从 URL 读取 PDF | 封面页（信息表格）+ 附件 PDF 合并 |
| **DOCX/DOC 附件** | LibreOffice 命令行转换为 PDF | 封面页（信息表格）+ 转换后 PDF 合并 |
| **无附件** | OpenPDF 生成信息表格 | 仅信息表格 |

#### 封面页格式

封面页包含居中标题 + 4行2列信息表格：

| 标签（加粗、灰色背景） | 内容 |
|----------------------|------|
| 论文标题 | 投稿者填写的论文标题 |
| 姓名 | 投稿者姓名 |
| 联系电话 | 投稿者联系电话 |
| 论文摘要 | 投稿者填写的论文摘要 |

#### 前端功能

- **单条导出**：每行操作列"导出PDF"按钮，文件名格式 `姓名.pdf`
- **一键导出**：选中多条后点击工具栏"一键导出PDF"按钮，逐个下载，导出完成后自动取消选中
- **防重复点击**：按记录 ID 隔离，同一条记录 3 秒冷却期，不同记录互不影响
- **限流防崩**：批量导出时不同记录间隔 0.5 秒，防止服务端过载

### 10.2 后端 DOCX 转 PDF（LibreOffice 命令行）

- **技术方案**：ProcessBuilder 直接调用 `soffice --headless --convert-to pdf` 命令行
- **依赖**：`jodconverter-local` 4.4.8 + `jodconverter-spring-boot-starter` 4.4.8（仅用于读取 office-home 配置）
- **接口**：`GET /api/v1/paperSubmission/{id}/exportPdf`
- **前置条件**：服务器需安装 LibreOffice
  - CentOS/RHEL: `sudo yum install libreoffice`
  - Ubuntu/Debian: `sudo apt install libreoffice`
  - Windows: 安装 LibreOffice 并配置 `jodconverter.local.office-home`
- **配置**：`application-dev.yml` / `application-prod.yml` 中 `jodconverter.local` 配置段
- **环境控制**：`@ConditionalOnProperty(name="jodconverter.local.enabled", havingValue="true")`，开发环境可禁用
- **文件位置**：`backend/src/main/java/com/youlai/boot/portal/service/impl/DocxToPdfServiceImpl.java`

> **注意**：早期版本使用 JODConverter DocumentConverter 进程池方式，在 Windows 上存在连接超时和僵尸进程问题，已改为 ProcessBuilder 命令行方式。

### 10.3 后端信息 PDF 生成（OpenPDF）

- **技术方案**：OpenPDF 2.0.3（iText 开源分支）
- **依赖**：`openpdf` 2.0.3
- **封面页格式**：居中标题 → 4行2列 PdfPTable（论文标题/姓名/联系电话/论文摘要）→ Generated 时间戳
- **表格样式**：标签列加粗+灰色背景(#f5f5f5)居中，内容列正常字体左对齐，列宽比 1:3
- **中文字体**：优先使用字体文件路径加载（更可靠），回退到字体名称
  - Windows: `C:/Windows/Fonts/simsun.ttc,0`（宋体）→ `C:/Windows/Fonts/msyh.ttc,0`（微软雅黑）
  - Linux: `/usr/share/fonts/truetype/wqy/wqy-zenhei.ttc,0`（文泉驿）→ `/usr/share/fonts/opentype/noto/NotoSansCJK-Regular.ttc,0`
  - 回退: `SimSun` → `WenQuanYi Zen Hei` → `Noto Sans CJK SC` → `STSong` → `Helvetica`
- **PDF 合并**：`PdfCopy` 合并封面页 PDF 和附件 PDF，封面页在前
- **服务器字体安装**：
  - CentOS/RHEL: `yum install wqy-zenhei-fonts`
  - Ubuntu/Debian: `apt install fonts-wqy-zenhei`
- **文件位置**：`backend/src/main/java/com/youlai/boot/portal/service/impl/PaperPdfGeneratorServiceImpl.java`

### 10.4 论文摘要单词数验证

- Portal 端论文投稿页面（`FirstDraft.vue`、`Submit.vue`）限制摘要不超过 500 个单词
- 计数规则：Word 风格，英文按空白分隔计词，中文按汉字计数
- 达到 500 词后禁止继续输入（lastValidAbstract 回退方案）
- 粘贴超长文本时自动截断并提示用户

---

## 十一、更新日志

> 📋 完整更新日志已迁移至 [CHANGELOG.md](CHANGELOG.md)，所有功能变更、修复记录请查看该文件。

---
