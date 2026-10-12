package com.youlai.boot.portal.model.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.io.Serializable;
import java.util.Date;
import lombok.Data;

/**
 * 会议日程板块表
 * @TableName meeting_session
 */
@TableName(value ="meeting_session")
@Data
public class MeetingSession implements Serializable {
    /**
     * 主键ID
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 关联会议ID
     */
    @TableField(value = "meeting_id")
    private Long meetingId;

    /**
     * 板块标题 (如：主旨报告)
     */
    @TableField(value = "title")
    private String title;

    /**
     * 板块标题（英文）
     */
    @TableField(value = "title_en")
    private String titleEn;

    /**
     * 主持人，多个逗号分隔
     */
    @TableField(value = "chairperson")
    private String chairperson;

    /**
     * 主持人（英文），多个逗号分隔
     */
    @TableField(value = "chairperson_en")
    private String chairpersonEn;

    /**
     * 讨论嘉宾，多个逗号分隔
     */
    @TableField(value = "panelists")
    private String panelists;

    /**
     * 讨论嘉宾（英文），多个逗号分隔
     */
    @TableField(value = "panelists_en")
    private String panelistsEn;

    /**
     * 时间段文本（如 09:00-10:30），用于 DISCUSSION 等无条目板块的时间显示
     */
    @TableField(value = "time_period")
    private String timePeriod;

    /**
     * 时间段文本（英文）
     */
    @TableField(value = "time_period_en")
    private String timePeriodEn;

    /**
     * 讨论主题内容（中文，含子条目）
     */
    @TableField(value = "topic_content")
    private String topicContent;

    /**
     * 讨论主题内容（英文）
     */
    @TableField(value = "topic_content_en")
    private String topicContentEn;

    /**
     * 板块开始时间 (仅用于辅助排序)
     */
    @TableField(value = "start_time")
    private Date startTime;

    /**
     * 板块类型枚举：DISCUSSION等
     */
    @TableField(value = "item_type")
    private String itemType;

    /**
     * 显示顺序
     */
    @TableField(value = "sort_order")
    private Integer sortOrder;

    /**
     * 备注 (如：茶歇、午餐)
     */
    @TableField(value = "remark")
    private String remark;

    /**
     * 备注（英文）
     */
    @TableField(value = "remark_en")
    private String remarkEn;

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
        MeetingSession other = (MeetingSession) that;
        return (this.getId() == null ? other.getId() == null : this.getId().equals(other.getId()))
            && (this.getMeetingId() == null ? other.getMeetingId() == null : this.getMeetingId().equals(other.getMeetingId()))
            && (this.getTitle() == null ? other.getTitle() == null : this.getTitle().equals(other.getTitle()))
            && (this.getChairperson() == null ? other.getChairperson() == null : this.getChairperson().equals(other.getChairperson()))
            && (this.getPanelists() == null ? other.getPanelists() == null : this.getPanelists().equals(other.getPanelists()))
            && (this.getStartTime() == null ? other.getStartTime() == null : this.getStartTime().equals(other.getStartTime()))
            && (this.getSortOrder() == null ? other.getSortOrder() == null : this.getSortOrder().equals(other.getSortOrder()))
            && (this.getRemark() == null ? other.getRemark() == null : this.getRemark().equals(other.getRemark()))
            && (this.getCreateTime() == null ? other.getCreateTime() == null : this.getCreateTime().equals(other.getCreateTime()))
            && (this.getUpdateTime() == null ? other.getUpdateTime() == null : this.getUpdateTime().equals(other.getUpdateTime()));
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((getId() == null) ? 0 : getId().hashCode());
        result = prime * result + ((getMeetingId() == null) ? 0 : getMeetingId().hashCode());
        result = prime * result + ((getTitle() == null) ? 0 : getTitle().hashCode());
        result = prime * result + ((getChairperson() == null) ? 0 : getChairperson().hashCode());
        result = prime * result + ((getPanelists() == null) ? 0 : getPanelists().hashCode());
        result = prime * result + ((getStartTime() == null) ? 0 : getStartTime().hashCode());
        result = prime * result + ((getSortOrder() == null) ? 0 : getSortOrder().hashCode());
        result = prime * result + ((getRemark() == null) ? 0 : getRemark().hashCode());
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
        sb.append(", meetingId=").append(meetingId);
        sb.append(", title=").append(title);
        sb.append(", chairperson=").append(chairperson);
        sb.append(", panelists=").append(panelists);
        sb.append(", startTime=").append(startTime);
        sb.append(", sortOrder=").append(sortOrder);
        sb.append(", remark=").append(remark);
        sb.append(", createTime=").append(createTime);
        sb.append(", updateTime=").append(updateTime);
        sb.append(", serialVersionUID=").append(serialVersionUID);
        sb.append("]");
        return sb.toString();
    }
}