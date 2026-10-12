package com.youlai.boot.portal.model.form;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import org.hibernate.validator.constraints.Range;

@Schema(description = "会前安排表单对象")
@Data
public class ArrangeForm {

    @Schema(description="ID")
    private Long id;

    @Schema(description="会议内容")
    @NotBlank(message = "会议内容不能为空")
    private String arrangeContent;

    @Schema(description="会议室")
    @NotBlank(message = "会议室不能为空")
    private String arrangeRoom;

    @Schema(description="国家类型")
    private String countryType;

    @Schema(description="安排类型(HEADER-标题描述 BLOCK-板块)")
    private String arrangeType;

    @Schema(description="排序")
    private Integer sort;

    @Schema(description="角色状态(1-正常；0-停用)")
    @Range(max = 1, min = 0, message = "角色状态不正确")
    private Integer status;

}
