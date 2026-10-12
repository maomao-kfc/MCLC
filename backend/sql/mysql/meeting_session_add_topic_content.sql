-- 迁移脚本：meeting_session 表新增 topic_content / topic_content_en 列
-- 用于 DISCUSSION 板块存储讨论主题内容（含子条目，如 "-角膜塑形术" 等）

ALTER TABLE `meeting_session`
  ADD COLUMN `topic_content` text DEFAULT NULL COMMENT '讨论主题内容（中文，含子条目）' AFTER `time_period`,
  ADD COLUMN `topic_content_en` text DEFAULT NULL COMMENT '讨论主题内容（英文）' AFTER `topic_content`;