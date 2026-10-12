# MCLC RBAC权限系统技术文档

> 基于角色的访问控制(RBAC) + 数据权限 + 4种认证方式 + JWT双令牌

## 1. 架构总览

```
┌────────────────────────────────────────────────────────────────────┐
│                     MCLC 安全架构                                   │
│                                                                    │
│  请求入口                                                          │
│  ┌──────────────────────────────────────────────────────────────┐ │
│  │ SecurityFilterChain                                          │ │
│  │  ┌──────────────┐ ┌──────────────┐ ┌──────────────────────┐ │ │
│  │  │RateLimiter   │→│CaptchaValid  │→│TokenAuthentication   │→│ │
│  │  │Filter        │ │Filter        │ │Filter(JWT解析)       │ │ │
│  │  └──────────────┘ └──────────────┘ └──────────────────────┘ │ │
│  └──────────────────────────────────────────────────────────────┘ │
│                              ↓                                     │
│  ┌──────────────────────────────────────────────────────────────┐ │
│  │ AuthenticationManager (ProviderManager)                      │ │
│  │  ┌────────────┐ ┌────────┐ ┌────────┐ ┌──────────────────┐ │ │
│  │  │DaoAuth     │ │SmsAuth │ │EmailAuth│ │WechatMiniAuth   │ │ │
│  │  │Provider    │ │Provider│ │Provider │ │Provider         │ │ │
│  │  │(用户名密码)│ │(短信)  │ │(邮箱)   │ │(微信小程序)     │ │ │
│  │  └────────────┘ └────────┘ └────────┘ └──────────────────┘ │ │
│  └──────────────────────────────────────────────────────────────┘ │
│                              ↓                                     │
│  ┌──────────────────────────────────────────────────────────────┐ │
│  │ JwtTokenManager                                              │ │
│  │  → generateToken(AccessToken + RefreshToken)                 │ │
│  │  → validateToken(签名+过期+tokenVersion+jti黑名单)           │ │
│  │  → invalidateToken(jti黑名单) / invalidateUserSessions      │ │
│  └──────────────────────────────────────────────────────────────┘ │
│                              ↓                                     │
│  ┌──────────────────────────────────────────────────────────────┐ │
│  │ 数据权限 MyDataPermissionHandler                             │ │
│  │  @DataPermission注解 → DataScopeEnum(5范围) → SQL条件注入   │ │
│  └──────────────────────────────────────────────────────────────┘ │
└────────────────────────────────────────────────────────────────────┘
```

## 2. 核心文件清单

### 2.1 后端安全核心文件

| 文件 | 路径 | 行数 | 职责 |
|------|------|------|------|
| SecurityConfig | `config/SecurityConfig.java` | 178 | Spring Security配置(过滤器链+4个Provider+Session策略) |
| SecurityProperties | `config/property/SecurityProperties.java` | 114 | 安全配置属性(ignoreUrls/unsecuredUrls/JWT/RedisToken) |
| TokenAuthenticationFilter | `security/filter/TokenAuthenticationFilter.java` | 60 | JWT Token解析过滤器(Authorization头→SecurityContext) |
| CaptchaValidationFilter | `security/filter/CaptchaValidationFilter.java` | - | 验证码校验过滤器(登录接口) |
| RateLimiterFilter | `core/filter/RateLimiterFilter.java` | - | Redis令牌桶限流过滤器 |
| PermissionService | `security/service/PermissionService.java` | 72 | @ss.hasPerm()权限校验组件(通配符匹配+Redis缓存) |
| JwtTokenManager | `security/token/JwtTokenManager.java` | - | JWT令牌管理(生成/解析/校验/撤销) |
| SysUserDetailsService | `security/service/SysUserDetailsService.java` | - | 用户认证信息服务(加载角色+权限) |
| DaoAuthenticationProvider | Spring内置 | - | 用户名+密码认证 |
| SmsAuthenticationProvider | `security/provider/SmsAuthenticationProvider.java` | - | 短信验证码认证 |
| EmailAuthenticationProvider | `security/provider/EmailAuthenticationProvider.java` | - | 邮箱验证码认证 |
| WechatMiniAuthenticationProvider | `security/provider/WechatMiniAuthenticationProvider.java` | - | 微信小程序认证 |
| MyDataPermissionHandler | `config/MyDataPermissionHandler.java` | 267 | 数据权限SQL注入处理器(JSQLParser+多角色OR并集) |
| DataPermission | `common/annotation/DataPermission.java` | 27 | 数据权限注解(4属性:deptAlias/deptIdColumnName/userAlias/userIdColumnName) |
| DataScopeEnum | `common/enums/DataScopeEnum.java` | 60 | 数据权限范围枚举(5种:ALL/DEPT_AND_SUB/DEPT/SELF/CUSTOM) |
| AuthController | `auth/controller/AuthController.java` | - | 认证API(登录/验证码/登出/刷新) |
| PortalAuthController | `portal/controller/PortalAuthController.java` | - | 门户邮箱登录API |
| WechatMiniappAuthController | `auth/controller/WechatMiniappAuthController.java` | - | 微信小程序认证API |

### 2.2 @DataPermission注解使用位置（仅3处）

| Mapper | 方法 | 注解参数 | SQL效果 |
|--------|------|----------|---------|
| UserMapper | `getUserPage()` | `deptAlias="u", userAlias="u"` | 用户分页列表按部门/本人过滤 |
| UserMapper | `listExportUsers()` | `deptAlias="u", userAlias="u"` | 用户导出列表按部门/本人过滤 |
| DeptMapper | `selectList()` | `deptIdColumnName="id"` | 部门列表按部门ID过滤 |

> **覆盖范围不足**：仅UserMapper和DeptMapper使用了@DataPermission注解，其他业务表（如注册用户、投稿、支付等）均无数据权限控制。

## 3. RBAC数据模型

### 3.1 五表关联模型

```
sys_user ──(多对多)── sys_user_role ──(多对多)── sys_role
    │                                               │
    │(dept_id)                                      │(dataScope)
    ↓                                               ↓
sys_dept                                    sys_role_menu ──(多对多)── sys_menu
                                                    │
                                              sys_role_dept (自定义数据权限)
```

### 3.2 实体字段

**sys_user** — 用户表
| 字段 | 说明 |
|------|------|
| username | 用户名(登录名) |
| nickname | 昵称 |
| password | 密码(BCrypt加密) |
| dept_id | 部门ID(FK→sys_dept) |
| status | 状态(1-正常 0-禁用) |
| email | 邮箱 |
| mobile | 手机号 |
| register_type | 注册类型(0-个人 1-团队) |

**sys_role** — 角色表
| 字段 | 说明 |
|------|------|
| name | 角色名称 |
| code | 角色编码(如ROLE_ADMIN) |
| data_scope | 数据权限范围(1-5) |
| status | 状态(1-正常 0-停用) |

**sys_menu** — 菜单表
| 字段 | 说明 |
|------|------|
| parent_id | 父菜单ID |
| name | 菜单名称 |
| type | 类型(C-目录 M-菜单 B-按钮) |
| route_name | Vue Router路由名称 |
| route_path | 路由路径 |
| component | 组件路径 |
| perm | 权限标识(如system:user:list) |
| visible | 显示状态(1-显示 0-隐藏) |
| keep_alive | 页面缓存(1-开启 0-关闭) |

**sys_dept** — 部门表
| 字段 | 说明 |
|------|------|
| name | 部门名称 |
| code | 部门编码 |
| parent_id | 父部门ID |
| tree_path | 父节点路径(逗号分隔) |
| status | 状态(1-正常 0-禁用) |

## 4. 四种认证方式

### 4.1 用户名密码认证(Dao)

```
POST /api/v1/auth/login
  ├── 请求: { username, password, captchaKey, captchaCode }
  ├── DaoAuthenticationProvider.authenticate()
  │     ├── SysUserDetailsService.loadUserByUsername(username)
  │     │     → 查询sys_user + 角色 + 权限
  │     ├── PasswordEncoder.matches(password, encodedPassword)
  │     └── 返回 SysUserDetails(含角色+数据权限)
  └── 响应: { accessToken, refreshToken, tokenType, expiresIn }
```

### 4.2 短信验证码认证(Sms)

```
POST /api/v1/auth/login/sms?mobile=xxx&code=xxx
  ├── SmsAuthenticationProvider.authenticate()
  │     ├── userService.getAuthInfoByMobile(mobile)
  │     ├── Redis校验验证码: captcha:sms:login:{mobile}
  │     ├── 验证成功后删除验证码
  │     └── 返回 SysUserDetails
  └── 响应: { accessToken, refreshToken, tokenType, expiresIn }
```

### 4.3 邮箱验证码认证(Email)

```
POST /api/v1/portal/auth/email/login
  ├── EmailAuthenticationProvider.authenticate()
  │     ├── registerUserService.getOne(email) → register_user_information表
  │     ├── Redis校验验证码: captcha:email:login:{email}
  │     ├── 验证成功后删除验证码
  │     └── 构建SysUserDetails(无角色/无数据权限)
  └── 响应: { accessToken, refreshToken, tokenType, expiresIn }

注意: 邮箱认证用户来自register_user_information表，非sys_user表
     → 无RBAC角色和数据权限
```

### 4.4 微信小程序认证(WechatMini)

```
POST /api/v1/wechat/miniapp/auth/silent-login?code=xxx
  ├── WechatMiniAuthenticationProvider.authenticate()
  │     ├── wxMaService.jsCode2SessionInfo(code) → openid + sessionKey
  │     ├── sysUserDetailsService.getWechatMiniBindInfo(openid) → sys_user_social
  │     ├── 未绑定 → 抛出NeedBindMobileException(openid, sessionKey)
  │     ├── 已绑定 → getAuthInfoByWechatOpenid(openid) → SysUserDetails
  │     └── 更新session_key
  └── 响应: { token } 或 { needBind: true, openid }

绑定手机号: POST /api/v1/wechat/miniapp/auth/bind-mobile
手机号快捷登录: POST /api/v1/wechat/miniapp/auth/phone-login
```

## 5. JWT令牌管理

### 5.1 双令牌机制

| 令牌类型 | 有效期 | 用途 |
|----------|--------|------|
| AccessToken | 配置项(秒) | API请求认证 |
| RefreshToken | 配置项(秒，更长) | 刷新AccessToken |

### 5.2 JWT Payload字段

| 字段 | 说明 |
|------|------|
| userId | 用户ID |
| deptId | 部门ID |
| dataScopes | 数据权限列表[{roleCode, dataScope, customDeptIds}] |
| authorities | 角色权限集合 |
| tokenType | 是否为RefreshToken |
| tokenVersion | Token版本号(会话失效控制) |
| iat/exp | 签发/过期时间 |
| jti | Token唯一标识(撤销用) |

### 5.3 Token校验流程

```
validateToken(token)
  ├── 1. 算法白名单校验(仅允许HS256)
  ├── 2. 签名验证 + 过期时间检查
  ├── 3. tokenVersion校验(用户级会话失效)
  │     → Redis: auth:user:token:version:{userId}
  │     → 版本号不匹配则Token无效
  └── 4. jti黑名单校验(单Token撤销)
        → Redis: auth:revoked:jti:{jti}
        → 存在则Token已撤销
```

### 5.4 会话失效机制

| 操作 | 机制 | 影响范围 |
|------|------|----------|
| 单端退出 | invalidateToken() → jti加入黑名单 | 仅当前Token |
| 修改密码/强制下线 | invalidateUserSessions() → tokenVersion++ | 该用户所有Token |
| Token过期 | 自动失效 | 仅当前Token |

## 6. 数据权限

### 6.1 DataScopeEnum — 5种数据范围

| 值 | 名称 | SQL效果 |
|----|------|---------|
| 1 | ALL | 无过滤(全部数据) |
| 2 | DEPT_AND_SUB | dept_id IN (SELECT id FROM sys_dept WHERE id=? OR FIND_IN_SET(?, tree_path)) |
| 3 | DEPT | dept_id = ? |
| 4 | SELF | create_by = ? |
| 5 | CUSTOM | dept_id IN (自定义部门ID列表，来自sys_role_dept) |

### 6.2 多角色合并策略(OR并集)

```
用户有角色A(DEPT) + 角色B(SELF)
  → SQL: (dept_id = ? OR create_by = ?)
  → 用户能看到本部门数据 + 自己的数据

用户有角色A(ALL) + 任何角色
  → 直接跳过过滤，查看所有数据
```

### 6.3 @DataPermission注解

```java
// 注解定义（4个属性）
@Target({ElementType.TYPE, ElementType.METHOD})
public @interface DataPermission {
    String deptAlias() default "";           // 部门表别名
    String deptIdColumnName() default "dept_id"; // 部门ID列名
    String userAlias() default "";           // 用户表别名
    String userIdColumnName() default "create_by"; // 用户ID列名
}
```

**实际使用位置（仅3处，覆盖范围不足）**：

| Mapper方法 | 注解参数 | SQL效果 |
|------------|----------|---------|
| `UserMapper.getUserPage()` | `deptAlias="u", userAlias="u"` | 用户分页按部门/本人过滤 |
| `UserMapper.listExportUsers()` | `deptAlias="u", userAlias="u"` | 用户导出按部门/本人过滤 |
| `DeptMapper.selectList()` | `deptIdColumnName="id"` | 部门列表按部门ID过滤 |

> ⚠️ **覆盖范围不足**：仅UserMapper和DeptMapper使用了@DataPermission注解，其他业务表（注册用户、投稿、支付、会议日程等）均无数据权限控制，任何登录用户可查看全部业务数据。

### 6.4 MyDataPermissionHandler源码级执行流程

```
MyDataPermissionHandler.getSqlSegment(where, mappedStatementId)  [行52]
  ├── 1. SecurityUtils.getUserId()==null || isRoot() → 跳过  [行54]
  ├── 2. SecurityUtils.getUser().getDataScopes() → List<RoleDataScope>  [行59]
  ├── 3. hasAllDataScope(dataScopes) → 任一角色ALL则跳过  [行64]
  ├── 4. Class.forName(mappedStatementId) → 反射获取Mapper接口  [行74]
  ├── 5. method.getAnnotation(DataPermission.class) → 无注解则跳过  [行82]
  └── 6. dataScopeFilterWithUnion()  [行88]
        ├── 遍历各角色dataScope  [行127]
        ├── buildRoleDataScopeExpression() → JSQLParser构建Expression  [行128]
        │     ├── ALL → null (不过滤)  [行185]
        │     ├── DEPT_AND_SUB → buildDeptAndSubExpression()  [行186]
        │     │     → SQL: dept_id IN (SELECT id FROM sys_dept WHERE id=? OR FIND_IN_SET(?, tree_path))  [行231-238]
        │     ├── DEPT → buildEqualsExpression(deptColumn, deptId)  [行187]
        │     ├── SELF → buildEqualsExpression(userColumn, userId)  [行188]
        │     └── CUSTOM → buildCustomDeptExpression(deptColumn, customDeptIds)  [行189]
        │           → 空列表返回1=0(无权限)  [行252-257]
        │           → 非空返回dept_id IN (?, ?, ...)  [行261-264]
        ├── OR连接各角色条件(并集)  [行135]
        ├── CCJSqlParserUtil.parseCondExpression()包裹括号  [行145]
        └── 与原始where用AND连接  [行152]
```

**关键实现细节**：
- DEPT_AND_SUB使用`FIND_IN_SET(deptId, tree_path)`实现部门树查询，而非递归SQL
- CUSTOM空列表返回`1=0`（永假条件），确保无权限时查不到任何数据
- JSQLParser构建Expression避免字符串拼接，但DEPT_AND_SUB和CUSTOM仍使用字符串解析后`CCJSqlParserUtil.parseCondExpression()`（行238/264），注释说明"避免不同JSqlParser版本渲染差异"
- 多角色OR并集：角色A(DEPT)+角色B(SELF)→`(dept_id = ? OR create_by = ?)`

## 7. 安全过滤器链

```
请求 → RateLimiterFilter → CaptchaValidationFilter → TokenAuthenticationFilter → Controller
         ↓                      ↓                        ↓
      Redis令牌桶限流         验证码校验(登录接口)     JWT解析→SecurityContext
```

### 7.1 过滤器配置详情

| 过滤器 | 顺序 | 注册方式 | 职责 |
|--------|------|----------|------|
| RateLimiterFilter | 1 | `addFilterBefore(..., UsernamePasswordAuthenticationFilter.class)` | Redis令牌桶限流 |
| CaptchaValidationFilter | 2 | `addFilterBefore(..., UsernamePasswordAuthenticationFilter.class)` | 登录接口验证码校验 |
| TokenAuthenticationFilter | 3 | `addFilterBefore(..., UsernamePasswordAuthenticationFilter.class)` | Authorization头解析JWT→Authentication |

> 三个过滤器均注册在`UsernamePasswordAuthenticationFilter`之前，执行顺序为注册顺序（先注册先执行）。

### 7.2 TokenAuthenticationFilter源码级流程

```
TokenAuthenticationFilter.doFilterInternal()  [行39]
  ├── 1. request.getHeader("Authorization")  [行41]
  ├── 2. 判断非空且以"Bearer "开头  [行44-45]
  ├── 3. rawToken = header.substring(7)  [行47]
  ├── 4. tokenManager.validateToken(rawToken) → 签名+过期+tokenVersion+jti黑名单  [行48]
  ├── 5. tokenManager.parseToken(rawToken) → Authentication对象  [行50]
  ├── 6. SecurityContextHolder.getContext().setAuthentication(auth)  [行51]
  └── 7. 异常时SecurityContextHolder.clearContext()  [行55]
```

**关键行为**：
- Token无效时不抛异常，仅清除SecurityContext，请求继续传递到Controller
- 后续`@PreAuthorize`或`authorizeHttpRequests`检查时发现无Authentication→返回401
- 不拦截无Authorization头的请求（允许公开接口正常访问）

### 7.3 SecurityConfig配置详情

```java
// SecurityConfig.java 核心配置 [行73-111]
http
  .authorizeHttpRequests(auth -> {
      ignoreUrls    → permitAll()   // 完全绕过安全过滤器
      unsecuredUrls → permitAll()   // 经过安全过滤器但允许匿名访问
      anyRequest()  → authenticated() // 其余需认证
  })
  .sessionManagement(session → sessionCreationPolicy(STATELESS))  // 无状态
  .csrf(disable)          // 前后端分离禁用CSRF
  .formLogin(disable)     // 禁用表单登录
  .httpBasic(disable)     // 禁用HTTP Basic
  .exceptionHandling(ex -> {
      authenticationEntryPoint → MyAuthenticationEntryPoint   // 401未认证
      accessDeniedHandler      → MyAccessDeniedHandler        // 403无权限
  })
```

**4个AuthenticationProvider Bean**：

| Provider | 行号 | 认证方式 | 数据源 |
|----------|------|----------|--------|
| DaoAuthenticationProvider | 127 | 用户名+密码 | SysUserDetailsService → sys_user表 |
| SmsAuthenticationProvider | 137 | 短信验证码 | UserService → Redis验证码校验 |
| EmailAuthenticationProvider | 145 | 邮箱验证码 | RegisterUserService → register_user_information表 |
| WechatMiniAuthenticationProvider | 153 | 微信小程序 | WxMaService + SysUserDetailsService → sys_user_social表 |

**AuthenticationManager**：ProviderManager包含上述4个Provider（行170-175）

**Session策略**：`SessionCreationPolicy.STATELESS` — 不创建HttpSession，完全依赖JWT令牌

## 8. 公开接口配置

### 8.1 ignore-urls（完全绕过安全过滤器，23个路径）

> ⚠️ **安全风险**：ignore-urls中的请求不经过任何安全过滤器（包括RateLimiterFilter、CaptchaValidationFilter、TokenAuthenticationFilter），即无认证、无限流、无验证码校验。

```yaml
security:
  ignore-urls:
    # === 认证相关（5个）===
    - /api/v1/auth/login/**          # 登录接口(账号密码/手机验证码/微信登录)
    - /api/v1/auth/captcha           # 验证码获取
    - /api/v1/auth/sms/code          # 发送登录短信验证码
    - /api/v1/auth/refresh-token     # 刷新令牌
    - /api/v1/wechat/miniapp/auth/** # 微信小程序认证(静默登录/手机号快捷登录/绑定手机号)

    # === 门户/注册相关（5个）⚠️ 业务API无认证暴露 ===
    - /api/v1/portal/**              # 门户查询后端信息
    - /api/v1/users/registerUser/**  # 注册
    - /api/v1/registerUser/**        # 公开注册用户新增
    - /api/v1/registrationType/**    # 公开注册类型查询
    - /api/v1/paperSubmission/**     # 公开投稿

    # === 支付相关（4个）⚠️ 支付回调+支付接口无认证暴露 ===
    - /api/v1/stripe/webhook         # Stripe支付回调
    - /api/v1/payment/**             # 支付接口
    - /api/v1/payment/alipay/notify  # 支付宝异步回调
    - /api/v1/payment/wechat/notify  # 微信支付异步回调

    # === 其他业务（6个）⚠️ 统计/文件/邮件/架构等无认证暴露 ===
    - /api/v1/statistics/**          # 统计分析接口(访问趋势/访问概览)
    - /ws/**                         # WebSocket接口
    - /api/v1/files/**               # 文件上传
    - /api/v1/discountConfiguration/** # 折扣配置
    - /api/v1/email-templates/**     # 邮箱配置
    - /api/v1/architecture/**        # 组织架构

    # === 会议日程（1个）===
    - /api/v1/meetingSchedule/allMeetingSchedule/** # 会议日程查询

    # === 重复配置（2个，与上面重复）===
    - /api/v1/registrationType/**    # ⚠️ 与上面重复，第161行
```

**安全风险标注**：
- 🔴 **高风险**：`/api/v1/files/**` — 文件上传接口无认证，可被滥用上传恶意文件
- 🔴 **高风险**：`/api/v1/statistics/**` — 统计数据无认证暴露，可能泄露业务数据
- 🟡 **中风险**：`/api/v1/paperSubmission/**` — 投稿接口无认证，可被恶意提交
- 🟡 **中风险**：`/api/v1/email-templates/**` — 邮件模板无认证暴露
- 🟢 **低风险**：支付回调接口（Stripe/支付宝/微信）— 第三方回调无法携带认证，属正常设计
- ℹ️ **冗余**：`/api/v1/registrationType/**` 出现两次（行155和行161），重复配置

### 8.2 unsecured-urls（经过安全过滤器但允许匿名访问，7个路径）

> 这些路径经过安全过滤器链（限流、验证码、JWT解析），但最终被`permitAll()`放行。适用于Swagger文档、监控端点等。

```yaml
security:
  unsecured-urls:
    - ${springdoc.swagger-ui.path}   # Swagger UI路径
    - /doc.html                      # Knife4j文档
    - /swagger-ui/**                 # Swagger UI资源
    - /v3/api-docs/**                # OpenAPI规范
    - /webjars/**                    # 前端依赖资源
    - /favicon.ico                   # 网站图标
    - /admin/**                      # Spring Boot Admin监控端点
    - /actuator/**                   # Actuator监控端点
```

### 8.3 ignore-urls vs unsecured-urls 区别

| 维度 | ignore-urls | unsecured-urls |
|------|-------------|----------------|
| 是否经过安全过滤器 | ❌ 不经过 | ✅ 经过 |
| 限流保护 | ❌ 无 | ✅ 有 |
| JWT解析 | ❌ 无 | ✅ 有（但不强制认证） |
| 适用场景 | 认证接口、第三方回调 | Swagger文档、监控端点 |
| 安全配置方式 | `requestMatchers(urls).permitAll()` | `requestMatchers(urls).permitAll()` |

> **注意**：Spring Security 6.x中，两种URL最终都通过`authorizeHttpRequests().permitAll()`配置，区别在于ignore-urls在Spring Security 5.x中通过`WebSecurity.ignoring()`完全绕过过滤器链，而6.x中已统一。当前代码注释（行115-121）说明已移除`WebSecurityCustomizer`，unsecured-urls改为`permitAll()`方式。

## 9. @PreAuthorize权限注解分布

### 9.1 PermissionService实现细节

```java
// PermissionService.java [行24-72]
@Component("ss")  // SpEL表达式引用: @ss.hasPerm('sys:user:add')
public class PermissionService {
    private final RoleMenuService roleMenuService;

    public boolean hasPerm(String requiredPerm) {
        // 1. 空权限直接拒绝 [行40]
        if (StrUtil.isBlank(requiredPerm)) return false;
        // 2. 超级管理员放行 [行45]
        if (SecurityUtils.isRoot()) return true;
        // 3. 获取当前用户角色编码 [行50]
        Set<String> roleCodes = SecurityUtils.getRoles();
        if (CollectionUtil.isEmpty(roleCodes)) return false;
        // 4. 获取角色权限列表（带Redis缓存） [行56]
        Set<String> rolePerms = roleMenuService.getRolePermsByRoleCodes(roleCodes);
        if (CollectionUtil.isEmpty(rolePerms)) return false;
        // 5. 通配符匹配 [行62]
        // PatternMatchUtils.simpleMatch(rolePerm, requiredPerm)
        // 例如: "sys:user:*" 可匹配 "sys:user:add"
        boolean hasPermission = rolePerms.stream()
                .anyMatch(rolePerm -> PatternMatchUtils.simpleMatch(rolePerm, requiredPerm));
        // 6. 无权限时记录警告日志 [行65-67]
        if (!hasPermission) log.warn("用户无操作权限: userId={}, username={}, requiredPerm={}");
        return hasPermission;
    }
}
```

**权限校验链路**：
```
@PreAuthorize("@ss.hasPerm('sys:user:create')")
  → PermissionService.hasPerm("sys:user:create")
    → SecurityUtils.isRoot() → true: 放行
    → SecurityUtils.getRoles() → 角色编码集合
    → roleMenuService.getRolePermsByRoleCodes(roleCodes) → Redis缓存读取
      → 缓存key: role:perms:{roleCode}
      → 缓存未命中 → sys_role_menu JOIN sys_menu 查询
    → PatternMatchUtils.simpleMatch(rolePerm, requiredPerm)
      → "sys:user:*" 匹配 "sys:user:create" ✓
      → "sys:user:list" 匹配 "sys:user:create" ✗
```

### 9.2 权限标识模式

所有权限标识遵循 `sys:资源:操作` 三段式命名：

| 段 | 说明 | 示例 |
|----|------|------|
| 第1段 | 固定前缀 `sys` | sys |
| 第2段 | 资源名称 | user, role, menu, dept, notice, dict... |
| 第3段 | 操作类型 | create, update, delete, list, assign, import, export... |

### 9.3 @PreAuthorize注解按模块分布（共92处）

**系统管理模块（system包，47处）**：

| Controller | 权限标识 | 数量 |
|------------|----------|------|
| UserController | sys:user:create/update/delete/reset-password/import/export | 8 |
| RoleController | sys:role:create/update/delete/assign | 7 |
| MenuController | sys:menu:create/update/delete | 4 |
| DeptController | sys:dept:create/update/delete | 3 |
| NoticeController | sys:notice:list/create/update/publish/revoke/delete | 7 |
| DictController | sys:dict:create/update/delete + sys:dict-item:create/update/delete | 7 |
| ConfigController | sys:config:list/create/refresh/update/delete | 5 |
| RegistrationTypeController | sys:registrationType:create/update/delete | 3 |
| EmailTemplateController | sys:email-template:create/update/delete | 3 |

**门户业务模块（portal包，45处）**：

| Controller | 权限标识 | 数量 |
|------------|----------|------|
| SpeechController | sys:speech:create/update/delete | 4 |
| QuestionController | sys:question:create/update/delete | 4 |
| PaperSubmissionController | sys:paperSubmission:update/delete | 2 |
| PaymentController | sys:registerUser:update | 2 |
| MomentsController | sys:moments:create/update/delete | 4 |
| MeetingSettingController | sys:meetingSetting:create/update/delete | 4 |
| MeetDateController | sys:meetDate:create/update/delete | 3 |
| HotelController | sys:hotel:create/update/delete | 4 |
| GuestController | sys:guest:create/update/delete | 4 |
| ContactController | sys:contact:create/update/delete | 4 |
| ArrangeController | sys:arrange:create/update/delete | 4 |
| ArchitectureController | sys:architecture:create/update/delete | 4 |
| UserCheckInController | sys:userCheckIn:delete | 1 |

**权限覆盖分析**：
- ✅ 写操作（create/update/delete）全部有@PreAuthorize保护
- ⚠️ 读操作（list/detail/get）大多无@PreAuthorize保护，依赖SecurityFilterChain的`authenticated()`检查
- ⚠️ PaymentController使用`sys:registerUser:update`权限标识，而非独立的`sys:payment:*`标识

### 10.1 管理端认证

| 方法 | URL | 说明 |
|------|-----|------|
| GET | /api/v1/auth/captcha | 获取图形验证码 |
| POST | /api/v1/auth/login | 用户名密码登录 |
| POST | /api/v1/auth/login/sms | 短信验证码登录 |
| POST | /api/v1/auth/sms/code | 发送短信验证码 |
| DELETE | /api/v1/auth/logout | 退出登录 |
| POST | /api/v1/auth/refresh-token | 刷新令牌 |

### 10.2 门户认证

| 方法 | URL | 说明 |
|------|-----|------|
| POST | /api/v1/portal/auth/email/code | 发送邮箱验证码 |
| POST | /api/v1/portal/auth/email/login | 邮箱验证码登录 |

### 10.3 微信小程序认证

| 方法 | URL | 说明 |
|------|-----|------|
| POST | /api/v1/wechat/miniapp/auth/silent-login | 静默登录 |
| POST | /api/v1/wechat/miniapp/auth/phone-login | 手机号快捷登录 |
| POST | /api/v1/wechat/miniapp/auth/bind-mobile | 绑定手机号 |

## 11. 常见问题排查

### 11.1 登录失败
1. 检查用户status是否为1(正常)
2. 检查密码是否正确(BCrypt加密)
3. 检查验证码是否过期(Redis key: `captcha:login:{captchaKey}`)
4. 检查RateLimiterFilter是否限流(Redis令牌桶)
5. 检查DaoAuthenticationProvider密码编码器是否匹配

### 11.2 Token无效
1. 检查Authorization头格式: `Bearer {token}`
2. 检查Token是否过期(JWT exp字段)
3. 检查Redis中tokenVersion是否匹配: `auth:user:token:version:{userId}`
4. 检查Redis中jti是否在黑名单: `auth:revoked:jti:{jti}`
5. TokenAuthenticationFilter异常时仅清除SecurityContext，不抛异常

### 11.3 数据权限不生效
1. 确认方法上有@DataPermission注解（当前仅UserMapper和DeptMapper有）
2. 确认角色dataScope字段已设置（sys_role.data_scope）
3. 确认用户非超级管理员（超级管理员跳过过滤）
4. 检查SQL中表别名是否与注解一致（deptAlias/userAlias）
5. 检查自定义部门数据(sys_role_dept表)是否配置

### 11.4 微信小程序登录失败
1. 检查WxMaService配置(appid/appsecret)
2. 检查code是否有效(5分钟过期)
3. 检查sys_user_social表中是否有绑定记录
4. 未绑定用户需先调用bind-mobile接口

### 11.5 @PreAuthorize权限不生效
1. 确认SecurityConfig上有`@EnableMethodSecurity`注解（行49）
2. 确认PermissionService注册为`@Component("ss")`（行24）
3. 确认权限标识格式为`sys:资源:操作`三段式
4. 检查sys_role_menu表中是否配置了对应权限
5. 检查Redis缓存是否过期或数据不一致

### 11.6 ignore-urls接口被限流
1. ignore-urls不经过RateLimiterFilter，不会被限流
2. 如果需要限流保护，应将URL移至unsecured-urls
3. 注意：unsecured-urls经过完整安全过滤器链

## 12. 安全风险发现

| 编号 | 风险 | 严重度 | 说明 |
|------|------|--------|------|
| SEC-006 | ignore-urls包含大量业务API | 高 | `/api/v1/files/**`、`/api/v1/statistics/**`、`/api/v1/paperSubmission/**`等业务接口无认证暴露，可被未登录用户访问 |
| SEC-007 | @DataPermission覆盖范围不足 | 中 | 仅UserMapper和DeptMapper使用数据权限注解，其他业务表(注册用户/投稿/支付等)无数据权限控制 |
| SEC-008 | /api/v1/files/**无认证文件上传 | 高 | 文件上传接口在ignore-urls中，可被未登录用户滥用上传恶意文件 |
| SEC-009 | /actuator/**在unsecured-urls中 | 中 | Actuator监控端点允许匿名访问，可能泄露应用运行信息 |
| SEC-010 | ignore-urls重复配置 | 低 | `/api/v1/registrationType/**`出现两次(行155+行161) |
| SEC-011 | PaymentController权限标识复用 | 低 | PaymentController使用`sys:registerUser:update`而非独立的`sys:payment:*`权限标识 |