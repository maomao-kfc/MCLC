package com.youlai.boot.portal.model.form;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import org.hibernate.validator.constraints.Range;

@Schema(description = "精彩瞬间表单对象")
@Data
public class MomentsForm {

    @Schema(description="ID")
    private Long id;

    @Schema(description="姓名")
    @NotBlank(message = "姓名不能为空")
    private String name;

    @Schema(description="国家类型")
    private String countryType;

    @Schema(description="图片")
    @NotBlank(message = "图片不能为空")
    private String momentsImage;

    @Schema(description="类型")
    @NotBlank(message = "类型不能为空")
    private String type;;

    @Schema(description="排序")
    private Integer sort;

    @Schema(description="角色状态(1-正常；0-停用)")
    @Range(max = 1, min = 0, message = "角色状态不正确")
    private Integer status;

}
