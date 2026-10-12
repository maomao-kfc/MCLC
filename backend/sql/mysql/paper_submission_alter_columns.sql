-- 论文投稿表字段类型修改
-- 1. abstract 列：VARCHAR 改为 TEXT，支持 500 词长摘要存储
-- 2. paper_title 列：VARCHAR 改为 TEXT，取消标题长度限制

ALTER TABLE paper_submission MODIFY COLUMN `abstract` TEXT NULL COMMENT '论文摘要';
ALTER TABLE paper_submission MODIFY COLUMN paper_title TEXT NULL COMMENT '论文标题';