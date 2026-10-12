-- 迁移脚本：meeting_session 表新增 time_period 列
-- 用于存储板块时间段文本（如 "09:00-10:30"），主要用于 DISCUSSION 等无条目板块的时间显示

ALTER TABLE `meeting_session`
  ADD COLUMN `time_period` varchar(50) DEFAULT NULL COMMENT '时间段文本（如 09:00-10:30）' AFTER `panelists_en`;