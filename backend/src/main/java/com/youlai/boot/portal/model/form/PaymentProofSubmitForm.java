package com.youlai.boot.portal.model.form;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 用户提交支付凭证表单
 */
@Data
public class PaymentProofSubmitForm {

    @NotNull(message = "注册用户ID不能为空")
    private Long registrationId;

    /** 用户声明的支付时间 */
    @NotNull(message = "支付时间不能为空")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime declaredPayTime;

    /** 用户声明的支付金额 */
    private BigDecimal declaredAmount;

    /** 用户声明的交易号（选填，如有则有助于对账匹配） */
    private String declaredTradeNo;

    /** 备注 */
    private String remark;
}