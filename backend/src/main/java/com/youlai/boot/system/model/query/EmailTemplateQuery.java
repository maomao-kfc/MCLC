package com.youlai.boot.system.model.query;

import com.youlai.boot.common.base.BaseQuery;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = false)
@Schema(description = "邮箱模板查询对象")
public class EmailTemplateQuery extends BaseQuery {

    @Schema(description = "关键字（邮箱标题）")
    private String keywords;

    @Schema(description = "状态（1-启用，0-禁用）")
    private Integer status;

}
