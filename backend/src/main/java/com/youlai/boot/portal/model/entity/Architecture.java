package com.youlai.boot.portal.model.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.youlai.boot.common.base.BaseEntity;
import lombok.Getter;
import lombok.Setter;

/**
 * 组织架构实体
 *
 * @author 2432691112
 * @since 2026/5/25
 */
@TableName("portal_architecture")
@Getter
@Setter
public class Architecture extends BaseEntity {

    /**
     * 图片
     */
    private String imageHeader;

    /**
     * 姓名
     */
    private String name;

    /**
     * 职位
     */
    private String job;

    /**
     * 国家类型
     */
    private String countryType;

    /**
     * 显示顺序
     */
    private Integer sort;

    /**
     * 角色状态(1-正常 0-停用)
     */
    private Integer status;

    /**
     * 创建人 ID
     */
    private Long createBy;

    /**
     * 更新人 ID
     */
    private Long updateBy;

    /**
     * 是否删除(0-否 1-是)
     */
    private Integer isDeleted;
}
