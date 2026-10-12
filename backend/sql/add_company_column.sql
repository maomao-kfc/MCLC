-- 论文投稿表添加"所属单位/医院"字段
ALTER TABLE paper_submission ADD COLUMN company VARCHAR(255) DEFAULT NULL COMMENT '所属单位/医院' AFTER name;