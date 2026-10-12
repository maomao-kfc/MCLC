-- portal_arrange 表新增 arrange_type 字段
-- 用于区分"标题描述"类型和"板块"类型的会前安排记录
-- 执行时间: 2026-08-20

-- 1. 新增 arrange_type 字段
ALTER TABLE `portal_arrange`
  ADD COLUMN `arrange_type` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL DEFAULT 'BLOCK' COMMENT '安排类型(HEADER-标题描述 BLOCK-板块)' AFTER `country_type`;

-- 2. 将现有的板块记录（id=10-13 中文板块, id=19-22 英文板块）标记为 BLOCK 类型
-- 这些记录的 arrange_type 默认值已是 BLOCK，无需额外更新

-- 3. 插入 HEADER 类型的默认记录（中文版）
INSERT INTO `portal_arrange` (`arrange_content`, `arrange_room`, `sort`, `status`, `create_by`, `create_time`, `update_by`, `update_time`, `is_deleted`, `country_type`, `arrange_type`)
VALUES ('详细议程于11月中旬公布', '会议日程', 0, 1, NULL, NOW(), NULL, NOW(), 0, 'ZH', 'HEADER');

-- 4. 插入 HEADER 类型的默认记录（英文版）
INSERT INTO `portal_arrange` (`arrange_content`, `arrange_room`, `sort`, `status`, `create_by`, `create_time`, `update_by`, `update_time`, `is_deleted`, `country_type`, `arrange_type`)
VALUES ('Detailed agenda will be announced in mid-November', 'Conference Schedule', 0, 1, NULL, NOW(), NULL, NOW(), 0, 'EN', 'HEADER');