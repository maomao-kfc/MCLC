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
@TableName("portal_question")
@Getter
@Setter
public class Question extends BaseEntity {

    /**
     * 问题标题
     */
    private String title;

    /**
     * 问题、答案描述
     */
    private String questionDetail;

    /**
     * 备注
     */
    private String remark;

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
