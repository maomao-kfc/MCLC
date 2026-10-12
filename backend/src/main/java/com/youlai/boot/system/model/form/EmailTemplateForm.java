package com.youlai.boot.system.model.form;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import org.hibernate.validator.constraints.Range;

@Schema(description = "邮箱模板表单")
@Data
public class EmailTemplateForm {

    @Schema(description = "模板ID", example = "1")
    private Long id;

    @Schema(description = "邮箱标题", example = "注册成功通知")
    @NotBlank(message = "邮箱标题不能为空")
    private String emailTitle;

    @Schema(description = "邮箱类型")
    private String emailType;

    @Schema(description = "邮箱内容（富文本编辑器JSON）")
    private String emailContent;

    @Schema(description = "排序", example = "1")
    private Integer sort;

    @Schema(description = "状态（1-启用，0-禁用）", example = "1")
    @Range(min = 0, max = 1, message = "状态不正确")
    private Integer status;

    @Schema(description = "备注")
    private String remark;

}
