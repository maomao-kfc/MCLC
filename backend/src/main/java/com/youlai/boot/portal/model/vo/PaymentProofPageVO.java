package com.youlai.boot.portal.model.vo;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 支付凭证审核列表 VO（管理端）
 */
@Data
public class PaymentProofPageVO {

    private Long id;

    private Long registrationId;

    /** 报名人姓名 */
    private String name;

    /** 报名人姓氏 */
    private String surname;

    /** 报名人邮箱 */
    private String email;

    /** 报名人手机号 */
    private String mobilePhone;

    /** 注册类型 */
    private String registerLabel;

    /** 应付金额 */
    private String paymentAmount;

    /** 实付金额 */
    private String actualPayment;

    /** 凭证文件URL */
    private String fileUrl;

    /** 用户声明的支付时间 */
    private LocalDateTime declaredPayTime;

    /** 用户声明的支付金额 */
    private BigDecimal declaredAmount;

    /** 用户声明的交易号 */
    private String declaredTradeNo;

    /** 用户备注 */
    private String remark;

    /** OCR识别金额 */
    private String ocrAmount;

    /** OCR识别收款方 */
    private String ocrPayee;

    /** OCR识别交易单号 */
    private String ocrTradeNo;

    /** OCR识别商户单号 */
    private String ocrMerchantNo;

    /** 金额比对结果: 0=不一致, 1=一致, null=未比对 */
    private Integer amountMatch;

    /** 凭证状态: proof_submitted/review_passed/rejected/reconciled */
    private String status;

    /** 审核人姓名 */
    private String reviewerName;

    /** 驳回原因 */
    private String rejectReason;

    /** 提交时间 */
    private LocalDateTime createTime;

    /** 审核时间 */
    private LocalDateTime reviewTime;
}