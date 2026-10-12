package com.youlai.boot.portal.model.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;

/**
 * 论文投稿实体
 *
 * @author 2432691112
 * @since 2026/5/6
 */
@TableName("paper_submission")
@Getter
@Setter
public class PaperSubmission implements Serializable {

    /**
     * 主键ID
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 投稿人姓名
     */
    private String name;

    /**
     * 所属单位/医院
     */
    private String company;

    /**
     * 姓
     */
    @TableField(exist = false)
    private String surname;

    /**
     * 论文标题
     */
    private String paperTitle;

    /**
     * 第一作者
     */
    private String firstAuthor;

    /**
     * 联系电话
     */
    private String contactNumber;

    /**
     * 联系邮箱
     */
    private String contactEmail;

    /**
     * 论文摘要
     */
    @TableField("abstract")
    private String paperAbstract;

    /**
     * 关键词
     */
    private String keywords;

    /**
     * 论文附件
     */
    private String appendix;

    /**
     * 投稿Id
     */
    private Long contributionId;

    /**
     * 审核状态(1-待审核 2-审核通过 3-审核拒绝)
     */
    private Integer paperStatus;

    /**
     * 拒绝原因
     */
    private String rejectReason;

    /**
     * 专家评审链接
     */
    private String expertUrl;

    /**
     * 邮件发送状态(0-未发送 1-发送失败 2-已发送)
     */
    private Integer emailStatus;

    /**
     * 抵扣状态
     */
    private Integer deductionStatus;

    /**
     * 抵扣码
     */
    private String discountCode;
}
