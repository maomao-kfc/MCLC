package com.youlai.boot.portal.model.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;

@TableName("questionnaire_survey")
@Getter
@Setter
public class QuestionnaireSurvey implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    @TableField("registration_category")
    private String registrationCategory;

    @TableField("registration_document")
    private String registrationDocument;

    @TableField("registration_which_member")
    private String registrationWhichMember;

    @TableField("registration_code")
    private String registrationCode;

    @TableField("is_match_drug")
    private Integer isMatchDrug;

    @TableField("dietary_requirements")
    private String dietaryRequirements;

    @TableField("special_requirements")
    private String specialRequirements;

    @TableField("is_first")
    private Integer isFirst;

    private String gender;

    private String occupation;

    private String experience;

    @TableField("field_of_interest")
    private String fieldOfInterest;

    private String other;
}
