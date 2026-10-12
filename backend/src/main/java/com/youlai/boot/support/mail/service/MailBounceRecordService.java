package com.youlai.boot.support.mail.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.youlai.boot.support.mail.model.entity.MailBounceRecord;
import com.youlai.boot.support.mail.model.query.MailBounceRecordQuery;

import java.util.List;

/**
 * 邮件退信记录服务接口
 *
 * 提供退信记录的保存、查询、幂等检查等功能。
 * 核心设计：
 * 1. bounce_message_id 唯一索引保证幂等（同一退信不重复处理）
 * 2. 记录退信的完整解析信息（类型、永久/临时、原因、收件人、DSN等）
 * 3. 记录匹配结果（匹配到的发送记录ID、匹配策略）
 *
 * @author system
 * @since 2026/8/3
 */
public interface MailBounceRecordService extends IService<MailBounceRecord> {

    /**
     * 根据退信邮件Message-ID查找退信记录（幂等检查）
     *
     * @param bounceMessageId 退信邮件的Message-ID
     * @return 已存在的退信记录，不存在返回null
     */
    MailBounceRecord findByBounceMessageId(String bounceMessageId);

    /**
     * 根据原始发送邮件Message-ID查找退信记录
     *
     * @param originalMessageId 原始发送邮件的Message-ID
     * @return 退信记录列表
     */
    List<MailBounceRecord> findByOriginalMessageId(String originalMessageId);

    /**
     * 根据匹配到的发送记录ID查找退信记录
     *
     * @param matchedRecordId mail_send_record记录ID
     * @return 退信记录列表
     */
    List<MailBounceRecord> findByMatchedRecordId(Long matchedRecordId);

    /**
     * 保存退信记录（幂等，如果bounce_message_id已存在则跳过）
     *
     * @param record 退信记录
     * @return true=新保存成功，false=已存在跳过
     */
    boolean saveIfAbsent(MailBounceRecord record);

    /**
     * 查询未处理的退信记录
     *
     * @param limit 最大返回数量
     * @return 未处理的退信记录列表
     */
    List<MailBounceRecord> findUnprocessed(int limit);

    /**
     * 分页查询退信记录
     *
     * @param query 查询条件
     * @return 分页结果
     */
    Page<MailBounceRecord> getMailBounceRecordPage(MailBounceRecordQuery query);
}