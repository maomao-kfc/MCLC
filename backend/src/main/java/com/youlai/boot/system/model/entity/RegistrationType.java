package com.youlai.boot.system.model.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@TableName("registration_type")
public class RegistrationType {
    /**
     * 注册类型ID
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 字段名称
     */
    @TableField(value = "field_name")
    private String fieldName;

    /**
     * 常规有效期
     */
    @TableField(value = "normal_valid_date")
    private LocalDateTime normalValidDate;

    /**
     * 截止日期
     */
    @TableField(value = "deadline_date")
    private LocalDateTime deadlineDate;

    /**
     * 常规有效期金额
     */
    @TableField(value = "normal_amount")
    private String normalAmount;

    /**
     * 截止日期金额
     */
    @TableField(value = "deadline_amount")
    private String deadlineAmount;

    /**
     * 排序
     */
    @TableField(value = "sort")
    private Integer sort;

    /**
     * 国家类型
     */
    @TableField(value = "country_type")
    private String countryType;
}
