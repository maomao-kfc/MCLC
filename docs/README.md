# MCLC 文档使用手册

> 本手册告诉你：docs 目录下有哪些文档、每个文档能帮你解决什么问题、遇到报错该查哪个文档。

---

## 一、文档地图（5 大分区）

| 分区 | 编号范围 | 用途 | 什么时候看 |
|------|----------|------|-----------|
| 总览架构 | 00-05 | 了解项目全貌、数据库、目录结构、代码地图、API 清单 | 刚接手项目 / 需要全局视角 |
| 功能链路 | 10-19 | 深入某个业务模块的实现细节和状态流转 | 要改某个模块的代码 / 想理解业务流程 |
| 故障案例 | 20-24 | 真实线上故障的完整复盘（现象→排查→修复→教训） | 遇到类似报错 / 想学习排查思路 |
| 面试简历 | 30-33 | 面试题库（含标准答案+追问）和简历素材 | 准备面试 / 写简历 |
| 学习参考 | 40-44 | 学习路线、已知问题清单、术语表、扫描报告、英语词汇表 | 系统性学习 / 查术语 / 查遗留问题 / 查单词读音 |

---

## 二、每个文档能给你什么

### 总览架构（00-05）

| 文档 | 你能从中得到 |
|------|-------------|
| [00-project-overview.md](00-project-overview.md) | 项目是什么、技术栈、三端结构、部署架构、37张表概览、10个关键设计决策 |
| [01-architecture.md](01-architecture.md) | 后端分层架构、模块依赖关系、核心调用链 |
| [02-database-schema.md](02-database-schema.md) | 全部37张表的字段定义、索引、表间关系（改表前必查） |
| [03-directory-structure.md](03-directory-structure.md) | 三端目录说明，找文件先看这里 |
| [04-codebase-map.md](04-codebase-map.md) | 核心类清单（Controller/Service/Mapper），按类名找文件 |
| [05-api-endpoint-catalog.md](05-api-endpoint-catalog.md) | 全部API接口清单（URL/方法/参数/返回值），前后端联调必查 |

### 功能链路（10-19）

| 文档 | 你能从中得到 |
|------|-------------|
| [10-payment-proof-flow.md](10-payment-proof-flow.md) | 凭证上传→OCR初筛→水印去重→审核→状态流转全链路 |
| [11-reconciliation-flow.md](11-reconciliation-flow.md) | 对账Excel导入→多维度匹配引擎（评分规则）→人工确认全链路 |
| [12-email-lifecycle.md](12-email-lifecycle.md) | 邮件9态状态机、退信检测、父子重试、黑名单、BounceParser解析 |
| [13-payment-subsystem.md](13-payment-subsystem.md) | 支付策略模式（Stripe/微信/支付宝/银行转账）、回调处理、幂等性、退款 |
| [14-email-subsystem.md](14-email-subsystem.md) | 邮件子系统最完整文档：9态详解、IMAP扫描、RFC3464 DSN解析、配置参考 |
| [15-registration-workflow.md](15-registration-workflow.md) | 注册9步向导、状态机、团队注册、三种注册成功触发方式 |
| [16-paper-submission.md](16-paper-submission.md) | 论文投稿双通道、专家审稿邮件、PDF导出、抵扣码 |
| [17-conference-agenda.md](17-conference-agenda.md) | 会议日程三级层次（天数→分会场→议程项）、双轨国际化 |
| [18-rbac-permission.md](18-rbac-permission.md) | RBAC权限链路、type=B约束、数据权限、前端权限指令 |
| [19-i18n-strategy.md](19-i18n-strategy.md) | 列级/行级双轨国际化实现、前端vue-i18n适配 |

### 故障案例（20-24）—— 每个都是真实发生的

| 文档 | 故障 | 核心教训 |
|------|------|----------|
| [20-case-email-record-type-missing.md](20-case-email-record-type-missing.md) | 邮件中心页面报错：表缺 record_type 等三列 | 实体字段变更必须同步DDL |
| [21-case-ocr-jna-error.md](21-case-ocr-jna-error.md) | OCR报 JNA Error 崩溃 | JNA/JNI必须catch(Throwable)不是Exception |
| [22-case-review-passed-not-paid.md](22-case-review-passed-not-paid.md) | 凭证审核通过但支付状态没变 | 状态机要匹配实际业务流程 |
| [23-case-rbac-menu-type-constraint.md](23-case-rbac-menu-type-constraint.md) | 配了权限还是403 | youlai-boot只认type=B按钮菜单的perm |
| [24-case-smtp-success-not-delivered.md](24-case-smtp-success-not-delivered.md) | SMTP发送成功但用户没收到 | SMTP成功≠投递成功，需退信检测 |

### 面试简历（30-33）

| 文档 | 你能从中得到 |
|------|-------------|
| [30-interview-email-system.md](30-interview-email-system.md) | 邮件系统6道面试题（9态设计/退信检测/父子重试/黑名单/不回滚/任务表） |
| [31-interview-payment-system.md](31-interview-payment-system.md) | 支付对账6道面试题（凭证vs回调/评分模型/OCR降级/两封邮件/水印去重） |
| [32-interview-backend-architecture.md](32-interview-backend-architecture.md) | 后端架构6道面试题（技术选型/RBAC/Redis db12/全局异常/数据库/XXL-Job） |
| [33-resume-materials.md](33-resume-materials.md) | 简历定位模板、核心亮点、4个STAR故障故事、量化数据、自我介绍模板 |

### 学习参考（40-44）

| 文档 | 你能从中得到 |
|------|-------------|
| [40-learning-guide.md](40-learning-guide.md) | 五阶段学习路线（跑起来→CRUD/RBAC→状态机→邮件系统→前端工程化） |
| [41-known-issues.md](41-known-issues.md) | 项目所有已知问题清单（含优先级和修复状态），改代码前先看避免踩坑 |
| [42-glossary.md](42-glossary.md) | 42个术语解释（方案B/PSM 11/dHash/DSN/RFC 3463/type=B等） |
| [43-phase1-scan-report.md](43-phase1-scan-report.md) | 全项目扫描报告：代码规模、核心调用链、文档差距分析 |
| [44-vocabulary.md](44-vocabulary.md) | 约300个英语单词的中文近音标注+中译+项目示例，看代码遇到不认识的词在这里查读音 |

---

## 三、遇到报错怎么查

### 按报错类型查文档

| 报错/现象 | 第一步查 | 第二步查 | 补充 |
|-----------|---------|---------|------|
| **403 无权限/访问未授权** | [23-case-rbac-menu-type-constraint.md](23-case-rbac-menu-type-constraint.md) | [18-rbac-permission.md](18-rbac-permission.md) | 检查菜单type是否=B，perm是否正确 |
| **邮件发送失败/报错** | [12-email-lifecycle.md](12-email-lifecycle.md) 第3节发送流程 | [14-email-subsystem.md](14-email-subsystem.md) 第13节问题排查 | 查mail_send_record表status字段 |
| **邮件发了但用户没收到** | [24-case-smtp-success-not-delivered.md](24-case-smtp-success-not-delivered.md) | [14-email-subsystem.md](14-email-subsystem.md) 第5节退信检测 | 查mail_bounce_record表 |
| **邮件中心页面报错（缺列）** | [20-case-email-record-type-missing.md](20-case-email-record-type-missing.md) | [02-database-schema.md](02-database-schema.md) | 对照实体类检查DDL是否同步 |
| **OCR报错/JNA Error** | [21-case-ocr-jna-error.md](21-case-ocr-jna-error.md) | [10-payment-proof-flow.md](10-payment-proof-flow.md) OCR降级章节 | 确认tessdata路径、catch(Throwable) |
| **凭证提交400** | [10-payment-proof-flow.md](10-payment-proof-flow.md) | [05-api-endpoint-catalog.md](05-api-endpoint-catalog.md) | 检查请求参数/文件大小/格式 |
| **审核通过但状态没变** | [22-case-review-passed-not-paid.md](22-case-review-passed-not-paid.md) | [13-payment-subsystem.md](13-payment-subsystem.md) 第14节 | 检查proof_status和payment_status |
| **对账匹配不上** | [11-reconciliation-flow.md](11-reconciliation-flow.md) 评分规则 | - | 姓名30/手机25/金额25/时间20，阈值60 |
| **数据库报错（字段/表不存在）** | [02-database-schema.md](02-database-schema.md) | [20-case-email-record-type-missing.md](20-case-email-record-type-missing.md) | 代码升级后必须同步DDL |
| **Redis缓存不生效** | [41-known-issues.md](41-known-issues.md) | - | 本地环境Redis用db 12，排查加 `-n 12` |
| **前端文案/翻译缺失** | [19-i18n-strategy.md](19-i18n-strategy.md) | - | 检查locales目录中英文key |
| **找不到某个类/接口** | [04-codebase-map.md](04-codebase-map.md) | [03-directory-structure.md](03-directory-structure.md) | 按类名搜索 |
| **接口参数/返回值不确定** | [05-api-endpoint-catalog.md](05-api-endpoint-catalog.md) | - | 或打开 Knife4j `/doc.html` |

### 通用排查步骤

```
1. 看报错信息 → 判断属于哪个模块（邮件/支付/权限/数据库/前端）
2. 查上面的表格 → 找到对应的功能文档和故障案例
3. 查 41-known-issues.md → 看是不是已知问题
4. 查 42-glossary.md → 看不懂的术语先查清楚
5. 查 44-vocabulary.md → 不认识的单词查读音和意思
6. 还没解决 → 按功能文档中的"常见问题排查"章节逐步检查
```

---

## 四、按场景推荐阅读顺序

### 场景1：刚接手项目（第1周）

```
00-project-overview.md（半天，建立全局认知）
→ 03-directory-structure.md（1小时，知道代码在哪）
→ 40-learning-guide.md 阶段一（1-2天，把项目跑起来）
→ 04-codebase-map.md（2小时，熟悉核心类）
```

### 场景2：要改某个模块的代码

```
先查 04-codebase-map.md 找到相关类
→ 读对应的功能文档（10-19中的某一篇）
→ 读 41-known-issues.md 确认没有已知坑
→ 读 02-database-schema.md 确认表结构
```

### 场景3：准备面试

```
33-resume-materials.md（先确定怎么讲项目）
→ 30/31/32-interview-*.md（按模块刷题）
→ 20-24-case-*.md（故障案例是最好的项目亮点）
→ 12-email-lifecycle.md（邮件系统是本项目最有深度的模块，面试必问）
```

### 场景4：线上出故障了

```
看报错 → 查"遇到报错怎么查"表格
→ 读对应故障案例（20-24）看之前怎么解决的
→ 读对应功能文档的排查章节
→ 查数据库（表结构见02-database-schema.md）
```

---

## 五、文档编号规则

| 编号段 | 含义 |
|--------|------|
| 00-09 | 总览与架构 |
| 10-19 | 功能链路 |
| 20-29 | 故障案例 |
| 30-39 | 面试与简历 |
| 40-49 | 学习与参考 |

新增文档时按内容归入对应编号段，取下一个可用编号。