package com.youlai.boot.portal.model.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.youlai.boot.common.base.BaseEntity;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

/**
 * 会议设置实体
 *
 * @author 2432691112
 * @since 2026/4/22
 */
@TableName("portal_meeting_setting")
@Getter
@Setter
public class MeetingSetting extends BaseEntity {

    /**
     * 会议主题
     */
    private String title;

    /**
     * 会议开始时间
     */
    @TableField("start_time")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime startTmie;

    /**
     * 会议结束时间
     */
    @TableField("end_time")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime endTmie;


    /**
     * 常规注册开始时间
     */
    @TableField("general_reg_start")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime generalRegStart;

    /**
     * 常规注册结束时间
     */
    @TableField("general_reg_end")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime generalRegEnd;

    /**
     * 早鸟优惠注册开始时间
     */
    @TableField("early_bird_reg_start")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime earlyBirdRegStart;

    /**
     * 早鸟优惠注册结束时间
     */
    @TableField("early_bird_reg_end")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime earlyBirdRegEnd;

    /**
     * 会议举办开始时间
     */
    @TableField("conference_date_start")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime conferenceDateStart;

    /**
     * 会议举办结束时间
     */
    @TableField("conference_date_end")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime conferenceDateEnd;

    /**
     * 国家类型
     */
    private String countryType;

    /**
     * 主办方
     */
    private String sponsor;

    /**
     * 承办方
     */
    private String organizer;

    /**
     * 主题图片
     */
    private String titleImage;

    /**
     * 会前安排 图片
     */
    private String hqapImage;

    /**
     * 会议日程名称
     */
    private String scheduleTitle;

    /**
     * 会议日程描述
     */
    private String scheduleDescription;

    /**
     * 是否展示日程(1-是 0-否)
     */
    private Integer showSchedule;

    /**
     * 显示顺序
     */
    private Integer sort;

    /**
     * 角色状态(1-正常 0-停用)
     */
    private Integer status;


    /**
     * 创建人 ID
     */
    private Long createBy;

    /**
     * 更新人 ID
     */
    private Long updateBy;

    /**
     * 是否开启注册(1-是 0-否)
     */
    @TableField("registration_enabled")
    private Integer registrationEnabled;

    /**
     * 是否开启日程(1-是 0-否)
     */
    @TableField("schedule_enabled")
    private Integer scheduleEnabled;

    /**
     * 是否删除(0-否 1-是)
     */
    private Integer isDeleted;
}
