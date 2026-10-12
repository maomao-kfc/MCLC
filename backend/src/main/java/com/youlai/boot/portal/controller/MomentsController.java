package com.youlai.boot.portal.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.youlai.boot.common.annotation.Log;
import com.youlai.boot.common.annotation.RepeatSubmit;
import com.youlai.boot.common.enums.LogModuleEnum;
import com.youlai.boot.core.web.PageResult;
import com.youlai.boot.core.web.Result;
import com.youlai.boot.portal.model.form.MomentsForm;
import com.youlai.boot.portal.model.query.MomentsQuery;
import com.youlai.boot.portal.model.vo.MomentsPageVO;
import com.youlai.boot.portal.service.MomentsService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;


/**
 *精彩瞬间宾控制层
 *
 * @author 243269112
 * @since 2026/4/23
 */
@Tag(name = "精彩瞬间接口")
@RestController
@RequestMapping("/api/v1/moments")
@RequiredArgsConstructor
public class MomentsController {

    private final MomentsService MomentsService;

    @Operation(summary = "精彩瞬间分页列表")
    @GetMapping
    @Log(value = "精彩瞬间分页列表", module = LogModuleEnum.MOMENTS)
    public PageResult<MomentsPageVO> getMomentsPage(MomentsQuery queryParams) {
        Page<MomentsPageVO> result = MomentsService.getMomentsPage(queryParams);
        return PageResult.success(result);
    }

    @Operation(summary = "新增精彩瞬间")
    @PostMapping
    @PreAuthorize("@ss.hasPerm('sys:moments:create')")
    @RepeatSubmit
    public Result<?> addMoments(@Valid @RequestBody MomentsForm MomentsForm) {
        boolean result = MomentsService.saveMoments(MomentsForm);
        return Result.judge(result);
    }


    @Operation(summary = "修改精彩瞬间")
    @PutMapping(value = "/{id}")
    @PreAuthorize("@ss.hasPerm('sys:moments:update')")
    public Result<?> updateMoments(@Valid @RequestBody MomentsForm MomentsForm) {
        boolean result = MomentsService.saveMoments(MomentsForm);
        return Result.judge(result);
    }

    @Operation(summary = "删除精彩瞬间")
    @DeleteMapping("/{ids}")
    @PreAuthorize("@ss.hasPerm('sys:moments:delete')")
    public Result<Void> deleteMomentss(
            @Parameter(description = "删除精彩瞬间，多个以英文逗号(,)拼接") @PathVariable String ids
    ) {
        MomentsService.deleteMomentss(ids);
        return Result.success();
    }

    @Operation(summary = "修改精彩瞬间状态")
    @PutMapping(value = "/{MomentsId}/status")
    @PreAuthorize("@ss.hasPerm('sys:moments:update')")
    public Result<?> updateMomentsStatus(
            @Parameter(description = "精彩瞬间ID") @PathVariable Long momentstId,
            @Parameter(description = "状态(1:启用;0:禁用)") @RequestParam Integer status
    ) {
        boolean result = MomentsService.updateMomentsStatus(momentstId, status);
        return Result.judge(result);
    }

}
