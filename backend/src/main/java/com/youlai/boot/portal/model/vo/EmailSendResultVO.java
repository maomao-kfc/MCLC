package com.youlai.boot.portal.model.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Schema(description = "邮件发送结果")
@Data
public class EmailSendResultVO {

    @Schema(description = "专家姓名")
    private String expertName;

    @Schema(description = "专家邮箱")
    private String expertEmail;

    @Schema(description = "发送是否成功")
    private boolean success;

}
