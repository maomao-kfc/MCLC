package com.youlai.boot.portal.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.youlai.boot.common.annotation.Log;
import com.youlai.boot.common.annotation.RepeatSubmit;
import com.youlai.boot.common.enums.LogModuleEnum;
import com.youlai.boot.core.web.PageResult;
import com.youlai.boot.core.web.Result;
import com.youlai.boot.portal.model.form.ArrangeForm;
import com.youlai.boot.portal.model.query.ArrangeQuery;
import com.youlai.boot.portal.model.vo.ArrangePageVO;
import com.youlai.boot.portal.service.ArrangeService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;


/**
 *会前安排宾控制层
 *
 * @author 243269112
 * @since 2026/4/23
 */
@Tag(name = "会前安排接口")
@RestController
@RequestMapping("/api/v1/arrange")
@RequiredArgsConstructor
public class ArrangeController {

    private final ArrangeService arrangeService;

    @Operation(summary = "会前安排分页列表")
    @GetMapping
    @Log(value = "会前安排分页列表", module = LogModuleEnum.ARRANGE)
    public PageResult<ArrangePageVO> getArrangePage(ArrangeQuery queryParams) {
        Page<ArrangePageVO> result = arrangeService.getArrangePage(queryParams);
        return PageResult.success(result);
    }

    @Operation(summary = "新增会前安排")
    @PostMapping
    @PreAuthorize("@ss.hasPerm('sys:arrange:create')")
    @RepeatSubmit
    public Result<?> addArrange(@Valid @RequestBody ArrangeForm ArrangeForm) {
        boolean result = arrangeService.saveArrange(ArrangeForm);
        return Result.judge(result);
    }


    @Operation(summary = "修改会前安排")
    @PutMapping(value = "/{id}")
    @PreAuthorize("@ss.hasPerm('sys:arrange:update')")
    public Result<?> updateArrange(@Valid @RequestBody ArrangeForm ArrangeForm) {
        boolean result = arrangeService.saveArrange(ArrangeForm);
        return Result.judge(result);
    }

    @Operation(summary = "删除会前安排")
    @DeleteMapping("/{ids}")
    @PreAuthorize("@ss.hasPerm('sys:arrange:delete')")
    public Result<Void> deleteArranges(
            @Parameter(description = "删除会前安排，多个以英文逗号(,)拼接") @PathVariable String ids
    ) {
        arrangeService.deleteArranges(ids);
        return Result.success();
    }

    @Operation(summary = "修改会前安排状态")
    @PutMapping(value = "/{arrangeId}/status")
    @PreAuthorize("@ss.hasPerm('sys:arrange:update')")
    public Result<?> updateArrangeStatus(
            @Parameter(description = "会前安排ID") @PathVariable Long arrangeId,
            @Parameter(description = "状态(1:启用;0:禁用)") @RequestParam Integer status
    ) {
        boolean result = arrangeService.updateArrangeStatus(arrangeId, status);
        return Result.judge(result);
    }

}
