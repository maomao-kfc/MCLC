package com.youlai.boot.portal.model.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.youlai.boot.common.base.BaseEntity;
import lombok.Getter;
import lombok.Setter;

/**
 * 精彩瞬间实体
 *
 * @author 2432691112
 * @since 2026/4/22
 */
@TableName("portal_moments")
@Getter
@Setter
public class Moments extends BaseEntity {

    /**
     * 姓名
     */
    private String name;

    /**
     * 职务
     */
    private String position;

    /**
     * 类型
     */
    private String type;

    /**
     * 国家类型
     */
    private String countryType;

    /**
     * 图片
     */
    private String momentsImage;

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
