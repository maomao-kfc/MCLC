# MCLC 学术会议管理系统 - 后端服务

> 📋 更新日志已迁移至 [CHANGELOG.md](../CHANGELOG.md)，所有功能变更、修复记录请查看该文件。

## 技术栈

| 技术 | 版本 | 说明 |
|------|------|------|
| Java | 17 | JDK 17+ |
| Spring Boot | 4.0.1 | 应用框架 |
| MyBatis-Plus | 3.5.15 | ORM 框架 |
| MySQL | 8.0 | 数据库 |
| Redis | 7.2 | 缓存 + 验证码存储 |
| Spring Mail | — | 邮件发送 |
| Knife4j | — | API 文档 |

## 项目结构

```
backend/
├── docker/                              # Docker 部署配置
│   ├── docker-compose.yml               # MySQL + Redis + MinIO 容器编排
│   ├── mysql/conf/my.cnf                # MySQL 配置
│   └── redis/config/redis.conf          # Redis 配置
├── sql/mysql/                           # 数据库初始化脚本
│   ├── youlai_admin.sql                 # 系统基础表（用户、角色、菜单、字典等）
│   ├── youlai_admin_template.sql        # 模板数据
│   ├── email_template.sql               # 邮件模板表
│   ├── payment_orders.sql               # 支付订单表
│   ├── portal_architecture.sql          # 首页架构表
│   ├── portal_meeting_setting_*.sql     # 会议设置增量更新
│   ├── mail_send_record.sql             # 邮件发送记录表
│   └── mail_send_record_bounce_increment.sql  # 退信检测增量（bounce_reason + idx_message_id）
├── src/main/java/com/youlai/boot/
│   ├── YouLaiBootApplication.java       # 启动类
│   ├── auth/                            # 认证模块
│   │   ├── controller/
│   │   │   ├── AuthController.java          # 管理端登录认证
│   │   │   └── WechatMiniappAuthController.java  # 微信小程序认证
│   │   ├── model/dto/
│   │   │   └── LoginRequest.java            # 登录请求 DTO
│   │   ├── model/vo/
│   │   │   ├── CaptchaVO.java               # 验证码 VO
│   │   │   └── WechatMiniappLoginResult.java # 微信登录结果
│   │   └── service/impl/
│   │       ├── AuthServiceImpl.java         # 认证服务实现
│   │       └── WechatMiniappAuthServiceImpl.java # 微信认证实现
│   ├── common/                          # 公共模块
│   │   ├── annotation/                  # 自定义注解（Log、RepeatSubmit、DataPermission）
│   │   ├── base/                        # 基类（BaseEntity、BaseQuery）
│   │   ├── constant/                    # 常量（Redis、Security、System）
│   │   ├── enums/                       # 枚举（StatusEnum、LogModuleEnum）
│   │   ├── util/                        # 工具类（QRCodeUtils、DateUtils、IPUtils）
│   │   └── model/                       # 通用模型（Option、KeyValue）
│   ├── config/                          # 配置模块
│   │   ├── SecurityConfig.java          # Spring Security 安全配置
│   │   ├── CorsConfig.java              # 跨域配置
│   │   ├── RedisConfig.java             # Redis 配置
│   │   ├── MybatisConfig.java           # MyBatis-Plus 配置
│   │   ├── MailConfig.java              # 邮件配置
│   │   ├── AlipayConfig.java            # 支付宝配置
│   │   ├── StripeConfig.java            # Stripe 配置
│   │   ├── WechatPayConfig.java         # 微信支付配置
│   │   ├── WebSocketConfig.java         # WebSocket 配置
│   │   └── property/                    # 配置属性类
│   ├── core/                            # 核心模块
│   │   ├── aspect/                      # AOP 切面（日志、防重复提交）
│   │   ├── exception/                   # 全局异常处理
│   │   ├── filter/                      # 过滤器（限流、请求日志）
│   │   └── web/                         # Web 基础（Result、PageResult、ResultCode）
│   ├── portal/                          # ★ 业务模块（学术会议核心）
│   │   ├── controller/                  # 控制器层（21个）
│   │   ├── service/                     # 服务层（20个）
│   │   ├── mapper/                      # 数据访问层（23个）
│   │   ├── model/                       # 数据模型
│   │   │   ├── entity/                  # 实体类（23个）
│   │   │   ├── form/                    # 表单 DTO（18个）
│   │   │   ├── query/                   # 查询参数（12个）
│   │   │   ├── vo/                      # 视图对象（19个）
│   │   │   └── dto/                     # 数据传输对象
│   │   ├── converter/                   # 对象转换器（MapStruct）
│   │   ├── enums/                       # 业务枚举（PaymentMethod、PaymentStatus）
│   │   └── payment/                     # 支付模块（Strategy 策略模式）
│   │       ├── PaymentProvider.java         # 支付提供者接口
│   │       ├── PaymentRouter.java           # 支付路由器
│   │       └── impl/                        # 支付实现（Stripe/支付宝/微信/银行转账）
│   ├── security/                        # 安全模块（JWT Token、认证过滤器）
│   ├── support/                         # 支撑服务
│   │   ├── mail/                        # 邮件服务
│   │   │   ├── controller/
│   │   │   │   └── MailSendRecordController.java  # 邮件发送记录管理接口
│   │   │   ├── service/
│   │   │   │   ├── MailService.java          # 邮件发送底层接口（JavaMailSender 封装）
│   │   │   │   ├── MailServiceImpl.java      # 邮件发送底层实现（同步发送、Message-ID 提取）
│   │   │   │   ├── MailSendRecordService.java  # 邮件记录服务接口
│   │   │   │   └── impl/
│   │   │   │       └── MailSendRecordServiceImpl.java  # 邮件记录服务实现（核心发送、退信处理、自动重发）
│   │   │   ├── mapper/
│   │   │   │   └── MailSendRecordMapper.java  # 邮件记录数据访问
│   │   │   ├── model/
│   │   │   │   ├── entity/MailSendRecord.java  # 邮件发送记录实体
│   │   │   │   ├── entity/MailBounceRecord.java  # 退信记录实体
│   │   │   │   ├── dto/BounceParseResult.java  # 退信解析结果 DTO
│   │   │   │   ├── dto/BounceMatchResult.java  # 退信匹配结果 DTO
│   │   │   │   ├── vo/
│   │   │   │   │   ├── MailSendRecordPageVO.java  # 分页列表 VO
│   │   │   │   │   └── MailSendRecordDetailVO.java  # 详情 VO
│   │   │   │   └── query/MailSendRecordQuery.java  # 分页查询参数
│   │   │   ├── enums/
│   │   │   │   ├── MailSendStatus.java     # 邮件发送状态枚举（6种状态）
│   │   │   │   ├── MailBusinessType.java   # 邮件业务类型枚举
│   │   │   │   └── BounceType.java         # 退信类型枚举（DOMAIN_NOT_FOUND/DNS_TEMPORARY_FAILURE/DNS_ERROR等）
│   │   │   ├── parser/
│   │   │   │   └── BounceParser.java       # 退信解析器（DSN标准字段优先+RFC 3463分类+6级匹配）
│   │   │   └── job/
│   │   │       └── BounceEmailJob.java     # 退信检测定时任务（IMAP 协议）
│   │   ├── sms/                         # 短信服务（阿里云 SMS）
│   │   └── websocket/                   # WebSocket 服务
│   ├── system/                          # 系统管理模块
│   │   ├── controller/                  # 用户、角色、菜单、部门、字典等
│   │   ├── service/
│   │   ├── mapper/
│   │   └── model/
│   ├── module/                          # 扩展模块
│   │   ├── codegen/                     # 代码生成器
│   │   └── file/                        # 文件服务（本地/MinIO/阿里云 OSS）
│   └── tool/                            # 工具模块
└── src/main/resources/
    ├── application.yml                  # 主配置
    ├── application-dev.yml              # 开发环境配置
    ├── application-prod.yml             # 生产环境配置
    ├── codegen.yml                      # 代码生成器配置
    └── mapper/                          # MyBatis XML 映射文件
```

## Portal 业务模块 API 一览

所有 Portal 业务接口前缀为 `/api/v1/`，认证接口需携带 JWT Token。

### 注册用户管理 (`RegisterUserController`)

| 方法 | 路径 | 说明 |
|------|------|------|
| GET | `/registerUser` | 注册用户分页列表（管理端） |
| POST | `/registerUser` | 新增注册用户（Portal 端注册） |
| PUT | `/registerUser/{id}` | 修改注册用户（Portal 端更新支付信息） |
| DELETE | `/registerUser/{ids}` | 删除注册用户 |
| GET | `/registerUser/{id}` | 注册用户详情 |
| POST | `/registerUser/{id}/audit` | 审核注册用户（通过/拒绝） |
| POST | `/registerUser/checkIn` | 签到 |

### 注册核心流程

```
1. POST /registerUser → 用户注册
   - 邮箱唯一校验
   - 数据入库，payment_status = "pending"
   - 发送"注册成功"邮件
   - 返回 registrationId + token

2. PUT /registerUser/{id} → 更新支付信息
   - 更新 paymentMethod/paymentAmount/actualPayment 等

3. POST /registerUser/{id}/audit → 管理员审核
   - reviewStatus=1（通过）→ 生成签到二维码 + 发邮件 + 处理抵扣码
   - reviewStatus=2（拒绝）→ 记录拒绝原因
```

### 论文投稿 (`PaperSubmissionController`)

| 方法 | 路径 | 说明 |
|------|------|------|
| GET | `/paperSubmission` | 投稿列表 |
| POST | `/paperSubmission` | 提交投稿 |
| PUT | `/paperSubmission/{id}/review` | 审核投稿 |
| POST | `/paperSubmission/{id}/expertReview` | 专家审核 |

#### 投稿查询公开接口（无需登录）

投稿者通过邮箱验证码查询自己的投稿记录，支持查看详情和修改待审核状态的投稿。

| 方法 | 路径 | 说明 |
|------|------|------|
| POST | `/paperSubmission/query/sendCode` | 发送查询验证码（校验邮箱有投稿记录后才发送） |
| GET | `/paperSubmission/query/byEmail` | 按邮箱+验证码查询投稿列表 |
| GET | `/paperSubmission/query/detail` | 查询投稿详情（需验证邮箱归属） |
| PUT | `/paperSubmission/query/update` | 投稿者修改投稿信息（仅待审核状态可修改） |

**查询流程：**

```
投稿者输入邮箱 → 点击"发送验证码" → 后端校验邮箱有投稿记录 → 发送验证码邮件
    → 投稿者输入验证码 → 点击"查询" → 返回该邮箱下所有投稿记录
    → 查看详情 → 弹窗展示完整投稿信息
    → 修改投稿（仅 paperStatus=1 待审核状态可修改）
```

**验证码机制：** Redis key `captcha:paper_query:{email}`，6位随机码，5分钟有效，一次性使用

**允许修改的字段：** paperTitle、firstAuthor、contactNumber、paperAbstract、keywords、appendix
**不可修改的字段：** name、surname、contactEmail（作为身份标识）

**邮箱匹配：** 查询时使用 `LOWER(contact_email)` 不区分大小写匹配，确保用户输入与数据库存储大小写不一致时仍可查询

### 支付管理 (`PaymentController`)

| 方法 | 路径 | 说明 |
|------|------|------|
| POST | `/payment/intent` | 创建支付意图（已停用） |
| GET | `/payment/status/{registrationId}` | 查询支付状态 |
| POST | `/payment/confirm/{registrationId}` | 确认银行转账 |
| POST | `/payment/refund/{registrationId}` | 退款 |

> **注意**：当前支付方式已改为前端静态二维码，后端支付接口（Stripe/支付宝/微信）已停用但代码保留。

### 其他业务接口

| Controller | 路径前缀 | 说明 |
|-----------|---------|------|
| `PortalAuthController` | `/portal/auth` | Portal 邮箱验证码登录 |
| `PortalController` | `/portal` | 首页数据、嘉宾、日程等公共接口 |
| `GuestController` | `/guest` | 嘉宾 CRUD |
| `HotelController` | `/hotel` | 酒店 CRUD |
| `MeetingScheduleController` | `/meetingSchedule` | 会议日程管理 |
| `MeetingSettingController` | `/meetingSetting` | 会议设置 |
| `SpeechController` | `/speech` | 演讲管理 |
| `ArchitectureController` | `/architecture` | 首页架构管理 |
| `MomentsController` | `/moments` | 精彩瞬间管理 |
| `ContactController` | `/contact` | 联系人管理 |
| `ArrangeController` | `/arrange` | 日程安排管理 |
| `QuestionController` | `/question` | 问卷调查管理 |
| `DiscountConfigurationController` | `/discountConfiguration` | 团队折扣配置 |
| `UserCheckInController` | `/userCheckIn` | 签到管理 |
| `MeetDateController` | `/meetDate` | 会议日期管理 |

### 支付回调接口（已停用）

| Controller | 路径前缀 | 说明 |
|-----------|---------|------|
| `AlipayCallbackController` | `/alipay` | 支付宝异步通知回调 |
| `WechatCallbackController` | `/wechat` | 微信支付回调 |
| `StripeWebhookController` | `/stripe` | Stripe Webhook |

### 邮件发送记录管理 (`MailSendRecordController`)

| 方法 | 路径 | 说明 |
|------|------|------|
| GET | `/mailSendRecord/page` | 邮件发送记录分页列表（支持状态、业务类型、收件人筛选） |
| GET | `/mailSendRecord/{id}` | 邮件发送记录详情 |
| PUT | `/mailSendRecord/{id}/retry` | 重试发送失败的邮件 |
| PUT | `/mailSendRecord/{id}/cancel` | 取消待发送/重试中的邮件 |
| GET | `/mailSendRecord/business/{businessType}/{businessId}` | 按业务类型和业务ID查询邮件记录 |

> 邮件发送采用**任务表模式**：先落库再发送，保留完整发送历史，同时承担发送队列职责。

## 数据库核心表

| 表名 | 说明 | 关键字段 |
|------|------|---------|
| `register_user_information` | 注册用户信息 | `review_status`(审核状态), `payment_status`(支付状态), `payment_method`(支付方式), `registration_type`(注册类别), `deduction_code`(抵扣码), `team_id`(团队ID) |
| `paper_submission` | 论文投稿 | `paper_status`(投稿状态), `deduction_status`(抵扣码状态) |
| `payment_orders` | 支付订单 | `stripe_payment_intent_id`, `payment_method`, `payment_status` |
| `user_check_in` | 签到记录 | `check_in_time`(签到时间) |
| `questionnaire_survey` | 问卷调查 | 关联 `register_user_information.connect_id` |
| `guest` | 嘉宾信息 | — |
| `hotel` | 酒店信息 | — |
| `meeting_setting` | 会议设置 | — |
| `discount_configuration` | 团队折扣配置 | `min_people`, `max_people`, `discount` |
| `mail_send_record` | 邮件发送记录 | `message_id`(邮件Message-ID), `send_status`(发送状态), `bounce_reason`(退信原因), `retry_count`(重试次数), `business_type`(业务类型), `business_id`(业务ID) |
| `registration_type` | 注册类别 | `field_name`, `deadline_amount`, `country_type`(ZH/EN) |

## 本地开发

### 环境要求

- JDK 17+
- MySQL 8.0+
- Redis 6.0+
- Maven 3.6+

### 启动步骤

**1. 初始化数据库**

执行 `sql/mysql/youlai_admin.sql` 创建数据库和基础数据。

**2. 修改配置**

编辑 `src/main/resources/application-dev.yml`：

```yaml
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/meeting_admin?...
    username: root
    password: 你的密码
  data:
    redis:
      host: localhost
      port: 6379
      password: 你的密码
  mail:
    host: smtp.163.com
    username: 你的邮箱
    password: 你的邮箱授权码
```

**3. 启动**

```bash
# 开发模式
mvn spring-boot:run

# 或打包运行
mvn clean package -DskipTests
java -jar target/youlai-boot.jar
```

- 开发环境端口：`8000`
- API 文档地址：`http://localhost:8000/doc.html`

### Docker 部署（本地环境）

```bash
cd docker
docker-compose up -d
```

启动 MySQL 8.0、Redis 7.2、MinIO 对象存储。

## 生产部署

### 打包

```bash
mvn clean package -DskipTests
# 产物：target/youlai-boot.jar
```

### Docker 部署

```bash
# 构建镜像
docker build -t mclc-backend .

# 运行
docker run -d -p 8989:8989 \
  -e SPRING_PROFILES_ACTIVE=prod \
  -e TZ=Asia/Shanghai \
  mclc-backend
```

### JAR 部署

```bash
java -jar youlai-boot.jar --spring.profiles.active=prod
```

生产环境端口：`8000`（application-prod.yml 配置）

### 关键配置项（application-prod.yml）

| 配置 | 说明 |
|------|------|
| `spring.datasource.url` | 生产数据库地址 |
| `spring.data.redis` | 生产 Redis 地址 |
| `spring.mail` | 邮件服务配置 |
| `spring.mail.imap` | 退信检测 IMAP 配置（enabled/host/port/cron/auto-resend） |
| `confirm-review` | 专家审批链接前缀 |
| `check-in` | 签到链接前缀 |
| `stripe.*` | Stripe 支付配置（已停用） |
| `alipay.*` | 支付宝配置（已停用） |

## 支付模块说明

支付模块采用 **Strategy 策略模式** 设计：

```
PaymentRouter（路由器）
    ├── StripePaymentProvider      → Stripe 信用卡支付（已停用）
    ├── AlipayPaymentProvider      → 支付宝支付（已停用）
    ├── WechatPaymentProvider      → 微信支付（已停用）
    └── BankTransferPaymentProvider → 银行转账（已停用）
```

当前支付方式已改为**前端静态二维码**，后端 `paymentMethod` 字段存储 `"qr_code"` 标识。原有支付代码保留未删除，后续如需恢复可重新启用。

## 邮件服务

邮件用于以下场景：

| 场景 | 触发时机 | 邮件内容 |
|------|---------|---------|
| 注册成功 | `saveRegisterUser()` 新用户入库 | 使用"注册通过模板" |
| 审核通过 | `auditRegisterUser()` reviewStatus=1 | 签到二维码 + 审核通过通知 |
| 论文审核 | `expertReview()` | 审核通过/未通过通知 |

邮件模板在 `email_template` 表中管理，可通过管理端 `emailConfiguration` 页面配置。

### 邮件发送记录模块

邮件发送采用**任务表模式**：先落库再发送，保留完整发送历史，同时承担发送队列职责。

**MailSendStatus 状态流转：**

```
PENDING(待发送) ──→ SUBMITTED(已提交至邮件服务器) ──→ 退信检测 ──→ BOUNCED(退信-投递失败)
    │                                                      │
    ├──→ FAILED(发送失败) ──→ RETRYING(重试中) ────────────┘
    │                              │
    └──→ CANCELLED(已取消)         └──→ SUBMITTED / FAILED
```

| 状态 | 说明 |
|------|------|
| `PENDING` | 待发送，邮件已落库尚未提交至邮件服务器 |
| `SUBMITTED` | 已提交至邮件服务器，邮件服务器已接受 |
| `FAILED` | 发送失败，邮件服务器拒绝或网络异常 |
| `RETRYING` | 重试中，失败后自动或手动重试 |
| `CANCELLED` | 已取消，管理员手动取消 |
| `BOUNCED` | 退信-投递失败，IMAP检测到收件人无法接收 |

**MailBusinessType 业务类型枚举：**

| 类型 | 说明 |
|------|------|
| `REGISTRATION` | 注册确认邮件 |
| `AUDIT_PASS` | 审核通过邮件 |
| `AUDIT_REJECT` | 审核拒绝邮件 |
| `PAPER_REVIEW` | 论文审核邮件 |
| `EXPERT_REVIEW` | 专家审稿邮件 |

### 退信检测定时任务（BounceEmailJob）

> **⚠️ 关键配置**：退信检测依赖`@EnableScheduling`注解激活Spring定时任务调度。`YouLaiBootApplication`必须添加此注解，否则`BounceEmailJob`的`@Scheduled(cron)`不会执行，退信检测完全不工作。部署后启动日志应出现"====== 退信检测定时任务已注册 ======"。

通过 IMAP 协议连接发件邮箱收件箱，扫描未读退信邮件，提取原始 Message-ID 匹配 `mail_send_record` 记录，将状态更新为"退信-投递失败"并触发管理员告警和自动重发。

**退信识别策略（三重匹配）：**

1. **发送者模式**：`MAILER-DAEMON@`、`postmaster@` 等邮件系统自动回复地址
2. **主题关键词**：`Undelivered`、`Returned Mail`、`Failure`、`bounce` 等
3. **Content-Type**：`multipart/report`（RFC 3462 退信标准格式）

**Message-ID 提取策略（优先级递减）：**

1. `In-Reply-To` 头 → 最准确的原始邮件引用
2. `References` 头 → 多引用链中取最后一个
3. 正文正则 → `<message-id@...>` 格式兜底

**退信处理流程：**

```
BounceEmailJob 定时执行
    │
    ├── IMAP 连接收件箱 → 搜索未读邮件
    │
    ├── 遍历邮件 → 退信识别（发送者+主题+Content-Type）
    │       │
    │       ├── 匹配成功 → 提取 Message-ID
    │       │       │
    │       │       └── 查询 mail_send_record → 更新状态为 BOUNCED
    │       │               │
    │       │               ├── 记录 bounceReason（退信原因）
    │       │               ├── 发送管理员告警邮件
    │       │               └── 自动重发（新建记录，保留原退信记录）
    │       │
    │       └── 非退信邮件 → 跳过
    │
    └── 标记已读 → 下次不再扫描
```

**IMAP 配置（application.yml）：**

```yaml
spring:
  mail:
    imap:
      enabled: true                    # 是否启用退信检测
      host: imap.163.com               # IMAP 服务器地址
      port: 993                        # IMAP 端口（SSL）
      username: ${spring.mail.username} # 默认复用 SMTP 账号
      password: ${spring.mail.password} # 默认复用 SMTP 密码
      cron: "0 0/30 * * * ?"           # 扫描频率（默认每30分钟）
      auto-resend: true                # 退信后是否自动重发
      max-retry-count: 3               # 最大自动重发次数
```

> **注意**：IMAP 账号需与 SMTP 发件账号一致，且邮箱需开启 IMAP 服务。

> **⚠️ 163/126邮箱IMAP "Unsafe Login" 说明**：163/126邮箱不遵循标准IMAP规范，要求客户端发送IMAP ID命令（RFC 2971）标识身份，否则返回 `Unsafe Login`。代码已内置ID命令发送逻辑（`BounceEmailJob.sendImapId()`），无需额外配置。如仍报错，检查IMAP服务是否开启、授权码是否正确、服务器IP是否在白名单。

## 常见问题排查

### JODConverter LibreOffice 端口占用

应用异常退出后，LibreOffice 进程（soffice.bin）可能残留并占用 2002/2003/2004 端口，导致重启时报错：

```
org.jodconverter.core.office.OfficeException: A process with --accept 'socket,host=127.0.0.1,port=2002,...' is already running and could not be killed
org.jodconverter.core.office.OfficeException: A process with --accept 'socket,host=127.0.0.1,port=2004,...' is already running and could not be killed
```

**根因：** JODConverter 在应用启动时会自动启动 3 个 LibreOffice 进程（端口 2002/2003/2004）。应用异常退出时这些进程不会自动关闭，重启后新进程无法绑定同一端口。

**配置化自动解决（推荐）：**

在 `application-dev.yml` / `application-prod.yml` 中已配置 `existing-process-action: connect-or-kill`，JODConverter 启动时会自动处理残留进程：

```yaml
jodconverter:
  local:
    # ... 其他配置 ...
    # 发现端口被旧进程占用时的处理策略
    existing-process-action: connect_or_kill
```

**`existing-process-action` 可选值说明：**

| 值 | 行为 | 适用场景 |
|---|---|---|
| `kill`（默认） | 直接杀掉旧进程 | Linux 下正常工作，Windows 下可能杀进程失败 |
| `connect` | 直接连接复用旧进程 | 旧进程一定可用时 |
| **`connect_or_kill`（推荐）** | 先尝试连接复用，失败则强制杀进程 | **开发/生产环境推荐，兼顾复用和清理** |
| `fail` | 直接报错退出 | 需要严格管控进程时 |

> 配置了 `connect-or-kill` 后，大多数情况下重启应用不再需要手动杀进程。如果仍有极少数情况需要手动处理，请参考下方手动清理步骤。

**手动清理步骤（备用）：**

```cmd
:: 1. 查找 Spring Boot 进程 PID（任选一种）
netstat -ano | findstr :8000           :: 按应用端口查找（推荐）
jps -l                                 :: JDK 自带，列出所有 Java 进程
tasklist | findstr java                :: 按进程名查找

:: 2. 先停 Spring Boot 应用（Ctrl+C 或 taskkill）
::    必须先停应用，否则 JODConverter 会自动重启 LibreOffice 进程
taskkill /F /PID <Spring Boot进程PID> 2>&1

:: 3. 批量杀所有 LibreOffice 进程
taskkill /F /IM soffice.bin 2>&1 & taskkill /F /IM soffice.exe 2>&1

:: 4. 检查端口是否已释放
netstat -ano | findstr ":2002 :2003 :2004" 2>&1

:: 5. 如果仍有残留进程占用端口，按 PID 逐个终止
taskkill /F /PID <残留进程PID> 2>&1

:: 6. 重新启动应用
mvn spring-boot:run
```

**一键清理脚本（Windows CMD）：**

```cmd
:: 保存为 kill-jodconverter.bat，双击运行即可清理所有残留进程
@echo off
echo 正在查找 Spring Boot 进程...
for /f "tokens=5" %%a in ('netstat -ano ^| findstr :8000 ^| findstr LISTENING') do (
    echo 终止 Spring Boot 进程 PID=%%a
    taskkill /F /PID %%a >nul 2>&1
)
echo 正在清理 LibreOffice 进程...
taskkill /F /IM soffice.bin >nul 2>&1
taskkill /F /IM soffice.exe >nul 2>&1
echo 正在检查残留端口...
netstat -ano | findstr ":2002 :2003 :2004"
if errorlevel 1 (
    echo 端口已全部释放，可以重新启动应用
) else (
    echo 仍有端口占用，请手动执行 taskkill /F /PID ^<PID^>
)
pause
```

> **提示：**
> - Spring Boot 启动后，日志每行前缀中的数字即为 PID，如 `INFO 11004 --- [youlai-boot]`
> - 步骤 2 是关键：必须先停 Spring Boot，否则 JODConverter 检测到 LibreOffice 进程被杀后会立刻重启新的
> - 步骤 4 中如果 `netstat` 无输出则端口已释放，可直接重启

---

## 更新日志

> 📋 完整更新日志已迁移至 [CHANGELOG.md](../CHANGELOG.md)，所有功能变更、修复记录请查看该文件。

---
