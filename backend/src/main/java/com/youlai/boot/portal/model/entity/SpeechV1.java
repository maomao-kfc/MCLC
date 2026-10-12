package com.youlai.boot.portal.model.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.youlai.boot.common.base.BaseEntity;
import lombok.Getter;
import lombok.Setter;

/**
 * 演讲集锦实体
 *
 * @author 2432691112
 * @since 2026/4/22
 */
@TableName("portal_speech")
@Getter
@Setter
public class SpeechV1 extends BaseEntity {

    /**
     * 标题
     */
    private String title;

    /**
     * 姓名
     */
    private String name;

    /**
     * 职务
     */
    private String position;

    /**
     * 医生座右铭。
     */
    private String motto;

    /**
     * 国家类型
     */
    private String countryType;

    /**
     * 个人图片展示
     */
    private String images;

    /**
     * 封面展示
     */
    private String cover;

    /**
     * 个人介绍
     */
    private String introduction;


    /**
     * 独家专访视频
     */
    private String video;



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
