package com.youlai.boot.portal.model.query;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.youlai.boot.common.base.BaseQuery;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

/**
 * 特邀嘉宾查询对象
 *
 * @author 243269112
 * @since 2024/4/22
 */
@Schema(description = "特邀嘉宾查询对象")
@Getter
@Setter
public class GuestQuery extends BaseQuery {

    @Schema(description="特邀嘉宾姓名")
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
