package com.youlai.boot.portal.service;


import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.youlai.boot.portal.model.entity.Hotel;
import com.youlai.boot.portal.model.form.HotelForm;
import com.youlai.boot.portal.model.query.HotelQuery;
import com.youlai.boot.portal.model.vo.HotelPageVO;

import java.util.List;


/**
 * 酒店信息业务接口层
 *
 * @author 243269112
 * @since 2024/4/23
 */
public interface HotelService extends IService<Hotel> {

    /**
     * 酒店信息分页列表
     *
     * @param queryParams
     * @return
     */
    Page<HotelPageVO> getHotelPage(HotelQuery queryParams);

    /**
     *
     * @param hotelForm
     * @return
     */
    boolean saveHotel(HotelForm hotelForm);

    /**
     * 修改酒店信息状态
     *
     * @param hotelId 酒店信息ID
     * @param status 角色状态(1:启用；0:禁用)
     * @return {@link Boolean}
     */
    boolean updateHotelStatus(Long hotelId, Integer status);

    /**
     * 批量删除酒店信息
     *
     * @param ids 酒店信息ID，多个使用英文逗号(,)分割
     */
    void deleteHotels(String ids);

    /**
     * 酒店信息列表
     *
     * @param queryParams
     * @return
     */
    List<HotelPageVO> getHotelList(HotelQuery queryParams);
}
