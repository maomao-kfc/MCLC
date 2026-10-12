package com.youlai.boot.portal.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.youlai.boot.portal.model.entity.MeetingSession;
import com.youlai.boot.portal.service.MeetingSessionService;
import com.youlai.boot.portal.mapper.MeetingSessionMapper;
import org.springframework.stereotype.Service;

/**
* @author dearth
* @description 针对表【meeting_session(会议日程板块表)】的数据库操作Service实现
* @createDate 2026-04-24 10:46:52
*/
@Service
public class MeetingSessionServiceImpl extends ServiceImpl<MeetingSessionMapper, MeetingSession>
    implements MeetingSessionService{

}




