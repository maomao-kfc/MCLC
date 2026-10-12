package com.youlai.boot.portal.service.impl;

import cn.hutool.core.lang.Assert;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.youlai.boot.core.exception.BusinessException;
import com.youlai.boot.portal.converter.HotelConverter;
import com.youlai.boot.portal.mapper.HotelMapper;
import com.youlai.boot.portal.model.entity.Contact;
import com.youlai.boot.portal.model.entity.Hotel;
import com.youlai.boot.portal.model.form.HotelForm;
import com.youlai.boot.portal.model.query.HotelQuery;
import com.youlai.boot.portal.model.vo.HotelPageVO;
import com.youlai.boot.portal.service.HotelService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Arrays;
import java.util.List;

/**
 * 酒店信息业务实现类
 *
 * @author 243269112
 * @since 2024/4/23
 */
@Service
@RequiredArgsConstructor
public class HotelServiceImpl extends ServiceImpl<HotelMapper, Hotel> implements HotelService {

    private final HotelConverter hotelConverter;

    /**
     * 酒店信息分页列表
     *
     * @param queryParams 酒店信息查询参数
     * @return {@link Page< HotelPageVO >} – 酒店信息分页列表
     */
    @Override
    public Page<HotelPageVO> getHotelPage(HotelQuery queryParams) {
        // 查询参数
        int pageNum = queryParams.getPageNum();
        int pageSize = queryParams.getPageSize();
        String keywords = queryParams.getHotelName();
        String countryType = queryParams.getCountryType();

        // 查询数据
        LambdaQueryWrapper<Hotel> queryWrapper = new LambdaQueryWrapper<Hotel>()
                .and(StrUtil.isNotBlank(keywords),
                    wrapper ->
                        wrapper.like(Hotel::getHotelName, keywords)
                )
                .orderByAsc(Hotel::getSort).orderByDesc(Hotel::getCreateTime).orderByDesc(Hotel::getUpdateTime);
        if (StrUtil.isNotBlank(countryType)) {
            queryWrapper.eq(Hotel::getCountryType, countryType);
        }
        Page<Hotel> hotelPage = this.page(new Page<>(pageNum, pageSize), queryWrapper);

        // 实体转换
        return hotelConverter.toPageVo(hotelPage);
    }


    /**
     * 保存酒店信息
     *
     * @param hotelForm 酒店信息表单数据
     * @return {@link Boolean}
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean saveHotel(HotelForm hotelForm) {

        Long hotelId = hotelForm.getId();

        // 编辑角色时，判断角色是否存在
        Hotel oldhotel = null;
        if (hotelId != null) {
            oldhotel = this.getById(hotelId);
            Assert.isTrue(oldhotel != null, "酒店信息不存在");
        }

        // 实体转换
        Hotel hotel = hotelConverter.toEntity(hotelForm);

        return this.saveOrUpdate(hotel);
    }


    /**
     * 修改酒店信息状态
     *
     * @param hotelId 酒店信息ID
     * @param status 角色状态(1:启用；0:禁用)
     * @return {@link Boolean}
     */
    @Override
    public boolean updateHotelStatus(Long hotelId, Integer status) {

        Hotel hotel = this.getById(hotelId);
        if (hotel == null) {
            throw new BusinessException("酒店信息不存在");
        }

        hotel.setStatus(status);

        return this.updateById(hotel);
    }

    /**
     * 批量删除酒店信息
     *
     * @param ids 酒店信息ID，多个使用英文逗号(,)分割
     */
    @Override
    public void deleteHotels(String ids) {
        Assert.isTrue(StrUtil.isNotBlank(ids), "删除的酒店信息ID不能为空");
        List<Long> hotelIds = Arrays.stream(ids.split(","))
            .map(Long::parseLong)
            .toList();

        for (Long hotelId : hotelIds) {
            Hotel hotel = this.getById(hotelId);
            Assert.isTrue(hotel != null, "酒店信息不存在");

            boolean deleteResult = this.removeById(hotelId);
        }
    }


    /**
     * 酒店信息列表
     *
     * @param queryParams 酒店信息查询参数
     * @return {@link Page< HotelPageVO >} – 酒店信息列表
     */
    @Override
    public List<HotelPageVO> getHotelList(HotelQuery queryParams) {

        // 查询数据
        LambdaQueryWrapper<Hotel> queryWrapper = new LambdaQueryWrapper<Hotel>()
                .eq(Hotel::getStatus, 1)
                .orderByAsc(Hotel::getSort).orderByDesc(Hotel::getCreateTime).orderByDesc(Hotel::getUpdateTime);
        if (queryParams.getCountryType() != null && !queryParams.getCountryType().isEmpty()) {
            queryWrapper.eq(Hotel::getCountryType, queryParams.getCountryType());
        }
        List<Hotel> hotelList = this.list(queryWrapper);

        // 实体转换
        return hotelConverter.toPageVo(hotelList);
    }


}
