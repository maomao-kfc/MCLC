package com.youlai.boot.portal.model.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.List;

@Schema(description = "会议时间段VO")
@Data
public class MeetingPeriodVO {

    @Schema(description = "时间段ID")
    private Long timePeriodId;

    @Schema(description = "阶段名称")
    private String timePeriodName;

    @Schema(description = "主持人")
    private String timePeriodPerson;

    @Schema(description = "时间")
    private String timePeriodTime;

    @Schema(description = "类型")
    private String timePeriodType;

    @Schema(description = "题目")
    private String timePeriodQuestion;

    @Schema(description = "讲者")
    private String timePeriodTeacher;

    @Schema(description = "单位")
    private String timePeriodUnit;

    @Schema(description = "排序")
    private Integer meetSort;

    @Schema(description = "项目列表")
    private List<MeetingPeriodVO> items;
}