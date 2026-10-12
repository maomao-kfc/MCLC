package com.youlai.boot.portal.service;


import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.youlai.boot.portal.model.entity.Speech;
import com.youlai.boot.portal.model.entity.SpeechV1;
import com.youlai.boot.portal.model.form.SpeechForm;
import com.youlai.boot.portal.model.query.SpeechQuery;
import com.youlai.boot.portal.model.vo.SpeechPageVO;

import java.util.List;


/**
 * 演讲业务接口层
 *
 * @author 243269112
 * @since 2024/4/23
 */
public interface SpeechServiceV1 extends IService<SpeechV1> {

    /**
     * 演讲分页列表
     *
     * @param queryParams
     * @return
     */
    Page<SpeechPageVO> getSpeechPage(SpeechQuery queryParams);

}
