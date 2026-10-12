package com.youlai.boot.portal.model.form;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.List;

@Schema(description = "会议阶段表单对象")
@Data
public class SessionForm {
    @Schema(description = "阶段名称")
    private String timePeriodName;

    @Schema(description = "主持人")
    private String timePeriodPerson;

    @Schema(description = "排序")
    private Integer meetSort;

    @Schema(description = "阶段内项目列表")
    private List<MeetingPeriodForm> items;
}