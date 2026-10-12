-- 会议设置表新增是否开启日程字段
ALTER TABLE `portal_meeting_setting`
    ADD COLUMN `schedule_enabled` tinyint DEFAULT '1' COMMENT '是否开启日程(1-是 0-否)' AFTER `registration_enabled`;
