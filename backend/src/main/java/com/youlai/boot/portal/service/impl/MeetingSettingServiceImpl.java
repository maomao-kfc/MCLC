package com.youlai.boot.portal.service.impl;

import cn.hutool.core.lang.Assert;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.youlai.boot.core.exception.BusinessException;
import com.youlai.boot.portal.converter.MeetingSettingConverter;
import com.youlai.boot.portal.mapper.MeetingSettingMapper;
import com.youlai.boot.portal.model.entity.MeetingSetting;
import com.youlai.boot.portal.model.form.MeetingSettingForm;
import com.youlai.boot.portal.model.query.MeetingSettingQuery;
import com.youlai.boot.portal.model.vo.MeetingSettingPageVO;
import com.youlai.boot.portal.service.MeetingSettingService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Arrays;
import java.util.List;

/**
 * 会议历程业务实现类
 *
 * @author 243269112
 * @since 2024/4/23
 */
@Service
@RequiredArgsConstructor
public class MeetingSettingServiceImpl extends ServiceImpl<MeetingSettingMapper, MeetingSetting> implements MeetingSettingService {

    private final MeetingSettingConverter meetingSettingConverter;

    /**
     * 会议历程分页列表
     *
     * @param queryParams 会议历程查询参数
     * @return {@link Page<  MeetingSettingPageVO  >} – 会议历程分页列表
     */
    @Override
    public Page<MeetingSettingPageVO> getMeetingSettingPage(MeetingSettingQuery queryParams) {
        // 查询参数
        int pageNum = queryParams.getPageNum();
        int pageSize = queryParams.getPageSize();
        String keywords = queryParams.getTitle();
        String countryType = queryParams.getCountryType();
        String sponsor = queryParams.getSponsor();
        String organizer = queryParams.getOrganizer();

        // 查询数据
        LambdaQueryWrapper<MeetingSetting> queryWrapper = new LambdaQueryWrapper<MeetingSetting>()
                .and(StrUtil.isNotBlank(keywords),
                    wrapper ->
                        wrapper.like(MeetingSetting::getTitle, keywords)
                )
                .orderByAsc(MeetingSetting::getSort).orderByDesc(MeetingSetting::getCreateTime).orderByDesc(MeetingSetting::getUpdateTime);
        if (StrUtil.isNotBlank(countryType)) {
            queryWrapper.eq(MeetingSetting::getCountryType, countryType);
        }
        if (StrUtil.isNotBlank(sponsor)) {
            queryWrapper.like(MeetingSetting::getSponsor, sponsor);
        }
        if (StrUtil.isNotBlank(organizer)) {
            queryWrapper.like(MeetingSetting::getOrganizer, organizer);
        }
        if (queryParams.getRegistrationEnabled() != null) {
            queryWrapper.eq(MeetingSetting::getRegistrationEnabled, queryParams.getRegistrationEnabled());
        }
        if (queryParams.getScheduleEnabled() != null) {
            queryWrapper.eq(MeetingSetting::getScheduleEnabled, queryParams.getScheduleEnabled());
        }
        Page<MeetingSetting> MeetingSettingPage = this.page(new Page<>(pageNum, pageSize), queryWrapper);

        // 实体转换
        return meetingSettingConverter.toPageVo(MeetingSettingPage);
    }


    /**
     * 保存会议设置
     *
     * @param meetingSettingForm 会议设置表单数据
     * @return {@link Boolean}
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean saveMeetingSetting(MeetingSettingForm meetingSettingForm) {

        Long MeetingSettingId = meetingSettingForm.getId();

        // 编辑角色时，判断角色是否存在
        MeetingSetting oldMeetingSetting = null;
        if (MeetingSettingId != null) {
            oldMeetingSetting = this.getById(MeetingSettingId);
            Assert.isTrue(oldMeetingSetting != null, "会议历程不存在");
        }

        // 实体转换
        MeetingSetting MeetingSetting = meetingSettingConverter.toEntity(meetingSettingForm);

        return this.saveOrUpdate(MeetingSetting);
    }


    /**
     * 修改会议历程状态
     *
     * @param MeetingSettingId 会议历程ID
     * @param status 角色状态(1:启用；0:禁用)
     * @return {@link Boolean}
     */
    @Override
    public boolean updateMeetingSettingStatus(Long MeetingSettingId, Integer status) {

        MeetingSetting MeetingSetting = this.getById(MeetingSettingId);
        if (MeetingSetting == null) {
            throw new BusinessException("会议历程不存在");
        }

        MeetingSetting.setStatus(status);

        return this.updateById(MeetingSetting);
    }

    /**
     * 批量删除会议历程
     *
     * @param ids 会议历程ID，多个使用英文逗号(,)分割
     */
    @Override
    public void deleteMeetingSettings(String ids) {
        Assert.isTrue(StrUtil.isNotBlank(ids), "删除的会议历程ID不能为空");
        List<Long> MeetingSettingIds = Arrays.stream(ids.split(","))
            .map(Long::parseLong)
            .toList();

        for (Long MeetingSettingId : MeetingSettingIds) {
            MeetingSetting MeetingSetting = this.getById(MeetingSettingId);
            Assert.isTrue(MeetingSetting != null, "会议历程不存在");

            boolean deleteResult = this.removeById(MeetingSettingId);
        }
    }


    /**
     * 会议历程列表
     *
     * @param queryParams 会议历程查询参数
     * @return {@link Page<  MeetingSettingPageVO  >} – 会议历程列表
     */
    @Override
    public List<MeetingSettingPageVO> getMeetingSettingList(MeetingSettingQuery queryParams) {

        // 查询数据
        List<MeetingSetting> MeetingSettingList = this.list(
            new LambdaQueryWrapper<MeetingSetting>()
                .eq(MeetingSetting::getStatus, 1)
                .orderByAsc(MeetingSetting::getSort).orderByDesc(MeetingSetting::getCreateTime).orderByDesc(MeetingSetting::getUpdateTime)
        );

        // 实体转换
        return meetingSettingConverter.toPageVo(MeetingSettingList);
    }


}
