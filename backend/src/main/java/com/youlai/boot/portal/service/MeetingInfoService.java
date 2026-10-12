package com.youlai.boot.portal.service;

import com.youlai.boot.portal.model.entity.MeetingInfo;
import com.baomidou.mybatisplus.extension.service.IService;

import java.time.LocalDate;

/**
* @author dearth
* @description 针对表【meeting_info(会议基础信息表)】的数据库操作Service
* @createDate 2026-04-24 10:46:18
*/
public interface MeetingInfoService extends IService<MeetingInfo> {

    /**
     * 获取所有日程（前台展示）
     */
    java.util.List<com.youlai.boot.portal.model.vo.MeetingScheduleVO> getAllSchedules();

    /**
     * 分页获取日程（管理端）
     */
    com.baomidou.mybatisplus.core.metadata.IPage<com.youlai.boot.portal.model.vo.MeetingScheduleVO> getAllSchedulesPage(int pageNum, int pageSize, String meetName);

    /**
     * 创建会议日程（含板块、条目、参与人级联创建）
     */
    boolean createSchedule(com.youlai.boot.portal.model.form.MeetingSessionForm form);

    /**
     * 更新会议日程板块（含条目、参与人级联更新）
     */
    boolean updateSession(com.youlai.boot.portal.model.form.MeetingSessionForm form);

    /**
     * 删除板块（级联删除条目和参与人）
     */
    boolean deleteSession(Long sessionId);

    /**
     * 更新会议状态（启用/禁用）
     */
    boolean updateMeetingStatus(Long meetingId, Integer status);

    /**
     * 更新会议基础信息（名称、日期、会场等）
     */
    boolean updateMeeting(Long meetingId, String name, String nameEn, LocalDate meetingDate, String venue, String venueEn, Integer sortOrder, Integer status);

    /**
     * 删除会议（级联删除板块、条目、参与人）
     */
    boolean deleteMeeting(Long meetingId);

    /**
     * 批量更新板块排序值
     */
    boolean batchUpdateSessionSortOrder(java.util.List<java.util.Map<String, Long>> sortOrders);
}
