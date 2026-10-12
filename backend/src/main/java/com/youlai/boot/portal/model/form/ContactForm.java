package com.youlai.boot.portal.model.form;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import org.hibernate.validator.constraints.Range;

@Schema(description = "联系人表单对象")
@Data
public class ContactForm {

    @Schema(description="ID")
    private Long id;

    @Schema(description="标题")
    @NotBlank(message = "标题不能为空")
    private String title;

    @Schema(description="联系人姓名")
    @NotBlank(message = "联系人姓名不能为空")
    private String contactName;

    @Schema(description="联系人电话")
    private String contactPhone;

    @Schema(description="联系人邮箱")
    @NotBlank(message = "联系人邮箱不能为空")
    private String contactMail;

    @Schema(description="国家类型")
    private String countryType;

    @Schema(description="排序")
    private Integer sort;

    @Schema(description="角色状态(1-正常；0-停用)")
    @Range(max = 1, min = 0, message = "角色状态不正确")
    private Integer status;

}
