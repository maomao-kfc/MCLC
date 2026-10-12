package com.youlai.boot.portal.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.youlai.boot.portal.model.entity.Speech;
import org.apache.ibatis.annotations.Mapper;

/**
 * 演讲集锦持久层接口
 *
 * @author 243269112
 * @since 2026/4/23
 */
@Mapper
public interface SpeechMapper extends BaseMapper<Speech> {


}
