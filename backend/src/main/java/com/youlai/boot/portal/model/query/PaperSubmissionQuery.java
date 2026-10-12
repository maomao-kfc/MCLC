package com.youlai.boot.portal.model.query;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.youlai.boot.common.base.BaseQuery;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

/**
 * 论文投稿查询对象
 *
 * @author 243269112
 * @since 2026/5/6
 */
@Schema(description = "论文投稿查询对象")
@Getter
@Setter
public class PaperSubmissionQuery extends BaseQuery {

    @Schema(description = "论文投稿ID")
    private Long id;

    @Schema(description = "论文标题")
    private String paperTitle;

    @Schema(description = "第一作者")
    private String firstAuthor;

    @Schema(description = "关键词")
    private String keywords;

    @Schema(description = "开始日期")
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDateTime startDate;

    @Schema(description = "结束日期")
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDateTime endDate;

    @Schema(description = "抵扣码")
    private String discountCode;
}
