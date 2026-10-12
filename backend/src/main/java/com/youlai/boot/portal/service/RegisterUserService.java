package com.youlai.boot.portal.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.youlai.boot.portal.model.entity.RegisterUserInformation;
import com.youlai.boot.portal.model.form.CheckInForm;
import com.youlai.boot.portal.model.form.RegisterUserForm;
import com.youlai.boot.portal.model.query.RegisterUserQuery;
import com.youlai.boot.portal.model.vo.RegisterUserPageVO;

import java.util.List;
import java.util.Map;

public interface RegisterUserService extends IService<RegisterUserInformation> {

    Page<RegisterUserPageVO> getRegisterUserPage(RegisterUserQuery queryParams);

    Map<String, Object> saveRegisterUser(RegisterUserForm form);

    void deleteRegisterUsers(String ids);

    RegisterUserPageVO getRegisterUserDetail(Long id);

    List<RegisterUserPageVO> getRegisterUserList(RegisterUserQuery queryParams);

    void sendLoginEmailCode(String email);

    void auditRegisterUser(Long id, Integer reviewStatus, String rejectReason);

    Map<String, Object> checkIn(CheckInForm form);
}
