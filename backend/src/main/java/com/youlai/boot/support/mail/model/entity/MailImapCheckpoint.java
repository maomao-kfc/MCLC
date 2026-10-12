package com.youlai.boot.support.mail.model.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * IMAP扫描检查点实体
 *
 * 记录IMAP邮箱的UID扫描进度，实现增量扫描避免每次全量遍历INBOX。
 * 设计要点：
 * 1. 每个folder_name+username组合一条记录（单发件箱场景）
 * 2. 记录UIDVALIDITY，当邮箱UID重置时自动重新全量扫描
 * 3. last_uid记录上次扫描到的最大UID，下次只扫描UID > last_uid的邮件
 *
 * @author system
 * @since 2026/8/3
 */
@TableName("mail_imap_checkpoint")
@Getter
@Setter
public class MailImapCheckpoint implements Serializable {

    /** 主键ID */
    @TableId(type = IdType.AUTO)
    private Long id;

    /** IMAP文件夹名称（如INBOX） */
    private String folderName;

    /** IMAP登录用户名 */
    private String username;

    /** IMAP文件夹的UIDVALIDITY（用于检测UID重置） */
    private Long uidValidity;

    /** 上次扫描到的最大UID（增量扫描起点） */
    private Long lastUid;

    /** 最近一次扫描时间 */
    private LocalDateTime lastCheckTime;

    /** 最近一次扫描处理的邮件数 */
    private Integer lastCheckCount;

    /** 最近一次扫描发现的退信数 */
    private Integer lastBounceCount;

    /** 创建时间 */
    private LocalDateTime createTime;

    /** 更新时间 */
    private LocalDateTime updateTime;
}