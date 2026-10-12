package com.youlai.boot.portal.payment.impl;

import cn.hutool.core.util.StrUtil;
import com.youlai.boot.config.property.BankTransferProperties;
import com.youlai.boot.portal.enums.PaymentMethodEnum;
import com.youlai.boot.portal.payment.CreatePaymentRequest;
import com.youlai.boot.portal.payment.PaymentProvider;
import com.youlai.boot.portal.payment.PaymentProviderResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.LinkedHashMap;
import java.util.Map;

@Slf4j
@Component
@RequiredArgsConstructor
public class BankTransferPaymentProvider implements PaymentProvider {

    private final BankTransferProperties bankTransferProperties;

    @Override
    public PaymentMethodEnum supportedMethod() {
        return PaymentMethodEnum.BANK_TRANSFER;
    }

    @Override
    public PaymentProviderResponse createPayment(CreatePaymentRequest request) {
        return PaymentProviderResponse.builder()
                .orderNo("BT" + System.currentTimeMillis())
                .paymentStatus("pending")
                .amount(request.getAmount().toPlainString())
                .extraData(buildBankInfo(request.getRegistrationId()))
                .build();
    }

    @Override
    public PaymentProviderResponse queryPayment(String orderNo) {
        return PaymentProviderResponse.builder()
                .orderNo(orderNo)
                .paymentStatus("pending")
                .build();
    }

    @Override
    public String handleCallback(Map<String, String> params) {
        return null;
    }

    @Override
    public void refund(String orderNo) {
        log.info("银行转账退款需线下处理: orderNo={}", orderNo);
    }

    private Map<String, String> buildBankInfo(Long registrationId) {
        Map<String, String> info = new LinkedHashMap<>();
        info.put("bankName", bankTransferProperties.getBankName());
        info.put("accountName", bankTransferProperties.getAccountName());
        info.put("accountNumber", bankTransferProperties.getAccountNumber());
        info.put("iban", bankTransferProperties.getIban());
        info.put("swiftCode", bankTransferProperties.getSwiftCode());
        info.put("bankAddress", bankTransferProperties.getBankAddress());
        String ref = registrationId != null ? String.valueOf(registrationId) : "ANONYMOUS";
        info.put("reference", StrUtil.format(
                bankTransferProperties.getReferenceFormat() != null ? bankTransferProperties.getReferenceFormat() : "{}",
                ref));
        return info;
    }
}
