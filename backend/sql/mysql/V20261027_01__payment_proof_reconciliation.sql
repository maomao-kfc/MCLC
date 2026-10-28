-- ============================================================
-- 支付凭证上传 + 人工对账 功能数据库变更
-- 日期: 2026-10-27
-- 说明: 外部协会网站支付场景下，参会人员上传支付凭证截图，
--       管理端人工审核 + 协会对账Excel批量匹配确认到账
-- ============================================================

-- -----------------------------------------------------------
-- 1. register_user_information 新增凭证相关字段
-- -----------------------------------------------------------
ALTER TABLE register_user_information
    ADD COLUMN proof_status VARCHAR(32) DEFAULT NULL COMMENT '凭证状态: null=未提交, proof_submitted=待核验, review_passed=待对账确认, rejected=已驳回, paid=已支付(对账确认), offline_paid=线下登记',
    ADD COLUMN proof_submit_time DATETIME DEFAULT NULL COMMENT '最近一次凭证提交时间',
    ADD COLUMN proof_review_time DATETIME DEFAULT NULL COMMENT '凭证审核时间',
    ADD COLUMN proof_reviewer_id BIGINT DEFAULT NULL COMMENT '凭证审核人用户ID',
    ADD COLUMN proof_reject_reason VARCHAR(500) DEFAULT NULL COMMENT '凭证驳回原因',
    ADD COLUMN declared_pay_time DATETIME DEFAULT NULL COMMENT '用户声明的支付时间(提交凭证时填写)';

-- -----------------------------------------------------------
-- 2. 支付凭证记录表(支持多次重传, 每次上传一条记录)
-- -----------------------------------------------------------
CREATE TABLE IF NOT EXISTS payment_proof (
    id BIGINT NOT NULL COMMENT '主键(ASSIGN_ID)',
    registration_id BIGINT NOT NULL COMMENT '注册用户ID(register_user_information.id)',
    user_id BIGINT DEFAULT NULL COMMENT '提交人系统用户ID(sys_user.id, 可为空)',
    file_url VARCHAR(500) NOT NULL COMMENT '带水印的凭证文件URL(后端加水印后保存)',
    file_md5 VARCHAR(64) NOT NULL COMMENT '文件MD5哈希(精确去重)',
    file_phash VARCHAR(32) DEFAULT NULL COMMENT '感知哈希pHash(防裁剪/压缩重复)',
    declared_pay_time DATETIME DEFAULT NULL COMMENT '用户声明的支付时间',
    declared_amount DECIMAL(10,2) DEFAULT NULL COMMENT '用户声明的支付金额',
    declared_trade_no VARCHAR(128) DEFAULT NULL COMMENT '用户声明的交易号(选填)',
    remark VARCHAR(500) DEFAULT NULL COMMENT '用户备注',
    ocr_amount VARCHAR(64) DEFAULT NULL COMMENT 'OCR识别-支付金额',
    ocr_payee VARCHAR(128) DEFAULT NULL COMMENT 'OCR识别-收款方名称',
    ocr_pay_time VARCHAR(64) DEFAULT NULL COMMENT 'OCR识别-支付时间',
    ocr_trade_no VARCHAR(128) DEFAULT NULL COMMENT 'OCR识别-交易号',
    ocr_merchant_no VARCHAR(128) DEFAULT NULL COMMENT 'OCR识别-商户单号',
    ocr_raw_text TEXT COMMENT 'OCR识别原始文本',
    ocr_status TINYINT DEFAULT 0 COMMENT 'OCR状态: 0=未识别, 1=已识别, 2=识别失败',
    amount_match TINYINT DEFAULT NULL COMMENT '金额比对结果: 0=不一致, 1=一致, NULL=未比对',
    status VARCHAR(32) NOT NULL DEFAULT 'proof_submitted' COMMENT '凭证状态: proof_submitted=待核验, review_passed=初审通过, rejected=已驳回, reconciled=对账确认',
    review_time DATETIME DEFAULT NULL COMMENT '审核时间',
    reviewer_id BIGINT DEFAULT NULL COMMENT '审核人ID',
    reviewer_name VARCHAR(64) DEFAULT NULL COMMENT '审核人姓名',
    reject_reason VARCHAR(500) DEFAULT NULL COMMENT '驳回原因',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (id),
    INDEX idx_pp_registration_id (registration_id),
    INDEX idx_pp_file_md5 (file_md5),
    INDEX idx_pp_status (status),
    INDEX idx_pp_create_time (create_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='支付凭证记录表';

-- -----------------------------------------------------------
-- 3. 对账批次表(每次导入协会对账Excel为一个批次)
-- -----------------------------------------------------------
CREATE TABLE IF NOT EXISTS reconciliation_batch (
    id BIGINT NOT NULL COMMENT '主键(ASSIGN_ID)',
    batch_no VARCHAR(64) NOT NULL COMMENT '批次号(如: RC20261027001)',
    file_name VARCHAR(255) DEFAULT NULL COMMENT '导入文件名',
    total_count INT DEFAULT 0 COMMENT '导入总记录数',
    matched_count INT DEFAULT 0 COMMENT '自动匹配成功数',
    unmatched_count INT DEFAULT 0 COMMENT '未匹配数',
    status VARCHAR(32) NOT NULL DEFAULT 'completed' COMMENT '批次状态: completed=已完成导入',
    operator_id BIGINT DEFAULT NULL COMMENT '操作人ID',
    operator_name VARCHAR(64) DEFAULT NULL COMMENT '操作人姓名',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (id),
    UNIQUE KEY uk_rb_batch_no (batch_no)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='对账批次表';

-- -----------------------------------------------------------
-- 4. 对账明细表(协会对账Excel每行对应一条记录)
-- -----------------------------------------------------------
CREATE TABLE IF NOT EXISTS reconciliation_record (
    id BIGINT NOT NULL COMMENT '主键(ASSIGN_ID)',
    batch_id BIGINT NOT NULL COMMENT '对账批次ID',
    raw_name VARCHAR(128) DEFAULT NULL COMMENT '协会数据-付款人姓名',
    raw_phone VARCHAR(32) DEFAULT NULL COMMENT '协会数据-付款人手机号',
    raw_amount DECIMAL(10,2) DEFAULT NULL COMMENT '协会数据-支付金额',
    raw_pay_time DATETIME DEFAULT NULL COMMENT '协会数据-支付时间',
    raw_trade_no VARCHAR(128) DEFAULT NULL COMMENT '协会数据-交易号',
    raw_remark VARCHAR(500) DEFAULT NULL COMMENT '协会数据-备注/摘要',
    matched_registration_id BIGINT DEFAULT NULL COMMENT '匹配到的注册用户ID',
    match_score INT DEFAULT 0 COMMENT '匹配得分(0-100, 越高越可信)',
    match_detail VARCHAR(500) DEFAULT NULL COMMENT '匹配维度说明(如: 姓名+金额+时间)',
    match_status VARCHAR(32) NOT NULL DEFAULT 'unmatched' COMMENT '匹配状态: auto_matched=自动匹配, manual_matched=人工确认, unmatched=未匹配, rejected=驳回',
    operator_id BIGINT DEFAULT NULL COMMENT '人工确认操作人ID',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (id),
    INDEX idx_rr_batch_id (batch_id),
    INDEX idx_rr_match_status (match_status),
    INDEX idx_rr_matched_reg_id (matched_registration_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='对账明细表';

-- -----------------------------------------------------------
-- 5. 菜单与权限(管理端)
--    会务门户管理(2807) -> 支付凭证审核 / 对账管理
--    sys_menu列: id, parent_id, tree_path, name, type(C目录/M菜单/B按钮),
--               route_name, route_path, component, perm, always_show, keep_alive,
--               visible, sort, icon, redirect, create_time, update_time, params
-- -----------------------------------------------------------
-- 5.1 支付凭证审核页面菜单(注意: M菜单不挂perm, list权限由B按钮2903承载)
INSERT INTO sys_menu (id, parent_id, tree_path, name, type, route_name, route_path, component, perm, always_show, keep_alive, visible, sort, icon, redirect, create_time, update_time)
VALUES (2900, 2807, '0,2807', '支付凭证审核', 'M', 'PaymentProof', 'paymentProof', 'portal/paymentProof/index', NULL, 0, 1, 1, 60, 'Money', NULL, NOW(), NOW());

INSERT INTO sys_menu (id, parent_id, tree_path, name, type, route_name, route_path, component, perm, always_show, keep_alive, visible, sort, icon, redirect, create_time, update_time)
VALUES (2903, 2900, '0,2807,2900', '凭证列表查询', 'B', NULL, NULL, NULL, 'sys:paymentProof:list', 0, 1, 1, 0, NULL, NULL, NOW(), NOW());

INSERT INTO sys_menu (id, parent_id, tree_path, name, type, route_name, route_path, component, perm, always_show, keep_alive, visible, sort, icon, redirect, create_time, update_time)
VALUES (2901, 2900, '0,2807,2900', '凭证审核', 'B', NULL, NULL, NULL, 'sys:paymentProof:review', 0, 1, 1, 1, NULL, NULL, NOW(), NOW());

INSERT INTO sys_menu (id, parent_id, tree_path, name, type, route_name, route_path, component, perm, always_show, keep_alive, visible, sort, icon, redirect, create_time, update_time)
VALUES (2902, 2900, '0,2807,2900', '线下支付登记', 'B', NULL, NULL, NULL, 'sys:paymentProof:offline', 0, 1, 1, 2, NULL, NULL, NOW(), NOW());

-- 5.2 对账管理页面菜单(注意: M菜单不挂perm, list权限由B按钮2913承载)
INSERT INTO sys_menu (id, parent_id, tree_path, name, type, route_name, route_path, component, perm, always_show, keep_alive, visible, sort, icon, redirect, create_time, update_time)
VALUES (2910, 2807, '0,2807', '对账管理', 'M', 'Reconciliation', 'reconciliation', 'portal/reconciliation/index', NULL, 0, 1, 1, 61, 'Document', NULL, NOW(), NOW());

INSERT INTO sys_menu (id, parent_id, tree_path, name, type, route_name, route_path, component, perm, always_show, keep_alive, visible, sort, icon, redirect, create_time, update_time)
VALUES (2913, 2910, '0,2807,2910', '对账列表查询', 'B', NULL, NULL, NULL, 'sys:reconciliation:list', 0, 1, 1, 0, NULL, NULL, NOW(), NOW());

INSERT INTO sys_menu (id, parent_id, tree_path, name, type, route_name, route_path, component, perm, always_show, keep_alive, visible, sort, icon, redirect, create_time, update_time)
VALUES (2911, 2910, '0,2807,2910', '导入对账表', 'B', NULL, NULL, NULL, 'sys:reconciliation:import', 0, 1, 1, 1, NULL, NULL, NOW(), NOW());

INSERT INTO sys_menu (id, parent_id, tree_path, name, type, route_name, route_path, component, perm, always_show, keep_alive, visible, sort, icon, redirect, create_time, update_time)
VALUES (2912, 2910, '0,2807,2910', '人工确认匹配', 'B', NULL, NULL, NULL, 'sys:reconciliation:confirm', 0, 1, 1, 2, NULL, NULL, NOW(), NOW());

-- 5.3 为 ADMIN 角色授权(role_id=2)
INSERT INTO sys_role_menu (role_id, menu_id) VALUES (2, 2900), (2, 2901), (2, 2902), (2, 2903), (2, 2910), (2, 2911), (2, 2912), (2, 2913);