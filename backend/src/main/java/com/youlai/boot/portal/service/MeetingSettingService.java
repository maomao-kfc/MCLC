package com.youlai.boot.portal.service;


import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.youlai.boot.portal.model.entity.MeetingSetting;
import com.youlai.boot.portal.model.form.MeetingSettingForm;
import com.youlai.boot.portal.model.query.MeetingSettingQuery;
import com.youlai.boot.portal.model.vo.MeetingSettingPageVO;

import java.util.List;


/**
 * 会议设置业务接口层
 *
 * @author 243269112
 * @since 2024/4/23
 */
public interface MeetingSettingService extends IService<MeetingSetting> {

    /**
     * 会议设置分页列表
     *
     * @param queryParams
     * @return
     */
    Page<MeetingSettingPageVO> getMeetingSettingPage(MeetingSettingQuery queryParams);

    /**
     *
     * @param MeetingSettingForm
     * @return
     */
    boolean saveMeetingSetting(MeetingSettingForm MeetingSettingForm);

    /**
     * 修改会议设置状态
     *
     * @param MeetingSettingId 会议历程ID
     * @param status 角色状态(1:启用；0:禁用)
     * @return {@link Boolean}
     */
    boolean updateMeetingSettingStatus(Long MeetingSettingId, Integer status);

    /**
     * 批量删除会议设置
     *
     * @param ids 会议设置ID，多个使用英文逗号(,)分割
     */
    void deleteMeetingSettings(String ids);

    /**
     * 会会议设置列表
     *
     * @param queryParams
     * @return
     */
    List<MeetingSettingPageVO> getMeetingSettingList(MeetingSettingQuery queryParams);
}
