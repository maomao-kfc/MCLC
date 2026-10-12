package com.youlai.boot.portal.model.form;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import lombok.Data;

@Schema(description = "会议时间段表单对象")
@Data
public class MeetingPeriodForm {

    @Schema(description = "时间段ID")
    private Long timePeriodId;

    @Schema(description = "表ID")
    private Long tableId;

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
}