# MCLC 代码地图

> 文件→类→方法→系统级映射，帮助快速定位代码
> 基于代码扫描生成，覆盖后端+前端双端

---

## 1. 后端架构总览

```
com.youlai.boot
├── auth/           认证中心（2 Controller, 2 Service, 4 Model）
├── common/         公共基础（4 Annotation, 3 Base, 4 Constant, 6 Enum, 4 Util）
├── config/         配置层（23 Config, 11 Properties）
├── core/           核心框架（2 Aspect, 2 Exception, 2 Filter, 1 Validator, 6 Web）
├── module/         通用模块（codegen 13文件, file 5文件）
├── portal/         门户业务（21 Controller, 23 Service, 24 Mapper, 24 Entity）
├── support/        支撑服务（mail 26文件, sms 3文件, websocket 8文件）
└── system/         系统管理（11 Controller, 16 Service, 16 Mapper, 16 Entity）
```

---

## 2. 认证中心（auth）

### Controller层
| 文件 | 类 | 端点前缀 | 关键方法 |
|------|-----|----------|----------|
| `auth/controller/AuthController.java` | AuthController | `/api/v1/auth` | captcha(), login(), loginBySms(), sendSmsCode(), logout(), refreshToken() |
| `auth/controller/WechatMiniappAuthController.java` | WechatMiniappAuthController | `/api/v1/wechat/miniapp/auth` | silentLogin(), phoneLogin(), bindMobile() |

### Service层
| 文件 | 类 | 关键方法 |
|------|-----|----------|
| `auth/service/impl/AuthServiceImpl.java` | AuthServiceImpl | login(), loginBySms(), logout(), refreshToken(), getCaptcha() |
| `auth/service/impl/WechatMiniappAuthServiceImpl.java` | WechatMiniappAuthServiceImpl | silentLogin(), phoneLogin(), bindMobile() |

### Model层
| 文件 | 类型 | 用途 |
|------|------|------|
| `auth/model/dto/LoginRequest.java` | DTO | 登录请求体 |
| `auth/model/vo/CaptchaVO.java` | VO | 验证码响应 |
| `auth/model/vo/WechatMiniappLoginResult.java` | VO | 微信登录结果 |

---

## 3. 系统管理（system）

### Controller层
| 文件 | 类 | 端点前缀 | 端点数 |
|------|-----|----------|--------|
| `system/controller/UserController.java` | UserController | `/api/v1/users` | 22 |
| `system/controller/RoleController.java` | RoleController | `/api/v1/roles` | 10 |
| `system/controller/MenuController.java` | MenuController | `/api/v1/menus` | 8 |
| `system/controller/DeptController.java` | DeptController | `/api/v1/depts` | 6 |
| `system/controller/DictController.java` | DictController | `/api/v1/dicts` | 12 |
| `system/controller/ConfigController.java` | ConfigController | `/api/v1/configs` | 6 |
| `system/controller/NoticeController.java` | NoticeController | `/api/v1/notices` | 10 |
| `system/controller/LogController.java` | LogController | `/api/v1/logs` | 1 |
| `system/controller/EmailTemplateController.java` | EmailTemplateController | `/api/v1/email-templates` | 5 |
| `system/controller/RegistrationTypeController.java` | RegistrationTypeController | `/api/v1/registrationType` | 4 |
| `system/controller/StatisticsController.java` | StatisticsController | `/api/v1/statistics` | 2 |

### Service层（核心）
| 文件 | 类 | 大小 | 关键职责 |
|------|-----|------|----------|
| `system/service/impl/UserServiceImpl.java` | UserServiceImpl | 22.1KB | 用户CRUD、导入导出、个人中心、手机/邮箱绑定 |
| `system/service/impl/RoleServiceImpl.java` | RoleServiceImpl | 13.4KB | 角色CRUD、权限分配、数据范围 |
| `system/service/impl/MenuServiceImpl.java` | MenuServiceImpl | 17.9KB | 菜单CRUD、路由生成、权限树 |
| `system/service/impl/NoticeServiceImpl.java` | NoticeServiceImpl | 11.0KB | 通知CRUD、发布/撤回、WebSocket推送 |
| `system/service/impl/DeptServiceImpl.java` | DeptServiceImpl | 8.9KB | 部门树CRUD、数据权限过滤 |
| `system/service/impl/DictServiceImpl.java` | DictServiceImpl | 5.5KB | 字典CRUD、缓存刷新 |
| `system/service/impl/RoleMenuServiceImpl.java` | RoleMenuServiceImpl | 6.1KB | 角色-菜单关联 |
| `system/service/impl/UserSocialServiceImpl.java` | UserSocialServiceImpl | 4.0KB | 社交账号绑定 |

### Entity层
| 实体 | 表名 | 关键字段 |
|------|------|----------|
| User | sys_user | username, password, phone, email, status, dept_id |
| Role | sys_role | name, code, data_scope, status |
| Menu | sys_menu | name, path, component, permission, type, sort |
| Dept | sys_dept | name, parent_id, sort, status |
| Dict | sys_dict | name, code, status |
| DictItem | sys_dict_item | label, value, sort, dict_id |
| Config | sys_config | name, key, value, type |
| Notice | sys_notice | title, content, type, target, publish_status |
| Log | sys_log | module, content, request_method, ip |
| EmailTemplate | sys_email_template | name, code, subject, content |
| RegistrationType | registration_type | name, code, price, status |
| UserSocial | sys_user_social | user_id, platform, open_id |
| UserRole | sys_user_role | user_id, role_id |
| RoleMenu | sys_role_menu | role_id, menu_id |
| RoleDept | sys_role_dept | role_id, dept_id |
| UserNotice | sys_user_notice | user_id, notice_id, read_status |

---

## 4. 门户业务（portal）

### Controller层
| 文件 | 类 | 端点前缀 | 端点数 | 业务域 |
|------|-----|----------|--------|--------|
| `portal/controller/PortalController.java` | PortalController | `/api/v1/portal` | 10 | 门户聚合查询 |
| `portal/controller/PortalAuthController.java` | PortalAuthController | `/api/v1/portal/auth` | 2 | 门户认证 |
| `portal/controller/RegisterUserController.java` | RegisterUserController | `/api/v1/registerUser` | 7 | 注册管理 |
| `portal/controller/PaymentController.java` | PaymentController | `/api/v1/payment` | 6 | 支付 |
| `portal/controller/PaperSubmissionController.java` | PaperSubmissionController | `/api/v1/paperSubmission` | 14 | 论文投稿 |
| `portal/controller/MeetingScheduleController.java` | MeetingScheduleController | `/api/v1/meetingSchedule` | 8 | 会议日程 |
| `portal/controller/MeetingSettingController.java` | MeetingSettingController | `/api/v1/meetingSetting` | 5 | 会议设置 |
| `portal/controller/GuestController.java` | GuestController | `/api/v1/guest` | 5 | 特邀嘉宾 |
| `portal/controller/HotelController.java` | HotelController | `/api/v1/hotel` | 5 | 酒店信息 |
| `portal/controller/ContactController.java` | ContactController | `/api/v1/contact` | 5 | 联系人 |
| `portal/controller/ArchitectureController.java` | ArchitectureController | `/api/v1/architecture` | 5 | 会议架构 |
| `portal/controller/ArrangeController.java` | ArrangeController | `/api/v1/arrange` | 5 | 会前安排 |
| `portal/controller/MomentsController.java` | MomentsController | `/api/v1/moments` | 5 | 精彩瞬间 |
| `portal/controller/SpeechController.java` | SpeechController | `/api/v1/speech` | 6 | 演讲集锦 |
| `portal/controller/QuestionController.java` | QuestionController | `/api/v1/question` | 5 | 问题解答 |
| `portal/controller/DiscountConfigurationController.java` | DiscountConfigurationController | `/api/v1/discountConfiguration` | 2 | 折扣配置 |
| `portal/controller/MeetDateController.java` | MeetDateController | `/api/v1/meetDate` | 5 | 会议日期 |
| `portal/controller/UserCheckInController.java` | UserCheckInController | `/api/v1/userCheckIn` | 2 | 签到记录 |
| `portal/controller/StripeWebhookController.java` | StripeWebhookController | `/api/v1/stripe` | 1 | Stripe回调 |
| `portal/controller/AlipayCallbackController.java` | AlipayCallbackController | `/api/v1/payment/alipay` | 1 | 支付宝回调 |
| `portal/controller/WechatCallbackController.java` | WechatCallbackController | `/api/v1/payment/wechat` | 1 | 微信回调 |

### Service层（核心，按大小排序）
| 文件 | 类 | 大小 | 关键职责 |
|------|-----|------|----------|
| `portal/service/impl/PaperSubmissionServiceImpl.java` | PaperSubmissionServiceImpl | 37.5KB | 论文CRUD、审核、邮件通知、PDF导出 |
| `portal/service/impl/RegisterUserServiceImpl.java` | RegisterUserServiceImpl | 25.2KB | 注册CRUD、审核、签到、折扣计算 |
| `portal/service/impl/MeetingInfoServiceImpl.java` | MeetingInfoServiceImpl | 20.7KB | 会议日程4级层次CRUD |
| `portal/service/impl/PaymentServiceImpl.java` | PaymentServiceImpl | 12.1KB | 支付意图创建、状态查询、退款 |
| `portal/service/impl/MeetingDateServiceImpl.java` | MeetingDateServiceImpl | 13.6KB | 会议日期管理 |
| `portal/service/impl/PaperPdfGeneratorServiceImpl.java` | PaperPdfGeneratorServiceImpl | 10.9KB | 论文PDF生成（封面+信息页+合并） |
| `portal/service/impl/MeetingSettingServiceImpl.java` | MeetingSettingServiceImpl | 6.0KB | 会议设置CRUD |
| `portal/service/impl/ContactServiceImpl.java` | ContactServiceImpl | 5.2KB | 联系人CRUD |
| `portal/service/impl/ArchitectureServiceImpl.java` | ArchitectureServiceImpl | 5.4KB | 会议架构CRUD |
| `portal/service/impl/QuestionServiceImpl.java` | QuestionServiceImpl | 5.1KB | 问题解答CRUD |
| `portal/service/impl/ArrangeServiceImpl.java` | ArrangeServiceImpl | 5.0KB | 会前安排CRUD |
| `portal/service/impl/GuestServiceImpl.java` | GuestServiceImpl | 5.0KB | 特邀嘉宾CRUD |
| `portal/service/impl/MomentsServiceImpl.java` | MomentsServiceImpl | 5.0KB | 精彩瞬间CRUD |
| `portal/service/impl/HotelServiceImpl.java` | HotelServiceImpl | 4.9KB | 酒店信息CRUD |
| `portal/service/impl/SpeechServiceImpl.java` | SpeechServiceImpl | 4.8KB | 演讲集锦CRUD |
| `portal/service/impl/DocxToPdfServiceImpl.java` | DocxToPdfServiceImpl | 6.5KB | DOCX转PDF（JODConverter） |
| `portal/service/impl/UserCheckInServiceImpl.java` | UserCheckInServiceImpl | 2.4KB | 签到记录CRUD |
| `portal/service/impl/SpeechServiceV1Impl.java` | SpeechServiceV1Impl | 2.6KB | 演讲V1兼容查询 |
| `portal/service/impl/DiscountConfigurationServiceImpl.java` | DiscountConfigurationServiceImpl | 1.5KB | 折扣配置CRUD |

### 支付策略模式
| 文件 | 类 | 接口 | 支付渠道 |
|------|-----|------|----------|
| `portal/payment/PaymentRouter.java` | PaymentRouter | — | 路由分发，按paymentMethod选择Provider |
| `portal/payment/PaymentProvider.java` | PaymentProvider | 接口 | createPayment(), refundPayment() |
| `portal/payment/impl/StripePaymentProvider.java` | StripePaymentProvider | PaymentProvider | Stripe信用卡 |
| `portal/payment/impl/WechatPaymentProvider.java` | WechatPaymentProvider | PaymentProvider | 微信支付 |
| `portal/payment/impl/AlipayPaymentProvider.java` | AlipayPaymentProvider | PaymentProvider | 支付宝 |
| `portal/payment/impl/BankTransferPaymentProvider.java` | BankTransferPaymentProvider | PaymentProvider | 银行转账 |

### Entity层
| 实体 | 表名 | 业务域 |
|------|------|--------|
| RegisterUserInformation | register_user_information | 注册 |
| RegistrationType | registration_type | 注册类型 |
| PaymentOrder | payment_orders | 支付 |
| PaperSubmission | paper_submission | 论文投稿 |
| PaperSubmissionEmail | paper_submission_email | 论文邮件 |
| MeetingInfo | meeting_info | 会议日程 |
| MeetingSession | meeting_session | 会议Session |
| MeetingAgendaItem | meeting_agenda_item | 议程项 |
| MeetingSessionParticipant | meeting_session_participant | Session参与者 |
| MeetingSpeaker | meeting_speaker | 演讲者 |
| MeetingSetting | portal_meeting_setting | 会议设置 |
| MeetingDate | meeting_date | 会议日期 |
| MeetingPeriod | meeting_period | 会议时段 |
| Guest | portal_guest | 特邀嘉宾 |
| Hotel | portal_hotel | 酒店信息 |
| Contact | portal_contact | 联系人 |
| Architecture | portal_architecture | 会议架构 |
| Arrange | portal_arrange | 会前安排 |
| Moments | portal_moments | 精彩瞬间 |
| Speech | speech | 演讲集锦 |
| SpeechV1 | speech_v1 | 演讲V1兼容 |
| Question | portal_question | 问题解答 |
| QuestionnaireSurvey | questionnaire_survey | 问卷 |
| DiscountConfiguration | discount_configuration | 折扣配置 |
| UserCheckIn | user_check_in | 签到 |

---

## 5. 邮件子系统（support/mail）

### Controller层
| 文件 | 类 | 端点前缀 | 端点数 |
|------|-----|----------|--------|
| `mail/controller/MailSendRecordController.java` | MailSendRecordController | `/api/v1/mailRecord` | 6 |
| `mail/controller/MailBounceRecordController.java` | MailBounceRecordController | `/api/v1/mailBounceRecord` | 4 |
| `mail/controller/MailBounceBlacklistController.java` | MailBounceBlacklistController | `/api/v1/mailBlacklist` | 4 |

### Service层（核心）
| 文件 | 类 | 大小 | 关键职责 |
|------|-----|------|----------|
| `mail/service/impl/MailSendRecordServiceImpl.java` | MailSendRecordServiceImpl | 39.3KB | 邮件发送、重试、取消、9态状态机 |
| `mail/job/BounceEmailJob.java` | BounceEmailJob | 46.2KB | IMAP退信检测+RFC3464解析+黑名单 |
| `mail/parser/BounceParser.java` | BounceParser | 26.6KB | RFC3464 DSN/XAR/状态码解析 |
| `mail/service/impl/MailBounceRecordServiceImpl.java` | MailBounceRecordServiceImpl | 6.3KB | 退信记录CRUD |
| `mail/service/impl/MailBounceBlacklistServiceImpl.java` | MailBounceBlacklistServiceImpl | 5.0KB | 黑名单CRUD+自动添加 |
| `mail/service/impl/MailServiceImpl.java` | MailServiceImpl | 7.2KB | JavaMailSender封装 |
| `mail/service/impl/MailImapCheckpointServiceImpl.java` | MailImapCheckpointServiceImpl | 3.6KB | IMAP检查点管理 |

### 枚举层
| 文件 | 类 | 用途 |
|------|-----|------|
| `mail/enums/MailSendStatus.java` | MailSendStatus | 9态：PENDING→QUEUED→SENDING→SENT→DELIVERED→FAILED→CANCELLED→BOUNCED→RETRYING |
| `mail/enums/DeliveryStatus.java` | DeliveryStatus | 投递状态 |
| `mail/enums/BounceType.java` | BounceType | 退信类型（HARD/SOFT/UNKNOWN） |
| `mail/enums/ImapExecutionStatus.java` | ImapExecutionStatus | IMAP执行状态 |
| `mail/enums/MailBusinessType.java` | MailBusinessType | 业务类型枚举 |

### Entity层
| 实体 | 表名 | 关键字段 |
|------|------|----------|
| MailSendRecord | mail_send_record | status, retry_count, max_retries, business_type, recipient |
| MailBounceRecord | mail_bounce_record | bounce_type, diagnostic_code, matched_record_id |
| MailBounceBlacklist | mail_bounce_blacklist | email, bounce_count, source |
| MailImapCheckpoint | mail_imap_checkpoint | folder_name, last_uid, last_fetch_time |

---

## 6. 配置层（config）

### 安全与认证
| 文件 | 类 | 职责 |
|------|-----|------|
| `config/SecurityConfig.java` | SecurityConfig | Spring Security配置，4种AuthenticationProvider |
| `config/property/SecurityProperties.java` | SecurityProperties | 白名单路径、JWT配置 |
| `config/property/ImapProperties.java` | ImapProperties | IMAP连接配置（主机/端口/协议） |
| `config/property/MailProperties.java` | MailProperties | SMTP配置 |
| `config/property/CaptchaProperties.java` | CaptchaProperties | 验证码配置 |

### 支付配置
| 文件 | 类 | 职责 |
|------|-----|------|
| `config/StripeConfig.java` | StripeConfig | Stripe SDK初始化 |
| `config/property/StripeProperties.java` | StripeProperties | Stripe密钥配置 |
| `config/AlipayConfig.java` | AlipayConfig | 支付宝SDK初始化 |
| `config/property/AlipayProperties.java` | AlipayProperties | 支付宝配置 |
| `config/WechatPayConfig.java` | WechatPayConfig | 微信支付SDK初始化(10.3KB) |
| `config/property/WechatPayProperties.java` | WechatPayProperties | 微信支付配置 |
| `config/property/BankTransferProperties.java` | BankTransferProperties | 银行转账信息配置 |

### 框架配置
| 文件 | 类 | 职责 |
|------|-----|------|
| `config/MybatisConfig.java` | MybatisConfig | MyBatis-Plus配置、数据权限拦截器 |
| `config/MyDataPermissionHandler.java` | MyDataPermissionHandler | 数据权限SQL注入(10.4KB) |
| `config/MyMetaObjectHandler.java` | MyMetaObjectHandler | 自动填充create_time/update_time |
| `config/RedisConfig.java` | RedisConfig | Redis序列化配置 |
| `config/RedisCacheConfig.java` | RedisCacheConfig | Spring Cache+Redis配置 |
| `config/CaffeineConfig.java` | CaffeineConfig | 本地缓存配置 |
| `config/AsyncConfig.java` | AsyncConfig | 异步线程池配置 |
| `config/CorsConfig.java` | CorsConfig | 跨域配置 |
| `config/JacksonConfig.java` | JacksonConfig | JSON序列化配置 |
| `config/WebSocketConfig.java` | WebSocketConfig | WebSocket配置(11.5KB) |
| `config/OpenApiConfig.java` | OpenApiConfig | Swagger/OpenAPI配置 |
| `config/XxlJobConfig.java` | XxlJobConfig | XXL-JOB定时任务配置 |

---

## 7. 通用模块（module）

### 代码生成器
| 文件 | 类 | 职责 |
|------|-----|------|
| `module/codegen/controller/CodegenController.java` | CodegenController | 代码生成API |
| `module/codegen/service/impl/CodegenServiceImpl.java` | CodegenServiceImpl | 代码生成核心逻辑(16.9KB) |
| `module/codegen/service/impl/GenTableServiceImpl.java` | GenTableServiceImpl | 生成表管理 |
| `module/codegen/mapper/DatabaseMapper.java` | DatabaseMapper | 数据库元数据查询 |

### 文件服务
| 文件 | 类 | 职责 |
|------|-----|------|
| `module/file/controller/FileController.java` | FileController | 文件上传/删除API |
| `module/file/service/impl/LocalFileService.java` | LocalFileService | 本地文件存储 |
| `module/file/service/impl/AliyunFileService.java` | AliyunFileService | 阿里云OSS存储 |
| `module/file/service/impl/MinioFileService.java` | MinioFileService | MinIO存储 |

---

## 8. 前端架构（Admin）

### 目录结构
```
frontend/src/
├── api/            API请求层（5文件）
├── assets/         静态资源
├── components/     公共组件
├── composables/    组合式函数（3文件）
├── constants/      常量定义
├── directives/     自定义指令
├── enums/          枚举（6文件）
├── lang/           国际化（2文件）
├── layouts/        布局组件（6文件）
├── plugins/        插件（2文件）
├── router/         路由配置
├── store/          Pinia状态管理
├── styles/         样式（4文件）
├── types/          TypeScript类型（5文件）
├── utils/          工具函数（9文件）
└── views/          页面视图
    ├── checkIn/            签到管理
    ├── codegen/            代码生成
    ├── contribution/       论文投稿
    ├── dashboard/          仪表盘
    ├── discountConfiguration/ 折扣配置
    ├── emailConfiguration/ 邮件配置
    ├── hotelRecords/       酒店记录
    ├── login/              登录
    ├── mailRecord/         邮件记录
    ├── paperSubmission/    论文投稿管理
    ├── payMoney/           支付管理
    ├── portal/             门户管理
    ├── profile/            个人中心
    ├── report/             报表
    ├── system/             系统管理
    └── userNum/            用户统计
```

---

## 9. 前端架构（Webpage）

### 目录结构
```
webpage/src/
├── api/            API请求层（9文件）
├── assets/         静态资源
├── components/     公共组件
├── composables/    组合式函数（1文件）
├── data/           数据配置（5文件）
├── locales/        国际化（3文件）
├── pages/          页面（14文件）
├── router/         路由
├── stores/         Pinia状态（5文件）
└── utils/          工具函数（5文件）
```

---

## 10. 关键代码定位索引

### 按业务场景定位
| 场景 | 入口文件 | 核心Service | 数据表 |
|------|----------|-------------|--------|
| 用户登录 | AuthController | AuthServiceImpl | sys_user, sys_user_social |
| 角色权限 | RoleController | RoleServiceImpl | sys_role, sys_role_menu, sys_role_dept |
| 菜单路由 | MenuController | MenuServiceImpl | sys_menu |
| 数据权限 | MyDataPermissionHandler | — | sys_role, sys_dept |
| 用户注册 | RegisterUserController | RegisterUserServiceImpl | register_user_information |
| 论文投稿 | PaperSubmissionController | PaperSubmissionServiceImpl | paper_submission, paper_submission_email |
| 论文PDF | PaperSubmissionController:exportPdf | PaperPdfGeneratorServiceImpl | paper_submission |
| 支付创建 | PaymentController | PaymentServiceImpl→PaymentRouter | payment_orders |
| Stripe回调 | StripeWebhookController | PaymentServiceImpl | payment_orders |
| 支付宝回调 | AlipayCallbackController | PaymentServiceImpl | payment_orders |
| 微信回调 | WechatCallbackController | PaymentServiceImpl | payment_orders |
| 邮件发送 | MailSendRecordController | MailSendRecordServiceImpl | mail_send_record |
| 退信检测 | BounceEmailJob(XXL-JOB) | BounceEmailJob | mail_bounce_record, mail_bounce_blacklist |
| 会议日程 | MeetingScheduleController | MeetingInfoServiceImpl | meeting_info, meeting_session, meeting_agenda_item |
| 签到 | RegisterUserController:checkIn | RegisterUserServiceImpl | user_check_in |

### 按技术关注点定位
| 关注点 | 文件 | 说明 |
|--------|------|------|
| JWT认证 | SecurityConfig, SecurityProperties | 4种AuthenticationProvider |
| 数据权限 | MyDataPermissionHandler(10.4KB) | 5级数据范围SQL注入 |
| 邮件9态状态机 | MailSendRecordServiceImpl(39.3KB) | PENDING→QUEUED→SENDING→SENT→DELIVERED→FAILED→CANCELLED→BOUNCED→RETRYING |
| 退信解析 | BounceParser(26.6KB) | RFC3464 DSN/XAR/状态码 |
| 支付策略 | PaymentRouter→4个Provider | 策略模式路由 |
| PDF生成 | PaperPdfGeneratorServiceImpl(10.9KB) | 封面页+信息页+合并 |
| DOCX转PDF | DocxToPdfServiceImpl(6.5KB) | JODConverter+LibreOffice |
| i18n双轨 | Entity字段_en后缀+country_type | 行级vs列级国际化 |
| WebSocket | WebSocketServiceImpl(6.6KB) | 在线用户数+通知推送 |
| 定时任务 | BounceEmailJob(46.2KB) | XXL-JOB调度 |

### 大文件风险清单（>10KB）
| 文件 | 大小 | 风险 |
|------|------|------|
| BounceEmailJob.java | 46.2KB | CODE-001: 过大，应拆分 |
| MailSendRecordServiceImpl.java | 39.3KB | 邮件发送核心，复杂度高 |
| PaperSubmissionServiceImpl.java | 37.5KB | 论文投稿核心，含PDF/邮件/审核 |
| RegisterUserServiceImpl.java | 25.2KB | 注册核心，含折扣/签到/审核 |
| BounceParser.java | 26.6KB | RFC3464解析，正则密集 |
| WechatPayConfig.java | 10.3KB | 微信支付配置复杂 |
| MyDataPermissionHandler.java | 10.4KB | 数据权限SQL拼接 |
| MenuServiceImpl.java | 17.9KB | 菜单树+路由生成 |
| CodegenServiceImpl.java | 16.9KB | 代码生成模板 |
| PaperSubmissionController.java | 17.5KB | 控制器过大(CODE-006) |

> 文档生成时间：2026-08-14 | 基于代码扫描，未经人工校验