package com.youlai.boot.portal.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.youlai.boot.common.annotation.Log;
import com.youlai.boot.common.annotation.RepeatSubmit;
import com.youlai.boot.common.enums.LogModuleEnum;
import com.youlai.boot.core.web.PageResult;
import com.youlai.boot.core.web.Result;
import com.youlai.boot.portal.model.form.HotelForm;
import com.youlai.boot.portal.model.form.QuestionForm;
import com.youlai.boot.portal.model.query.HotelQuery;
import com.youlai.boot.portal.model.query.QuestionQuery;
import com.youlai.boot.portal.model.vo.HotelPageVO;
import com.youlai.boot.portal.model.vo.QuestionPageVO;
import com.youlai.boot.portal.service.HotelService;
import com.youlai.boot.portal.service.QuestionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;


/**
 *问题解答控制层
 *
 * @author 243269112
 * @since 2026/4/23
 */
@Tag(name = "问题解答接口")
@RestController
@RequestMapping("/api/v1/question")
@RequiredArgsConstructor
public class QuestionController {

    private final QuestionService questionService;

    @Operation(summary = "问题解答分页列表")
    @GetMapping
    @Log(value = "问题解答分页列表", module = LogModuleEnum.QUESTION)
    public PageResult<QuestionPageVO> getQuestionPage(QuestionQuery queryParams) {
        Page<QuestionPageVO> result = questionService.getQuestionPage(queryParams);
        return PageResult.success(result);
    }

    @Operation(summary = "新增问题解答")
    @PostMapping
    @PreAuthorize("@ss.hasPerm('sys:question:create')")
    @RepeatSubmit
    public Result<?> addQuestion(@Valid @RequestBody QuestionForm questionForm) {
        boolean result = questionService.saveQuestion(questionForm);
        return Result.judge(result);
    }


    @Operation(summary = "修改问题解答")
    @PutMapping(value = "/{id}")
    @PreAuthorize("@ss.hasPerm('sys:question:update')")
    public Result<?> updateQuestion(@Valid @RequestBody QuestionForm questionForm) {
        boolean result = questionService.saveQuestion(questionForm);
        return Result.judge(result);
    }

    @Operation(summary = "删除问题解答")
    @DeleteMapping("/{ids}")
    @PreAuthorize("@ss.hasPerm('sys:question:delete')")
    public Result<Void> deleteQuestions(
            @Parameter(description = "删除问题解答，多个以英文逗号(,)拼接") @PathVariable String ids
    ) {
        questionService.deleteQuestions(ids);
        return Result.success();
    }

    @Operation(summary = "修改问题解答状态")
    @PutMapping(value = "/{hotelId}/status")
    @PreAuthorize("@ss.hasPerm('sys:question:update')")
    public Result<?> updateQuestionStatus(
            @Parameter(description = "问题解答ID") @PathVariable Long questionId,
            @Parameter(description = "状态(1:启用;0:禁用)") @RequestParam Integer status
    ) {
        boolean result = questionService.updateQuestionStatus(questionId, status);
        return Result.judge(result);
    }

}
