package com.youlai.boot.portal.service.impl;

import cn.hutool.db.PageResult;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;

import com.youlai.boot.portal.mapper.MeetingAgendaItemMapper;
import com.youlai.boot.portal.model.entity.MeetingAgendaItem;
import com.youlai.boot.portal.model.entity.MeetingInfo;
import com.youlai.boot.portal.model.entity.MeetingSession;
import com.youlai.boot.portal.model.entity.MeetingSessionParticipant;
import com.youlai.boot.portal.model.form.AgendaItemForm;
import com.youlai.boot.portal.model.form.MeetingSessionForm;
import com.youlai.boot.portal.model.form.ParticipantForm;
import com.youlai.boot.portal.model.vo.AgendaItemVO;
import com.youlai.boot.portal.model.vo.MeetingScheduleVO;
import com.youlai.boot.portal.model.vo.ParticipantVO;
import com.youlai.boot.portal.model.vo.SessionVO;
import com.youlai.boot.portal.service.MeetingAgendaItemService;
import com.youlai.boot.portal.service.MeetingInfoService;
import com.youlai.boot.portal.mapper.MeetingInfoMapper;
import com.youlai.boot.portal.service.MeetingSessionParticipantService;
import com.youlai.boot.portal.service.MeetingSessionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
* @author dearth
* @description 针对表【meeting_info(会议基础信息表)】的数据库操作Service实现
* @createDate 2026-04-24 10:46:18
*/
@Service
public class MeetingInfoServiceImpl extends ServiceImpl<MeetingInfoMapper, MeetingInfo>
    implements MeetingInfoService{

    @Autowired
    private MeetingSessionService sessionService;

    @Autowired
    private MeetingAgendaItemMapper meetingAgendaItemMapper;

    @Autowired
    private MeetingSessionParticipantService participantService;

    @Autowired
    private MeetingAgendaItemService agendaItemService;

    /** itemType枚举 → 中文名/英文名 映射，用于type为空时自动填充 */
    private static final Map<String, String[]> ITEM_TYPE_MAP = Map.ofEntries(
        Map.entry("OPENING", new String[]{"开幕式", "Opening Ceremony"}),
        Map.entry("KEYNOTE", new String[]{"主旨报告", "Keynote Speech"}),
        Map.entry("INVITED_SPEECH", new String[]{"专题发言", "Invited Speaker"}),
        Map.entry("GENERAL_SPEECH", new String[]{"大会发言", "Free Paper"}),
        Map.entry("DISCUSSION", new String[]{"真实临床案例专家研讨", "Panel Discussion"}),
        Map.entry("BREAK", new String[]{"茶歇/壁报", "Tea/Poster Break"}),
        Map.entry("LUNCH", new String[]{"中餐-专题会", "Lunchtime Symposium"}),
        Map.entry("CLOSING", new String[]{"闭幕式", "Closing Ceremony"}),
        Map.entry("OTHER", new String[]{"其他", "Other"})
    );

    /** 根据itemType自动填充type/typeEn（当type为空时） */
    private void fillTypeFromItemType(MeetingAgendaItem item) {
        if ((item.getType() == null || item.getType().isBlank()) && item.getItemType() != null) {
            String[] labels = ITEM_TYPE_MAP.get(item.getItemType());
            if (labels != null) {
                item.setType(labels[0]);
                if (item.getTypeEn() == null || item.getTypeEn().isBlank()) {
                    item.setTypeEn(labels[1]);
                }
            }
        }
    }

    //前端网页展示不分页
    public List<MeetingScheduleVO> getAllSchedules() {
        // 1. 查询所有启用的会议，按日期排序
        List<MeetingInfo> meetings = lambdaQuery()
            .eq(MeetingInfo::getStatus, 1)
            .orderByAsc(MeetingInfo::getSortOrder)
            .list();

        List<MeetingScheduleVO> result = new ArrayList<>();
        for (MeetingInfo meeting : meetings) {
            MeetingScheduleVO vo = new MeetingScheduleVO();
            vo.setMeetingId(meeting.getId());
            vo.setMeetingName(meeting.getName());
            vo.setMeetingNameEn(meeting.getNameEn());
            vo.setStatus(meeting.getStatus());
            // 格式化日期为前端需要的样式
            LocalDate date = meeting.getMeetingDate();
            String dateStr = (date.getMonthValue()) + "/" + date.getDayOfMonth();
            vo.setDate(dateStr);
            vo.setWeek(getChineseWeek(date));
            vo.setWeekEn(getEnglishWeek(date));
            vo.setHall(meeting.getVenue());
            vo.setHallEn(meeting.getVenueEn());

            // 2. 查询该会议下的所有板块
            List<MeetingSession> sessions = sessionService.lambdaQuery()
                .eq(MeetingSession::getMeetingId, meeting.getId())
                .orderByAsc(MeetingSession::getSortOrder)
                .list();

            List<SessionVO> sessionVOs = new ArrayList<>();
            for (MeetingSession session : sessions) {
                SessionVO svo = new SessionVO();
                svo.setId(session.getId());
                svo.setTitle(session.getTitle());
                svo.setTitleEn(session.getTitleEn());
                svo.setChair(session.getChairperson());
                svo.setChairEn(session.getChairpersonEn());
                svo.setPanelists(session.getPanelists());
                svo.setPanelistsEn(session.getPanelistsEn());
                svo.setTimePeriod(session.getTimePeriod());
                svo.setTimePeriodEn(session.getTimePeriodEn());
                svo.setTopicContent(session.getTopicContent());
                svo.setTopicContentEn(session.getTopicContentEn());
                svo.setRemark(session.getRemark());
                svo.setRemarkEn(session.getRemarkEn());
                svo.setItemType(session.getItemType());
                svo.setSortOrder(session.getSortOrder());

                // 3. 查询板块下所有条目（含讲者）
                List<AgendaItemVO> items = meetingAgendaItemMapper.selectItemsWithSpeakerBySessionId(session.getId());
                svo.setItems(items);

                // 4. 查询板块参与人（Panel Discussion等）
                List<MeetingSessionParticipant> participants = participantService.lambdaQuery()
                    .eq(MeetingSessionParticipant::getSessionId, session.getId())
                    .orderByAsc(MeetingSessionParticipant::getSortOrder)
                    .list();
                List<ParticipantVO> participantVOs = participants.stream().map(p -> {
                    ParticipantVO pvo = new ParticipantVO();
                    pvo.setId(p.getId());
                    pvo.setSessionId(p.getSessionId());
                    pvo.setSpeakerId(p.getSpeakerId());
                    pvo.setName(p.getName());
                    pvo.setNameEn(p.getNameEn());
                    pvo.setRole(p.getRole());
                    pvo.setSortOrder(p.getSortOrder());
                    return pvo;
                }).collect(Collectors.toList());
                svo.setParticipants(participantVOs);

                sessionVOs.add(svo);
            }
            vo.setSessions(sessionVOs);
            result.add(vo);
        }
        return result;
    }

    //管理系统前端展示分页
    public IPage<MeetingScheduleVO> getAllSchedulesPage(int pageNum, int pageSize, String meetName) {
        var query = lambdaQuery()
            .orderByAsc(MeetingInfo::getSortOrder);
        
        // 支持按会议名称模糊搜索
        if (meetName != null && !meetName.trim().isEmpty()) {
            query.like(MeetingInfo::getName, meetName.trim());
        }
        
        Page<MeetingInfo> page = query.page(new Page<>(pageNum, pageSize));

        List<MeetingScheduleVO> records = new ArrayList<>();
        for (MeetingInfo meeting : page.getRecords()) {
            MeetingScheduleVO vo = new MeetingScheduleVO();
            LocalDate date = meeting.getMeetingDate();
            String dateStr = (date.getMonthValue()) + "/" + date.getDayOfMonth();
            vo.setDate(dateStr);
            vo.setWeek(getChineseWeek(date));
            vo.setWeekEn(getEnglishWeek(date));
            vo.setHall(meeting.getVenue());
            vo.setHallEn(meeting.getVenueEn());
            vo.setMeetingId(meeting.getId());
            vo.setMeetingName(meeting.getName());
            vo.setMeetingNameEn(meeting.getNameEn());
            vo.setStatus(meeting.getStatus());
            vo.setSortOrder(meeting.getSortOrder());
            // 原始日期（ISO格式，用于编辑弹窗）
            vo.setMeetingDate(meeting.getMeetingDate() != null ? meeting.getMeetingDate().toString() : null);

            List<MeetingSession> sessions = sessionService.lambdaQuery()
                .eq(MeetingSession::getMeetingId, meeting.getId())
                .orderByAsc(MeetingSession::getSortOrder)
                .list();

            List<SessionVO> sessionVOs = new ArrayList<>();
            for (MeetingSession session : sessions) {
                SessionVO svo = new SessionVO();
                svo.setId(session.getId());
                svo.setTitle(session.getTitle());
                svo.setTitleEn(session.getTitleEn());
                svo.setChair(session.getChairperson());
                svo.setChairEn(session.getChairpersonEn());
                svo.setPanelists(session.getPanelists());
                svo.setPanelistsEn(session.getPanelistsEn());
                svo.setTimePeriod(session.getTimePeriod());
                svo.setTimePeriodEn(session.getTimePeriodEn());
                svo.setTopicContent(session.getTopicContent());
                svo.setTopicContentEn(session.getTopicContentEn());
                svo.setRemark(session.getRemark());
                svo.setRemarkEn(session.getRemarkEn());
                svo.setItemType(session.getItemType());
                svo.setSortOrder(session.getSortOrder());

                List<AgendaItemVO> items = meetingAgendaItemMapper.selectItemsWithSpeakerBySessionId(session.getId());
                svo.setItems(items);

                // 查询板块参与人
                List<MeetingSessionParticipant> participants = participantService.lambdaQuery()
                    .eq(MeetingSessionParticipant::getSessionId, session.getId())
                    .orderByAsc(MeetingSessionParticipant::getSortOrder)
                    .list();
                List<ParticipantVO> participantVOs = participants.stream().map(p -> {
                    ParticipantVO pvo = new ParticipantVO();
                    pvo.setId(p.getId());
                    pvo.setSessionId(p.getSessionId());
                    pvo.setSpeakerId(p.getSpeakerId());
                    pvo.setName(p.getName());
                    pvo.setNameEn(p.getNameEn());
                    pvo.setRole(p.getRole());
                    pvo.setSortOrder(p.getSortOrder());
                    return pvo;
                }).collect(Collectors.toList());
                svo.setParticipants(participantVOs);

                sessionVOs.add(svo);
            }
            vo.setSessions(sessionVOs);
            records.add(vo);
        }

        // 将 records 设置到分页结果中
        Page<MeetingScheduleVO> resultPage = new Page<>(pageNum, pageSize, page.getTotal());
        resultPage.setRecords(records);
        return resultPage;
    }

    private String getChineseWeek(LocalDate date){
        String [] weeks = {"星期日", "星期一", "星期二", "星期三", "星期四", "星期五", "星期六"};
        return weeks[date.getDayOfWeek().getValue() % 7];
    }

    private String getEnglishWeek(LocalDate date){
        return date.getDayOfWeek().toString();
    }

    // ========== 管理端CRUD方法 ==========

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean createSchedule(MeetingSessionForm form) {
        // 创建板块
        MeetingSession session = new MeetingSession();
        session.setMeetingId(form.getMeetingId() != null ? form.getMeetingId() : 1L); // 默认关联第一个会议
        session.setTitle(form.getTitle());
        session.setTitleEn(form.getTitleEn());
        session.setChairperson(form.getChairperson());
        session.setChairpersonEn(form.getChairpersonEn());
        session.setPanelists(form.getPanelists());
        session.setPanelistsEn(form.getPanelistsEn());
        session.setTimePeriod(form.getTimePeriod());
        session.setTimePeriodEn(form.getTimePeriodEn());
        session.setTopicContent(form.getTopicContent());
        session.setTopicContentEn(form.getTopicContentEn());
        session.setRemark(form.getRemark());
        session.setRemarkEn(form.getRemarkEn());
        session.setItemType(form.getItemType());
        session.setSortOrder(form.getSortOrder());
        sessionService.save(session);

        Long sessionId = session.getId();

        // 创建条目
        if (form.getItems() != null && !form.getItems().isEmpty()) {
            List<MeetingAgendaItem> items = new ArrayList<>();
            for (AgendaItemForm itemForm : form.getItems()) {
                MeetingAgendaItem item = new MeetingAgendaItem();
                item.setSessionId(sessionId);
                item.setTopic(itemForm.getTitle());
                item.setTopicEn(itemForm.getTitleEn());
                item.setType(itemForm.getType());
                item.setTypeEn(itemForm.getTypeEn());
                item.setItemType(itemForm.getItemType());
                item.setTimeRange(itemForm.getTimePeriod());
                item.setSortOrder(itemForm.getSortOrder());
                item.setSpeakerId(itemForm.getSpeakerId());
                item.setSpeakerName(itemForm.getSpeakerName());
                item.setSpeakerNameEn(itemForm.getSpeakerNameEn());
                item.setSpeakerOrg(itemForm.getSpeakerOrg());
                item.setSpeakerOrgEn(itemForm.getSpeakerOrgEn());
                fillTypeFromItemType(item);
                items.add(item);
            }
            agendaItemService.saveBatch(items);
        }

        // 创建参与人
        if (form.getParticipants() != null && !form.getParticipants().isEmpty()) {
            List<MeetingSessionParticipant> participants = new ArrayList<>();
            for (ParticipantForm pForm : form.getParticipants()) {
                MeetingSessionParticipant participant = new MeetingSessionParticipant();
                participant.setSessionId(sessionId);
                participant.setName(pForm.getName());
                participant.setNameEn(pForm.getNameEn());
                participant.setSortOrder(pForm.getSortOrder());
                participants.add(participant);
            }
            participantService.saveBatch(participants);
        }

        return true;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean updateSession(MeetingSessionForm form) {
        if (form.getId() == null) {
            return false;
        }

        // 更新板块
        MeetingSession session = sessionService.getById(form.getId());
        if (session == null) {
            return false;
        }
        session.setTitle(form.getTitle());
        session.setTitleEn(form.getTitleEn());
        session.setChairperson(form.getChairperson());
        session.setChairpersonEn(form.getChairpersonEn());
        session.setPanelists(form.getPanelists());
        session.setPanelistsEn(form.getPanelistsEn());
        session.setTimePeriod(form.getTimePeriod());
        session.setTimePeriodEn(form.getTimePeriodEn());
        session.setTopicContent(form.getTopicContent());
        session.setTopicContentEn(form.getTopicContentEn());
        session.setRemark(form.getRemark());
        session.setRemarkEn(form.getRemarkEn());
        session.setItemType(form.getItemType());
        session.setSortOrder(form.getSortOrder());
        sessionService.updateById(session);

        Long sessionId = form.getId();

        // 更新条目：先删后增
        if (form.getItems() != null) {
            agendaItemService.lambdaUpdate()
                .eq(MeetingAgendaItem::getSessionId, sessionId)
                .remove();
            if (!form.getItems().isEmpty()) {
                List<MeetingAgendaItem> items = new ArrayList<>();
                for (AgendaItemForm itemForm : form.getItems()) {
                    MeetingAgendaItem item = new MeetingAgendaItem();
                    item.setSessionId(sessionId);
                    item.setTopic(itemForm.getTitle());
                    item.setTopicEn(itemForm.getTitleEn());
                    item.setType(itemForm.getType());
                    item.setTypeEn(itemForm.getTypeEn());
                    item.setItemType(itemForm.getItemType());
                    item.setTimeRange(itemForm.getTimePeriod());
                    item.setSortOrder(itemForm.getSortOrder());
                    item.setSpeakerId(itemForm.getSpeakerId());
                    item.setSpeakerName(itemForm.getSpeakerName());
                    item.setSpeakerNameEn(itemForm.getSpeakerNameEn());
                    item.setSpeakerOrg(itemForm.getSpeakerOrg());
                    item.setSpeakerOrgEn(itemForm.getSpeakerOrgEn());
                    fillTypeFromItemType(item);
                    items.add(item);
                }
                agendaItemService.saveBatch(items);
            }
        }

        // 更新参与人：先删后增
        if (form.getParticipants() != null) {
            participantService.lambdaUpdate()
                .eq(MeetingSessionParticipant::getSessionId, sessionId)
                .remove();
            if (!form.getParticipants().isEmpty()) {
                List<MeetingSessionParticipant> participants = new ArrayList<>();
                for (ParticipantForm pForm : form.getParticipants()) {
                    MeetingSessionParticipant participant = new MeetingSessionParticipant();
                    participant.setSessionId(sessionId);
                    participant.setName(pForm.getName());
                    participant.setNameEn(pForm.getNameEn());
                    participant.setSortOrder(pForm.getSortOrder());
                    participants.add(participant);
                }
                participantService.saveBatch(participants);
            }
        }

        return true;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean deleteSession(Long sessionId) {
        // 级联删除：条目 → 参与人 → 板块
        agendaItemService.lambdaUpdate()
            .eq(MeetingAgendaItem::getSessionId, sessionId)
            .remove();
        participantService.lambdaUpdate()
            .eq(MeetingSessionParticipant::getSessionId, sessionId)
            .remove();
        sessionService.removeById(sessionId);
        return true;
    }

    @Override
    public boolean updateMeetingStatus(Long meetingId, Integer status) {
        MeetingInfo meeting = getById(meetingId);
        if (meeting == null) {
            return false;
        }
        meeting.setStatus(status);
        return updateById(meeting);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean deleteMeeting(Long meetingId) {
        // 1. 查询该会议下所有板块
        List<MeetingSession> sessions = sessionService.lambdaQuery()
            .eq(MeetingSession::getMeetingId, meetingId)
            .list();
        
        // 2. 级联删除每个板块的条目和参与人
        for (MeetingSession session : sessions) {
            agendaItemService.lambdaUpdate()
                .eq(MeetingAgendaItem::getSessionId, session.getId())
                .remove();
            participantService.lambdaUpdate()
                .eq(MeetingSessionParticipant::getSessionId, session.getId())
                .remove();
        }
        
        // 3. 删除所有板块
        if (!sessions.isEmpty()) {
            sessionService.removeByIds(
                sessions.stream().map(MeetingSession::getId).collect(Collectors.toList())
            );
        }
        
        // 4. 删除会议本身
        removeById(meetingId);
        return true;
    }

    @Override
    public boolean updateMeeting(Long meetingId, String name, String nameEn, LocalDate meetingDate, String venue, String venueEn, Integer sortOrder, Integer status) {
        MeetingInfo meeting = getById(meetingId);
        if (meeting == null) {
            return false;
        }
        if (name != null) meeting.setName(name);
        if (nameEn != null) meeting.setNameEn(nameEn);
        if (meetingDate != null) meeting.setMeetingDate(meetingDate);
        if (venue != null) meeting.setVenue(venue);
        if (venueEn != null) meeting.setVenueEn(venueEn);
        if (sortOrder != null) meeting.setSortOrder(sortOrder);
        if (status != null) meeting.setStatus(status);
        return updateById(meeting);
    }

    @Override
    public boolean batchUpdateSessionSortOrder(List<Map<String, Long>> sortOrders) {
        for (Map<String, Long> item : sortOrders) {
            Long id = item.get("id");
            Long sortOrder = item.get("sortOrder");
            if (id != null && sortOrder != null) {
                sessionService.lambdaUpdate()
                    .eq(MeetingSession::getId, id)
                    .set(MeetingSession::getSortOrder, sortOrder.intValue())
                    .update();
            }
        }
        return true;
    }

}






























