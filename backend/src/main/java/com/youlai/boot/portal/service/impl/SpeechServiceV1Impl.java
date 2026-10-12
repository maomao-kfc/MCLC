package com.youlai.boot.portal.service.impl;

import cn.hutool.core.lang.Assert;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.youlai.boot.core.exception.BusinessException;
import com.youlai.boot.portal.converter.SpeechConverter;
import com.youlai.boot.portal.converter.SpeechV1Converter;
import com.youlai.boot.portal.mapper.SpeechMapper;
import com.youlai.boot.portal.mapper.SpeechV1Mapper;
import com.youlai.boot.portal.model.entity.Speech;
import com.youlai.boot.portal.model.entity.SpeechV1;
import com.youlai.boot.portal.model.form.SpeechForm;
import com.youlai.boot.portal.model.query.SpeechQuery;
import com.youlai.boot.portal.model.vo.SpeechPageVO;
import com.youlai.boot.portal.service.SpeechService;
import com.youlai.boot.portal.service.SpeechServiceV1;
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
public class SpeechServiceV1Impl extends ServiceImpl<SpeechV1Mapper, SpeechV1> implements SpeechServiceV1 {

    private final SpeechV1Converter speechV1Converter;

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
        LambdaQueryWrapper<SpeechV1> queryWrapper = new LambdaQueryWrapper<SpeechV1>()
                .and(StrUtil.isNotBlank(keywords),
                    wrapper ->
                        wrapper.like(SpeechV1::getName, keywords)
                )
                .orderByAsc(SpeechV1::getSort).orderByDesc(SpeechV1::getCreateTime).orderByDesc(SpeechV1::getUpdateTime);
        if (StrUtil.isNotBlank(countryType)) {
            queryWrapper.eq(SpeechV1::getCountryType, countryType);
        }
        Page<SpeechV1> SpeechPage = this.page(new Page<>(pageNum, pageSize), queryWrapper);

        // 实体转换
        return speechV1Converter.toPageVo(SpeechPage);
    }



}
