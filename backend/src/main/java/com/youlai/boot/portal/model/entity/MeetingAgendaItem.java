package com.youlai.boot.portal.model.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.io.Serializable;
import java.util.Date;
import lombok.Data;

/**
 * 会议详细议程条目表
 * @TableName meeting_agenda_item
 */
@TableName(value ="meeting_agenda_item")
@Data
public class MeetingAgendaItem implements Serializable {
    /**
     * 主键ID
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 关联板块ID
     */
    @TableField(value = "session_id")
    private Long sessionId;

    /**
     * 时间段 (如：08:00-08:10)
     */
    @TableField(value = "time_range")
    private String timeRange;

    /**
     * 类型 (如：开幕式、主旨报告、专题演讲)
     */
    @TableField(value = "type")
    private String type;

    /**
     * 类型（英文）
     */
    @TableField(value = "type_en")
    private String typeEn;

    /**
     * 题目
     */
    @TableField(value = "topic")
    private String topic;

    /**
     * 题目（英文）
     */
    @TableField(value = "topic_en")
    private String topicEn;

    /**
     * 日程类型枚举：OPENING/KEYNOTE/INVITED_SPEECH/DISCUSSION/BREAK/LUNCH/CLOSING/OTHER
     */
    @TableField(value = "item_type")
    private String itemType;

    /**
     * 关联嘉宾ID
     */
    @TableField(value = "speaker_id")
    private Long speakerId;

    /**
     * 讲者姓名（中文）- 直接输入，不依赖嘉宾表
     */
    @TableField(value = "speaker_name")
    private String speakerName;

    /**
     * 讲者姓名（英文）
     */
    @TableField(value = "speaker_name_en")
    private String speakerNameEn;

    /**
     * 讲者单位（中文）
     */
    @TableField(value = "speaker_org")
    private String speakerOrg;

    /**
     * 讲者单位（英文）
     */
    @TableField(value = "speaker_org_en")
    private String speakerOrgEn;

    /**
     * 板块内排序
     */
    @TableField(value = "sort_order")
    private Integer sortOrder;

    /**
     * 是否为茶歇/午餐等特殊项 0-否 1-是
     */
    @TableField(value = "is_break")
    private Integer isBreak;

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
        MeetingAgendaItem other = (MeetingAgendaItem) that;
        return (this.getId() == null ? other.getId() == null : this.getId().equals(other.getId()))
            && (this.getSessionId() == null ? other.getSessionId() == null : this.getSessionId().equals(other.getSessionId()))
            && (this.getTimeRange() == null ? other.getTimeRange() == null : this.getTimeRange().equals(other.getTimeRange()))
            && (this.getType() == null ? other.getType() == null : this.getType().equals(other.getType()))
            && (this.getTopic() == null ? other.getTopic() == null : this.getTopic().equals(other.getTopic()))
            && (this.getSpeakerId() == null ? other.getSpeakerId() == null : this.getSpeakerId().equals(other.getSpeakerId()))
            && (this.getSortOrder() == null ? other.getSortOrder() == null : this.getSortOrder().equals(other.getSortOrder()))
            && (this.getIsBreak() == null ? other.getIsBreak() == null : this.getIsBreak().equals(other.getIsBreak()))
            && (this.getCreateTime() == null ? other.getCreateTime() == null : this.getCreateTime().equals(other.getCreateTime()))
            && (this.getUpdateTime() == null ? other.getUpdateTime() == null : this.getUpdateTime().equals(other.getUpdateTime()));
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((getId() == null) ? 0 : getId().hashCode());
        result = prime * result + ((getSessionId() == null) ? 0 : getSessionId().hashCode());
        result = prime * result + ((getTimeRange() == null) ? 0 : getTimeRange().hashCode());
        result = prime * result + ((getType() == null) ? 0 : getType().hashCode());
        result = prime * result + ((getTopic() == null) ? 0 : getTopic().hashCode());
        result = prime * result + ((getSpeakerId() == null) ? 0 : getSpeakerId().hashCode());
        result = prime * result + ((getSortOrder() == null) ? 0 : getSortOrder().hashCode());
        result = prime * result + ((getIsBreak() == null) ? 0 : getIsBreak().hashCode());
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
        sb.append(", sessionId=").append(sessionId);
        sb.append(", timeRange=").append(timeRange);
        sb.append(", type=").append(type);
        sb.append(", topic=").append(topic);
        sb.append(", speakerId=").append(speakerId);
        sb.append(", sortOrder=").append(sortOrder);
        sb.append(", isBreak=").append(isBreak);
        sb.append(", createTime=").append(createTime);
        sb.append(", updateTime=").append(updateTime);
        sb.append(", serialVersionUID=").append(serialVersionUID);
        sb.append("]");
        return sb.toString();
    }
}
