package com.youlai.boot.portal.model.form;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Schema(description = "论文投稿表单对象")
@Data
public class PaperSubmissionForm {

    @Schema(description = "ID")
    private Long id;

    @Schema(description = "投稿人姓名")
    private String name;

    @Schema(description = "所属单位/医院")
    private String company;

    @Schema(description = "姓")
    private String surname;

    @Schema(description = "论文标题")
    @NotBlank(message = "论文标题不能为空")
    private String paperTitle;

    @Schema(description = "第一作者")
    @NotBlank(message = "第一作者不能为空")
    private String firstAuthor;

    @Schema(description = "联系电话")
    private String contactNumber;

    @Schema(description = "联系邮箱")
    private String contactEmail;

    @Schema(description = "论文摘要")
    private String paperAbstract;

    @Schema(description = "关键词")
    private String keywords;

    @Schema(description = "论文附件")
    private String appendix;

    @Schema(description = "投稿Id")
    private Long contributionId;

    @Schema(description = "拒绝原因")
    private String rejectReason;
}
