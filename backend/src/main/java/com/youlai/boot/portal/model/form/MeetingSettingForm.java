package com.youlai.boot.portal.model.form;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import org.hibernate.validator.constraints.Range;

import java.time.LocalDateTime;

@Schema(description = "会议历程表单对象")
@Data
public class MeetingSettingForm {

    @Schema(description="ID")
    private Long id;

    @Schema(description="会议主题")
    @NotBlank(message = "会议主题不能为空")
    private String title;

    @Schema(description="会议开始时间")
@NotNull(message = "会议开始时间不能为空")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime startTmie;


    @NotNull(message = "会议结束时间不能为空")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime endTmie;

    @NotNull(message = "常规注册开始时间不能为空")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime generalRegStart;

    @NotNull(message = "常规注册结束时间不能为空")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime generalRegEnd;

    @NotNull(message = "早鸟优惠注册开始时间不能为空")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime earlyBirdRegStart;

    @NotNull(message = "早鸟优惠注册结束时间不能为空")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime earlyBirdRegEnd;

    @NotNull(message = "会议举办开始时间不能为空")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime conferenceDateStart;

    @NotNull(message = "会议举办结束时间不能为空")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime conferenceDateEnd;

    @Schema(description="国家类型")
    private String countryType;

    @Schema(description="主办方")
    private String sponsor;

    @Schema(description="承办方")
    private String organizer;

    @Schema(description="主题图片")
    @NotBlank(message = "主题图片不能为空")
    private String titleImage;

    @Schema(description="会前安排 图片")
    @NotBlank(message = "会前安排 图片不能为空")
    private String hqapImage;

    @Schema(description="会议日程名称")
    @NotBlank(message = "会议日程名称不能为空")
    private String scheduleTitle;

    @Schema(description="会议日程描述")
    @NotBlank(message = "会议日程描述不能为空")
    private String scheduleDescription;

    @Schema(description="是否展示日程(1-是 0-否)")
    @Range(max = 1, min = 0, message = "是否展示日程不正确")
    private Integer showSchedule;

    @Schema(description="排序")
    private Integer sort;

    @Schema(description="角色状态(1-正常；0-停用)")
    @Range(max = 1, min = 0, message = "角色状态不正确")
    private Integer status;

    @Schema(description="是否开启注册(1-是 0-否)")
    @Range(max = 1, min = 0, message = "是否开启注册不正确")
    private Integer registrationEnabled;

    @Schema(description="是否开启日程(1-是 0-否)")
    @Range(max = 1, min = 0, message = "是否开启日程不正确")
    private Integer scheduleEnabled;

}
