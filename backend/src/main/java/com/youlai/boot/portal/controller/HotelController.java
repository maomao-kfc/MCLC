package com.youlai.boot.portal.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.youlai.boot.common.annotation.Log;
import com.youlai.boot.common.annotation.RepeatSubmit;
import com.youlai.boot.common.enums.LogModuleEnum;
import com.youlai.boot.core.web.PageResult;
import com.youlai.boot.core.web.Result;
import com.youlai.boot.portal.model.form.HotelForm;
import com.youlai.boot.portal.model.query.HotelQuery;
import com.youlai.boot.portal.model.vo.HotelPageVO;
import com.youlai.boot.portal.service.HotelService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;


/**
 *酒店信息宾控制层
 *
 * @author 243269112
 * @since 2026/4/23
 */
@Tag(name = "酒店信息接口")
@RestController
@RequestMapping("/api/v1/hotel")
@RequiredArgsConstructor
public class HotelController {

    private final HotelService hotelService;

    @Operation(summary = "酒店信息分页列表")
    @GetMapping
    @Log(value = "酒店信息分页列表", module = LogModuleEnum.HOTEL)
    public PageResult<HotelPageVO> getHotelPage(HotelQuery queryParams) {
        Page<HotelPageVO> result = hotelService.getHotelPage(queryParams);
        return PageResult.success(result);
    }

    @Operation(summary = "新增酒店信息")
    @PostMapping
    @PreAuthorize("@ss.hasPerm('sys:hotel:create')")
    @RepeatSubmit
    public Result<?> addHotel(@Valid @RequestBody HotelForm hotelForm) {
        boolean result = hotelService.saveHotel(hotelForm);
        return Result.judge(result);
    }


    @Operation(summary = "修改酒店信息")
    @PutMapping(value = "/{id}")
    @PreAuthorize("@ss.hasPerm('sys:hotel:update')")
    public Result<?> updateHotel(@Valid @RequestBody HotelForm hotelForm) {
        boolean result = hotelService.saveHotel(hotelForm);
        return Result.judge(result);
    }

    @Operation(summary = "删除酒店信息")
    @DeleteMapping("/{ids}")
    @PreAuthorize("@ss.hasPerm('sys:hotel:delete')")
    public Result<Void> deleteHotels(
            @Parameter(description = "删除酒店信息，多个以英文逗号(,)拼接") @PathVariable String ids
    ) {
        hotelService.deleteHotels(ids);
        return Result.success();
    }

    @Operation(summary = "修改酒店信息状态")
    @PutMapping(value = "/{hotelId}/status")
    @PreAuthorize("@ss.hasPerm('sys:hotel:update')")
    public Result<?> updateHotelStatus(
            @Parameter(description = "酒店信息ID") @PathVariable Long hoteltId,
            @Parameter(description = "状态(1:启用;0:禁用)") @RequestParam Integer status
    ) {
        boolean result = hotelService.updateHotelStatus(hoteltId, status);
        return Result.judge(result);
    }

}
