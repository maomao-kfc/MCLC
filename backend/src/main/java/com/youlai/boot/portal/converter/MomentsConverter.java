package com.youlai.boot.portal.converter;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.youlai.boot.portal.model.entity.Moments;
import com.youlai.boot.portal.model.form.MomentsForm;
import com.youlai.boot.portal.model.vo.MomentsPageVO;
import org.mapstruct.Mapper;

import java.util.List;


/**
 * 精彩瞬间对象转换器
 *
 * @author 243269112
 * @since 2026/4/23
 */
@Mapper(componentModel = "spring")
public interface MomentsConverter {

    MomentsPageVO toPageVo(Moments Moments);

    Page<MomentsPageVO> toPageVo(Page<Moments> page);

    Moments toEntity(MomentsForm MomentsForm);

    MomentsForm toForm(Moments entity);

    List<MomentsPageVO> toPageVo(List<Moments> bo);
}
