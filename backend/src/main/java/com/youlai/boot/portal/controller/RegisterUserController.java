package com.youlai.boot.portal.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.youlai.boot.common.annotation.Log;
import com.youlai.boot.common.annotation.RepeatSubmit;
import com.youlai.boot.common.enums.LogModuleEnum;
import com.youlai.boot.core.web.PageResult;
import com.youlai.boot.core.web.Result;
import com.youlai.boot.portal.model.form.CheckInForm;
import com.youlai.boot.portal.model.form.RegisterUserForm;
import com.youlai.boot.portal.model.query.RegisterUserQuery;
import com.youlai.boot.portal.model.vo.RegisterUserPageVO;
import com.youlai.boot.portal.service.RegisterUserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;import org.springframework.web.bind.annotation.*;

import java.util.Map;

@Tag(name = "注册用户接口")
@RestController
@RequestMapping("/api/v1/registerUser")
@RequiredArgsConstructor
public class RegisterUserController {

    private final RegisterUserService registerUserService;

    @Operation(summary = "注册用户分页列表")
    @GetMapping
    @Log(value = "注册用户分页列表", module = LogModuleEnum.REGISTERUSER)
    public PageResult<RegisterUserPageVO> getRegisterUserPage(RegisterUserQuery queryParams) {
        Page<RegisterUserPageVO> result = registerUserService.getRegisterUserPage(queryParams);
        return PageResult.success(result);
    }

    @Operation(summary = "新增注册用户")
    @PostMapping
    @RepeatSubmit
    public Result<Map<String, Object>> addRegisterUser(@Valid @RequestBody RegisterUserForm form) {
        Map<String, Object> result = registerUserService.saveRegisterUser(form);
        return Result.success(result);
    }

    @Operation(summary = "修改注册用户")
    @PutMapping(value = "/{id}")
    public Result<Map<String, Object>> updateRegisterUser(@RequestBody RegisterUserForm form) {
        Map<String, Object> result = registerUserService.saveRegisterUser(form);
        return Result.success(result);
    }

    @Operation(summary = "删除注册用户")
    @DeleteMapping("/{ids}")

    public Result<Void> deleteRegisterUsers(
            @Parameter(description = "删除注册用户，多个以英文逗号(,)拼接") @PathVariable String ids
    ) {
        registerUserService.deleteRegisterUsers(ids);
        return Result.success();
    }

    @Operation(summary = "注册用户详情")
    @GetMapping("/{id}")

    public Result<RegisterUserPageVO> getRegisterUserDetail(
            @Parameter(description = "注册用户ID") @PathVariable Long id
    ) {
        RegisterUserPageVO vo = registerUserService.getRegisterUserDetail(id);
        return Result.success(vo);
    }

    @Operation(summary = "审核注册用户")
    @PostMapping("/{id}/audit")
    public Result<Void> auditRegisterUser(
            @Parameter(description = "注册用户ID") @PathVariable Long id,
            @RequestBody Map<String, Object> params
    ) {
            Integer reviewStatus = params.get("reviewStatus") != null ? ((Number) params.get("reviewStatus")).intValue() : null;
            String rejectReason = (String) params.get("rejectReason");
            registerUserService.auditRegisterUser(id, reviewStatus, rejectReason);
        return Result.success();
    }

    @Operation(summary = "签到")
    @PostMapping("/checkIn")
    public Result<Map<String, Object>> checkIn(@Valid @RequestBody CheckInForm form) {
        Map<String, Object> result = registerUserService.checkIn(form);
        return Result.success(result);
    }
}
