package com.youlai.boot.portal.model.form;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Schema(description = "签到表单对象")
@Data
public class CheckInForm {

    @Schema(description = "注册表ID")
    @NotNull(message = "注册表ID不能为空")
    private Long checkId;

    @Schema(description = "姓名")
    @NotBlank(message = "姓名不能为空")
    private String checkName;

    @Schema(description = "手机号")
    private String checkNumber;

    @Schema(description = "邮箱号")
    private String checkEmail;
}
