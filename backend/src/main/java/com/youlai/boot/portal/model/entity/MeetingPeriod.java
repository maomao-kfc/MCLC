package com.youlai.boot.portal.model.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Getter;
import lombok.Setter;

@TableName("portal_meeting_period")
@Getter
@Setter
public class MeetingPeriod {

    @TableId(value = "time_period_id", type = IdType.AUTO)
    private Long timePeriodId;

    @TableField("table_id")
    private Long tableId;

    @TableField("time_period_name")
    private String timePeriodName;

    @TableField("time_period_person")
    private String timePeriodPerson;

    @TableField("time_period_time")
    private String timePeriodTime;

    @TableField("time_period_type")
    private String timePeriodType;

    @TableField("time_period_question")
    private String timePeriodQuestion;

    @TableField("time_period_teacher")
    private String timePeriodTeacher;

    @TableField("time_period_unit")
    private String timePeriodUnit;

    @TableField("time_period_sort")
    private Integer timePeriodSort;
}