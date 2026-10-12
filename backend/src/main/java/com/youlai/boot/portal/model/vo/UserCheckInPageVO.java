package com.youlai.boot.portal.model.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Schema(description = "签到记录分页对象")
@Data
public class UserCheckInPageVO {

    @Schema(description = "签到记录ID")
    private Long id;

    @Schema(description = "关联签到ID")
    private Long checkId;

    @Schema(description = "签到时间")
    private String checkTime;

    @Schema(description = "签到人姓名")
    private String checkName;

    @Schema(description = "签到人编号")
    private String checkNumber;

    @Schema(description = "签到人邮箱")
    private String checkEmail;

    @Schema(description = "签到状态")
    private String checkStatus;
}
