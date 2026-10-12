package com.youlai.boot.portal.converter;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.youlai.boot.portal.model.entity.Architecture;
import com.youlai.boot.portal.model.form.ArchitectureForm;
import com.youlai.boot.portal.model.vo.ArchitecturePageVO;
import org.mapstruct.Mapper;

import java.util.List;


/**
 * 组织架构对象转换器
 *
 * @author 243269112
 * @since 2026/5/25
 */
@Mapper(componentModel = "spring")
public interface ArchitectureConverter {

    ArchitecturePageVO toPageVo(Architecture architecture);

    Page<ArchitecturePageVO> toPageVo(Page<Architecture> page);

    Architecture toEntity(ArchitectureForm architectureForm);

    ArchitectureForm toForm(Architecture entity);

    List<ArchitecturePageVO> toPageVo(List<Architecture> bo);
}
