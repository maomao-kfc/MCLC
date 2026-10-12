package com.youlai.boot.system.model.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.youlai.boot.common.base.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

@EqualsAndHashCode(callSuper = false)
@TableName("sys_email_template")
@Data
public class EmailTemplate extends BaseEntity {

    private String emailTitle;

    private String emailType;

    private String emailContent;

    private Integer sort;

    private Integer status;

    private String remark;

    private Integer isDeleted;

}
