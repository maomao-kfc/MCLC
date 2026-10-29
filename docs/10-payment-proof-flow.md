# 支付凭证全链路（方案B）

> **状态**: 【已实现】【已验证】  
> **核心文件**: PaymentProofServiceImpl.java (25.7KB) / TesseractOcrServiceImpl.java (4.1KB)  
> **数据库表**: payment_proof / register_user_information  
> **最后验证**: 2026-10-29 本地环境

---

## 1. 业务背景

MCLC 大会注册需要支付注册费。由于协会支付系统无法提供实时回调确认，采用**方案B**：用户上传支付凭证截图 → 系统OCR初筛 → 管理员人工审核 → 确认支付成功。

## 2. 全链路流程

```
用户注册完成(9步) → 确认页Confirm.vue
  │
  ├─ 在线支付(支付宝/微信/Stripe) → 回调确认
  │
  └─ 上传支付凭证截图 → PaymentProofController.submitProof()
      │
      ├─ 1. 校验注册用户存在 + 未重复提交
      ├─ 2. 文件上传(MinIO/本地)
      ├─ 3. OCR初筛(TesseractOcrServiceImpl)
      │     ├─ 识别金额 → 自动填充actual_payment
      │     └─ 识别交易号 → 自动填充transaction_no
      ├─ 4. 水印处理(管理员可见水印版)
      ├─ 5. MD5+dHash去重校验
      ├─ 6. 保存payment_proof记录(status=proof_submitted)
      ├─ 7. 更新register_user.proof_status=proof_submitted
      └─ 8. 发第一封邮件"注册待核对"(sendProofSubmittedEmail)
            → 邮件中心createAndSend(USER_REGISTER)
            → 模板"注册待核对模板"(id=7)
            → 标题"MCLC注册凭证已提交，待核对"

管理端审核 → PaymentProofController.reviewProof()
  │
  ├─ pass(通过):
  │     ├─ proof.status = review_passed
  │     ├─ register_user.proof_status = paid ← 直接确认，不再走对账
  │     ├─ register_user.payment_status = paid
  │     └─ 发第二封邮件"注册成功"(sendRegistrationSuccessEmail)
  │           → 模板"注册通过模板"(id=6)
  │           → 标题"MCLC大会注册成功" → 引导到"我的注册"页面
  │
  └─ reject(驳回):
        ├─ proof.status = rejected
        ├─ register_user.proof_status = rejected
        └─ 记录驳回原因
```

## 3. 凭证状态流转

```
null → proof_submitted(待核验) → paid(审核通过/对账确认)
                             → rejected(驳回)
                             → offline_paid(线下登记)
```

**三种触发注册成功的方式**（均发第二封邮件）：
1. 凭证审核通过（reviewProof pass → proof_status=paid）
2. 对账确认（confirmMatch → proof_status=paid）
3. 线下登记（offlineRegister → proof_status=offline_paid）

## 4. OCR 初筛机制【已实现】【已验证】

**文件**: TesseractOcrServiceImpl.java

### 4.1 技术方案
- 库：Tess4j 5.13.0（Tesseract OCR 的 Java JNA 封装）
- 语言包：chi_sim+eng（中英文混合识别）
- PSM 模式：11（Sparse text）——不假设布局，尽量识别所有文字
  - **原因**：支付截图是多区域布局（大号金额+小号详情），PSM 6（单块文本）会漏掉大号金额

### 4.2 优雅降级设计（关键）
```java
@PostConstruct
public void init() {
    // 1. 探测tessdata路径（未配置时自动探测常见路径）
    // 2. 探测不到 → available=false，OCR不可用但不影响主流程
    // 3. 初始化异常(含UnsatisfiedLinkError) → available=false
}

public String recognizeText(InputStream imageStream, String fileName) {
    if (!available) return null;  // 降级：跳过OCR，不影响凭证提交
    try {
        return tesseract.doOCR(image);
    } catch (TesseractException e) {
        return null;  // 识别失败不阻断
    } catch (Throwable e) {
        // 关键：捕获Throwable含Error
        // tess4j加载语言包失败会抛java.lang.Error: Invalid memory access
        // catch(Exception)捕获不到，会导致整个凭证提交失败
        return null;
    }
}
```

### 4.3 路径探测
```
候选路径：
- /usr/share/tesseract-ocr/5/tessdata (Debian/Ubuntu Docker)
- /usr/share/tesseract-ocr/4.00/tessdata
- /usr/share/tessdata
- C:\Program Files\Tesseract-OCR\tessdata (Windows)
```

## 5. 水印 + 去重【已实现】

- **水印**：凭证图片添加管理员可见水印（watermarked_file_url 字段）
- **去重**：MD5（文件内容）+ dHash（感知哈希，抵抗轻微修改）双重校验

## 6. 两封邮件机制【已实现】【已验证】

| 邮件 | 触发时机 | 模板 | 标题 | 走邮件中心 |
|------|----------|------|------|-----------|
| 第一封 | 凭证提交成功 | 注册待核对模板(id=7) | MCLC注册凭证已提交，待核对 | 是(USER_REGISTER) |
| 第二封 | 审核通过/对账确认/线下登记 | 注册通过模板(id=6) | MCLC大会注册成功 | 是(USER_REGISTER) |

**设计要点**：
- 邮件发送在 try-catch 中，失败仅 log.error，不阻断业务主流程
- 走邮件中心 createAndSend → 9态状态机可追踪 → 支持退信检测和重试

## 7. 关键设计决策

| 决策 | 理由 |
|------|------|
| OCR 降级而非强制 | 服务器可能未安装 tesseract-ocr，OCR 是辅助初筛而非必须 |
| catch(Throwable) 而非 catch(Exception) | tess4j JNA 层抛 Error 而非 Exception |
| 审核通过直接确认 paid | 简化流程，审核通过即注册成功，无需再走对账确认 |
| 邮件发送不阻断业务 | 邮件是通知手段，发送失败不应导致凭证提交/审核失败 |
| PSM 11 而非 PSM 6 | 支付截图多区域布局，PSM 6 漏识别大号金额 |