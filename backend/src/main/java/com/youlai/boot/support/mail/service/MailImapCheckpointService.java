package com.youlai.boot.support.mail.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.youlai.boot.support.mail.model.entity.MailImapCheckpoint;

/**
 * IMAP扫描检查点服务接口
 *
 * 管理IMAP邮箱的UID扫描进度，实现增量扫描。
 * 核心设计：
 * 1. 每个folder_name+username组合一条记录
 * 2. 记录UIDVALIDITY，当邮箱UID重置时自动重新全量扫描
 * 3. last_uid记录上次扫描到的最大UID，下次只扫描UID > last_uid的邮件
 *
 * @author system
 * @since 2026/8/3
 */
public interface MailImapCheckpointService extends IService<MailImapCheckpoint> {

    /**
     * 获取检查点
     *
     * @param folderName IMAP文件夹名称
     * @param username   IMAP登录用户名
     * @return 检查点记录，不存在返回null
     */
    MailImapCheckpoint getCheckpoint(String folderName, String username);

    /**
     * 更新检查点（保存或更新）
     *
     * @param folderName     IMAP文件夹名称
     * @param username       IMAP登录用户名
     * @param uidValidity    当前UIDVALIDITY
     * @param lastUid        最新扫描到的UID
     * @param checkCount     本次扫描邮件数
     * @param bounceCount    本次发现退信数
     */
    void updateCheckpoint(String folderName, String username, Long uidValidity,
                          Long lastUid, int checkCount, int bounceCount);

    /**
     * 重置检查点（UIDVALIDITY变化时调用）
     *
     * @param folderName IMAP文件夹名称
     * @param username   IMAP登录用户名
     */
    void resetCheckpoint(String folderName, String username);
}