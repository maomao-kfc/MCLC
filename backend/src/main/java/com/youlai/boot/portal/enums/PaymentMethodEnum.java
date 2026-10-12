package com.youlai.boot.portal.enums;

import lombok.Getter;

@Getter
public enum PaymentMethodEnum {

    CREDIT_CARD("credit_card", "信用卡"),
    WECHAT("wechat", "微信支付"),
    ALIPAY("alipay", "支付宝"),
    BANK_TRANSFER("bank_transfer", "银行转账");

    private final String code;
    private final String label;

    PaymentMethodEnum(String code, String label) {
        this.code = code;
        this.label = label;
    }

    public static PaymentMethodEnum fromCode(String code) {
        for (PaymentMethodEnum item : values()) {
            if (item.code.equals(code)) {
                return item;
            }
        }
        return null;
    }
}
