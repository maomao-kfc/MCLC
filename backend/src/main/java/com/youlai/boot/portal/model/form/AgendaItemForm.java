package com.youlai.boot.portal.model.form;

import lombok.Data;

import jakarta.validation.constraints.NotBlank;

/**
 * 日程条目表单（管理端创建/更新）
 */
@Data
public class AgendaItemForm {

    private Long id;  // 更新时传入

    private Long sessionId;  // 所属板块ID

    @NotBlank(message = "条目标题不能为空")
    private String title;

    private String titleEn;

    private String type;  // 类型显示名（中文，如：开幕式）

    private String typeEn;  // 类型显示名（英文）

    private String itemType;  // 条目类型枚举

    private String timePeriod;  // 时间段

    private String timePeriodEn;

    private String location;

    private String locationEn;

    private String speakerName;

    private String speakerNameEn;

    private String speakerTitle;

    private String speakerTitleEn;

    private String speakerOrg;

    private String speakerOrgEn;

    private String description;

    private String descriptionEn;

    private Integer sortOrder;

    private Long speakerId;  // 关联嘉宾ID
}