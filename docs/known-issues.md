# MCLC 已知问题清单

> 基于Phase 1全量代码扫描发现的问题，按严重程度分类
> 更新时间：2026-08-14
> 原则：仅记录，不修改业务逻辑

## 问题统计

| 类别 | 数量 | 严重程度 |
|------|------|----------|
| SEC 安全问题 | 11 | 🔴 高 |
| CODE 代码质量 | 10 | 🟡 中 |
| DB 数据库问题 | 3 | 🟡 中 |
| ARCH 架构问题 | 7 | 🟠 中高 |
| PAY 支付问题 | 4 | 🟡 中 |
| PAPER 论文投稿问题 | 3 | 🟡 中 |
| **合计** | **38** | - |

---

## SEC 安全问题

### SEC-001: SMTP密码明文存储在配置文件

- **位置**: `application-dev.yml` / `application-prod.yml`
- **描述**: Spring Mail的SMTP密码以明文形式存储在YAML配置文件中
- **风险**: 配置文件泄露导致邮件服务被滥用
- **建议**: 使用Jasypt加密或环境变量注入
- **优先级**: P0

### SEC-002: JWT密钥硬编码在配置文件

- **位置**: `application.yml` → `jwt.secret`
- **描述**: JWT签名密钥以Base64明文存储在配置文件中
- **风险**: 密钥泄露可伪造任意用户的JWT令牌
- **建议**: 使用Vault/K8s Secret管理，或至少使用Jasypt加密
- **优先级**: P0

### SEC-003: IMAP密码明文存储

- **位置**: `application.yml` → `mail.imap.password`
- **描述**: IMAP邮箱密码明文存储在配置文件中
- **风险**: 退信检测邮箱被入侵
- **建议**: 同SEC-001
- **优先级**: P0

### SEC-004: Stripe密钥明文存储

- **位置**: `application.yml` → `stripe.secret-key` / `stripe.publishable-key`
- **描述**: Stripe支付密钥明文存储
- **风险**: 支付服务被滥用，可能造成资金损失
- **建议**: 使用环境变量或Secret Manager
- **优先级**: P0

### SEC-005: 阿里云SMS AccessKey明文存储

- **位置**: `application.yml` → `aliyun.sms.access-key-id` / `access-key-secret`
- **描述**: 阿里云短信服务AccessKey明文存储
- **风险**: 短信服务被滥用，产生费用
- **建议**: 使用环境变量或Secret Manager
- **优先级**: P0

### SEC-006: ignore-urls包含大量业务API无认证暴露

- **位置**: `application-dev.yml` → `security.ignore-urls`
- **描述**: 23个ignore-urls路径中，`/api/v1/files/**`、`/api/v1/statistics/**`、`/api/v1/paperSubmission/**`、`/api/v1/email-templates/**`、`/api/v1/discountConfiguration/**`、`/api/v1/architecture/**`等业务API完全绕过安全过滤器，未登录用户可直接访问
- **风险**: 文件上传被滥用上传恶意文件；统计数据泄露业务数据；投稿/折扣配置被未授权修改
- **建议**: 将非认证必需的业务API从ignore-urls移至unsecured-urls（经过安全过滤器），或添加独立的认证机制
- **优先级**: P1

### SEC-007: @DataPermission覆盖范围不足

- **位置**: `UserMapper.java`(2处) + `DeptMapper.java`(1处)
- **描述**: 仅UserMapper和DeptMapper使用了@DataPermission注解，其他业务表（注册用户、投稿、支付、会议日程等）均无数据权限控制
- **风险**: 任何登录用户可查看全部业务数据，无法按部门/角色隔离
- **建议**: 为RegisterUserMapper、PaperSubmissionMapper等核心业务Mapper添加@DataPermission注解
- **优先级**: P2

### SEC-008: /api/v1/files/**无认证文件上传

- **位置**: `application-dev.yml` → `security.ignore-urls` → `/api/v1/files/**`
- **描述**: 文件上传接口在ignore-urls中，不经过任何安全过滤器，可被未登录用户直接调用
- **风险**: 恶意文件上传（webshell、大文件DDoS等）
- **建议**: 将`/api/v1/files/**`从ignore-urls移除，或至少移至unsecured-urls添加限流保护
- **优先级**: P1

### SEC-009: /actuator/**在unsecured-urls中允许匿名访问

- **位置**: `application-dev.yml` → `security.unsecured-urls` → `/actuator/**`
- **描述**: Actuator监控端点允许匿名访问，可能泄露应用运行信息（健康状态、环境变量、Bean信息等）
- **风险**: 信息泄露，攻击者获取应用内部信息
- **建议**: 生产环境限制Actuator端点暴露范围，或添加认证保护
- **优先级**: P2

### SEC-010: ignore-urls重复配置

- **位置**: `application-dev.yml` 行155+行161
- **描述**: `/api/v1/registrationType/**`在ignore-urls中出现两次
- **影响**: 无功能影响，但配置冗余
- **建议**: 删除重复配置
- **优先级**: P3

### SEC-011: PaymentController权限标识复用

- **位置**: `PaymentController.java` 行50+59
- **描述**: PaymentController使用`sys:registerUser:update`权限标识，而非独立的`sys:payment:*`权限标识
- **影响**: 支付操作权限与注册用户更新权限耦合，无法独立控制支付权限
- **建议**: 定义独立的`sys:payment:*`权限标识
- **优先级**: P3

---

### CODE-001: BounceEmailJob文件过大（46.2KB）

- **位置**: `backend/src/main/java/com/youlai/mclc/mail/job/BounceEmailJob.java`
- **描述**: 单文件46.2KB，包含IMAP连接、邮件解析、退信匹配、黑名单更新等全部逻辑
- **影响**: 可维护性差，难以测试
- **建议**: 拆分为BounceImapConnector、BounceParser、BounceMatcher、BounceBlacklistUpdater 4个类
- **优先级**: P1

### CODE-002: @Resource和@Autowired混用

- **位置**: 全项目Service层
- **描述**: 部分类使用`@Resource`注入，部分使用`@Autowired`，不一致
- **影响**: 代码风格不统一，新人困惑
- **建议**: 统一使用构造器注入（Spring推荐方式）
- **优先级**: P2

### CODE-003: 魔法值硬编码

- **位置**: 多处Service实现
- **描述**: 状态值如`"1"`, `"2"`, `"3"`直接硬编码在业务逻辑中，未使用枚举常量
- **示例**: `paper_status`使用字符串`"1"/"2"/"3"`而非枚举
- **影响**: 可读性差，容易出错
- **建议**: 定义PaperStatus、ReviewStatus等枚举类
- **优先级**: P2

### CODE-004: paper_submission表缺少审计字段

- **位置**: `paper_submission`表DDL
- **描述**: 缺少`create_time`、`update_time`、`create_by`、`update_by`、`is_deleted`字段
- **影响**: 无法追踪创建/修改时间和操作人
- **建议**: 添加标准审计字段
- **优先级**: P1

### CODE-005: Controller层包含PDF导出逻辑

- **位置**: `PaperSubmissionController.exportPdf()`
- **描述**: PDF生成逻辑（JODConverter调用）直接写在Controller中
- **影响**: 违反分层架构，难以复用和测试
- **建议**: 抽取到PaperPdfExportService
- **优先级**: P2

### CODE-006: 旧版portal_meeting_date/period表仍在使用

- **位置**: `portal_meeting_date`, `portal_meeting_period`
- **描述**: 已被meeting_info/session/agenda_item替代，但代码和表中仍保留
- **影响**: 数据冗余，维护负担
- **建议**: 确认无引用后废弃
- **优先级**: P3

### CODE-007: sys_log使用MyISAM引擎

- **位置**: `sys_log`表DDL
- **描述**: 唯一使用MyISAM引擎的表，其余均为InnoDB
- **影响**: 不支持事务，崩溃恢复能力差
- **建议**: 迁移到InnoDB引擎
- **优先级**: P2

### CODE-008: team_id字段exist=false，团队关联功能未启用

- **位置**: `RegisterUserInformation.java:team_id`
- **描述**: team_id字段标记`@TableField(value = "team_id", exist = false)`，数据库表中无此列。团队成员创建时设置teamId但不会写入数据库
- **影响**: 无法通过team_id查询团队成员关联关系，团队注册功能不完整
- **建议**: 数据库添加team_id列，移除exist=false注解
- **优先级**: P2

### CODE-009: questionnaire_survey代码全部注释，功能未启用

- **位置**: `RegisterUserServiceImpl.java` getRegisterUserList()方法中问卷字段映射代码
- **描述**: connectId关联questionnaire_survey的代码全部注释掉，前端Survey.vue组件存在但未纳入路由stepOrder
- **影响**: 10个问卷字段(registrationCategory/gender/occupation等)在Form中存在但永远为空
- **建议**: 确认是否需要问卷调查功能，如需要则启用路由和代码；如不需要则清理Form中问卷字段
- **优先级**: P2

### CODE-010: deleteTeamMembers()空方法，团队成员累积

- **位置**: `RegisterUserServiceImpl.java:222`
- **描述**: deleteTeamMembers()方法体为空，更新注册时不会删除旧团队成员，重新创建会导致记录累积
- **影响**: 同一注册用户多次更新后团队成员记录重复累积
- **建议**: 实现deleteTeamMembers()方法，按主注册人ID删除旧团队成员记录
- **优先级**: P3

---

## DB 数据库问题

### DB-001: SQL基线脚本不统一

- **位置**: `backend/sql/` 目录
- **描述**: 存在`youlai_admin.sql`和`meeting_admin(结构).sql`两个基线脚本，表结构有差异
- **影响**: 新环境初始化可能使用错误脚本
- **建议**: 统一为一个基线脚本`meeting_admin.sql`
- **优先级**: P1

### DB-002: 增量SQL脚本无版本控制机制

- **位置**: `backend/sql/` 目录下27个增量脚本
- **描述**: 增量脚本按日期命名但无强制执行顺序，缺少Flyway/Liquibase管理
- **影响**: 脚本执行顺序错误或遗漏导致schema不一致
- **建议**: 引入Flyway做版本化迁移管理
- **优先级**: P1

### DB-003: gen_table/gen_table_column为代码生成器残留

- **位置**: `gen_table`, `gen_table_column`
- **描述**: MyBatis-Plus代码生成器的元数据表，生产环境不需要
- **影响**: 暴露表结构信息
- **建议**: 生产环境删除这两张表
- **优先级**: P3

---

## ARCH 架构问题

### ARCH-001: 邮件9态状态机缺少状态转换文档

- **位置**: `mail_send_record.send_status`
- **描述**: 9个发送状态+4个投递状态的完整转换规则仅存在于代码中
- **影响**: 新人理解成本高，容易引入状态转换bug
- **建议**: 在05-email-subsystem.md中补充完整状态转换表
- **优先级**: P1
- **状态**: ✅ 已在05-email-subsystem.md中补充

### ARCH-002: i18n双轨策略缺少实现规范

- **位置**: portal_*行级 vs meeting_*列级
- **描述**: 两种i18n策略的实现细节（查询方式、降级策略、前端适配）未形成规范文档
- **影响**: 新功能开发时i18n实现不一致
- **建议**: 在04-i18n-strategy.md中补充实现规范
- **优先级**: P2

### ARCH-003: 支付回调缺少幂等性保障文档

- **位置**: 支付回调Controller
- **描述**: Stripe/微信/支付宝的回调处理缺少幂等性设计文档
- **影响**: 网络重试可能导致重复处理
- **建议**: 在06-payment-subsystem.md中补充幂等性设计
- **优先级**: P1
- **状态**: ✅ 已在06-payment-subsystem.md第11节补充

### ARCH-004: 支付回调Controller缺少事务保护

- **位置**: `AlipayCallbackController.handleNotify()`, `WechatCallbackController.handleNotify()`
- **描述**: Controller回调路径直接调用Provider.handleCallback()+syncRegisterUserByOrderNo()，无@Transactional。Provider内部更新payment_orders与后续syncRegisterUserByOrderNo不在同一事务中
- **影响**: payment_orders已更新为paid但register_user_information未更新，导致双表状态不一致
- **建议**: 将Controller回调逻辑迁移到Service层事务方法，或为Controller方法添加@Transactional
- **优先级**: P1

### ARCH-005: PaymentService冗余回调方法

- **位置**: `PaymentServiceImpl.handleAlipayNotify()`, `PaymentServiceImpl.handleWechatNotify()`
- **描述**: 这两个@Service事务方法未被任何Controller调用，与AlipayCallbackController/WechatCallbackController功能重复
- **影响**: 代码冗余，新人困惑于使用哪条路径
- **建议**: 统一为一条路径，删除冗余方法或将Controller改为调用Service方法
- **优先级**: P2

### ARCH-006: Survey/Expert/Skills组件未纳入路由

- **位置**: `webpage/src/pages/register/Survey.vue`, `Expert.vue`, `Skills.vue`
- **描述**: 3个注册步骤组件(Survey 490行/Expert 201行/Skills 265行)存在但未在Register.vue的stepOrder中注册，用户无法访问
- **影响**: 问卷调查、专家课程、技能培训功能不可用；registerFormMapper.js中有survey字段映射但数据源始终为空
- **建议**: 确认业务需求后纳入路由或删除组件
- **优先级**: P3

### ARCH-007: 团队折扣仅前端计算，后端无验证

- **位置**: `webpage/src/pages/register/Confirm.vue` 折扣计算逻辑
- **描述**: 团队注册折扣通过discountConfiguration API获取规则后在前端计算最终价格，后端auditRegisterUser无对应折扣验证逻辑
- **影响**: 前端价格可被篡改（修改discountRate或totalPrice），后端无法校验实际应付金额
- **建议**: 后端增加折扣验证逻辑，在审核或支付确认时校验实际金额
- **优先级**: P1

---

## PAY 支付问题

### PAY-001: StripePaymentProvider注入了未使用的BankTransferProperties

- **位置**: `StripePaymentProvider.java:22`
- **描述**: StripePaymentProvider构造器注入了BankTransferProperties，但从未使用
- **影响**: 无功能影响，但增加不必要的依赖
- **建议**: 移除BankTransferProperties注入
- **优先级**: P3

### PAY-002: 退款仅检查Stripe，不检查其他支付方式

- **位置**: `PaymentServiceImpl.refund():160`
- **描述**: 退款方法仅检查`stripePaymentIntentId`来决定是否调用Stripe退款，不检查微信/支付宝的payment_orders
- **影响**: 微信/支付宝退款需手动调用Provider.refund()，Service层退款流程不完整
- **建议**: 根据paymentMethod路由到对应Provider执行退款
- **优先级**: P1

### PAY-003: 订单号使用timestamp生成，高并发下可能冲突

- **位置**: `WechatPaymentProvider.java:55`, `AlipayPaymentProvider.java:45`
- **描述**: 订单号格式为"WXP/ALI"+System.currentTimeMillis()+registrationId后4位，同一毫秒内同一用户创建多个订单可能冲突
- **影响**: 订单号重复导致数据库插入失败或数据混乱
- **建议**: 使用UUID或雪花算法生成唯一订单号
- **优先级**: P2

### PAY-004: 支付宝验签公钥格式可能不兼容

- **位置**: `AlipayPaymentProvider.java:196-206`
- **描述**: 使用X509EncodedKeySpec解析alipayPublicKey，但支付宝公钥通常为PKCS#1格式或裸Base64编码
- **影响**: 验签可能失败，导致支付宝回调无法正常处理
- **建议**: 使用AlipaySignature.verifyV1()替代手动验签，或确保公钥格式正确
- **优先级**: P2

### SEC-012: 专家审核接口无认证保护

- **位置**: `PaperSubmissionController.java:164` → `POST/PUT /api/v1/paperSubmission/expertReview`
- **描述**: 专家审核接口为公开接口，无任何认证机制，任何人都可调用
- **风险**: 恶意调用可篡改审核结果，影响论文通过率判定
- **建议**: 添加token验证或审核链接中的一次性token校验
- **优先级**: P2

### SEC-013: 投稿者修改接口仅靠邮箱验证身份

- **位置**: `PaperSubmissionServiceImpl.java:761` → `updateBySubmitter()`
- **描述**: PUT /api/v1/paperSubmission/query/update接口仅通过contactEmail匹配身份，无验证码校验
- **风险**: 知道投稿者邮箱即可修改其论文信息
- **建议**: 修改操作也应要求验证码验证
- **优先级**: P3

---

## PAPER 论文投稿问题

### PAPER-001: 抵扣码生成未做唯一性校验

- **位置**: `PaperSubmissionServiceImpl.java:353-358`
- **描述**: 抵扣码使用A-Z+0-9共36字符随机生成8位码，理论上36^8≈2.8万亿组合碰撞概率极低，但未做唯一性校验
- **影响**: 极端情况下可能生成重复抵扣码，导致注册时折扣冲突
- **建议**: 在paper_submission表的discount_code字段添加唯一索引，或在生成后查询校验
- **优先级**: P2

### PAPER-002: 验证码使用Math.random()而非SecureRandom

- **位置**: `PaperSubmissionServiceImpl.java:647`
- **描述**: 投稿者查询验证码使用`(int)((Math.random()*9+1)*100000)`生成，Math.random()为伪随机数生成器
- **影响**: 验证码可预测性高于SecureRandom，但6位数字验证码本身安全强度有限
- **建议**: 改用SecureRandom提升安全性
- **优先级**: P3

### PAPER-003: getPaperSubmissionList无筛选条件全量查询

- **位置**: `PaperSubmissionServiceImpl.java:152-158`
- **描述**: getPaperSubmissionList()方法无分页参数，直接查询全量数据返回List
- **影响**: 数据量增大时可能OOM或响应超时
- **建议**: 添加分页参数或限制返回条数
- **优先级**: P3

---

## 修复优先级排序

### P0 - 立即修复（安全风险）
1. SEC-001~005: 所有敏感配置项迁移到环境变量或Secret Manager

### P1 - 本迭代修复（数据/架构风险）
2. DB-001: 统一SQL基线脚本
3. DB-002: 引入Flyway版本化迁移
4. CODE-001: 拆分BounceEmailJob
5. CODE-004: paper_submission添加审计字段
6. ARCH-001: 邮件状态机文档（✅已完成）
7. ARCH-003: 支付回调幂等性文档（✅已完成）
8. ARCH-004: 支付回调Controller添加事务保护
9. ARCH-007: 团队折扣后端验证（⚠新增）
10. PAY-002: 退款流程补全（根据paymentMethod路由退款）
11. SEC-006: ignore-urls业务API添加认证保护（⚠新增）
12. SEC-008: /api/v1/files/**文件上传添加认证（⚠新增）

### P2 - 下迭代修复（代码质量）
13. CODE-002: 统一依赖注入方式
14. CODE-003: 消除魔法值
15. CODE-005: PDF导出逻辑分层
16. CODE-007: sys_log迁移InnoDB
17. CODE-008: team_id字段启用或清理（⚠新增）
18. CODE-009: 问卷功能启用或清理（⚠新增）
19. ARCH-002: i18n实现规范
20. ARCH-005: 统一支付回调路径（删除冗余Service方法）
21. PAY-003: 订单号生成改用UUID/雪花算法
22. PAY-004: 支付宝验签改用AlipaySignature.verifyV1()
23. SEC-007: @DataPermission覆盖范围扩展（⚠新增）
24. SEC-009: Actuator端点限制暴露范围（⚠新增）
25. SEC-012: 专家审核接口添加认证保护（⚠新增）
26. PAPER-001: 抵扣码唯一性校验（⚠新增）

### P3 - 长期优化
27. CODE-006: 废弃旧版日程表
28. CODE-010: 实现deleteTeamMembers()方法（⚠新增）
29. DB-003: 删除代码生成器表
30. ARCH-006: Survey/Expert/Skills组件纳入路由或删除（⚠新增）
31. PAY-001: 移除StripePaymentProvider未使用的BankTransferProperties
32. SEC-010: 删除ignore-urls重复配置（⚠新增）
33. SEC-011: PaymentController独立权限标识（⚠新增）
34. SEC-013: 投稿者修改接口添加验证码校验（⚠新增）
35. PAPER-002: 验证码改用SecureRandom（⚠新增）
36. PAPER-003: getPaperSubmissionList添加分页（⚠新增）