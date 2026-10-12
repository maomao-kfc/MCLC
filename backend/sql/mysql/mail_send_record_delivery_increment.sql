-- ============================================================
-- 邮件发送记录表 - 投递状态增量脚本
-- 新增 delivery_status 等投递追踪字段
-- 核心理念：send_status 表示SMTP阶段状态，delivery_status 表示最终投递状态
-- 状态流转：SMTP成功(1) → 投递中(DELIVERING) → 已投递(DELIVERED) / 已退信(BOUNCED)
-- ============================================================

-- 1. 新增投递状态字段（SMTP成功后追踪最终投递结果）
ALTER TABLE mail_send_record ADD COLUMN delivery_status TINYINT DEFAULT 0 COMMENT '投递状态：0-未知(等待投递), 1-已投递, 2-已退信, 3-投递中' AFTER send_status;

-- 2. 新增投递成功时间
ALTER TABLE mail_send_record ADD COLUMN delivery_time DATETIME DEFAULT NULL COMMENT '投递成功时间（最终送达收件人邮箱的时间）' AFTER send_time;

-- 3. 新增退信时间
ALTER TABLE mail_send_record ADD COLUMN bounce_time DATETIME DEFAULT NULL COMMENT '退信检测时间（IMAP检测到退信的时间）' AFTER delivery_time;

-- 4. 新增退信代码（标准化退信分类码）
ALTER TABLE mail_send_record ADD COLUMN bounce_code VARCHAR(50) DEFAULT NULL COMMENT '退信代码（如550、5.1.1、DNS_ERROR等标准化分类码）' AFTER bounce_reason;

-- 5. 新增退信邮件Message-ID（退信邮件本身的Message-ID，用于幂等去重）
ALTER TABLE mail_send_record ADD COLUMN bounce_message_id VARCHAR(255) DEFAULT NULL COMMENT '退信邮件的Message-ID（用于幂等去重，同一退信不重复处理）' AFTER bounce_code;

-- 6. 新增IMAP检查次数和最后检查时间
ALTER TABLE mail_send_record ADD COLUMN imap_check_count INT DEFAULT 0 COMMENT 'IMAP退信检测扫描次数' AFTER bounce_message_id;
ALTER TABLE mail_send_record ADD COLUMN last_imap_check_time DATETIME DEFAULT NULL COMMENT '最近一次IMAP退信检测时间' AFTER imap_check_count;

-- 7. 新增最后投递检查状态（记录每次IMAP检查的结果摘要）
ALTER TABLE mail_send_record ADD COLUMN last_delivery_check_status VARCHAR(100) DEFAULT NULL COMMENT '最近一次投递检查状态摘要' AFTER last_imap_check_time;

-- 8. 新增父记录ID（重发时关联原始退信记录，形成追踪链）
ALTER TABLE mail_send_record ADD COLUMN parent_record_id BIGINT DEFAULT NULL COMMENT '父记录ID（重发邮件关联原始退信记录，形成追踪链）' AFTER last_delivery_check_status;

-- 9. 新增索引
ALTER TABLE mail_send_record ADD INDEX idx_delivery_status (delivery_status);
ALTER TABLE mail_send_record ADD INDEX idx_bounce_message_id (bounce_message_id);
ALTER TABLE mail_send_record ADD INDEX idx_parent_record_id (parent_record_id);

-- 10. 更新 send_status 字段注释，补充状态值5
ALTER TABLE mail_send_record MODIFY COLUMN send_status TINYINT NOT NULL DEFAULT 0 COMMENT '发送状态：0-待发送, 1-已提交至邮件服务器, 2-失败, 3-重试中, 4-已取消, 5-退信-投递失败';

-- 11. 更新已有数据：send_status=1且无退信的记录，设置delivery_status=0（等待投递检测）
-- send_status=5的记录，设置delivery_status=2（已退信）
UPDATE mail_send_record SET delivery_status = 2 WHERE send_status = 5;
UPDATE mail_send_record SET delivery_status = 0 WHERE send_status = 1 AND delivery_status IS NULL;