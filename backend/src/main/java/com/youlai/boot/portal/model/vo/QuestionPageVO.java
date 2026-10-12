package com.youlai.boot.portal.model.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

@Schema(description ="问题解答分页对象")
@Data
public class QuestionPageVO {

    @Schema(description="ID")
    private Long id;

    @Schema(description="标题")
    private String title;

    @Schema(description="问题、答案描述")
    private String questionDetail;

    @Schema(description="备注")
    private String remark;

    @Schema(description="国家类型")
    private String countryType;

    @Schema(description="状态")
    private Integer status;

    @Schema(description="排序")
    private Integer sort;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createTime;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime updateTime;
}
