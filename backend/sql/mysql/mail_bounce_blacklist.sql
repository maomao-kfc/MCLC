-- ============================================================
-- 邮件退信黑名单表
-- 记录永久无效的收件地址，发送前校验，避免无效重发浪费资源
-- ============================================================

CREATE TABLE IF NOT EXISTS `mail_bounce_blacklist` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `email` VARCHAR(255) NOT NULL COMMENT '黑名单邮箱地址',
  `reason` VARCHAR(1000) DEFAULT NULL COMMENT '加入黑名单原因（退信原因摘要）',
  `source` VARCHAR(50) NOT NULL DEFAULT 'AUTO' COMMENT '来源：AUTO-自动检测, MANUAL-手动添加',
  `bounce_count` INT NOT NULL DEFAULT 1 COMMENT '累计退信次数',
  `last_bounce_time` DATETIME DEFAULT NULL COMMENT '最近一次退信时间',
  `related_record_id` BIGINT DEFAULT NULL COMMENT '关联的邮件发送记录ID',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_email` (`email`),
  KEY `idx_create_time` (`create_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='邮件退信黑名单';

-- ============================================================
-- 邮件退信黑名单菜单（添加到"邮件中心"下）
-- 注意：parent_id 需要根据实际环境中"邮件中心"菜单的ID进行调整
-- 可通过以下SQL查询邮件中心菜单ID：
--   SELECT id FROM sys_menu WHERE name = '邮件中心' OR route_path = 'mailRecord';
-- ============================================================
INSERT INTO `sys_menu` (`id`, `parent_id`, `tree_path`, `name`, `type`, `route_name`, `route_path`, `component`, `perm`, `always_show`, `keep_alive`, `visible`, `sort`, `icon`, `redirect`, `create_time`, `update_time`, `params`)
SELECT NULL, p.id, CONCAT('0,', p.id), '退信黑名单', 'M', 'MailBlacklist', 'blacklist', 'mailRecord/blacklist', NULL, NULL, 1, 1, 2, 'shield', NULL, NOW(), NOW(), NULL
FROM (SELECT id FROM sys_menu WHERE route_path = 'mailRecord' LIMIT 1) p;

-- ============================================================
-- 测试数据（仅用于开发/测试环境，生产环境请勿执行）
-- ============================================================
INSERT INTO `mail_bounce_blacklist` (`email`, `reason`, `source`, `bounce_count`, `last_bounce_time`, `related_record_id`, `create_time`, `update_time`) VALUES
('nonexistent_user163@163.com', '550 User not found - 用户不存在，邮件无法投递', 'AUTO', 3, '2026-07-28 10:15:00', NULL, '2026-07-28 10:15:00', '2026-07-30 09:20:00'),
('invalid_recipient@qq.com', '550 5.1.1 recipient rejected - 收件人被拒绝', 'AUTO', 2, '2026-07-29 14:30:00', NULL, '2026-07-29 14:30:00', '2026-07-30 08:45:00'),
('expired_mailbox@sina.com', '550 5.4.4 host not found - 邮箱域名无法解析', 'AUTO', 1, '2026-07-30 16:00:00', NULL, '2026-07-30 16:00:00', '2026-07-30 16:00:00'),
('test_blacklist@126.com', '[手动添加] 该邮箱多次退信，手动加入黑名单', 'MANUAL', 0, NULL, NULL, '2026-08-01 09:00:00', '2026-08-01 09:00:00'),
('spam_target@hotmail.com', '554 delivery error - 投递错误，目标服务器拒绝', 'AUTO', 2, '2026-08-02 11:30:00', NULL, '2026-08-02 11:30:00', '2026-08-02 11:30:00');