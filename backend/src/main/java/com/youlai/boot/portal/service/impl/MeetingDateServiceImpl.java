package com.youlai.boot.portal.service.impl;

import cn.hutool.core.lang.Assert;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.youlai.boot.portal.mapper.MeetingDateMapper;
import com.youlai.boot.portal.mapper.MeetingPeriodMapper;
import com.youlai.boot.portal.model.entity.MeetingDate;
import com.youlai.boot.portal.model.entity.MeetingPeriod;
import com.youlai.boot.portal.model.form.MeetingDateForm;
import com.youlai.boot.portal.model.form.MeetingPeriodForm;
import com.youlai.boot.portal.model.form.SessionForm;
import com.youlai.boot.portal.model.vo.MeetingDateVO;
import com.youlai.boot.portal.model.vo.MeetingPeriodVO;
import com.youlai.boot.portal.service.MeetingDateService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class MeetingDateServiceImpl extends ServiceImpl<MeetingDateMapper, MeetingDate> implements MeetingDateService {

    private final MeetingPeriodMapper meetingPeriodMapper;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean saveMeetingDate(MeetingDateForm form) {
        MeetingDate exist = this.getOne(
            new LambdaQueryWrapper<MeetingDate>()
                .eq(MeetingDate::getMeetName, form.getMeetName())
                .eq(MeetingDate::getMeetTime, form.getMeetTime())
        );
        Assert.isTrue(exist == null, "会议名称和会议时间不能相同");

        MeetingDate meetingDate = new MeetingDate();
        BeanUtils.copyProperties(form, meetingDate);
        if (meetingDate.getTimePeriodId() == null) {
            meetingDate.setTimePeriodId(System.nanoTime() % 1000000000L);
        }
        this.save(meetingDate);

        if (form.getSessions() != null && !form.getSessions().isEmpty()) {
            int sessionIndex = 0;
            for (SessionForm session : form.getSessions()) {
                if (session.getItems() != null && !session.getItems().isEmpty()) {
                    for (MeetingPeriodForm item : session.getItems()) {
                        MeetingPeriod period = new MeetingPeriod();
                        period.setTimePeriodId(System.nanoTime() % 1000000000L);
                        period.setTableId(meetingDate.getId());
                        period.setTimePeriodName(session.getTimePeriodName());
                        period.setTimePeriodPerson(session.getTimePeriodPerson());
                        period.setTimePeriodTime(item.getTimePeriodTime());
                        period.setTimePeriodType(item.getTimePeriodType());
                        period.setTimePeriodQuestion(item.getTimePeriodQuestion());
                        period.setTimePeriodTeacher(item.getTimePeriodTeacher());
                        period.setTimePeriodUnit(item.getTimePeriodUnit());
                        period.setTimePeriodSort(sessionIndex);
                        meetingPeriodMapper.insert(period);
                    }
                } else if (session.getTimePeriodName() != null && !session.getTimePeriodName().isEmpty()) {
                    MeetingPeriod period = new MeetingPeriod();
                    period.setTimePeriodId(System.nanoTime() % 1000000000L);
                    period.setTableId(meetingDate.getId());
                    period.setTimePeriodName(session.getTimePeriodName());
                    period.setTimePeriodPerson(session.getTimePeriodPerson());
                    period.setTimePeriodSort(sessionIndex);
                    meetingPeriodMapper.insert(period);
                }
                sessionIndex++;
            }
        }
        return true;
    }

    @Override
    public MeetingDateVO getMeetingDateById(Long id) {
        MeetingDate meetingDate = this.getById(id);
        if (meetingDate == null) {
            return null;
        }

        MeetingDateVO vo = new MeetingDateVO();
        BeanUtils.copyProperties(meetingDate, vo);

        List<MeetingPeriod> periods = meetingPeriodMapper.selectList(
            new LambdaQueryWrapper<MeetingPeriod>()
                .eq(MeetingPeriod::getTableId, meetingDate.getId())
                .orderByAsc(MeetingPeriod::getTimePeriodSort)
        );

        vo.setSessions(buildSessionVoList(periods));

        return vo;
    }

    @Override
    public List<MeetingDateVO> getMeetingDateList(String countryType) {
        LambdaQueryWrapper<MeetingDate> queryWrapper = new LambdaQueryWrapper<MeetingDate>()
                .orderByAsc(MeetingDate::getMeetSort)
                .orderByDesc(MeetingDate::getCreateTime);
        if (countryType != null && !countryType.isEmpty()) {
            queryWrapper.eq(MeetingDate::getCountryType, countryType);
        }
        List<MeetingDate> meetingDates = this.list(queryWrapper);

        if (meetingDates.isEmpty()) {
            return new ArrayList<>();
        }

        List<Long> ids = meetingDates.stream().map(MeetingDate::getId).collect(Collectors.toList());
        List<MeetingPeriod> allPeriods = meetingPeriodMapper.selectList(
            new LambdaQueryWrapper<MeetingPeriod>()
                .in(MeetingPeriod::getTableId, ids)
                .orderByAsc(MeetingPeriod::getTimePeriodSort)
        );

        Map<Long, List<MeetingPeriod>> periodMap = new HashMap<>();
        for (MeetingPeriod period : allPeriods) {
            periodMap.computeIfAbsent(period.getTableId(), k -> new ArrayList<>()).add(period);
        }

        List<MeetingDateVO> voList = new ArrayList<>();
        for (MeetingDate meetingDate : meetingDates) {
            MeetingDateVO vo = new MeetingDateVO();
            BeanUtils.copyProperties(meetingDate, vo);
            vo.setSessions(buildSessionVoList(periodMap.getOrDefault(meetingDate.getId(), new ArrayList<>())));
            voList.add(vo);
        }

        return voList;
    }

    @Override
    public Page<MeetingDateVO> getMeetingDatePage(int pageNum, int pageSize, String countryType) {
        Page<MeetingDate> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<MeetingDate> queryWrapper = new LambdaQueryWrapper<MeetingDate>()
                .orderByAsc(MeetingDate::getMeetSort)
                .orderByDesc(MeetingDate::getCreateTime);
        if (countryType != null && !countryType.isEmpty()) {
            queryWrapper.eq(MeetingDate::getCountryType, countryType);
        }
        Page<MeetingDate> pageResult = this.page(page, queryWrapper);

        if (pageResult.getRecords().isEmpty()) {
            return new Page<>(pageNum, pageSize, pageResult.getTotal());
        }

        List<Long> ids = pageResult.getRecords().stream().map(MeetingDate::getId).collect(Collectors.toList());
        List<MeetingPeriod> allPeriods = meetingPeriodMapper.selectList(
            new LambdaQueryWrapper<MeetingPeriod>()
                .in(MeetingPeriod::getTableId, ids)
                .orderByAsc(MeetingPeriod::getTimePeriodSort)
        );

        Map<Long, List<MeetingPeriod>> periodMap = new HashMap<>();
        for (MeetingPeriod period : allPeriods) {
            periodMap.computeIfAbsent(period.getTableId(), k -> new ArrayList<>()).add(period);
        }

        List<MeetingDateVO> voList = new ArrayList<>();
        for (MeetingDate meetingDate : pageResult.getRecords()) {
            MeetingDateVO vo = new MeetingDateVO();
            BeanUtils.copyProperties(meetingDate, vo);
            vo.setSessions(buildSessionVoList(periodMap.getOrDefault(meetingDate.getId(), new ArrayList<>())));
            voList.add(vo);
        }

        Page<MeetingDateVO> resultPage = new Page<>(pageNum, pageSize, pageResult.getTotal());
        resultPage.setRecords(voList);
        return resultPage;
    }

    private List<MeetingPeriodVO> buildSessionVoList(List<MeetingPeriod> periods) {
        if (periods == null || periods.isEmpty()) {
            return new ArrayList<>();
        }

        periods.sort(Comparator.comparingInt(MeetingPeriod::getTimePeriodSort));

        Map<Integer, List<MeetingPeriod>> sortGroupMap = new HashMap<>();
        for (MeetingPeriod period : periods) {
            sortGroupMap.computeIfAbsent(period.getTimePeriodSort(), k -> new ArrayList<>()).add(period);
        }

        List<MeetingPeriodVO> sessionList = new ArrayList<>();
        for (Map.Entry<Integer, List<MeetingPeriod>> entry : sortGroupMap.entrySet()) {
            List<MeetingPeriod> groupPeriods = entry.getValue();
            String key = groupPeriods.get(0).getTimePeriodName() + "_" + groupPeriods.get(0).getTimePeriodPerson();

            boolean hasValidItem = false;
            for (MeetingPeriod p : groupPeriods) {
                if (p.getTimePeriodTime() != null || p.getTimePeriodType() != null ||
                    p.getTimePeriodQuestion() != null || p.getTimePeriodTeacher() != null) {
                    hasValidItem = true;
                    break;
                }
            }

            if (!hasValidItem && groupPeriods.get(0).getTimePeriodName() != null) {
                MeetingPeriodVO sessionVO = new MeetingPeriodVO();
                sessionVO.setTimePeriodName(groupPeriods.get(0).getTimePeriodName());
                sessionVO.setTimePeriodPerson(groupPeriods.get(0).getTimePeriodPerson());
                sessionVO.setItems(new ArrayList<>());
                sessionList.add(sessionVO);
                continue;
            }

            MeetingPeriodVO sessionVO = new MeetingPeriodVO();
            if (!groupPeriods.isEmpty()) {
                sessionVO.setTimePeriodName(groupPeriods.get(0).getTimePeriodName());
                sessionVO.setTimePeriodPerson(groupPeriods.get(0).getTimePeriodPerson());
            }

            List<MeetingPeriodVO> itemList = new ArrayList<>();
            for (MeetingPeriod period : groupPeriods) {
                if (period.getTimePeriodTime() != null || period.getTimePeriodType() != null ||
                    period.getTimePeriodQuestion() != null || period.getTimePeriodTeacher() != null) {
                    MeetingPeriodVO itemVO = new MeetingPeriodVO();
                    BeanUtils.copyProperties(period, itemVO);
                    itemList.add(itemVO);
                }
            }
            sessionVO.setItems(itemList);
            sessionList.add(sessionVO);
        }

        return sessionList;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean updateMeetingDate(MeetingDateForm form) {
        Assert.isTrue(form.getId() != null, "ID不能为空");

        MeetingDate oldMeetingDate = this.getById(form.getId());
        Assert.isTrue(oldMeetingDate != null, "会议日期不存在");

        MeetingDate meetingDate = new MeetingDate();
        BeanUtils.copyProperties(form, meetingDate);
        this.updateById(meetingDate);

        meetingPeriodMapper.delete(
            new LambdaQueryWrapper<MeetingPeriod>()
                .eq(MeetingPeriod::getTableId, form.getId())
        );

        if (form.getSessions() != null && !form.getSessions().isEmpty()) {
            int sessionIndex = 0;
            for (SessionForm session : form.getSessions()) {
                if (session.getItems() != null && !session.getItems().isEmpty()) {
                    for (MeetingPeriodForm item : session.getItems()) {
                        MeetingPeriod period = new MeetingPeriod();
                        period.setTimePeriodId(System.nanoTime() % 1000000000L);
                        period.setTableId(meetingDate.getId());
                        period.setTimePeriodName(session.getTimePeriodName());
                        period.setTimePeriodPerson(session.getTimePeriodPerson());
                        period.setTimePeriodTime(item.getTimePeriodTime());
                        period.setTimePeriodType(item.getTimePeriodType());
                        period.setTimePeriodQuestion(item.getTimePeriodQuestion());
                        period.setTimePeriodTeacher(item.getTimePeriodTeacher());
                        period.setTimePeriodUnit(item.getTimePeriodUnit());
                        period.setTimePeriodSort(sessionIndex);
                        meetingPeriodMapper.insert(period);
                    }
                } else if (session.getTimePeriodName() != null && !session.getTimePeriodName().isEmpty()) {
                    MeetingPeriod period = new MeetingPeriod();
                    period.setTimePeriodId(System.nanoTime() % 1000000000L);
                    period.setTableId(meetingDate.getId());
                    period.setTimePeriodName(session.getTimePeriodName());
                    period.setTimePeriodPerson(session.getTimePeriodPerson());
                    period.setTimePeriodSort(sessionIndex);
                    meetingPeriodMapper.insert(period);
                }
                sessionIndex++;
            }
        }
        return true;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean deleteMeetingDateBatch(String ids) {
        if (ids == null || ids.isEmpty()) {
            return false;
        }
        List<Long> idList = new ArrayList<>();
        for (String id : ids.split(",")) {
            idList.add(Long.parseLong(id.trim()));
        }
        meetingPeriodMapper.delete(
            new LambdaQueryWrapper<MeetingPeriod>()
                .in(MeetingPeriod::getTableId, idList)
        );
        this.removeByIds(idList);
        return true;
    }
}