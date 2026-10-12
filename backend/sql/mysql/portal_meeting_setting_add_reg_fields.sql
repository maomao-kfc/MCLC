-- 将 deadline、check_time、paper_submission_deadline 替换为带开始/结束的时间段字段
-- 新增: general_reg_start, general_reg_end, early_bird_reg_start, early_bird_reg_end,
--       conference_date_start, conference_date_end
-- 幂等脚本：已存在的列会自动跳过，可重复执行

DROP PROCEDURE IF EXISTS portal_meeting_setting_add_col;

DELIMITER //
CREATE PROCEDURE portal_meeting_setting_add_col()
BEGIN
  IF NOT EXISTS (SELECT * FROM INFORMATION_SCHEMA.COLUMNS WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'portal_meeting_setting' AND COLUMN_NAME = 'general_reg_start') THEN
    ALTER TABLE portal_meeting_setting ADD COLUMN general_reg_start DATETIME NULL COMMENT '常规注册开始时间' AFTER deadline;
  END IF;

  IF NOT EXISTS (SELECT * FROM INFORMATION_SCHEMA.COLUMNS WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'portal_meeting_setting' AND COLUMN_NAME = 'general_reg_end') THEN
    ALTER TABLE portal_meeting_setting ADD COLUMN general_reg_end DATETIME NULL COMMENT '常规注册结束时间' AFTER general_reg_start;
  END IF;

  IF NOT EXISTS (SELECT * FROM INFORMATION_SCHEMA.COLUMNS WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'portal_meeting_setting' AND COLUMN_NAME = 'early_bird_reg_start') THEN
    ALTER TABLE portal_meeting_setting ADD COLUMN early_bird_reg_start DATETIME NULL COMMENT '早鸟优惠注册 & 学生注册开始时间' AFTER paper_submission_deadline;
  END IF;

  IF NOT EXISTS (SELECT * FROM INFORMATION_SCHEMA.COLUMNS WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'portal_meeting_setting' AND COLUMN_NAME = 'early_bird_reg_end') THEN
    ALTER TABLE portal_meeting_setting ADD COLUMN early_bird_reg_end DATETIME NULL COMMENT '早鸟优惠注册 & 学生注册结束时间' AFTER early_bird_reg_start;
  END IF;

  IF NOT EXISTS (SELECT * FROM INFORMATION_SCHEMA.COLUMNS WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'portal_meeting_setting' AND COLUMN_NAME = 'conference_date_start') THEN
    ALTER TABLE portal_meeting_setting ADD COLUMN conference_date_start DATETIME NULL COMMENT '会议举办开始时间' AFTER check_time;
  END IF;

  IF NOT EXISTS (SELECT * FROM INFORMATION_SCHEMA.COLUMNS WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'portal_meeting_setting' AND COLUMN_NAME = 'conference_date_end') THEN
    ALTER TABLE portal_meeting_setting ADD COLUMN conference_date_end DATETIME NULL COMMENT '会议举办结束时间' AFTER conference_date_start;
  END IF;
END //
DELIMITER ;

CALL portal_meeting_setting_add_col();

DROP PROCEDURE IF EXISTS portal_meeting_setting_add_col;
