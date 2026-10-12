package com.youlai.boot.portal.model.vo;

import lombok.Data;

/**
 * 条目对象
 */
@Data
public class AgendaItemVO {
    private Long id;
    private String time;            // 时间段，对应数据库 time_range
    private String type;
    private String typeEn;
    private String topic;
    private String topicEn;
    private String speaker;         // 讲者姓名
    private String speakerEn;       // 讲者姓名（英文）
    private String affiliation;
    private String affiliationEn;
    private String itemType;        // 日程类型枚举：OPENING/KEYNOTE/INVITED_SPEECH/DISCUSSION/BREAK/LUNCH/CLOSING/OTHER
    private Boolean isSpecial;      // 是否特殊项（茶歇等），对应 is_break
    private Integer sortOrder;      // 排序值
    private Long speakerId;         // 关联嘉宾ID，用于管理端编辑
}