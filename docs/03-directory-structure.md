# MCLC 目录结构文档

> 后端Java包结构 + 前端Vue目录结构 + 文件命名规范

## 1. 后端包结构

```
com.youlai.boot/
├── auth/                          # 认证模块
│   ├── controller/                # 认证控制器
│   │   ├── AuthController         # 管理端登录/验证码/登出/刷新令牌
│   │   └── WechatMiniappAuthController  # 微信小程序认证(静默/手机号/绑定)
│   ├── model/
│   │   ├── dto/LoginRequest       # 登录请求DTO
│   │   └── vo/
│   │       ├── CaptchaVO          # 验证码响应
│   │       └── WechatMiniappLoginResult  # 微信登录结果
│   └── service/
│       ├── AuthService            # 认证服务接口
│       ├── WechatMiniappAuthService  # 微信认证服务接口
│       └── impl/
│           ├── AuthServiceImpl    # 认证服务实现
│           └── WechatMiniappAuthServiceImpl  # 微信认证实现
│
├── common/                        # 公共模块
│   ├── annotation/                # 自定义注解
│   │   ├── DataPermission         # 数据权限注解
│   │   ├── Log                    # 操作日志注解
│   │   ├── RepeatSubmit           # 防重提交注解
│   │   └── ValidField             # 字段校验注解
│   ├── base/                      # 基础类
│   │   ├── BaseEntity             # 实体基类(id/createTime/updateTime)
│   │   ├── BaseQuery              # 查询基类
│   │   └── IBaseEnum              # 枚举接口
│   ├── constant/                  # 常量
│   │   ├── JwtClaimConstants      # JWT Payload字段常量
│   │   ├── RedisConstants         # Redis Key常量
│   │   ├── SecurityConstants      # 安全常量(Bearer前缀)
│   │   └── SystemConstants        # 系统常量
│   ├── enums/                     # 枚举
│   │   ├── DataScopeEnum          # 数据权限范围(5种)
│   │   ├── LogModuleEnum          # 日志模块枚举
│   │   └── ...
│   ├── model/                     # 公共模型
│   │   ├── KeyValue               # 键值对
│   │   └── Option                 # 选项
│   └── util/                      # 工具类
│       ├── DateUtils              # 日期工具
│       ├── ExcelUtils             # Excel工具
│       ├── IPUtils                # IP工具
│       └── QRCodeUtils            # 二维码生成工具
│
├── config/                        # 配置类(23个)
│   ├── SecurityConfig             # Spring Security配置
│   ├── MybatisConfig              # MyBatis配置(数据权限拦截器)
│   ├── MyDataPermissionHandler    # 数据权限处理器
│   ├── WebSocketConfig            # WebSocket配置
│   ├── CorsConfig                 # 跨域配置
│   ├── AsyncConfig                # 异步配置
│   ├── MailConfig                 # 邮件配置
│   ├── AlipayConfig/WechatPayConfig/StripeConfig  # 支付配置
│   ├── RedisConfig/RedisCacheConfig  # Redis配置
│   ├── CaptchaConfig              # 验证码配置
│   ├── JacksonConfig              # JSON序列化配置
│   ├── DruidConfig                # 数据源配置
│   ├── OpenApiConfig/Knife4jOpenApiCustomizer  # API文档配置
│   └── property/                  # 配置属性类(11个)
│       ├── SecurityProperties     # 安全配置(ignoreUrls/JWT)
│       ├── MailProperties/ImapProperties  # 邮件/IMAP配置
│       ├── AlipayProperties/WechatPayProperties/StripeProperties/BankTransferProperties  # 支付配置
│       ├── CaptchaProperties      # 验证码配置
│       ├── AliyunSmsProperties    # 短信配置
│       ├── CodegenProperties      # 代码生成配置
│       └── WxMaProperties         # 微信小程序配置
│
├── core/                          # 核心模块
│   ├── aspect/                    # AOP切面
│   │   ├── LogAspect              # 操作日志切面
│   │   └── RepeatSubmitAspect     # 防重提交切面
│   ├── exception/                 # 异常处理
│   │   ├── BusinessException      # 业务异常
│   │   └── GlobalExceptionHandler # 全局异常处理器
│   ├── filter/                    # 过滤器
│   │   ├── RateLimiterFilter      # 限流过滤器
│   │   └── RequestLogFilter       # 请求日志过滤器
│   ├── validator/                 # 校验器
│   │   └── FieldValidator         # 字段校验器
│   └── web/                       # Web基础设施
│       ├── Result                 # 统一响应Result<T>
│       ├── ResultCode             # 响应码枚举
│       ├── PageResult             # 分页响应
│       ├── ExcelResult            # Excel导入结果
│       └── WebResponseWriter      # 响应写入器
│
├── module/                        # 功能模块
│   ├── codegen/                   # 代码生成模块
│   │   ├── controller/CodegenController
│   │   ├── converter/CodegenConverter
│   │   ├── enums/(FormTypeEnum/JavaTypeEnum/QueryTypeEnum)
│   │   ├── mapper/(DatabaseMapper/GenTableMapper/GenTableColumnMapper)
│   │   ├── model/(entity/bo/form/query/vo)
│   │   └── service/(CodegenService/GenTableService/GenTableColumnService + impl)
│   └── file/                      # 文件上传模块
│       ├── controller/FileController
│       ├── model/FileInfo
│       └── service/(FileService + impl: AliyunFileService/LocalFileService/MinioFileService)
│
├── portal/                        # 门户业务模块(核心)
│   ├── controller/                # 21个控制器
│   │   ├── RegisterUserController      # 注册用户管理
│   │   ├── PaymentController           # 支付
│   │   ├── PaperSubmissionController   # 论文投稿(17.5KB最大)
│   │   ├── MeetingScheduleController   # 会议日程
│   │   ├── MeetingSettingController    # 会议设置
│   │   ├── PortalAuthController        # 门户邮箱登录
│   │   ├── PortalController            # 门户首页
│   │   ├── UserCheckInController       # 签到
│   │   ├── AlipayCallbackController    # 支付宝回调
│   │   ├── WechatCallbackController    # 微信回调
│   │   ├── StripeWebhookController     # Stripe Webhook
│   │   └── (Guest/Hotel/Contact/Question/Speech/Architecture/Arrange/Moments/DiscountConfiguration/MeetDate)
│   ├── converter/                 # 13个MapStruct转换器
│   ├── enums/                     # 支付枚举
│   │   ├── PaymentMethodEnum      # 支付方式(4种)
│   │   └── PaymentStatusEnum      # 支付状态
│   ├── mapper/                    # 24个Mapper
│   ├── model/
│   │   ├── dto/EmailLoginRequest  # 邮箱登录请求
│   │   ├── entity/                # 24个实体
│   │   ├── form/                  # 23个表单
│   │   ├── query/                 # 12个查询
│   │   └── vo/                    # 21个VO
│   ├── payment/                   # 支付策略模式
│   │   ├── PaymentProvider        # 支付提供者接口
│   │   ├── PaymentRouter          # 支付路由
│   │   ├── CreatePaymentRequest   # 创建支付请求
│   │   ├── PaymentProviderResponse  # 支付响应
│   │   └── impl/                  # 4个实现
│   │       ├── StripePaymentProvider
│   │       ├── WechatPaymentProvider
│   │       ├── AlipayPaymentProvider
│   │       └── BankTransferPaymentProvider
│   └── service/                   # 23个服务接口 + 23个实现
│       ├── RegisterUserService         # 注册用户服务
│       ├── PaymentServiceImpl          # 支付服务(12.1KB)
│       ├── PaperSubmissionServiceImpl  # 论文投稿服务(37.5KB，最大)
│       ├── MeetingInfoServiceImpl      # 会议信息服务(19.8KB)
│       ├── MeetingDateServiceImpl      # 会议日期服务(13.6KB)
│       └── ...
│
├── security/                      # 安全模块
│   ├── exception/                 # 认证异常
│   │   ├── CaptchaValidationException  # 验证码异常
│   │   └── NeedBindMobileException     # 需绑定手机号异常
│   ├── filter/                    # 安全过滤器
│   │   ├── TokenAuthenticationFilter   # JWT Token过滤器
│   │   └── CaptchaValidationFilter     # 验证码校验过滤器
│   ├── handler/                   # 安全处理器
│   │   ├── MyAccessDeniedHandler       # 无权限处理器
│   │   └── MyAuthenticationEntryPoint  # 未认证处理器
│   ├── model/                     # 安全模型
│   │   ├── SysUserDetails              # 用户认证详情
│   │   ├── UserAuthInfo               # 用户认证信息
│   │   ├── AuthenticationToken        # 令牌响应
│   │   ├── RoleDataScope              # 角色数据权限
│   │   ├── SmsAuthenticationToken     # 短信认证Token
│   │   ├── EmailAuthenticationToken   # 邮箱认证Token
│   │   └── WechatMiniAuthenticationToken  # 微信认证Token
│   ├── provider/                  # 认证提供者
│   │   ├── SmsAuthenticationProvider   # 短信验证码认证
│   │   ├── EmailAuthenticationProvider # 邮箱验证码认证
│   │   └── WechatMiniAuthenticationProvider  # 微信小程序认证
│   ├── service/
│   │   ├── SysUserDetailsService      # 用户详情服务
│   │   └── PermissionService          # 权限服务
│   ├── token/                     # 令牌管理
│   │   ├── TokenManager               # 令牌管理接口
│   │   ├── JwtTokenManager            # JWT令牌管理(16.1KB)
│   │   └── RedisTokenManager         # Redis令牌管理(12.4KB)
│   └── util/
│       └── SecurityUtils              # 安全工具类
│
├── support/                       # 支撑模块
│   └── mail/                      # 邮件子系统
│       ├── controller/            # 3个控制器(SendRecord/BounceRecord/Blacklist)
│       ├── converter/             # 转换器
│       ├── enums/                 # 5个枚举(SendStatus/BusinessType/BounceType/DeliveryStatus/ImapExecutionStatus)
│       ├── job/BounceEmailJob     # 退信检测定时任务
│       ├── mapper/                # 4个Mapper
│       ├── model/(entity/dto/query/vo)  # 邮件模型
│       ├── parser/BounceParser    # RFC3464 DSN退信解析器
│       └── service/               # 5个服务接口+实现(Mail/MailSendRecord/MailBounceRecord/MailBounceBlacklist/MailImapCheckpoint)
│
└── system/                        # 系统管理模块
    ├── controller/                # 11个控制器
    │   ├── UserController              # 用户管理(11.3KB)
    │   ├── RoleController              # 角色管理
    │   ├── MenuController              # 菜单管理
    │   ├── DeptController              # 部门管理
    │   ├── DictController/DictItemController  # 字典管理
    │   ├── ConfigController            # 系统配置
    │   ├── NoticeController            # 通知公告
    │   ├── EmailTemplateController     # 邮件模板
    │   ├── LogController               # 操作日志
    │   ├── StatisticsController        # 统计
    │   └── RegistrationTypeController  # 注册类型
    ├── converter/                 # 10个转换器
    ├── enums/                     # 5个枚举
    ├── handler/XxlJobSampleHandler  # XXL-Job示例
    ├── listener/UserImportListener  # 用户导入监听器
    ├── mapper/                    # 16个Mapper
    ├── model/(bo/dto/entity/form/query/vo)  # 系统模型
    └── service/                   # 16个服务接口+实现
```

## 2. 前端Admin目录结构

```
frontend/src/
├── api/                           # API接口
│   ├── auth.ts                    # 认证API(登录/登出/刷新/验证码)
│   ├── codegen.ts                 # 代码生成API
│   ├── file.ts                    # 文件上传API
│   ├── portal/                    # 门户业务API(17个)
│   │   ├── registerUser.ts        # 注册用户
│   │   ├── paperSubmission.ts     # 论文投稿
│   │   ├── meeting.ts             # 会议日程
│   │   ├── mailSendRecord.ts      # 邮件发送记录
│   │   ├── mailBounceRecord.ts    # 退信记录
│   │   ├── mailBlacklist.ts       # 黑名单
│   │   ├── guest.ts               # 嘉宾
│   │   ├── hotel.ts               # 酒店
│   │   ├── contact.ts             # 联系方式
│   │   ├── speech.ts              # 演讲
│   │   ├── setting.ts             # 会议设置
│   │   ├── answer.ts              # 问卷回答
│   │   ├── architecture.ts        # 架构
│   │   ├── arrange.ts             # 安排
│   │   ├── moments.ts             # 精彩瞬间
│   │   ├── discountConfiguration.ts  # 折扣配置
│   │   └── userCheckIn.ts         # 签到
│   └── system/                    # 系统管理API(13个)
│       ├── user.ts, role.ts, menu.ts, dept.ts
│       ├── dict.ts, dictItem.ts
│       ├── config.ts, notice.ts, log.ts
│       ├── emailTemplate.ts, registration.ts
│       └── ...
│
├── views/                         # 页面视图
│   ├── login/                     # 登录页
│   ├── dashboard/                 # 仪表盘
│   ├── system/                    # 系统管理
│   │   ├── user/                  # 用户管理
│   │   ├── role/                  # 角色管理
│   │   ├── menu/                  # 菜单管理
│   │   ├── dept/                  # 部门管理
│   │   ├── dict/                  # 字典管理
│   │   ├── config/                # 系统配置
│   │   ├── notice/                # 通知公告
│   │   ├── log/                   # 操作日志
│   │   ├── registration/          # 注册类型
│   │   └── tenant/                # 租户管理
│   ├── portal/                    # 门户管理
│   │   ├── guest/                 # 嘉宾管理
│   │   ├── hotel/                 # 酒店管理
│   │   ├── contact/               # 联系方式
│   │   ├── meeting/               # 会议日程
│   │   ├── setting/               # 会议设置
│   │   ├── arrange/               # 安排管理
│   │   ├── answer/                # 问卷回答
│   │   └── home/                  # 门户首页
│   ├── paperSubmission/           # 论文投稿
│   ├── payMoney/                  # 支付管理
│   ├── mailRecord/                # 邮件记录
│   ├── emailConfiguration/        # 邮件配置
│   ├── hotelRecords/              # 酒店记录
│   ├── discountConfiguration/     # 折扣配置
│   ├── userNum/                   # 用户统计
│   ├── report/                    # 报表
│   ├── contribution/              # 贡献
│   ├── checkIn/                   # 签到
│   ├── codegen/                   # 代码生成
│   ├── profile/                   # 个人中心
│   └── error/                     # 错误页
│
├── router/                        # 路由配置
├── store/                         # Pinia状态管理
├── composables/                   # 组合式函数
├── components/                    # 公共组件
├── layouts/                       # 布局组件
├── plugins/                       # 插件
├── directives/                    # 自定义指令
├── enums/                         # 枚举
├── constants/                     # 常量
├── lang/                          # 国际化
├── styles/                        # 样式
├── types/                         # TypeScript类型
├── utils/                         # 工具函数
├── assets/                        # 静态资源
├── App.vue                        # 根组件
├── main.ts                        # 入口文件
└── settings.ts                    # 应用设置
```

## 3. 文件命名规范

### 3.1 后端命名规范

| 类型 | 命名规则 | 示例 |
|------|----------|------|
| Controller | {Entity}Controller | RegisterUserController |
| Service | {Entity}Service | RegisterUserService |
| ServiceImpl | {Entity}ServiceImpl | RegisterUserServiceImpl |
| Mapper | {Entity}Mapper | RegisterUserInformationMapper |
| Entity | {TableName} | RegisterUserInformation |
| Form | {Entity}Form | RegisterUserForm |
| Query | {Entity}Query | RegisterUserQuery |
| PageVO | {Entity}PageVO | RegisterUserPageVO |
| Converter | {Entity}Converter | RegisterUserConverter |
| Enum | {Domain}Enum | PaymentMethodEnum |

### 3.2 前端命名规范

| 类型 | 命名规则 | 示例 |
|------|----------|------|
| API文件 | camelCase.ts | registerUser.ts |
| 页面目录 | camelCase/ | paperSubmission/ |
| 组件文件 | PascalCase.vue | UserForm.vue |
| 工具文件 | camelCase.ts | dateFormat.ts |

## 4. 模块-包映射

| 业务模块 | 后端包 | 前端页面 | 数据库表 |
|----------|--------|----------|----------|
| 认证 | auth/ | login/ | sys_user, sys_user_social |
| 用户管理 | system/ | system/user/ | sys_user |
| 角色管理 | system/ | system/role/ | sys_role, sys_user_role, sys_role_menu, sys_role_dept |
| 菜单管理 | system/ | system/menu/ | sys_menu |
| 部门管理 | system/ | system/dept/ | sys_dept |
| 字典管理 | system/ | system/dict/ | sys_dict, sys_dict_item |
| 系统配置 | system/ | system/config/ | sys_config |
| 通知公告 | system/ | system/notice/ | sys_notice, sys_user_notice |
| 邮件模板 | system/ | - | sys_email_template |
| 操作日志 | system/ | system/log/ | sys_log |
| 注册用户 | portal/ | portal/ | register_user_information |
| 论文投稿 | portal/ | paperSubmission/ | paper_submission, paper_submission_email |
| 支付 | portal/ | payMoney/ | payment_orders |
| 会议日程 | portal/ | portal/meeting/ | meeting_info, meeting_session, meeting_agenda_item, meeting_speaker, meeting_session_participant |
| 会议设置 | portal/ | portal/setting/ | portal_meeting_setting |
| 嘉宾 | portal/ | portal/guest/ | portal_guest |
| 酒店 | portal/ | portal/hotel/ | portal_hotel |
| 联系方式 | portal/ | portal/contact/ | portal_question, portal_contact |
| 邮件子系统 | support/mail/ | mailRecord/, emailConfiguration/ | mail_send_record, mail_bounce_record, mail_bounce_blacklist, mail_imap_checkpoint |
| 签到 | portal/ | checkIn/ | user_check_in |
| 代码生成 | module/codegen/ | codegen/ | gen_table, gen_table_column |
| 文件上传 | module/file/ | - | (OSS/本地/Minio) |