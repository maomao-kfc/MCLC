-- ----------------------------
-- Table structure for sys_email_template
-- ----------------------------
DROP TABLE IF EXISTS `sys_email_template`;
CREATE TABLE `sys_email_template` (
    `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键',
    `email_title` varchar(255) NOT NULL COMMENT '邮箱标题',
    `email_type` varchar(50) DEFAULT NULL COMMENT '邮箱类型',
    `email_content` longtext COMMENT '邮箱内容（富文本编辑器JSON）',
    `sort` int DEFAULT '0' COMMENT '排序',
    `status` tinyint DEFAULT '1' COMMENT '状态（1-启用，0-禁用）',
    `remark` varchar(255) COMMENT '备注',
    `create_time` datetime COMMENT '创建时间',
    `update_time` datetime COMMENT '更新时间',
    `is_deleted` tinyint DEFAULT '0' COMMENT '逻辑删除标识（1-已删除，0-未删除）',
    PRIMARY KEY (`id`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='邮箱模板表';
