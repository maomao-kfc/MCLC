package com.youlai.boot.portal.model.form;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import org.hibernate.validator.constraints.Range;

@Schema(description = "问题解答表单对象")
@Data
public class QuestionForm {

    @Schema(description="问题解答ID")
    private Long id;

    @Schema(description="标题")
    @NotBlank(message = "标题名称不能为空")
    private String title;

    @Schema(description="问题、答案信息")
    @NotBlank(message = "问题、答案信息不能为空")
    private String questionDetail;

    @Schema(description="备注")
    private String remark;

    @Schema(description="国家类型")
    private String countryType;

    @Schema(description="排序")
    private Integer sort;

    @Schema(description="状态(1-正常；0-停用)")
    @Range(max = 1, min = 0, message = "状态不正确")
    private Integer status;



}
