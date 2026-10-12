package com.youlai.boot.portal.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.youlai.boot.common.annotation.Log;
import com.youlai.boot.common.annotation.RepeatSubmit;
import com.youlai.boot.common.enums.LogModuleEnum;
import com.youlai.boot.core.web.PageResult;
import com.youlai.boot.core.web.Result;
import com.youlai.boot.portal.model.form.GuestForm;
import com.youlai.boot.portal.model.query.GuestQuery;
import com.youlai.boot.portal.model.vo.GuestPageVO;
import com.youlai.boot.portal.service.GuestService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;


/**
 *特邀嘉宾控制层
 *
 * @author 243269112
 * @since 2026/4/22
 */
@Tag(name = "特邀嘉宾接口")
@RestController
@RequestMapping("/api/v1/guest")
@RequiredArgsConstructor
public class GuestController {

    private final GuestService guestService;

    @Operation(summary = "特邀嘉宾分页列表")
    @GetMapping
    @Log(value = "特邀嘉宾分页列表", module = LogModuleEnum.GUEST)
    public PageResult<GuestPageVO> getGuestPage(GuestQuery queryParams) {
        Page<GuestPageVO> result = guestService.getGuestPage(queryParams);
        return PageResult.success(result);
    }

    @Operation(summary = "新增特邀嘉宾")
    @PostMapping
    @PreAuthorize("@ss.hasPerm('sys:guest:create')")
    @RepeatSubmit
    public Result<?> addGuest(@Valid @RequestBody GuestForm guestForm) {
        boolean result = guestService.saveGuest(guestForm);
        return Result.judge(result);
    }


    @Operation(summary = "修改特邀嘉宾")
    @PutMapping(value = "/{id}")
    @PreAuthorize("@ss.hasPerm('sys:guest:update')")
    public Result<?> updateGuest(@Valid @RequestBody GuestForm guestForm) {
        boolean result = guestService.saveGuest(guestForm);
        return Result.judge(result);
    }

    @Operation(summary = "删除特邀嘉宾")
    @DeleteMapping("/{ids}")
    @PreAuthorize("@ss.hasPerm('sys:guest:delete')")
    public Result<Void> deleteGuests(
            @Parameter(description = "删除特邀嘉宾，多个以英文逗号(,)拼接") @PathVariable String ids
    ) {
        guestService.deleteGuests(ids);
        return Result.success();
    }

    @Operation(summary = "修改特邀嘉宾状态")
    @PutMapping(value = "/{guestId}/status")
    @PreAuthorize("@ss.hasPerm('sys:guest:update')")
    public Result<?> updateRoleStatus(
            @Parameter(description = "特邀嘉宾ID") @PathVariable Long guestId,
            @Parameter(description = "状态(1:启用;0:禁用)") @RequestParam Integer status
    ) {
        boolean result = guestService.updateGuestStatus(guestId, status);
        return Result.judge(result);
    }

}
