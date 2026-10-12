package com.youlai.boot.portal.mapper;

import com.youlai.boot.portal.model.entity.MeetingInfo;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

/**
* @author dearth
* @description 针对表【meeting_info(会议基础信息表)】的数据库操作Mapper
* @createDate 2026-04-24 10:46:18
* @Entity com.youlai.boot.portal.model.entity.MeetingInfo
*/
@Mapper
public interface MeetingInfoMapper extends BaseMapper<MeetingInfo> {

}




