package com.youlai.boot.portal.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.youlai.boot.common.annotation.Log;
import com.youlai.boot.common.annotation.RepeatSubmit;
import com.youlai.boot.common.enums.LogModuleEnum;
import com.youlai.boot.core.web.Result;
import com.youlai.boot.portal.model.form.MeetingSessionForm;
import com.youlai.boot.portal.model.vo.MeetingScheduleVO;
import com.youlai.boot.portal.service.MeetingInfoService;
import com.youlai.boot.portal.service.MeetingSessionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

/**
 * 会议日程控制类
 */

@Slf4j
@RestController
@RequiredArgsConstructor
@Tag(name = "会议日程")
@RequestMapping("/api/v1/meetingSchedule")
public class MeetingScheduleController {

    private final MeetingInfoService meetingInfoService;

    @Operation(summary = "查询所有日程（前台网页展示）")
    @GetMapping("allMeetingSchedule")
    public Result<List<MeetingScheduleVO>> getAllMeetingSchedule() {
        List<MeetingScheduleVO> schedules = meetingInfoService.getAllSchedules();
        return Result.success(schedules);
    }

    // 分页查询
    @Operation(summary = "分页查询会议列表")
    @GetMapping("/page")
    public Result<IPage<MeetingScheduleVO>> getAllSchedulePage(
        @RequestParam(defaultValue = "1") Integer pageNum,
        @RequestParam(defaultValue = "10") Integer pageSize,
        @RequestParam(required = false) String meetName) {
        return Result.success(meetingInfoService.getAllSchedulesPage(pageNum, pageSize, meetName));
    }

    @Operation(summary = "新增日程板块")
    @PostMapping("/session")
    @RepeatSubmit
    public Result<?> createSession(@Valid @RequestBody MeetingSessionForm form) {
        boolean result = meetingInfoService.createSchedule(form);
        return Result.judge(result);
    }

    @Operation(summary = "修改日程板块")
    @PutMapping("/session/{id}")
    public Result<?> updateSession(
            @Parameter(description = "板块ID") @PathVariable Long id,
            @Valid @RequestBody MeetingSessionForm form) {
        form.setId(id);
        boolean result = meetingInfoService.updateSession(form);
        return Result.judge(result);
    }

    @Operation(summary = "删除日程板块")
    @DeleteMapping("/session/{id}")
    public Result<?> deleteSession(
            @Parameter(description = "板块ID") @PathVariable Long id) {
        boolean result = meetingInfoService.deleteSession(id);
        return Result.judge(result);
    }

    @Operation(summary = "更新会议状态")
    @PutMapping("/{meetingId}/status")
    public Result<?> updateMeetingStatus(
            @Parameter(description = "会议ID") @PathVariable Long meetingId,
            @Parameter(description = "状态值(0-禁用 1-启用)") @RequestParam Integer status) {
        boolean result = meetingInfoService.updateMeetingStatus(meetingId, status);
        return Result.judge(result);
    }

    @Operation(summary = "更新会议基础信息")
    @PutMapping("/meeting/{meetingId}")
    public Result<?> updateMeeting(
            @Parameter(description = "会议ID") @PathVariable Long meetingId,
            @Parameter(description = "会议名称") @RequestParam(required = false) String name,
            @Parameter(description = "会议名称(英文)") @RequestParam(required = false) String nameEn,
            @Parameter(description = "开会日期") @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate meetingDate,
            @Parameter(description = "会场") @RequestParam(required = false) String venue,
            @Parameter(description = "会场(英文)") @RequestParam(required = false) String venueEn,
            @Parameter(description = "排序") @RequestParam(required = false) Integer sortOrder,
            @Parameter(description = "状态") @RequestParam(required = false) Integer status) {
        boolean result = meetingInfoService.updateMeeting(meetingId, name, nameEn, meetingDate, venue, venueEn, sortOrder, status);
        return Result.judge(result);
    }

    @Operation(summary = "删除会议")
    @DeleteMapping("/{meetingId}")
    public Result<?> deleteMeeting(
            @Parameter(description = "会议ID") @PathVariable Long meetingId) {
        boolean result = meetingInfoService.deleteMeeting(meetingId);
        return Result.judge(result);
    }

    @Operation(summary = "批量更新板块排序值")
    @PutMapping("/session/batchSortOrder")
    public Result<?> batchUpdateSessionSortOrder(@RequestBody List<Map<String, Long>> sortOrders) {
        boolean result = meetingInfoService.batchUpdateSessionSortOrder(sortOrders);
        return Result.judge(result);
    }
}
