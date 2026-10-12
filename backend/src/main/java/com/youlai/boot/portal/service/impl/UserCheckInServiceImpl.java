package com.youlai.boot.portal.service.impl;

import cn.hutool.core.lang.Assert;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.youlai.boot.portal.converter.UserCheckInConverter;
import com.youlai.boot.portal.mapper.UserCheckInMapper;
import com.youlai.boot.portal.model.entity.UserCheckIn;
import com.youlai.boot.portal.model.query.UserCheckInQuery;
import com.youlai.boot.portal.model.vo.UserCheckInPageVO;
import com.youlai.boot.portal.service.UserCheckInService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Arrays;
import java.util.List;

@Service
@RequiredArgsConstructor
public class UserCheckInServiceImpl extends ServiceImpl<UserCheckInMapper, UserCheckIn> implements UserCheckInService {

    private final UserCheckInConverter userCheckInConverter;

    @Override
    public Page<UserCheckInPageVO> getUserCheckInPage(UserCheckInQuery queryParams) {
        int pageNum = queryParams.getPageNum();
        int pageSize = queryParams.getPageSize();

        Page<UserCheckIn> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<UserCheckIn> query = new LambdaQueryWrapper<UserCheckIn>()
                .like(StrUtil.isNotBlank(queryParams.getCheckName()), UserCheckIn::getCheckName, queryParams.getCheckName())
                .like(StrUtil.isNotBlank(queryParams.getCheckEmail()), UserCheckIn::getCheckEmail, queryParams.getCheckEmail())
                .like(StrUtil.isNotBlank(queryParams.getCheckNumber()), UserCheckIn::getCheckNumber, queryParams.getCheckNumber())
                .eq(StrUtil.isNotBlank(queryParams.getCheckStatus()), UserCheckIn::getCheckStatus, queryParams.getCheckStatus())
                .orderByDesc(UserCheckIn::getId);

        this.page(page, query);
        return userCheckInConverter.toPageVo(page);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteUserCheckIns(String ids) {
        Assert.isTrue(StrUtil.isNotBlank(ids), "删除的签到记录ID不能为空");
        List<Long> checkInIds = Arrays.stream(ids.split(","))
                .map(Long::parseLong)
                .toList();
        this.removeByIds(checkInIds);
    }
}
