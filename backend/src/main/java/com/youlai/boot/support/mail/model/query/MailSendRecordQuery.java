package com.youlai.boot.support.mail.model.query;

import com.youlai.boot.common.base.BaseQuery;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 邮件发送记录查询条件
 *
 * @author system
 * @since 2026/7/24
 */
@Schema(description = "邮件发送记录查询条件")
@Data
@EqualsAndHashCode(callSuper = true)
public class MailSendRecordQuery extends BaseQuery {

    @Schema(description = "业务类型")
    private String businessType;

    @Schema(description = "业务ID")
    private Long businessId;

    @Schema(description = "收件邮箱（模糊搜索）")
    private String receiver;

    @Schema(description = "发送状态：0-待发送, 1-成功, 2-失败, 3-重试中, 4-已取消")
    private Integer sendStatus;

    @Schema(description = "投递状态：0-等待投递, 1-已投递, 2-已退信, 3-投递中")
    private Integer deliveryStatus;

    @Schema(description = "退信类型（DNS_ERROR/USER_NOT_FOUND等）")
    private String bounceType;

    @Schema(description = "邮件标题（模糊搜索）")
    private String subject;

    @Schema(description = "开始时间")
    private String startTime;

    @Schema(description = "结束时间")
    private String endTime;
}