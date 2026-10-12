-- ----------------------------
-- Table structure for portal_architecture
-- ----------------------------
DROP TABLE IF EXISTS `portal_architecture`;
CREATE TABLE `portal_architecture` (
    `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键',
    `image_header` varchar(500) DEFAULT NULL COMMENT '图片',
    `name` varchar(100) NOT NULL COMMENT '姓名',
    `job` varchar(100) DEFAULT NULL COMMENT '职位',
    `country_type` varchar(10) DEFAULT 'ZH' COMMENT '国家类型（ZH-中文，EN-英文）',
    `sort` int DEFAULT '0' COMMENT '排序',
    `status` tinyint DEFAULT '1' COMMENT '状态（1-启用，0-禁用）',
    `create_by` bigint DEFAULT NULL COMMENT '创建人ID',
    `update_by` bigint DEFAULT NULL COMMENT '更新人ID',
    `create_time` datetime DEFAULT NULL COMMENT '创建时间',
    `update_time` datetime DEFAULT NULL COMMENT '更新时间',
    `is_deleted` tinyint DEFAULT '0' COMMENT '逻辑删除标识（1-已删除，0-未删除）',
    PRIMARY KEY (`id`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='组织架构表';
