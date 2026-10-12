package com.youlai.boot.system.model.bo;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 注册类型对象
 *
 * @author Theo
 * @since 2024-09-01 10:31
 */
@Data
public class RegistrationTypeBo {
    /**
     * 注册类型ID
     */
    private Long id;

    /**
     * 字段名称
     */
    private String fieldName;

    /**
     * 常规有效期
     */
    private LocalDateTime normalValidDate;

    /**
     * 截止日期
     */
    private LocalDateTime deadlineDate;

    /**
     * 常规有效期金额
     */
    private String normalAmount;

    /**
     * 截止日期金额
     */
    private String deadlineAmount;

    /**
     * 排序
     */
    private Integer sort;

    /**
     * 国家类型
     */
    private String countryType;
}
