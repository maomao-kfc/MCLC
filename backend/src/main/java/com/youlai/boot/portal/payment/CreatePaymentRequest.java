package com.youlai.boot.portal.payment;

import com.youlai.boot.portal.model.entity.RegisterUserInformation;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Builder
public class CreatePaymentRequest {
    private Long registrationId;
    private String paymentMethod;
    private BigDecimal amount;
    private String currency;
    private String scene;
    private String returnUrl;
    private RegisterUserInformation registerUser;
}
