package com.youlai.boot.portal.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.youlai.boot.portal.model.entity.MeetingSessionParticipant;
import org.apache.ibatis.annotations.Mapper;

/**
 * @author system
 * @description 针对表【meeting_session_participant(会议板块参与人表)】的数据库操作Mapper
 * @createDate 2026-08-10
 */
@Mapper
public interface MeetingSessionParticipantMapper extends BaseMapper<MeetingSessionParticipant> {

}