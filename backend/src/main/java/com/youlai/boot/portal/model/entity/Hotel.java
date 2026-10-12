package com.youlai.boot.portal.model.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.youlai.boot.common.base.BaseEntity;
import lombok.Getter;
import lombok.Setter;

/**
 * 酒店信息实体
 *
 * @author 2432691112
 * @since 2026/4/22
 */
@TableName("portal_hotel")
@Getter
@Setter
public class Hotel extends BaseEntity {

    /**
     * 酒店名称
     */
    private String hotelName;

    /**
     * 酒店电话
     */
    private String hotelPhone;

    /**
     * 酒店联系人
     */
    private String hotelContact;

    /**
     * 国家类型
     */
    private String countryType;

    /**
     * 酒店图片
     */
    private String hotelImage;

    /**
     * 酒店地址
     */
    private String hotelAddress;

    /**
     * 酒店描述
     */
    private String hotelDesc;

    /**
     * 酒店链接
     */
    private String hotelUrl;

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
