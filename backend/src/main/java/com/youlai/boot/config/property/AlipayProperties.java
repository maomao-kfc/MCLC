package com.youlai.boot.config.property;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "alipay")
@Data
public class AlipayProperties {
    private String appId;
    private String privateKey;
    private String alipayPublicKey;
    private String notifyUrl;
    private String returnUrl;
    private String gatewayUrl = "https://openapi.alipay.com/gateway.do";
    private String signType = "RSA2";
    private String charset = "UTF-8";
    private String format = "json";
}
