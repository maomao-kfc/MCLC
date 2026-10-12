# MCLC大会管理系统 - 项目总览

## 1. 项目简介

**项目名称**：MCLC大会管理系统（MCLC Conference Management System）

**基础框架**：基于 youlai-boot 4.1.0 二次开发

**业务定位**：学术会议全生命周期管理平台，服务于MCLC（国际眼科学交流大会）的会议组织工作。

**核心功能**：
- 会议信息展示与日程管理
- 嘉宾/演讲者管理
- 论文投稿与专家审稿
- 参会注册（9步流程）与多渠道支付
- 邮件系统（SMTP发送 + IMAP退信检测 + 黑名单管理）
- 签到管理（二维码生成与验证）
- 系统管理（用户/角色/菜单/字典/部门/通知/配置）

## 2. 技术栈

### 后端
| 技术 | 版本 | 用途 |
|------|------|------|
| Java | 17 | 开发语言 |
| Spring Boot | 4.0.1 | 应用框架 |
| Spring Security | 6.x | 安全认证与授权 |
| MyBatis-Plus | 3.5.15 | ORM框架 |
| Redis + Redisson | - | 缓存/分布式锁/Token存储/验证码 |
| MySQL | 8.x | 关系数据库 |
| Druid | 1.2.28 | 数据库连接池 |

### 前端（管理后台 frontend/）
| 技术 | 版本 | 用途 |
|------|------|------|
| Vue | 3.x | 前端框架 |
| TypeScript | - | 类型安全 |
| Vite | - | 构建工具 |
| Element Plus | - | UI组件库 |
| Pinia | - | 状态管理 |

### 前台（官网 webpage/）
| 技术 | 版本 | 用途 |
|------|------|------|
| Vue | 3.x | 前端框架 |
| JavaScript | - | 开发语言 |
| Vite | - | 构建工具 |
| Element Plus | - | UI组件库 |
| Pinia | - | 状态管理 |
| vue-i18n | - | 国际化（中英双语） |

### 中间件与第三方服务
| 服务 | 用途 |
|------|------|
| SMTP | 邮件发送 |
| IMAP | 退信检测（定时扫描收件箱） |
| Stripe | 信用卡支付 |
| 支付宝 | 支付宝支付 |
| 微信支付 | 微信支付 |
| 微信小程序 | 小程序登录认证 |
| 阿里云OSS / MinIO | 文件存储 |
| 阿里云短信 | 验证码短信 |
| LibreOffice + JODConverter | DOCX转PDF |
| OpenPDF | PDF生成（封面页/信息页） |
| WebSocket (STOMP) | 实时推送（在线人数/字典变更） |
| Knife4j + OpenAPI3 | 接口文档 |
| xxl-job | 分布式任务调度（可选，dev环境disabled） |

## 3. 项目结构概览

```
mclc-admin-main/
├── backend/          # 后端 Spring Boot 项目
├── frontend/         # 管理后台（Vue3 + TypeScript）
├── webpage/          # 前台官网（Vue3 + JavaScript）
├── docs/             # 技术文档（本文档所在目录）
└── backend/sql/      # 数据库SQL脚本
```

## 4. 部署架构

### Docker部署
- 后端：Docker容器（eclipse-temurin:17-jre + LibreOffice + 中文字体）
- 端口映射：8989→8000
- 环境变量：SPRING_PROFILES_ACTIVE=prod, TZ=Asia/Shanghai

### 部署命令
```bash
# 构建并启动
docker-compose build
docker-compose up -d

# 查看日志
docker-compose logs -f
```

### 前端部署
- 管理后台（frontend/）：构建后由Nginx托管
- 前台官网（webpage/）：构建后由Nginx托管

## 5. 环境配置

| 环境 | 配置文件 | 说明 |
|------|---------|------|
| 开发 | application-dev.yml | 本地开发，包含所有第三方服务配置 |
| 生产 | application-prod.yml | 生产环境，敏感配置通过环境变量注入 |

**注意**：配置文件中包含数据库密码、SMTP密码、Stripe密钥、支付宝私钥等敏感信息，严禁在文档或代码仓库中明文输出。

## 6. 数据库

- 数据库名：meeting_admin
- 字符集：utf8mb4
- 系统表：15张（sys_前缀）
- 业务表：30+张（meeting_/register_/paper_/mail_/portal_前缀）

## 7. API规范

- 统一前缀：`/api/v1/`
- 认证方式：JWT AccessToken + Redis RefreshToken 双Token机制
- 统一响应格式：`Result<T>`（code/msg/data）
- 分页响应格式：`PageResult<T>`（records/total/current/size）
- 接口文档：Knife4j UI（`/doc.html`）

## 8. 关键设计决策

1. **双Token认证**：AccessToken（短有效期）+ RefreshToken（长有效期，存Redis），兼顾安全与用户体验
2. **邮件任务表模式**：邮件发送采用任务表（mail_send_record），支持重试、取消、退信检测，9种发送状态完整追踪
3. **支付策略模式**：PaymentRouter根据支付方式路由到4种PaymentProvider，新增支付渠道只需实现PaymentProvider接口
4. **退信检测**：IMAP定时扫描收件箱 + BounceParser 15+正则模式解析 + 5级匹配算法 + 自动黑名单
5. **PDF导出**：封面页（OpenPDF生成）+ 附件内容（PDF直接合并/DOCX经JODConverter转换后合并/无附件则OpenPDF生成完整信息PDF）
6. **中英双语**：Entity层name/nameEn双字段设计，前台vue-i18n国际化