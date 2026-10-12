package com.youlai.boot.portal.model.query;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.youlai.boot.common.base.BaseQuery;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

/**
 * 联系人查询对象
 *
 * @author 243269112
 * @since 2024/4/23
 */
@Schema(description = "联系人查询对象")
@Getter
@Setter
public class ContactQuery extends BaseQuery {

    @Schema(description="标题")
    private String title;

    @Schema(description="国家类型")
    private String countryType;

    @Schema(description="联系人姓名")
    private String contactName;

    @Schema(description="联系人电话")
    private String contactPhone;

    @Schema(description="联系人邮箱")
    private String contactMail;

    @Schema(description="开始日期")
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDateTime startDate;

    @Schema(description="结束日期")
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDateTime endDate;
}
