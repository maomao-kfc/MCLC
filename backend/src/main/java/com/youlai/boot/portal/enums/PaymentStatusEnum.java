package com.youlai.boot.portal.enums;

import lombok.Getter;

@Getter
public enum PaymentStatusEnum {

    PENDING("pending", "待支付"),
    PAID("paid", "已支付"),
    REFUNDED("refunded", "已退款");

    private final String code;
    private final String label;

    PaymentStatusEnum(String code, String label) {
        this.code = code;
        this.label = label;
    }
}
