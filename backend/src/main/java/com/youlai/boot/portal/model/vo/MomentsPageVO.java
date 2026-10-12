package com.youlai.boot.portal.model.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

@Schema(description ="精彩瞬间分页对象")
@Data
public class MomentsPageVO {

    @Schema(description="ID")
    private Long id;

    @Schema(description="姓名")
    private String name;

    @Schema(description="类型")
    private String type;

    @Schema(description="职称")
    private String position;

    @Schema(description="国家类型")
    private String countryType;

    @Schema(description="图片")
    private String momentsImage;

    @Schema(description="精彩瞬间状态")
    private Integer status;

    @Schema(description="排序")
    private Integer sort;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createTime;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime updateTime;
}
