# 案例03：凭证审核通过后注册未确认成功 — 状态流转设计缺陷

> **故障等级**: P2（用户已付费但状态未更新，第二封邮件未发送）  
> **发生时间**: 2026-10  
> **状态**: 【曾经发生】【已修复】【已验证】

---

## A. 故障现象

管理端凭证审核点击"通过"后：
- 注册用户 `proof_status` 变为 `review_passed`，但**不是** `paid`
- `payment_status` 未更新
- 第二封"注册成功"邮件**未发送**
- 用户在"我的注册"页面看到"审核中"而非"已完成"

## B. 排查过程

1. **确认操作路径**：管理端 → 支付凭证管理 → 审核通过（reviewProof pass）
2. **检查代码**：reviewProof 的 pass 分支设置 `proof.setStatus("review_passed")` + `registerUser.setProofStatus("review_passed")`
3. **对比设计**：`review_passed` 是中间状态，需要再走"对账确认"才能变为 `paid`
4. **检查对账记录**：`reconciliation_record` 表为空——从未执行对账确认
5. **确认业务诉求**：凭证审核通过即应视为注册成功，无需再走对账

## C. 根因分析

**状态流转设计过度复杂，与实际业务流程不匹配**。

原设计：
```
proof_submitted → review_passed（审核通过）→ paid（对账确认后）
```

实际问题：
- 协会不一定每次都提供对账 Excel
- 管理员审核凭证时已经核对了支付信息，通过即应确认
- `review_passed` 作为中间状态导致流程"卡住"

## D. 修复方案

**简化状态流转**：审核通过直接确认支付成功。

```java
// 修复前
if ("pass".equals(form.getAction())) {
    proof.setStatus("review_passed");
    registerUser.setProofStatus("review_passed");  // 中间状态，需对账确认
}

// 修复后
if ("pass".equals(form.getAction())) {
    proof.setStatus("review_passed");
    registerUser.setProofStatus("paid");       // 直接确认
    registerUser.setPaymentStatus("paid");     // 同步支付状态
}
// 审核通过后立即发送注册成功邮件
if ("pass".equals(form.getAction())) {
    sendRegistrationSuccessEmail(registerUser);
}
```

**统一三种触发注册成功的方式**：
1. 凭证审核通过（reviewProof pass → paid + 发邮件）
2. 对账确认（confirmMatch → paid + 发邮件）
3. 线下登记（offlineRegister → offline_paid + 发邮件）

## E. 验证方式

1. 管理端审核通过 → 检查 `proof_status=paid` + `payment_status=paid`
2. 邮件中心出现第二封"注册成功"记录（USER_REGISTER 类型）
3. 用户"我的注册"页面显示"已完成"状态
4. 驳回操作不受影响（proof_status=rejected）

## F. 影响范围

- 修复前：所有凭证审核通过的用户都卡在 review_passed，需手动对账确认
- 修复后：审核通过即注册成功，对账作为补充确认手段（处理未上传凭证的用户）

## G. 经验教训

1. **状态机设计要匹配实际业务流程**：不要为了"完整性"增加实际不会走的中间状态
2. **多种确认方式应殊途同归**：无论审核/对账/线下登记，最终状态和通知应一致
3. **用户视角验证**：修复后从用户"我的注册"页面确认状态展示正确
4. **状态字段联动**：proof_status 和 payment_status 需同步更新，避免状态不一致

## H. 相关代码/文件

| 文件 | 说明 |
|------|------|
| backend/.../PaymentProofServiceImpl.java | reviewProof pass 分支（直接设 paid + 发邮件） |
| backend/.../PaymentReconciliationServiceImpl.java | confirmMatch（对账确认 → paid + 发邮件） |
| frontend/.../paymentProof/index.vue | 审核通过确认文案更新 |
| webpage/.../MyRegistration.vue | 我的注册页面状态展示 |