package com.youlai.boot.portal.model.form;

import lombok.Data;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;
import java.util.List;

/**
 * 会议信息表单（管理端创建/更新）
 */
@Data
public class MeetingInfoForm {

    @NotBlank(message = "会议名称不能为空")
    private String name;

    private String nameEn;

    @NotNull(message = "会议日期不能为空")
    private LocalDate meetingDate;

    private String venue;

    private String venueEn;

    private Integer status = 1;

    private Integer sortOrder;

    /** 板块列表（含条目和参与人） */
    private List<SessionForm> sessions;
}