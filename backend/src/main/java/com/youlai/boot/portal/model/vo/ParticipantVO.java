package com.youlai.boot.portal.model.vo;

import lombok.Data;

/**
 * 板块参与人VO（Panel Discussion等）
 */
@Data
public class ParticipantVO {
    private Long id;
    private Long sessionId;
    private Long speakerId;
    private String name;
    private String nameEn;
    private String role;          // MODERATOR/PANELIST/OTHER
    private Integer sortOrder;
    // 以下字段来自关联的meeting_speaker表（当speakerId不为空时）
    private String speakerTitle;
    private String speakerTitleEn;
    private String affiliation;
    private String affiliationEn;
}