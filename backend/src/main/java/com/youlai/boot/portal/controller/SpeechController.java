package com.youlai.boot.portal.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.youlai.boot.common.annotation.Log;
import com.youlai.boot.common.annotation.RepeatSubmit;
import com.youlai.boot.common.enums.LogModuleEnum;
import com.youlai.boot.core.web.PageResult;
import com.youlai.boot.core.web.Result;
import com.youlai.boot.portal.model.entity.Speech;
import com.youlai.boot.portal.model.form.SpeechForm;
import com.youlai.boot.portal.model.query.SpeechQuery;
import com.youlai.boot.portal.model.vo.SpeechPageVO;
import com.youlai.boot.portal.service.SpeechService;
import com.youlai.boot.portal.service.SpeechServiceV1;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;


/**
 *演讲集锦宾控制层
 *
 * @author 243269112
 * @since 2026/4/23
 */
@Tag(name = "演讲集锦接口")
@RestController
@RequestMapping("/api/v1/speech")
@RequiredArgsConstructor
public class SpeechController {

    private final SpeechService speechService;

    private final SpeechServiceV1 speechServiceV1;


    @Operation(summary = "演讲集锦分页列表")
    @GetMapping
    @Log(value = "演讲集锦分页列表", module = LogModuleEnum.SPEECH)
    public PageResult<SpeechPageVO> getSpeechPage(SpeechQuery queryParams) {
        Page<SpeechPageVO> result = speechServiceV1.getSpeechPage(queryParams);
        return PageResult.success(result);
    }

    @Operation(summary = "获取讲演集锦ID查询信息")
    @GetMapping("/{id}/form")
    public Result<Speech> getSpeechByID(@Parameter(description = "ID") @PathVariable Long id) {

        Speech speech = speechService.getSpeechByID(id);
        return Result.success(speech);
    }

    @Operation(summary = "新增演讲集锦")
    @PostMapping
    @PreAuthorize("@ss.hasPerm('sys:speech:create')")
    @RepeatSubmit
    public Result<?> addSpeech(@Valid @RequestBody SpeechForm SpeechForm) {
        boolean result = speechService.saveSpeech(SpeechForm);
        return Result.judge(result);
    }


    @Operation(summary = "修改演讲集锦")
    @PutMapping(value = "/{id}")
    @PreAuthorize("@ss.hasPerm('sys:speech:update')")
    public Result<?> updateSpeech(@Valid @RequestBody SpeechForm SpeechForm) {
        boolean result = speechService.saveSpeech(SpeechForm);
        return Result.judge(result);
    }

    @Operation(summary = "删除演讲集锦")
    @DeleteMapping("/{ids}")
    @PreAuthorize("@ss.hasPerm('sys:speech:delete')")
    public Result<Void> deleteSpeechs(
            @Parameter(description = "删除演讲集锦，多个以英文逗号(,)拼接") @PathVariable String ids
    ) {
        speechService.deleteSpeechs(ids);
        return Result.success();
    }

    @Operation(summary = "修改演讲集锦状态")
    @PutMapping(value = "/{SpeechId}/status")
    @PreAuthorize("@ss.hasPerm('sys:speech:update')")
    public Result<?> updateSpeechStatus(
            @Parameter(description = "演讲集锦ID") @PathVariable Long SpeechtId,
            @Parameter(description = "状态(1:启用;0:禁用)") @RequestParam Integer status
    ) {
        boolean result = speechService.updateSpeechStatus(SpeechtId, status);
        return Result.judge(result);
    }

}
