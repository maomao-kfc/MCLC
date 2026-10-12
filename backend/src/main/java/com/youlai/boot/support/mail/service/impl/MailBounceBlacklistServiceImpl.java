package com.youlai.boot.support.mail.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.youlai.boot.core.exception.BusinessException;
import com.youlai.boot.support.mail.mapper.MailBounceBlacklistMapper;
import com.youlai.boot.support.mail.model.entity.MailBounceBlacklist;
import com.youlai.boot.support.mail.service.MailBounceBlacklistService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 邮件退信黑名单服务实现类
 *
 * @author system
 * @since 2026/8/3
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class MailBounceBlacklistServiceImpl extends ServiceImpl<MailBounceBlacklistMapper, MailBounceBlacklist>
        implements MailBounceBlacklistService {

    @Override
    public boolean isBlacklisted(String email) {
        if (email == null || email.isBlank()) {
            return false;
        }
        return this.count(new LambdaQueryWrapper<MailBounceBlacklist>()
                .eq(MailBounceBlacklist::getEmail, email.trim().toLowerCase())) > 0;
    }

    @Override
    public void autoAdd(String email, String reason, Long relatedRecordId) {
        if (email == null || email.isBlank()) {
            return;
        }
        String normalizedEmail = email.trim().toLowerCase();

        // 检查是否已在黑名单中
        MailBounceBlacklist existing = this.getOne(new LambdaQueryWrapper<MailBounceBlacklist>()
                .eq(MailBounceBlacklist::getEmail, normalizedEmail)
                .last("LIMIT 1"));

        if (existing != null) {
            // 已在黑名单中，递增退信次数并更新时间
            existing.setBounceCount(existing.getBounceCount() == null ? 2 : existing.getBounceCount() + 1);
            existing.setLastBounceTime(LocalDateTime.now());
            existing.setReason(reason);
            existing.setUpdateTime(LocalDateTime.now());
            this.updateById(existing);
            log.info("黑名单更新：email={}，累计退信次数={}", normalizedEmail, existing.getBounceCount());
        } else {
            // 新增黑名单记录
            MailBounceBlacklist blacklist = new MailBounceBlacklist();
            blacklist.setEmail(normalizedEmail);
            blacklist.setReason(reason);
            blacklist.setSource("AUTO");
            blacklist.setBounceCount(1);
            blacklist.setLastBounceTime(LocalDateTime.now());
            blacklist.setRelatedRecordId(relatedRecordId);
            blacklist.setCreateTime(LocalDateTime.now());
            blacklist.setUpdateTime(LocalDateTime.now());
            this.save(blacklist);
            log.info("黑名单新增：email={}，原因={}", normalizedEmail, reason);
        }
    }

    @Override
    public void manualAdd(String email, String reason) {
        if (email == null || email.isBlank()) {
            throw new BusinessException("邮箱地址不能为空");
        }
        String normalizedEmail = email.trim().toLowerCase();

        // 检查是否已在黑名单中
        long count = this.count(new LambdaQueryWrapper<MailBounceBlacklist>()
                .eq(MailBounceBlacklist::getEmail, normalizedEmail));
        if (count > 0) {
            throw new BusinessException("该邮箱已在黑名单中");
        }

        MailBounceBlacklist blacklist = new MailBounceBlacklist();
        blacklist.setEmail(normalizedEmail);
        blacklist.setReason(reason);
        blacklist.setSource("MANUAL");
        blacklist.setBounceCount(0);
        blacklist.setCreateTime(LocalDateTime.now());
        blacklist.setUpdateTime(LocalDateTime.now());
        this.save(blacklist);
        log.info("手动添加黑名单：email={}，原因={}", normalizedEmail, reason);
    }

    @Override
    public void remove(Long id) {
        MailBounceBlacklist blacklist = this.getById(id);
        if (blacklist == null) {
            throw new BusinessException("黑名单记录不存在");
        }
        this.removeById(id);
        log.info("移除黑名单：id={}，email={}", id, blacklist.getEmail());
    }

    @Override
    public Page<MailBounceBlacklist> listPage(Integer pageNum, Integer pageSize) {
        int page = pageNum != null && pageNum > 0 ? pageNum : 1;
        int size = pageSize != null && pageSize > 0 ? pageSize : 10;
        return this.page(new Page<>(page, size),
                new LambdaQueryWrapper<MailBounceBlacklist>()
                        .orderByDesc(MailBounceBlacklist::getCreateTime));
    }

    @Override
    public List<MailBounceBlacklist> listAll() {
        return this.list(new LambdaQueryWrapper<MailBounceBlacklist>()
                .orderByDesc(MailBounceBlacklist::getCreateTime));
    }
}