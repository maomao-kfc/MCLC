package com.youlai.boot.portal.model.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 支付凭证记录实体
 * <p>每次用户上传凭证截图生成一条记录，支持驳回后重传（多条记录）</p>
 */
@TableName("payment_proof")
@Getter
@Setter
public class PaymentProof implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    @TableField("registration_id")
    private Long registrationId;

    @TableField("user_id")
    private Long userId;

    /** 带水印的凭证文件URL */
    @TableField("file_url")
    private String fileUrl;

    @TableField("file_md5")
    private String fileMd5;

    @TableField("file_phash")
    private String filePhash;

    @TableField("declared_pay_time")
    private LocalDateTime declaredPayTime;

    @TableField("declared_amount")
    private BigDecimal declaredAmount;

    @TableField("declared_trade_no")
    private String declaredTradeNo;

    private String remark;

    @TableField("ocr_amount")
    private String ocrAmount;

    @TableField("ocr_payee")
    private String ocrPayee;

    @TableField("ocr_pay_time")
    private String ocrPayTime;

    @TableField("ocr_trade_no")
    private String ocrTradeNo;

    @TableField("ocr_raw_text")
    private String ocrRawText;

    /** OCR状态: 0=未识别, 1=已识别, 2=识别失败 */
    @TableField("ocr_status")
    private Integer ocrStatus;

    /** 金额比对结果: 0=不一致, 1=一致, NULL=未比对 */
    @TableField("amount_match")
    private Integer amountMatch;

    /** 凭证状态: proof_submitted=待核验, review_passed=初审通过, rejected=已驳回, reconciled=对账确认 */
    private String status;

    @TableField("review_time")
    private LocalDateTime reviewTime;

    @TableField("reviewer_id")
    private Long reviewerId;

    @TableField("reviewer_name")
    private String reviewerName;

    @TableField("reject_reason")
    private String rejectReason;

    @TableField("create_time")
    private LocalDateTime createTime;

    @TableField("update_time")
    private LocalDateTime updateTime;
}