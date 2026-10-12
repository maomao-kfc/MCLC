package com.youlai.boot.portal.service;


import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.youlai.boot.portal.model.entity.Speech;
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
public interface SpeechService extends IService<Speech> {

    /**
     * 演讲分页列表
     *
     * @param queryParams
     * @return
     */
    Page<SpeechPageVO> getSpeechPage(SpeechQuery queryParams);

    /**
     *
     * @param SpeechForm
     * @return
     */
    boolean saveSpeech(SpeechForm SpeechForm);

    /**
     * 修改演讲状态
     *
     * @param SpeechId 演讲ID
     * @param status 角色状态(1:启用；0:禁用)
     * @return {@link Boolean}
     */
    boolean updateSpeechStatus(Long SpeechId, Integer status);

    /**
     * 批量删除演讲
     *
     * @param ids 演讲ID，多个使用英文逗号(,)分割
     */
    void deleteSpeechs(String ids);

    /**
     * 演讲列表
     *
     * @param queryParams
     * @return
     */
    List<SpeechPageVO> getSpeechList(SpeechQuery queryParams);

    /**
     * 根据ID查询演讲对象
     *
     * @param id
     * @return
     */
    Speech getSpeechByID(Long id);
}
