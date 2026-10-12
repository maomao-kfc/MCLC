package com.youlai.boot.support.mail.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.youlai.boot.support.mail.model.entity.MailBounceRecord;
import org.apache.ibatis.annotations.Mapper;

/**
 * 邮件退信记录 Mapper
 *
 * @author system
 * @since 2026/8/3
 */
@Mapper
public interface MailBounceRecordMapper extends BaseMapper<MailBounceRecord> {
}