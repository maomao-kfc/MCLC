package com.youlai.boot.portal.model.form;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import org.hibernate.validator.constraints.Range;

@Schema(description = "酒店信息表单对象")
@Data
public class HotelForm {

    @Schema(description="酒店信息ID")
    private Long id;

    @Schema(description="酒店名称")
    @NotBlank(message = "酒店名称不能为空")
    private String hotelName;

    @Schema(description="国家类型")
    private String countryType;

    @Schema(description="酒店图片")
    @NotBlank(message = "酒店图片不能为空")
    private String hotelImage;

    @Schema(description="酒店电话")
    @NotBlank(message = "酒店电话不能为空")
    private String hotelPhone;

    @Schema(description="酒店联系人")
    @NotBlank(message = "酒店联系人不能为空")
    private String hotelContact;

    @Schema(description="酒店地址")
    @NotBlank(message = "酒店地址不能为空")
    private String hotelAddress;

    @Schema(description="订购链接")
    private String hotelUrl;

    @Schema(description="排序")
    private Integer sort;

    @Schema(description="角色状态(1-正常；0-停用)")
    @Range(max = 1, min = 0, message = "角色状态不正确")
    private Integer status;

}
