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
 * 对账明细实体
 * <p>协会对账Excel每行对应一条记录，与系统注册用户做多维度匹配</p>
 */
@TableName("reconciliation_record")
@Getter
@Setter
public class ReconciliationRecord implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    @TableField("batch_id")
    private Long batchId;

    @TableField("raw_name")
    private String rawName;

    @TableField("raw_phone")
    private String rawPhone;

    @TableField("raw_amount")
    private BigDecimal rawAmount;

    @TableField("raw_pay_time")
    private LocalDateTime rawPayTime;

    @TableField("raw_trade_no")
    private String rawTradeNo;

    @TableField("raw_remark")
    private String rawRemark;

    @TableField("matched_registration_id")
    private Long matchedRegistrationId;

    @TableField("match_score")
    private Integer matchScore;

    @TableField("match_detail")
    private String matchDetail;

    /** 匹配状态: auto_matched=自动匹配, manual_matched=人工确认, unmatched=未匹配, rejected=驳回 */
    @TableField("match_status")
    private String matchStatus;

    @TableField("operator_id")
    private Long operatorId;

    @TableField("create_time")
    private LocalDateTime createTime;
}