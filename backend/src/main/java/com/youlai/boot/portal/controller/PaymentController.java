package com.youlai.boot.portal.controller;

import com.youlai.boot.config.property.StripeProperties;
import com.youlai.boot.core.web.Result;
import com.youlai.boot.portal.model.form.PaymentIntentForm;
import com.youlai.boot.portal.model.vo.PaymentIntentResponse;
import com.youlai.boot.portal.service.PaymentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@Tag(name = "支付接口")
@RestController
@RequestMapping("/api/v1/payment")
@RequiredArgsConstructor
public class PaymentController {

    private final PaymentService paymentService;
    private final StripeProperties stripeProperties;

    @Operation(summary = "创建支付意图（信用卡/微信/支付宝/银行转账）")
    @PostMapping("/create-intent")
    public Result<PaymentIntentResponse> createPaymentIntent(@Valid @RequestBody PaymentIntentForm form) {
        PaymentIntentResponse response = paymentService.createPaymentIntent(form);
        return Result.success(response);
    }

    @Operation(summary = "查询支付状态")
    @GetMapping("/status/{registrationId}")
    public Result<PaymentIntentResponse> getPaymentStatus(
            @Parameter(description = "注册用户ID") @PathVariable Long registrationId) {
        PaymentIntentResponse response = paymentService.getPaymentStatus(registrationId);
        return Result.success(response);
    }

    @Operation(summary = "获取银行转账信息")
    @GetMapping("/bank-info")
    public Result<Map<String, String>> getBankTransferInfo() {
        return Result.success(paymentService.getBankTransferInfo());
    }

    @Operation(summary = "确认银行转账到账")
    @PostMapping("/confirm/{registrationId}")
    @PreAuthorize("@ss.hasPerm('sys:registerUser:update')")
    public Result<Void> confirmBankTransfer(
            @Parameter(description = "注册用户ID") @PathVariable Long registrationId) {
        paymentService.confirmBankTransfer(registrationId);
        return Result.success();
    }

    @Operation(summary = "退款")
    @PostMapping("/refund/{registrationId}")
    @PreAuthorize("@ss.hasPerm('sys:registerUser:update')")
    public Result<Void> refund(
            @Parameter(description = "注册用户ID") @PathVariable Long registrationId) {
        paymentService.refund(registrationId);
        return Result.success();
    }

    @Operation(summary = "获取Stripe发布密钥")
    @GetMapping("/config")
    public Result<Map<String, String>> getPaymentConfig() {
        return Result.success(Map.of("publishableKey", stripeProperties.getPublishableKey()));
    }
}
