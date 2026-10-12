package com.youlai.boot.portal.model.form;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import org.hibernate.validator.constraints.Range;

@Schema(description = "酒店信息表单对象")
@Data
public class SpeechForm {

    @Schema(description="ID")
    private Long id;

    @Schema(description="姓名")
    @NotBlank(message = "姓名不能为空")
    private String name;

    @Schema(description="职务")
    private String position;

    @Schema(description="标题")
    private String title;

@Schema(description="医生座右铭")
    private String motto;

    @Schema(description="国家类型")
    private String countryType;

    @Schema(description="封面展示")
    private String cover;

    @Schema(description="个人图片展示")
    private String images;

    @Schema(description="个人介绍")
    private String introduction;

    @Schema(description="报告内容")
    private String content;

    @Schema(description="独家专访视频")
    private String video;

    @Schema(description="参考文献与学习资料")
    private String reference;

    @Schema(description="排序")
    private Integer sort;

    @Schema(description="角色状态(1-正常；0-停用)")
    @Range(max = 1, min = 0, message = "角色状态不正确")
    private Integer status;

}
