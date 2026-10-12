-- ============================================================
-- 迁移脚本：meeting_session 表新增 item_type 列
-- 日期：2026-08-11
-- 目的：板块表新增 item_type 字段，用于标识板块类型枚举
--       （OPENING/KEYNOTE/INVITED_SPEECH/DISCUSSION/BREAK/LUNCH/CLOSING/OTHER）
-- 原因：Java实体 MeetingSession 已有 itemType 字段映射 item_type 列，
--       但线上数据库缺少该列，导致 SQLSyntaxErrorException
-- ============================================================

ALTER TABLE `meeting_session`
  ADD COLUMN `item_type` varchar(30) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL
  COMMENT '板块类型枚举：OPENING/KEYNOTE/INVITED_SPEECH/DISCUSSION/BREAK/LUNCH/CLOSING/OTHER'
  AFTER `start_time`;

-- 验证
-- SELECT column_name, data_type, column_comment FROM information_schema.columns
-- WHERE table_name = 'meeting_session' AND column_name = 'item_type';