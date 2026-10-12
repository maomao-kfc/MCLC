package com.youlai.boot.portal.model.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.time.LocalDateTime;

@TableName("payment_orders")
@Getter
@Setter
public class PaymentOrder implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    @TableField("registration_id")
    private Long registrationId;

    @TableField("payment_method")
    private String paymentMethod;

    @TableField("order_no")
    private String orderNo;

    @TableField("transaction_id")
    private String transactionId;

    private String amount;

    private String currency;

    private String status;

    @TableField("qr_code_url")
    private String qrCodeUrl;

    @TableField("pay_url")
    private String payUrl;

    @TableField("extra_data")
    private String extraData;

    @TableField("expire_time")
    private LocalDateTime expireTime;

    @TableField("paid_time")
    private LocalDateTime paidTime;

    @TableField("create_time")
    private LocalDateTime createTime;

    @TableField("update_time")
    private LocalDateTime updateTime;
}
