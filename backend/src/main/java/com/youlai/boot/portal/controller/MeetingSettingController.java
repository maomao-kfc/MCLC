package com.youlai.boot.portal.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.youlai.boot.common.annotation.Log;
import com.youlai.boot.common.annotation.RepeatSubmit;
import com.youlai.boot.common.enums.LogModuleEnum;
import com.youlai.boot.core.web.PageResult;
import com.youlai.boot.core.web.Result;
import com.youlai.boot.portal.model.form.MeetingSettingForm;
import com.youlai.boot.portal.model.query.MeetingSettingQuery;
import com.youlai.boot.portal.model.vo.MeetingSettingPageVO;
import com.youlai.boot.portal.service.MeetingSettingService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;


/**
 *会议设置控制层
 *
 * @author 243269112
 * @since 2026/4/23
 */
@Tag(name = "会议设置接口")
@RestController
@RequestMapping("/api/v1/meetingSetting")
@RequiredArgsConstructor
public class MeetingSettingController {

    private final MeetingSettingService meeetingSettingService;

    @Operation(summary = "会议设置分页列表")
    @GetMapping
    @Log(value = "会议设置分页列表", module = LogModuleEnum.MEETINGSETTING)
    public PageResult<MeetingSettingPageVO> getMeetingSettingPage(MeetingSettingQuery queryParams) {
        Page<MeetingSettingPageVO> result = meeetingSettingService.getMeetingSettingPage(queryParams);
        return PageResult.success(result);
    }

    @Operation(summary = "新增会议设置")
    @PostMapping
    @PreAuthorize("@ss.hasPerm('sys:meetingSetting:create')")
    @RepeatSubmit
    public Result<?> addMeetingSetting(@Valid @RequestBody MeetingSettingForm MeetingSettingForm) {
        boolean result = meeetingSettingService.saveMeetingSetting(MeetingSettingForm);
        return Result.judge(result);
    }


    @Operation(summary = "修改会议设置")
    @PutMapping(value = "/{id}")
    @PreAuthorize("@ss.hasPerm('sys:meetingSetting:update')")
    public Result<?> updateMeetingSetting(@Valid @RequestBody MeetingSettingForm MeetingSettingForm) {
        boolean result = meeetingSettingService.saveMeetingSetting(MeetingSettingForm);
        return Result.judge(result);
    }

    @Operation(summary = "删除会议设置")
    @DeleteMapping("/{ids}")
    @PreAuthorize("@ss.hasPerm('sys:meetingSetting:delete')")
    public Result<Void> deleteMeetingSettings(
            @Parameter(description = "删除会议设置，多个以英文逗号(,)拼接") @PathVariable String ids
    ) {
        meeetingSettingService.deleteMeetingSettings(ids);
        return Result.success();
    }

    @Operation(summary = "修改会议设置状态")
    @PutMapping(value = "/{meetingSettingId}/status")
    @PreAuthorize("@ss.hasPerm('sys:meetingSetting:update')")
    public Result<?> updateMeetingSettingStatus(
            @Parameter(description = "会议设置ID") @PathVariable Long meetingSettingtId,
            @Parameter(description = "状态(1:启用;0:禁用)") @RequestParam Integer status
    ) {
        boolean result = meeetingSettingService.updateMeetingSettingStatus(meetingSettingtId, status);
        return Result.judge(result);
    }

}
