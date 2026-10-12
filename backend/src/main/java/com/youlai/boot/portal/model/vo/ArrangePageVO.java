package com.youlai.boot.portal.model.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

@Schema(description ="酒店信息分页对象")
@Data
public class ArrangePageVO {

    @Schema(description="酒店信息ID")
    private Long id;

    @Schema(description="会议室")
    private String arrangeRoom;

    @Schema(description="酒店信息状态")
    private Integer status;

    @Schema(description="排序")
    private Integer sort;

    @Schema(description="国家类型")
    private String countryType;

    @Schema(description="安排类型(HEADER-标题描述 BLOCK-板块)")
    private String arrangeType;

    @Schema(description="会议内容")
    private String arrangeContent;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createTime;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime updateTime;
}
