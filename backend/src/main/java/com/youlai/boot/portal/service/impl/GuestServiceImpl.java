package com.youlai.boot.portal.service.impl;

import cn.hutool.core.lang.Assert;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.youlai.boot.core.exception.BusinessException;
import com.youlai.boot.portal.converter.GuestConverter;
import com.youlai.boot.portal.mapper.GuestMapper;
import com.youlai.boot.portal.model.entity.Contact;
import com.youlai.boot.portal.model.entity.Guest;
import com.youlai.boot.portal.model.form.GuestForm;
import com.youlai.boot.portal.model.query.GuestQuery;
import com.youlai.boot.portal.model.vo.GuestPageVO;
import com.youlai.boot.portal.service.GuestService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Arrays;
import java.util.List;
/**
 * 特邀嘉宾业务实现类
 *
 * @author 243269112
 * @since 2024/4/22
 */
@Service
@RequiredArgsConstructor
public class GuestServiceImpl extends ServiceImpl<GuestMapper, Guest> implements GuestService {

    private final GuestConverter guestConverter;

    /**
     * 特邀嘉宾分页列表
     *
     * @param queryParams 特邀嘉宾查询参数
     * @return {@link Page< GuestPageVO >} – 特邀嘉宾分页列表
     */
    @Override
    public Page<GuestPageVO> getGuestPage(GuestQuery queryParams) {
        // 查询参数
        int pageNum = queryParams.getPageNum();
        int pageSize = queryParams.getPageSize();
        String keywords = queryParams.getName();
        String countryType = queryParams.getCountryType();

        // 查询数据
        LambdaQueryWrapper<Guest> queryWrapper = new LambdaQueryWrapper<Guest>()
                .and(StrUtil.isNotBlank(keywords),
                        wrapper ->
                                wrapper.like(Guest::getName, keywords)
                )
                .orderByAsc(Guest::getSort).orderByDesc(Guest::getCreateTime).orderByDesc(Guest::getUpdateTime);
        if (StrUtil.isNotBlank(countryType)) {
            queryWrapper.eq(Guest::getCountryType, countryType);
        }
        Page<Guest> guestPage = this.page(new Page<>(pageNum, pageSize), queryWrapper);

        // 实体转换
        return guestConverter.toPageVo(guestPage);
    }


    /**
     * 保存特邀嘉宾
     *
     * @param guestForm 特邀嘉宾表单数据
     * @return {@link Boolean}
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean saveGuest(GuestForm guestForm) {

        Long guestId = guestForm.getId();

        // 编辑角色时，判断角色是否存在
        Guest oldGuest = null;
        List<Long> oldDeptIds = null;
        if (guestId != null) {
            oldGuest = this.getById(guestId);
            Assert.isTrue(oldGuest != null, "特邀嘉宾不存在");
        }

        // 实体转换
        Guest guest = guestConverter.toEntity(guestForm);

        return this.saveOrUpdate(guest);
    }


    /**
     * 修改特邀嘉宾状态
     *
     * @param guestId 特邀嘉宾ID
     * @param status 角色状态(1:启用；0:禁用)
     * @return {@link Boolean}
     */
    @Override
    public boolean updateGuestStatus(Long guestId, Integer status) {

        Guest guest = this.getById(guestId);
        if (guest == null) {
            throw new BusinessException("特邀嘉宾不存在");
        }

        guest.setStatus(status);

        return this.updateById(guest);
    }

    /**
     * 批量删除特邀嘉宾
     *
     * @param ids 特邀嘉宾ID，多个使用英文逗号(,)分割
     */
    @Override
    public void deleteGuests(String ids) {
        Assert.isTrue(StrUtil.isNotBlank(ids), "删除的特邀嘉宾ID不能为空");
        List<Long> guestIds = Arrays.stream(ids.split(","))
                .map(Long::parseLong)
                .toList();

        for (Long guestId : guestIds) {
            Guest role = this.getById(guestId);
            Assert.isTrue(role != null, "特邀嘉宾不存在");

            boolean deleteResult = this.removeById(guestId);
        }
    }

    /**
     * 特邀嘉宾列表
     *
     * @param queryParams 特邀嘉宾查询参数
     * @return {@link Page< GuestPageVO >} – 特邀嘉宾列表
     */
    @Override
    public List<GuestPageVO> getGuestList(GuestQuery queryParams) {

        // 查询数据
        LambdaQueryWrapper<Guest> queryWrapper = new LambdaQueryWrapper<Guest>()
                .eq(Guest::getStatus, 1)
                .orderByAsc(Guest::getSort).orderByDesc(Guest::getCreateTime).orderByDesc(Guest::getUpdateTime);
        if (queryParams.getCountryType() != null && !queryParams.getCountryType().isEmpty()) {
            queryWrapper.eq(Guest::getCountryType, queryParams.getCountryType());
        }
        List<Guest> guestList = this.list(queryWrapper);

        // 实体转换
        return guestConverter.toPageVo(guestList);
    }


}
