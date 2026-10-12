package com.youlai.boot.portal.model.query;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.youlai.boot.common.base.BaseQuery;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

/**
 * 精彩瞬间查询对象
 *
 * @author 243269112
 * @since 2024/4/23
 */
@Schema(description = "精彩瞬间查询对象")
@Getter
@Setter
public class MomentsQuery extends BaseQuery {

    @Schema(description="姓名")
    private String name;

    @Schema(description="国家类型")
    private String countryType;

    @Schema(description="开始日期")
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDateTime startDate;

    @Schema(description="结束日期")
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDateTime endDate;
}
