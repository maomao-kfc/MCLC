package com.youlai.boot.portal.model.form;

import lombok.Data;

import jakarta.validation.constraints.NotBlank;

/**
 * Panel Discussion参与人表单
 */
@Data
public class ParticipantForm {

    private Long id;

    private Long sessionId;

    @NotBlank(message = "参与人姓名不能为空")
    private String name;

    private String nameEn;

    private String title;

    private String titleEn;

    private String organization;

    private String organizationEn;

    private Integer sortOrder;
}