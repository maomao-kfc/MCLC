package com.youlai.boot.portal.converter;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.youlai.boot.portal.model.entity.Hotel;
import com.youlai.boot.portal.model.form.HotelForm;
import com.youlai.boot.portal.model.vo.HotelPageVO;
import org.mapstruct.Mapper;

import java.util.List;


/**
 * 酒店信息对象转换器
 *
 * @author 243269112
 * @since 2026/4/23
 */
@Mapper(componentModel = "spring")
public interface HotelConverter {

    HotelPageVO toPageVo(Hotel hotel);

    Page<HotelPageVO> toPageVo(Page<Hotel> page);

    Hotel toEntity(HotelForm hotelForm);

    HotelForm toForm(Hotel entity);

    List<HotelPageVO> toPageVo(List<Hotel> bo);
}
