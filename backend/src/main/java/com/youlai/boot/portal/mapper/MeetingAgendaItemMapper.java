package com.youlai.boot.portal.mapper;

import com.youlai.boot.portal.model.entity.MeetingAgendaItem;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.youlai.boot.portal.model.vo.AgendaItemVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
* @author dearth
* @description 针对表【meeting_agenda_item(会议详细议程条目表)】的数据库操作Mapper
* @createDate 2026-04-24 10:43:02
* @Entity com.youlai.boot/portal.model.entity.MeetingAgendaItem
*/
@Mapper
public interface MeetingAgendaItemMapper extends BaseMapper<MeetingAgendaItem> {

    /**
     * 根据板块ID查询条目列表（包含讲者信息）
     */
    List<AgendaItemVO> selectItemsWithSpeakerBySessionId(@Param("sessionId") Long sessionId);
}




