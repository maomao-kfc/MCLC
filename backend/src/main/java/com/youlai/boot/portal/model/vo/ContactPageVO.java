package com.youlai.boot.portal.model.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

@Schema(description ="联系人分页对象")
@Data
public class ContactPageVO {

    @Schema(description="ID")
    private Long id;

    @Schema(description="标题")
    private String title;

    @Schema(description="状态")
    private Integer status;

    @Schema(description="排序")
    private Integer sort;

    @Schema(description="国家类型")
    private String countryType;

    @Schema(description="联系人姓名")
    private String contactName;

    @Schema(description="联系人电话")
    private String contactPhone;

    @Schema(description="联系人邮箱")
    private String contactMail;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createTime;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime updateTime;
}
