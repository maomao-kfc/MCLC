package com.youlai.boot.portal.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.youlai.boot.core.web.PageResult;
import com.youlai.boot.core.web.Result;
import com.youlai.boot.portal.model.entity.ReconciliationBatch;
import com.youlai.boot.portal.model.entity.ReconciliationRecord;
import com.youlai.boot.portal.service.PaymentReconciliationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.Map;

/**
 * 支付对账控制器（管理端）
 * <p>导入协会对账Excel → 自动多维度匹配 → 人工确认/驳回</p>
 */
@Tag(name = "支付对账接口")
@RestController
@RequestMapping("/api/v1/reconciliation")
@RequiredArgsConstructor
public class PaymentReconciliationController {

    private final PaymentReconciliationService reconciliationService;

    @Operation(summary = "导入对账Excel并执行自动匹配")
    @PostMapping("/import")
    @PreAuthorize("@ss.hasPerm('sys:reconciliation:import')")
    public Result<Map<String, Object>> importReconciliation(
            @RequestPart(value = "file") MultipartFile file
    ) {
        Map<String, Object> result = reconciliationService.importReconciliation(file);
        return Result.success(result);
    }

    @Operation(summary = "对账批次分页列表")
    @GetMapping("/batch/page")
    @PreAuthorize("@ss.hasPerm('sys:reconciliation:list')")
    public PageResult<ReconciliationBatch> getBatchPage(
            @Parameter(description = "页码") @RequestParam(defaultValue = "1") Integer pageNum,
            @Parameter(description = "每页条数") @RequestParam(defaultValue = "10") Integer pageSize
    ) {
        Page<ReconciliationBatch> page = reconciliationService.getBatchPage(pageNum, pageSize);
        return PageResult.success(page);
    }

    @Operation(summary = "对账明细分页列表")
    @GetMapping("/record/page")
    @PreAuthorize("@ss.hasPerm('sys:reconciliation:list')")
    public PageResult<ReconciliationRecord> getRecordPage(
            @Parameter(description = "页码") @RequestParam(defaultValue = "1") Integer pageNum,
            @Parameter(description = "每页条数") @RequestParam(defaultValue = "10") Integer pageSize,
            @Parameter(description = "批次ID") @RequestParam(required = false) Long batchId,
            @Parameter(description = "匹配状态(auto_matched/unmatched/confirmed/rejected)") @RequestParam(required = false) String matchStatus
    ) {
        Page<ReconciliationRecord> page = reconciliationService.getRecordPage(pageNum, pageSize, batchId, matchStatus);
        return PageResult.success(page);
    }

    @Operation(summary = "人工确认匹配")
    @PostMapping("/record/{recordId}/confirm")
    @PreAuthorize("@ss.hasPerm('sys:reconciliation:confirm')")
    public Result<Void> confirmMatch(
            @Parameter(description = "对账记录ID") @PathVariable Long recordId,
            @Parameter(description = "注册用户ID") @RequestParam Long registrationId
    ) {
        reconciliationService.confirmMatch(recordId, registrationId);
        return Result.success();
    }

    @Operation(summary = "驳回对账记录")
    @PostMapping("/record/{recordId}/reject")
    @PreAuthorize("@ss.hasPerm('sys:reconciliation:confirm')")
    public Result<Void> rejectMatch(
            @Parameter(description = "对账记录ID") @PathVariable Long recordId
    ) {
        reconciliationService.rejectMatch(recordId);
        return Result.success();
    }
}