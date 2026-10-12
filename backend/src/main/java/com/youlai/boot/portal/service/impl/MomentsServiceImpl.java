package com.youlai.boot.portal.service.impl;

import cn.hutool.core.lang.Assert;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.youlai.boot.core.exception.BusinessException;
import com.youlai.boot.portal.converter.MomentsConverter;
import com.youlai.boot.portal.mapper.MomentsMapper;
import com.youlai.boot.portal.model.entity.Moments;
import com.youlai.boot.portal.model.form.MomentsForm;
import com.youlai.boot.portal.model.query.MomentsQuery;
import com.youlai.boot.portal.model.vo.MomentsPageVO;
import com.youlai.boot.portal.service.MomentsService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Arrays;
import java.util.List;

/**
 * 精彩瞬间业务实现类
 *
 * @author 243269112
 * @since 2024/4/23
 */
@Service
@RequiredArgsConstructor
public class MomentsServiceImpl extends ServiceImpl<MomentsMapper, Moments> implements MomentsService {

    private final MomentsConverter MomentsConverter;

    /**
     * 精彩瞬间分页列表
     *
     * @param queryParams 精彩瞬间查询参数
     * @return {@link Page< MomentsPageVO >} – 精彩瞬间分页列表
     */
    @Override
    public Page<MomentsPageVO> getMomentsPage(MomentsQuery queryParams) {
        // 查询参数
        int pageNum = queryParams.getPageNum();
        int pageSize = queryParams.getPageSize();
        String keywords = queryParams.getName();
        String countryType = queryParams.getCountryType();

        // 查询数据
        LambdaQueryWrapper<Moments> queryWrapper = new LambdaQueryWrapper<Moments>()
                .and(StrUtil.isNotBlank(keywords),
                    wrapper ->
                        wrapper.like(Moments::getName, keywords)
                )
                .orderByAsc(Moments::getSort).orderByDesc(Moments::getCreateTime).orderByDesc(Moments::getUpdateTime);
        if (StrUtil.isNotBlank(countryType)) {
            queryWrapper.eq(Moments::getCountryType, countryType);
        }
        Page<Moments> MomentsPage = this.page(new Page<>(pageNum, pageSize), queryWrapper);

        // 实体转换
        return MomentsConverter.toPageVo(MomentsPage);
    }


    /**
     * 保存精彩瞬间
     *
     * @param momentsForm 精彩瞬间表单数据
     * @return {@link Boolean}
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean saveMoments(MomentsForm momentsForm) {

        Long momentsId = momentsForm.getId();

        // 编辑角色时，判断角色是否存在
        Moments oldMoments = null;
        if (momentsId != null) {
            oldMoments = this.getById(momentsId);
            Assert.isTrue(oldMoments != null, "精彩瞬间不存在");
        }

        // 实体转换
        Moments moments = MomentsConverter.toEntity(momentsForm);

        return this.saveOrUpdate(moments);
    }


    /**
     * 修改精彩瞬间状态
     *
     * @param momentsId 精彩瞬间ID
     * @param status 角色状态(1:启用；0:禁用)
     * @return {@link Boolean}
     */
    @Override
    public boolean updateMomentsStatus(Long momentsId, Integer status) {

        Moments moments = this.getById(momentsId);
        if (moments == null) {
            throw new BusinessException("精彩瞬间不存在");
        }

        moments.setStatus(status);

        return this.updateById(moments);
    }

    /**
     * 批量删除精彩瞬间
     *
     * @param ids 精彩瞬间ID，多个使用英文逗号(,)分割
     */
    @Override
    public void deleteMomentss(String ids) {
        Assert.isTrue(StrUtil.isNotBlank(ids), "删除的精彩瞬间ID不能为空");
        List<Long> MomentsIds = Arrays.stream(ids.split(","))
            .map(Long::parseLong)
            .toList();

        for (Long MomentsId : MomentsIds) {
            Moments Moments = this.getById(MomentsId);
            Assert.isTrue(Moments != null, "精彩瞬间不存在");

            boolean deleteResult = this.removeById(MomentsId);
        }
    }


    /**
     * 精彩瞬间列表
     *
     * @param queryParams 精彩瞬间查询参数
     * @return {@link Page< MomentsPageVO >} – 精彩瞬间列表
     */
    @Override
    public List<MomentsPageVO> getMomentsList(MomentsQuery queryParams) {

        // 查询数据
        LambdaQueryWrapper<Moments> queryWrapper = new LambdaQueryWrapper<Moments>()
                .eq(Moments::getStatus, 1)
                .orderByAsc(Moments::getSort).orderByDesc(Moments::getCreateTime).orderByDesc(Moments::getUpdateTime);
        if (queryParams.getCountryType() != null && !queryParams.getCountryType().isEmpty()) {
            queryWrapper.eq(Moments::getCountryType, queryParams.getCountryType());
        }
        List<Moments> MomentsList = this.list(queryWrapper);

        // 实体转换
        return MomentsConverter.toPageVo(MomentsList);
    }


}
