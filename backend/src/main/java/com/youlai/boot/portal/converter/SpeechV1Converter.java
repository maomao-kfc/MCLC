package com.youlai.boot.portal.converter;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.youlai.boot.portal.model.entity.Speech;
import com.youlai.boot.portal.model.entity.SpeechV1;
import com.youlai.boot.portal.model.form.SpeechForm;
import com.youlai.boot.portal.model.vo.SpeechPageVO;
import org.mapstruct.Mapper;

import java.util.List;


/**
 * 演讲集锦对象转换器
 *
 * @author 243269112
 * @since 2026/4/23
 */
@Mapper(componentModel = "spring")
public interface SpeechV1Converter {

    Page<SpeechPageVO> toPageVo(Page<SpeechV1> page);

}
