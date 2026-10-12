package com.youlai.boot.portal.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.youlai.boot.portal.model.entity.Guest;
import org.apache.ibatis.annotations.Mapper;

/**
 * 特邀嘉宾持久层接口
 *
 * @author 243269112
 * @since 2026/4/22
 */
@Mapper
public interface GuestMapper extends BaseMapper<Guest> {


}
