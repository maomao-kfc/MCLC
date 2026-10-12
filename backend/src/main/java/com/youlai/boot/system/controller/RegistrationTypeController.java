package com.youlai.boot.system.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.youlai.boot.core.web.PageResult;
import com.youlai.boot.core.web.Result;
import com.youlai.boot.system.model.form.RegistrationTypeForm;
import com.youlai.boot.system.model.query.RegistrationQuery;
import com.youlai.boot.system.model.vo.RegistrationTypeVO;
import com.youlai.boot.system.service.RegistrationTypeService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

/**
 * 注册类型控制层
 */
@Tag(name = "注册类型")
@RestController
@RequestMapping("/api/v1/registrationType")
@RequiredArgsConstructor
public class RegistrationTypeController {
    private final RegistrationTypeService registrationType;

    @Operation(summary = "注册类型列表")
    @GetMapping
    public PageResult<RegistrationTypeVO> getRegistrationPage(RegistrationQuery queryParams) {
        IPage<RegistrationTypeVO> result = registrationType.getRegistrationTypePage(queryParams);
        return PageResult.success(result);
    }

    @Operation(summary = "新增注册类型")
    @PostMapping
    @PreAuthorize("@ss.hasPerm('sys:registrationType:create')")
    public Result<?> saveRegistrationType(@RequestBody @Valid RegistrationTypeForm formData) {
        boolean result = registrationType.saveRegistrationType(formData);
        return Result.judge(result);
    }

    @Operation(summary = "修改注册类型")
    @PutMapping(value = "/{id}")
    @PreAuthorize("@ss.hasPerm('sys:registrationType:update')")
    public Result<?> updateRegistrationType(
        @Parameter(description = "通知公告ID") @PathVariable Long id,
        @RequestBody @Validated RegistrationTypeForm formData
    ) {
        boolean result = registrationType.updateRegistrationType(id, formData);
        return Result.judge(result);
    }

    @Operation(summary = "删除通知公告")
    @DeleteMapping("/{ids}")
    @PreAuthorize("@ss.hasPerm('sys:registrationType:delete')")
    public Result<Void> deleteRegistrationType(
        @Parameter(description = "注册类型ID，多个以英文逗号(,)分割") @PathVariable String ids
    ) {
        boolean result = registrationType.deleteRegistrationType(ids);
        return Result.judge(result);
    }
}
