package com.youlai.boot.portal.controller;

import com.youlai.boot.common.annotation.Log;
import com.youlai.boot.common.enums.LogModuleEnum;
import com.youlai.boot.core.web.Result;
import com.youlai.boot.portal.model.entity.SpeechV1;
import com.youlai.boot.portal.model.query.*;
import com.youlai.boot.portal.model.vo.*;
import com.youlai.boot.portal.model.entity.Speech;
import com.youlai.boot.portal.model.entity.MeetingSetting;
import com.youlai.boot.portal.service.*;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;

import org.springframework.web.bind.annotation.*;

import java.util.List;


/**
 *portalweb控制层
 *
 * @author 243269112
 * @since 2026/4/23
 */
@Tag(name = "portal接口")
@RestController
@RequestMapping("/api/v1/portal")
@RequiredArgsConstructor
public class PortalController {

    private final ContactService ContactService;
    private final GuestService guestService;
    private final HotelService hotelService;
    private final QuestionService questionService;
    private final ArrangeService arrangeService;
    private final MeetingSettingService courseService;
    private final MomentsService momentsService;
    private final MeetingDateService meetingDateService;
    private final SpeechServiceV1 speechServiceV1;

    private final SpeechService speechService;

    @Deprecated
    @Operation(summary = "会议日程列表（已弃用，请使用 /api/v1/meetingSchedule/allMeetingSchedule）")
    @GetMapping("/getMeetingDateList")
    public Result<List<MeetingDateVO>> getMeetingDateList(
            @Parameter(description = "国家类型") @RequestParam(required = false) String countryType) {
        List<MeetingDateVO> result = meetingDateService.getMeetingDateList(countryType);
        return Result.success(result);
    }

    @Operation(summary = "联系人列表")
    @GetMapping("/getContactList")
    @Log(value = "联系人分页列表", module = LogModuleEnum.CONTACT)
    public Result<List<ContactPageVO>> getContactList(ContactQuery queryParams) {
        List<ContactPageVO> result = ContactService.getContactList(queryParams);
        return Result.success(result);
    }

    @Operation(summary = "特邀嘉宾列表")
    @GetMapping("/getGuestList")
    @Log(value = "特邀嘉宾分页列表", module = LogModuleEnum.GUEST)
    public Result<List<GuestPageVO>> getGuestList(GuestQuery queryParams) {
        List<GuestPageVO> result = guestService.getGuestList(queryParams);
        return Result.success(result);
    }


    @Operation(summary = "酒店信息列表")
    @GetMapping("/getHotelList")
    @Log(value = "酒店信息分页列表", module = LogModuleEnum.HOTEL)
    public Result<List<HotelPageVO>> getHotelList(HotelQuery queryParams) {
        List<HotelPageVO> result = hotelService.getHotelList(queryParams);
        return Result.success(result);
    }


    @Operation(summary = "问题解答列表")
    @GetMapping("/getQuestionList")
    @Log(value = "问题解答列表", module = LogModuleEnum.QUESTION)
    public Result<List<QuestionPageVO>> getQuestionList(QuestionQuery queryParams) {
        List<QuestionPageVO> result = questionService.getQuestionList(queryParams);
        return Result.success(result);
    }


    @Operation(summary = "会前安排列表")
    @GetMapping("/getArrangeList")
    @Log(value = "会前安排列表", module = LogModuleEnum.ARRANGE)
    public Result<List<ArrangePageVO>> getArrangeList(ArrangeQuery queryParams) {
        List<ArrangePageVO> result = arrangeService.getArrangeList(queryParams);
        return Result.success(result);
    }



    @Operation(summary = "精彩瞬间列表")
    @GetMapping("/getMomentsList")
    @Log(value = "精彩瞬间列表", module = LogModuleEnum.MOMENTS)
    public Result<List<MomentsPageVO>> getMomentsList(MomentsQuery queryParams) {
        List<MomentsPageVO> result = momentsService.getMomentsList(queryParams);
        return Result.success(result);
    }

    @Operation(summary = "演讲列表")
    @GetMapping("/getSpeechList")
    public Result<List<SpeechV1>> getSpeechList(
            @Parameter(description = "国家类型") @RequestParam(required = false) String countryType) {
        LambdaQueryWrapper<SpeechV1> queryWrapper = new LambdaQueryWrapper<>();
        if (countryType != null && !countryType.isEmpty()) {
            queryWrapper.eq(SpeechV1::getCountryType, countryType);
        }
        queryWrapper.orderByAsc(SpeechV1::getSort);
        List<SpeechV1> result = speechServiceV1.list(queryWrapper);
        return Result.success(result);
    }

    @Operation(summary = "获取讲演集锦ID查询信息")
    @GetMapping("/{id}/form")
    public Result<Speech> getSpeechByID(@Parameter(description = "ID") @PathVariable Long id) {
        Speech speech = speechService.getSpeechByID(id);
        return Result.success(speech);
    }


    @Operation(summary = "会议设置列表")
    @GetMapping("/getMeetingSettingList")
    public Result<List<MeetingSetting>> getMeetingSettingList(
            @Parameter(description = "国家类型") @RequestParam(required = false) String countryType) {
        LambdaQueryWrapper<MeetingSetting> queryWrapper = new LambdaQueryWrapper<>();
        if (countryType != null && !countryType.isEmpty()) {
            queryWrapper.eq(MeetingSetting::getCountryType, countryType);
        }
        queryWrapper.orderByAsc(MeetingSetting::getSort);
        List<MeetingSetting> result = courseService.list(queryWrapper);
        return Result.success(result);
    }

}
