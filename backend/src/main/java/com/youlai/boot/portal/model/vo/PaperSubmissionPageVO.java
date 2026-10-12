package com.youlai.boot.portal.model.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Schema(description = "论文投稿分页对象")
@Data
public class PaperSubmissionPageVO {

    @Schema(description = "ID")
    private Long id;

    @Schema(description = "投稿人姓名")
    private String name;

    @Schema(description = "所属单位/医院")
    private String company;

    @Schema(description = "姓")
    private String surname;

    @Schema(description = "论文标题")
    private String paperTitle;

    @Schema(description = "第一作者")
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

    @Schema(description = "审核状态(1-待审核 2-审核通过 3-审核拒绝)")
    private Integer paperStatus;

    @Schema(description = "拒绝原因")
    private String rejectReason;

    @Schema(description = "邮件发送状态(0-未发送 1-发送失败 2-已发送)")
    private Integer emailStatus;

    @Schema(description = "抵扣状态")
    private Integer deductionStatus;

    @Schema(description = "抵扣码")
    private String discountCode;
}
