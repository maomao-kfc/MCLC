package com.youlai.boot.portal.service.impl;

import cn.hutool.core.lang.Assert;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.youlai.boot.core.exception.BusinessException;
import com.youlai.boot.portal.converter.QuestionConverter;
import com.youlai.boot.portal.mapper.QuestionMapper;
import com.youlai.boot.portal.model.entity.Question;
import com.youlai.boot.portal.model.form.QuestionForm;
import com.youlai.boot.portal.model.query.QuestionQuery;
import com.youlai.boot.portal.model.vo.QuestionPageVO;
import com.youlai.boot.portal.service.QuestionService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Arrays;
import java.util.List;

/**
 * 问题解答业务实现类
 *
 * @author 243269112
 * @since 2024/4/23
 */
@Service
@RequiredArgsConstructor
public class QuestionServiceImpl extends ServiceImpl<QuestionMapper, Question> implements QuestionService {

    private final QuestionConverter QuestionConverter;

    /**
     * 问题解答分页列表
     *
     * @param queryParams 问题解答查询参数
     * @return {@link Page< QuestionPageVO >} – 问题解答分页列表
     */
    @Override
    public Page<QuestionPageVO> getQuestionPage(QuestionQuery queryParams) {
        // 查询参数
        int pageNum = queryParams.getPageNum();
        int pageSize = queryParams.getPageSize();
        String keywords = queryParams.getTitle();
        String countryType = queryParams.getCountryType();

        // 查询数据
        LambdaQueryWrapper<Question> queryWrapper = new LambdaQueryWrapper<Question>()
                .and(StrUtil.isNotBlank(keywords),
                    wrapper ->
                        wrapper.like(Question::getTitle, keywords)
                )
                .orderByAsc(Question::getSort).orderByDesc(Question::getCreateTime).orderByDesc(Question::getUpdateTime);
        if (StrUtil.isNotBlank(countryType)) {
            queryWrapper.eq(Question::getCountryType, countryType);
        }
        Page<Question> QuestionPage = this.page(new Page<>(pageNum, pageSize), queryWrapper);

        // 实体转换
        return QuestionConverter.toPageVo(QuestionPage);
    }


    /**
     * 保存问题解答
     *
     * @param QuestionForm 问题解答表单数据
     * @return {@link Boolean}
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean saveQuestion(QuestionForm QuestionForm) {

        Long QuestionId = QuestionForm.getId();

        // 编辑角色时，判断角色是否存在
        Question oldQuestion = null;
        if (QuestionId != null) {
            oldQuestion = this.getById(QuestionId);
            Assert.isTrue(oldQuestion != null, "问题解答不存在");
        }

        // 实体转换
        Question Question = QuestionConverter.toEntity(QuestionForm);

        return this.saveOrUpdate(Question);
    }


    /**
     * 修改问题解答状态
     *
     * @param QuestionId 问题解答ID
     * @param status 角色状态(1:启用；0:禁用)
     * @return {@link Boolean}
     */
    @Override
    public boolean updateQuestionStatus(Long QuestionId, Integer status) {

        Question Question = this.getById(QuestionId);
        if (Question == null) {
            throw new BusinessException("问题解答不存在");
        }

        Question.setStatus(status);

        return this.updateById(Question);
    }

    /**
     * 批量删除问题解答
     *
     * @param ids 问题解答ID，多个使用英文逗号(,)分割
     */
    @Override
    public void deleteQuestions(String ids) {
        Assert.isTrue(StrUtil.isNotBlank(ids), "删除的问题解答ID不能为空");
        List<Long> QuestionIds = Arrays.stream(ids.split(","))
            .map(Long::parseLong)
            .toList();

        for (Long QuestionId : QuestionIds) {
            Question Question = this.getById(QuestionId);
            Assert.isTrue(Question != null, "问题解答不存在");

            boolean deleteResult = this.removeById(QuestionId);
        }
    }

    /**
     * 问题解答列表
     *
     * @param queryParams 问题解答查询参数
     * @return {@link Page< QuestionPageVO >} – 问题解答列表
     */
    @Override
    public List<QuestionPageVO> getQuestionList(QuestionQuery queryParams) {


        // 查询数据
        LambdaQueryWrapper<Question> queryWrapper = new LambdaQueryWrapper<Question>()
                .eq(Question::getStatus, 1)
                .orderByAsc(Question::getSort).orderByDesc(Question::getCreateTime).orderByDesc(Question::getUpdateTime);
        if (queryParams.getCountryType() != null && !queryParams.getCountryType().isEmpty()) {
            queryWrapper.eq(Question::getCountryType, queryParams.getCountryType());
        }
        List<Question> QuestionPage = this.list(queryWrapper);

        // 实体转换
        return QuestionConverter.toPageVo(QuestionPage);
    }

}
