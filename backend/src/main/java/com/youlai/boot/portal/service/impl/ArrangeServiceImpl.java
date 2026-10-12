package com.youlai.boot.portal.service.impl;

import cn.hutool.core.lang.Assert;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.youlai.boot.core.exception.BusinessException;
import com.youlai.boot.portal.converter.ArrangeConverter;
import com.youlai.boot.portal.mapper.ArrangeMapper;
import com.youlai.boot.portal.model.entity.Arrange;
import com.youlai.boot.portal.model.form.ArrangeForm;
import com.youlai.boot.portal.model.query.ArrangeQuery;
import com.youlai.boot.portal.model.vo.ArrangePageVO;
import com.youlai.boot.portal.service.ArrangeService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Arrays;
import java.util.List;

/**
 * 会前安排业务实现类
 *
 * @author 243269112
 * @since 2024/4/23
 */
@Service
@RequiredArgsConstructor
public class ArrangeServiceImpl extends ServiceImpl<ArrangeMapper, Arrange> implements ArrangeService {

    private final ArrangeConverter ArrangeConverter;

    /**
     * 会前安排分页列表
     *
     * @param queryParams 会前安排查询参数
     * @return {@link Page< ArrangePageVO >} – 会前安排分页列表
     */
    @Override
    public Page<ArrangePageVO> getArrangePage(ArrangeQuery queryParams) {
        // 查询参数
        int pageNum = queryParams.getPageNum();
        int pageSize = queryParams.getPageSize();
        String keywords = queryParams.getArrangeRoom();
        String countryType = queryParams.getCountryType();
        String arrangeType = queryParams.getArrangeType();

        // 查询数据
        LambdaQueryWrapper<Arrange> queryWrapper = new LambdaQueryWrapper<Arrange>()
                .and(StrUtil.isNotBlank(keywords),
                    wrapper ->
                        wrapper.like(Arrange::getArrangeRoom, keywords)
                )
                .orderByAsc(Arrange::getSort).orderByDesc(Arrange::getCreateTime).orderByDesc(Arrange::getUpdateTime);
        if (StrUtil.isNotBlank(countryType)) {
            queryWrapper.eq(Arrange::getCountryType, countryType);
        }
        if (StrUtil.isNotBlank(arrangeType)) {
            queryWrapper.eq(Arrange::getArrangeType, arrangeType);
        }
        Page<Arrange> ArrangePage = this.page(new Page<>(pageNum, pageSize), queryWrapper);

        // 实体转换
        return ArrangeConverter.toPageVo(ArrangePage);
    }


    /**
     * 保存会前安排
     *
     * @param ArrangeForm 会前安排表单数据
     * @return {@link Boolean}
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean saveArrange(ArrangeForm ArrangeForm) {

        Long ArrangeId = ArrangeForm.getId();

        // 编辑角色时，判断角色是否存在
        Arrange oldArrange = null;
        if (ArrangeId != null) {
            oldArrange = this.getById(ArrangeId);
            Assert.isTrue(oldArrange != null, "会前安排不存在");
        }

        // 实体转换
        Arrange Arrange = ArrangeConverter.toEntity(ArrangeForm);

        return this.saveOrUpdate(Arrange);
    }


    /**
     * 修改会前安排状态
     *
     * @param ArrangeId 会前安排ID
     * @param status 角色状态(1:启用；0:禁用)
     * @return {@link Boolean}
     */
    @Override
    public boolean updateArrangeStatus(Long ArrangeId, Integer status) {

        Arrange Arrange = this.getById(ArrangeId);
        if (Arrange == null) {
            throw new BusinessException("会前安排不存在");
        }

        Arrange.setStatus(status);

        return this.updateById(Arrange);
    }

    /**
     * 批量删除会前安排
     *
     * @param ids 会前安排ID，多个使用英文逗号(,)分割
     */
    @Override
    public void deleteArranges(String ids) {
        Assert.isTrue(StrUtil.isNotBlank(ids), "删除的会前安排ID不能为空");
        List<Long> ArrangeIds = Arrays.stream(ids.split(","))
            .map(Long::parseLong)
            .toList();

        for (Long ArrangeId : ArrangeIds) {
            Arrange Arrange = this.getById(ArrangeId);
            Assert.isTrue(Arrange != null, "会前安排不存在");

            boolean deleteResult = this.removeById(ArrangeId);
        }
    }


    /**
     * 会前安排列表
     *
     * @param queryParams 会前安排查询参数
     * @return {@link Page< ArrangePageVO >} – 会前安排列表
     */
    @Override
    public List<ArrangePageVO> getArrangeList(ArrangeQuery queryParams) {

        // 查询数据
        LambdaQueryWrapper<Arrange> queryWrapper = new LambdaQueryWrapper<Arrange>()
                .eq(Arrange::getStatus, 1)
                .orderByAsc(Arrange::getSort).orderByDesc(Arrange::getCreateTime).orderByDesc(Arrange::getUpdateTime);
        if (queryParams.getCountryType() != null && !queryParams.getCountryType().isEmpty()) {
            queryWrapper.eq(Arrange::getCountryType, queryParams.getCountryType());
        }
        List<Arrange> arrangeList = this.list(queryWrapper);

        // 实体转换
        return ArrangeConverter.toPageVo(arrangeList);
    }
}
