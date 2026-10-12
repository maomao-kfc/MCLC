-- 会议设置表新增会议日程名称、描述、是否展示日程字段
ALTER TABLE `portal_meeting_setting`
    ADD COLUMN `schedule_title` varchar(255) DEFAULT NULL COMMENT '会议日程名称' AFTER `hqap_image`,
    ADD COLUMN `schedule_description` varchar(500) DEFAULT NULL COMMENT '会议日程描述' AFTER `schedule_title`,
    ADD COLUMN `show_schedule` tinyint DEFAULT '1' COMMENT '是否展示日程(1-是 0-否)' AFTER `schedule_description`;
