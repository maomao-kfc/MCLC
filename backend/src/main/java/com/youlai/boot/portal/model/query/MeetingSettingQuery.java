package com.youlai.boot.portal.model.query;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.youlai.boot.common.base.BaseQuery;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

/**
 * 会议历程查询对象
 *
 * @author 243269112
 * @since 2024/4/23
 */
@Schema(description = "会议历程查询对象")
@Getter
@Setter
public class MeetingSettingQuery extends BaseQuery {

    @Schema(description="会议主题")
    private String title;

    @Schema(description="国家类型")
    private String countryType;

    @Schema(description="主办方")
    private String sponsor;

    @Schema(description="承办方")
    private String organizer;

    @Schema(description="开始日期")
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDateTime startDate;

    @Schema(description="结束日期")
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDateTime endDate;

    @Schema(description="是否开启注册(1-是 0-否)")
    private Integer registrationEnabled;

    @Schema(description="是否开启日程(1-是 0-否)")
    private Integer scheduleEnabled;
}
