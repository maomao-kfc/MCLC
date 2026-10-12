package com.youlai.boot.portal.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.youlai.boot.portal.model.entity.MeetingSessionParticipant;
import com.youlai.boot.portal.service.MeetingSessionParticipantService;
import com.youlai.boot.portal.mapper.MeetingSessionParticipantMapper;
import org.springframework.stereotype.Service;

/**
 * @author system
 * @description 针对表【meeting_session_participant(会议板块参与人表)】的数据库操作Service实现
 * @createDate 2026-08-10
 */
@Service
public class MeetingSessionParticipantServiceImpl extends ServiceImpl<MeetingSessionParticipantMapper, MeetingSessionParticipant>
    implements MeetingSessionParticipantService {

}