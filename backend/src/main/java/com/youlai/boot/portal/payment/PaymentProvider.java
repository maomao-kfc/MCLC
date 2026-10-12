package com.youlai.boot.portal.payment;

import com.youlai.boot.portal.enums.PaymentMethodEnum;

import java.util.Map;

public interface PaymentProvider {

    PaymentProviderResponse createPayment(CreatePaymentRequest request);

    PaymentProviderResponse queryPayment(String orderNo);

    String handleCallback(Map<String, String> params);

    void refund(String orderNo);

    PaymentMethodEnum supportedMethod();
}
