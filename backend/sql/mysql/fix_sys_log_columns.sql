-- 修复 sys_log 表缺失的列（服务器数据库由旧版脚本初始化，缺少以下两列）
-- 执行方式：在服务器 MySQL 中运行此脚本
USE youlai_admin;

ALTER TABLE `sys_log`
    ADD COLUMN `request_params` text NULL COMMENT '请求参数(批量请求参数可能会超过text)' AFTER `request_method`,
    ADD COLUMN `response_content` mediumtext NULL COMMENT '返回参数' AFTER `request_params`;