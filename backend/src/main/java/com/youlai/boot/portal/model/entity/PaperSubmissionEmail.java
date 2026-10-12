package com.youlai.boot.portal.model.entity;

import com.baomidou.mybatisplus.annotation.FieldStrategy;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 论文投稿发送邮件记录实体
 *
 * @author 2432691112
 * @since 2026/5/13
 */
@TableName("paper_submission_email")
@Getter
@Setter
public class PaperSubmissionEmail implements Serializable {

    /**
     * 主键ID
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 论文投稿ID
     */
    private Long submissionId;

    /**
     * 专家姓名
     */
    private String expertName;

    /**
     * 专家邮箱
     */
    private String expertEmail;

    /**
     * 投稿件链接
     */
    private String url;

    /**
     * 发送人姓名
     */
    private String senderName;

    /**
     * 发送状态(1-发送中 2-发送成功 3-发送失败)
     */
    private Integer sendStatus;

    /**
     * 发送失败时的错误信息（仅在sendStatus=3时有值）
     * 重发成功后需要清除，使用 ALWAYS 策略确保 null 值也能写入数据库
     */
    @TableField(updateStrategy = FieldStrategy.ALWAYS)
    private String sendErrorMsg;

    /**
     * 发送时间
     */
    private LocalDateTime sendTime;

    /**
     * 审核结果(0-未审核 1-已通过 2-未通过)
     */
    private Integer reviewResult;

    /**
     * 拒绝原因
     */
    private String rejectReason;

    /**
     * 审核时间
     */
    private LocalDateTime reviewTime;
}
