package com.youlai.boot.portal.model.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

@Schema(description ="分页对象")
@Data
public class MeetingSettingPageVO {

    @Schema(description="酒店信息ID")
    private Long id;

    @Schema(description="会议主题")
    private String title;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @Schema(description="会议开始时间")
    private LocalDateTime startTmie;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @Schema(description="会议结束时间")
    private LocalDateTime endTmie;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @Schema(description="常规注册开始时间")
    private LocalDateTime generalRegStart;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @Schema(description="常规注册结束时间")
    private LocalDateTime generalRegEnd;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @Schema(description="早鸟优惠注册开始时间")
    private LocalDateTime earlyBirdRegStart;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @Schema(description="早鸟优惠注册结束时间")
    private LocalDateTime earlyBirdRegEnd;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @Schema(description="会议举办开始时间")
    private LocalDateTime conferenceDateStart;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @Schema(description="会议举办结束时间")
    private LocalDateTime conferenceDateEnd;

    @Schema(description="国家类型")
    private String countryType;

    @Schema(description="主办方")
    private String sponsor;

    @Schema(description="承办方")
    private String organizer;

    @Schema(description="主题宣传图片")
    private String titleImage;

    @Schema(description="会前安排 图片")
    private String hqapImage;

    @Schema(description="会议日程名称")
    private String scheduleTitle;

    @Schema(description="会议日程描述")
    private String scheduleDescription;

    @Schema(description="是否展示日程(1-是 0-否)")
    private Integer showSchedule;

    @Schema(description="")
    private Integer status;

    @Schema(description="排序")
    private Integer sort;

    @Schema(description="是否开启注册(1-是 0-否)")
    private Integer registrationEnabled;

    @Schema(description="是否开启日程(1-是 0-否)")
    private Integer scheduleEnabled;

    @Schema(description="历程描述")
    private String detail;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createTime;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime updateTime;
}

