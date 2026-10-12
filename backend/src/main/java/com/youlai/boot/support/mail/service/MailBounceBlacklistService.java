package com.youlai.boot.support.mail.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.youlai.boot.support.mail.model.entity.MailBounceBlacklist;

import java.util.List;

/**
 * 邮件退信黑名单服务接口
 *
 * 提供黑名单的增删查功能，以及发送前的黑名单校验。
 *
 * @author system
 * @since 2026/8/3
 */
public interface MailBounceBlacklistService extends IService<MailBounceBlacklist> {

    /**
     * 检查邮箱是否在黑名单中
     *
     * @param email 邮箱地址
     * @return true=在黑名单中，false=不在
     */
    boolean isBlacklisted(String email);

    /**
     * 自动添加黑名单（IMAP退信检测触发）
     *
     * 如果邮箱已在黑名单中，则递增退信次数并更新最近退信时间。
     * 如果邮箱不在黑名单中，则新增记录。
     *
     * @param email 邮箱地址
     * @param reason 退信原因
     * @param relatedRecordId 关联的邮件发送记录ID
     */
    void autoAdd(String email, String reason, Long relatedRecordId);

    /**
     * 手动添加黑名单（管理员操作）
     *
     * @param email 邮箱地址
     * @param reason 原因
     */
    void manualAdd(String email, String reason);

    /**
     * 移除黑名单（管理员操作）
     *
     * @param id 黑名单记录ID
     */
    void remove(Long id);

    /**
     * 分页查询黑名单列表
     *
     * @param pageNum 页码（从1开始）
     * @param pageSize 每页条数
     * @return 分页结果
     */
    Page<MailBounceBlacklist> listPage(Integer pageNum, Integer pageSize);

    /**
     * 查询所有黑名单列表
     *
     * @return 黑名单列表
     */
    List<MailBounceBlacklist> listAll();
}