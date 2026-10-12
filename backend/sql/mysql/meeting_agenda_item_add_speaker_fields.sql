-- meeting_agenda_item 添加讲者直接输入字段
-- 目的：支持条目直接输入讲者姓名和单位，不依赖嘉宾表(speaker_id)
-- 执行前提：线上数据库 meeting_agenda_item 表不存在 speaker_name 等列

-- 添加讲者姓名（中文）
ALTER TABLE meeting_agenda_item ADD COLUMN speaker_name varchar(100) DEFAULT NULL COMMENT '讲者姓名（中文）- 直接输入' AFTER speaker_id;

-- 添加讲者姓名（英文）
ALTER TABLE meeting_agenda_item ADD COLUMN speaker_name_en varchar(100) DEFAULT NULL COMMENT '讲者姓名（英文）' AFTER speaker_name;

-- 添加讲者单位（中文）
ALTER TABLE meeting_agenda_item ADD COLUMN speaker_org varchar(200) DEFAULT NULL COMMENT '讲者单位（中文）' AFTER speaker_name_en;

-- 添加讲者单位（英文）
ALTER TABLE meeting_agenda_item ADD COLUMN speaker_org_en varchar(300) DEFAULT NULL COMMENT '讲者单位（英文）' AFTER speaker_org;

-- 回填：将已有 speaker_id 关联的嘉宾信息写入新字段（可选，确保数据完整性）
-- UPDATE meeting_agenda_item i
--   LEFT JOIN meeting_speaker s ON i.speaker_id = s.id
--   SET i.speaker_name = s.name,
--       i.speaker_name_en = s.name_en,
--       i.speaker_org = s.affiliation,
--       i.speaker_org_en = s.affiliation_en
--   WHERE i.speaker_id IS NOT NULL;