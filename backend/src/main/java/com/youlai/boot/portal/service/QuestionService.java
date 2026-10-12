package com.youlai.boot.portal.service;


import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.youlai.boot.portal.model.entity.Question;
import com.youlai.boot.portal.model.form.QuestionForm;
import com.youlai.boot.portal.model.query.QuestionQuery;
import com.youlai.boot.portal.model.vo.QuestionPageVO;

import java.util.List;


/**
 * 问题解答业务接口层
 *
 * @author 243269112
 * @since 2024/4/23
 */
public interface QuestionService extends IService<Question> {

    /**
     * 问题解答分页列表
     *
     * @param queryParams
     * @return
     */
    Page<QuestionPageVO> getQuestionPage(QuestionQuery queryParams);

    /**
     *
     * @param questionForm
     * @return
     */
    boolean saveQuestion(QuestionForm questionForm);

    /**
     * 修改问题解答状态
     *
     * @param questionId 问题解答ID
     * @param status 角色状态(1:启用；0:禁用)
     * @return {@link Boolean}
     */
    boolean updateQuestionStatus(Long questionId, Integer status);

    /**
     * 批量删除问题解答
     *
     * @param ids 问题解答ID，多个使用英文逗号(,)分割
     */
    void deleteQuestions(String ids);

    /**
     * 问题解答列表
     *
     * @param queryParams
     * @return
     */
    List<QuestionPageVO> getQuestionList(QuestionQuery queryParams);
}
