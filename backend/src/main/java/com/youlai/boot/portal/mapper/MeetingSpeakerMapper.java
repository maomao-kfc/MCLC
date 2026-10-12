package com.youlai.boot.portal.mapper;

import com.youlai.boot.portal.model.entity.MeetingSpeaker;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

/**
* @author dearth
* @description 针对表【meeting_speaker(嘉宾信息表)】的数据库操作Mapper
* @createDate 2026-04-24 10:46:58
* @Entity com.youlai.boot.portal.model.entity.MeetingSpeaker
*/
@Mapper
public interface MeetingSpeakerMapper extends BaseMapper<MeetingSpeaker> {

}




