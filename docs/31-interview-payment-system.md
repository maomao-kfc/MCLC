# MCLC 面试题库：支付与对账系统

> **基于实际代码，严禁编造**。每道题标注考察点和代码出处。

---

## Q1: 为什么用"支付凭证上传+人工审核"而不是支付回调自动确认？

**考察点**: 业务场景分析、技术方案选择

**参考答案**:

**根本原因：协会支付系统无法提供实时回调**。

MCLC 大会的收款方是协会（第三方），不是我们的系统。用户向协会付款后：
1. 协会的支付系统不提供 API 对接或 Webhook 回调
2. 我们无法实时感知用户是否已付款
3. 协会只能定期提供 Excel 对账单

**方案B设计**（PaymentProofServiceImpl.java）：
```
用户注册 → 向协会付款（支付宝/微信/银行转账） → 上传支付凭证截图
  → OCR初筛（自动识别金额/交易号）
  → 管理员人工审核 → 通过 → 确认支付成功
  → 或：上传协会对账Excel → 多维度匹配引擎自动匹配
```

**为什么不只依赖在线支付回调**：
- 系统支持支付宝/微信/Stripe 在线支付（PaymentServiceImpl），但这些是协会的收款渠道
- 国际用户可能使用银行转账等无法自动回调的方式
- 凭证上传是对所有支付方式的统一兜底

**追问方向**: 如果协会未来提供 API，怎么改造？（增加回调接口，自动确认 paid，凭证变为可选）

**代码出处**: PaymentProofServiceImpl.java / PaymentServiceImpl.java

---

## Q2: 对账匹配引擎的评分模型是怎么设计的？为什么阈值是 60？

**考察点**: 算法设计、业务权衡

**参考答案**:

**多维度评分模型**（PaymentReconciliationServiceImpl.java findBestMatch）：

| 维度 | 满分 | 规则 |
|------|------|------|
| 姓名 | 30 | 完全匹配30（含去空格/忽略大小写），包含匹配20 |
| 手机号 | 25 | 归一化后（去空格/横线/加号/括号）完全匹配 |
| 金额 | 25 | BigDecimal 精确比较（清洗逗号→点、去¥/€） |
| 时间 | 20 | ±1天=20，±3天=10（对比声明支付时间）；±7天=10（对比报名时间） |

**阈值 60 的理由**：
- 姓名(30) + 手机号(25) + 时间部分(10) = 65 ≥ 60 → 可自动匹配
- 姓名(30) + 金额(25) + 时间部分(10) = 65 ≥ 60 → 可自动匹配
- 仅姓名(30) = 30 < 60 → 不自动匹配（同名概率高，需人工确认）
- 60 分意味着**至少两个维度匹配**，降低误匹配率

**为什么不用精确匹配**：
- 协会流水的姓名格式可能与注册信息不完全一致（如"张三"vs"张 三"，"Zhang San"vs"zhangsan"）
- 手机号可能带国际区号（+86）或分隔符
- 金额可能有币种符号或千分位

**人工兜底**：score < 60 进入 unmatched 差异列表，管理员可人工确认匹配（confirmMatch → manual_matched）。

**追问方向**: 如果两个注册用户得分相同怎么办？（当前取第一个最高分，可加 tie-breaker 如时间更近者优先）

**代码出处**: PaymentReconciliationServiceImpl.java findBestMatch() / matchName() / matchTime()

---

## Q3: OCR 识别失败怎么办？会不会导致用户无法提交凭证？

**考察点**: 优雅降级、外部依赖处理

**参考答案**:

**不会**。OCR 设计为**可选辅助功能**，三级降级保障：

**第一级：初始化降级**（@PostConstruct）
```java
// 探测tessdata路径，探测不到 → available=false
if (tessdataPath == null) {
    available = false;
    log.warn("未找到tessdata目录，OCR初筛功能不可用");
    return;  // 不阻止Bean创建，不阻止应用启动
}
```

**第二级：运行时降级**
```java
public String recognizeText(...) {
    if (!available) return null;  // 直接跳过，不调用OCR
    ...
}
```

**第三级：异常降级**（catch Throwable）
```java
catch (Throwable e) {
    // 关键：tess4j JNA层抛Error而非Exception
    // catch(Exception)捕获不到，会导致凭证提交失败
    return null;
}
```

**OCR 返回 null 的影响**：
- 凭证正常保存，proof_status=proof_submitted
- 金额/交易号留空，由管理员审核时人工填写
- 不影响后续审核/对账流程

**为什么 catch(Throwable) 而不是 catch(Exception)**：
- Tess4j 通过 JNA 调用 C++ 库，原生层内存访问失败抛 `java.lang.Error: Invalid memory access`
- `Error` 不继承 `Exception`，`catch(Exception)` 捕获不到
- 曾因这个问题导致凭证提交完全失败（案例02）

**追问方向**: 为什么用 PSM 11 而不是 PSM 6？（支付截图多区域布局，PSM 6 漏识别大号金额）

**代码出处**: TesseractOcrServiceImpl.java / 案例02

---

## Q4: 凭证状态流转是怎么设计的？为什么从 review_passed 改为直接 paid？

**考察点**: 状态机设计、业务流程匹配

**参考答案**:

**当前状态流转**：
```
null → proof_submitted(待核验) → paid(审核通过/对账确认)
                             → rejected(驳回)
                             → offline_paid(线下登记)
```

**曾经的设计缺陷**（案例03）：
```
null → proof_submitted → review_passed(审核通过) → paid(对账确认后)
```

问题：审核通过后还需对账确认才变 paid，但协会不一定提供对账 Excel，导致状态"卡住"在 review_passed，用户在"我的注册"页面一直看到"审核中"，第二封邮件也不发送。

**修复**：审核通过直接确认 paid + 发邮件。对账作为**补充确认手段**（处理未上传凭证的用户）。

**三种触发注册成功的方式**（殊途同归）：
1. 凭证审核通过 → proof_status=paid + 发第二封邮件
2. 对账确认 → proof_status=paid + 发第二封邮件
3. 线下登记 → proof_status=offline_paid + 发第二封邮件

**教训**：状态机设计要匹配**实际业务流程**，不要为了理论完整性增加实际不会走的中间状态。

**追问方向**: proof_status 和 payment_status 为什么要分开？（凭证状态和支付状态是两个维度，如线下登记 proof_status=offline_paid 但 payment_status 可能为空）

**代码出处**: PaymentProofServiceImpl.java reviewProof() / 案例03

---

## Q5: 两封邮件机制是怎么设计的？为什么要两封？

**考察点**: 用户体验设计、业务通知策略

**参考答案**:

| 邮件 | 触发 | 模板 | 内容 |
|------|------|------|------|
| 第一封 | 凭证提交成功 | 注册待核对模板(id=7) | "已收到注册信息及支付凭证，待工作人员核对" |
| 第二封 | 审核通过/对账确认/线下登记 | 注册通过模板(id=6) | "大会注册成功"+引导到我的注册页面 |

**为什么两封**：
1. **第一封：即时反馈**。用户提交凭证后最焦虑的是"我提交成功了吗？我的钱会不会白花？"——第一封立即消除焦虑
2. **第二封：最终确认**。审核通过后告知"你已正式注册成功"，并引导到"我的注册"页面报名小会
3. **时间差可能很大**：提交到审核可能几小时到几天，只发一封会让用户在等待期无感知

**技术实现**：
- 均走邮件中心 createAndSend(USER_REGISTER, ...)，9态可追踪
- 均在 try-catch 中，失败不阻断业务
- 模板存储在 sys_email_template 表，通过 getEmailTemplateForm("模板名") 获取，变量 {{name1}} 替换

**追问方向**: 如果用户上传凭证后又被驳回，发什么邮件？（当前驳回不发邮件，用户在"我的注册"页面可见驳回状态和原因——可改进点）

**代码出处**: PaymentProofServiceImpl.java sendProofSubmittedEmail() / sendRegistrationSuccessEmail()

---

## Q6: 水印和去重是怎么实现的？为什么要做？

**考察点**: 安全防刷、文件处理

**参考答案**:

**水印**（watermarked_file_url 字段）：
- 凭证图片添加管理员可见水印（标识"MCLC审核专用"等）
- 防止凭证图片被盗用于其他用途
- 原图和水印版分开存储

**去重**（MD5 + dHash 双重校验）：
- **MD5**：文件内容哈希，完全相同的文件 MD5 相同 → 拦截重复上传
- **dHash**（差异哈希/感知哈希）：图片视觉相似度哈希，轻微修改（裁剪/压缩/加滤镜）后 MD5 变了但 dHash 仍相近 → 拦截"换皮"重复
- 双重校验原因：只靠 MD5 可以通过微小修改绕过，dHash 补充感知层去重

**为什么需要**：
- 防止同一张支付截图被多个账号重复使用（A 付款截图给 B 用）
- 防止用户重复提交相同凭证增加审核负担

**追问方向**: dHash 的原理？（将图片缩放到 9x8，比较相邻像素亮度差异，生成 64 位哈希，汉明距离≤10 视为相似）

**代码出处**: PaymentProofServiceImpl.java（水印+去重逻辑）