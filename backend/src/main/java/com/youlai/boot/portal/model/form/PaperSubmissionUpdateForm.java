package com.youlai.boot.portal.model.form;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 投稿者修改投稿信息表单（仅允许修改部分字段）
 *
 * 不可修改的字段：name、surname、contactEmail（作为身份标识）
 * 仅 paperStatus=1（待审核）时允许修改
 *
 * @author 243269112
 * @since 2026/7/31
 */
@Schema(description = "投稿者修改投稿信息表单")
@Data
public class PaperSubmissionUpdateForm {

    @Schema(description = "投稿记录ID")
    @NotNull(message = "投稿记录ID不能为空")
    private Long id;

    @Schema(description = "联系邮箱（用于身份验证）")
    @NotBlank(message = "联系邮箱不能为空")
    private String contactEmail;

    @Schema(description = "所属单位/医院")
    private String company;

    @Schema(description = "论文标题")
    @NotBlank(message = "论文标题不能为空")
    private String paperTitle;

    @Schema(description = "第一作者")
    @NotBlank(message = "第一作者不能为空")
    private String firstAuthor;

    @Schema(description = "联系电话")
    private String contactNumber;

    @Schema(description = "论文摘要")
    private String paperAbstract;

    @Schema(description = "关键词")
    private String keywords;

    @Schema(description = "论文附件")
    private String appendix;
}