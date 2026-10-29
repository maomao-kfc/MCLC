# MCLC 论文投稿子系统技术文档

> 双通道交互(管理端+投稿者端)、专家审稿邮件流程、50%通过率自动判定、抵扣码生成、PDF导出(OpenPDF+JODConverter)
>
> **最后更新**: 2026-10-29

## 更新日志（2026-08-14 后变更）

| 变更 | 说明 | 关联文档 |
|------|------|----------|
| 论文通过邮件未发送修复 | 服务器上论文审核通过后邮件未发送，排查发现发送条件判断问题并修复 | [14-email-subsystem.md](14-email-subsystem.md) |
| 审批502修复 | 论文审批接口返回502，修复后端异常 | - |
| 审批拦截BUG修复 | 审批流程中的拦截逻辑BUG修复 | - |
| 论文投稿测试数据 | 补充论文投稿功能的测试数据 | - |

## 1. 架构总览

论文投稿子系统是 MCLC 大会学术核心模块，支持 **双通道交互**：

```
管理端 (Admin)                    投稿者端 (Public)
  │                                  │
  ├─ 分页查询投稿列表                  ├─ 邮箱验证码查询
  ├─ 新增/修改/删除投稿                ├─ 查看投稿详情
  ├─ 发送审稿邮件 ──────→ 专家审核      ├─ 修改投稿信息(仅待审核)
  ├─ 查看审核记录                     └─ 无需登录，邮箱鉴权
  ├─ 重发审稿邮件
  └─ 导出PDF
```

### 核心业务流程

```
投稿提交 → 待审核(1) → 发送审稿邮件 → 专家审核 → 通过率判定
                                                    ├─ ≥50%通过 → 审核通过(2) + 生成抵扣码
                                                    └─ <50%通过 → 审核拒绝(3)
```

### 关键文件索引

#### 核心业务层

| 文件 | 行数 | 职责 |
|------|------|------|
| `portal/controller/PaperSubmissionController.java` | 397 | 14个API端点(10管理端+4投稿者公开)，PDF导出3策略路由 |
| `portal/service/PaperSubmissionService.java` | 55 | 接口层：11个方法签名(7管理端+4公开) |
| `portal/service/impl/PaperSubmissionServiceImpl.java` | 797 | 核心实现：审稿邮件发送+专家审核+抵扣码生成+投稿者鉴权查询 |
| `portal/mapper/PaperSubmissionMapper.java` | 24 | Mapper：1个自定义SQL分页查询selectPaperSubmissionPage |

#### PDF导出层

| 文件 | 行数 | 职责 |
|------|------|------|
| `portal/service/PaperPdfGeneratorService.java` | 30 | 接口：generateCoverPage + generatePdf |
| `portal/service/impl/PaperPdfGeneratorServiceImpl.java` | 257 | OpenPDF实现：封面页/信息页生成+PDF合并+中文字体9路径加载 |
| `portal/service/DocxToPdfService.java` | 21 | 接口：convertDocxToPdf |
| `portal/service/impl/DocxToPdfServiceImpl.java` | 159 | LibreOffice命令行实现：@ConditionalOnProperty条件装配 |

#### 数据模型层

| 文件 | 行数 | 职责 |
|------|------|------|
| `portal/model/entity/PaperSubmission.java` | 110 | 主表实体：14字段，surname为非持久化字段(@TableField(exist=false)) |
| `portal/model/entity/PaperSubmissionEmail.java` | 87 | 邮件记录实体：10字段，sendErrorMsg使用ALWAYS策略 |
| `portal/model/form/PaperSubmissionForm.java` | 9 | 新增/修改表单 |
| `portal/model/form/PaperSubmissionReviewForm.java` | 37 | 审核表单：ExpertEmailItem内部类(name+email) |
| `portal/model/form/PaperSubmissionUpdateForm.java` | 48 | 投稿者修改表单：6个可修改字段+id+contactEmail(身份验证) |
| `portal/model/query/PaperSubmissionQuery.java` | 20+ | 查询参数(继承BaseQuery) |
| `portal/model/vo/PaperSubmissionPageVO.java` | 8+ | 管理端分页VO |
| `portal/model/vo/PaperSubmissionQueryVO.java` | 50 | 投稿者脱敏VO：隐藏emailStatus/deductionStatus/discountCode |
| `portal/model/vo/EmailSendResultVO.java` | - | 邮件发送结果VO(expertName+expertEmail+success) |
| `portal/converter/PaperSubmissionConverter.java` | - | Form↔Entity↔VO转换器 |

## 2. 数据模型

### 2.1 paper_submission (论文投稿主表)

| 字段 | 类型 | 说明 |
|------|------|------|
| id | Long | 主键(AUTO) |
| name | String | 投稿人姓名 |
| surname | String | 姓(非数据库字段，@TableField(exist=false)) |
| paper_title | String | 论文标题 |
| first_author | String | 第一作者 |
| contact_number | String | 联系电话 |
| contact_email | String | 联系邮箱 |
| abstract | String | 论文摘要(@TableField("abstract")) |
| keywords | String | 关键词 |
| appendix | String | 论文附件URL |
| contribution_id | Long | 投稿ID |
| paper_status | Integer | 审核状态(1=待审核, 2=审核通过, 3=审核拒绝) |
| reject_reason | String | 拒绝原因 |
| expert_url | String | 专家评审链接 |
| email_status | Integer | 邮件发送状态(0=未发送, 1=发送失败, 2=已发送) |
| deduction_status | Integer | 抵扣状态 |
| discount_code | String | 抵扣码(审核通过时生成8位随机码) |

### 2.2 paper_submission_email (审稿邮件记录表)

| 字段 | 类型 | 说明 |
|------|------|------|
| id | Long | 主键(AUTO) |
| submission_id | Long | 外键→paper_submission.id |
| expert_name | String | 专家姓名 |
| expert_email | String | 专家邮箱 |
| url | String | 投稿件链接 |
| sender_name | String | 发送人姓名 |
| send_status | Integer | 发送状态(1=发送中, 2=发送成功, 3=发送失败) |
| send_error_msg | String | 发送失败错误信息(FieldStrategy.ALWAYS) |
| send_time | LocalDateTime | 发送时间 |
| review_result | Integer | 审核结果(0=未审核, 1=已通过, 2=未通过) |
| reject_reason | String | 拒绝原因 |
| review_time | LocalDateTime | 审核时间 |

### 2.3 ER关系

```
paper_submission (1) ──→ (N) paper_submission_email
                     ↑
                     └─── 专家审核链接: {confirmReview}{emailRecord.id}
```

## 3. 状态机

### 3.1 paper_status (审核状态)

```
  [新增] ──→ 待审核(1) ──→ 发送审稿邮件 ──→ 专家审核中
                                              │
                              ┌───────────────┤
                              │               │
                         通过率≥50%      通过率<50%
                              │               │
                              ▼               ▼
                        审核通过(2)      审核拒绝(3)
                     + 生成8位抵扣码    + 记录拒绝原因
                     + 通知投稿人通过    + 通知投稿人未通过
```

### 3.2 email_status (邮件发送状态)

```
未发送(0) ──→ 发送审稿邮件 ──→ 全部成功(2) / 部分失败(1)
                                  │
                            重发失败邮件 ──→ 更新状态
```

### 3.3 send_status (单条邮件发送状态)

```
发送中(1) ──→ 发送成功(2) / 发送失败(3, 记录send_error_msg)
                              │
                        重发 ──→ 发送中(1) ──→ 成功(2, 清除error) / 失败(3)
```

### 3.4 review_result (专家审核结果)

```
未审核(0) ──→ 已通过(1) / 未通过(2, 记录reject_reason)
                  │
            不允许重复审核(校验reviewResult!=0)
```

## 4. API接口

**Controller**: `PaperSubmissionController`
**基础路径**: `/api/v1/paperSubmission`

### 4.1 管理端接口(需认证)

| 方法 | 路径 | 说明 | 权限 |
|------|------|------|------|
| GET | / | 分页查询投稿列表 | 登录 |
| POST | / | 新增投稿 | 登录 |
| PUT | /{id} | 修改投稿 | sys:paperSubmission:update |
| DELETE | /{ids} | 删除投稿(批量) | sys:paperSubmission:delete |
| PUT | /{id}/review | 发送审稿邮件 | 登录 |
| POST/PUT | /expertReview | 专家审核 | 公开(邮件链接) |
| GET | /expertReview/list | 查询专家审核记录 | 登录 |
| POST | /resendEmail/{emailId} | 重发审稿邮件 | 登录 |
| GET | /emailRecord/{emailId} | 查询邮件记录详情 | 登录 |
| GET | /{id}/exportPdf | 导出论文PDF | 登录 |

### 4.2 投稿者公开接口(无需登录，邮箱验证码鉴权)

| 方法 | 路径 | 说明 | 鉴权方式 |
|------|------|------|----------|
| POST | /query/sendCode | 发送查询验证码 | 邮箱必须有投稿记录 |
| GET | /query/byEmail | 按邮箱查询投稿列表 | 验证码(Redis 5分钟) |
| GET | /query/detail | 查询投稿详情 | 邮箱归属验证 |
| PUT | /query/update | 投稿者修改投稿信息 | 邮箱归属+仅待审核状态 |

## 5. PaperSubmissionServiceImpl 方法级分解

> 源码位置：`portal/service/impl/PaperSubmissionServiceImpl.java`（797行）

### 5.1 方法总览

| # | 方法 | 行号 | 事务 | 调用者 | 核心逻辑 |
|---|------|------|------|--------|----------|
| 1 | getPaperSubmissionPage | 101 | - | Controller | Mapper自定义SQL分页查询 |
| 2 | savePaperSubmission | 119 | ✅ | Controller | Form→Entity→save |
| 3 | deletePaperSubmissions | 132 | - | Controller | 逗号分隔ID→逐个校验→逐个删除 |
| 4 | getPaperSubmissionList | 152 | - | Controller | LambdaQueryWrapper全量查询(无筛选) |
| 5 | updatePaperSubmissionStatus | 182 | - | Controller | **核心**：发送审稿邮件(5步流程) |
| 6 | expertReview | 312 | - | Controller(公开) | **核心**：专家审核+通过率判定+抵扣码生成+通知 |
| 7 | getExpertReviewList | 426 | - | Controller | 按submissionId查询所有邮件记录 |
| 8 | getEmailRecord | 440 | - | Controller | 按emailId查询单条邮件记录 |
| 9 | resendReviewEmail | 467 | - | Controller | 重发审稿邮件(不创建新记录) |
| 10 | sendQueryCode | 635 | - | Controller(公开) | 6位验证码+Redis 5分钟+邮箱校验 |
| 11 | queryByEmail | 673 | - | Controller(公开) | 验证码校验+一次性使用+LOWER匹配+脱敏VO |
| 12 | getDetailByEmail | 717 | - | Controller(公开) | 邮箱归属验证 |
| 13 | updateBySubmitter | 761 | ✅ | Controller(公开) | 邮箱归属+仅待审核可修改+6字段更新 |

**私有方法**：

| 方法 | 行号 | 用途 |
|------|------|------|
| buildSubmissionInfoTable | 558 | 构建投稿者信息表格HTML(5行2列) |
| buildTableRow | 581 | 构建单行HTML(标签灰色背景+值) |
| sendAdminErrorNotification | 598 | 异步发送错误通知邮件给ADMIN_EMAIL |

### 5.2 依赖注入清单

| 字段 | 类型 | 用途 |
|------|------|------|
| paperSubmissionConverter | PaperSubmissionConverter | Form↔Entity↔VO转换 |
| mailService | MailService | 同步发送HTML邮件(管理员通知) |
| paperSubmissionEmailMapper | PaperSubmissionEmailMapper | 操作paper_submission_email表 |
| emailTemplateService | EmailTemplateService | 获取邮件模板内容 |
| mailSendRecordService | MailSendRecordService | 任务表模式发送邮件(先落库再发送) |
| redisTemplate | RedisTemplate | 验证码存取 |
| ADMIN_EMAIL | String(常量) | `maojiangjun@foxmail.com` |
| confirmReview | @Value("${confirm-review}") | 专家审批链接前缀 |

### 5.3 核心业务流程

#### 5.3.1 发送审稿邮件 (updatePaperSubmissionStatus, 行182-290)

**源码级流程**（行182-290）：

```
步骤1(行184-187): 校验投稿存在 → getById(id)，不存在抛BusinessException
步骤2(行190-196): 防重复发送 → 查paper_submission_email记录数>0则抛异常
  ⚠️ 提示用户使用"重发邮件"功能而非重复发送
步骤3(行199-201): 更新投稿 → setExpertUrl(url) + setEmailStatus(0)
步骤4(行206-275): 遍历专家列表发送邮件
  步骤4a(行220-228): 创建emailRecord → submissionId+expertName+expertEmail+url+senderName+sendStatus=1
  步骤4b(行231-250): 构建邮件内容(双通道)
    有附件(url非空): 模板.replace("{{name1}}", expertName)
                          .replace("{{name2}}", senderName)
                          .replace("{{论文稿件}}", url)
                          .replace("{{审批论文稿件}}", confirmReview+emailRecord.id)
    无附件(url为空):  buildSubmissionInfoTable()构建5行2列HTML表格
                     正则替换<a>标签: <a[^>]*\{\{论文稿件\}\}[^>]*>[^<]*</a>
                     兜底直接替换: {{论文稿件}} → submissionInfoHtml
  步骤4c(行257-260): mailSendRecordService.createAndSend(PAPER_SUBMIT, id, ...)
  步骤4d(行263-274): 更新emailRecord
    成功: sendStatus=2
    失败: sendStatus=3 + sendErrorMsg + sendAdminErrorNotification()
步骤5(行277-286): 更新投稿emailStatus
  allSuccess → emailStatus=2(全部发送成功)
  anyFailed  → emailStatus=1(部分或全部发送失败)
```

**审批链接格式**: `{confirmReview配置}{emailRecord.id}`
- 例: `https://myopiacontactlens.com/review-file?id=123`
- 专家点击后进入前端审核页面，调用 `/expertReview` 接口

**邮件内容双通道逻辑详解**：

| 条件 | {{论文稿件}}替换方式 | {{审批论文稿件}}替换方式 |
|------|---------------------|------------------------|
| 有附件(url非空) | 直接替换为附件URL | confirmReview+emailRecord.id |
| 无附件(url为空) | 正则替换`<a>`标签→投稿者信息表格HTML | confirmReview+emailRecord.id |

**投稿者信息表格**（buildSubmissionInfoTable, 行558-568）：
```html
<table style="border-collapse:collapse;width:100%;margin:10px 0;">
  <tr><td>投稿人姓名</td><td>{name}</td></tr>
  <tr><td>论文标题</td><td>{paperTitle}</td></tr>
  <tr><td>论文摘要</td><td>{paperAbstract}</td></tr>
  <tr><td>联系电话</td><td>{contactNumber}</td></tr>
  <tr><td>联系邮箱</td><td>{contactEmail}</td></tr>
</table>
```

#### 5.3.2 专家审核 (expertReview, 行312-417)

**源码级流程**：

```
步骤1(行314-317): 校验邮件记录 → selectById(id)，不存在抛BusinessException
步骤2(行320-323): 防重复审核 → reviewResult!=0时抛异常(含当前审核结果)
步骤3(行326-330): 更新审核结果 → setReviewResult+setRejectReason+setReviewTime
步骤4(行333-339): 检查全部审核完毕 → allRecords.stream().allMatch(r->r.getReviewResult()!=0)
步骤5(行342-414): 全部审核完毕 → 计算结果并通知
  步骤5a(行344-347): 计算通过数 → passedCount = allRecords.filter(r->reviewResult==1).count()
  步骤5b(行350-363): 通过率判定
    ≥50%通过:
      paperStatus=2(审核通过)
      生成8位随机抵扣码(行353-358):
        chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789"
        Random.nextInt(chars.length()) × 8次 → StringBuilder
        setDiscountCode(code.toString())
    <50%通过:
      paperStatus=3(审核拒绝)
  步骤5c(行364): updateById(paperSubmission)
  步骤5d(行367-413): 通知投稿人审核结果
    前置: contactEmail非空 + paperStatus!=1(非待审核)
    通过: 模板="论文审核通过模板", businessType=PAPER_PASS
    拒绝: 模板="论文审核未通过模板", businessType=PAPER_REJECT
    占位符: {{name1}}=投稿人名, {{name2}}=论文标题, {{code}}=抵扣码(拒绝时为空)
    发送: mailSendRecordService.createAndSend()
    失败: 异步通知管理员(sendAdminErrorNotification)
    异常: catch不影响审核结果，仅记录日志+通知管理员
```

**抵扣码生成逻辑详解**（行353-358）：

```java
String chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";  // 36个字符
Random random = new Random();
StringBuilder code = new StringBuilder(8);
for (int j = 0; j < 8; j++) {
    code.append(chars.charAt(random.nextInt(chars.length())));
}
paperSubmission.setDiscountCode(code.toString());
```

- **字符集**: A-Z(26) + 0-9(10) = 36个字符
- **长度**: 8位
- **组合数**: 36^8 ≈ 2.8万亿
- **碰撞概率**: 极低，但未做唯一性校验 ⚠️
- **生成时机**: 仅在审核通过(paperStatus=2)时生成
- **存储位置**: paper_submission.discount_code字段
- **与注册折扣关联**: 抵扣码在注册流程中使用，见15-registration-workflow.md

#### 5.3.3 重发审稿邮件 (resendReviewEmail, 行467-547)

**源码级流程**（行467-547）：

```
步骤1(行469-472): 校验邮件记录 → selectById(emailId)，不存在抛BusinessException
步骤2(行475-478): 获取关联投稿 → getById(emailRecord.getSubmissionId())
步骤3(行481-504): 构建邮件内容(与首次发送逻辑完全相同)
  有附件: 模板替换4个占位符
  无附件: buildSubmissionInfoTable()+正则替换<a>标签
步骤4(行507-509): mailSendRecordService.createAndSend(PAPER_SUBMIT, ...)
步骤5(行512-525): 更新emailRecord
  成功: sendStatus=2 + sendTime=now + sendErrorMsg=null(清除之前错误)
  失败: sendStatus=3 + sendTime=now + sendErrorMsg + sendAdminErrorNotification()
步骤6(行529-540): 同步更新投稿emailStatus
  查询该投稿所有邮件记录 → allSuccess=2 / anyFailed=1
```

**与首次发送的区别**：

| 对比项 | 首次发送(updatePaperSubmissionStatus) | 重发(resendReviewEmail) |
|--------|--------------------------------------|------------------------|
| 邮件记录 | 创建新记录 | 不创建，只更新已有记录 |
| 重复检查 | 检查已有记录数>0 | 不检查 |
| 状态更新 | emailStatus从0开始 | 取所有邮件最差状态 |
| 错误清除 | 无(首次) | 重发成功清除sendErrorMsg |
| 返回值 | List\<EmailSendResultVO\> | 单个EmailSendResultVO |

#### 5.3.4 投稿者查询鉴权流程（4个公开接口）

**sendQueryCode(email, 行635-658)**：
```
1. 邮箱trim+toLowerCase
2. 校验邮箱有投稿记录: LOWER(contact_email) = email (大小写不敏感)
3. 生成6位验证码: (int)((Math.random()*9+1)*100000) → 100000~999999
4. 发送验证码邮件: mailService.sendMail(纯文本)
5. 存入Redis: key=captcha:paper_query:{email}, TTL=5分钟
```

**queryByEmail(email, code, 行673-707)**：
```
1. 邮箱trim+toLowerCase
2. 校验Redis验证码: get(captcha:paper_query:{email})
   - 不匹配或过期 → 抛BusinessException("验证码错误或已过期")
3. 验证通过后删除验证码(一次性使用): delete(key)
4. 查询: LOWER(contact_email) = email, ORDER BY id DESC
5. 转换为脱敏VO: PaperSubmissionQueryVO
   - 包含: id, name, paperTitle, firstAuthor, contactNumber, contactEmail,
           paperAbstract, keywords, appendix, paperStatus, rejectReason
   - 隐藏: emailStatus, deductionStatus, discountCode
```

**getDetailByEmail(id, email, 行717-743)**：
```
1. 邮箱trim+toLowerCase
2. 校验投稿存在: getById(id)
3. 验证邮箱归属: emailTrimmed.equals(entity.getContactEmail().trim().toLowerCase())
   - 不匹配 → 抛BusinessException("无权查看此投稿记录")
4. 转换为脱敏VO(同queryByEmail)
```

**updateBySubmitter(updateForm, 行761-795)**：
```
1. 邮箱trim+toLowerCase
2. 校验投稿存在: getById(updateForm.getId())
3. 验证邮箱归属(同getDetailByEmail)
4. 校验投稿状态: 仅paperStatus=1(待审核)可修改
   - paperStatus=2 → "审核通过，不可修改"
   - paperStatus=3 → "审核拒绝，不可修改"
5. 更新6个允许修改的字段:
   ✅ paperTitle, firstAuthor, contactNumber, paperAbstract, keywords, appendix
   ❌ name, surname, contactEmail(身份标识，不可修改)
```

## 6. PDF导出

### 6.1 三种导出策略（Controller层路由，行232-326）

Controller层 `exportPdf()` 根据附件URL后缀选择策略：

| 附件类型 | 判断条件 | 策略 | 实现方式 |
|----------|----------|------|----------|
| PDF附件 | `lowerUrl.endsWith(".pdf")` | 封面页+附件PDF合并 | OpenPDF封面→URL读取→PdfCopy合并 |
| DOCX/DOC附件 | `endsWith(".docx")‖endsWith(".doc")` | 封面页+JODConverter转PDF合并 | OpenPDF封面→DocxToPdfService→PdfCopy合并 |
| 无附件/其他 | else | OpenPDF完整信息PDF | 直接生成标题+作者+摘要+关键词 |

**Controller层关键代码**（行256-313）：
```java
String lowerUrl = (appendix != null) ? appendix.toLowerCase() : "";
if (lowerUrl.endsWith(".pdf")) {
    // 情况1: 封面页 + URL读取附件PDF → mergePdfs()
} else if (lowerUrl.endsWith(".docx") || lowerUrl.endsWith(".doc")) {
    if (docxToPdfService == null) {
        // LibreOffice未安装 → 回退到OpenPDF完整信息PDF
    } else {
        try {
            // 封面页 + DocxToPdfService转换 → mergePdfs()
        } catch (Exception e) {
            // 转换失败 → response.reset() → 回退到OpenPDF完整信息PDF
        }
    }
} else {
    // 情况3: 无附件 → OpenPDF完整信息PDF
}
```

**DocxToPdfService注入方式**：`@Autowired(required = false)` — LibreOffice未安装时为null

### 6.2 回退机制详解

```
DOCX转PDF请求:
  ├─ docxToPdfService == null (LibreOffice未安装)
  │     └─ 直接回退: paperPdfGeneratorService.generatePdf() (OpenPDF完整信息)
  └─ docxToPdfService != null (LibreOffice已安装)
        ├─ convertDocxToPdf() 成功
        │     └─ 封面页 + 转换后PDF → mergePdfs()
        └─ convertDocxToPdf() 失败
              ├─ response.reset() (清除已写入的响应头)
              ├─ 重新设置Content-Type和Content-Disposition
              └─ paperPdfGeneratorService.generatePdf() (OpenPDF完整信息)
```

**关键细节**：DOCX转PDF失败时需要`response.reset()`清除已写入的响应头，再重新设置PDF响应头。

### 6.3 PaperPdfGeneratorServiceImpl 源码分析（257行）

#### 6.3.1 两种生成模式

| 方法 | 行号 | 用途 | 差异 |
|------|------|------|------|
| generateCoverPage(paper, outputStream) | 83 | 有附件时的封面页 | isCoverPage=true，顶部3行空行 |
| generatePdf(paper, outputStream) | 93 | 无附件时的完整信息页 | isCoverPage=false |

两者共用 `generateInfoPdf(paper, outputStream, isCoverPage)` 私有方法（行105-168）。

#### 6.3.2 PDF内容结构

**封面页** (isCoverPage=true)：
```
[3行空行(Chunk.NEWLINE)]
论文标题 (18pt 加粗 居中, spacingAfter=20)
┌──────────┬──────────────────────┐  ← 80%宽度, 居中, 列宽1:3
│ 论文标题  │ {paperTitle}         │  ← 标签灰色背景(#f5f5f5), 加粗, 居中
│ 姓名     │ {name}               │
│ 联系电话  │ {contactNumber}      │
│ 论文摘要  │ {paperAbstract}      │
└──────────┴──────────────────────┘
Generated: yyyy-MM-dd HH:mm:ss  ← 8pt 灰色, 居中
```

**完整信息页** (isCoverPage=false)：同上，但无顶部3行空行。

#### 6.3.3 中文字体加载（9+7路径回退机制）

**方式1：字体文件路径**（行50-65, 9个路径按优先级尝试）：

| # | 路径 | 平台 |
|---|------|------|
| 1 | `C:/Windows/Fonts/simsun.ttc,0` | Windows 宋体 |
| 2 | `C:/Windows/Fonts/msyh.ttc,0` | Windows 微软雅黑 |
| 3 | `/usr/share/fonts/truetype/wqy/wqy-zenhei.ttc,0` | Linux 文泉驿 |
| 4 | `/usr/share/fonts/opentype/noto/NotoSansCJK-Regular.ttc,0` | Linux Noto CJK |
| 5 | `/usr/share/fonts/opentype/noto/NotoSansCJKsc-Regular.otf` | Linux Noto SC |
| 6 | `/usr/share/fonts/truetype/noto/NotoSansCJK-Regular.ttc,0` | Linux Noto CJK |
| 7 | `/usr/share/fonts/truetype/noto/NotoSansCJKsc-Regular.otf` | Linux Noto SC |
| 8 | `/usr/share/fonts/noto-cjk/NotoSansCJK-Regular.ttc,0` | Linux Noto CJK |
| 9 | `/usr/share/fonts/opentype/noto/NotoSansSC-Regular.otf` | Linux Noto SC |

**方式2：字体名称**（行68-76, 7个名称按优先级尝试）：
SimSun → WenQuanYi Zen Hei → Noto Sans CJK SC → NotoSansCJKsc-Regular → Noto Sans SC → STSong → AR PL UMing CN

**方式3：最终回退**（行252-254）：
`BaseFont.HELVETICA` + `BaseFont.WINANSI` — **中文将无法显示**，仅输出诊断日志

**加载方法** `getChineseBaseFont()`（行221-256）：
```java
for (String fontPath : CHINESE_FONT_PATHS) {  // 方式1: 文件路径
    BaseFont bf = BaseFont.createFont(fontPath, IDENTITY_H, EMBEDDED);
    return bf;  // 第一个成功的即返回
}
for (String fontName : CHINESE_FONT_NAMES) {  // 方式2: 字体名称
    BaseFont bf = BaseFont.createFont(fontName, IDENTITY_H, EMBEDDED);
    return bf;
}
return BaseFont.createFont(HELVETICA, WINANSI, EMBEDDED);  // 方式3: 回退
```

#### 6.3.4 PDF合并（mergePdfs, 行193-215）

```java
public static void mergePdfs(byte[] coverPdfBytes, byte[] attachmentPdfBytes, OutputStream outputStream)
```

- 使用 `PdfCopy` 逐页合并
- 封面页在前，附件内容在后
- 合并后自动关闭 PdfReader

### 6.4 DocxToPdfServiceImpl 源码分析（159行）

#### 6.4.1 条件装配

```java
@ConditionalOnProperty(name = "jodconverter.local.enabled", havingValue = "true")
```
- 配置 `jodconverter.local.enabled=true` 时才创建Bean
- 未配置或为false时，Controller中 `@Autowired(required=false)` 注入为null

#### 6.4.2 转换流程（行49-118）

```
1. 创建临时目录: java.io.tmpdir/jodconverter/
2. 下载DOCX到临时文件: source_{timestamp}.docx
   - URI.create(docxUrl).toURL().openStream() → Files.copy()
3. 构建LibreOffice命令行:
   soffice --headless --convert-to pdf --outdir {tempDir} {tempDocx}
4. 执行转换: ProcessBuilder.start() → waitFor()
   - exitCode != 0 → 抛RuntimeException
5. 查找PDF: 源文件名.docx → 源文件名.pdf
   - 不存在 → 抛RuntimeException
6. 写入输出流: Files.copy(tempPdf, outputStream)
7. 清理临时文件: finally块中删除tempDocx+tempPdf
```

#### 6.4.3 soffice路径查找（行129-158）

| 优先级 | 路径 | 条件 |
|--------|------|------|
| 1 | `{office-home}/program/soffice.exe` | Windows + office-home配置 |
| 2 | `{office-home}/program/soffice` | Linux + office-home配置 |
| 3 | `/usr/bin/soffice` | Debian/Ubuntu包管理器符号链接 |
| 4 | `soffice` | 系统PATH(最后回退) |

**配置项**: `jodconverter.local.office-home` — LibreOffice安装目录

## 7. 邮件模板

### 7.1 使用的邮件模板

| 模板名称 | 用途 | 占位符 |
|----------|------|--------|
| 专家审核模板 | 发送审稿邮件 | {{name1}}(专家名), {{name2}}(发送人名), {{论文稿件}}(附件URL/信息表格), {{审批论文稿件}}(审批链接) |
| 论文审核通过模板 | 通知投稿人通过 | {{name1}}(投稿人名), {{name2}}(论文标题), {{code}}(抵扣码) |
| 论文审核未通过模板 | 通知投稿人未通过 | {{name1}}(投稿人名), {{name2}}(论文标题), {{code}}(空) |

### 7.2 邮件业务类型

| MailBusinessType | 用途 |
|------------------|------|
| PAPER_SUBMIT | 审稿邮件发送 |
| PAPER_PASS | 审核通过通知 |
| PAPER_REJECT | 审核拒绝通知 |

## 8. Redis缓存

| Key模式 | 用途 | TTL |
|---------|------|-----|
| `paper:query:code:{email}` | 投稿查询验证码 | 5分钟 |

## 9. 配置项

| 配置 | 说明 | 示例值 |
|------|------|--------|
| `confirm-review` | 专家审批链接前缀 | `https://myopiacontactlens.com/review-file?id=` |
| ADMIN_EMAIL | 管理员邮箱(硬编码) | `maojiangjun@foxmail.com` |

## 10. 开发指南

### 10.1 新增邮件模板

1. 在系统管理-邮件模板中添加模板
2. 在 `PaperSubmissionServiceImpl` 中通过 `emailTemplateService.getEmailTemplateForm("模板名")` 获取
3. 确保模板占位符与代码中的 `replace()` 调用一致

### 10.2 修改审核通过阈值

当前阈值为50%(`passedCount * 1.0 / total >= 0.5`)，修改位置:
- `PaperSubmissionServiceImpl.expertReview()` 第350行

### 10.3 修改抵扣码规则

当前为8位大写字母+数字随机码，修改位置:
- `PaperSubmissionServiceImpl.expertReview()` 第353-358行

### 10.4 注意事项

- **防重复发送**: 首次发送审稿邮件时检查 `paper_submission_email` 是否已有记录
- **防重复审核**: `reviewResult != 0` 时抛异常
- **邮箱大小写不敏感**: 查询时使用 `LOWER(contact_email)` 匹配
- **验证码一次性使用**: 验证通过后立即从Redis删除
- **sendErrorMsg字段**: 使用 `FieldStrategy.ALWAYS` 确保null值也能写入数据库(重发成功时清除错误信息)
- **DOCX转PDF**: `DocxToPdfService` 使用 `@Autowired(required=false)` 注入，LibreOffice未安装时为null
- **PDF中文字体**: Docker部署需安装 `fonts-wqy-zenhei` 和 `fonts-noto-cjk`

## 11. 已知问题与风险

### 11.1 代码风险

| 编号 | 严重度 | 描述 | 位置 | 建议 |
|------|--------|------|------|------|
| CODE-004 | P2 | paper_submission表缺少create_time/update_time审计字段 | PaperSubmission.java | 添加@TableField(fill=FieldFill.INSERT)审计字段 |
| CODE-012 | P2 | 抵扣码生成未做唯一性校验，理论上可能碰撞 | PaperSubmissionServiceImpl.java:353-358 | 添加数据库唯一索引或生成后校验 |
| CODE-013 | P3 | getPaperSubmissionList无筛选条件，全量查询可能OOM | PaperSubmissionServiceImpl.java:152-158 | 添加分页或限制条件 |
| CODE-014 | P3 | deletePaperSubmissions逐个删除而非批量，大量删除性能差 | PaperSubmissionServiceImpl.java:132-143 | 改用removeByIds批量删除 |
| CODE-015 | P3 | 验证码生成使用Math.random()而非SecureRandom | PaperSubmissionServiceImpl.java:647 | 改用SecureRandom提升安全性 |
| CODE-016 | P3 | ADMIN_EMAIL硬编码在代码中 | PaperSubmissionServiceImpl.java:82 | 移至配置文件 |

### 11.2 安全风险

| 编号 | 严重度 | 描述 | 位置 | 建议 |
|------|--------|------|------|------|
| SEC-012 | P2 | 专家审核接口(/expertReview)无认证，任何人都可调用 | PaperSubmissionController.java:164 | 添加token验证或CAPTCHA |
| SEC-013 | P3 | 投稿者修改接口(/query/update)仅靠邮箱验证身份 | PaperSubmissionServiceImpl.java:761 | 考虑增加验证码校验 |

### 11.3 PDF导出风险

| 编号 | 严重度 | 描述 | 位置 | 建议 |
|------|--------|------|------|------|
| PDF-001 | P2 | 中文字体最终回退HELVETICA，中文将显示为方框 | PaperPdfGeneratorServiceImpl.java:252 | 部署前确认字体安装 |
| PDF-002 | P3 | DOCX转PDF使用ProcessBuilder执行soffice命令，存在命令注入风险 | DocxToPdfServiceImpl.java:70 | 输入URL已通过URI.create()限制，风险较低 |
| PDF-003 | P3 | 临时文件清理在finally块中，但进程异常终止时可能残留 | DocxToPdfServiceImpl.java:111-116 | 考虑添加定时清理机制 |

## 12. 常见问题排查

### 12.1 审稿邮件发送失败

```
1. 检查mail_send_record表：send_status和error_message
2. 检查paper_submission_email表：send_status(1=发送中/2=成功/3=失败)
3. 检查邮件模板：email_template表中是否存在"专家审核模板"
4. 检查占位符：模板中必须有{{name1}}, {{name2}}, {{论文稿件}}, {{审批论文稿件}}
5. 检查confirm-review配置：application-dev.yml中的审批链接前缀
6. 检查管理员通知：ADMIN_EMAIL是否收到错误通知邮件
```

### 12.2 专家审核不生效

```
1. 检查paper_submission_email.review_result：0=未审核, 1=已通过, 2=未通过
2. 确认所有专家都已审核：allRecords.stream().allMatch(r->reviewResult!=0)
3. 检查通过率：passedCount/totalCount是否≥50%
4. 检查paper_submission.paper_status：1=待审核, 2=通过, 3=拒绝
5. 检查discount_code：审核通过时应自动生成8位码
```

### 12.3 PDF导出中文乱码

```
1. 检查服务器字体：fc-list :lang=zh
2. 检查字体文件：ls -la /usr/share/fonts/truetype/wqy/
3. 安装字体包：apt install fonts-wqy-zenhei fonts-noto-cjk
4. 检查日志：搜索"成功加载中文字体"或"未找到任何中文字体"
5. Docker部署：Dockerfile中添加RUN apt-get install -y fonts-wqy-zenhei
```

### 12.4 DOCX转PDF失败

```
1. 检查LibreOffice安装：soffice --version
2. 检查配置：jodconverter.local.enabled=true, jodconverter.local.office-home
3. 检查Bean注入：docxToPdfService是否为null(日志搜索"LibreOffice未安装")
4. 检查临时目录：java.io.tmpdir/jodconverter/是否有写权限
5. 检查DOCX文件URL：URI.create(url)是否可访问
6. 检查回退机制：失败时应自动回退到OpenPDF完整信息PDF
```

### 12.5 投稿者查询验证码问题

```
1. 检查Redis：key=captcha:paper_query:{email}, TTL=5分钟
2. 检查邮箱匹配：LOWER(contact_email) = LOWER(input_email)
3. 检查验证码一次性使用：验证通过后立即delete(key)
4. 检查验证码生成：(int)((Math.random()*9+1)*100000) → 100000~999999
```

### 12.6 抵扣码问题

```
1. 抵扣码仅在审核通过(paperStatus=2)时生成
2. 检查paper_submission.discount_code字段
3. 抵扣码为8位大写字母+数字随机码(A-Z+0-9)
4. 当前未做唯一性校验(CODE-012)
5. 抵扣码在注册流程中使用，见15-registration-workflow.md
```

## 13. 2026-08-14 后修复记录

### 13.1 论文通过邮件服务器未发送【线上故障】

**现象**：本地环境论文审核通过后邮件正常发送，部署到服务器后邮件未发送。

**排查过程**：
1. 检查邮件中心记录 → 无 PAPER_PASS 类型记录
2. 检查发送条件判断逻辑 → 发现条件判断在特定数据状态下不满足
3. 修复发送条件 → 重新打包部署 → 验证通过

**经验**：本地测试数据与线上数据状态差异可能导致条件分支不同，需覆盖各种数据状态测试。

### 13.2 审批502修复

**现象**：论文审批接口返回502 Bad Gateway。

**原因**：后端异常导致请求处理失败。

**修复**：修复后端异常逻辑，重新编译打包部署。

### 13.3 审批拦截BUG

**现象**：审批流程中被错误拦截，无法正常完成审批。

**修复**：修正拦截逻辑的条件判断。