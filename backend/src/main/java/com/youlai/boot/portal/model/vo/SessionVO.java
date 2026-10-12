package com.youlai.boot.portal.model.vo;

import lombok.Data;

import java.util.List;

/**
 * 板块对象
 */
@Data
public class SessionVO {
    private Long id;
    private String title;
    private String titleEn;
    private String chair;
    private String chairEn;
    private String panelists;
    private String panelistsEn;
    private String timePeriod;       // 时间段文本（如 09:00-10:30）
    private String timePeriodEn;     // 时间段文本（英文）
    private String topicContent;     // 讨论主题内容（中文）
    private String topicContentEn;   // 讨论主题内容（英文）
    private String remark;
    private String remarkEn;
    private String itemType;        // 板块类型（用于Panel Discussion等特殊板块识别）
    private Integer sortOrder;      // 排序值
    private List<AgendaItemVO> items;
    private List<ParticipantVO> participants;  // Panel Discussion参与人列表
}