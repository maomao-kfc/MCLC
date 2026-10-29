# MCLC 项目总览

> 最后更新: 2026-10-29

## 项目简介

MCLC（国际学术会议）管理系统是一个面向国际学术会议的全流程管理平台，涵盖会议信息管理、在线注册、论文投稿、支付结算、邮件通信、签到管理等核心业务。

- **基础框架**：后端基于 youlai-boot 4.1.0 二次开发
- **业务定位**：学术会议全生命周期管理平台，服务于MCLC国际学术会议的会议组织工作
- **核心功能**：
  - 会议信息展示与日程管理
  - 嘉宾/演讲者管理
  - 论文投稿与专家审稿
  - 参会注册（9步流程）与多渠道支付
  - 邮件系统（SMTP发送 + IMAP退信检测 + 黑名单管理）
  - 签到管理（二维码生成与验证）
  - 系统管理（用户/角色/菜单/字典/部门/通知/配置）

## 技术栈

### 后端
| 技术 | 版本 | 用途 |
|------|------|------|
| Java | 17 | 运行时 |
| Spring Boot | 4.0.1 | 应用框架 |
| Spring Security | 6.x | 认证授权 |
| MyBatis-Plus | 3.5.15 | ORM框架 |
| MySQL | 8.0.45 | 关系数据库 |
| Redis + Redisson | 4.1.0 | 缓存/分布式锁/Token存储 |
| Druid | 1.2.28 | 连接池 |
| Knife4j | 4.5.0 | API文档 |
| MapStruct | 1.6.3 | 对象映射 |
| XXL-Job | 3.2.0 | 定时任务 |
| Hutool | 5.8.41 | 工具库 |
| FastExcel | 1.3.0 | Excel导入导出 |
| Minio | 8.5.10 | 对象存储 |
| ZXing | 3.5.3 | 二维码生成 |
| Tess4j | 5.13.0 | OCR文字识别 |
| JavaMail | - | 邮件发送 |
| JODConverter + LibreOffice | - | 文档转PDF |
| OpenPDF | - | PDF生成（封面页/信息页） |
| Caffeine | 2.9.3 | 本地缓存 |
| WebSocket (STOMP) | - | 实时推送 |

### 前端（Admin管理后台）
| 技术 | 用途 |
|------|------|
| Vue 3 + TypeScript | UI框架/类型安全 |
| Vite | 构建工具 |
| Element Plus | UI组件库 |
| Pinia | 状态管理 |
| Vue I18n | 国际化 |
| UnoCSS | 原子CSS |

### 前端（Webpage公开网站）
| 技术 | 用途 |
|------|------|
| Vue 3 + JavaScript | UI框架/开发语言 |
| Vite | 构建工具 |
| Element Plus | UI组件库 |
| vue-i18n | 国际化（中英双语） |

## 项目结构概览

```
mclc-admin-main/
├── backend/                    # 后端 Spring Boot 项目
│   ├── src/main/java/com/youlai/boot/
│   │   ├── auth/               # 认证模块
│   │   ├── common/             # 公共模块（注解/基础类/常量/枚举/工具）
│   │   ├── config/             # 配置类
│   │   ├── core/               # 核心模块（AOP/异常/过滤器/Web响应）
│   │   ├── module/             # 代码生成器/文件上传
│   │   ├── portal/             # 门户业务模块（核心业务）
│   │   │   ├── controller/     # 控制器
│   │   │   ├── service/        # Service接口+实现
│   │   │   ├── mapper/         # Mapper接口
│   │   │   ├── payment/        # 支付策略模式实现
│   │   │   └── model/          # 实体/DTO/VO/Form/Query
│   │   ├── support/            # 支撑模块
│   │   │   ├── mail/           # 邮件子系统（发送/退信/黑名单/IMAP）
│   │   │   ├── sms/            # 短信服务
│   │   │   └── websocket/      # WebSocket实时通信
│   │   └── system/             # 系统管理模块
│   ├── sql/                    # SQL脚本
│   └── pom.xml                 # Maven依赖
├── frontend/                   # Admin管理后台（Vue3 + TS）
├── webpage/                    # 公开会议网站（Vue3 + JS）
└── docs/                       # 技术文档
```

## 部署架构

### Docker部署
- 后端：Docker容器（eclipse-temurin:17-jre + LibreOffice + 中文字体）
- 端口映射：8989→8000
- 环境变量：SPRING_PROFILES_ACTIVE=prod, TZ=Asia/Shanghai

### 部署命令
```bash
docker-compose build
docker-compose up -d
docker-compose logs -f
```

### 前端部署
- 管理后台（frontend/）：构建后由Nginx托管
- 前台官网（webpage/）：构建后由Nginx托管

## 环境配置

| 环境 | 配置文件 | 说明 |
|------|---------|------|
| 开发 | application-dev.yml | 本地开发，包含所有第三方服务配置 |
| 生产 | application-prod.yml | 生产环境，敏感配置通过环境变量注入 |

## API规范

- 统一前缀：`/api/v1/`
- 认证方式：JWT AccessToken + Redis RefreshToken 双Token机制
- 统一响应格式：`Result<T>`（code/msg/data）
- 分页响应格式：`PageResult<T>`（records/total/current/size）
- 接口文档：Knife4j UI（`/doc.html`）

## 核心业务模块

| 模块 | 后端包 | 数据库表 | 说明 |
|------|--------|----------|------|
| 会议日程 | `portal/` | `meeting_*` (5张) | 会议天数→分会场→议程项三级层次 |
| 门户CMS | `portal/` | `portal_*` (10张) | 嘉宾/酒店/FAQ/联系/安排/瞬间/演讲 |
| 注册管理 | `portal/` | `register_user_information` | 9步注册+签证+账单+审核 |
| 论文投稿 | `portal/` | `paper_submission` | 投稿→专家邀请→审批→折扣码 |
| 支付系统 | `portal/` | `payment_orders` | 信用卡(Stripe)/微信/支付宝/银行转账 |
| 邮件系统 | `support/mail/` | `mail_*` (4张) | 发送→退信检测→黑名单→重试 |
| 对账系统 | `portal/` | `payment_reconciliation_*` | Excel导入→多维度匹配→人工确认 |
| 签到管理 | `portal/` | `user_check_in` | 现场签到 |
| RBAC权限 | `system/` | `sys_*` (7张) | 角色/菜单/部门/数据权限 |
| 邮件模板 | `system/` | `sys_email_template` | 富文本邮件模板 |

## 运行端口

| 服务 | 端口 | 说明 |
|------|------|------|
| 后端 API | 8000 | Spring Boot |
| 前端 Admin | 3000 | Vite dev server |
| Webpage | 5174 | Vite dev server |
| MySQL | 3306 | 数据库 |
| Redis | 6379 | 缓存（后端使用 db 12） |

## 数据库概览

- **数据库名**: `meeting_admin`
- **字符集**: `utf8mb4` / `utf8mb4_0900_ai_ci`
- **引擎**: InnoDB（`sys_log`使用MyISAM）
- **表数量**: 37张
- **命名规范**:
  - 业务表: `portal_*`（门户CMS）、`meeting_*`（会议日程）、`mail_*`（邮件）、`paper_*`（论文）、`payment_*`（支付）
  - 系统表: `sys_*`
  - 注册表: `register_user_information`、`registration_type`

## 国际化策略

项目采用**双轨国际化**策略：

| 策略 | 适用表 | 实现方式 | 示例 |
|------|--------|----------|------|
| 列级国际化 | `meeting_*` 表 | 同一记录包含中英文字段（`_en`后缀） | `name` + `name_en` |
| 行级国际化 | `portal_*` 表 | 通过 `country_type` 字段区分语言（ZH/EN） | 同一内容两条记录 |

## 关键设计模式与决策

1. **双Token认证** - AccessToken（短有效期）+ RefreshToken（长有效期，存Redis），兼顾安全与用户体验
2. **邮件任务表模式** - 邮件发送采用任务表（mail_send_record），支持重试、取消、退信检测，9种发送状态完整追踪
3. **支付策略模式** - PaymentRouter根据支付方式路由到多种PaymentProvider，新增支付渠道只需实现接口
4. **退信检测** - IMAP定时扫描收件箱 + BounceParser 15+正则模式解析 + 5级匹配算法 + 自动黑名单
5. **PDF导出** - 封面页（OpenPDF生成）+ 附件内容（PDF直接合并/DOCX经JODConverter转换后合并）
6. **模板方法** - 代码生成器: Velocity模板自动生成前后端代码
7. **观察者模式** - WebSocket: WebSocketPublisher 发布消息，前端实时接收
8. **状态机** - 邮件发送: 9种状态流转 + 父子重试链
9. **MapStruct** - 对象转换: Entity ↔ Form ↔ VO ↔ Query
10. **MyBatis-Plus** - 数据权限: DataPermission 注解 + MyDataPermissionHandler