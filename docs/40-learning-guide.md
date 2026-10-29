# MCLC 项目学习指南

> **目标**: 通过本项目的实际代码学习 Spring Boot + Vue 3 全栈开发  
> **方法**: 按阶段递进，每阶段对应项目中的真实模块，先读代码再动手改

---

## 阶段一：项目跑起来（1-2天）

**目标**: 本地启动三端，理解项目结构

1. **环境准备**: JDK 17 / Maven / Node.js / pnpm / MySQL / Redis
2. **数据库初始化**: 导入 `backend/sql/SQLbackups/meeting_admin(本地).sql`
3. **后端启动**: `backend/` → `mvn spring-boot:run`（端口 8000）
4. **管理端启动**: `frontend/` → `pnpm install && pnpm dev`（端口 3000）
5. **门户端启动**: `webpage/` → `pnpm install && pnpm dev`（端口 5174）

**关键认知**:
- Redis 用 db 12（不是默认 db 0），排查缓存必须 `redis-cli -n 12`
- 三端独立部署：后端 API + 管理端（管理员用）+ 门户端（用户用）
- 读 `docs/43-phase1-scan-report.md` 建立全局认知

---

## 阶段二：CRUD 与 RBAC（3-5天）

**目标**: 掌握 MyBatis-Plus CRUD + Spring Security 权限

**学习路径**:
1. **简单 CRUD**: 读 `GuestServiceImpl.java`（5KB，最简单的 Service）→ 理解 Service→Mapper→Entity 三层
2. **MapStruct 转换**: 读 `GuestConverter.java` → 理解 Entity↔VO/Form 编译期转换
3. **RBAC 链路**: 
   - 登录: `AuthServiceImpl.java` → JWT 生成
   - 鉴权: `SecurityConfig.java` → 过滤器链
   - 权限: `MenuServiceImpl.java` → type=B 菜单 perm 加载
   - 数据权限: `MyDataPermissionHandler.java` → SQL 动态拼接
4. **动手**: 用 codegen 模块生成一个新表的 CRUD 代码

**关键约束（踩过的坑）**:
- 权限 perm 只认 type=B（按钮）菜单，type=M 挂 perm 无效 → 403

---

## 阶段三：业务流程与状态机（5-7天）

**目标**: 理解复杂业务的状态流转和事务设计

**学习路径**:
1. **注册流程**: `webpage/src/pages/register/` 9 步组件 → `RegisterUserServiceImpl.java`（28KB）
2. **凭证审核**: `docs/10-payment-proof-flow.md` → `PaymentProofServiceImpl.java`（25.7KB）
   - 重点：OCR 降级、水印去重、状态流转、两封邮件
3. **对账匹配**: `docs/11-reconciliation-flow.md` → `PaymentReconciliationServiceImpl.java`（18KB）
   - 重点：多维度评分模型、自动/人工匹配
4. **事务边界**: 注意邮件发送的 try-catch 不阻断业务

**关键认知**:
- proof_status 流转: null → proof_submitted → paid/rejected/offline_paid
- 三种确认方式殊途同归：审核通过/对账确认/线下登记 → 均发注册成功邮件

---

## 阶段四：邮件系统与定时任务（7-10天，最有深度）

**目标**: 掌握邮件全生命周期、退信检测、定时任务

**学习路径**:
1. **邮件发送**: `docs/12-email-lifecycle.md` → `MailSendRecordServiceImpl.java`（39KB）
   - 重点：任务表模式、9 态状态机、父子重试模型
2. **退信检测**: `BounceEmailJob.java`（46KB）+ `BounceParser.java`（26KB）
   - 重点：IMAP 轮询、断点续传、DSN 解析、永久/临时退信分类
3. **黑名单**: `MailBounceBlacklistServiceImpl.java` → 发送前拦截
4. **XXL-Job**: `XxlJobConfig.java` → 定时任务配置

**面试高频题**（对照 `docs/30-interview-email-system.md`）:
- 为什么 9 个状态而不是 2 个？
- 退信怎么检测？怎么区分永久/临时？
- 重试为什么不覆盖原记录？

---

## 阶段五：前端工程化（3-5天）

**目标**: 掌握 Vue 3 + TypeScript + 组件化开发

**学习路径**:
1. **管理端 CURD 模板**: `frontend/src/components/CURD/` → PageContent/PageModal/PageSearch/usePage 四件套
2. **SSE 实时推送**: `frontend/src/composables/sse/` → useSse/useDictSync/useOnlineCount
3. **权限指令**: `frontend/src/directives/permission/` → v-permission 按钮级控制
4. **门户端 i18n**: `webpage/src/locales/` → vue-i18n 中英双语
5. **数据驱动导航**: `webpage/src/data/nav.js` → navItems 数组 → NavBar.vue v-for 渲染

---

## 推荐阅读顺序（按文件大小和复杂度递增）

| 顺序 | 文件 | 大小 | 学什么 |
|------|------|------|--------|
| 1 | GuestServiceImpl.java | 5KB | 最简单的 CRUD |
| 2 | TesseractOcrServiceImpl.java | 4KB | 优雅降级、catch(Throwable) |
| 3 | MailSendStatus.java | 1.9KB | 9 态状态机定义 |
| 4 | BounceType.java | 3.7KB | 退信分类、RFC 3463 |
| 5 | PaymentReconciliationServiceImpl.java | 18KB | 多维度匹配引擎 |
| 6 | PaymentProofServiceImpl.java | 25KB | 凭证全链路、OCR+水印+去重 |
| 7 | RegisterUserServiceImpl.java | 28KB | 注册全流程、状态管理 |
| 8 | MailSendRecordServiceImpl.java | 39KB | 邮件全生命周期 |
| 9 | BounceEmailJob.java | 46KB | IMAP 轮询、断点续传 |
| 10 | PaperSubmissionServiceImpl.java | 40KB | 投稿全链路、PDF 生成 |

---

## 故障案例学习（对照 docs/20-24-case-*.md）

| 案例 | 核心教训 |
|------|----------|
| 01 邮件中心缺列 | 实体字段变更必须同步 DDL |
| 02 OCR JNA Error | JNA/JNI 必须 catch(Throwable) |
| 03 审核未确认成功 | 状态机要匹配实际业务流程 |
| 04 权限 type=B | 框架设计约束必须了解 |
| 05 SMTP≠投递成功 | 邮件投递链路多环节 |