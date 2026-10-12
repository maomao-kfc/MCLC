package com.youlai.boot.portal.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.youlai.boot.common.annotation.Log;
import com.youlai.boot.common.annotation.RepeatSubmit;
import com.youlai.boot.common.enums.LogModuleEnum;
import com.youlai.boot.core.web.PageResult;
import com.youlai.boot.core.web.Result;
import com.youlai.boot.portal.model.form.ArchitectureForm;
import com.youlai.boot.portal.model.query.ArchitectureQuery;
import com.youlai.boot.portal.model.vo.ArchitecturePageVO;
import com.youlai.boot.portal.service.ArchitectureService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;


/**
 * 组织架构控制层
 *
 * @author 243269112
 * @since 2026/5/25
 */
@Tag(name = "组织架构接口")
@RestController
@RequestMapping("/api/v1/architecture")
@RequiredArgsConstructor
public class ArchitectureController {

    private final ArchitectureService architectureService;

    @Operation(summary = "组织架构分页列表")
    @GetMapping
    @Log(value = "组织架构分页列表", module = LogModuleEnum.ARCHITECTURE)
    public PageResult<ArchitecturePageVO> getArchitecturePage(ArchitectureQuery queryParams) {
        Page<ArchitecturePageVO> result = architectureService.getArchitecturePage(queryParams);
        return PageResult.success(result);
    }

    @Operation(summary = "新增组织架构")
    @PostMapping
    @PreAuthorize("@ss.hasPerm('sys:architecture:create')")
    @RepeatSubmit
    public Result<?> addArchitecture(@Valid @RequestBody ArchitectureForm architectureForm) {
        boolean result = architectureService.saveArchitecture(architectureForm);
        return Result.judge(result);
    }


    @Operation(summary = "修改组织架构")
    @PutMapping(value = "/{id}")
    @PreAuthorize("@ss.hasPerm('sys:architecture:update')")
    public Result<?> updateArchitecture(@Valid @RequestBody ArchitectureForm architectureForm) {
        boolean result = architectureService.saveArchitecture(architectureForm);
        return Result.judge(result);
    }

    @Operation(summary = "删除组织架构")
    @DeleteMapping("/{ids}")
    @PreAuthorize("@ss.hasPerm('sys:architecture:delete')")
    public Result<Void> deleteArchitectures(
            @Parameter(description = "删除组织架构，多个以英文逗号(,)拼接") @PathVariable String ids
    ) {
        architectureService.deleteArchitectures(ids);
        return Result.success();
    }

    @Operation(summary = "修改组织架构状态")
    @PutMapping(value = "/{architectureId}/status")
    @PreAuthorize("@ss.hasPerm('sys:architecture:update')")
    public Result<?> updateArchitectureStatus(
            @Parameter(description = "组织架构ID") @PathVariable Long architectureId,
            @Parameter(description = "状态(1:启用;0:禁用)") @RequestParam Integer status
    ) {
        boolean result = architectureService.updateArchitectureStatus(architectureId, status);
        return Result.judge(result);
    }

}
