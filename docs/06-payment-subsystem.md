# MCLC 支付子系统技术文档

> 策略模式驱动的多网关支付系统，支持Stripe信用卡/微信支付/支付宝/银行转账4种支付方式
>
> **增强记录**：基于源码精确分析，修正回调端点URL、补充双路径回调架构、4个Provider方法级分解、
> 幂等性分析、签名验证流程、订单号生成规则、退款流程对比

## 1. 架构总览

```
┌──────────────────────────────────────────────────────────────────────────┐
│                          支付子系统架构                                     │
│                                                                          │
│  ┌──────────────────┐                                                    │
│  │ PaymentController │── POST /create-intent ──→ PaymentServiceImpl     │
│  │ (REST API)        │── GET  /status/{id}      → .createPaymentIntent() │
│  │                   │── POST /confirm/{id}     → .confirmBankTransfer() │
│  │                   │── POST /refund/{id}      → .refund()              │
│  └──────────────────┘                                                    │
│                                                                          │
│  ┌──────────────────────┐                                                │
│  │ StripeWebhookController│── POST /stripe/webhook → handleStripeWebhook│
│  └──────────────────────┘                                                │
│                                                                          │
│  ┌──────────────────────┐                                                │
│  │ AlipayCallbackController│── POST /alipay/notify → Provider+sync     │
│  │ WechatCallbackController│── POST /wechat/notify → Provider+sync     │
│  └──────────────────────┘                                                │
│                                                                          │
│  ┌──────────────────────────────────────────────────────────────────┐   │
│  │                     PaymentRouter (策略路由)                       │   │
│  │  route(paymentMethod) → 遍历List<PaymentProvider>匹配              │   │
│  │  routeByCallback(callbackType) → 按回调类型匹配                    │   │
│  └──────────────┬───────────────────────────────────────────────────┘   │
│                 │                                                        │
│  ┌──────────────┼──────────────────────────────────────────────────┐   │
│  │        PaymentProvider接口 (5方法)                                │   │
│  │  ┌───────────┐ ┌───────────┐ ┌──────────┐ ┌──────────────┐     │   │
│  │  │Stripe     │ │Wechat     │ │Alipay    │ │BankTransfer  │     │   │
│  │  │Provider   │ │Provider   │ │Provider  │ │Provider      │     │   │
│  │  │:86行      │ │:230行     │ │:265行    │ │:71行         │     │   │
│  │  └─────┬─────┘ └─────┬─────┘ └────┬─────┘ └──────┬───────┘     │   │
│  │        │              │            │               │             │   │
│  │  ┌─────▼─────┐ ┌─────▼─────┐ ┌────▼─────┐ ┌──────▼──────┐     │   │
│  │  │Stripe SDK │ │微信SDK V3 │ │支付宝SDK │ │(线下确认)    │     │   │
│  │  └───────────┘ └───────────┘ └──────────┘ └─────────────┘     │   │
│  └────────────────────────────────────────────────────────────────┘   │
│                                                                        │
│  ┌────────────────────────────────────────────────────────────────┐   │
│  │                       数据层                                    │   │
│  │  ┌──────────────────┐  ┌───────────────────────────────────┐  │   │
│  │  │ payment_orders   │  │ register_user_information          │  │   │
│  │  │ (支付订单)        │  │ (payment_method/payment_status/    │  │   │
│  │  │                  │  │  payment_amount/stripe_intent_id)  │  │   │
│  │  └──────────────────┘  └───────────────────────────────────┘  │   │
│  └────────────────────────────────────────────────────────────────┘   │
└──────────────────────────────────────────────────────────────────────────┘
```

## 2. 核心文件清单

### 2.1 接口与路由层

| 文件 | 路径(相对backend/src/main/java) | 行数 | 职责 |
|------|------|------|------|
| [`PaymentProvider`](backend/src/main/java/com/youlai/boot/portal/payment/PaymentProvider.java) | `portal/payment/` | 19 | 支付Provider接口，5方法定义 |
| [`PaymentRouter`](backend/src/main/java/com/youlai/boot/portal/payment/PaymentRouter.java) | `portal/payment/` | 39 | 策略路由器，route()+routeByCallback()双路由 |
| [`CreatePaymentRequest`](backend/src/main/java/com/youlai/boot/portal/payment/CreatePaymentRequest.java) | `portal/payment/` | 20 | 创建支付请求DTO(@Builder) |
| [`PaymentProviderResponse`](backend/src/main/java/com/youlai/boot/portal/payment/PaymentProviderResponse.java) | `portal/payment/` | 19 | Provider统一响应DTO(@Builder) |

### 2.2 Provider实现层

| 文件 | 路径 | 行数 | 支付方式 | 关键依赖 |
|------|------|------|----------|----------|
| [`StripePaymentProvider`](backend/src/main/java/com/youlai/boot/portal/payment/impl/StripePaymentProvider.java) | `portal/payment/impl/` | 86 | CREDIT_CARD | stripe-java SDK, BankTransferProperties(误注入⚠️) |
| [`WechatPaymentProvider`](backend/src/main/java/com/youlai/boot/portal/payment/impl/WechatPaymentProvider.java) | `portal/payment/impl/` | 230 | WECHAT | wechatpay-java V3, NativePayService, H5Service, RefundService |
| [`AlipayPaymentProvider`](backend/src/main/java/com/youlai/boot/portal/payment/impl/AlipayPaymentProvider.java) | `portal/payment/impl/` | 265 | ALIPAY | alipay-sdk-java, AlipayClient |
| [`BankTransferPaymentProvider`](backend/src/main/java/com/youlai/boot/portal/payment/impl/BankTransferPaymentProvider.java) | `portal/payment/impl/` | 71 | BANK_TRANSFER | BankTransferProperties |

### 2.3 服务与控制层

| 文件 | 路径 | 行数 | 职责 |
|------|------|------|------|
| [`PaymentService`](backend/src/main/java/com/youlai/boot/portal/service/PaymentService.java) | `portal/service/` | 29 | 支付服务接口，9方法 |
| [`PaymentServiceImpl`](backend/src/main/java/com/youlai/boot/portal/service/impl/PaymentServiceImpl.java) | `portal/service/impl/` | 274 | 支付业务实现(创建/查询/回调/退款/同步) |
| [`PaymentController`](backend/src/main/java/com/youlai/boot/portal/controller/PaymentController.java) | `portal/controller/` | 72 | 支付REST API，7端点 |
| [`StripeWebhookController`](backend/src/main/java/com/youlai/boot/portal/controller/StripeWebhookController.java) | `portal/controller/` | 27 | Stripe Webhook回调端点 |
| [`AlipayCallbackController`](backend/src/main/java/com/youlai/boot/portal/controller/AlipayCallbackController.java) | `portal/controller/` | 54 | 支付宝异步通知回调 |
| [`WechatCallbackController`](backend/src/main/java/com/youlai/boot/portal/controller/WechatCallbackController.java) | `portal/controller/` | 62 | 微信支付异步通知回调 |

### 2.4 模型与配置层

| 文件 | 路径 | 职责 |
|------|------|------|
| [`PaymentOrder`](backend/src/main/java/com/youlai/boot/portal/model/entity/PaymentOrder.java) | `portal/model/entity/` | 支付订单实体(@TableName="payment_orders") |
| [`PaymentIntentForm`](backend/src/main/java/com/youlai/boot/portal/model/form/PaymentIntentForm.java) | `portal/model/form/` | 创建支付意图表单(@Valid) |
| [`PaymentIntentResponse`](backend/src/main/java/com/youlai/boot/portal/model/vo/PaymentIntentResponse.java) | `portal/model/vo/` | 支付意图响应VO(@Builder) |
| [`PaymentMethodEnum`](backend/src/main/java/com/youlai/boot/portal/enums/PaymentMethodEnum.java) | `portal/enums/` | 支付方式枚举(4值+fromCode) |
| [`StripeProperties`](backend/src/main/java/com/youlai/boot/config/property/StripeProperties.java) | `config/property/` | @ConfigurationProperties(prefix="stripe") |
| [`WechatPayProperties`](backend/src/main/java/com/youlai/boot/config/property/WechatPayProperties.java) | `config/property/` | @ConfigurationProperties(prefix="wechat-pay") |
| [`AlipayProperties`](backend/src/main/java/com/youlai/boot/config/property/AlipayProperties.java) | `config/property/` | @ConfigurationProperties(prefix="alipay") |
| [`BankTransferProperties`](backend/src/main/java/com/youlai/boot/config/property/BankTransferProperties.java) | `config/property/` | @ConfigurationProperties(prefix="bank-transfer") |

## 3. PaymentProvider接口 — 策略模式

### 3.1 接口定义

```java
// PaymentProvider.java:8-18
public interface PaymentProvider {
    PaymentProviderResponse createPayment(CreatePaymentRequest request);  // 创建支付
    PaymentProviderResponse queryPayment(String orderNo);                  // 查询支付状态
    String handleCallback(Map<String, String> params);                    // 处理回调→返回orderNo
    void refund(String orderNo);                                           // 退款
    PaymentMethodEnum supportedMethod();                                   // 声明支持的支付方式
}
```

### 3.2 PaymentRouter双路由逻辑

[`PaymentRouter`](backend/src/main/java/com/youlai/boot/portal/payment/PaymentRouter.java) 通过Spring自动注入`List<PaymentProvider>`，运行时收集所有Provider实现。

**route(paymentMethod)** — 创建支付时使用（:22-29）：
```
PaymentRouter.route(paymentMethod)
  │
  ├── 1. PaymentMethodEnum.fromCode(paymentMethod) → 枚举校验
  │     └── null → 抛出IllegalArgumentException("不支持的支付方式: " + paymentMethod)
  ├── 2. 遍历List<PaymentProvider> providers
  │     └── provider.supportedMethod() == method → 返回该Provider
  └── 3. 未找到 → 抛出IllegalArgumentException("未找到支付Provider: " + paymentMethod)
```

**routeByCallback(callbackType)** — 回调处理时使用（:31-37）：
```
PaymentRouter.routeByCallback(callbackType)
  │
  ├── 遍历List<PaymentProvider> providers
  │     └── StrUtil.equals(callbackType, provider.supportedMethod().getCode()) → 返回
  └── 未找到 → 抛出IllegalArgumentException("未找到回调Provider: " + callbackType)
```

**关键区别**：
- `route()` 使用枚举等值比较 `==`，用于创建支付
- `routeByCallback()` 使用字符串比较 `StrUtil.equals()`，用于回调路由
- 两者都能定位到同一个Provider，但入口参数类型不同

### 3.3 PaymentMethodEnum枚举

[`PaymentMethodEnum`](backend/src/main/java/com/youlai/boot/portal/enums/PaymentMethodEnum.java:6-30)：

| 枚举 | code | label | 订单号前缀 |
|------|------|-------|-----------|
| CREDIT_CARD | `credit_card` | 信用卡 | (Stripe PI前缀`pi_`) |
| WECHAT | `wechat` | 微信支付 | `WXP` |
| ALIPAY | `alipay` | 支付宝 | `ALI` |
| BANK_TRANSFER | `bank_transfer` | 银行转账 | `BT` |

`fromCode(String code)` — 遍历枚举值匹配code，未找到返回null（不抛异常）

## 4. 四种支付方式详解

### 4.1 四Provider对比总览

| 维度 | Stripe | 微信 | 支付宝 | 银行转账 |
|------|--------|------|--------|----------|
| **文件行数** | 86 | 230 | 265 | 71 |
| **SDK** | stripe-java | wechatpay-java V3 | alipay-sdk-java | 无 |
| **创建支付** | PaymentIntent.create() | NativePayService/H5Service | AlipayTradePrecreate/WapPay | 返回银行信息Map |
| **查询支付** | PaymentIntent.retrieve() | queryOrderByOutTradeNo() | AlipayTradeQueryRequest | 始终返回pending |
| **回调处理** | Webhook(独立Controller) | NotificationParser验签 | RSA2手动验签 | 无回调 |
| **退款** | PaymentIntent.cancel() | RefundService.create() | AlipayTradeRefundRequest | 仅日志(线下处理) |
| **创建payment_orders** | ❌不创建 | ✅创建(含30分钟过期) | ✅创建(含30分钟过期) | ❌不创建 |
| **handleCallback返回** | null(由Service直接处理) | orderNo或null | orderNo或null | null |
| **金额单位** | 最小单位(×100) | 分(×100) | 元(保留2位) | 元(原样) |
| **订单号格式** | `pi_xxx`(Stripe生成) | `WXP`+timestamp+后4位 | `ALI`+timestamp+后4位 | `BT`+timestamp |

### 4.2 Stripe信用卡支付 — [`StripePaymentProvider`](backend/src/main/java/com/youlai/boot/portal/payment/impl/StripePaymentProvider.java)

**SDK**: `com.stripe:stripe-java`

**方法分解**：

| 方法 | 行号 | 实现 |
|------|------|------|
| [`supportedMethod()`](backend/src/main/java/com/youlai/boot/portal/payment/impl/StripePaymentProvider.java:28) | :28 | 返回`CREDIT_CARD` |
| [`createPayment()`](backend/src/main/java/com/youlai/boot/portal/payment/impl/StripePaymentProvider.java:32) | :32-55 | PaymentIntentCreateParams→PaymentIntent.create()→返回clientSecret |
| [`queryPayment()`](backend/src/main/java/com/youlai/boot/portal/payment/impl/StripePaymentProvider.java:57) | :57-66 | PaymentIntent.retrieve(orderNo) |
| [`handleCallback()`](backend/src/main/java/com/youlai/boot/portal/payment/impl/StripePaymentProvider.java:68) | :68-70 | 返回null（Stripe回调由Service直接处理） |
| [`refund()`](backend/src/main/java/com/youlai/boot/portal/payment/impl/StripePaymentProvider.java:72) | :72-80 | PaymentIntent.retrieve()+cancel() |

**createPayment详细流程**（:32-55）：
```
1. PaymentIntentCreateParams.builder()
     .setAmount(amount × 100)       // 元→分（Stripe最小单位）
     .setCurrency(currency)
     .setDescription("Registration fee")
2. if registrationId != null → putMetadata("registration_id", id)  // 关键：metadata关联注册用户
3. addPaymentMethodType("card")
4. PaymentIntent.create(params) → Stripe SDK发起
5. 返回 PaymentProviderResponse:
     orderNo = intent.getId()           // "pi_xxx"格式
     extraData = {clientSecret: intent.getClientSecret()}
```

**⚠️ 注意**：Stripe不创建payment_orders记录，直接使用PaymentIntent ID作为orderNo

**支付流程**：
```
前端 → POST /api/v1/payment/create-intent {paymentMethod: "credit_card"}
后端 → StripePaymentProvider.createPayment() → PaymentIntent → clientSecret
前端 → Stripe.js用clientSecret确认支付（前端直接与Stripe交互）
Stripe → POST /api/v1/stripe/webhook (payment_intent.succeeded事件)
后端 → StripeWebhookController → PaymentServiceImpl.handleStripeWebhook()
     → Webhook.constructEvent()验签 → 从metadata取registration_id
     → 直接更新register_user_information.payment_status = "paid"
     → 不经过payment_orders表
```

**配置**：`stripe.api-key`, `stripe.publishable-key`, `stripe.webhook-secret`

### 4.3 微信支付 — [`WechatPaymentProvider`](backend/src/main/java/com/youlai/boot/portal/payment/impl/WechatPaymentProvider.java)

**SDK**: `com.github.wechatpay-apiv3:wechatpay-java` (V3 API)

**依赖注入**（6个Bean）：
- `WechatPayProperties` — 配置
- `NativePayService` — 扫码付服务
- `H5Service` — H5支付服务
- `RefundService` — 退款服务
- `NotificationParser` — 回调通知解析器
- `PaymentOrderMapper` — 订单DB操作

**方法分解**：

| 方法 | 行号 | 实现 |
|------|------|------|
| [`supportedMethod()`](backend/src/main/java/com/youlai/boot/portal/payment/impl/WechatPaymentProvider.java:49) | :49 | 返回`WECHAT` |
| [`createPayment()`](backend/src/main/java/com/youlai/boot/portal/payment/impl/WechatPaymentProvider.java:53) | :53-72 | 生成订单号→创建payment_orders→按scene分流 |
| [`createNativePayment()`](backend/src/main/java/com/youlai/boot/portal/payment/impl/WechatPaymentProvider.java:74) | :74-99 | NativePayService.prepay()→qrCodeUrl→更新order |
| [`createH5Payment()`](backend/src/main/java/com/youlai/boot/portal/payment/impl/WechatPaymentProvider.java:101) | :101-132 | H5Service.prepay()→h5Url→更新order |
| [`queryPayment()`](backend/src/main/java/com/youlai/boot/portal/payment/impl/WechatPaymentProvider.java:134) | :134-155 | queryOrderByOutTradeNo()→SUCCESS时更新order为paid |
| [`handleCallback()`](backend/src/main/java/com/youlai/boot/portal/payment/impl/WechatPaymentProvider.java:157) | :157-187 | NotificationParser验签→SUCCESS时更新order→返回orderNo |
| [`refund()`](backend/src/main/java/com/youlai/boot/portal/payment/impl/WechatPaymentProvider.java:189) | :189-213 | 校验order状态→RefundService.create()→更新refunded |
| [`convertTradeState()`](backend/src/main/java/com/youlai/boot/portal/payment/impl/WechatPaymentProvider.java:215) | :215-223 | TradeState枚举→内部状态映射 |

**createPayment详细流程**（:53-72）：
```
1. 生成订单号: "WXP" + System.currentTimeMillis() + registrationId后4位
   （匿名支付: "WXP" + System.currentTimeMillis()）
2. 创建PaymentOrder实体:
     orderNo, paymentMethod="wechat", amount, currency, status="pending"
     createTime=now, expireTime=now+30分钟
3. paymentOrderMapper.insert(order) — 先落库
4. if scene=="h5" → createH5Payment()
   else → createNativePayment()
```

**handleCallback验签流程**（:157-187）：
```
1. 从params提取: wechatpaySignature/Nonce/Timestamp/Serial/body
2. 构建RequestParam(serialNumber, nonce, signature, timestamp, body)
3. notificationParser.parse(requestParam, Transaction.class)
   → SDK自动验签（使用平台证书）
4. if TradeState == SUCCESS:
     查找payment_orders by orderNo
     if order != null && status=="pending":
       order.status = "paid"
       order.transactionId = transaction.getTransactionId()
       order.paidTime = now
       paymentOrderMapper.updateById(order)
       return orderNo
5. else: return null
```

**convertTradeState映射**（:215-223）：
| TradeState | 内部状态 |
|------------|----------|
| SUCCESS | paid |
| CLOSED / REVOKED | closed |
| 其他 | pending |

**配置**：`wechat-pay.app-id`, `mch-id`, `api-v3-key`, `private-key-path`, `mch-serial-no`, `platform-cert-path`, `notify-url`, `refund-notify-url`

### 4.4 支付宝 — [`AlipayPaymentProvider`](backend/src/main/java/com/youlai/boot/portal/payment/impl/AlipayPaymentProvider.java)

**SDK**: `com.alipay.sdk:alipay-sdk-java`

**方法分解**：

| 方法 | 行号 | 实现 |
|------|------|------|
| [`supportedMethod()`](backend/src/main/java/com/youlai/boot/portal/payment/impl/AlipayPaymentProvider.java:40) | :40 | 返回`ALIPAY` |
| [`createPayment()`](backend/src/main/java/com/youlai/boot/portal/payment/impl/AlipayPaymentProvider.java:44) | :44-63 | 生成订单号→创建payment_orders→按scene分流 |
| [`createScanPayment()`](backend/src/main/java/com/youlai/boot/portal/payment/impl/AlipayPaymentProvider.java:65) | :65-105 | AlipayTradePrecreateRequest→qrCode→更新order |
| [`createH5Payment()`](backend/src/main/java/com/youlai/boot/portal/payment/impl/AlipayPaymentProvider.java:107) | :107-143 | AlipayTradeWapPayRequest→payUrl(HTML表单)→更新order |
| [`queryPayment()`](backend/src/main/java/com/youlai/boot/portal/payment/impl/AlipayPaymentProvider.java:145) | :145-173 | AlipayTradeQueryRequest→TRADE_SUCCESS时更新order |
| [`handleCallback()`](backend/src/main/java/com/youlai/boot/portal/payment/impl/AlipayPaymentProvider.java:175) | :175-232 | RSA2手动验签→TRADE_SUCCESS时更新order→返回orderNo |
| [`refund()`](backend/src/main/java/com/youlai/boot/portal/payment/impl/AlipayPaymentProvider.java:234) | :234-261 | 校验order→AlipayTradeRefundRequest→更新refunded |
| [`convertStatus()`](backend/src/main/java/com/youlai/boot/portal/payment/impl/AlipayPaymentProvider.java:263) | :263-263 | tradeStatus→内部状态映射 |

**handleCallback验签流程（手动RSA2验签）**（:175-232）：
```
1. 复制params → verifyParams
2. 移除sign和sign_type
3. 按key排序拼接: key1=value1&key2=value2...
4. 从alipayPublicKey构造X509EncodedKeySpec → RSA PublicKey
5. Signature.getInstance("SHA256withRSA") → initVerify(pubKey)
6. sig.update(content.getBytes(UTF_8)) → sig.verify(Base64.decode(sign))
7. if !verified → log.warn("支付宝回调验签失败") → return null
8. if TRADE_SUCCESS:
     查找payment_orders by out_trade_no
     if order != null && status=="pending":
       order.status = "paid"
       order.transactionId = tradeNo
       order.paidTime = now
       paymentOrderMapper.updateById(order)
       return orderNo
```

**⚠️ 验签实现问题**：使用`X509EncodedKeySpec`解析`alipayPublicKey`，但支付宝公钥通常为PKCS#1格式或Base64编码的裸公钥，可能需要`X509EncodedKeySpec`包装。如果验签失败，检查公钥格式是否匹配。

**convertStatus映射**（:263-263）：
| tradeStatus | 内部状态 |
|-------------|----------|
| TRADE_SUCCESS / TRADE_FINISHED | paid |
| TRADE_CLOSED | closed |
| null / 其他 | pending |

**配置**：`alipay.app-id`, `private-key`, `alipay-public-key`, `notify-url`, `return-url`, `gateway-url`, `sign-type`(默认RSA2), `charset`(默认UTF-8)

### 4.5 银行转账 — [`BankTransferPaymentProvider`](backend/src/main/java/com/youlai/boot/portal/payment/impl/BankTransferPaymentProvider.java)

**特点**：无在线支付，仅返回银行信息，管理员手动确认

**方法分解**：

| 方法 | 行号 | 实现 |
|------|------|------|
| [`supportedMethod()`](backend/src/main/java/com/youlai/boot/portal/payment/impl/BankTransferPaymentProvider.java:28) | :28 | 返回`BANK_TRANSFER` |
| [`createPayment()`](backend/src/main/java/com/youlai/boot/portal/payment/impl/BankTransferPaymentProvider.java:32) | :32-38 | 生成BT+timestamp订单号→返回银行信息extraData |
| [`queryPayment()`](backend/src/main/java/com/youlai/boot/portal/payment/impl/BankTransferPaymentProvider.java:40) | :40-44 | 始终返回pending（无在线查询） |
| [`handleCallback()`](backend/src/main/java/com/youlai/boot/portal/payment/impl/BankTransferPaymentProvider.java:46) | :46-48 | 返回null（无异步回调） |
| [`refund()`](backend/src/main/java/com/youlai/boot/portal/payment/impl/BankTransferPaymentProvider.java:50) | :50-52 | 仅记录日志（需线下处理） |
| [`buildBankInfo()`](backend/src/main/java/com/youlai/boot/portal/payment/impl/BankTransferPaymentProvider.java:54) | :54-67 | 构建银行信息Map(7字段+reference) |

**buildBankInfo返回字段**（:54-67）：
| key | 来源 | 说明 |
|-----|------|------|
| bankName | BankTransferProperties | 银行名称 |
| accountName | BankTransferProperties | 账户名 |
| accountNumber | BankTransferProperties | 账号 |
| iban | BankTransferProperties | IBAN |
| swiftCode | BankTransferProperties | SWIFT代码 |
| bankAddress | BankTransferProperties | 银行地址 |
| reference | referenceFormat格式化 | 汇款参考号(含registrationId) |

**管理员确认流程**（PaymentServiceImpl:121-131）：
```
POST /api/v1/payment/confirm/{registrationId}  (@PreAuthorize sys:registerUser:update)
  → Assert registerUser != null
  → Assert paymentMethod == "bank_transfer"
  → Assert paymentStatus != "paid"
  → registerUser.paymentStatus = "paid"
  → registerUserService.updateById(registerUser)
```

**⚠️ 注意**：银行转账确认不创建payment_orders记录，直接更新register_user_information

**配置**：`bank-transfer.bank-name`, `account-name`, `account-number`, `iban`, `swift-code`, `bank-address`, `reference-format`(默认`{}`)

## 5. 支付状态流转

### 5.1 payment_orders.status

```
pending ──→ paid ──→ refunded
  │
  └──→ closed (微信CLOSED/REVOKED, 支付宝TRADE_CLOSED)
```

| 状态 | 说明 | 触发方式 |
|------|------|----------|
| pending | 已创建，等待支付 | createPayment()时设置 |
| paid | 支付成功 | 回调验签成功 / 管理员确认 |
| refunded | 已退款 | refund()成功 |
| closed | 已关闭/已撤销 | 微信CLOSED/REVOKED, 支付宝TRADE_CLOSED |

**⚠️ 注意**：Stripe和银行转账不创建payment_orders记录，因此这两个支付方式没有此表的状态流转

### 5.2 register_user_information支付相关字段

| 字段 | 类型 | 说明 |
|------|------|------|
| payment_method | String | 支付方式(credit_card/wechat/alipay/bank_transfer) |
| payment_status | String | 支付状态(null/pending/paid/refunded) |
| payment_amount | String | 支付金额(字符串类型) |
| stripe_payment_intent_id | String | Stripe PaymentIntent ID(仅信用卡) |

**状态流转**：
```
null/pending ──→ paid ──→ refunded
                   ↑
        ┌──────────┴──────────┐
        │                     │
  回调验签成功          管理员确认(银行转账)
```

**幂等保护**：所有更新payment_status的路径都检查`!"paid".equals(status) && !"refunded".equals(status)`，防止重复更新

### 5.3 双表状态同步关系

| 支付方式 | payment_orders | register_user_information | 同步方式 |
|----------|---------------|--------------------------|----------|
| Stripe | ❌不创建 | ✅直接更新 | Webhook→metadata→直接更新 |
| 微信 | ✅创建 | ✅回调后同步 | Provider更新order→syncRegisterUserByOrderNo |
| 支付宝 | ✅创建 | ✅回调后同步 | Provider更新order→syncRegisterUserByOrderNo |
| 银行转账 | ❌不创建 | ✅直接更新 | 管理员确认→直接更新 |

## 6. 核心业务流程

### 6.1 PaymentServiceImpl方法分解

[`PaymentServiceImpl`](backend/src/main/java/com/youlai/boot/portal/service/impl/PaymentServiceImpl.java) (274行)：

| 方法 | 行号 | 事务 | 职责 |
|------|------|------|------|
| [`createPaymentIntent()`](backend/src/main/java/com/youlai/boot/portal/service/impl/PaymentServiceImpl.java:48) | :48-105 | @Transactional | 创建支付意图(7步) |
| [`getPaymentStatus()`](backend/src/main/java/com/youlai/boot/portal/service/impl/PaymentServiceImpl.java:107) | :107-122 | 无 | 查询支付状态 |
| [`getBankTransferInfo()`](backend/src/main/java/com/youlai/boot/portal/service/impl/PaymentServiceImpl.java:124) | :124-134 | 无 | 获取银行转账信息(7字段) |
| [`confirmBankTransfer()`](backend/src/main/java/com/youlai/boot/portal/service/impl/PaymentServiceImpl.java:136) | :136-146 | @Transactional | 确认银行转账到账 |
| [`refund()`](backend/src/main/java/com/youlai/boot/portal/service/impl/PaymentServiceImpl.java:148) | :148-170 | @Transactional | 退款(含Stripe退款) |
| [`handleAlipayNotify()`](backend/src/main/java/com/youlai/boot/portal/service/impl/PaymentServiceImpl.java:172) | :172-180 | @Transactional | 支付宝回调处理 |
| [`handleWechatNotify()`](backend/src/main/java/com/youlai/boot/portal/service/impl/PaymentServiceImpl.java:182) | :182-196 | @Transactional | 微信回调处理 |
| [`syncRegisterUserByOrderNo()`](backend/src/main/java/com/youlai/boot/portal/service/impl/PaymentServiceImpl.java:198) | :198-215 | 无 | 订单→用户状态同步 |
| [`handleStripeWebhook()`](backend/src/main/java/com/youlai/boot/portal/service/impl/PaymentServiceImpl.java:217) | :217-254 | @Transactional | Stripe Webhook处理 |
| [`buildBankTransferInfo()`](backend/src/main/java/com/youlai/boot/portal/service/impl/PaymentServiceImpl.java:256) | :256-274 | 无 | 构建银行信息Map(含reference) |

### 6.2 创建支付意图详细流程

[`createPaymentIntent()`](backend/src/main/java/com/youlai/boot/portal/service/impl/PaymentServiceImpl.java:48) (:48-105)：

```
PaymentServiceImpl.createPaymentIntent(form)
  │
  ├── 1. 查找注册用户
  │     ├── form.registrationId != null → registerUserService.getById()
  │     │     └── Assert.isTrue(registerUser != null, "注册用户不存在")
  │     └── form.registrationId == null → 匿名支付
  │
  ├── 2. 确定支付金额
  │     ├── 有registrationId && amount==null → 取registerUser.paymentAmount
  │     │     └── Assert.isTrue(paymentAmount不为空, "注册用户未设置支付金额")
  │     ├── 有registrationId && amount!=null → 使用form.amount
  │     └── 无registrationId → Assert.isTrue(amount!=null, "匿名支付时支付金额不能为空")
  │
  ├── 3. PaymentRouter.route(form.paymentMethod) → 获取Provider
  │
  ├── 4. 构建CreatePaymentRequest(@Builder)
  │     registrationId, paymentMethod, amount, currency, scene, returnUrl, registerUser
  │
  ├── 5. 更新registerUser的paymentMethod和paymentAmount
  │     └── registerUserService.updateById(registerUser)  ⚠️ 在Provider调用前更新
  │
  ├── 6. provider.createPayment(request) → 获取Provider响应
  │     ├── Stripe → clientSecret
  │     ├── 微信scan → qrCodeUrl + 创建payment_orders
  │     ├── 微信h5 → payUrl + 创建payment_orders
  │     ├── 支付宝scan → qrCodeUrl + 创建payment_orders
  │     ├── 支付宝h5 → payUrl + 创建payment_orders
  │     └── 银行转账 → bankTransferInfo
  │
  └── 7. 构建PaymentIntentResponse(@Builder)
        ├── 通用字段: registrationId, orderNo, paymentMethod, paymentStatus="pending", amount
        ├── if qrCodeUrl不为空 → builder.qrCodeUrl()
        ├── if payUrl不为空 → builder.payUrl()
        ├── if extraData.containsKey("clientSecret") → builder.clientSecret()  (Stripe)
        └── if paymentMethod=="bank_transfer" → builder.bankTransferInfo()  (银行转账)
```

### 6.3 双路径回调架构

**⚠️ 关键发现**：微信和支付宝回调存在两条处理路径：

```
路径A（Controller直接处理 — 实际使用路径）:
┌──────────────────────────┐
│ AlipayCallbackController │  POST /api/v1/payment/alipay/notify
│ WechatCallbackController │  POST /api/v1/payment/wechat/notify
└──────────┬───────────────┘
           │
           ├── paymentRouter.routeByCallback(code) → Provider
           ├── provider.handleCallback(params) → 验签+更新payment_orders → 返回orderNo
           └── if orderNo != null → paymentService.syncRegisterUserByOrderNo(orderNo)
                   └── 更新register_user_information.payment_status = "paid"

路径B（Service层方法 — 另一条路径）:
┌──────────────────────────┐
│ PaymentServiceImpl       │
│ .handleAlipayNotify()    │  @Transactional
│ .handleWechatNotify()    │  @Transactional
└──────────┬───────────────┘
           │
           ├── paymentRouter.route(code) → Provider
           ├── provider.handleCallback(params) → 验签+更新payment_orders → 返回orderNo
           └── if orderNo != null → syncRegisterUserByOrderNo(orderNo)
```

**两条路径的区别**：
| 维度 | 路径A (Controller) | 路径B (Service) |
|------|-------------------|----------------|
| 路由方法 | `routeByCallback()` | `route()` |
| 事务控制 | 无@Transactional | @Transactional |
| 回调返回值 | "success"/"fail" 或 "SUCCESS"/"FAIL" | void |
| 当前使用 | ✅实际使用 | ⚠️未被Controller调用 |

**⚠️ 风险**：路径A的Controller方法没有@Transactional，但Provider内部更新payment_orders是独立操作，syncRegisterUserByOrderNo也是独立调用，如果中间失败可能导致payment_orders已更新但register_user_information未更新

### 6.4 Stripe Webhook回调流程

[`handleStripeWebhook()`](backend/src/main/java/com/youlai/boot/portal/service/impl/PaymentServiceImpl.java:217) (:217-254)：

```
StripeWebhookController.handleWebhook(payload, Stripe-Signature header)
  → PaymentServiceImpl.handleStripeWebhook(payload, sigHeader)
  │
  ├── 1. Webhook.constructEvent(payload, sigHeader, webhookSecret)
  │     └── SignatureVerificationException → 抛RuntimeException("签名验证失败")
  │
  ├── 2. if event.type == "payment_intent.succeeded":
  │     ├── PaymentIntent intent = event.getDataObjectDeserializer().getObject()
  │     ├── if intent == null → log.warn → return
  │     ├── registrationId = intent.getMetadata().get("registration_id")
  │     ├── if registrationId为空 → log.warn → return
  │     ├── registerUser = registerUserService.getById(Long.valueOf(registrationId))
  │     └── if registerUser != null && status != "paid" && status != "refunded":
  │           registerUser.paymentStatus = "paid"
  │           registerUserService.updateById(registerUser)
  │
  └── 3. 其他事件类型 → 忽略（不处理）
```

**Stripe回调特点**：
- 不经过payment_orders表，直接通过metadata中的registration_id更新register_user_information
- 不使用Provider.handleCallback()（返回null）
- 幂等保护：检查status != "paid" && != "refunded"

### 6.5 订单-用户状态同步

[`syncRegisterUserByOrderNo()`](backend/src/main/java/com/youlai/boot/portal/service/impl/PaymentServiceImpl.java:198) (:198-215)：

```
syncRegisterUserByOrderNo(orderNo)
  │
  ├── 1. 查找payment_orders by orderNo
  │     └── if null → log.warn("未找到对应订单") → return
  │
  ├── 2. if payment_orders.status != "paid" → return (非成功状态不同步)
  │
  ├── 3. 查找register_user_information by registrationId
  │
  └── 4. if registerUser != null && status != "paid" && status != "refunded":
        registerUser.paymentStatus = "paid"
        registerUserService.updateById(registerUser)
```

**幂等保护**：检查`!"paid".equals(status) && !"refunded".equals(status)`，已支付/已退款不重复更新

## 7. PaymentOrder实体与订单号生成

### 7.1 PaymentOrder实体

[`PaymentOrder`](backend/src/main/java/com/youlai/boot/portal/model/entity/PaymentOrder.java) (@TableName="payment_orders")：

| 字段 | 类型 | @TableField | 说明 |
|------|------|-------------|------|
| id | Long | @TableId(ASSIGN_ID) | 主键 |
| registrationId | Long | registration_id | 关联注册用户ID |
| paymentMethod | String | payment_method | 支付方式 |
| orderNo | String | order_no | 商户订单号 |
| transactionId | String | transaction_id | 三方交易号 |
| amount | String | - | 金额(字符串) |
| currency | String | - | 币种 |
| status | String | - | 状态(pending/paid/refunded/closed) |
| qrCodeUrl | String | qr_code_url | 二维码链接 |
| payUrl | String | pay_url | 支付跳转链接 |
| extraData | String | extra_data | 扩展数据 |
| expireTime | LocalDateTime | expire_time | 过期时间 |
| paidTime | LocalDateTime | paid_time | 支付成功时间 |
| createTime | LocalDateTime | create_time | 创建时间 |
| updateTime | LocalDateTime | update_time | 更新时间 |

### 7.2 订单号生成规则

| 支付方式 | 前缀 | 格式 | 示例 | 生成位置 |
|----------|------|------|------|----------|
| 微信 | WXP | `WXP` + timestamp + registrationId后4位 | WXP17236123456781234 | WechatPaymentProvider:55-56 |
| 支付宝 | ALI | `ALI` + timestamp + registrationId后4位 | ALI17236123456781234 | AlipayPaymentProvider:45-46 |
| 银行转账 | BT | `BT` + timestamp | BT1723612345678 | BankTransferPaymentProvider:33 |
| Stripe | (Stripe生成) | `pi_` + 随机串 | pi_3ABC... | StripePaymentProvider:47 |

**⚠️ 注意**：
- 微信和支付宝：匿名支付时不含registrationId后4位
- Stripe：使用Stripe SDK生成的PaymentIntent ID，不自行生成
- 银行转账：生成BT前缀订单号但不创建payment_orders记录

### 7.3 payment_orders使用情况

| 支付方式 | 是否创建payment_orders | 更新时机 |
|----------|----------------------|----------|
| Stripe | ❌不创建 | Webhook直接更新register_user_information |
| 微信 | ✅创建 | createPayment()时insert，回调/查询时update |
| 支付宝 | ✅创建 | createPayment()时insert，回调/查询时update |
| 银行转账 | ❌不创建 | 管理员确认直接更新register_user_information |

## 8. API接口

### 8.1 PaymentController — [`PaymentController`](backend/src/main/java/com/youlai/boot/portal/controller/PaymentController.java)

| 方法 | HTTP | URL | 权限 | 说明 |
|------|------|-----|------|------|
| [`createPaymentIntent()`](backend/src/main/java/com/youlai/boot/portal/controller/PaymentController.java:33) | POST | /api/v1/payment/create-intent | 公开 | 创建支付意图 |
| [`getPaymentStatus()`](backend/src/main/java/com/youlai/boot/portal/controller/PaymentController.java:40) | GET | /api/v1/payment/status/{registrationId} | 公开 | 查询支付状态 |
| [`getBankTransferInfo()`](backend/src/main/java/com/youlai/boot/portal/controller/PaymentController.java:48) | GET | /api/v1/payment/bank-info | 公开 | 获取银行转账信息 |
| [`confirmBankTransfer()`](backend/src/main/java/com/youlai/boot/portal/controller/PaymentController.java:54) | POST | /api/v1/payment/confirm/{registrationId} | sys:registerUser:update | 确认银行转账到账 |
| [`refund()`](backend/src/main/java/com/youlai/boot/portal/controller/PaymentController.java:61) | POST | /api/v1/payment/refund/{registrationId} | sys:registerUser:update | 退款 |
| [`getPaymentConfig()`](backend/src/main/java/com/youlai/boot/portal/controller/PaymentController.java:68) | GET | /api/v1/payment/config | 公开 | 获取Stripe发布密钥 |

### 8.2 回调Controller

| Controller | HTTP | URL | 权限 | 说明 |
|------------|------|-----|------|------|
| [`StripeWebhookController`](backend/src/main/java/com/youlai/boot/portal/controller/StripeWebhookController.java:24) | POST | /api/v1/stripe/webhook | 公开 | Stripe Webhook回调 |
| [`AlipayCallbackController`](backend/src/main/java/com/youlai/boot/portal/controller/AlipayCallbackController.java:30) | POST | /api/v1/payment/alipay/notify | 公开 | 支付宝异步通知 |
| [`WechatCallbackController`](backend/src/main/java/com/youlai/boot/portal/controller/WechatCallbackController.java:30) | POST | /api/v1/payment/wechat/notify | 公开 | 微信支付异步通知 |

**⚠️ 修正**：原文档标注支付宝和微信回调URL为"推测"，实际源码确认为：
- 支付宝：`/api/v1/payment/alipay/notify`
- 微信：`/api/v1/payment/wechat/notify`

### 8.3 回调返回值约定

| 支付方式 | 成功返回 | 失败返回 | 说明 |
|----------|----------|----------|------|
| Stripe | "OK" (String) | 抛异常 | Stripe要求返回200 OK |
| 支付宝 | "success" | "fail" | 支付宝要求返回"success"才停止重试 |
| 微信 | "SUCCESS" | "FAIL" | 微信要求返回"SUCCESS"才停止重试 |

### 8.4 PaymentIntentForm字段

[`PaymentIntentForm`](backend/src/main/java/com/youlai/boot/portal/model/form/PaymentIntentForm.java)：

| 字段 | 类型 | 校验 | 默认值 | 说明 |
|------|------|------|--------|------|
| registrationId | Long | 可选 | null | 注册用户ID(不传=匿名支付) |
| paymentMethod | String | @NotBlank | - | credit_card/wechat/alipay/bank_transfer |
| amount | BigDecimal | 可选 | null | 支付金额(匿名支付必填) |
| currency | String | 可选 | "eur" | 货币 |
| scene | String | 可选 | "scan" | scan(扫码)/h5(手机H5) |
| returnUrl | String | 可选 | null | H5支付完成后的回跳地址 |

### 8.5 PaymentIntentResponse字段

[`PaymentIntentResponse`](backend/src/main/java/com/youlai/boot/portal/model/vo/PaymentIntentResponse.java) (@Builder)：

| 字段 | 类型 | 说明 | 返回条件 |
|------|------|------|----------|
| registrationId | Long | 注册用户ID | 始终 |
| orderNo | String | 商户订单号 | 始终 |
| clientSecret | String | Stripe clientSecret | credit_card |
| qrCodeUrl | String | 二维码链接 | wechat/alipay(scan) |
| payUrl | String | 支付跳转链接 | wechat/alipay(h5) |
| paymentMethod | String | 付款方式 | 始终 |
| paymentStatus | String | 支付状态 | 始终(创建时="pending") |
| amount | String | 支付金额 | 始终 |
| bankTransferInfo | Map | 银行转账信息 | bank_transfer |

## 9. 配置参考

### 9.1 StripeProperties — [`StripeProperties`](backend/src/main/java/com/youlai/boot/config/property/StripeProperties.java)

@ConfigurationProperties(prefix="stripe")

| 属性 | 类型 | 说明 |
|------|------|------|
| apiKey | String | Stripe密钥(sk_test_/sk_live_) |
| publishableKey | String | Stripe发布密钥(pk_test_/pk_live_) |
| webhookSecret | String | Webhook签名密钥(whsec_) |

### 9.2 WechatPayProperties — [`WechatPayProperties`](backend/src/main/java/com/youlai/boot/config/property/WechatPayProperties.java)

@ConfigurationProperties(prefix="wechat-pay")

| 属性 | 类型 | 说明 |
|------|------|------|
| appId | String | 应用ID |
| mchId | String | 商户号 |
| apiV3Key | String | API V3密钥 |
| privateKeyPath | String | 商户私钥路径 |
| mchSerialNo | String | 商户证书序列号 |
| platformCertPath | String | 平台证书路径 |
| notifyUrl | String | 支付回调通知URL |
| refundNotifyUrl | String | 退款回调通知URL |

### 9.3 AlipayProperties — [`AlipayProperties`](backend/src/main/java/com/youlai/boot/config/property/AlipayProperties.java)

@ConfigurationProperties(prefix="alipay")

| 属性 | 类型 | 默认值 | 说明 |
|------|------|--------|------|
| appId | String | - | 应用ID |
| privateKey | String | - | 应用私钥 |
| alipayPublicKey | String | - | 支付宝公钥(用于验签) |
| notifyUrl | String | - | 异步通知URL |
| returnUrl | String | - | 同步回跳URL |
| gatewayUrl | String | https://openapi.alipay.com/gateway.do | 网关地址 |
| signType | String | RSA2 | 签名算法 |
| charset | String | UTF-8 | 字符集 |
| format | String | json | 数据格式 |

### 9.4 BankTransferProperties — [`BankTransferProperties`](backend/src/main/java/com/youlai/boot/config/property/BankTransferProperties.java)

@ConfigurationProperties(prefix="bank-transfer")

| 属性 | 类型 | 说明 |
|------|------|------|
| bankName | String | 银行名称 |
| accountName | String | 账户名 |
| accountNumber | String | 账号 |
| iban | String | IBAN |
| swiftCode | String | SWIFT代码 |
| bankAddress | String | 银行地址 |
| referenceFormat | String | 汇款参考号格式(如"MCLC-{}") |

### 9.5 application.yml配置示例

```yaml
stripe:
  api-key: ${STRIPE_API_KEY}
  publishable-key: ${STRIPE_PUBLISHABLE_KEY}
  webhook-secret: ${STRIPE_WEBHOOK_SECRET}

wechat-pay:
  app-id: ${WECHAT_APP_ID}
  mch-id: ${WECHAT_MCH_ID}
  api-v3-key: ${WECHAT_API_V3_KEY}
  private-key-path: ${WECHAT_PRIVATE_KEY_PATH}
  mch-serial-no: ${WECHAT_MCH_SERIAL_NO}
  platform-cert-path: ${WECHAT_PLATFORM_CERT_PATH}
  notify-url: ${WECHAT_NOTIFY_URL}
  refund-notify-url: ${WECHAT_REFUND_NOTIFY_URL}

alipay:
  gateway-url: https://openapi.alipay.com/gateway.do
  app-id: ${ALIPAY_APP_ID}
  private-key: ${ALIPAY_PRIVATE_KEY}
  alipay-public-key: ${ALIPAY_PUBLIC_KEY}
  notify-url: ${ALIPAY_NOTIFY_URL}
  return-url: ${ALIPAY_RETURN_URL}

bank-transfer:
  bank-name: ${BANK_NAME}
  account-name: ${BANK_ACCOUNT_NAME}
  account-number: ${BANK_ACCOUNT_NUMBER}
  iban: ${BANK_IBAN}
  swift-code: ${BANK_SWIFT_CODE}
  bank-address: ${BANK_ADDRESS}
  reference-format: "MCLC-{}"
```

## 10. 退款流程对比

### 10.1 四种退款机制

| 维度 | Stripe | 微信 | 支付宝 | 银行转账 |
|------|--------|------|--------|----------|
| **触发方式** | PaymentServiceImpl.refund() | PaymentServiceImpl.refund() | PaymentServiceImpl.refund() | PaymentServiceImpl.refund() |
| **退款API** | PaymentIntent.cancel() | RefundService.create() | AlipayTradeRefundRequest | 仅日志 |
| **退款粒度** | 全额(取消PaymentIntent) | 全额(order.getAmount()) | 全额(order.getAmount()) | 线下处理 |
| **三方确认** | ✅Stripe API确认 | ✅微信API确认 | ✅支付宝API确认 | ❌无三方确认 |
| **payment_orders更新** | ❌无记录 | ✅status→refunded | ✅status→refunded | ❌无记录 |
| **register_user更新** | ✅paymentStatus→refunded | ✅paymentStatus→refunded | ✅paymentStatus→refunded | ✅paymentStatus→refunded |
| **退款单号** | 无 | "RF"+orderNo | 无 | 无 |

### 10.2 退款详细流程

[`refund()`](backend/src/main/java/com/youlai/boot/portal/service/impl/PaymentServiceImpl.java:148) (:148-170)：

```
PaymentServiceImpl.refund(registrationId)
  │
  ├── 1. registerUser = registerUserService.getById(registrationId)
  │     └── Assert.isTrue(registerUser != null, "注册用户不存在")
  │
  ├── 2. Assert.isTrue(paymentStatus == "paid", "该订单未支付，无法退款")
  │
  ├── 3. if stripePaymentIntentId不为空:
  │     ├── paymentRouter.route("credit_card") → StripePaymentProvider
  │     ├── provider.refund(stripePaymentIntentId)
  │     │     └── PaymentIntent.retrieve()+cancel()
  │     └── 失败 → 抛RuntimeException("退款失败")
  │
  └── 4. registerUser.paymentStatus = "refunded"
        registerUserService.updateById(registerUser)
```

**⚠️ 退款设计问题**：
1. 仅检查`stripePaymentIntentId`来决定是否调用Stripe退款，不检查其他支付方式的payment_orders
2. 微信/支付宝退款未在Service层触发，Provider.refund()直接操作payment_orders但Service层不知道
3. 银行转账退款仅更新状态为refunded，无实际退款操作

## 11. 幂等性分析

### 11.1 各回调幂等保护机制

| 回调 | 幂等保护 | 实现位置 |
|------|----------|----------|
| Stripe Webhook | ✅ 检查paymentStatus != "paid" && != "refunded" | PaymentServiceImpl:244 |
| 微信回调 | ✅ 检查payment_orders.status == "pending"才更新 | WechatPaymentProvider:177 |
| 支付宝回调 | ✅ 检查payment_orders.status == "pending"才更新 | AlipayPaymentProvider:209 |
| syncRegisterUserByOrderNo | ✅ 检查payment_orders.status == "paid" + registerUser != "paid"/"refunded" | PaymentServiceImpl:206-210 |

### 11.2 幂等保护层级

```
第一层: payment_orders.status == "pending" (Provider层)
  → 只有pending状态才更新为paid，已paid的不会重复更新

第二层: register_user_information.payment_status != "paid" && != "refunded" (Service层)
  → 即使payment_orders已更新，register_user也不会重复更新

第三层: 事务保护 (@Transactional on Service方法)
  → 保证payment_orders和register_user更新的原子性(仅路径B)
```

**⚠️ 幂等风险**：路径A(Controller直接处理)没有@Transactional，Provider内部更新payment_orders和后续syncRegisterUserByOrderNo不在同一事务中

## 12. 常见问题排查

### 12.1 Stripe支付失败

1. 检查`stripe.api-key`和`stripe.publishable-key`配置
2. 检查`stripe.webhook-secret`是否匹配
3. 查看Webhook日志: `payment_intent.succeeded`事件是否收到
4. 检查metadata中的`registration_id`是否正确
5. SQL排查:
```sql
SELECT id, payment_method, payment_status, stripe_payment_intent_id, payment_amount
FROM register_user_information
WHERE stripe_payment_intent_id = 'pi_xxx';
```

### 12.2 微信/支付宝回调未收到

1. 检查`notify-url`是否可从公网访问
2. 检查回调端点是否返回200/成功标识
3. 微信: 检查`api-v3-key`和证书配置
4. 支付宝: 检查`alipay-public-key`是否正确(用于验签)
5. 检查回调Controller日志:
   - AlipayCallbackController: "支付宝支付成功" 或 "支付宝回调验签失败"
   - WechatCallbackController: "微信支付成功" 或 "微信支付回调处理异常"
6. SQL排查:
```sql
-- 检查订单状态
SELECT order_no, status, transaction_id, paid_time
FROM payment_orders
WHERE order_no LIKE 'WXP%' OR order_no LIKE 'ALI%';

-- 检查用户支付状态
SELECT id, payment_method, payment_status, payment_amount
FROM register_user_information
WHERE id = {registrationId};
```

### 12.3 支付状态未同步

1. 检查`payment_orders`表的`status`是否已更新为`paid`
2. 检查`syncRegisterUserByOrderNo`是否执行
3. 检查`register_user_information.payment_status`
4. **关键**：Stripe不使用payment_orders，直接更新register_user_information
5. SQL排查:
```sql
-- 检查双表同步状态
SELECT po.order_no, po.status AS order_status, po.paid_time,
       rui.payment_status AS user_payment_status
FROM payment_orders po
LEFT JOIN register_user_information rui ON po.registration_id = rui.id
WHERE po.order_no = '{orderNo}';
```

### 12.4 银行转账确认

1. 仅管理员可操作: 需要`sys:registerUser:update`权限
2. 确认前检查: `paymentMethod == "bank_transfer"` 且 `paymentStatus != "paid"`
3. 确认后: `payment_status`直接更新为`"paid"`(无三方验证)
4. 银行转账不创建payment_orders记录

### 12.5 退款失败

1. 检查register_user_information.payment_status是否为"paid"
2. Stripe退款: 检查stripe_payment_intent_id是否有效
3. 微信/支付宝退款: 检查payment_orders.status是否为"paid"
4. SQL排查:
```sql
SELECT id, payment_method, payment_status, stripe_payment_intent_id, payment_amount
FROM register_user_information
WHERE id = {registrationId};

SELECT order_no, status, transaction_id, amount
FROM payment_orders
WHERE registration_id = {registrationId};
```

### 12.6 订单号重复或冲突

1. 订单号格式: WXP/ALI/BT + timestamp + (registrationId后4位)
2. 同一毫秒内同一用户创建多个订单可能冲突
3. SQL排查:
```sql
SELECT order_no, COUNT(*) AS cnt
FROM payment_orders
GROUP BY order_no
HAVING cnt > 1;
```

### 12.7 支付宝验签失败

1. 检查`alipay.alipay-public-key`是否正确
2. 支付宝公钥格式：需为PKCS#8或X509格式
3. 当前实现使用`X509EncodedKeySpec`解析，如公钥为裸Base64可能失败
4. 检查`sign_type`是否为RSA2(SHA256withRSA)
5. 查看日志: "支付宝回调验签失败"

## 13. 已知问题

| 编号 | 严重程度 | 描述 | 位置 |
|------|----------|------|------|
| PAY-001 | 🟡中 | StripePaymentProvider注入了BankTransferProperties(未使用) | StripePaymentProvider:22 |
| PAY-002 | 🟠中高 | Controller回调路径无@Transactional，可能导致双表状态不一致 | AlipayCallbackController/WechatCallbackController |
| PAY-003 | 🟡中 | 退款仅检查stripePaymentIntentId，不检查其他支付方式的payment_orders | PaymentServiceImpl:160 |
| PAY-004 | 🟡中 | 订单号使用timestamp生成，高并发下可能冲突 | WechatPaymentProvider:55, AlipayPaymentProvider:45 |
| PAY-005 | 🟢低 | PaymentService.handleAlipayNotify/handleWechatNotify未被Controller调用(冗余方法) | PaymentServiceImpl:172, :182 |
| PAY-006 | 🟢低 | 支付宝验签使用X509EncodedKeySpec，公钥格式可能不兼容 | AlipayPaymentProvider:196-206 |