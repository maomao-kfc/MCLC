package com.youlai.boot.portal.model.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

@Schema(description ="组织架构分页对象")
@Data
public class ArchitecturePageVO {

    @Schema(description="组织架构ID")
    private Long id;

    @Schema(description="图片")
    private String imageHeader;

    @Schema(description="姓名")
    private String name;

    @Schema(description="职位")
    private String job;

    @Schema(description="国家类型")
    private String countryType;

    @Schema(description="组织架构状态")
    private Integer status;

    @Schema(description="排序")
    private Integer sort;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createTime;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime updateTime;
}
