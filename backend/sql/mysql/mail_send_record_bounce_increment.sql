-- ============================================================
-- 邮件发送记录表 - 退信检测增量脚本
-- 新增 bounce_reason 字段和 idx_message_id 索引
-- 状态流转补充：成功(1) → 退信-投递失败(5)（IMAP定时任务检测到退信后回退）
-- ============================================================

-- 新增退信原因字段
ALTER TABLE mail_send_record ADD COLUMN bounce_reason TEXT COMMENT '退信原因（IMAP检测到退信时记录退信原因）' AFTER error_message;

-- 新增 Message-ID 索引（用于退信检测时按 Message-ID 匹配邮件记录）
ALTER TABLE mail_send_record ADD INDEX idx_message_id (message_id);

-- 更新 send_status 字段注释，补充状态值5
ALTER TABLE mail_send_record MODIFY COLUMN send_status TINYINT NOT NULL DEFAULT 0 COMMENT '发送状态：0-待发送, 1-已提交至邮件服务器, 2-失败, 3-重试中, 4-已取消, 5-退信-投递失败';