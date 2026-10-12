package com.youlai.boot.support.mail.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.youlai.boot.support.mail.model.entity.MailImapCheckpoint;
import org.apache.ibatis.annotations.Mapper;

/**
 * IMAP扫描检查点 Mapper
 *
 * @author system
 * @since 2026/8/3
 */
@Mapper
public interface MailImapCheckpointMapper extends BaseMapper<MailImapCheckpoint> {
}