package com.youlai.boot.portal.model.form;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.math.BigDecimal;

@Schema(description = "支付意图表单")
@Data
public class PaymentIntentForm {

    @Schema(description = "注册用户ID（非必填，不传则创建匿名支付意图）")
    private Long registrationId;

    @Schema(description = "付款方式：credit_card/wechat/alipay/bank_transfer")
    @NotBlank(message = "付款方式不能为空")
    private String paymentMethod;

    @Schema(description = "支付金额（若不传registrationId则必填）")
    private BigDecimal amount;

    @Schema(description = "货币，默认eur")
    private String currency = "eur";

    @Schema(description = "支付场景：scan(扫码付)/h5(手机H5)，默认scan")
    private String scene = "scan";

    @Schema(description = "H5支付完成后的回跳地址")
    private String returnUrl;
}
