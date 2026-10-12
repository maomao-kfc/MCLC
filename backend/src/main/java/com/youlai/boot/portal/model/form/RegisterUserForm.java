package com.youlai.boot.portal.model.form;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.util.List;

@Schema(description = "注册用户表单对象")
@Data
public class RegisterUserForm {

    @Schema(description = "注册用户信息ID")
    private Long id;

    @Schema(description = "关联问卷ID")
    private Long connectId;

    @Schema(description = "团队ID(团队注册时使用)")
    private Long teamId;

    @Schema(description = "名")
    @NotBlank(message = "名不能为空")
    private String name;

    @Schema(description = "姓")
    @NotBlank(message = "姓不能为空")
    private String surname;

    @Schema(description = "电子邮件")
    @Email(message = "电子邮件格式不正确")
    private String email;

    @Schema(description = "组织")
    private String organization;

    @Schema(description = "手机号")
    private String mobilePhone;

    @Schema(description = "街道")
    private String street;

    @Schema(description = "邮政编号")
    private String postalCode;

    @Schema(description = "状态")
    private Integer status;

    @Schema(description = "国家")
    private String country;

    @Schema(description = "账单组织")
    private String billOrganization;

    @Schema(description = "账单增值税收")
    private String billVat;

    @Schema(description = "账单采购订单号")
    private String billProcurement;

    @Schema(description = "账单街道")
    private String billStreet;

    @Schema(description = "账单邮政编号")
    private String billPostalCode;

    @Schema(description = "账单城市")
    private String billCity;

    @Schema(description = "账单状态")
    private Integer billStatus;

    @Schema(description = "账单国家")
    private String billCountry;

    @Schema(description = "注册类型")
    private String registrationType;

    @Schema(description = "注册费")
    private String registrationFee;

    @Schema(description = "注册日期")
    private String registrationDate;

    @Schema(description = "付款方式")
    private String paymentMethod;

    @Schema(description = "支付金额")
    private String paymentAmount;

    @Schema(description = "签证名")
    private String visaName;

    @Schema(description = "签证姓")
    private String visaSurname;

    @Schema(description = "签证护照")
    private String visaPassport;

    @Schema(description = "签证签发日")
    private String visaIssueTime;

    @Schema(description = "签证结束日")
    private String visaEndTime;

    @Schema(description = "签证出生日期")
    private String visaTime;

    @Schema(description = "签证出生地")
    private String visaArea;

    @Schema(description = "是否需要签证(0不需要、1需要)")
    private Integer visaIsNeed;

    @Schema(description = "注册类别")
    private String registrationCategory;

    @Schema(description = "注册文件")
    private String registrationDocument;

    @Schema(description = "注册成员")
    private String registrationWhichMember;

    @Schema(description = "注册码")
    private String registrationCode;

    @Schema(description = "抵扣码")
    private String deductionCode;

    @Schema(description = "是否拥有配药权")
    private Integer isMatchDrug;

    @Schema(description = "饮食要求")
    private String dietaryRequirements;

    @Schema(description = "特殊要求")
    private String specialRequirements;

    @Schema(description = "是否第一次参加")
    private Integer isFirst;

    @Schema(description = "性别")
    private String gender;

    @Schema(description = "职业")
    private String occupation;

    @Schema(description = "经验")
    private String experience;

    @Schema(description = "兴趣领域")
    private String fieldOfInterest;

    @Schema(description = "其他")
    private String other;

    @Schema(description = "银行上传凭证")
    private String picBank;

    @Schema(description = "实际付款金额")
    private String actualPayment;

    @Schema(description = "注册类型文字label")
    private String registerLabel;

    @Schema(description = "团队成员列表(团队注册时填写)")
    private List<TeamMemberForm> teamMembers;

    @Data
    @Schema(description = "团队成员表单")
    public static class TeamMemberForm {

        @Schema(description = "名")
        @NotBlank(message = "成员名不能为空")
        private String name;

        @Schema(description = "姓")
        @NotBlank(message = "成员姓不能为空")
        private String surname;

        @Schema(description = "电子邮件")
        @Email(message = "电子邮件格式不正确")
        private String email;

        @Schema(description = "手机号")
        private String mobilePhone;
    }
}
