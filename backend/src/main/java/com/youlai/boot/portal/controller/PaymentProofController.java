package com.youlai.boot.portal.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.youlai.boot.core.web.PageResult;
import com.youlai.boot.core.web.Result;
import com.youlai.boot.portal.model.form.PaymentProofSubmitForm;
import com.youlai.boot.portal.model.form.ProofReviewForm;
import com.youlai.boot.portal.model.vo.PaymentProofPageVO;
import com.youlai.boot.portal.service.PaymentProofService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.Map;

/**
 * 支付凭证控制器
 * <p>用户端：提交凭证、查询凭证状态</p>
 * <p>管理端：凭证审核列表、审核通过/驳回、线下支付登记</p>
 */
@Tag(name = "支付凭证接口")
@RestController
@RequestMapping("/api/v1/paymentProof")
@RequiredArgsConstructor
public class PaymentProofController {

    private final PaymentProofService paymentProofService;

    // ==================== 用户端接口 ====================

    @Operation(summary = "提交支付凭证（用户端）")
    @PostMapping("/submit")
    public Result<Map<String, Object>> submitProof(
            @RequestPart(value = "file") MultipartFile file,
            @Valid @RequestPart(value = "form") PaymentProofSubmitForm form
    ) {
        Map<String, Object> result = paymentProofService.submitProof(file, form);
        return Result.success(result);
    }

    @Operation(summary = "查询凭证状态（用户端）")
    @GetMapping("/status/{registrationId}")
    public Result<Map<String, Object>> getProofStatus(
            @Parameter(description = "注册用户ID") @PathVariable Long registrationId
    ) {
        Map<String, Object> result = paymentProofService.getProofStatus(registrationId);
        return Result.success(result);
    }

    // ==================== 管理端接口 ====================

    @Operation(summary = "凭证审核分页列表（管理端）")
    @GetMapping("/page")
    @PreAuthorize("@ss.hasPerm('sys:paymentProof:list')")
    public PageResult<PaymentProofPageVO> getProofPage(
            @Parameter(description = "页码") @RequestParam(defaultValue = "1") Integer pageNum,
            @Parameter(description = "每页条数") @RequestParam(defaultValue = "10") Integer pageSize,
            @Parameter(description = "凭证状态") @RequestParam(required = false) String status,
            @Parameter(description = "姓名/邮箱搜索") @RequestParam(required = false) String name
    ) {
        Page<PaymentProofPageVO> page = paymentProofService.getProofPage(pageNum, pageSize, status, name);
        return PageResult.success(page);
    }

    @Operation(summary = "审核凭证：通过/驳回（管理端）")
    @PostMapping("/review")
    @PreAuthorize("@ss.hasPerm('sys:paymentProof:review')")
    public Result<Void> reviewProof(@Valid @RequestBody ProofReviewForm form) {
        paymentProofService.reviewProof(form);
        return Result.success();
    }

    @Operation(summary = "线下支付登记（管理端）")
    @PostMapping("/offline/{registrationId}")
    @PreAuthorize("@ss.hasPerm('sys:paymentProof:offline')")
    public Result<Void> offlineRegister(
            @Parameter(description = "注册用户ID") @PathVariable Long registrationId,
            @RequestParam(required = false) String remark
    ) {
        paymentProofService.offlineRegister(registrationId, remark);
        return Result.success();
    }
}