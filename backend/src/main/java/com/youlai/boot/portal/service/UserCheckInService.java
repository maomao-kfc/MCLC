package com.youlai.boot.portal.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.youlai.boot.portal.model.entity.UserCheckIn;
import com.youlai.boot.portal.model.query.UserCheckInQuery;
import com.youlai.boot.portal.model.vo.UserCheckInPageVO;

public interface UserCheckInService extends IService<UserCheckIn> {

    Page<UserCheckInPageVO> getUserCheckInPage(UserCheckInQuery queryParams);

    void deleteUserCheckIns(String ids);
}
