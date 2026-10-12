-- ============================================================
-- 邮件退信记录表
-- 独立记录每封退信邮件的完整信息，与 mail_send_record 通过原始 Message-ID 关联
-- 设计要点：
-- 1. bounce_message_id 唯一索引保证幂等（同一退信不重复处理）
-- 2. 记录退信的完整解析信息（类型、永久/临时、原因、收件人、DSN等）
-- 3. 记录匹配结果（匹配到的发送记录ID、匹配策略）
-- ============================================================

CREATE TABLE IF NOT EXISTS `mail_bounce_record` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `bounce_message_id` VARCHAR(255) NOT NULL COMMENT '退信邮件的Message-ID（唯一标识一封退信，幂等去重）',
    `original_message_id` VARCHAR(255) DEFAULT NULL COMMENT '原始发送邮件的Message-ID（退信引用的原始邮件ID）',
    `matched_record_id` BIGINT DEFAULT NULL COMMENT '匹配到的mail_send_record记录ID（NULL表示未匹配）',
    `match_strategy` VARCHAR(50) DEFAULT NULL COMMENT '匹配策略：MESSAGE_ID/IN_REPLY_TO/REFERENCES/RECEIVER_TIME_WINDOW/UNMATCHED',
    `bounce_type` VARCHAR(30) NOT NULL COMMENT '退信类型：DNS_ERROR/USER_NOT_FOUND/MAILBOX_UNAVAILABLE/SPAM_REJECTED/MAILBOX_FULL/RELAY_DENIED/OTHER',
    `bounce_permanent` TINYINT(1) NOT NULL DEFAULT 1 COMMENT '是否永久性退信：1-永久退信（禁止重发），0-临时退信（可重发）',
    `bounce_code` VARCHAR(50) DEFAULT NULL COMMENT '退信代码（如550、5.1.1、DNS_ERROR等标准化分类码）',
    `bounce_reason` TEXT COMMENT '退信原因（从退信邮件中提取的完整原因文本）',
    `bounced_recipient` VARCHAR(255) DEFAULT NULL COMMENT '退信收件人地址',
    `dsn_status` VARCHAR(100) DEFAULT NULL COMMENT 'DSN状态码（如5.1.1、5.4.4等，来自delivery-status部分）',
    `dsn_diagnostic_code` VARCHAR(500) DEFAULT NULL COMMENT 'DSN诊断码（来自delivery-status部分的Diagnostic-Code字段）',
    `bounce_subject` VARCHAR(500) DEFAULT NULL COMMENT '退信邮件主题',
    `bounce_sender` VARCHAR(255) DEFAULT NULL COMMENT '退信邮件发送者（通常为mailer-daemon）',
    `bounce_date` DATETIME DEFAULT NULL COMMENT '退信邮件日期',
    `processed` TINYINT(1) NOT NULL DEFAULT 0 COMMENT '是否已处理：0-未处理，1-已处理',
    `process_time` DATETIME DEFAULT NULL COMMENT '处理时间',
    `process_result` VARCHAR(200) DEFAULT NULL COMMENT '处理结果摘要',
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_bounce_message_id` (`bounce_message_id`),
    KEY `idx_original_message_id` (`original_message_id`),
    KEY `idx_matched_record_id` (`matched_record_id`),
    KEY `idx_bounce_type` (`bounce_type`),
    KEY `idx_bounced_recipient` (`bounced_recipient`),
    KEY `idx_processed` (`processed`),
    KEY `idx_create_time` (`create_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='邮件退信记录';