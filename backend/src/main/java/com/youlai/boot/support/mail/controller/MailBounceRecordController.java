package com.youlai.boot.support.mail.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.youlai.boot.core.web.PageResult;
import com.youlai.boot.core.web.Result;
import com.youlai.boot.support.mail.model.entity.MailBounceRecord;
import com.youlai.boot.support.mail.model.query.MailBounceRecordQuery;
import com.youlai.boot.support.mail.service.MailBounceRecordService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 邮件退信记录管理接口
 *
 * 提供退信记录的分页查询、详情查看、按发送记录关联查询等功能。
 * 前端可在"邮件中心"页面查看退信详情、退信类型、匹配策略等信息。
 *
 * @author system
 * @since 2026/8/3
 */
@Tag(name = "邮件退信记录")
@RestController
@RequestMapping("/api/v1/mailBounceRecord")
@RequiredArgsConstructor
public class MailBounceRecordController {

    private final MailBounceRecordService mailBounceRecordService;

    /**
     * 分页查询退信记录
     *
     * 支持按退信类型、永久/临时、收件人、匹配策略、处理状态等条件筛选
     */
    @Operation(summary = "分页查询退信记录")
    @GetMapping("/page")
    public PageResult<MailBounceRecord> getMailBounceRecordPage(MailBounceRecordQuery query) {
        Page<MailBounceRecord> page = mailBounceRecordService.getMailBounceRecordPage(query);
        return PageResult.success(page);
    }

    /**
     * 查看退信记录详情
     */
    @Operation(summary = "查看退信记录详情")
    @GetMapping("/{id}")
    public Result<MailBounceRecord> getMailBounceRecordDetail(@PathVariable Long id) {
        MailBounceRecord record = mailBounceRecordService.getById(id);
        return Result.success(record);
    }

    /**
     * 根据发送记录ID查询关联的退信记录
     *
     * 在邮件发送记录详情页中，查看该邮件的退信信息
     */
    @Operation(summary = "按发送记录查询退信")
    @GetMapping("/bySendRecord/{matchedRecordId}")
    public Result<List<MailBounceRecord>> listByMatchedRecordId(@PathVariable Long matchedRecordId) {
        List<MailBounceRecord> list = mailBounceRecordService.findByMatchedRecordId(matchedRecordId);
        return Result.success(list);
    }

    /**
     * 查询未处理的退信记录
     *
     * 管理员可查看未自动处理成功的退信记录，进行人工处理
     */
    @Operation(summary = "查询未处理的退信记录")
    @GetMapping("/unprocessed")
    public Result<List<MailBounceRecord>> listUnprocessed(@RequestParam(defaultValue = "20") int limit) {
        List<MailBounceRecord> list = mailBounceRecordService.findUnprocessed(limit);
        return Result.success(list);
    }
}