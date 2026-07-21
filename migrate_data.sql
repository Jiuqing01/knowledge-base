-- 迁移 operation_logs 表数据
UPDATE operation_logs 
SET action = operation, 
    details = detail, 
    target_type = target 
WHERE action IS NULL;

-- 删除 operation_logs 表中的旧列
ALTER TABLE operation_logs 
    DROP COLUMN operation, 
    DROP COLUMN detail, 
    DROP COLUMN target, 
    DROP COLUMN username;

-- 确保 system_configs 表的 id 列是主键
ALTER TABLE system_configs 
    DROP PRIMARY KEY;

ALTER TABLE system_configs 
    MODIFY COLUMN id BIGINT AUTO_INCREMENT PRIMARY KEY;

-- 确保 config_key 唯一约束
ALTER TABLE system_configs 
    ADD CONSTRAINT uk_config_key UNIQUE (config_key);

-- 修改 config_value 为 TEXT 类型
ALTER TABLE system_configs 
    MODIFY COLUMN config_value TEXT NOT NULL;

-- 修改 updated_at 为 NOT NULL
ALTER TABLE system_configs 
    MODIFY COLUMN updated_at DATETIME NOT NULL;