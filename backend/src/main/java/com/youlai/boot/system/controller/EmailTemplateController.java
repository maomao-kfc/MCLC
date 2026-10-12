package com.youlai.boot.system.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.youlai.boot.common.annotation.Log;
import com.youlai.boot.common.annotation.RepeatSubmit;
import com.youlai.boot.common.enums.LogModuleEnum;
import com.youlai.boot.core.web.PageResult;
import com.youlai.boot.core.web.Result;
import com.youlai.boot.system.model.form.EmailTemplateForm;
import com.youlai.boot.system.model.query.EmailTemplateQuery;
import com.youlai.boot.system.model.vo.EmailTemplatePageVO;
import com.youlai.boot.system.service.EmailTemplateService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.Arrays;

@Tag(name = "07.邮箱模板接口")
@RestController
@RequestMapping("/api/v1/email-templates")
@RequiredArgsConstructor
public class EmailTemplateController {

    private final EmailTemplateService emailTemplateService;

    @Operation(summary = "邮箱模板分页列表")
    @GetMapping
    @Log(value = "邮箱模板分页列表", module = LogModuleEnum.EMAIL_TEMPLATE)
    public PageResult<EmailTemplatePageVO> getEmailTemplatePage(
            EmailTemplateQuery queryParams
    ) {
        Page<EmailTemplatePageVO> result = emailTemplateService.getEmailTemplatePage(queryParams);
        return PageResult.success(result);
    }

    @Operation(summary = "获取邮箱模板表单数据")
    @GetMapping("/form")
    public Result<EmailTemplateForm> getEmailTemplateForm(
            @Parameter(description = "模板类型") @RequestParam String emailType
    ) {
        EmailTemplateForm formData = emailTemplateService.getEmailTemplateForm(emailType);
        return Result.success(formData);
    }

    @Operation(summary = "新增邮箱模板")
    @PostMapping
    @PreAuthorize("@ss.hasPerm('sys:email-template:create')")
    @RepeatSubmit
    public Result<?> saveEmailTemplate(@Valid @RequestBody EmailTemplateForm formData) {
        boolean result = emailTemplateService.saveEmailTemplate(formData);
        return Result.judge(result);
    }

    @Operation(summary = "修改邮箱模板")
    @PutMapping("/{id}")
    @PreAuthorize("@ss.hasPerm('sys:email-template:update')")
    public Result<?> updateEmailTemplate(
            @PathVariable Long id,
            @RequestBody EmailTemplateForm formData
    ) {
        boolean result = emailTemplateService.updateEmailTemplate(id, formData);
        return Result.judge(result);
    }

    @Operation(summary = "删除邮箱模板")
    @DeleteMapping("/{ids}")
    @PreAuthorize("@ss.hasPerm('sys:email-template:delete')")
    public Result<?> deleteEmailTemplates(
            @Parameter(description = "模板ID，多个以英文逗号(,)拼接") @PathVariable String ids
    ) {
        emailTemplateService.deleteEmailTemplateByIds(Arrays.stream(ids.split(",")).toList());
        return Result.success();
    }

}
