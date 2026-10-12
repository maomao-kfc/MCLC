package com.youlai.boot.portal.service.impl;

import cn.hutool.core.lang.Assert;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.stripe.exception.SignatureVerificationException;
import com.stripe.model.Event;
import com.stripe.model.PaymentIntent;
import com.stripe.net.Webhook;
import com.youlai.boot.config.property.BankTransferProperties;
import com.youlai.boot.config.property.StripeProperties;
import com.youlai.boot.portal.mapper.PaymentOrderMapper;
import com.youlai.boot.portal.model.entity.PaymentOrder;
import com.youlai.boot.portal.model.entity.RegisterUserInformation;
import com.youlai.boot.portal.model.form.PaymentIntentForm;
import com.youlai.boot.portal.model.vo.PaymentIntentResponse;
import com.youlai.boot.portal.payment.CreatePaymentRequest;
import com.youlai.boot.portal.payment.PaymentProvider;
import com.youlai.boot.portal.payment.PaymentProviderResponse;
import com.youlai.boot.portal.payment.PaymentRouter;
import com.youlai.boot.portal.service.PaymentService;
import com.youlai.boot.portal.service.RegisterUserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * 支付服务实现类
 *
 * 核心业务流程：
 * 1. 创建支付意图 — createPaymentIntent():44, @Transactional
 *    支持注册用户支付(从register_user读取金额)和匿名支付(需指定金额)
 *    通过PaymentRouter路由到对应Provider创建支付
 * 2. 处理Stripe回调 — handleStripeWebhook():处理Event→PaymentIntent→更新payment_order状态
 * 3. 处理通用回调结果 — handleCallbackResult():更新payment_order+register_user支付状态+WebSocket通知
 * 4. 退款 — refund():仅检查stripePaymentIntentId调用Stripe退款(⚠PAY-002:未路由到其他Provider)
 * 5. 查询支付状态 — getPaymentStatus():按registrationId查询payment_order
 *
 * 依赖注入(5个)：
 * - registerUserService, stripeProperties, bankTransferProperties
 * - paymentRouter, paymentOrderMapper
 *
 * 已知问题：PAY-002(退款仅检查Stripe) / PAY-001(StripeProvider注入未使用的BankTransferProperties)
 *
 * 技术文档详见：docs/06-payment-subsystem.md
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class PaymentServiceImpl implements PaymentService {

    private final RegisterUserService registerUserService;
    private final StripeProperties stripeProperties;
    private final BankTransferProperties bankTransferProperties;
    private final PaymentRouter paymentRouter;
    private final PaymentOrderMapper paymentOrderMapper;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public PaymentIntentResponse createPaymentIntent(PaymentIntentForm form) {
        RegisterUserInformation registerUser = null;
        BigDecimal amount = form.getAmount();

        if (form.getRegistrationId() != null) {
            registerUser = registerUserService.getById(form.getRegistrationId());
            Assert.isTrue(registerUser != null, "注册用户不存在");

            if (amount == null) {
                Assert.isTrue(StrUtil.isNotBlank(registerUser.getPaymentAmount()), "注册用户未设置支付金额");
                amount = new BigDecimal(registerUser.getPaymentAmount());
            }
        } else {
            Assert.isTrue(amount != null, "匿名支付时支付金额不能为空");
        }

        PaymentProvider provider = paymentRouter.route(form.getPaymentMethod());

        CreatePaymentRequest request = CreatePaymentRequest.builder()
                .registrationId(form.getRegistrationId())
                .paymentMethod(form.getPaymentMethod())
                .amount(amount)
                .currency(form.getCurrency())
                .scene(form.getScene())
                .returnUrl(form.getReturnUrl())
                .registerUser(registerUser)
                .build();

        if (registerUser != null) {
            registerUser.setPaymentMethod(form.getPaymentMethod());
            registerUser.setPaymentAmount(amount.toPlainString());
            registerUserService.updateById(registerUser);
        }

        PaymentProviderResponse providerResponse = provider.createPayment(request);

        PaymentIntentResponse.PaymentIntentResponseBuilder builder = PaymentIntentResponse.builder()
                .registrationId(form.getRegistrationId())
                .orderNo(providerResponse.getOrderNo())
                .paymentMethod(form.getPaymentMethod())
                .paymentStatus("pending")
                .amount(amount.toPlainString());

        if (StrUtil.isNotBlank(providerResponse.getQrCodeUrl())) {
            builder.qrCodeUrl(providerResponse.getQrCodeUrl());
        }
        if (StrUtil.isNotBlank(providerResponse.getPayUrl())) {
            builder.payUrl(providerResponse.getPayUrl());
        }
        if (providerResponse.getExtraData() != null) {
            if (providerResponse.getExtraData().containsKey("clientSecret")) {
                builder.clientSecret(providerResponse.getExtraData().get("clientSecret"));
            }
            if ("bank_transfer".equals(form.getPaymentMethod())) {
                builder.bankTransferInfo(providerResponse.getExtraData());
            }
        }

        return builder.build();
    }

    @Override
    public PaymentIntentResponse getPaymentStatus(Long registrationId) {
        RegisterUserInformation registerUser = registerUserService.getById(registrationId);
        Assert.isTrue(registerUser != null, "注册用户不存在");

        PaymentIntentResponse.PaymentIntentResponseBuilder builder = PaymentIntentResponse.builder()
                .registrationId(registerUser.getId())
                .paymentMethod(registerUser.getPaymentMethod())
                .paymentStatus(registerUser.getPaymentStatus())
                .amount(registerUser.getPaymentAmount());

        if ("bank_transfer".equals(registerUser.getPaymentMethod())) {
            builder.bankTransferInfo(buildBankTransferInfo(registerUser));
        }

        return builder.build();
    }

    @Override
    public Map<String, String> getBankTransferInfo() {
        Map<String, String> info = new LinkedHashMap<>();
        info.put("bankName", bankTransferProperties.getBankName());
        info.put("accountName", bankTransferProperties.getAccountName());
        info.put("accountNumber", bankTransferProperties.getAccountNumber());
        info.put("iban", bankTransferProperties.getIban());
        info.put("swiftCode", bankTransferProperties.getSwiftCode());
        info.put("bankAddress", bankTransferProperties.getBankAddress());
        info.put("referenceFormat", bankTransferProperties.getReferenceFormat());
        return info;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void confirmBankTransfer(Long registrationId) {
        RegisterUserInformation registerUser = registerUserService.getById(registrationId);
        Assert.isTrue(registerUser != null, "注册用户不存在");
        Assert.isTrue("bank_transfer".equals(registerUser.getPaymentMethod()), "该订单非银行转账方式");
        Assert.isTrue(!"paid".equals(registerUser.getPaymentStatus()), "该订单已支付");

        registerUser.setPaymentStatus("paid");
        registerUserService.updateById(registerUser);

        log.info("银行转账已确认：registrationId={}", registrationId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void refund(Long registrationId) {
        RegisterUserInformation registerUser = registerUserService.getById(registrationId);
        Assert.isTrue(registerUser != null, "注册用户不存在");
        Assert.isTrue("paid".equals(registerUser.getPaymentStatus()), "该订单未支付，无法退款");

        if (StrUtil.isNotBlank(registerUser.getStripePaymentIntentId())) {
            try {
                PaymentProvider provider = paymentRouter.route("credit_card");
                provider.refund(registerUser.getStripePaymentIntentId());
                log.info("Stripe退款成功：paymentIntentId={}", registerUser.getStripePaymentIntentId());
            } catch (Exception e) {
                log.error("退款失败", e);
                throw new RuntimeException("退款失败：" + e.getMessage());
            }
        }

        registerUser.setPaymentStatus("refunded");
        registerUserService.updateById(registerUser);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void handleAlipayNotify(Map<String, String> params) {
        PaymentProvider provider = paymentRouter.route("alipay");
        String orderNo = provider.handleCallback(params);

        if (orderNo != null) {
            syncRegisterUserByOrderNo(orderNo);
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void handleWechatNotify(String body, String wechatpaySignature, String wechatpayNonce,
                                   String wechatpayTimestamp, String wechatpaySerial) {
        PaymentProvider provider = paymentRouter.route("wechat");
        Map<String, String> params = new HashMap<>();
        params.put("wechatpaySignature", wechatpaySignature);
        params.put("wechatpayNonce", wechatpayNonce);
        params.put("wechatpayTimestamp", wechatpayTimestamp);
        params.put("wechatpaySerial", wechatpaySerial);
        params.put("body", body);
        String orderNo = provider.handleCallback(params);

        if (orderNo != null) {
            syncRegisterUserByOrderNo(orderNo);
        }
    }

    @Override
    public void syncRegisterUserByOrderNo(String orderNo) {
        PaymentOrder paymentOrder = paymentOrderMapper.selectOne(
                new LambdaQueryWrapper<PaymentOrder>().eq(PaymentOrder::getOrderNo, orderNo));
        if (paymentOrder == null) {
            log.warn("未找到对应订单: orderNo={}", orderNo);
            return;
        }

        if (!"paid".equals(paymentOrder.getStatus())) {
            return;
        }

        RegisterUserInformation registerUser = registerUserService.getById(paymentOrder.getRegistrationId());
        if (registerUser != null && !"paid".equals(registerUser.getPaymentStatus()) && !"refunded".equals(registerUser.getPaymentStatus())) {
            registerUser.setPaymentStatus("paid");
            registerUserService.updateById(registerUser);
            log.info("支付回调确认成功：orderNo={}, registrationId={}", orderNo, registerUser.getId());
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void handleStripeWebhook(String payload, String sigHeader) {
        try {
            Event event = Webhook.constructEvent(payload, sigHeader, stripeProperties.getWebhookSecret());

            if ("payment_intent.succeeded".equals(event.getType())) {
                PaymentIntent intent = (PaymentIntent) event.getDataObjectDeserializer().getObject().orElse(null);
                if (intent == null) {
                    log.warn("Webhook received with null PaymentIntent");
                    return;
                }

                String registrationId = intent.getMetadata().get("registration_id");
                if (StrUtil.isBlank(registrationId)) {
                    log.warn("Webhook received without registration_id metadata");
                    return;
                }

                RegisterUserInformation registerUser = registerUserService.getById(Long.valueOf(registrationId));
                if (registerUser != null && !"paid".equals(registerUser.getPaymentStatus()) && !"refunded".equals(registerUser.getPaymentStatus())) {
                    registerUser.setPaymentStatus("paid");
                    registerUserService.updateById(registerUser);
                    log.info("支付成功：registrationId={}, paymentIntentId={}", registrationId, intent.getId());
                }
            }

        } catch (SignatureVerificationException e) {
            log.error("Stripe Webhook签名验证失败", e);
            throw new RuntimeException("签名验证失败");
        } catch (Exception e) {
            log.error("处理Stripe Webhook失败", e);
            throw new RuntimeException("Webhook处理失败：" + e.getMessage());
        }
    }

    private Map<String, String> buildBankTransferInfo(RegisterUserInformation registerUser) {
        Map<String, String> info = new LinkedHashMap<>();
        info.put("bankName", bankTransferProperties.getBankName());
        info.put("accountName", bankTransferProperties.getAccountName());
        info.put("accountNumber", bankTransferProperties.getAccountNumber());
        info.put("iban", bankTransferProperties.getIban());
        info.put("swiftCode", bankTransferProperties.getSwiftCode());
        info.put("bankAddress", bankTransferProperties.getBankAddress());
        info.put("reference", StrUtil.format(
                bankTransferProperties.getReferenceFormat() != null ? bankTransferProperties.getReferenceFormat() : "{}",
                registerUser.getId()));
        return info;
    }
}
