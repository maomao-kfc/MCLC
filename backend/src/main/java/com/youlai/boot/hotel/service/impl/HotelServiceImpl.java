package com.youlai.boot.hotel.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.youlai.boot.hotel.model.entity.Hotel;
import com.youlai.boot.hotel.service.HotelService;
import com.youlai.boot.hotel.mapper.HotelMapper;
import org.springframework.stereotype.Service;

/**
* @author dearth
* @description 针对表【hotel(酒店信息表)】的数据库操作Service实现
* @createDate 2026-04-21 11:47:54
*/
@Service
public class HotelServiceImpl extends ServiceImpl<HotelMapper, Hotel>
    implements HotelService{

}




