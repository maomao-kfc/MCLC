package com.youlai.boot.portal.controller;

import com.youlai.boot.common.annotation.Log;
import com.youlai.boot.common.annotation.RepeatSubmit;
import com.youlai.boot.common.enums.LogModuleEnum;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.youlai.boot.core.web.Result;
import com.youlai.boot.portal.model.form.MeetingDateForm;
import com.youlai.boot.portal.model.vo.MeetingDateVO;
import com.youlai.boot.portal.service.MeetingDateService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "会议日期接口")
@RestController
@RequestMapping("/api/v1/meetDate")
@RequiredArgsConstructor
public class MeetDateController {

    private final MeetingDateService meetingDateService;

    @Operation(summary = "会议日期分页")
    @GetMapping
    public Result<Page<MeetingDateVO>> getMeetingDatePage(
            @Parameter(description = "页码") @RequestParam(defaultValue = "1") int pageNum,
            @Parameter(description = "每页条数") @RequestParam(defaultValue = "10") int pageSize,
            @Parameter(description = "国家类型") @RequestParam(required = false) String countryType) {
        Page<MeetingDateVO> result = meetingDateService.getMeetingDatePage(pageNum, pageSize, countryType);
        return Result.success(result);
    }

    @Operation(summary = "会议日期详情")
    @GetMapping("/{id}")
    public Result<MeetingDateVO> getMeetingDateById(@Parameter(description = "ID") @PathVariable Long id) {
        MeetingDateVO result = meetingDateService.getMeetingDateById(id);
        return Result.success(result);
    }

    @Operation(summary = "新增会议日期")
    @PostMapping
    @PreAuthorize("@ss.hasPerm('sys:meetDate:create')")
    @RepeatSubmit
    public Result<?> addMeetingDate(@Valid @RequestBody MeetingDateForm form) {
        boolean result = meetingDateService.saveMeetingDate(form);
        return Result.judge(result);
    }

    @Operation(summary = "修改会议日期")
    @PutMapping("/{id}")
        @PreAuthorize("@ss.hasPerm('sys:meetDate:update')")
    public Result<?> updateMeetingDate(@Valid @RequestBody MeetingDateForm form) {
        boolean result = meetingDateService.updateMeetingDate(form);
        return Result.judge(result);
    }

    @Operation(summary = "删除会议日期")
    @DeleteMapping("/{ids}")
    @PreAuthorize("@ss.hasPerm('sys:meetDate:delete')")
    public Result<Void> deleteMeetingDate(
            @Parameter(description = "删除会议日期，多个以英文逗号(,)拼接") @PathVariable String ids
    ) {
        meetingDateService.deleteMeetingDateBatch(ids);
        return Result.success();
    }
}
