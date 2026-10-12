package com.youlai.boot.system.converter;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.youlai.boot.system.model.bo.RegistrationTypeBo;
import com.youlai.boot.system.model.entity.RegistrationType;
import com.youlai.boot.system.model.form.RegistrationTypeForm;
import com.youlai.boot.system.model.vo.RegistrationTypeVO;
import org.mapstruct.Mapper;
/**
 * 注册类型转换器
 */
@Mapper(componentModel = "spring")
public interface RegistrationTypeConverter {
    Page<RegistrationTypeVO> toPageVo(Page<RegistrationTypeBo> page);

    RegistrationType toEntity(RegistrationTypeForm formData);


}
