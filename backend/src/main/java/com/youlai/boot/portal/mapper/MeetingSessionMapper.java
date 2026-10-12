package com.youlai.boot.portal.mapper;

import com.youlai.boot.portal.model.entity.MeetingSession;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

/**
* @author dearth
* @description 针对表【meeting_session(会议日程板块表)】的数据库操作Mapper
* @createDate 2026-04-24 10:46:52
* @Entity com.youlai.boot.portal.model.entity.MeetingSession
*/
@Mapper
public interface MeetingSessionMapper extends BaseMapper<MeetingSession> {

}




