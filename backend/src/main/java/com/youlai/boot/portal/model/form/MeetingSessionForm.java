package com.youlai.boot.portal.model.form;

import lombok.Data;

import jakarta.validation.constraints.NotBlank;
import java.util.List;

/**
 * 会议板块表单（管理端创建/更新）
 */
@Data
public class MeetingSessionForm {

    private Long id;  // 更新时传入

    private Long meetingId;  // 关联会议ID

    @NotBlank(message = "板块标题不能为空")
    private String title;

    private String titleEn;

    private String chairperson;

    private String chairpersonEn;

    private String panelists;

    private String panelistsEn;

    private String timePeriod;  // 时间段文本（如 09:00-10:30）
    private String timePeriodEn; // 时间段文本（英文）

    private String topicContent;     // 讨论主题内容（中文）
    private String topicContentEn;   // 讨论主题内容（英文）

    private String remark;

    private String remarkEn;

    private String itemType;  // 板块类型：DISCUSSION等

    private Integer sortOrder;

    /** 板块内条目列表 */
    private List<AgendaItemForm> items;

    /** Panel Discussion参与人列表 */
    private List<ParticipantForm> participants;
}