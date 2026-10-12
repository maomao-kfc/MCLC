package com.youlai.boot.system.model.query;

import com.youlai.boot.common.base.BaseQuery;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 注册类型查询对象
 *
 * @author youlaitech
 * @since 2024-08-27 10:31
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Schema(description = "注册类型查询对象")
public class RegistrationQuery extends BaseQuery {
    @Schema(description = "字段名称")
    private String fieldName;

    @Schema(description = "常规有效期")
    private String normalValidDate;

    @Schema(description = "截止日期")
    private String deadlineDate;

    @Schema(description = "常规有效期金额")
    private String normalAmount;

    @Schema(description = "截止日期金额")
    private String deadlineAmount;

    @Schema(description = "国家类型")
    private String countryType;
}
