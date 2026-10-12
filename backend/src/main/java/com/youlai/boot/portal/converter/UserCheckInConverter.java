package com.youlai.boot.portal.converter;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.youlai.boot.portal.model.entity.UserCheckIn;
import com.youlai.boot.portal.model.vo.UserCheckInPageVO;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface UserCheckInConverter {

    UserCheckInPageVO toPageVo(UserCheckIn entity);

    Page<UserCheckInPageVO> toPageVo(Page<UserCheckIn> page);
}
