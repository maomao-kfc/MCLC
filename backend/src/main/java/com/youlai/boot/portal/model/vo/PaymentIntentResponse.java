package com.youlai.boot.portal.model.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;

import java.util.Map;

@Schema(description = "支付意图响应")
@Data
@Builder
public class PaymentIntentResponse {

    @Schema(description = "注册用户ID")
    private Long registrationId;

    @Schema(description = "商户订单号")
    private String orderNo;

    @Schema(description = "Stripe PaymentIntent clientSecret（信用卡支付时使用）")
    private String clientSecret;

    @Schema(description = "二维码链接（微信/支付宝扫码付时使用）")
    private String qrCodeUrl;

    @Schema(description = "支付跳转链接（微信/支付宝H5支付时使用）")
    private String payUrl;

    @Schema(description = "付款方式")
    private String paymentMethod;

    @Schema(description = "支付状态")
    private String paymentStatus;

    @Schema(description = "支付金额")
    private String amount;

    @Schema(description = "银行转账信息（付款方式为bank_transfer时返回）")
    private Map<String, String> bankTransferInfo;
}
