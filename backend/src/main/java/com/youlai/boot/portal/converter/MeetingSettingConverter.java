package com.youlai.boot.portal.converter;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.youlai.boot.portal.model.entity.MeetingSetting;
import com.youlai.boot.portal.model.form.MeetingSettingForm;
import com.youlai.boot.portal.model.vo.MeetingSettingPageVO;
import org.mapstruct.Mapper;

import java.util.List;


/**
 * 会议设置对象转换器
 *
 * @author 243269112
 * @since 2026/4/23
 */
@Mapper(componentModel = "spring")
public interface MeetingSettingConverter {

    MeetingSettingPageVO toPageVo(MeetingSetting meetingSetting);

    Page<MeetingSettingPageVO> toPageVo(Page<MeetingSetting> page);

    MeetingSetting toEntity(MeetingSettingForm meetingSettingForm);

    MeetingSettingForm toForm(MeetingSetting entity);

    List<MeetingSettingPageVO> toPageVo(List<MeetingSetting> bo);
}
