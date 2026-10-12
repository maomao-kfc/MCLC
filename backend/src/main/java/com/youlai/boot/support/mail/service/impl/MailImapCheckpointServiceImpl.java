package com.youlai.boot.support.mail.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.youlai.boot.support.mail.mapper.MailImapCheckpointMapper;
import com.youlai.boot.support.mail.model.entity.MailImapCheckpoint;
import com.youlai.boot.support.mail.service.MailImapCheckpointService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

/**
 * IMAP扫描检查点服务实现类
 *
 * @author system
 * @since 2026/8/3
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class MailImapCheckpointServiceImpl extends ServiceImpl<MailImapCheckpointMapper, MailImapCheckpoint>
        implements MailImapCheckpointService {

    @Override
    public MailImapCheckpoint getCheckpoint(String folderName, String username) {
        return this.getOne(new LambdaQueryWrapper<MailImapCheckpoint>()
                .eq(MailImapCheckpoint::getFolderName, folderName)
                .eq(MailImapCheckpoint::getUsername, username)
                .last("LIMIT 1"));
    }

    @Override
    public void updateCheckpoint(String folderName, String username, Long uidValidity,
                                  Long lastUid, int checkCount, int bounceCount) {
        MailImapCheckpoint checkpoint = getCheckpoint(folderName, username);

        if (checkpoint == null) {
            // 首次扫描，创建检查点
            checkpoint = new MailImapCheckpoint();
            checkpoint.setFolderName(folderName);
            checkpoint.setUsername(username);
            checkpoint.setUidValidity(uidValidity);
            checkpoint.setLastUid(lastUid);
            checkpoint.setLastCheckTime(LocalDateTime.now());
            checkpoint.setLastCheckCount(checkCount);
            checkpoint.setLastBounceCount(bounceCount);
            checkpoint.setCreateTime(LocalDateTime.now());
            checkpoint.setUpdateTime(LocalDateTime.now());
            this.save(checkpoint);
            log.info("IMAP检查点创建：folder={}, user={}, uidValidity={}, lastUid={}",
                    folderName, username, uidValidity, lastUid);
        } else {
            // 更新检查点
            checkpoint.setUidValidity(uidValidity);
            checkpoint.setLastUid(lastUid);
            checkpoint.setLastCheckTime(LocalDateTime.now());
            checkpoint.setLastCheckCount(checkCount);
            checkpoint.setLastBounceCount(bounceCount);
            checkpoint.setUpdateTime(LocalDateTime.now());
            this.updateById(checkpoint);
            log.debug("IMAP检查点更新：folder={}, user={}, lastUid={}, checkCount={}, bounceCount={}",
                    folderName, username, lastUid, checkCount, bounceCount);
        }
    }

    @Override
    public void resetCheckpoint(String folderName, String username) {
        MailImapCheckpoint checkpoint = getCheckpoint(folderName, username);
        if (checkpoint != null) {
            // UIDVALIDITY变化，重置last_uid为0，下次将全量扫描
            checkpoint.setLastUid(0L);
            checkpoint.setLastCheckTime(LocalDateTime.now());
            checkpoint.setLastCheckCount(0);
            checkpoint.setLastBounceCount(0);
            checkpoint.setUpdateTime(LocalDateTime.now());
            this.updateById(checkpoint);
            log.warn("IMAP检查点重置（UIDVALIDITY变化）：folder={}, user={}", folderName, username);
        }
    }
}