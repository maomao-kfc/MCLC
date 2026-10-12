package com.youlai.boot.system.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.youlai.boot.system.model.bo.RegistrationTypeBo;
import com.youlai.boot.system.model.entity.RegistrationType;
import com.youlai.boot.system.model.query.RegistrationQuery;
import org.apache.ibatis.annotations.Mapper;

/**
 * 注册类型Mapper接口
 */
@Mapper
public interface RegistrationTypeMapper extends BaseMapper<RegistrationType> {
    /**
     * 注册类型分页数据
     *
     * @param page 分页对象
     * @param queryParams 查询参数
     * @return 通知公告分页数据
     */
    Page<RegistrationTypeBo> getRegistrationTypePage(Page<RegistrationTypeBo> page, RegistrationQuery queryParams);
}
