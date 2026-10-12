-- ============================================================
-- 邮件退信记录表增强 - 退信/原始邮件信息分离
-- 核心原则：退信通知邮件与原始邮件信息严格分离
-- 注意：bounce_sender、bounce_subject、bounce_recipient 已在基础脚本中创建，此处跳过
-- 幂等设计：所有 ALTER TABLE ADD COLUMN/ADD INDEX 均检查是否已存在，可重复执行
-- ============================================================

-- 创建临时存储过程：添加列（如果不存在）
DROP PROCEDURE IF EXISTS add_column_if_not_exists;
DELIMITER $$
CREATE PROCEDURE add_column_if_not_exists(
  IN p_table VARCHAR(100),
  IN p_column VARCHAR(100),
  IN p_definition VARCHAR(500)
)
BEGIN
  DECLARE col_exists INT;
  SELECT COUNT(*) INTO col_exists
  FROM INFORMATION_SCHEMA.COLUMNS
  WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = p_table AND COLUMN_NAME = p_column;
  
  IF col_exists = 0 THEN
    SET @sql = CONCAT('ALTER TABLE ', p_table, ' ADD COLUMN ', p_column, ' ', p_definition);
    PREPARE stmt FROM @sql;
    EXECUTE stmt;
    DEALLOCATE PREPARE stmt;
    SELECT CONCAT('Added column ', p_column, ' to ', p_table) AS result;
  ELSE
    SELECT CONCAT('Column ', p_column, ' already exists in ', p_table, ', skipping') AS result;
  END IF;
END$$
DELIMITER ;

-- 创建临时存储过程：添加索引（如果不存在）
DROP PROCEDURE IF EXISTS add_index_if_not_exists;
DELIMITER $$
CREATE PROCEDURE add_index_if_not_exists(
  IN p_table VARCHAR(100),
  IN p_index VARCHAR(100),
  IN p_columns VARCHAR(500)
)
BEGIN
  DECLARE idx_exists INT;
  SELECT COUNT(*) INTO idx_exists
  FROM INFORMATION_SCHEMA.STATISTICS
  WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = p_table AND INDEX_NAME = p_index;
  
  IF idx_exists = 0 THEN
    SET @sql = CONCAT('ALTER TABLE ', p_table, ' ADD INDEX ', p_index, ' (', p_columns, ')');
    PREPARE stmt FROM @sql;
    EXECUTE stmt;
    DEALLOCATE PREPARE stmt;
    SELECT CONCAT('Added index ', p_index, ' to ', p_table) AS result;
  ELSE
    SELECT CONCAT('Index ', p_index, ' already exists on ', p_table, ', skipping') AS result;
  END IF;
END$$
DELIMITER ;

-- 1. 原始邮件信息字段（真正发生投递失败的邮件）
CALL add_column_if_not_exists('mail_bounce_record', 'original_sender', "VARCHAR(255) DEFAULT NULL COMMENT '原始邮件发件人' AFTER original_message_id");
CALL add_column_if_not_exists('mail_bounce_record', 'original_recipient', "VARCHAR(255) DEFAULT NULL COMMENT '原始邮件真正的目标收件人（如 maojiangjun@faxmai.com）' AFTER original_sender");
CALL add_column_if_not_exists('mail_bounce_record', 'original_subject', "VARCHAR(500) DEFAULT NULL COMMENT '原始邮件主题（如 \"论文审稿邀请\"）' AFTER original_recipient");

-- 2. DSN标准字段（RFC 3464）
CALL add_column_if_not_exists('mail_bounce_record', 'dsn_action', "VARCHAR(50) DEFAULT NULL COMMENT 'DSN Action字段（如 failed）' AFTER dsn_diagnostic_code");
CALL add_column_if_not_exists('mail_bounce_record', 'final_recipient', "VARCHAR(255) DEFAULT NULL COMMENT 'DSN Final-Recipient字段（如 rfc822;maojiangjun@faxmai.com）' AFTER dsn_action");
CALL add_column_if_not_exists('mail_bounce_record', 'original_recipient_dsn', "VARCHAR(255) DEFAULT NULL COMMENT 'DSN Original-Recipient字段' AFTER final_recipient");

-- 3. 匹配增强字段
CALL add_column_if_not_exists('mail_bounce_record', 'match_confidence', "INT DEFAULT NULL COMMENT '匹配置信度（0-100），数值越高表示匹配越可靠' AFTER match_strategy");
CALL add_column_if_not_exists('mail_bounce_record', 'recipient_source', "VARCHAR(50) DEFAULT NULL COMMENT '原始收件人信息来源：DSN_FINAL_RECIPIENT/DSN_ORIGINAL_RECIPIENT/HEADER/BODY_FALLBACK' AFTER match_confidence");

-- 4. Header辅助字段
CALL add_column_if_not_exists('mail_bounce_record', 'in_reply_to', "VARCHAR(500) DEFAULT NULL COMMENT 'In-Reply-To头（引用原始邮件Message-ID）' AFTER bounce_reason");
CALL add_column_if_not_exists('mail_bounce_record', 'references_header', "VARCHAR(1000) DEFAULT NULL COMMENT 'References头' AFTER in_reply_to");

-- 5. 新增索引
CALL add_index_if_not_exists('mail_bounce_record', 'idx_original_recipient', 'original_recipient');
CALL add_index_if_not_exists('mail_bounce_record', 'idx_bounce_recipient', 'bounce_recipient');

-- 6. 数据迁移：将旧的 bounced_recipient 复制到 original_recipient（如果 original_recipient 为空）
-- bounced_recipient 字段保留用于向后兼容
UPDATE mail_bounce_record SET original_recipient = bounced_recipient WHERE original_recipient IS NULL AND bounced_recipient IS NOT NULL;

-- 清理临时存储过程
DROP PROCEDURE IF EXISTS add_column_if_not_exists;
DROP PROCEDURE IF EXISTS add_index_if_not_exists;