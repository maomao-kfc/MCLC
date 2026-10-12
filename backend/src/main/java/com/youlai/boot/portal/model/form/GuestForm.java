package com.youlai.boot.portal.model.form;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import org.hibernate.validator.constraints.Range;

@Schema(description = "特邀嘉宾表单对象")
@Data
public class GuestForm {

    @Schema(description="特邀嘉宾ID")
    private Long id;

    @Schema(description="特邀嘉宾姓名")
    @NotBlank(message = "特邀嘉宾不能为空")
    private String name;

    @Schema(description="国家类型")
    private String countryType;

    @Schema(description="特邀嘉宾图像")
    @NotBlank(message = "特邀嘉宾图像不能为空")
    private String image;

    @Schema(description="特邀嘉宾职务")
    @NotBlank(message = "特邀嘉宾职务不能为空")
    private String position;

    @Schema(description="特邀嘉宾类型")
    @NotBlank(message = "特邀嘉宾类型不能为空")
    private String type;

    @Schema(description="排序")
    private Integer sort;

    @Schema(description="角色状态(1-正常；0-停用)")
    @Range(max = 1, min = 0, message = "角色状态不正确")
    private Integer status;

}
