-- 论文投稿邮件记录表：添加发送失败错误信息字段
ALTER TABLE paper_submission_email ADD COLUMN send_error_msg VARCHAR(500) DEFAULT NULL COMMENT '发送失败时的错误信息（仅在sendStatus=3时有值）';