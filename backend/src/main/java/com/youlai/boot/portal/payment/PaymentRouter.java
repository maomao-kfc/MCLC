package com.youlai.boot.portal.payment;

import cn.hutool.core.util.StrUtil;
import com.youlai.boot.portal.enums.PaymentMethodEnum;
import com.youlai.boot.portal.payment.impl.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * 支付路由器 — 策略模式核心
 *
 * 根据支付方式(paymentMethod)或回调类型(callbackType)路由到对应的PaymentProvider实现。
 * Spring自动注入所有PaymentProvider实现到providers列表，无需手动注册。
 *
 * 路由规则：
 * - route(paymentMethod): 按PaymentMethodEnum匹配provider.supportedMethod()
 * - routeByCallback(callbackType): 按回调URL路径参数匹配provider.supportedMethod().getCode()
 *
 * 支持的Provider：
 * - StripePaymentProvider  → credit_card (Stripe API)
 * - WechatPaymentProvider  → wechat (微信支付V3 API)
 * - AlipayPaymentProvider  → alipay (支付宝API)
 * - BankTransferProvider   → bank_transfer (线下转账，无API调用)
 *
 * @see PaymentProvider
 * @see PaymentMethodEnum
 */
@Component
@RequiredArgsConstructor
public class PaymentRouter {

    private final List<PaymentProvider> providers;

    public PaymentProvider route(String paymentMethod) {
        PaymentMethodEnum method = PaymentMethodEnum.fromCode(paymentMethod);
        if (method == null) {
            throw new IllegalArgumentException("不支持的支付方式: " + paymentMethod);
        }
        for (PaymentProvider provider : providers) {
            if (provider.supportedMethod() == method) {
                return provider;
            }
        }
        throw new IllegalArgumentException("未找到支付Provider: " + paymentMethod);
    }

    public PaymentProvider routeByCallback(String callbackType) {
        for (PaymentProvider provider : providers) {
            if (StrUtil.equals(callbackType, provider.supportedMethod().getCode())) {
                return provider;
            }
        }
        throw new IllegalArgumentException("未找到回调Provider: " + callbackType);
    }
}
