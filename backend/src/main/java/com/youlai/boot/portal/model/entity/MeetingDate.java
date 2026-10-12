package com.youlai.boot.portal.model.entity;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Getter;
import lombok.Setter;
import java.time.LocalDate;
import java.time.LocalDateTime;
@TableName("portal_meeting_date")
@Getter
@Setter
public class MeetingDate {
    @TableId(value = "meet_id", type = IdType.AUTO)
    private Long id;
private String meetName;
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate meetTime;
    private Integer meetStatus;
    private String meetAddress;
    private Long timePeriodId;
    private Integer meetSort;
    private String countryType;
    @TableField("create_by")
    private Long createBy;
    @TableField("update_by")
    private Long updateBy;
    @TableField("is_deleted")
    private Integer isDeleted;
    @TableField("create_time")
    private LocalDateTime createTime;
    @TableField("update_time")
    private LocalDateTime updateTime;
}
