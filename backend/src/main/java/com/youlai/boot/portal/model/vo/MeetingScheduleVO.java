package com.youlai.boot.portal.model.vo;

import lombok.Data;

import java.util.List;

/**
 * 整体日程对象
 */
@Data
public class MeetingScheduleVO {
    private Long meetingId;
    private String meetingName;
    private String meetingNameEn;
    private Integer status;
    private String date;           // 如 "1/16星期五"
    private String week;           // 星期几（中文）
    private String weekEn;
    private String hall;           // 会议厅名称
    private String hallEn;
    private List<SessionVO> sessions;

    /** 原始会议日期（ISO格式，用于编辑） */
    private String meetingDate;
    /** 排序值 */
    private Integer sortOrder;
}