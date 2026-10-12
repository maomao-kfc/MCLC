package com.youlai.boot.portal.model.query;

import com.youlai.boot.common.base.BaseQuery;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

@Schema(description = "签到记录查询对象")
@Getter
@Setter
public class UserCheckInQuery extends BaseQuery {

    @Schema(description = "签到人姓名")
    private String checkName;

    @Schema(description = "签到人邮箱")
    private String checkEmail;

    @Schema(description = "签到人编号")
    private String checkNumber;

    @Schema(description = "签到状态")
    private String checkStatus;
}
