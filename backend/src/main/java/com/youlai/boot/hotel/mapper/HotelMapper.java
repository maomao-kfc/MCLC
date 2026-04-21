package com.youlai.boot.hotel.mapper;

import com.youlai.boot.hotel.model.entity.Hotel;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

/**
* @author dearth
* @description 针对表【hotel(酒店信息表)】的数据库操作Mapper
* @createDate 2026-04-21 11:47:54
* @Entity com.youlai.boot.hotel.model.entity.Hotel
*/

@Mapper
public interface HotelMapper extends BaseMapper<Hotel> {

}




