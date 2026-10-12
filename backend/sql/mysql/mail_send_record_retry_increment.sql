-- ============================================================
-- 邮件发送记录表 - 退信重试机制增量脚本
-- 新增 record_type / retry_sequence / root_record_id 字段
-- 核心理念：原始邮件与重试邮件通过 root_record_id 扁平关联（非递归）
--           record_type 区分原始/重试，retry_sequence 标识重试序号
--           唯一约束 (root_record_id, retry_sequence) 防止重复重试
-- ============================================================

-- 1. 新增记录类型字段：ORIGINAL=原始发送, RETRY=重试发送
ALTER TABLE mail_send_record ADD COLUMN record_type VARCHAR(20) DEFAULT 'ORIGINAL'
    COMMENT '记录类型：ORIGINAL-原始发送, RETRY-重试发送' AFTER parent_record_id;

-- 2. 新增重试序号字段：1=第一次重试, 2=第二次重试, NULL=原始记录
ALTER TABLE mail_send_record ADD COLUMN retry_sequence INT DEFAULT NULL
    COMMENT '重试序号：1=第一次重试, 2=第二次重试, NULL=原始记录' AFTER record_type;

-- 3. 新增根记录ID字段：始终指向原始邮件记录ID（重试记录必填，原始记录等于自身ID）
ALTER TABLE mail_send_record ADD COLUMN root_record_id BIGINT DEFAULT NULL
    COMMENT '根记录ID（始终指向原始邮件记录，重试记录必填）' AFTER retry_sequence;

-- 4. 添加唯一约束：(root_record_id, retry_sequence) 防止同一原始邮件重复创建相同序号的重试
ALTER TABLE mail_send_record ADD UNIQUE INDEX uk_root_retry (root_record_id, retry_sequence);

-- 5. 添加索引：加速按根记录查询重试记录、按记录类型筛选
ALTER TABLE mail_send_record ADD INDEX idx_root_record_id (root_record_id);
ALTER TABLE mail_send_record ADD INDEX idx_record_type (record_type);

-- 6. 回填现有数据：已有 parent_record_id 的记录标记为 RETRY
UPDATE mail_send_record SET record_type = 'RETRY' WHERE parent_record_id IS NOT NULL;

-- 7. 回填现有数据：原始记录的 root_record_id 指向自身ID
UPDATE mail_send_record SET root_record_id = id WHERE parent_record_id IS NULL;

-- 8. 回填现有数据：已有重试记录的 root_record_id 指向 parent_record_id
UPDATE mail_send_record SET root_record_id = parent_record_id WHERE parent_record_id IS NOT NULL;

-- 9. 更新 send_status 字段注释，补充状态值6/7/8
ALTER TABLE mail_send_record MODIFY COLUMN send_status TINYINT NOT NULL DEFAULT 0
    COMMENT '发送状态：0-待发送, 1-已提交至邮件服务器, 2-失败, 3-重试中, 4-已取消, 5-退信-投递失败, 6-待重试, 7-重试成功, 8-重试失败';