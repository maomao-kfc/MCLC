package com.youlai.boot.portal.service.impl;

import cn.hutool.core.lang.Assert;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.youlai.boot.core.exception.BusinessException;
import com.youlai.boot.portal.converter.SpeechConverter;
import com.youlai.boot.portal.mapper.SpeechMapper;
import com.youlai.boot.portal.model.entity.Speech;
import com.youlai.boot.portal.model.form.SpeechForm;
import com.youlai.boot.portal.model.query.SpeechQuery;
import com.youlai.boot.portal.model.vo.SpeechPageVO;
import com.youlai.boot.portal.service.SpeechService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Arrays;
import java.util.List;

/**
 * 演讲业务实现类
 *
 * @author 243269112
 * @since 2024/4/23
 */
@Service
@RequiredArgsConstructor
public class SpeechServiceImpl extends ServiceImpl<SpeechMapper, Speech> implements SpeechService {

    private final SpeechConverter SpeechConverter;

    /**
     * 演讲分页列表
     *
     * @param queryParams 演讲查询参数
     * @return {@link Page< SpeechPageVO >} – 演讲分页列表
     */
    @Override
    public Page<SpeechPageVO> getSpeechPage(SpeechQuery queryParams) {
        // 查询参数
        int pageNum = queryParams.getPageNum();
        int pageSize = queryParams.getPageSize();
        String keywords = queryParams.getName();
        String countryType = queryParams.getCountryType();

        // 查询数据
        LambdaQueryWrapper<Speech> queryWrapper = new LambdaQueryWrapper<Speech>()
                .and(StrUtil.isNotBlank(keywords),
                    wrapper ->
                        wrapper.like(Speech::getName, keywords)
                )
                .orderByAsc(Speech::getSort).orderByDesc(Speech::getCreateTime).orderByDesc(Speech::getUpdateTime);
        if (StrUtil.isNotBlank(countryType)) {
            queryWrapper.eq(Speech::getCountryType, countryType);
        }
        Page<Speech> SpeechPage = this.page(new Page<>(pageNum, pageSize), queryWrapper);

        // 实体转换
        return SpeechConverter.toPageVo(SpeechPage);
    }


    /**
     * 保存演讲
     *
     * @param SpeechForm 演讲表单数据
     * @return {@link Boolean}
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean saveSpeech(SpeechForm SpeechForm) {

        Long SpeechId = SpeechForm.getId();

        // 编辑角色时，判断角色是否存在
        Speech oldSpeech = null;
        if (SpeechId != null) {
            oldSpeech = this.getById(SpeechId);
            Assert.isTrue(oldSpeech != null, "演讲不存在");
        }

        // 实体转换
        Speech Speech = SpeechConverter.toEntity(SpeechForm);

        return this.saveOrUpdate(Speech);
    }


    /**
     * 修改演讲状态
     *
     * @param SpeechId 演讲ID
     * @param status 角色状态(1:启用；0:禁用)
     * @return {@link Boolean}
     */
    @Override
    public boolean updateSpeechStatus(Long SpeechId, Integer status) {

        Speech Speech = this.getById(SpeechId);
        if (Speech == null) {
            throw new BusinessException("演讲不存在");
        }

        Speech.setStatus(status);

        return this.updateById(Speech);
    }

    /**
     * 批量删除演讲
     *
     * @param ids 演讲ID，多个使用英文逗号(,)分割
     */
    @Override
    public void deleteSpeechs(String ids) {
        Assert.isTrue(StrUtil.isNotBlank(ids), "删除的演讲ID不能为空");
        List<Long> SpeechIds = Arrays.stream(ids.split(","))
            .map(Long::parseLong)
            .toList();

        for (Long SpeechId : SpeechIds) {
            Speech Speech = this.getById(SpeechId);
            Assert.isTrue(Speech != null, "演讲不存在");

            boolean deleteResult = this.removeById(SpeechId);
        }
    }


    /**
     * 演讲列表
     *
     * @param queryParams 演讲查询参数
     * @return {@link Page< SpeechPageVO >} – 演讲列表
     */
    @Override
    public List<SpeechPageVO> getSpeechList(SpeechQuery queryParams) {

        // 查询数据
        List<Speech> SpeechList = this.list(
            new LambdaQueryWrapper<Speech>()
                .eq(Speech::getStatus, 1)
                .orderByAsc(Speech::getSort).orderByDesc(Speech::getCreateTime).orderByDesc(Speech::getUpdateTime)
        );

        // 实体转换
        return SpeechConverter.toPageVo(SpeechList);
    }

    @Override
    public Speech getSpeechByID(Long id) {
        Speech speech = this.getById(id);
        return speech;
    }


}
