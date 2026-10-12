-- 会议设置表新增加论文投稿截至日期字段
ALTER TABLE `portal_meeting_setting`
    ADD COLUMN `paper_submission_deadline` datetime DEFAULT NULL COMMENT '论文投稿截至日期' AFTER `check_time`;
