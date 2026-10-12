package com.youlai.boot.portal.payment;

import lombok.Builder;
import lombok.Data;

import java.util.Map;

@Data
@Builder
public class PaymentProviderResponse {
    private String orderNo;
    private String transactionId;
    private String qrCodeUrl;
    private String payUrl;
    private String paymentStatus;
    private String amount;
    private Map<String, String> extraData;
}
