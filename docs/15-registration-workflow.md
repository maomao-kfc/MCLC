# MCLC 注册工作流技术文档

> 会议注册全流程：前端6步向导(Enroll→Consent→Contact→Signup→Confirm→Final) + 后端审核→支付→签到
> 含团队注册、签证信息、账单信息、抵扣码双重验证、Pinia状态管理
>
> **最后更新**: 2026-10-29

## 更新日志（2026-08-14 后变更）

| 变更 | 说明 | 关联文档 |
|------|------|----------|
| 注册确认页面流程重构 | 从"提交即完成"改为"提交→确认→凭证上传"分步确认流程 | - |
| 注册成功判定+通知机制重构 | 注册成功不再自动判定，改为三种显式触发（凭证审核通过/对账确认/线下登记） | [13-payment-subsystem.md](13-payment-subsystem.md) |
| 两封邮件机制 | 凭证提交→第一封"待核对"(id=7)；审核通过→第二封"注册通过"(id=6) | [12-email-lifecycle.md](12-email-lifecycle.md) |
| "我的注册"入口迁移 | 从 HeaderLang（语言切换区域）移到主导航栏，提升可见性 | - |
| 支付时间手动填写 | 支付时间从自动取当前时间改为用户手动填写+日期选择器（中英文适配） | - |
| i18n中英文key补全 | 注册流程相关的前端中英文翻译key全部写入 | [19-i18n-strategy.md](19-i18n-strategy.md) |

## 1. 业务流程总览

### 1.1 前端6步向导流程

```
┌─────────────────────────────────────────────────────────────────────────────────┐
│                     前端6步注册向导 (Register.vue stepOrder)                       │
│                                                                                 │
│  ┌──────────┐  ┌──────────┐  ┌──────────┐  ┌──────────┐  ┌──────────┐  ┌──────────┐
│  │1.Enroll  │→ │2.Consent │→ │3.Contact │→ │4.Signup  │→ │5.Confirm │→ │6.Final   │
│  │创建账户  │  │隐私政策  │  │联系信息  │  │注册类型  │  │订单确认  │  │注册完成  │
│  │姓名+邮箱 │  │4项同意   │  │地址+团队 │  │签证+API  │  │抵扣码+付 │  │摘要+跳转 │
│  │          │  │          │  │          │  │          │  │款二维码  │  │          │
│  └──────────┘  └──────────┘  └──────────┘  └──────────┘  └──────────┘  └──────────┘
│       │              │              │              │              │              │
│  store.enroll  store.consent  store.contact  userStore.     updateRegister  clearRegister
│  firstName     agreePolicy1  title/phone    doRegister()   User API        Data
│  lastName      agreePolicy2  organization  →POST注册API   →PUT更新支付
│  email         agreeThird    street/zip     →获取token     →paymentMethod
│  registerType  agreeBadge    country/team                  →deductionCode
│                                                                             │
│  ⚠ 未纳入路由的组件: Survey.vue(问卷490行) / Expert.vue(专家课程201行)         │
│                        / Skills.vue(技能培训265行)                           │
└─────────────────────────────────────────────────────────────────────────────────┘
```

### 1.2 前后端交互时序

```
用户浏览器                    Pinia Store                后端API
    │                            │                         │
    │──Enroll步:填写姓名邮箱────→│store.enroll              │
    │──Consent步:4项同意────────→│store.consent             │
    │──Contact步:联系信息+团队──→│store.contact             │
    │──Signup步:选择注册类型────→│                          │
    │   └──handleNext()────────→│userStore.doRegister()──→│POST /registerUser
    │                            │  mapStateToRegisterUser │  (upsert模式)
    │                            │  Form()转换             │  ↓
    │                            │  ←token+id+userInfo────│  邮箱唯一校验
    │                            │  →sessionStorage持久化  │  设置pending
    │──Confirm步:抵扣码验证────→│                          │  发送注册邮件
    │   └──checkDeductionCode──→│────────────────────────→│GET /paperSubmission
    │   └──handleConfirm()────→│────────────────────────→│PUT /registerUser/{id}
    │                            │                          │  更新支付信息
    │──Final步:注册完成────────→│clearRegisterData()       │
    │   └──跳转首页/登录────────→│                          │
    │                            │                         │
    │═══════ 后续:管理员审核 ═════════════════════════════│
    │                            │                         │
    │                            │    POST /registerUser/  │
    │                            │    {id}/audit           │
    │                            │                         │  reviewStatus=1
    │                            │                         │  验证抵扣码
    │                            │                         │  生成二维码
    │                            │   ←审核通过邮件─────────│  发送邮件
    │                            │                         │
    │═══════ 后续:支付 ═══════════════════════════════════│
    │                            │                         │
    │                            │    支付系统回调──────────→│payment_status=paid
    │                            │                         │
    │═══════ 后续:签到 ═══════════════════════════════════│
    │                            │                         │
    │──扫码签到──────────────────────────────────────────→│POST /registerUser/checkIn
    │                            │                         │
```

### 1.3 数据实体关联

```
register_user_information (35+字段)
  │
  ├── connect_id → questionnaire_survey.id (⚠ 代码已注释，功能未启用)
  ├── team_id → register_user_information.id (⚠ exist=false，数据库无此列)
  ├── deduction_code → paper_submission.discount_code (抵扣码)
  │
  ├── payment_orders.registration_id → 支付订单
  └── user_check_in.check_id → 签到记录
```

## 2. 核心文件清单

### 2.1 后端文件

| 文件 | 路径 | 行数 | 职责 |
|------|------|------|------|
| RegisterUserController | `portal/controller/` | 92 | 7个REST端点(CRUD+审核+签到)，@Log+@RepeatSubmit |
| RegisterUserService | `portal/service/` | - | 注册用户服务接口 |
| RegisterUserServiceImpl | `portal/service/impl/` | 508 | 8个方法，注册核心业务逻辑 |
| RegisterUserInformation | `portal/model/entity/` | 135 | 注册用户实体，35+字段，team_id exist=false |
| RegisterUserForm | `portal/model/form/` | 190 | 注册表单，45+字段，含TeamMemberForm内部类 |
| RegisterUserQuery | `portal/model/query/` | 61 | 15个查询字段，继承BaseQuery |
| RegisterUserPageVO | `portal/model/vo/` | 181 | 分页展示VO，45+字段，含@JsonFormat |
| RegisterUserConverter | `portal/converter/` | - | MapStruct转换器 |
| CheckInForm | `portal/model/form/` | - | 签到表单(@Valid) |
| UserCheckIn | `portal/model/entity/` | - | 签到记录实体 |

### 2.2 前端文件（Webpage端）

| 文件 | 路径 | 行数 | 职责 |
|------|------|------|------|
| Register.vue | `pages/` | 282 | 6步向导主页面，stepOrder路由定义，步骤验证逻辑 |
| register.js | `stores/` | 185 | Pinia Store，7个state分区+sessionStorage持久化 |
| registerFormMapper.js | `utils/` | 119 | Store state → RegisterUserForm字段映射转换 |
| Enroll.vue | `pages/register/` | 220 | 步骤1：姓名+邮箱+注册类型 |
| Consent.vue | `pages/register/` | 189 | 步骤2：4项隐私政策同意 |
| Contact.vue | `pages/register/` | 576 | 步骤3：联系信息+账单地址+团队成员 |
| Signup.vue | `pages/register/` | 389 | 步骤4：注册类型+签证信息，触发注册API |
| Confirm.vue | `pages/register/` | 482 | 步骤5：订单确认+抵扣码+支付二维码+更新支付 |
| Final.vue | `pages/register/` | 189 | 步骤6：注册完成摘要+跳转登录 |
| Survey.vue | `pages/register/` | 490 | ⚠ 问卷调查（未纳入路由） |
| Expert.vue | `pages/register/` | 201 | ⚠ 专家课程选择（未纳入路由） |
| Skills.vue | `pages/register/` | 265 | ⚠ 技能培训选择（未纳入路由） |
| user.js | `api/` | - | 注册/查询/更新3个API |
| contribution.js | `api/` | - | checkDeductionCode抵扣码验证API |
| discountConfiguration.js | `api/` | - | 折扣配置查询API |
| user.js | `stores/` | 100+ | 用户Store，doRegister()+doLogin()+token管理 |

## 3. 注册用户数据模型

### 3.1 RegisterUserInformation — 35+字段分组

| 分组 | 字段 | 说明 |
|------|------|------|
| **基本信息** | name, surname, email, organization, mobilePhone, country, street, postalCode | 个人信息 |
| **账单信息** | billOrganization, billVat, billProcurement, billStreet, billPostalCode, billCity, billStatus, billCountry | 发票/账单地址 |
| **注册信息** | registrationType, registrationFee, registrationDate, registerLabel | 注册类型和费用 |
| **支付信息** | paymentMethod, paymentStatus, paymentAmount, stripePaymentIntentId, actualPayment, picBank | 支付方式和状态 |
| **签证信息** | visaName, visaSurname, visaPassport, visaIssueTime, visaEndTime, visaTime, visaArea, visaIsNeed | 签证申请材料 |
| **审核信息** | reviewStatus, rejectReason | 审核状态和拒绝原因 |
| **折扣** | deductionCode | 抵扣码(关联paper_submission.discount_code) |
| **关联** | connectId(⚠问卷ID，代码已注释), teamId(⚠团队ID，exist=false) | 关联表 |

### 3.2 RegisterUserForm — 45+字段（前端→后端传输）

| 分组 | 字段 | 校验 | 说明 |
|------|------|------|------|
| **基本信息** | name, surname | @NotBlank | 姓名（必填） |
| | email | @Email | 邮箱（必填+格式） |
| | organization, mobilePhone, street, postalCode, country | - | 联系信息 |
| **账单信息** | billOrganization, billVat, billProcurement, billStreet, billPostalCode, billCity, billCountry | - | 账单地址（可选） |
| **注册信息** | registrationType, registrationFee, registerLabel | - | 注册类型/费用/标签 |
| **支付信息** | paymentMethod, paymentAmount, actualPayment, picBank, deductionCode | - | 支付方式/金额/抵扣码 |
| **签证信息** | visaName, visaSurname, visaPassport, visaIssueTime, visaEndTime, visaTime, visaArea, visaIsNeed | - | 签证邀请函 |
| **审核信息** | reviewStatus, rejectReason | - | 审核状态 |
| **团队成员** | teamMembers: List\<TeamMemberForm\> | - | 团队成员列表 |
| **问卷字段** | registrationCategory, registrationDocument, registrationWhichMember, registrationCode, isMatchDrug, dietaryRequirements, specialRequirements, isFirst, gender, occupation, experience, fieldOfInterest, other | - | ⚠ 10个问卷字段，代码已注释 |

**TeamMemberForm内部类**: name(@NotBlank), surname(@NotBlank), email(@Email), mobilePhone

### 3.3 reviewStatus — 审核状态

| 值 | 说明 | 触发点 |
|-----|------|--------|
| null/0 | 待审核 | 新增注册时默认值 |
| 1 | 审核通过 | 管理员POST /{id}/audit |
| 2 | 审核拒绝 | 管理员POST /{id}/audit + rejectReason |

### 3.4 paymentStatus — 支付状态

| 值 | 说明 | 触发点 |
|-----|------|--------|
| null/pending | 待支付 | 新增注册时设置"pending" |
| paid | 已支付 | 支付回调确认 |
| refunded | 已退款 | 退款操作 |

### 3.5 前端Store→后端Form字段映射（registerFormMapper.js）

| Store分区 | Store字段 | Form字段 | 转换逻辑 |
|-----------|----------|---------|---------|
| enroll | firstName/lastName | name/surname | contact优先，enroll兜底 |
| enroll | email | email | contact优先，enroll兜底 |
| enroll | registerType | registrationType | '0'=个人, '1'=团队 |
| contact | firstName/lastName | name/surname | 主地址字段 |
| contact | phone | mobilePhone | - |
| contact | street/zipCode/country | street/postalCode/country | - |
| contact | organization | organization | - |
| contact.billingAddress | * | bill* | hasBillingAddress时映射 |
| contact | teamMembers | teamMembers | firstName→name, lastName→surname |
| survey.questionnaire | registerType | registrationCategory | 问卷注册类别 |
| survey.questionnaire | prescription | isMatchDrug | yes→1, no→0 |
| survey.questionnaire | gender | gender | male→男, female→女, unknown→不愿披露 |
| survey.questionnaire | job | occupation | eyeDoctor→眼科医生等5种映射 |
| signup | registerType | registrationFee | 注册费用类型 |
| signup | visaIsNeed/visaInfo | visa* | visaIsNeed?1:0, 7个签证字段 |
| state | deductionCode | deductionCode | 仅deductionVerified=true时映射 |
| signup | registerLabel/registerTypeName | registerLabel | 注册标签 |

**注意**: 问卷字段（survey分区）虽然在mapper中有映射逻辑，但Survey组件未纳入路由，用户无法填写这些字段。

## 4. RegisterUserServiceImpl 方法级分解

### 4.1 方法总览

| 方法 | 行号 | 事务 | 说明 |
|------|------|------|------|
| `getRegisterUserPage()` | :68 | - | 分页查询，委托Mapper.selectRegisterUserPage |
| `saveRegisterUser()` | :77 | @Transactional | **核心**：upsert模式，id==null新增否则更新 |
| `deleteRegisterUsers()` | :227 | @Transactional | 批量删除，逗号分隔ID |
| `getRegisterUserDetail()` | :247 | - | 委托Mapper.selectRegisterUserDetail |
| `getRegisterUserList()` | :253 | - | 列表查询，手动entity→vo转换 |
| `sendLoginEmailCode()` | :310 | - | 邮箱验证码，Redis 5分钟过期 |
| `auditRegisterUser()` | :328 | @Transactional | 审核流程+抵扣码验证+二维码+邮件 |
| `checkIn()` | :376 | @Transactional | 签到upsert |
| `sanitizeDateFields()` | :207 | - | 空字符串日期转null（私有辅助方法） |
| `deleteTeamMembers()` | :222 | - | ⚠ 空方法，team_id未启用 |

### 4.2 saveRegisterUser() — upsert模式详解

```
RegisterUserServiceImpl.saveRegisterUser(form)  [:77, @Transactional]
  │
  ├── 分支判断: form.getId() == null ?
  │
  ├── [新增分支] form.getId() == null
  │     ├── 1. 邮箱唯一性校验
  │     │     └── 同一邮箱不可重复注册(排除自身ID)
  │     │
  │     ├── 2. 转换并设置默认值
  │     │     ├── registerUserConverter.toRegisterUserInformation(form)
  │     │     ├── 设置paymentStatus = "pending"
  │     │     ├── 设置registrationDate = 当天日期
  │     │     └── sanitizeDateFields() → 空字符串日期转null
  │     │
  │     ├── 3. 插入数据库
  │     │     └── this.save(registerUser)
  │     │
  │     ├── 4. 发送注册成功邮件
  │     │     ├── 获取"注册通过模板"邮件模板
  │     │     ├── 替换{{name1}}为用户全名
  │     │     └── mailService.sendHtmlMail()
  │     │
  │     └── 5. 处理团队成员(如有)
  │           ├── 遍历form.teamMembers
  │           ├── 为每个成员创建RegisterUserInformation
  │           ├── 设置teamId = 主注册人ID  ⚠ exist=false,实际不写入DB
  │           ├── 继承主注册人的组织/账单/签证等信息
  │           └── this.save(memberUser)
  │
  └── [更新分支] form.getId() != null
        ├── 1. 查找已有记录
        │     └── this.getById(form.getId())
        │
        ├── 2. 更新所有字段
        │     ├── registerUserConverter.toRegisterUserInformation(form)
        │     ├── 设置id + paymentStatus(保留原值)
        │     └── sanitizeDateFields()
        │
        ├── 3. 删除旧团队成员
        │     └── deleteTeamMembers(registerUser.getId())  ⚠ 空方法,无实际操作
        │
        ├── 4. 重新创建团队成员
        │     └── 同新增分支步骤5
        │
        └── 5. 更新数据库
              └── this.updateById(registerUser)
```

**关键发现**:
- 同一个方法处理创建和修改，通过`id==null`判断分支
- 前端Signup步骤调用`doRegister()`触发POST（新增），Confirm步骤调用`updateRegisterUser`触发PUT（更新支付信息）
- `deleteTeamMembers()`是空方法，更新团队成员时旧成员不会被删除
- `team_id`标记`exist=false`，团队成员的teamId字段不会写入数据库

### 4.3 auditRegisterUser() — 审核流程详解

```
RegisterUserServiceImpl.auditRegisterUser(id, reviewStatus, rejectReason)  [:328, @Transactional]
  │
  ├── 1. 查找注册用户
  │     └── this.getById(id)
  │
  ├── 2. 更新审核状态
  │     ├── registerUser.setReviewStatus(reviewStatus)
  │     └── registerUser.setRejectReason(rejectReason)
  │
  └── 3. 审核通过(reviewStatus == 1)时
        ├── 3a. 验证邮箱不为空
        │
        ├── 3b. 抵扣码验证(如有deductionCode)
        │     ├── 查找paper_submission表discount_code
        │     ├── 校验存在且deductionStatus == 0(未使用)
        │     └── 更新deductionStatus = 1(已使用)
        │
        ├── 3c. 生成签到二维码
        │     ├── qrContent = checkInUrl + registerUser.id
        │     │     (配置: http://139.196.41.138:18080/#/checkIn?id=)
        │     └── QRCodeUtils.generateBase64QRCode(qrContent)
        │
        └── 3d. 发送审核通过邮件
              ├── 邮件标题: "注册审核通过 - 现场签到二维码"
              ├── 邮件内容: 审核通过通知 + 二维码图片(base64内嵌)
              └── mailService.sendHtmlMail()
```

**抵扣码双重验证机制**:
- **前端验证**（Confirm步骤）: `checkDeductionCode` API → GET /paperSubmission?discountCode=xxx → 检查deductionStatus===0
- **后端验证**（审核时）: `auditRegisterUser` → 查paper_submission表 → 检查deductionStatus==0 → 标记为1
- 两次验证场景不同：前端是用户输入时的即时验证，后端是管理员审核时的最终确认

## 5. 前端6步向导详细字段映射

### 5.1 步骤1: Enroll — 创建账户 (Enroll.vue, 220行)

| 字段 | Store路径 | 验证规则 | 说明 |
|------|----------|---------|------|
| firstName | store.enroll.firstName | @NotBlank, 必填 | 名 |
| lastName | store.enroll.lastName | @NotBlank, 必填 | 姓 |
| email | store.enroll.email | @Email, 必填 | 邮箱 |
| registerType | store.enroll.registerType | 默认'0' | 注册类型(0=个人,1=团队) |

**同步逻辑**: firstName/lastName/email同时写入store.enroll和store.contact

### 5.2 步骤2: Consent — 隐私政策同意 (Consent.vue, 189行)

| 字段 | Store路径 | 验证规则 | 说明 |
|------|----------|---------|------|
| agreePolicy1 | store.consent.agreePolicy1 | 必填 | 数据隐私政策 |
| agreePolicy2 | store.consent.agreePolicy2 | 必填 | ICO隐私政策 |
| agreeThirdParty | store.consent.agreeThirdParty | 必填 | 第三方数据共享 |
| agreeBadgeScan | store.consent.agreeBadgeScan | 必填 | 徽章扫描同意 |

**完成条件**: `allChecked`计算属性 = 4项全部勾选

### 5.3 步骤3: Contact — 联系信息 (Contact.vue, 576行)

**主地址字段**:

| 字段 | Store路径 | 验证规则 | 说明 |
|------|----------|---------|------|
| title | store.contact.title | @required | 称谓 |
| firstName | store.contact.firstName | @required | 名 |
| lastName | store.contact.lastName | @required | 姓 |
| email | store.contact.email | @required + pattern | 邮箱 |
| organization | store.contact.organization | @required | 组织 |
| phone | store.contact.phone | @required + pattern | 电话 |
| street | store.contact.street | @required | 街道 |
| zipCode | store.contact.zipCode | @required + pattern | 邮编 |
| city | store.contact.city | @required + pattern | 城市 |
| country | store.contact.country | @required | 国家 |

**账单地址字段**（hasBillingAddress勾选时显示）:

| 字段 | Store路径 | 验证规则 | 说明 |
|------|----------|---------|------|
| organization | store.contact.billingAddress.organization | - | 账单组织 |
| vatNumber | store.contact.billingAddress.vatNumber | - | VAT号 |
| poNumber | store.contact.billingAddress.poNumber | - | PO号 |
| street | store.contact.billingAddress.street | @required | 街道 |
| zipCode | store.contact.billingAddress.zipCode | @required + pattern | 邮编 |
| city | store.contact.billingAddress.city | @required + pattern | 城市 |
| country | store.contact.billingAddress.country | @required | 国家 |

**团队成员**（registerType==='1'时显示）:

| 字段 | Store路径 | 验证规则 | 说明 |
|------|----------|---------|------|
| firstName | store.contact.teamMembers[n].firstName | @required | 成员名 |
| lastName | store.contact.teamMembers[n].lastName | @required | 成员姓 |
| email | store.contact.teamMembers[n].email | @required + pattern | 成员邮箱 |
| phone | store.contact.teamMembers[n].phone | @required + pattern | 成员电话 |

**验证**: contactRules(10字段) + billingRules(4字段) + validateTeamMembers()

### 5.4 步骤4: Signup — 注册类型+签证 (Signup.vue, 389行)

| 字段 | Store路径 | 验证规则 | 说明 |
|------|----------|---------|------|
| registerType | store.signup.registerType | @required | 注册费类型(从API获取feeList) |
| registerLabel | store.signup.registerLabel | - | 注册标签 |
| registerTypeName | store.signup.registerTypeName | - | 注册类型名称 |
| visaIsNeed | store.signup.visaIsNeed | - | 是否需要签证邀请函 |
| visaName | store.signup.visaInfo.visaName | @required(签证时) | 签证名 |
| visaSurname | store.signup.visaInfo.visaSurname | @required(签证时) | 签证姓 |
| visaPassport | store.signup.visaInfo.visaPassport | @required(签证时) | 护照号 |
| visaIssueTime | store.signup.visaInfo.visaIssueTime | @required(签证时) | 签发日期 |
| visaEndTime | store.signup.visaInfo.visaEndTime | @required(签证时) | 到期日期 |
| visaTime | store.signup.visaInfo.visaTime | @required(签证时) | 签证时长 |
| visaArea | store.signup.visaInfo.visaArea | @required(签证时) | 签证区域 |

**关键**: `handleNext()`调用`userStore.doRegister(registerStore.state)` → `mapStateToRegisterUserForm()` → `POST /api/v1/registerUser`（触发实际注册API调用）

### 5.5 步骤5: Confirm — 订单确认 (Confirm.vue, 482行)

| 功能 | API调用 | 说明 |
|------|---------|------|
| 抵扣码验证 | `checkDeductionCode(code)` → GET /paperSubmission | deductionStatus===0有效 |
| 团队折扣 | `getDiscountConfigurationList()` → GET /discountConfiguration | 按memberCount匹配minPeople/maxPeople |
| 价格计算 | 前端计算 | unitPrice × count × discountRate |
| 抵扣码生效 | 前端设置 | finalPrice=0 |
| 支付二维码 | 静态图片 | studentRegistration.png / regularRegistration.png |
| 支付链接 | URL跳转 | 按类型跳转不同coursecenter URL |

**关键**: `handleConfirm()`调用`updateRegisterUser API` → `PUT /api/v1/registerUser/{id}`（更新支付信息，非首次创建）

更新字段: paymentMethod / picBank / paymentAmount / actualPayment / deductionCode

### 5.6 步骤6: Final — 注册完成 (Final.vue, 189行)

| 显示内容 | 来源 | 说明 |
|----------|------|------|
| 用户摘要 | store.enroll + store.contact | lastName/firstName/phone/email/title/organization |
| 支付摘要 | store (抵扣码未使用时) | paymentMethod/total/dueTotal/amountReceived |

**操作**: `handleConfirm()` → 跳转首页 + `registerStore.clearRegisterData()`（清空sessionStorage）

### 5.7 未纳入路由的组件

| 组件 | 行数 | 对应Form字段 | 说明 |
|------|------|-------------|------|
| Survey.vue | 490 | registrationCategory/registrationWhichMember/registrationCode/isMatchDrug/dietaryRequirements/specialRequirements/isFirst/gender/occupation/experience/fieldOfInterest/other | 问卷调查，10个字段在Form中有映射但无法填写 |
| Expert.vue | 201 | - | 专家课程选择，硬编码6月26-27日数据 |
| Skills.vue | 265 | - | 技能培训选择，硬编码6月26-29日数据 |

**架构问题**: 这3个组件存在但未在Register.vue的stepOrder中注册，用户无法访问。registerFormMapper.js中有survey字段的映射逻辑，但survey数据始终为空对象。

## 6. 注册状态机

### 6.1 前端步骤完成条件

| 步骤 | 完成条件 | 验证方法 |
|------|---------|---------|
| Enroll | firstName + lastName + email 非空 | `isEnrollCompleted()` |
| Consent | 4项policy全部勾选 | `isConsentCompleted()` → allChecked |
| Contact | 主地址必填字段全部填写 + 团队成员验证 | `isContactCompleted()` |
| Signup | 注册类型已选 + 签证字段(如需)已填 | handleNext()内验证 |
| Confirm | 支付信息已更新 | handleConfirm()调用API |
| Final | 无条件，显示摘要 | 自动完成 |

**跳转规则**: 向前跳转需验证之前步骤完成，向后跳转无需验证

### 6.2 后端状态转换

```
                         ┌──────────────────────────────┐
                         │    register_user_information   │
                         │                                │
   POST /registerUser    │  paymentStatus: null → pending │  POST /{id}/audit
   ──────────────────→   │  reviewStatus: null            │  ──────────────────→
                         │                                │
                         │  ┌───────┐     ┌───────────┐   │
                         │  │pending│────→│   paid    │   │  支付回调确认
                         │  └───────┘     └───────────┘   │
                         │       │              │         │
                         │       │              ↓         │
                         │       │        ┌───────────┐   │
                         │       │        │ refunded  │   │  退款操作
                         │       │        └───────────┘   │
                         │                                │
                         │  ┌───────┐     ┌───────────┐   │
                         │  │ null/0│────→│     1     │   │  审核通过
                         │  └───────┘     └───────────┘   │
                         │       │              │         │
                         │       │              ↓         │
                         │       │        生成二维码      │
                         │       │        验证抵扣码      │
                         │       │        发送邮件        │
                         │       │                        │
                         │       ↓                        │
                         │  ┌───────────┐                │
                         │  │     2     │                │  审核拒绝
                         │  └───────────┘                │
                         └──────────────────────────────┘
```

### 6.3 Pinia Store状态管理

```
register.js (Pinia Store, 185行)
  │
  ├── State分区 (7个):
  │   ├── enroll: { firstName, lastName, email, registerType }
  │   ├── consent: { agreePolicy1, agreePolicy2, agreeThirdParty, agreeBadgeScan }
  │   ├── contact: { title, firstName, lastName, email, organization, phone,
  │   │              street, zipCode, city, country, hasBillingAddress,
  │   │              billingAddress: {...}, teamMembers: [...] }
  │   ├── survey: { questionnaire: {...} }  ⚠ 未使用
  │   ├── signup: { registerType, registerLabel, registerTypeName, visaIsNeed, visaInfo: {...} }
  │   ├── skills: { selectedSkills: [...] }  ⚠ 未使用
  │   ├── expert: { selectedCourses: [...] }  ⚠ 未使用
  │   └── 顶层: { paymentMethod, deductionVerified, deductionCode, picBank }
  │
  ├── 持久化: sessionStorage key='registerData'
  │
  ├── 关键方法:
  │   ├── getAllRegisterData() → 合并enroll+contact+signup+paymentMethod提交后端
  │   └── clearRegisterData() → 清空所有状态+移除sessionStorage
  │
  └── user.js Store交互:
      ├── doRegister(state) → mapStateToRegisterUserForm() → POST /registerUser
      │   └── 成功后: token→sessionStorage, registrationId→sessionStorage
      └── doLogin(email, code) → loginByEmail → fetchAndStoreSubmissionUserId
```

## 7. 团队注册机制

### 7.1 前端团队注册流程

```
Contact.vue (registerType==='1'时显示团队成员区域)
  │
  ├── 动态添加成员: addTeamMember() → push空对象到teamMembers数组
  ├── 动态删除成员: removeTeamMember(index) → splice删除
  ├── 成员字段: firstName/lastName/email/phone (4个必填)
  │
  └── Confirm.vue (团队折扣计算)
        ├── getDiscountConfigurationList() → 获取折扣规则
        │     └── GET /api/v1/discountConfiguration
        │
        ├── 按memberCount匹配规则:
        │     ├── minPeople <= memberCount <= maxPeople
        │     └── discountRate = 匹配到的折扣率
        │
        └── 价格计算:
              ├── unitPrice = 注册类型单价
              ├── count = 1(主注册人) + teamMembers.length
              ├── discountRate = 团队折扣率
              └── totalPrice = unitPrice × count × discountRate
```

### 7.2 后端团队成员处理

```
saveRegisterUser() — 新增分支:
  │
  ├── 遍历form.teamMembers
  ├── 为每个成员创建RegisterUserInformation
  │     ├── name/surname/email/mobilePhone ← 成员自身字段
  │     ├── organization/country/street/postalCode ← 继承主注册人
  │     ├── bill* ← 继承主注册人账单信息
  │     ├── visa* ← 继承主注册人签证信息
  │     ├── teamId ← 主注册人ID  ⚠ exist=false, 不写入DB
  │     ├── registrationType ← 主注册人
  │     └── paymentStatus ← "pending"
  │
  └── this.save(memberUser) → 插入数据库

saveRegisterUser() — 更新分支:
  │
  ├── deleteTeamMembers(registerUser.getId())  ⚠ 空方法,无实际操作
  │
  └── 重新创建团队成员 (同新增分支)
```

**关键问题**:
1. `team_id`字段`exist=false`：团队成员与主注册人的关联关系无法在数据库层面维护
2. `deleteTeamMembers()`是空方法：更新注册时旧团队成员不会被删除，会累积
3. 团队折扣仅前端计算：后端无对应折扣验证逻辑，存在价格篡改风险

## 8. 支付集成

注册与支付系统的交互通过`PaymentServiceImpl`实现：

```
注册用户 → 创建支付意图
  │
  ├── POST /api/v1/payment/create-intent
  │     └── form.registrationId → 关联注册用户
  │     └── form.amount → 如为空则取registerUser.paymentAmount
  │
  ├── 支付回调 → 更新register_user_information.payment_status = "paid"
  │     ├── Stripe Webhook → payment_intent.succeeded
  │     ├── 支付宝回调 → TRADE_SUCCESS
  │     └── 微信回调 → TradeState.SUCCESS
  │
  └── 银行转账确认
        └── POST /api/v1/payment/confirm/{registrationId}
              └── 管理员手动确认 → payment_status = "paid"
```

**前端Confirm步骤的支付信息更新**:
- `handleConfirm()` → `PUT /api/v1/registerUser/{id}` → 更新paymentMethod/picBank/paymentAmount/actualPayment/deductionCode
- 这是注册后的支付信息补充，而非首次创建注册记录

## 9. 签到流程

```
RegisterUserServiceImpl.checkIn(form)  [:376, @Transactional]
  │
  ├── 1. 查找注册用户 by form.checkId
  ├── 2. 查找已有签到记录
  │     ├── 已有 → 更新签到时间和信息 (upsert)
  │     └── 无 → 创建新签到记录
  │
  ├── 3. 签到信息
  │     ├── checkId → 注册用户ID
  │     ├── checkName → 签到姓名
  │     ├── checkNumber → 签到编号
  │     ├── checkEmail → 签到邮箱
  │     └── checkStatus → "签到成功"
  │
  └── 4. 返回签到结果
        ├── checkStatus: "签到成功"
        └── checkTime: 签到时间
```

**签到二维码生成**: 审核通过时生成，内容为 `{checkInUrl}{userId}`，前端扫码后调用签到API。

## 10. 邮箱登录验证码

```
RegisterUserServiceImpl.sendLoginEmailCode(email)  [:310]
  │
  ├── 1. 校验邮箱已注册
  ├── 2. 生成6位随机验证码
  ├── 3. 发送验证码邮件
  └── 4. 存入Redis，5分钟过期
        └── key: captcha:email:login:{email}
```

## 11. API接口

| 方法 | URL | 权限 | 说明 |
|------|-----|------|------|
| GET | /api/v1/registerUser | @Log | 注册用户分页列表 |
| POST | /api/v1/registerUser | @RepeatSubmit | 新增注册用户 |
| PUT | /api/v1/registerUser/{id} | - | 修改注册用户(复用saveRegisterUser) |
| DELETE | /api/v1/registerUser/{ids} | - | 删除注册用户(逗号分隔) |
| GET | /api/v1/registerUser/{id} | - | 注册用户详情 |
| POST | /api/v1/registerUser/{id}/audit | - | 审核注册用户 |
| POST | /api/v1/registerUser/checkIn | @Valid | 签到 |

**前端额外调用的API**:

| 方法 | URL | 步骤 | 说明 |
|------|-----|------|------|
| POST | /api/v1/registerUser | Signup | 首次注册(通过userStore.doRegister) |
| PUT | /api/v1/registerUser/{id} | Confirm | 更新支付信息 |
| GET | /api/v1/paperSubmission | Confirm | 抵扣码验证(checkDeductionCode) |
| GET | /api/v1/discountConfiguration | Confirm | 团队折扣规则查询 |
| GET | /api/v1/registrationType | Signup | 注册费类型列表 |

## 12. 数据关联

```
register_user_information
  │
  ├── connect_id → questionnaire_survey.id  ⚠ 代码已注释，功能未启用
  ├── team_id → register_user_information.id  ⚠ exist=false，数据库无此列
  ├── deduction_code → paper_submission.discount_code (抵扣码)
  │
  ├── payment_orders.registration_id → 支付订单
  └── user_check_in.check_id → 签到记录
```

## 13. 已知问题

| 编号 | 类别 | 描述 | 优先级 |
|------|------|------|--------|
| ARCH-006 | 架构 | Survey/Expert/Skills组件存在但未纳入路由stepOrder，用户无法访问 | P3 |
| ARCH-007 | 架构 | 团队折扣仅前端计算，后端无验证逻辑，存在价格篡改风险 | P1 |
| CODE-008 | 代码 | team_id字段exist=false，团队成员关联功能未启用 | P2 |
| CODE-009 | 代码 | questionnaire_survey相关代码全部注释，connectId关联未启用 | P2 |
| CODE-010 | 代码 | deleteTeamMembers()空方法，更新注册时旧团队成员不会被删除 | P3 |

## 14. 常见问题排查

### 14.1 注册失败
1. 检查邮箱是否已注册（唯一性校验）
2. 检查必填字段：name, surname
3. 检查email格式（@Email校验）
4. **SQL**: `SELECT * FROM register_user_information WHERE email = ? AND id != ?`

### 14.2 审核通过但未收到邮件
1. 检查用户email字段是否为空
2. 检查邮件子系统状态（参见14-email-subsystem.md）
3. 检查mail_send_record表中对应记录的send_status
4. **SQL**: `SELECT * FROM mail_send_record WHERE receiver = ? ORDER BY create_time DESC LIMIT 5`

### 14.3 抵扣码无效
1. 检查paper_submission表中discount_code是否存在
2. 检查deduction_status是否为0（未使用）
3. 抵扣码使用后会标记为1，不可重复使用
4. **前端验证**: Confirm步骤checkDeductionCode API → deductionStatus===0
5. **后端验证**: auditRegisterUser审核时再次验证 → deductionStatus==0 → 标记为1
6. **SQL**: `SELECT discount_code, deduction_status FROM paper_submission WHERE discount_code = ?`

### 14.4 签到二维码不显示
1. 检查check-in配置（application-dev.yml中checkInUrl）
2. 检查QRCodeUtils是否正常生成base64图片
3. 确认审核已通过（reviewStatus == 1）才会生成二维码
4. **SQL**: `SELECT review_status FROM register_user_information WHERE id = ?`

### 14.5 团队成员未关联主注册人
1. 检查team_id字段：实体标记exist=false，数据库无此列
2. 团队成员记录已创建但无法通过team_id查询关联
3. **SQL**: `SELECT * FROM register_user_information WHERE email IN (团队成员邮箱列表)`

### 14.6 更新注册后团队成员累积
1. deleteTeamMembers()是空方法，不会删除旧团队成员
2. 每次更新注册会重新创建团队成员，导致记录累积
3. **SQL**: `SELECT email, COUNT(*) FROM register_user_information GROUP BY email HAVING COUNT(*) > 1`

### 14.7 问卷字段始终为空
1. Survey.vue未纳入路由stepOrder，用户无法填写问卷
2. registerFormMapper.js中有survey字段映射，但数据源始终为空对象
3. 10个问卷字段(registrationCategory/gender/occupation等)在Form中存在但永远为空

## 15. 2026-08-14 后新增功能

### 15.1 注册确认页面流程重构

原始流程：前端6步向导最后一步(Final)提交后即完成注册。重构后：

```
6步向导完成 → 进入确认页面（Confirm Review）
  → 用户确认信息 → 提交注册
  → 跳转凭证上传页面（支付凭证）
  → 上传凭证 → 发送第一封"待核对"邮件
  → 等待管理员审核/对账确认
  → 审核通过 → payment_status=paid → 发送第二封"注册通过"邮件
```

### 15.2 三种注册成功触发方式

注册成功（payment_status 变为 paid/offline_paid）不再自动判定，改为三种显式触发：

| 触发方式 | 入口 | payment_status | 说明 |
|----------|------|----------------|------|
| 凭证审核通过 | 管理端 reviewProof(pass) | paid | 用户上传凭证→OCR初筛→管理员审核 |
| 对账确认匹配 | 管理端 confirmMatch | paid | 银行流水Excel导入→匹配引擎→人工确认 |
| 线下登记 | 管理端 offlineRegister | offline_paid | 管理员直接登记线下付款用户 |

三种触发方式均通过 `sendRegistrationSuccessEmail()` 发送"注册通过"邮件（模板id=6，变量{{name1}}）。

### 15.3 "我的注册"入口

- **原始位置**：HeaderLang 组件内（与语言切换按钮并列，可见性差）
- **当前位置**：主导航栏（navItems 数组末尾，faq 之后）
- **路由**：`/my-registration`
- **配置文件**：`webpage/src/data/nav.js`

### 15.4 支付时间手动填写

凭证上传表单中，支付时间从自动填充 `LocalDateTime.now()` 改为用户手动选择：

- 前端：Element Plus DatePicker，中英文环境格式适配（yyyy-MM-dd / MM/dd/yyyy）
- 后端：接收用户填写的支付时间字段，用于对账时间维度匹配（±1天20分/±3天10分/±7天10分）