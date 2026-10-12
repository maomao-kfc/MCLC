package com.youlai.boot.portal.service;

import com.youlai.boot.portal.model.form.PaymentIntentForm;
import com.youlai.boot.portal.model.vo.PaymentIntentResponse;

import java.util.Map;

public interface PaymentService {

    PaymentIntentResponse createPaymentIntent(PaymentIntentForm form);

    PaymentIntentResponse getPaymentStatus(Long registrationId);

    Map<String, String> getBankTransferInfo();

    void confirmBankTransfer(Long registrationId);

    void refund(Long registrationId);

    void handleStripeWebhook(String payload, String sigHeader);

    void handleAlipayNotify(Map<String, String> params);

    void handleWechatNotify(String body, String wechatpaySignature, String wechatpayNonce,
                            String wechatpayTimestamp, String wechatpaySerial);

    void syncRegisterUserByOrderNo(String orderNo);
}
