# 论文投稿模块代码流程文档

## 一、模块概述

论文投稿模块实现了从投稿人提交论文、管理员发送审稿邮件、专家审核、到审核结果通知的完整流程。

---

## 二、文件结构一览

```
backend/src/main/java/com/youlai/boot/portal/
├── controller/
│   └── PaperSubmissionController.java    ← 接口层：接收HTTP请求，调用Service
├── service/
│   ├── PaperSubmissionService.java       ← 接口定义
│   └── impl/
│       ├── PaperSubmissionServiceImpl.java  ← 核心业务逻辑（9个功能方法）
│       └── PaperPdfGeneratorServiceImpl.java ← PDF生成服务（封面页、信息页、合并）
├── model/
│   ├── entity/
│   │   ├── PaperSubmission.java          ← 论文投稿实体（对应 paper_submission 表）
│   │   └── PaperSubmissionEmail.java     ← 邮件发送记录实体（对应 paper_submission_email 表）
│   ├── form/
│   │   ├── PaperSubmissionForm.java      ← 新增/修改投稿表单
│   │   └── PaperSubmissionReviewForm.java ← 发送审稿邮件表单
│   ├── query/
│   │   └── PaperSubmissionQuery.java     ← 查询参数
│   └── vo/
│       ├── PaperSubmissionPageVO.java    ← 分页列表展示对象
│       └── EmailSendResultVO.java        ← 邮件发送结果
├── mapper/
│   ├── PaperSubmissionMapper.java        ← 论文投稿Mapper
│   └── PaperSubmissionEmailMapper.java   ← 邮件记录Mapper
└── converter/
    └── PaperSubmissionConverter.java     ← 对象转换器

frontend/src/
├── api/portal/paperSubmission.ts         ← 前端API调用
├── types/api/paperSubmission.ts          ← 前端类型定义
└── views/contribution/contributionIndex/
    └── index.vue                         ← 前端页面组件
```

---

## 三、数据库表结构

### 3.1 paper_submission（论文投稿表）

| 字段 | 类型 | 说明 |
|------|------|------|
| id | bigint | 主键ID |
| name | varchar | 投稿人姓名 |
| paper_title | varchar | 论文标题 |
| first_author | varchar | 第一作者 |
| contact_number | varchar | 联系电话 |
| contact_email | varchar | 联系邮箱 |
| abstract | longtext | 论文摘要 |
| keywords | varchar | 关键词 |
| appendix | varchar | 论文附件URL |
| contribution_id | bigint | 投稿ID |
| paper_status | int | 审核状态（1-待审核 2-通过 3-拒绝） |
| reject_reason | varchar | 拒绝原因 |
| expert_url | varchar | 专家评审链接 |
| email_status | int | 邮件发送状态（0-未发送 1-失败 2-已发送） |
| deduction_status | int | 抵扣状态 |
| discount_code | varchar | 抵扣码（审核通过时生成） |

### 3.2 paper_submission_email（邮件发送记录表）

| 字段 | 类型 | 说明 |
|------|------|------|
| id | bigint | 主键ID |
| submission_id | bigint | 关联的论文投稿ID |
| expert_name | varchar | 专家姓名 |
| expert_email | varchar | 专家邮箱 |
| url | varchar | 投稿件链接（可为空） |
| sender_name | varchar | 发送人姓名 |
| send_status | int | 发送状态（1-发送中 2-成功 3-失败） |
| send_time | datetime | 发送时间 |
| review_result | int | 审核结果（0-未审核 1-通过 2-未通过） |
| reject_reason | varchar | 拒绝原因 |
| review_time | datetime | 审核时间 |

---

## 四、核心业务流程

### 4.1 发送审稿邮件流程（最核心）

```
前端点击"发送审稿邮件"
    │
    ▼
Controller: PUT /api/v1/paperSubmission/{id}/review
    │  文件: PaperSubmissionController.java → reviewPaperSubmission()
    │
    ▼
Service: updatePaperSubmissionStatus(id, form)
    │  文件: PaperSubmissionServiceImpl.java → 第96行
    │
    ├─ 步骤1: 校验论文投稿是否存在
    ├─ 步骤2: 检查是否已发送过邮件（防止重复）
    ├─ 步骤3: 更新论文投稿的 expertUrl 和 emailStatus
    ├─ 步骤4: 遍历专家列表，为每个专家：
    │   ├─ 4a: 创建邮件发送记录（paper_submission_email表）
    │   ├─ 4b: 构建邮件内容
    │   │   ├─ 有附件 → 使用邮件模板替换占位符
    │   │   │   {{name1}} → 专家姓名
    │   │   │   {{name2}} → 发送人姓名
    │   │   │   {{论文稿件}} → 附件URL
    │   │   │   {{审批论文稿件}} → 审批链接
    │   │   │
    │   │   └─ 无附件 → 也使用邮件模板，但特殊处理{{论文稿件}}
    │   │       用正则替换包含{{论文稿件}}的<a>标签为投稿者信息表格
    │   │       buildSubmissionInfoTable() 方法
    │   │       表格包含：姓名、论文标题、摘要、联系电话、联系邮箱
    │   │
    │   ├─ 4c: 调用 mailService.sendHtmlMailSync() 同步发送
    │   └─ 4d: 更新邮件发送状态（2-成功/3-失败）
    │
    └─ 步骤5: 更新论文投稿 emailStatus = 2（已发送）
```

**关键代码位置：**
- Controller入口: `PaperSubmissionController.java` 第92-100行
- Service核心逻辑: `PaperSubmissionServiceImpl.java` 第96-170行
- 无附件邮件构建: `PaperSubmissionServiceImpl.java` 第332-349行
- 邮件发送服务: `MailServiceImpl.java` 第107-128行

### 4.2 专家审核流程

```
专家点击邮件中的审批链接
    │
    ▼
前端审批页面 → 调用审核接口
    │
    ▼
Controller: POST/PUT /api/v1/paperSubmission/expertReview
    │  文件: PaperSubmissionController.java → expertReview()
    │
    ▼
Service: expertReview(id, reviewResult, rejectReason)
    │  文件: PaperSubmissionServiceImpl.java → 第173行
    │
    ├─ 步骤1: 校验邮件记录是否存在
    ├─ 步骤2: 防止重复审核
    ├─ 步骤3: 更新审核结果和审核时间
    ├─ 步骤4: 检查该投稿下所有专家是否都已审核
    └─ 步骤5: 全部审核完毕时：
        ├─ 计算通过率（≥50% → 通过，<50% → 拒绝）
        ├─ 通过：生成8位随机抵扣码，paperStatus=2
        ├─ 拒绝：paperStatus=3
        └─ 异步发送邮件通知投稿人
```

### 4.3 重发审稿邮件流程

```
前端点击"重发"按钮
    │
    ▼
Controller: POST /api/v1/paperSubmission/resendEmail/{emailId}
    │  文件: PaperSubmissionController.java → resendReviewEmail()
    │
    ▼
Service: resendReviewEmail(emailId)
    │  文件: PaperSubmissionServiceImpl.java → 第266行
    │
    ├─ 步骤1: 校验邮件记录
    ├─ 步骤2: 获取关联的论文投稿信息
    ├─ 步骤3: 构建邮件内容（同首次发送逻辑）
    ├─ 步骤4: 同步发送邮件
    ├─ 步骤5: 更新发送状态和时间
    └─ 步骤6: 同步更新论文投稿的邮件状态
```

### 4.4 导出PDF流程

```
前端点击"导出PDF"按钮
    │
    ▼
Controller: GET /api/v1/paperSubmission/{id}/exportPdf
    │  文件: PaperSubmissionController.java → exportPdf()
    │
    ├─ 附件是PDF → 封面页 + 附件PDF 合并
    │   PaperPdfGeneratorServiceImpl.generateCoverPage() → 生成封面页
    │   PaperPdfGeneratorServiceImpl.mergePdfs() → 合并PDF
    │
    ├─ 附件是DOCX/DOC → 封面页 + JODConverter转PDF 合并
    │   DocxToPdfService.convertDocxToPdf() → DOCX转PDF
    │   失败时回退到OpenPDF生成信息页
    │
    └─ 无附件 → OpenPDF生成完整信息PDF
        PaperPdfGeneratorServiceImpl.generatePdf()
        包含：论文标题、姓名、联系电话、论文摘要
```

---

## 五、功能方法索引

| 功能 | 方法名 | 文件 | 行号 |
|------|--------|------|------|
| 分页查询 | getPaperSubmissionPage() | PaperSubmissionServiceImpl.java | 56 |
| 新增投稿 | savePaperSubmission() | PaperSubmissionServiceImpl.java | 66 |
| 删除投稿 | deletePaperSubmissions() | PaperSubmissionServiceImpl.java | 72 |
| 发送审稿邮件 | updatePaperSubmissionStatus() | PaperSubmissionServiceImpl.java | 96 |
| 专家审核 | expertReview() | PaperSubmissionServiceImpl.java | 173 |
| 查询审核记录 | getExpertReviewList() | PaperSubmissionServiceImpl.java | 245 |
| 查询邮件记录 | getEmailRecord() | PaperSubmissionServiceImpl.java | 253 |
| 重发审稿邮件 | resendReviewEmail() | PaperSubmissionServiceImpl.java | 266 |
| 构建投稿者信息表格 | buildSubmissionInfoTable() | PaperSubmissionServiceImpl.java | 493 |
| 构建表格行 | buildTableRow() | PaperSubmissionServiceImpl.java | 512 |
| 导出PDF | exportPdf() | PaperSubmissionController.java | 152 |
| 生成封面页PDF | generateCoverPage() | PaperPdfGeneratorServiceImpl.java | 74 |
| 生成信息页PDF | generatePdf() | PaperPdfGeneratorServiceImpl.java | 52 |
| 合并PDF | mergePdfs() | PaperPdfGeneratorServiceImpl.java | 190 |

---

## 六、邮件模板说明

邮件模板存储在数据库 `sys_email_template` 表中，通过 `emailType` 字段区分：

| 模板类型 | emailType | 占位符 |
|----------|-----------|--------|
| 专家审核模板 | 专家审核模板 | {{name1}}(专家姓名) {{name2}}(发送人) {{论文稿件}}(附件链接) {{审批论文稿件}}(审批链接) |
| 审核通过模板 | 论文审核通过模板 | {{name1}}(投稿人姓名) {{name2}}(论文标题) {{code}}(抵扣码) |
| 审核未通过模板 | 论文审核未通过模板 | {{name1}}(投稿人姓名) {{name2}}(论文标题) {{code}}(抵扣码) |

**注意：** 无附件时，也使用"专家审核模板"，但用正则将包含 `{{论文稿件}}` 的 `<a>` 标签整体替换为投稿者信息表格（`buildSubmissionInfoTable`方法），避免 `<a>` 标签内嵌套 `<table>` 导致HTML错乱。

---

## 七、配置项

| 配置项 | 文件 | 说明 |
|--------|------|------|
| confirm-review | application-dev.yml / application-prod.yml | 专家审批链接前缀 |
| spring.mail.* | application-dev.yml / application-prod.yml | 邮件服务器配置 |

---

## 八、状态码说明

### paperStatus（论文审核状态）
- 1 = 待审核
- 2 = 审核通过
- 3 = 审核拒绝

### emailStatus（邮件发送状态）
- 0 = 未发送
- 1 = 发送失败
- 2 = 已发送

### sendStatus（邮件记录发送状态）
- 1 = 发送中
- 2 = 发送成功
- 3 = 发送失败

### reviewResult（专家审核结果）
- 0 = 未审核
- 1 = 已通过
- 2 = 未通过

---

## 九、前端调用流程

```
前端页面: contributionIndex/index.vue
    │
    ├─ 分页查询: PaperSubmissionAPI.getPage()
    │   → GET /api/v1/paperSubmission
    │
    ├─ 发送审稿邮件: PaperSubmissionAPI.review(id, data)
    │   → PUT /api/v1/paperSubmission/{id}/review
    │   data = { emailsList: [{name, email}], url: appendix, senderName }
    │
    ├─ 重发邮件: PaperSubmissionAPI.resendEmail(emailId)
    │   → POST /api/v1/paperSubmission/resendEmail/{emailId}
    │
    ├─ 查询审核记录: PaperSubmissionAPI.getExpertReviewList(id)
    │   → GET /api/v1/paperSubmission/expertReview/list?id=
    │
    └─ 导出PDF: PaperSubmissionAPI.exportPdf(id)
        → GET /api/v1/paperSubmission/{id}/exportPdf (responseType: blob)
```

---

## 十、常见修改指南

### 10.1 修改无附件邮件内容
修改 `PaperSubmissionServiceImpl.java` 中的 `buildSubmissionInfoTable()` 方法（第493行）。
该方法构建投稿者信息表格HTML，用于替换模板中{{论文稿件}}所在的<a>标签。

### 10.2 修改有附件邮件模板
登录后台管理系统，在"邮箱配置"中修改"专家审核模板"的内容。

### 10.3 修改审核通过/拒绝的邮件模板
登录后台管理系统，修改"论文审核通过模板"或"论文审核未通过模板"。

### 10.4 修改审批链接域名
修改 `application-dev.yml` 或 `application-prod.yml` 中的 `confirm-review` 配置。

### 10.5 修改审核通过率阈值
修改 `PaperSubmissionServiceImpl.java` 第205行的 `0.5`（当前为50%通过率）。

### 10.6 修改抵扣码长度
修改 `PaperSubmissionServiceImpl.java` 第209行的 `8`（当前为8位）。