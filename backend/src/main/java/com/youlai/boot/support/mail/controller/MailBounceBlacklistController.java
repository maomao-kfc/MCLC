package com.youlai.boot.support.mail.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.youlai.boot.core.web.PageResult;
import com.youlai.boot.core.web.Result;
import com.youlai.boot.support.mail.model.entity.MailBounceBlacklist;
import com.youlai.boot.support.mail.service.MailBounceBlacklistService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

/**
 * 邮件退信黑名单管理接口
 *
 * 提供黑名单的查询、手动添加、移除等功能。
 * 管理员可在邮件中心查看和管理无效邮箱黑名单。
 *
 * @author system
 * @since 2026/8/3
 */
@Tag(name = "邮件退信黑名单")
@RestController
@RequestMapping("/api/v1/mailBlacklist")
@RequiredArgsConstructor
public class MailBounceBlacklistController {

    private final MailBounceBlacklistService mailBounceBlacklistService;

    /**
     * 分页查询黑名单列表
     */
    @Operation(summary = "分页查询黑名单列表")
    @GetMapping("/list")
    public PageResult<MailBounceBlacklist> list(
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize) {
        Page<MailBounceBlacklist> page = mailBounceBlacklistService.listPage(pageNum, pageSize);
        return PageResult.success(page);
    }

    /**
     * 检查邮箱是否在黑名单中
     */
    @Operation(summary = "检查邮箱是否在黑名单中")
    @GetMapping("/check")
    public Result<Boolean> check(@RequestParam String email) {
        boolean blacklisted = mailBounceBlacklistService.isBlacklisted(email);
        return Result.success(blacklisted);
    }

    /**
     * 手动添加黑名单
     */
    @Operation(summary = "手动添加黑名单")
    @PostMapping("/add")
    public Result<Void> add(@RequestParam String email, @RequestParam(required = false) String reason) {
        mailBounceBlacklistService.manualAdd(email, reason);
        return Result.success();
    }

    /**
     * 移除黑名单
     */
    @Operation(summary = "移除黑名单")
    @PostMapping("/remove/{id}")
    public Result<Void> remove(@PathVariable Long id) {
        mailBounceBlacklistService.remove(id);
        return Result.success();
    }
}