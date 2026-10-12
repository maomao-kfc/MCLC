-- ============================================================
-- 会议日程表 - 双语字段增量脚本（幂等版本，可重复执行）
-- 为 meeting_info / meeting_session / meeting_agenda_item / meeting_speaker
-- 添加英文字段，实现中英文一体化存储
-- 核心理念：同一数据实体同时包含中英文两套字段，消除 country_type 双记录模式
-- 执行方式：mysql -uroot -p meeting_admin < meeting_schedule_bilingual_fields.sql
-- ============================================================

-- 设置连接字符集，避免存储过程中中文COMMENT报编码错误
SET NAMES utf8mb4;
SET CHARACTER_SET_CONNECTION = utf8mb4;

-- ============================
-- 0. 创建辅助存储过程：安全添加列
-- ============================
DROP PROCEDURE IF EXISTS `add_column_if_not_exists`;
DELIMITER //
CREATE PROCEDURE `add_column_if_not_exists`(
  IN p_table_name VARCHAR(100),
  IN p_column_name VARCHAR(100),
  IN p_column_definition VARCHAR(500)
)
BEGIN
  DECLARE col_exists INT;
  SELECT COUNT(*) INTO col_exists
  FROM INFORMATION_SCHEMA.COLUMNS
  WHERE TABLE_SCHEMA = DATABASE()
    AND TABLE_NAME = p_table_name
    AND COLUMN_NAME = p_column_name;

  IF col_exists = 0 THEN
    SET @sql = CONCAT('ALTER TABLE `', p_table_name, '` ADD COLUMN `', p_column_name, '` ', p_column_definition);
    PREPARE stmt FROM @sql;
    EXECUTE stmt;
    DEALLOCATE PREPARE stmt;
  END IF;
END //
DELIMITER ;

-- ============================
-- 1. meeting_info 添加英文字段
-- ============================
CALL `add_column_if_not_exists`('meeting_info', 'name_en', "varchar(200) DEFAULT NULL COMMENT '会议名称（英文）' AFTER `name`");
CALL `add_column_if_not_exists`('meeting_info', 'venue_en', "varchar(200) DEFAULT NULL COMMENT '地点（英文）' AFTER `venue`");

-- ============================
-- 2. meeting_session 添加英文字段 + itemType枚举字段
-- ============================
CALL `add_column_if_not_exists`('meeting_session', 'title_en', "varchar(200) DEFAULT NULL COMMENT '板块标题（英文）' AFTER `title`");
CALL `add_column_if_not_exists`('meeting_session', 'chairperson_en', "varchar(300) DEFAULT NULL COMMENT '主持人（英文），多个逗号分隔' AFTER `chairperson`");
CALL `add_column_if_not_exists`('meeting_session', 'panelists_en', "varchar(800) DEFAULT NULL COMMENT '讨论嘉宾（英文），多个逗号分隔' AFTER `panelists`");
CALL `add_column_if_not_exists`('meeting_session', 'remark_en', "varchar(100) DEFAULT NULL COMMENT '备注（英文）' AFTER `remark`");
CALL `add_column_if_not_exists`('meeting_session', 'item_type', "varchar(30) DEFAULT NULL COMMENT '板块类型枚举：DISCUSSION/OTHER等' AFTER `start_time`");

-- ============================
-- 3. meeting_agenda_item 添加英文字段 + itemType枚举字段
-- ============================
CALL `add_column_if_not_exists`('meeting_agenda_item', 'type_en', "varchar(50) DEFAULT NULL COMMENT '类型（英文）' AFTER `type`");
CALL `add_column_if_not_exists`('meeting_agenda_item', 'topic_en', "varchar(800) DEFAULT NULL COMMENT '题目（英文）' AFTER `topic`");
CALL `add_column_if_not_exists`('meeting_agenda_item', 'item_type', "varchar(30) DEFAULT NULL COMMENT '日程类型枚举：OPENING/KEYNOTE/INVITED_SPEECH/DISCUSSION/BREAK/CLOSING/OTHER' AFTER `topic_en`");

-- 添加item_type索引（如果不存在）
SET @index_exists = (SELECT COUNT(*) FROM INFORMATION_SCHEMA.STATISTICS
  WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'meeting_agenda_item' AND INDEX_NAME = 'idx_item_type');
SET @sql = IF(@index_exists = 0,
  'ALTER TABLE `meeting_agenda_item` ADD INDEX `idx_item_type` (`item_type`)',
  'SELECT "Index idx_item_type already exists"');
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- ============================
-- 4. meeting_speaker 添加英文字段
-- ============================
CALL `add_column_if_not_exists`('meeting_speaker', 'name_en', "varchar(100) DEFAULT NULL COMMENT '嘉宾姓名（英文）' AFTER `name`");
CALL `add_column_if_not_exists`('meeting_speaker', 'title_en', "varchar(100) DEFAULT NULL COMMENT '职称/级别（英文）' AFTER `title`");
CALL `add_column_if_not_exists`('meeting_speaker', 'affiliation_en', "varchar(300) DEFAULT NULL COMMENT '所属单位（英文）' AFTER `affiliation`");
CALL `add_column_if_not_exists`('meeting_speaker', 'brief_intro_en', "varchar(600) DEFAULT NULL COMMENT '嘉宾简介（英文）' AFTER `brief_intro`");
CALL `add_column_if_not_exists`('meeting_speaker', 'detail_intro_en', "text DEFAULT NULL COMMENT '嘉宾详情介绍（英文）' AFTER `detail_intro`");
CALL `add_column_if_not_exists`('meeting_speaker', 'research_fields_en', "varchar(600) DEFAULT NULL COMMENT '研究领域（英文），逗号分隔' AFTER `research_fields`");

-- ============================
-- 5. meeting_session_participant 新建表（Panel Discussion多参与人）
-- ============================
-- DROP TABLE IF EXISTS 已保证幂等
DROP TABLE IF EXISTS `meeting_session_participant`;
CREATE TABLE `meeting_session_participant` (
    `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `session_id` bigint NOT NULL COMMENT '关联板块ID',
    `speaker_id` bigint NULL DEFAULT NULL COMMENT '关联嘉宾ID（可为空，表示未录入嘉宾库的临时参与人）',
    `name` varchar(100) DEFAULT NULL COMMENT '参与人姓名（中文）',
    `name_en` varchar(100) DEFAULT NULL COMMENT '参与人姓名（英文）',
    `role` varchar(50) DEFAULT NULL COMMENT '参与角色：MODERATOR/PANELIST/OTHER',
    `sort_order` int NOT NULL DEFAULT 0 COMMENT '显示顺序',
    `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`) USING BTREE,
    INDEX `idx_session_id` (`session_id`) USING BTREE,
    INDEX `idx_speaker_id` (`speaker_id`) USING BTREE,
    CONSTRAINT `fk_participant_session` FOREIGN KEY (`session_id`) REFERENCES `meeting_session` (`id`) ON DELETE CASCADE ON UPDATE RESTRICT
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='会议板块参与人表（Panel Discussion等）' ROW_FORMAT=DYNAMIC;

-- ============================
-- 6. 回填item_type枚举值（基于现有type字段推断）
-- ============================
UPDATE `meeting_agenda_item` SET `item_type` = 'OPENING' WHERE `type` = '开幕式';
UPDATE `meeting_agenda_item` SET `item_type` = 'KEYNOTE' WHERE `type` = '主旨报告';
UPDATE `meeting_agenda_item` SET `item_type` = 'INVITED_SPEECH' WHERE `type` IN ('专题演讲', '大会发言');
UPDATE `meeting_agenda_item` SET `item_type` = 'BREAK' WHERE `is_break` = 1;
UPDATE `meeting_agenda_item` SET `item_type` = 'OTHER' WHERE `item_type` IS NULL;

-- ============================
-- 7. 回填英文字段（从旧表portal_meeting_period迁移，仅迁移有英文数据的记录）
-- 注意：此步骤需在生产环境执行前确认旧表数据完整性
-- ============================

-- 7.1 回填 meeting_info 英文名称和地点
-- 从 portal_meeting_date 中 country_type='EN' 的记录合并到 meeting_info
-- 注意：portal_meeting_date 的地点列名是 meet_address（下划线命名）
UPDATE `meeting_info` mi
    INNER JOIN `portal_meeting_date` pmd_zh ON pmd_zh.country_type = 'ZH' AND pmd_zh.is_deleted = 0
    INNER JOIN `portal_meeting_date` pmd_en ON pmd_en.country_type = 'EN' AND pmd_en.is_deleted = 0
        AND DATE(pmd_en.meet_time) = DATE(pmd_zh.meet_time)
    SET mi.name_en = pmd_en.meet_name,
        mi.venue_en = pmd_en.meet_address
    WHERE DATE(pmd_zh.meet_time) = mi.meeting_date;

-- 7.2 回填 meeting_session 英文标题和主持人
-- 从 portal_meeting_period 中 country_type='EN' 的记录，按 table_id 和 sort 分组匹配
UPDATE `meeting_session` ms
    INNER JOIN `meeting_info` mi ON ms.meeting_id = mi.id
    INNER JOIN `portal_meeting_date` pmd ON pmd.country_type = 'ZH' AND pmd.is_deleted = 0
        AND DATE(pmd.meet_time) = mi.meeting_date
    INNER JOIN `portal_meeting_period` pmp_zh ON pmp_zh.table_id = pmd.meet_id
        AND pmp_zh.time_period_sort = ms.sort_order
    INNER JOIN `portal_meeting_date` pmd_en ON pmd_en.country_type = 'EN' AND pmd_en.is_deleted = 0
        AND DATE(pmd_en.meet_time) = mi.meeting_date
    INNER JOIN `portal_meeting_period` pmp_en ON pmp_en.table_id = pmd_en.meet_id
        AND pmp_en.time_period_sort = ms.sort_order
    SET ms.title_en = pmp_en.time_period_name,
        ms.chairperson_en = pmp_en.time_period_person;

-- 7.3 回填 meeting_agenda_item 英文类型和题目
-- 通过 sort_order 和 session 关联匹配
UPDATE `meeting_agenda_item` mai
    INNER JOIN `meeting_session` ms ON mai.session_id = ms.id
    INNER JOIN `meeting_info` mi ON ms.meeting_id = mi.id
    INNER JOIN `portal_meeting_date` pmd_zh ON pmd_zh.country_type = 'ZH' AND pmd_zh.is_deleted = 0
        AND DATE(pmd_zh.meet_time) = mi.meeting_date
    INNER JOIN `portal_meeting_period` pmp_zh ON pmp_zh.table_id = pmd_zh.meet_id
        AND pmp_zh.time_period_sort = ms.sort_order
    INNER JOIN `portal_meeting_date` pmd_en ON pmd_en.country_type = 'EN' AND pmd_en.is_deleted = 0
        AND DATE(pmd_en.meet_time) = mi.meeting_date
    INNER JOIN `portal_meeting_period` pmp_en ON pmp_en.table_id = pmd_en.meet_id
        AND pmp_en.time_period_sort = ms.sort_order
    SET mai.type_en = pmp_en.time_period_type,
        mai.topic_en = pmp_en.time_period_question
    WHERE mai.sort_order = 1;

-- ============================
-- 8. 数据校验SQL（执行后人工检查）
-- ============================
-- SELECT 'meeting_info双语校验' AS check_name, COUNT(*) AS total,
--     SUM(CASE WHEN name_en IS NOT NULL THEN 1 ELSE 0 END) AS has_en
--     FROM meeting_info;
-- SELECT 'meeting_session双语校验' AS check_name, COUNT(*) AS total,
--     SUM(CASE WHEN title_en IS NOT NULL THEN 1 ELSE 0 END) AS has_en
--     FROM meeting_session;
-- SELECT 'meeting_agenda_item双语校验' AS check_name, COUNT(*) AS total,
--     SUM(CASE WHEN topic_en IS NOT NULL THEN 1 ELSE 0 END) AS has_en
--     FROM meeting_agenda_item;

-- ============================
-- 9. 回滚SQL（如需回滚，逐条执行）
-- ============================
-- ALTER TABLE `meeting_info` DROP COLUMN `name_en`, DROP COLUMN `venue_en`;
-- ALTER TABLE `meeting_session` DROP COLUMN `title_en`, DROP COLUMN `chairperson_en`, DROP COLUMN `panelists_en`, DROP COLUMN `remark_en`, DROP COLUMN `item_type`;
-- ALTER TABLE `meeting_agenda_item` DROP COLUMN `type_en`, DROP COLUMN `topic_en`, DROP COLUMN `item_type`;
-- ALTER TABLE `meeting_speaker` DROP COLUMN `name_en`, DROP COLUMN `title_en`, DROP COLUMN `affiliation_en`, DROP COLUMN `brief_intro_en`, DROP COLUMN `detail_intro_en`, DROP COLUMN `research_fields_en`;
-- DROP TABLE IF EXISTS `meeting_session_participant`;

-- ============================
-- 10. 清理辅助存储过程
-- ============================
DROP PROCEDURE IF EXISTS `add_column_if_not_exists`;