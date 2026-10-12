package com.youlai.boot.portal.converter;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.youlai.boot.portal.model.entity.Arrange;
import com.youlai.boot.portal.model.form.ArrangeForm;
import com.youlai.boot.portal.model.vo.ArrangePageVO;
import org.mapstruct.Mapper;

import java.util.List;


/**
 * 会前安排对象转换器
 *
 * @author 243269112
 * @since 2026/4/23
 */
@Mapper(componentModel = "spring")
public interface ArrangeConverter {

    ArrangePageVO toPageVo(Arrange Arrange);

    Page<ArrangePageVO> toPageVo(Page<Arrange> page);

    Arrange toEntity(ArrangeForm ArrangeForm);

    ArrangeForm toForm(Arrange entity);

    List<ArrangePageVO> toPageVo(List<Arrange> bo);
}
