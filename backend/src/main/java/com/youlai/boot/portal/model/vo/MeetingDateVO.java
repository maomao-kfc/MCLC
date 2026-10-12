package com.youlai.boot.portal.model.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Schema(description = "会议日期VO")
@Data
public class MeetingDateVO {

    @Schema(description = "ID")
    private Long id;

    @Schema(description = "会议名称")
    private String meetName;

    @Schema(description = "会议时间")
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate meetTime;

    @Schema(description = "会议状态")
    private Integer meetStatus;

    @Schema(description = "会议地点")
    private String meetAddress;

    @Schema(description = "时间段ID")
    private Long timePeriodId;

    @Schema(description = "排序")
    private Integer meetSort;

    @Schema(description = "国家类型")
    private String countryType;

    @Schema(description = "会议阶段列表")
    private List<MeetingPeriodVO> sessions;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createTime;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime updateTime;
}