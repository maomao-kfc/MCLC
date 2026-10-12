package com.youlai.boot.support.mail.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.youlai.boot.support.mail.enums.BounceType;
import com.youlai.boot.support.mail.mapper.MailBounceRecordMapper;
import com.youlai.boot.support.mail.model.entity.MailBounceRecord;
import com.youlai.boot.support.mail.model.query.MailBounceRecordQuery;
import com.youlai.boot.support.mail.service.MailBounceRecordService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * 邮件退信记录服务实现类
 *
 * @author system
 * @since 2026/8/3
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class MailBounceRecordServiceImpl extends ServiceImpl<MailBounceRecordMapper, MailBounceRecord>
        implements MailBounceRecordService {

    @Override
    public MailBounceRecord findByBounceMessageId(String bounceMessageId) {
        if (bounceMessageId == null || bounceMessageId.isBlank()) {
            return null;
        }
        return this.getOne(new LambdaQueryWrapper<MailBounceRecord>()
                .eq(MailBounceRecord::getBounceMessageId, bounceMessageId)
                .last("LIMIT 1"));
    }

    @Override
    public List<MailBounceRecord> findByOriginalMessageId(String originalMessageId) {
        if (originalMessageId == null || originalMessageId.isBlank()) {
            return List.of();
        }
        return this.list(new LambdaQueryWrapper<MailBounceRecord>()
                .eq(MailBounceRecord::getOriginalMessageId, originalMessageId)
                .orderByDesc(MailBounceRecord::getCreateTime));
    }

    @Override
    public List<MailBounceRecord> findByMatchedRecordId(Long matchedRecordId) {
        if (matchedRecordId == null) {
            return List.of();
        }
        return this.list(new LambdaQueryWrapper<MailBounceRecord>()
                .eq(MailBounceRecord::getMatchedRecordId, matchedRecordId)
                .orderByDesc(MailBounceRecord::getCreateTime));
    }

    @Override
    public boolean saveIfAbsent(MailBounceRecord record) {
        // 先检查是否已存在
        MailBounceRecord existing = findByBounceMessageId(record.getBounceMessageId());
        if (existing != null) {
            log.info("退信记录已存在，跳过保存：bounceMessageId={}", record.getBounceMessageId());
            return false;
        }

        try {
            this.save(record);
            log.info("退信记录保存成功：bounceMessageId={}, bounceType={}, bounceCode={}",
                    record.getBounceMessageId(), record.getBounceType(), record.getBounceCode());
            return true;
        } catch (DuplicateKeyException e) {
            // 唯一索引冲突，说明并发场景下其他线程已插入
            log.info("退信记录并发插入冲突，跳过：bounceMessageId={}", record.getBounceMessageId());
            return false;
        }
    }

    @Override
    public List<MailBounceRecord> findUnprocessed(int limit) {
        return this.list(new LambdaQueryWrapper<MailBounceRecord>()
                .eq(MailBounceRecord::getProcessed, false)
                .orderByAsc(MailBounceRecord::getCreateTime)
                .last("LIMIT " + limit));
    }

    @Override
    public Page<MailBounceRecord> getMailBounceRecordPage(MailBounceRecordQuery query) {
        LambdaQueryWrapper<MailBounceRecord> wrapper = new LambdaQueryWrapper<>();

        // 退信收件人（模糊搜索）
        if (query.getBouncedRecipient() != null && !query.getBouncedRecipient().isBlank()) {
            wrapper.like(MailBounceRecord::getBouncedRecipient, query.getBouncedRecipient());
        }

        // 退信类型
        if (query.getBounceType() != null && !query.getBounceType().isBlank()) {
            try {
                BounceType bounceType = BounceType.valueOf(query.getBounceType());
                wrapper.eq(MailBounceRecord::getBounceType, bounceType);
            } catch (IllegalArgumentException e) {
                log.warn("无效的退信类型：{}", query.getBounceType());
            }
        }

        // 是否永久性退信
        if (query.getBouncePermanent() != null) {
            wrapper.eq(MailBounceRecord::getBouncePermanent, query.getBouncePermanent());
        }

        // 匹配策略
        if (query.getMatchStrategy() != null && !query.getMatchStrategy().isBlank()) {
            wrapper.eq(MailBounceRecord::getMatchStrategy, query.getMatchStrategy());
        }

        // 是否已处理
        if (query.getProcessed() != null) {
            wrapper.eq(MailBounceRecord::getProcessed, query.getProcessed());
        }

        // 匹配到的发送记录ID
        if (query.getMatchedRecordId() != null) {
            wrapper.eq(MailBounceRecord::getMatchedRecordId, query.getMatchedRecordId());
        }

        // 时间范围（按退信日期筛选）
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        if (query.getStartTime() != null && !query.getStartTime().isBlank()) {
            try {
                LocalDateTime start = LocalDateTime.parse(query.getStartTime(), formatter);
                wrapper.ge(MailBounceRecord::getBounceDate, start);
            } catch (Exception e) {
                log.warn("无效的开始时间格式：{}", query.getStartTime());
            }
        }
        if (query.getEndTime() != null && !query.getEndTime().isBlank()) {
            try {
                LocalDateTime end = LocalDateTime.parse(query.getEndTime(), formatter);
                wrapper.le(MailBounceRecord::getBounceDate, end);
            } catch (Exception e) {
                log.warn("无效的结束时间格式：{}", query.getEndTime());
            }
        }

        // 排序：按退信日期倒序
        wrapper.orderByDesc(MailBounceRecord::getBounceDate);

        return this.page(new Page<>(query.getPageNum(), query.getPageSize()), wrapper);
    }
}