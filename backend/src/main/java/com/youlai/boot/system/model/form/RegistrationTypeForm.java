package com.youlai.boot.system.model.form;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDate;

@Schema(description = "注册类型表单对象")
@Data
public class RegistrationTypeForm {

    @Schema(description = "注册类型ID")
    private Long id;

    @Schema(description = "字段名称")
    @NotBlank(message = "字段名称不能为空")
    private String fieldName;

    @Schema(description = "常规有效期")
    @NotNull(message = "常规有效期不能为空")
    private LocalDate normalValidDate;

    @Schema(description = "截止日期")
    @NotNull(message = "截止日期不能为空")
    private LocalDate deadlineDate;

    @Schema(description = "常规有效期金额")
    @NotBlank(message = "常规有效期金额不能为空")
    private String normalAmount;

    @Schema(description = "截止日期金额")
    @NotBlank(message = "截止日期金额不能为空")
    private String deadlineAmount;

    @Schema(description = "排序")
    @NotNull(message = "排序不能为空")
    private Integer sort;

    @Schema(description = "国家类型")
    private String countryType;
}
