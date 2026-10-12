package com.youlai.boot.portal.model.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 投稿查询返回VO（面向投稿者，脱敏处理）
 *
 * 与PaperSubmissionPageVO的区别：
 * - 隐藏emailStatus、deductionStatus、discountCode等内部管理字段
 *
 * @author 243269112
 * @since 2026/7/31
 */
@Schema(description = "投稿查询返回对象")
@Data
public class PaperSubmissionQueryVO {

    @Schema(description = "ID")
    private Long id;

    @Schema(description = "投稿人姓名")
    private String name;

    @Schema(description = "所属单位/医院")
    private String company;

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

    @Schema(description = "审核状态(1-待审核 2-审核通过 3-审核拒绝)")
    private Integer paperStatus;

    @Schema(description = "拒绝原因")
    private String rejectReason;
}