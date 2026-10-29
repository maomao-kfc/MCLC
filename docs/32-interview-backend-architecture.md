# MCLC 面试题库：后端架构与工程实践

> **基于实际代码，严禁编造**。每道题标注考察点和代码出处。

---

## Q1: 项目的技术选型理由？为什么用 Spring Boot 4 + MyBatis-Plus？

**考察点**: 技术选型、框架理解

**参考答案**:

**技术栈**（pom.xml 实测）：Spring Boot 4.0.1 / Java 17 / MyBatis-Plus 3.5.15 / MySQL 9.1.0 / Druid / Redisson 4.1.0 / Caffeine / XXL-Job 3.2.0 / MinIO / Tess4j / MapStruct / Knife4j

**选型理由**：
- **Spring Boot 4**：最新稳定版，Jakarta EE 10 命名空间，虚拟线程支持
- **MyBatis-Plus**：相比 JPA/Hibernate，SQL 控制力更强，单表 CRUD 零 SQL（BaseMapper），复杂查询写 XML。会议系统有大量复杂统计查询，需要 SQL 灵活性
- **Druid**：连接池+SQL 监控+慢查询日志，生产排查必备
- **Redisson + Caffeine 二级缓存**：Redisson 做分布式缓存（多实例共享），Caffeine 做本地缓存（高频读如字典数据），减少 Redis 网络开销
- **XXL-Job**：退信轮询（BounceEmailJob）需要定时执行+失败重试+执行日志，XXL-Job 提供可视化管理
- **MapStruct**：编译期代码生成，Entity↔VO/Form 转换零反射开销，比 BeanUtils 快且类型安全

**追问方向**: MyBatis-Plus 和 MyBatis 的区别？（增强不改，BaseMapper/条件构造器/分页插件/代码生成器）

**代码出处**: backend/pom.xml

---

## Q2: RBAC 权限系统是怎么实现的？有什么框架级约束？

**考察点**: Spring Security、RBAC 模型、框架源码理解

**参考答案**:

**模型**（system 模块）：用户 → 角色 → 菜单(type=C/M/B) → perm

```
sys_user → sys_user_role → sys_role → sys_role_menu → sys_menu
```

**鉴权链路**：
1. 登录 → AuthServiceImpl → 生成 JWT（含 userId/username）
2. 请求 → JwtFilter 解析 token → 加载用户权限
3. 权限来源：MenuServiceImpl.getRolePerms() → 查 sys_menu WHERE type='B' → 收集 perm
4. Spring Security @PreAuthorize("hasAuthority('portal:paymentProof:review')") 鉴权

**框架级约束**（案例04，踩过坑）：
- **youlai-boot 只加载 type=B（按钮）菜单的 perm**
- type=M（菜单）只用于路由展示，perm 不参与鉴权
- 新增菜单时 perm 必须配在 B 类型子菜单上，配在 M 上不生效 → 403

**前端联动**：
- v-permission 指令：根据用户 perm 列表控制按钮显示/隐藏
- 路由守卫：根据菜单树动态生成路由

**数据权限**（进阶）：
- @DataPermission 注解 + MyDataPermissionHandler（10.4KB）
- 基于 DataScopeEnum（全部/部门/本部门及以下/仅本人）动态拼接 SQL 条件

**追问方向**: JWT 无状态怎么实现登出？（Redis 黑名单，登出时 token 加入黑名单，过滤器检查）

**代码出处**: SecurityConfig.java / MenuServiceImpl.java / MyDataPermissionHandler.java

---

## Q3: Redis 缓存是怎么用的？为什么要用 db 12 而不是默认 db 0？

**考察点**: Redis 实践、环境隔离

**参考答案**:

**缓存架构**：Redisson（分布式）+ Caffeine（本地）二级缓存

**Redis 存储内容**：
- JWT token / 刷新 token
- 验证码（CaptchaProperties 配置过期时间）
- 字典数据缓存
- RepeatSubmit 防重复提交（@RepeatSubmit 注解 + RepeatSubmitAspect）
- RateLimiter 限流计数（RateLimiterFilter）

**db 12 而非 db 0**（本地环境特殊配置）：
- 本地 Redis 同时被多个项目共用
- 默认 db 0 已被其他项目占用
- MCLC 后端使用 db 12 隔离，排查缓存必须 `redis-cli -n 12`
- **教训**：排查缓存问题时如果忘记加 `-n 12`，会查不到 key，误以为缓存未生效

**Redisson 使用**：
- 分布式锁（RLock）
- 布隆过滤器（可选）
- 配置在 RedisConfig.java / RedisCacheConfig.java

**追问方向**: Caffeine 和 Redis 的缓存一致性怎么保证？（SSE 推送字典变更事件，各节点清除本地缓存——useDictSync.ts）

**代码出处**: RedisConfig.java / RedisCacheConfig.java / CaffeineConfig.java / RedisConstants.java

---

## Q4: 全局异常处理是怎么设计的？前端怎么拿到友好的错误信息？

**考察点**: 异常处理体系、前后端协作

**参考答案**:

**后端**（GlobalExceptionHandler.java 11.1KB）：
```java
@RestControllerAdvice
public class GlobalExceptionHandler {
    // BusinessException → Result.fail(code, msg)  — 业务异常，msg 直接给用户看
    // MethodArgumentNotValidException → 参数校验错误，拼接字段+消息
    // SQLSyntaxErrorException → 数据库异常，log.error 但返回通用提示
    // Exception → 兜底，log.error 完整堆栈，返回"系统繁忙"
}
```

**统一响应体**（Result<T>）：
```json
{ "code": 200, "msg": "成功", "data": {...} }
{ "code": 500, "msg": "收件地址在退信黑名单中，已拦截发送", "data": null }
```

**前端错误透传**（关键改进）：
- 修复前：前端 catch 后固定显示"操作失败"，丢失后端具体错误信息
- 修复后：前端从 response.data.msg 提取后端错误信息展示
- ElMessage.error(response.data.msg || '操作失败')

**BusinessException vs RuntimeException**：
- BusinessException：业务可预期异常（"该记录已处理，不可重复操作"），msg 面向用户
- RuntimeException：系统异常，msg 面向开发者，不直接暴露给用户

**追问方向**: 参数校验用什么？（@Valid + JSR 303 注解，或自定义 @ValidField + FieldValidator）

**代码出处**: GlobalExceptionHandler.java / Result.java / ResultCode.java

---

## Q5: 数据库设计有哪些核心表？有什么设计考量？

**考察点**: 数据库设计、ER 建模

**参考答案**:

**37 张表，5 大域**：

1. **注册支付域**（核心）：register_user_information / payment_orders / payment_proof / reconciliation_batch / reconciliation_record
2. **邮件域**：mail_send_record / mail_bounce_record / mail_bounce_blacklist / mail_imap_checkpoint
3. **会议内容域**：meeting_info / meeting_session / portal_* (11 张)
4. **论文域**：paper_submission / paper_submission_email
5. **系统域**：sys_user / sys_role / sys_menu / ... (14 张)

**设计考量**：

| 决策 | 理由 |
|------|------|
| proof_status 和 payment_status 分开 | 凭证审核和支付确认是两个独立维度 |
| 邮件用任务表模式 | 先落库再发送，记录不丢失 |
| 邮件重试用父子模型 | 保留完整发送历史链 |
| 对账用批次+记录两层 | 一次导入一个批次，批次下多条记录 |
| 软删除（逻辑删除） | BaseEntity 含 deleted 字段，MyBatis-Plus 自动过滤 |
| 乐观锁 | BaseEntity 含 version 字段，@Version 注解 |

**追问方向**: register_user_information 表为什么不用 JSON 存问卷答案？（单独 questionnaire_survey 表，JSON 查询效率低且无法建索引）

**代码出处**: backend/sql/SQLbackups/meeting_admin(结构).sql

---

## Q6: 定时任务是怎么设计的？退信轮询的频率怎么定？

**考察点**: XXL-Job、定时任务设计

**参考答案**:

**XXL-Job 配置**（XxlJobConfig.java）：
- 执行器自动注册
- 任务通过 XXL-Job 管理台配置（频率/重试/告警）

**BounceEmailJob**（46.2KB，最复杂的 Job）：
1. 连接 IMAP 收件箱
2. 读 mail_imap_checkpoint 游标
3. 拉取新邮件（UID > 游标）
4. BounceParser 解析
5. 匹配发送记录 → 更新状态
6. 永久退信 → 加黑名单
7. 更新游标

**频率考量**：
- 太频繁：IMAP 连接开销大，可能触发邮件服务商限流
- 太稀疏：退信发现延迟，用户等待久
- 建议每 5-15 分钟一次（通过 XXL-Job 管理台动态调整）

**断点续传**：
- IMAP UID 单调递增
- checkpoint 记录最后处理的 UID
- Job 崩溃重启后从 checkpoint 继续，不丢不重

**幂等性**：
- 同一 UID 不会重复处理（checkpoint 保证）
- 匹配发送记录时校验当前状态（只有 SUCCESS 才更新为 BOUNCE_DETECTED，已是 BOUNCE_DETECTED 则跳过）

**追问方向**: 如果 IMAP 连接一直失败怎么办？（XXL-Job 有失败重试+告警邮件，连续失败会通知管理员）

**代码出处**: BounceEmailJob.java / XxlJobConfig.java / MailImapCheckpointServiceImpl.java