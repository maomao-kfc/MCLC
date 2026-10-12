-- 修复会前安排模块的角色权限
-- ADMIN角色(role_id=2)缺少会前安排的按钮权限(menu_id 2831-2834)
-- 执行时间: 2026-08-20

-- 插入ADMIN角色的会前安排按钮权限（忽略已存在的记录）
INSERT IGNORE INTO `sys_role_menu` (`role_id`, `menu_id`) VALUES (2, 2831);
INSERT IGNORE INTO `sys_role_menu` (`role_id`, `menu_id`) VALUES (2, 2832);
INSERT IGNORE INTO `sys_role_menu` (`role_id`, `menu_id`) VALUES (2, 2833);
INSERT IGNORE INTO `sys_role_menu` (`role_id`, `menu_id`) VALUES (2, 2834);

-- 权限说明：
-- 2831: sys:arrange:list  (会前安排查询)
-- 2832: sys:arrange:create (会前安排新增)
-- 2833: sys:arrange:update (会前安排编辑)
-- 2834: sys:arrange:delete (会前安排删除)