package com.youlai.boot.system.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.youlai.boot.system.model.form.RegistrationTypeForm;
import com.youlai.boot.system.model.query.RegistrationQuery;
import com.youlai.boot.system.model.vo.RegistrationTypeVO;

public interface RegistrationTypeService {
    /**
     * 注册类型分页列表
     *
     * @return 注册类型分页列表
     */
    IPage<RegistrationTypeVO> getRegistrationTypePage(RegistrationQuery queryParams);

    /**
     * 新增注册类型
     *
     * @param formData 通知公告表单对象
     * @return 是否新增成功
     */
    boolean saveRegistrationType(RegistrationTypeForm formData);

    boolean updateRegistrationType(Long id, RegistrationTypeForm formData);

    boolean deleteRegistrationType(String ids);
}
