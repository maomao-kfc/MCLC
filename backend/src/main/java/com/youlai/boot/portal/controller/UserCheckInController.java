package com.youlai.boot.portal.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.youlai.boot.common.annotation.Log;
import com.youlai.boot.common.enums.LogModuleEnum;
import com.youlai.boot.core.web.PageResult;
import com.youlai.boot.core.web.Result;
import com.youlai.boot.portal.model.query.UserCheckInQuery;
import com.youlai.boot.portal.model.vo.UserCheckInPageVO;
import com.youlai.boot.portal.service.UserCheckInService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@Tag(name = "签到记录接口")
@RestController
@RequestMapping("/api/v1/userCheckIn")
@RequiredArgsConstructor
public class UserCheckInController {

    private final UserCheckInService userCheckInService;

    @Operation(summary = "签到记录分页列表")
    @GetMapping
    @Log(value = "签到记录分页列表", module = LogModuleEnum.CHECK_IN)
    public PageResult<UserCheckInPageVO> getUserCheckInPage(UserCheckInQuery queryParams) {
        Page<UserCheckInPageVO> result = userCheckInService.getUserCheckInPage(queryParams);
        return PageResult.success(result);
    }

    @Operation(summary = "删除签到记录")
    @DeleteMapping("/{ids}")
    @PreAuthorize("@ss.hasPerm('sys:userCheckIn:delete')")
    @Log(value = "删除签到记录", module = LogModuleEnum.CHECK_IN)
    public Result<Void> deleteUserCheckIns(
            @Parameter(description = "删除签到记录，多个以英文逗号(,)拼接") @PathVariable String ids
    ) {
        userCheckInService.deleteUserCheckIns(ids);
        return Result.success();
    }
}
