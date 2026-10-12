package com.youlai.boot.support.mail.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.youlai.boot.support.mail.model.entity.MailBounceBlacklist;
import org.apache.ibatis.annotations.Mapper;

/**
 * 邮件退信黑名单 Mapper
 *
 * @author system
 * @since 2026/8/3
 */
@Mapper
public interface MailBounceBlacklistMapper extends BaseMapper<MailBounceBlacklist> {
}