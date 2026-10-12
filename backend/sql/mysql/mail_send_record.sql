-- ============================================================
-- 邮件发送记录表（任务表设计）
-- 核心理念：日志永远不覆盖，永远新增；同时承担发送队列职责
-- 状态流转：待发送(0) → 成功(1) / 失败(2) → 重试中(3) → 成功(1) / 失败(2)
-- ============================================================
CREATE TABLE IF NOT EXISTS mail_send_record
(
    id              BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键',
    business_type   VARCHAR(50)  NOT NULL COMMENT '业务类型：PAPER_SUBMIT-审稿邮件, PAPER_PASS-审核通过, PAPER_REJECT-审核拒绝, USER_REGISTER-注册成功, RESET_PASSWORD-密码重置, MEETING_NOTICE-会议通知',
    business_id     BIGINT       NOT NULL COMMENT '业务ID（如paper_submission.id）',
    receiver        VARCHAR(255) NOT NULL COMMENT '收件邮箱',
    receiver_name   VARCHAR(100)          COMMENT '收件人姓名',
    subject         VARCHAR(500)          COMMENT '邮件标题',
    content         LONGTEXT              COMMENT '邮件正文（保留完整内容，便于追溯）',
    send_status     TINYINT      NOT NULL DEFAULT 0 COMMENT '发送状态：0-待发送, 1-成功, 2-失败, 3-重试中, 4-已取消',
    retry_count     INT                   DEFAULT 0 COMMENT '重试次数',
    error_message   TEXT                  COMMENT '错误信息（SMTP异常详情）',
    message_id      VARCHAR(255)          COMMENT 'SMTP返回的Message-ID（用于查重）',
    consume_time    BIGINT                COMMENT '发送耗时（毫秒）',
    sender          VARCHAR(100)          COMMENT '发送账号（如notice@mclc.com）',
    send_time       DATETIME              COMMENT '实际发送成功时间',
    create_time     DATETIME              DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time     DATETIME              DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    INDEX idx_business (business_type, business_id),
    INDEX idx_receiver (receiver),
    INDEX idx_status (send_status),
    INDEX idx_create_time (create_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='邮件发送记录';