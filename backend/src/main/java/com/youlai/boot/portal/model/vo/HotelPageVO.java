package com.youlai.boot.portal.model.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

@Schema(description ="酒店信息分页对象")
@Data
public class HotelPageVO {

    @Schema(description="酒店信息ID")
    private Long id;

    @Schema(description="酒店名称")
    private String hotelName;

    @Schema(description="酒店信息状态")
    private Integer status;

    @Schema(description="排序")
    private Integer sort;

    @Schema(description="国家类型")
    private String countryType;

    @Schema(description="酒店图片")
    private String hotelImage;

    @Schema(description="酒店电话")
    private String hotelPhone;

    @Schema(description="酒店联系人")
    private String hotelContact;

    @Schema(description="酒店地址")
    private String hotelAddress;

    @Schema(description="酒店描述")
    private String hotelDesc;

    @Schema(description="酒店url")
    private String hotelUrl;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createTime;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime updateTime;
}
