package com.youlai.boot.portal.model.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.youlai.boot.common.base.BaseEntity;
import lombok.Getter;
import lombok.Setter;

/**
 * 特邀嘉宾实体
 *
 * @author 2432691112
 * @since 2026/4/22
 */
@TableName("portal_guest")
@Getter
@Setter
public class Guest extends BaseEntity {

    /**
     * 特邀嘉宾姓名
     */
    private String name;

    /**
     * 特邀嘉宾职务
     */
    private String position;

    /**
     * 国家类型
     */
    private String countryType;

    /**
     * 特邀嘉图像
     */
    private String image;

    /**
     * 特邀嘉宾类型
     */
    private String type;

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
