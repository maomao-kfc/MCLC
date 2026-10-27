package com.youlai.boot.portal.model.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 对账批次实体
 * <p>每次导入协会对账Excel生成一个批次</p>
 */
@TableName("reconciliation_batch")
@Getter
@Setter
public class ReconciliationBatch implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    @TableField("batch_no")
    private String batchNo;

    @TableField("file_name")
    private String fileName;

    @TableField("total_count")
    private Integer totalCount;

    @TableField("matched_count")
    private Integer matchedCount;

    @TableField("unmatched_count")
    private Integer unmatchedCount;

    private String status;

    @TableField("operator_id")
    private Long operatorId;

    @TableField("operator_name")
    private String operatorName;

    @TableField("create_time")
    private LocalDateTime createTime;
}