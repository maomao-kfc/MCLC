package com.youlai.boot.config;

import com.stripe.Stripe;
import com.youlai.boot.config.property.BankTransferProperties;
import com.youlai.boot.config.property.StripeProperties;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableConfigurationProperties({StripeProperties.class, BankTransferProperties.class})
@RequiredArgsConstructor
public class StripeConfig {

    private final StripeProperties stripeProperties;

    @PostConstruct
    public void init() {
        Stripe.apiKey = stripeProperties.getApiKey();
    }
}
