-- 支付订单表
CREATE TABLE IF NOT EXISTS payment_orders (
    id              BIGINT       NOT NULL COMMENT '订单ID',
    registration_id BIGINT                COMMENT '注册用户ID（可空，支持匿名支付）',
    payment_method  VARCHAR(32)  NOT NULL COMMENT 'credit_card/wechat/alipay/bank_transfer',
    order_no        VARCHAR(64)  NOT NULL COMMENT '商户订单号',
    transaction_id  VARCHAR(64)           COMMENT '第三方交易号',
    amount          VARCHAR(32)  NOT NULL COMMENT '支付金额',
    currency        VARCHAR(8)   DEFAULT 'CNY' COMMENT '货币',
    status          VARCHAR(16)  DEFAULT 'pending' COMMENT 'pending/paid/refunded/closed',
    qr_code_url     TEXT                  COMMENT '二维码链接',
    pay_url         TEXT                  COMMENT 'H5支付跳转链接',
    extra_data      JSON                  COMMENT '扩展数据',
    expire_time     DATETIME              COMMENT '过期时间',
    paid_time       DATETIME              COMMENT '支付时间',
    create_time     DATETIME     DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time     DATETIME     DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (id),
    INDEX idx_registration_id (registration_id),
    INDEX idx_order_no (order_no)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='支付订单表';
