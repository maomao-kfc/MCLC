package com.youlai.boot.portal.payment.impl;

import com.stripe.model.PaymentIntent;
import com.stripe.param.PaymentIntentCreateParams;
import com.youlai.boot.config.property.BankTransferProperties;
import com.youlai.boot.portal.enums.PaymentMethodEnum;
import com.youlai.boot.portal.payment.CreatePaymentRequest;
import com.youlai.boot.portal.payment.PaymentProvider;
import com.youlai.boot.portal.payment.PaymentProviderResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.Map;

@Slf4j
@Component
@RequiredArgsConstructor
public class StripePaymentProvider implements PaymentProvider {

    private final BankTransferProperties bankTransferProperties;

    @Override
    public PaymentMethodEnum supportedMethod() {
        return PaymentMethodEnum.CREDIT_CARD;
    }

    @Override
    public PaymentProviderResponse createPayment(CreatePaymentRequest request) {
        try {
            PaymentIntentCreateParams.Builder paramsBuilder = PaymentIntentCreateParams.builder()
                    .setAmount(request.getAmount().multiply(new BigDecimal("100")).longValue())
                    .setCurrency(request.getCurrency())
                    .setDescription("Registration fee");
            if (request.getRegistrationId() != null) {
                paramsBuilder.putMetadata("registration_id", String.valueOf(request.getRegistrationId()));
            }
            paramsBuilder.addPaymentMethodType("card");

            PaymentIntent intent = PaymentIntent.create(paramsBuilder.build());

            return PaymentProviderResponse.builder()
                    .orderNo(intent.getId())
                    .paymentStatus("pending")
                    .amount(request.getAmount().toPlainString())
                    .extraData(java.util.Map.of("clientSecret", intent.getClientSecret()))
                    .build();
        } catch (Exception e) {
            log.error("Stripe创建支付失败", e);
            throw new RuntimeException("创建支付失败: " + e.getMessage());
        }
    }

    @Override
    public PaymentProviderResponse queryPayment(String orderNo) {
        try {
            PaymentIntent intent = PaymentIntent.retrieve(orderNo);
            return PaymentProviderResponse.builder()
                    .orderNo(orderNo)
                    .paymentStatus(intent.getStatus())
                    .build();
        } catch (Exception e) {
            log.error("Stripe查询支付失败", e);
            throw new RuntimeException("查询支付失败: " + e.getMessage());
        }
    }

    @Override
    public String handleCallback(Map<String, String> params) {
        return null;
    }

    @Override
    public void refund(String orderNo) {
        try {
            PaymentIntent intent = PaymentIntent.retrieve(orderNo);
            intent.cancel();
            log.info("Stripe退款成功: paymentIntentId={}", orderNo);
        } catch (Exception e) {
            log.error("Stripe退款失败", e);
            throw new RuntimeException("退款失败: " + e.getMessage());
        }
    }
}
