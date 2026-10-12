package com.youlai.boot.system.service.impl;

import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.youlai.boot.core.exception.BusinessException;
import com.youlai.boot.security.util.SecurityUtils;
import com.youlai.boot.system.converter.RegistrationTypeConverter;
import com.youlai.boot.system.mapper.RegistrationTypeMapper;
import com.youlai.boot.system.model.bo.RegistrationTypeBo;
import com.youlai.boot.system.model.entity.RegistrationType;
import com.youlai.boot.system.model.form.RegistrationTypeForm;
import com.youlai.boot.system.model.query.RegistrationQuery;
import com.youlai.boot.system.model.vo.RegistrationTypeVO;
import com.youlai.boot.system.service.RegistrationTypeService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.List;

@Service
@RequiredArgsConstructor
public class RegistrationTypeServiceImpl extends ServiceImpl<RegistrationTypeMapper, RegistrationType>  implements RegistrationTypeService {

    private final RegistrationTypeConverter registrationTypeConverter;

    /**
     * 查询注册类型
     */
    @Override
    public IPage<RegistrationTypeVO> getRegistrationTypePage(RegistrationQuery queryParams) {
        Page<RegistrationTypeBo> registrationTypePage = this.baseMapper.getRegistrationTypePage(
            new Page<>(queryParams.getPageNum(), queryParams.getPageSize()),
            queryParams
        );
        return registrationTypeConverter.toPageVo(registrationTypePage);
    }

    /**
     * 新增注册类型
     */
    @Override
    public boolean saveRegistrationType(RegistrationTypeForm formData) {
        long count = this.count(new LambdaQueryWrapper<RegistrationType>()
            .eq(RegistrationType::getFieldName, formData.getFieldName()));
        if (count > 0) {
            throw new BusinessException("字段名称已存在");
        }
        RegistrationType entity = registrationTypeConverter.toEntity(formData);
        return this.save(entity);
    }

    /**
     * 更新注册类型
     */
    @Override
    public boolean updateRegistrationType(Long id, RegistrationTypeForm formData) {
        long count = this.count(new LambdaQueryWrapper<RegistrationType>()
            .eq(RegistrationType::getFieldName, formData.getFieldName())
            .ne(RegistrationType::getId, id));
        if (count > 0) {
            throw new BusinessException("字段名称已存在");
        }
        RegistrationType entity = registrationTypeConverter.toEntity(formData);
        entity.setId(id);
        return this.updateById(entity);
    }

    /**
     * 删除注册类型
     */
    @Override
    public boolean deleteRegistrationType(String ids) {
        if (StrUtil.isBlank(ids)) {
            throw new BusinessException("删除的注册数据为空");
        }

        // 逻辑删除
        List<Long> idList = Arrays.stream(ids.split(","))
            .map(Long::parseLong)
            .toList();
        return this.removeByIds(idList);
    }

}
