package com.youlai.boot.system.model.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Schema(description = "邮箱模板分页对象")
@Getter
@Setter
public class EmailTemplatePageVO {

    @Schema(description = "模板ID")
    private Long id;

    @Schema(description = "邮箱标题")
    private String emailTitle;

    @Schema(description = "邮箱类型")
    private String emailType;

    @Schema(description = "邮箱内容（富文本编辑器JSON）")
    private String emailContent;

    @Schema(description = "排序")
    private Integer sort;

    @Schema(description = "状态（1-启用，0-禁用）")
    private Integer status;

    @Schema(description = "备注")
    private String remark;

    @Schema(description = "创建时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createTime;

    @Schema(description = "更新时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime updateTime;

}
