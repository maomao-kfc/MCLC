# MCLC 项目总览

## 项目简介

MCLC（国际学术会议）管理系统是一个面向国际学术会议的全流程管理平台，涵盖会议信息管理、在线注册、论文投稿、支付结算、邮件通信、签到管理等核心业务。

## 技术栈

### 后端
| 技术 | 版本 | 用途 |
|------|------|------|
| Java | 17 | 运行时 |
| Spring Boot | 4.0.1 | 应用框架 |
| Spring Security | 6.x | 认证授权 |
| MyBatis-Plus | 3.5.15 | ORM框架 |
| MySQL | 8.0.45 | 关系数据库 |
| Redis | - | 缓存/会话 |
| Druid | 1.2.28 | 连接池 |
| Knife4j | 4.5.0 | API文档 |
| MapStruct | 1.6.3 | 对象映射 |
| XXL-Job | 3.2.0 | 定时任务 |
| Lombok | - | 代码简化 |
| Hutool | 5.8.41 | 工具库 |
| Redisson | 4.1.0 | 分布式锁 |
| FastExcel | 1.3.0 | Excel导入导出 |
| Minio | 8.5.10 | 对象存储 |
| Aliyun OSS | 3.16.3 | 对象存储 |
| Aliyun SMS | 2.2.1 | 短信服务 |
| ZXing | 3.5.3 | 二维码生成 |
| WxJava | 4.8.1.B | 微信小程序 |
| Stripe SDK | - | 国际信用卡支付 |
| Alipay SDK | - | 支付宝支付 |
| WeChat Pay SDK | - | 微信支付 |
| Spring Boot Admin | 4.0.2 | 应用监控 |
| Caffeine | 2.9.3 | 本地缓存 |
| IP2Region | 2.7.0 | IP地域解析 |
| JavaMail | - | 邮件发送 |
| JODConverter + LibreOffice | - | 文档转PDF |

### 前端（Admin管理后台）
| 技术 | 版本 | 用途 |
|------|------|------|
| Vue | 3.x | UI框架 |
| Vite | - | 构建工具 |
| TypeScript | - | 类型安全 |
| Element Plus | - | UI组件库 |
| Pinia | - | 状态管理 |
| Vue Router | - | 路由管理 |
| Vue I18n | - | 国际化 |
| UnoCSS | - | 原子CSS |
| VueUse | - | 组合式工具 |

### 前端（Webpage公开网站）
| 技术 | 用途 |
|------|------|
| Vue 3 | UI框架 |
| Vite | 构建工具 |
| JavaScript | 开发语言 |

## 项目结构概览

```
mclc-admin-main/
├── backend/                    # 后端 Spring Boot 项目
│   ├── src/main/java/com/youlai/boot/
│   │   ├── auth/               # 认证模块（登录、微信小程序登录）
│   │   ├── common/             # 公共模块（注解、基础类、常量、枚举、工具）
│   │   ├── config/             # 配置类（23个配置文件）
│   │   ├── core/               # 核心模块（AOP、异常、过滤器、Web响应）
│   │   ├── module/
│   │   │   ├── codegen/        # 代码生成器
│   │   │   └── file/           # 文件上传（阿里云/本地/Minio）
│   │   ├── portal/             # 门户业务模块（核心业务）
│   │   │   ├── controller/     # 21个控制器
│   │   │   ├── converter/      # 13个MapStruct转换器
│   │   │   ├── enums/          # 支付枚举
│   │   │   ├── mapper/         # 24个Mapper接口
│   │   │   ├── model/          # 实体/DTO/VO/Form/Query
│   │   │   ├── payment/        # 支付策略模式实现
│   │   │   └── service/        # 23个Service接口+实现
│   │   ├── support/            # 支撑模块
│   │   │   ├── mail/           # 邮件子系统（发送/退信/黑名单/IMAP）
│   │   │   ├── sms/            # 短信服务（阿里云）
│   │   │   └── websocket/      # WebSocket实时通信
│   │   └── system/             # 系统管理模块
│   │       ├── controller/     # 11个控制器
│   │       ├── converter/      # 10个转换器
│   │       ├── enums/          # 系统枚举
│   │       ├── handler/        # XXL-Job处理器
│   │       ├── listener/       # 用户导入监听器
│   │       ├── mapper/         # 16个Mapper接口
│   │       ├── model/          # 实体/DTO/VO/Form/Query
│   │       └── service/        # 16个Service接口+实现
│   ├── src/main/resources/
│   │   ├── application.yml     # 主配置
│   │   ├── application-dev.yml # 开发环境配置
│   │   ├── application-prod.yml# 生产环境配置
│   │   ├── mapper/             # MyBatis XML映射文件
│   │   ├── templates/          # 代码生成模板 + Excel模板
│   │   └── cert/               # 微信支付证书
│   ├── sql/                    # SQL脚本
│   └── pom.xml                 # Maven依赖
├── frontend/                   # Admin管理后台（Vue3 + TS）
│   ├── src/
│   │   ├── api/                # API请求层
│   │   ├── views/              # 页面组件
│   │   ├── components/         # 公共组件
│   │   ├── composables/        # 组合式函数
│   │   ├── constants/          # 常量定义
│   │   ├── directives/         # 自定义指令
│   │   ├── enums/              # 枚举定义
│   │   ├── lang/               # 国际化语言包
│   │   ├── layouts/            # 布局组件
│   │   ├── plugins/            # 插件
│   │   ├── router/             # 路由配置
│   │   ├── store/              # Pinia状态管理
│   │   ├── styles/             # 全局样式
│   │   ├── types/              # TypeScript类型
│   │   └── utils/              # 工具函数
│   └── vite.config.ts          # Vite配置
├── webpage/                    # 公开会议网站（Vue3 + JS）
│   ├── src/
│   │   ├── api/                # API请求
│   │   ├── pages/              # 页面
│   │   ├── stores/             # 状态管理
│   │   ├── locales/            # 国际化
│   │   └── utils/              # 工具函数
│   └── vite.config.js
└── docs/                       # 技术文档
```

## 核心业务模块

| 模块 | 后端包 | 前端页面 | 数据库表 | 说明 |
|------|--------|----------|----------|------|
| 会议日程 | `portal/` | `portal/meeting/` | `meeting_info`, `meeting_session`, `meeting_agenda_item`, `meeting_speaker`, `meeting_session_participant` | 会议天数→分会场→议程项三级层次 |
| 门户CMS | `portal/` | `portal/*` | `portal_*` (10张表) | 嘉宾/酒店/FAQ/联系/安排/瞬间/演讲/架构/设置 |
| 注册管理 | `portal/` | `system/registration/` | `register_user_information`, `registration_type` | 用户注册+签证+账单+审核 |
| 论文投稿 | `portal/` | `paperSubmission/` | `paper_submission`, `paper_submission_email` | 投稿→专家邀请→审批→折扣码 |
| 支付系统 | `portal/` | `payMoney/` | `payment_orders` | 信用卡(Stripe)/微信/支付宝/银行转账 |
| 邮件系统 | `support/mail/` | `mailRecord/` | `mail_send_record`, `mail_bounce_record`, `mail_bounce_blacklist`, `mail_imap_checkpoint` | 发送→退信检测→黑名单→重试 |
| 签到管理 | `portal/` | `checkIn/` | `user_check_in` | 现场签到 |
| 问卷调查 | `portal/` | - | `questionnaire_survey` | 注册附加问卷 |
| 团购折扣 | `portal/` | `discountConfiguration/` | `discount_configuration` | 团体注册折扣规则 |
| RBAC权限 | `system/` | `system/*` | `sys_user`, `sys_role`, `sys_menu`, `sys_dept`, `sys_role_menu`, `sys_role_dept`, `sys_user_role` | 角色/菜单/部门/数据权限 |
| 字典管理 | `system/` | `system/dict/` | `sys_dict`, `sys_dict_item` | 数据字典 |
| 通知公告 | `system/` | `system/notice/` | `sys_notice`, `sys_user_notice` | 系统通知 |
| 邮件模板 | `system/` | `emailConfiguration/` | `sys_email_template` | 富文本邮件模板 |
| 操作日志 | `system/` | - | `sys_log` | 接口调用日志 |
| 系统配置 | `system/` | `system/config/` | `sys_config` | 键值对配置 |
| 第三方登录 | `auth/` | - | `sys_user_social` | 微信/支付宝/QQ/Apple |
| 代码生成 | `module/codegen/` | `codegen/` | `gen_table`, `gen_table_column` | 自动生成CRUD代码 |

## 运行端口

| 服务 | 端口 | 说明 |
|------|------|------|
| 后端 API | 8000 | Spring Boot |
| 前端 Admin | 5173 (默认) | Vite dev server |
| Webpage | - | Vite dev server |
| MySQL | 3306 | 数据库 |
| Redis | 6379 | 缓存 |

## 数据库概览

- **数据库名**: `meeting_admin`
- **字符集**: `utf8mb4` / `utf8mb4_0900_ai_ci`
- **引擎**: InnoDB（`sys_log`使用MyISAM）
- **表数量**: 45张
- **命名规范**:
  - 业务表: `portal_*`（门户CMS）、`meeting_*`（会议日程）、`mail_*`（邮件）、`paper_*`（论文）、`payment_*`（支付）
  - 系统表: `sys_*`
  - 注册表: `register_user_information`、`registration_type`
  - 签到表: `user_check_in`
  - 代码生成: `gen_*`

## 国际化策略

项目采用**双轨国际化**策略：

| 策略 | 适用表 | 实现方式 | 示例 |
|------|--------|----------|------|
| 列级国际化 | `meeting_*` 表 | 同一记录包含中英文字段（`_en`后缀） | `name` + `name_en` |
| 行级国际化 | `portal_*` 表 | 通过 `country_type` 字段区分语言（ZH/EN） | 同一内容两条记录 |

## 关键设计模式

1. **策略模式** - 支付系统: `PaymentProvider` 接口 + `PaymentRouter` 路由 + 4种实现
2. **模板方法** - 代码生成器: Velocity模板自动生成前后端代码
3. **观察者模式** - WebSocket: `WebSocketPublisher` 发布消息，前端实时接收
4. **状态机** - 邮件发送: 9种状态流转 + 重试链
5. **MapStruct** - 对象转换: Entity ↔ Form ↔ VO ↔ Query
6. **MyBatis-Plus** - 数据权限: `DataPermission` 注解 + `MyDataPermissionHandler`