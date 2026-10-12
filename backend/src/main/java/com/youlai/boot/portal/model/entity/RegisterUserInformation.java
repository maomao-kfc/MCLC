package com.youlai.boot.portal.model.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;

@TableName("register_user_information")
@Getter
@Setter
public class RegisterUserInformation implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    @TableField("connect_id")
    private Long connectId;

    @TableField(value = "team_id", exist = false)
    private Long teamId;

    private String name;

    private String surname;

    private String email;

    private String organization;

    @TableField("mobile_phone")
    private String mobilePhone;

    private String street;

    @TableField("postal_code")
    private String postalCode;

    private Integer status;

    private String country;

    @TableField("bill_organization")
    private String billOrganization;

    @TableField("bill_vat")
    private String billVat;

    @TableField("bill_procurement")
    private String billProcurement;

    @TableField("bill_street")
    private String billStreet;

    @TableField("bill_postal_code")
    private String billPostalCode;

    @TableField("bill_city")
    private String billCity;

    @TableField("bill_status")
    private Integer billStatus;

    @TableField("bill_country")
    private String billCountry;

    @TableField("registration_type")
    private String registrationType;

    @TableField("registration_fee")
    private String registrationFee;

    @TableField("registration_date")
    private String registrationDate;

    @TableField("payment_method")
    private String paymentMethod;

    @TableField("payment_status")
    private String paymentStatus;

    @TableField("stripe_payment_intent_id")
    private String stripePaymentIntentId;

    @TableField("payment_amount")
    private String paymentAmount;

    @TableField("visa_name")
    private String visaName;

    @TableField("visa_surname")
    private String visaSurname;

    @TableField("visa_passport")
    private String visaPassport;

    @TableField("visa_issue_time")
    private String visaIssueTime;

    @TableField("visa_end_time")
    private String visaEndTime;

    @TableField("visa_time")
    private String visaTime;

    @TableField("visa_area")
    private String visaArea;

    @TableField("visa_is_need")
    private Integer visaIsNeed;

    @TableField("deduction_code")
    private String deductionCode;

    @TableField("pic_bank")
    private String picBank;

    @TableField("actual_payment")
    private String actualPayment;

    @TableField("register_label")
    private String registerLabel;

    @TableField("review_status")
    private Integer reviewStatus;

    @TableField("reject_reason")
    private String rejectReason;
}
