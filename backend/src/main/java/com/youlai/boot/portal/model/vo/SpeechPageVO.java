package com.youlai.boot.portal.model.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.time.LocalDateTime;

@Schema(description ="分页对象")
@Data
public class SpeechPageVO {

    @Schema(description="ID")
    private Long id;

    @Schema(description="姓名")
    @NotBlank(message = "姓名不能为空")
    private String name;

    @Schema(description="职务")
    @NotBlank(message = "职务不能为空")
    private String position;

    @Schema(description="标题")
    private String title;

    @Schema(description="医生座右铭")
    private String motto;

    @Schema(description="国家类型")
    private String countryType;

    @Schema(description="个人图片展示")
    private String images;

@Schema(description="封面展示")
    private String cover;

    @Schema(description="个人介绍")
    private String introduction;

    @Schema(description="报告内容")
    private String content;

    @Schema(description="显示顺序")
    private Integer sort;

    @Schema(description="状态")
    private Integer status;

    @Schema(description="独家专访视频")
    private String video;

    @Schema(description="参考文献与学习资料")
    private String reference;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createTime;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime updateTime;
}
