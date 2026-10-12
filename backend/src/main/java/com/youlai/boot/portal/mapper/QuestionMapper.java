package com.youlai.boot.portal.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.youlai.boot.portal.model.entity.Question;
import org.apache.ibatis.annotations.Mapper;

/**
 * 酒店信息持久层接口
 *
 * @author 243269112
 * @since 2026/4/23
 */
@Mapper
public interface QuestionMapper extends BaseMapper<Question> {


}
