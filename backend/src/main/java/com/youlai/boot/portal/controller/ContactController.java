package com.youlai.boot.portal.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.youlai.boot.common.annotation.Log;
import com.youlai.boot.common.annotation.RepeatSubmit;
import com.youlai.boot.common.enums.LogModuleEnum;
import com.youlai.boot.core.web.PageResult;
import com.youlai.boot.core.web.Result;
import com.youlai.boot.portal.model.form.ContactForm;
import com.youlai.boot.portal.model.query.ContactQuery;
import com.youlai.boot.portal.model.vo.ContactPageVO;
import com.youlai.boot.portal.service.ContactService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;


/**
 *联系人宾控制层
 *
 * @author 243269112
 * @since 2026/4/23
 */
@Tag(name = "联系人接口")
@RestController
@RequestMapping("/api/v1/contact")
@RequiredArgsConstructor
public class ContactController {

    private final ContactService ContactService;

    @Operation(summary = "联系人分页列表")
    @GetMapping
    @Log(value = "联系人分页列表", module = LogModuleEnum.CONTACT)
    public PageResult<ContactPageVO> getContactPage(ContactQuery queryParams) {
        Page<ContactPageVO> result = ContactService.getContactPage(queryParams);
        return PageResult.success(result);
    }

    @Operation(summary = "新增联系人")
    @PostMapping
    @PreAuthorize("@ss.hasPerm('sys:contact:create')")
    @RepeatSubmit
    public Result<?> addContact(@Valid @RequestBody ContactForm ContactForm) {
        boolean result = ContactService.saveContact(ContactForm);
        return Result.judge(result);
    }


    @Operation(summary = "修改联系人")
    @PutMapping(value = "/{id}")
    @PreAuthorize("@ss.hasPerm('sys:contact:update')")
    public Result<?> updateContact(@Valid @RequestBody ContactForm ContactForm) {
        boolean result = ContactService.saveContact(ContactForm);
        return Result.judge(result);
    }

    @Operation(summary = "删除联系人")
    @DeleteMapping("/{ids}")
    @PreAuthorize("@ss.hasPerm('sys:contact:delete')")
    public Result<Void> deleteContacts(
            @Parameter(description = "删除联系人，多个以英文逗号(,)拼接") @PathVariable String ids
    ) {
        ContactService.deleteContacts(ids);
        return Result.success();
    }

    @Operation(summary = "修改联系人状态")
    @PutMapping(value = "/{contactId}/status")
    @PreAuthorize("@ss.hasPerm('sys:contact:update')")
    public Result<?> updateContactStatus(
            @Parameter(description = "联系人ID") @PathVariable Long contactId,
            @Parameter(description = "状态(1:启用;0:禁用)") @RequestParam Integer status
    ) {
        boolean result = ContactService.updateContactStatus(contactId, status);
        return Result.judge(result);
    }

}
