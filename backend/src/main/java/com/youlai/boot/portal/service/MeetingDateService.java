package com.youlai.boot.portal.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.youlai.boot.portal.model.entity.MeetingDate;
import com.youlai.boot.portal.model.form.MeetingDateForm;
import com.youlai.boot.portal.model.vo.MeetingDateVO;

import java.util.List;

public interface MeetingDateService extends IService<MeetingDate> {

    boolean saveMeetingDate(MeetingDateForm form);

    MeetingDateVO getMeetingDateById(Long id);

    List<MeetingDateVO> getMeetingDateList(String countryType);

    Page<MeetingDateVO> getMeetingDatePage(int pageNum, int pageSize, String countryType);

    boolean updateMeetingDate(MeetingDateForm form);

    boolean deleteMeetingDateBatch(String ids);
}