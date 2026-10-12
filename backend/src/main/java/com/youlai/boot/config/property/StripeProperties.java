package com.youlai.boot.config.property;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "stripe")
@Data
public class StripeProperties {

    private String apiKey;

    private String publishableKey;

    private String webhookSecret;
}
