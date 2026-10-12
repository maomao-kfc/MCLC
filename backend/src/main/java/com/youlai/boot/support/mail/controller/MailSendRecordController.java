package com.youlai.boot.support.mail.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.youlai.boot.core.web.PageResult;
import com.youlai.boot.core.web.Result;
import com.youlai.boot.support.mail.model.entity.MailSendRecord;
import com.youlai.boot.support.mail.model.query.MailSendRecordQuery;
import com.youlai.boot.support.mail.model.vo.MailSendRecordDetailVO;
import com.youlai.boot.support.mail.model.vo.MailSendRecordPageVO;
import com.youlai.boot.support.mail.service.MailSendRecordService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

/**
 * 邮件发送记录管理接口（邮件中心）
 *
 * 提供邮件发送记录的查询、重试、取消等功能。
 * 前端可在"邮件中心"页面查看所有邮件发送历史、失败原因、重试等。
 *
 * @author system
 * @since 2026/7/24
 */
@Tag(name = "邮件发送记录")
@RestController
@RequestMapping("/api/v1/mailRecord")
@RequiredArgsConstructor
public class MailSendRecordController {

    private final MailSendRecordService mailSendRecordService;

    /**
     * 分页查询邮件发送记录
     *
     * 支持按业务类型、业务ID、收件邮箱、发送状态、邮件标题、时间范围筛选
     */
    @Operation(summary = "分页查询邮件发送记录")
    @GetMapping("/page")
    public PageResult<MailSendRecordPageVO> getMailSendRecordPage(MailSendRecordQuery query) {
        Page<MailSendRecordPageVO> page = mailSendRecordService.getMailSendRecordPage(query);
        return PageResult.success(page);
    }

    /**
     * 查看邮件发送记录详情
     */
    @Operation(summary = "查看邮件发送记录详情")
    @GetMapping("/{id}")
    public Result<MailSendRecordDetailVO> getMailSendRecordDetail(@PathVariable Long id) {
        MailSendRecord record = mailSendRecordService.getById(id);
        return Result.success(MailSendRecordDetailVO.fromEntity(record));
    }

    /**
     * 重试发送失败的邮件
     *
     * 仅允许重试发送失败的记录，且重试次数不超过5次
     */
    @Operation(summary = "重试发送失败的邮件")
    @PostMapping("/retry/{id}")
    public Result<MailSendRecord> retry(@PathVariable Long id) {
        MailSendRecord record = mailSendRecordService.retry(id);
        return Result.success(record);
    }

    /**
     * 取消待发送的邮件
     *
     * 仅允许取消状态为"待发送"的记录
     */
    @Operation(summary = "取消待发送的邮件")
    @PostMapping("/cancel/{id}")
    public Result<Void> cancel(@PathVariable Long id) {
        mailSendRecordService.cancel(id);
        return Result.success();
    }

    /**
     * 根据业务类型和业务ID查询邮件记录
     *
     * 例如：查询投稿ID=321的所有邮件记录
     */
    @Operation(summary = "按业务查询邮件记录")
    @GetMapping("/business")
    public Result<java.util.List<MailSendRecord>> listByBusiness(
            @RequestParam String businessType,
            @RequestParam Long businessId) {
        java.util.List<MailSendRecord> list = mailSendRecordService.listByBusiness(
                com.youlai.boot.support.mail.enums.MailBusinessType.fromValue(businessType), businessId);
        return Result.success(list);
    }

    /**
     * 手动标记邮件为退信-投递失败
     *
     * 管理员在邮件中心手动标记退信，用于IMAP自动检测遗漏的退信场景。
     * 仅允许标记状态为"已提交至邮件服务器"的记录。
     */
    @Operation(summary = "手动标记退信")
    @PostMapping("/markBounce/{id}")
    public Result<MailSendRecord> markBounce(@PathVariable Long id, @RequestParam String bounceReason) {
        MailSendRecord record = mailSendRecordService.markBounce(id, bounceReason);
        return Result.success(record);
    }
}