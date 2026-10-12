package com.youlai.boot.portal.model.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;

@TableName("user_check_in")
@Getter
@Setter
public class UserCheckIn implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    @TableField("check_id")
    private Long checkId;

    @TableField("check_time")
    private String checkTime;

    @TableField("check_name")
    private String checkName;

    @TableField("check_number")
    private String checkNumber;

    @TableField("check_email")
    private String checkEmail;

    @TableField("check_status")
    private String checkStatus;
}
