package com.youlai.boot.portal.converter;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.youlai.boot.portal.model.entity.Question;
import com.youlai.boot.portal.model.form.QuestionForm;
import com.youlai.boot.portal.model.vo.QuestionPageVO;
import org.mapstruct.Mapper;

import java.util.List;


/**
 * 问题解答对象转换器
 *
 * @author 243269112
 * @since 2026/4/23
 */
@Mapper(componentModel = "spring")
public interface QuestionConverter {

    QuestionPageVO toPageVo(Question Question);

    Page<QuestionPageVO> toPageVo(Page<Question> page);

    Question toEntity(QuestionForm QuestionForm);

    QuestionForm toForm(Question entity);

    List<QuestionPageVO> toPageVo(List<Question> bo);
}
