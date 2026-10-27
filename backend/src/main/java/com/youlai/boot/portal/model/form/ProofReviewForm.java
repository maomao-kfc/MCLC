package com.youlai.boot.portal.model.form;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 管理端凭证审核表单
 */
@Data
public class ProofReviewForm {

    @NotNull(message = "凭证ID不能为空")
    private Long proofId;

    /** 审核动作: pass=通过(待对账确认), reject=驳回 */
    @NotBlank(message = "审核动作不能为空")
    private String action;

    /** 驳回原因（action=reject时必填） */
    private String rejectReason;
}