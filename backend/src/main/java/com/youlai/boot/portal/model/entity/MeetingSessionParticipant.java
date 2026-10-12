package com.youlai.boot.portal.model.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.io.Serializable;
import java.util.Date;
import lombok.Data;

/**
 * 会议板块参与人表（Panel Discussion等）
 * @TableName meeting_session_participant
 */
@TableName(value ="meeting_session_participant")
@Data
public class MeetingSessionParticipant implements Serializable {

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
     * 关联嘉宾ID（可为空，表示未录入嘉宾库的临时参与人）
     */
    @TableField(value = "speaker_id")
    private Long speakerId;

    /**
     * 参与人姓名（中文）
     */
    @TableField(value = "name")
    private String name;

    /**
     * 参与人姓名（英文）
     */
    @TableField(value = "name_en")
    private String nameEn;

    /**
     * 参与角色：MODERATOR/PANELIST/OTHER
     */
    @TableField(value = "role")
    private String role;

    /**
     * 显示顺序
     */
    @TableField(value = "sort_order")
    private Integer sortOrder;

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
}