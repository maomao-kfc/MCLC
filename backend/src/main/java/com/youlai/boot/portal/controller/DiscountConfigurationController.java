package com.youlai.boot.portal.controller;

import com.youlai.boot.core.web.Result;
import com.youlai.boot.portal.model.entity.DiscountConfiguration;
import com.youlai.boot.portal.model.form.DiscountConfigurationForm;
import com.youlai.boot.portal.service.DiscountConfigurationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 折扣配置控制层
 *
 * @author 243269112
 * @since 2026/5/13
 */
@Tag(name = "折扣配置接口")
@RestController
@RequestMapping("/api/v1/discountConfiguration")
@RequiredArgsConstructor
public class DiscountConfigurationController {

    private final DiscountConfigurationService discountConfigurationService;

    @Operation(summary = "查询所有折扣配置")
    @GetMapping
    public Result<List<DiscountConfiguration>> getAll() {
        List<DiscountConfiguration> list = discountConfigurationService.getAll();
        return Result.success(list);
    }

    @Operation(summary = "保存折扣配置(替换全量)")
    @PostMapping
    public Result<?> saveAll(@Valid @RequestBody DiscountConfigurationForm form) {
        boolean result = discountConfigurationService.saveAll(form);
        return Result.judge(result);
    }
}
