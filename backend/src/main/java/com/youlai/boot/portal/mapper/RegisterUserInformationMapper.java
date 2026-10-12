package com.youlai.boot.portal.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.youlai.boot.portal.model.entity.RegisterUserInformation;
import com.youlai.boot.portal.model.query.RegisterUserQuery;
import com.youlai.boot.portal.model.vo.RegisterUserPageVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface RegisterUserInformationMapper extends BaseMapper<RegisterUserInformation> {

    Page<RegisterUserPageVO> selectRegisterUserPage(Page<RegisterUserPageVO> page, @Param("query") RegisterUserQuery query);

    RegisterUserPageVO selectRegisterUserDetail(@Param("id") Long id);
}
