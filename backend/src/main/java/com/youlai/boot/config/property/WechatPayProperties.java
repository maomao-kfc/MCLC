package com.youlai.boot.config.property;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "wechat-pay")
@Data
public class WechatPayProperties {
    private String appId;
    private String mchId;
    private String apiV3Key;
    private String privateKeyPath;
    private String mchSerialNo;
    private String platformCertPath;
    private String notifyUrl;
    private String refundNotifyUrl;
}
