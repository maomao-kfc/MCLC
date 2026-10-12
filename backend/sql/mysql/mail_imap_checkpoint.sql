-- ============================================================
-- IMAP扫描检查点表
-- 记录IMAP邮箱的UID扫描进度，实现增量扫描避免每次全量遍历INBOX
-- 设计要点：
-- 1. 每个folder_name+username组合一条记录（单发件箱场景）
-- 2. 记录UIDVALIDITY，当邮箱UID重置时自动重新全量扫描
-- 3. last_uid记录上次扫描到的最大UID，下次只扫描UID > last_uid的邮件
-- ============================================================

CREATE TABLE IF NOT EXISTS `mail_imap_checkpoint` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `folder_name` VARCHAR(100) NOT NULL COMMENT 'IMAP文件夹名称（如INBOX）',
    `username` VARCHAR(255) NOT NULL COMMENT 'IMAP登录用户名',
    `uid_validity` BIGINT DEFAULT NULL COMMENT 'IMAP文件夹的UIDVALIDITY（用于检测UID重置）',
    `last_uid` BIGINT NOT NULL DEFAULT 0 COMMENT '上次扫描到的最大UID（增量扫描起点）',
    `last_check_time` DATETIME NOT NULL COMMENT '最近一次扫描时间',
    `last_check_count` INT DEFAULT 0 COMMENT '最近一次扫描处理的邮件数',
    `last_bounce_count` INT DEFAULT 0 COMMENT '最近一次扫描发现的退信数',
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_folder_user` (`folder_name`, `username`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='IMAP扫描检查点';