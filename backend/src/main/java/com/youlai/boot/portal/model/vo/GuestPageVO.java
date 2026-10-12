package com.youlai.boot.portal.model.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

@Schema(description ="特邀嘉宾分页对象")
@Data
public class GuestPageVO {

    @Schema(description="特邀嘉宾ID")
    private Long id;

    @Schema(description="特邀嘉宾姓名")
    private String name;

    @Schema(description="特邀嘉宾类型")
    private String type;

    @Schema(description="特邀嘉宾状态")
    private Integer status;

    @Schema(description="排序")
    private Integer sort;

    @Schema(description="国家类型")
    private String countryType;

    @Schema(description="特邀嘉宾图像")
    private String image;

    @Schema(description="特邀嘉宾职务")
    private String position;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createTime;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime updateTime;
}
