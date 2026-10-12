package com.youlai.boot.portal.model.query;

import com.youlai.boot.common.base.BaseQuery;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

/**
 * 组织架构查询对象
 *
 * @author 243269112
 * @since 2026/5/25
 */
@Schema(description = "组织架构查询对象")
@Getter
@Setter
public class ArchitectureQuery extends BaseQuery {

    @Schema(description="姓名")
    private String name;

    @Schema(description="职位")
    private String job;

    @Schema(description="国家类型")
    private String countryType;

}
