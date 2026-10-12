package com.youlai.boot.portal.model.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.io.Serializable;
import java.util.Date;
import lombok.Data;

/**
 * 嘉宾信息表
 * @TableName meeting_speaker
 */
@TableName(value ="meeting_speaker")
@Data
public class MeetingSpeaker implements Serializable {
    /**
     * 主键ID
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 嘉宾姓名 (中英文均可)
     */
    @TableField(value = "name")
    private String name;

    /**
     * 嘉宾姓名（英文）
     */
    @TableField(value = "name_en")
    private String nameEn;

    /**
     * 头像URL
     */
    @TableField(value = "avatar_url")
    private String avatarUrl;

    /**
     * 职称/级别 (如：教授、主任医师、博士)
     */
    @TableField(value = "title")
    private String title;

    /**
     * 职称/级别（英文）
     */
    @TableField(value = "title_en")
    private String titleEn;

    /**
     * 所属单位
     */
    @TableField(value = "affiliation")
    private String affiliation;

    /**
     * 所属单位（英文）
     */
    @TableField(value = "affiliation_en")
    private String affiliationEn;

    /**
     * 嘉宾简介 (一句话描述)
     */
    @TableField(value = "brief_intro")
    private String briefIntro;

    /**
     * 嘉宾简介（英文）
     */
    @TableField(value = "brief_intro_en")
    private String briefIntroEn;

    /**
     * 嘉宾详情介绍 (富文本)
     */
    @TableField(value = "detail_intro")
    private String detailIntro;

    /**
     * 嘉宾详情介绍（英文）
     */
    @TableField(value = "detail_intro_en")
    private String detailIntroEn;

    /**
     * 研究领域，逗号分隔
     */
    @TableField(value = "research_fields")
    private String researchFields;

    /**
     * 研究领域（英文），逗号分隔
     */
    @TableField(value = "research_fields_en")
    private String researchFieldsEn;

    /**
     * 显示排序
     */
    @TableField(value = "sort_order")
    private Integer sortOrder;

    /**
     * 状态 0-隐藏 1-显示
     */
    @TableField(value = "status")
    private Integer status;

    /**
     * 创建时间
     */
    @TableField(value = "create_time")
    private Date createTime;

    /**
     * 更新时间
     */
    @TableField(value = "update_time")
    private Date updateTime;

    @TableField(exist = false)
    private static final long serialVersionUID = 1L;

    @Override
    public boolean equals(Object that) {
        if (this == that) {
            return true;
        }
        if (that == null) {
            return false;
        }
        if (getClass() != that.getClass()) {
            return false;
        }
        MeetingSpeaker other = (MeetingSpeaker) that;
        return (this.getId() == null ? other.getId() == null : this.getId().equals(other.getId()))
            && (this.getName() == null ? other.getName() == null : this.getName().equals(other.getName()))
            && (this.getAvatarUrl() == null ? other.getAvatarUrl() == null : this.getAvatarUrl().equals(other.getAvatarUrl()))
            && (this.getTitle() == null ? other.getTitle() == null : this.getTitle().equals(other.getTitle()))
            && (this.getAffiliation() == null ? other.getAffiliation() == null : this.getAffiliation().equals(other.getAffiliation()))
            && (this.getBriefIntro() == null ? other.getBriefIntro() == null : this.getBriefIntro().equals(other.getBriefIntro()))
            && (this.getDetailIntro() == null ? other.getDetailIntro() == null : this.getDetailIntro().equals(other.getDetailIntro()))
            && (this.getResearchFields() == null ? other.getResearchFields() == null : this.getResearchFields().equals(other.getResearchFields()))
            && (this.getSortOrder() == null ? other.getSortOrder() == null : this.getSortOrder().equals(other.getSortOrder()))
            && (this.getStatus() == null ? other.getStatus() == null : this.getStatus().equals(other.getStatus()))
            && (this.getCreateTime() == null ? other.getCreateTime() == null : this.getCreateTime().equals(other.getCreateTime()))
            && (this.getUpdateTime() == null ? other.getUpdateTime() == null : this.getUpdateTime().equals(other.getUpdateTime()));
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((getId() == null) ? 0 : getId().hashCode());
        result = prime * result + ((getName() == null) ? 0 : getName().hashCode());
        result = prime * result + ((getAvatarUrl() == null) ? 0 : getAvatarUrl().hashCode());
        result = prime * result + ((getTitle() == null) ? 0 : getTitle().hashCode());
        result = prime * result + ((getAffiliation() == null) ? 0 : getAffiliation().hashCode());
        result = prime * result + ((getBriefIntro() == null) ? 0 : getBriefIntro().hashCode());
        result = prime * result + ((getDetailIntro() == null) ? 0 : getDetailIntro().hashCode());
        result = prime * result + ((getResearchFields() == null) ? 0 : getResearchFields().hashCode());
        result = prime * result + ((getSortOrder() == null) ? 0 : getSortOrder().hashCode());
        result = prime * result + ((getStatus() == null) ? 0 : getStatus().hashCode());
        result = prime * result + ((getCreateTime() == null) ? 0 : getCreateTime().hashCode());
        result = prime * result + ((getUpdateTime() == null) ? 0 : getUpdateTime().hashCode());
        return result;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(getClass().getSimpleName());
        sb.append(" [");
        sb.append("Hash = ").append(hashCode());
        sb.append(", id=").append(id);
        sb.append(", name=").append(name);
        sb.append(", avatarUrl=").append(avatarUrl);
        sb.append(", title=").append(title);
        sb.append(", affiliation=").append(affiliation);
        sb.append(", briefIntro=").append(briefIntro);
        sb.append(", detailIntro=").append(detailIntro);
        sb.append(", researchFields=").append(researchFields);
        sb.append(", sortOrder=").append(sortOrder);
        sb.append(", status=").append(status);
        sb.append(", createTime=").append(createTime);
        sb.append(", updateTime=").append(updateTime);
        sb.append(", serialVersionUID=").append(serialVersionUID);
        sb.append("]");
        return sb.toString();
    }
}