# 对账全链路（多维度匹配引擎）

> **状态**: 【已实现】【已验证】  
> **核心文件**: PaymentReconciliationServiceImpl.java (18.1KB)  
> **数据库表**: reconciliation_batch / reconciliation_record / register_user_information  
> **最后验证**: 2026-10-29 本地环境

---

## 1. 业务背景

协会银行流水与系统注册记录需要对账确认。协会无法提供 API 对接，只能提供 Excel 对账单。系统通过**多维度匹配引擎**自动关联银行流水与注册用户，不依赖协会改造系统。

## 2. 全链路流程

```
管理端上传协会对账Excel → importReconciliation()
  │
  ├─ 1. 解析Excel(EasyExcel → ReconciliationImportDTO)
  ├─ 2. 创建对账批次(reconciliation_batch, batchNo=RC+yyyyMMddHHmmss)
  ├─ 3. 加载待匹配注册用户(proof_status IN [proof_submitted, review_passed])
  ├─ 4. 逐条匹配(findBestMatch → 多维度评分)
  │     ├─ score ≥ 60 → 自动匹配(auto_matched)
  │     │     → register_user.proof_status=paid + payment_status=paid
  │     │     → 发注册成功邮件
  │     └─ score < 60 → 未匹配(unmatched) → 进入差异列表
  └─ 5. 更新批次统计(matchedCount/unmatchedCount)

差异处理:
  ├─ 人工确认匹配(confirmMatch) → manual_matched → paid + 发邮件
  └─ 驳回(rejectMatch) → rejected
```

## 3. 多维度匹配引擎【代码实测】

### 3.1 评分模型

| 维度 | 满分 | 匹配规则 |
|------|------|----------|
| 姓名 | 30 | 完全匹配=30（含去空格），包含匹配=20 |
| 手机号 | 25 | 归一化后（去空格/横线/加号/括号）完全匹配=25 |
| 金额 | 25 | BigDecimal 精确比较（清洗逗号→点、去¥/€符号） |
| 时间 | 20 | ±1天=20，±3天=10（对比声明支付时间）；±7天=10（对比报名时间） |
| **总分** | **100** | **自动匹配阈值 ≥ 60** |

### 3.2 匹配逻辑细节

**姓名匹配** (matchName):
```
完全匹配(忽略大小写): 30分
去空格后完全匹配: 30分
包含匹配(协会备注可能带其他字): 20分
```

**手机号匹配** (normalizePhone):
```
归一化: 去除 空格/横线/加号/括号
归一化后完全匹配: 25分
```

**金额匹配**:
```
清洗: 逗号→点, 去¥/€, trim
BigDecimal.compareTo() == 0: 25分
```

**时间匹配** (matchTime):
```
优先对比 declaredPayTime（用户声明的支付时间）:
  ±1天: 20分
  ±3天: 10分
其次对比 registrationDate（报名时间）:
  ±7天: 10分（支付应在报名后7天内）
```

### 3.3 最佳匹配选择

遍历所有候选注册用户，取得分最高者。score > 0 且为当前最高分时更新 best。

## 4. 数据模型

### reconciliation_batch（对账批次）

| 字段 | 说明 |
|------|------|
| batch_no | 批次号（RC+yyyyMMddHHmmss） |
| file_name | 上传文件名 |
| total_count | 总记录数 |
| matched_count | 自动匹配数 |
| unmatched_count | 未匹配数 |
| status | completed |
| operator_id/name | 操作人 |

### reconciliation_record（对账记录）

| 字段 | 说明 |
|------|------|
| batch_id | 所属批次 |
| name/phone/amount/pay_time | 协会流水信息 |
| matched_registration_id | 匹配的注册用户ID |
| match_score | 匹配得分 |
| match_detail | 匹配维度详情（如"姓名精确匹配+手机号匹配(得分:55)"） |
| match_status | auto_matched / unmatched / manual_matched / rejected |

## 5. 与凭证审核的关系

| 场景 | 凭证审核 | 对账 | 结果 |
|------|----------|------|------|
| 用户上传凭证 → 管理员审核通过 | ✅ pass → paid | 不需要对账 | 注册成功 |
| 用户上传凭证 → 对账自动匹配 | 不需要审核 | ✅ auto_matched → paid | 注册成功 |
| 用户上传凭证 → 对账人工确认 | 不需要审核 | ✅ manual_matched → paid | 注册成功 |
| 线下付款 → 管理员登记 | 不需要 | 线下登记 → offline_paid | 注册成功 |

**三种触发注册成功的方式**均发送第二封"注册成功"邮件。

## 6. 关键设计决策

| 决策 | 理由 |
|------|------|
| 多维度评分而非精确匹配 | 协会流水姓名格式/手机号格式可能与注册信息不完全一致 |
| 阈值 60 分（姓名+任意一项即可） | 平衡准确率与召回率，低于 60 进入人工差异处理 |
| 人工确认兜底 | 自动匹配无法覆盖的场景由人工确认 |
| 对账确认也发注册成功邮件 | 无论哪种方式确认支付，用户都应收到通知 |
| 金额清洗多币种符号 | 国际会议支持人民币/欧元等多种支付方式 |
| 不依赖协会加字段 | 纯靠双方已有信息交叉匹配，降低协作成本 |