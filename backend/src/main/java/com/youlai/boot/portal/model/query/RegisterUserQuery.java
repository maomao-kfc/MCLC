package com.youlai.boot.portal.model.query;

import com.youlai.boot.common.base.BaseQuery;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

@Schema(description = "注册用户查询对象")
@Getter
@Setter
public class RegisterUserQuery extends BaseQuery {

    @Schema(description = "名")
    private String name;

    @Schema(description = "姓")
    private String surname;

    @Schema(description = "电子邮件")
    private String email;

    @Schema(description = "组织")
    private String organization;

    @Schema(description = "手机号")
    private String mobilePhone;

    @Schema(description = "状态")
    private Integer status;

    @Schema(description = "国家")
    private String country;

    @Schema(description = "注册类型")
    private String registrationType;

    @Schema(description = "付款方式")
    private String paymentMethod;

    @Schema(description = "支付状态")
    private String paymentStatus;

    @Schema(description = "签证名")
    private String visaName;

    @Schema(description = "签证护照")
    private String visaPassport;

    @Schema(description = "是否需要签证(0不需要、1需要)")
    private Integer visaIsNeed;

    @Schema(description = "注册类别")
    private String registrationCategory;

    @Schema(description = "团队ID(查询团队成员)")
    private Long teamId;

    @Schema(description = "抵扣码")
    private String deductionCode;
}
