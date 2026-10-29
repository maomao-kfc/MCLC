# 案例01：邮件中心 Unknown column 'record_type' — 代码与数据库 Schema 不同步

> **故障等级**: P1（管理端邮件中心页面完全不可用）  
> **发生时间**: 2026-10  
> **状态**: 【曾经发生】【已修复】【已验证】

---

## A. 故障现象

管理端"邮件中心"页面打开报错：
```
### Error querying database
### Cause: java.sql.SQLSyntaxErrorException: Unknown column 'record_type' in 'where clause'
### The error may exist in file [MailSendRecordMapper.xml]
### SQL: SELECT COUNT(*) AS total FROM mail_send_record 
      WHERE (record_type = 'ORIGINAL' OR record_type IS NULL OR parent_record_id IS NULL)
```

## B. 排查过程

1. **定位报错 SQL**：MailSendRecordMapper.xml 中分页查询的 WHERE 条件引用了 `record_type` 列
2. **检查实体类**：MailSendRecord.java 已有 recordType/retrySequence/rootRecordId 三个字段
3. **检查数据库**：`DESC mail_send_record` 发现表中**没有**这三列
4. **追溯原因**：代码已升级为父子重试模型（新增3字段），但本地数据库未执行对应的 DDL

## C. 根因分析

**代码版本与数据库 Schema 不同步**。

- 代码层：MailSendRecord 实体 + MailSendRecordMapper.xml + MailSendRecordServiceImpl 均已引用 `record_type` / `retry_sequence` / `root_record_id`
- 数据库层：本地 `mail_send_record` 表仍为旧结构（只有 parent_record_id，无 record_type 等）
- 根本原因：功能升级时只改了代码，没有同步提供数据库迁移脚本或未在本地执行

## D. 修复方案

```sql
-- 参照 SQL 备份文件确认列定义后执行
ALTER TABLE mail_send_record 
  ADD COLUMN record_type VARCHAR(20) DEFAULT 'ORIGINAL' COMMENT '记录类型: ORIGINAL/RETRY',
  ADD COLUMN retry_sequence INT COMMENT '重试序号',
  ADD COLUMN root_record_id BIGINT COMMENT '指向原始记录ID';

-- 索引
CREATE INDEX idx_record_type ON mail_send_record(record_type);
CREATE INDEX idx_root_record_id ON mail_send_record(root_record_id);
CREATE UNIQUE INDEX uk_root_retry ON mail_send_record(root_record_id, retry_sequence);

-- 现有记录兼容：DEFAULT 'ORIGINAL' 自动填充
```

## E. 验证方式

1. `DESC mail_send_record` 确认三列存在
2. 管理端邮件中心页面正常打开，无 SQL 报错
3. 现有记录 record_type 自动为 'ORIGINAL'，分页查询正常

## F. 影响范围

- 管理端邮件中心列表页面完全不可用（PENDING/SENT/FAILED 等所有状态查询均走该 SQL）
- 邮件发送本身不受影响（createAndSend 不依赖该查询）
- 仅影响本地环境，服务器环境需确认是否同步

## G. 经验教训

1. **实体字段与数据库列必须同步**：MyBatis-Plus 实体新增字段时，必须同步提供 ALTER TABLE 脚本
2. **SQL 备份文件是列定义的权威证据**：修复前先对照 `SQLbackups/` 下的备份文件确认设计
3. **DEFAULT 值保证存量数据兼容**：新增列时设 DEFAULT，避免存量行该列为 NULL 导致查询遗漏
4. **建议引入 Flyway/Liquibase**：自动化数据库版本管理，避免手动同步遗漏

## H. 相关代码/文件

| 文件 | 说明 |
|------|------|
| backend/.../MailSendRecord.java | 实体类（含 recordType/retrySequence/rootRecordId） |
| backend/.../MailSendRecordMapper.xml | 分页查询 SQL（WHERE 条件引用 record_type） |
| backend/.../MailSendRecordServiceImpl.java | getMailSendRecordPage 树形查询逻辑 |
| backend/sql/SQLbackups/ | 列定义参考 |