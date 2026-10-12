package com.youlai.boot.system.model.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@Schema(description = "注册类型视图对象")
public class RegistrationTypeVO {
    /**
     * 注册类型ID
     */
    @Schema(description = "注册类型ID")
    private Long id;

    @Schema(description = "字段名称")
    private String fieldName;

    @Schema(description = "常规有效期")
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDateTime normalValidDate;

    @Schema(description = "截止日期")
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDateTime deadlineDate;

    @Schema(description = "常规有效期金额")
    private String normalAmount;

    @Schema(description = "截止日期金额")
    private String deadlineAmount;

    @Schema(description = "排序")
    private Integer sort;

    @Schema(description = "国家类型")
    private String countryType;
}
