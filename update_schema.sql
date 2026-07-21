-- 更新 system_configs 表结构
ALTER TABLE system_configs
    ADD COLUMN id BIGINT AUTO_INCREMENT PRIMARY KEY FIRST;

ALTER TABLE system_configs
    MODIFY COLUMN config_value TEXT NOT NULL;

ALTER TABLE system_configs
    ADD CONSTRAINT uk_config_key UNIQUE (config_key);

ALTER TABLE system_configs
    MODIFY COLUMN updated_at DATETIME NOT NULL;

-- 更新 operation_logs 表结构
ALTER TABLE operation_logs
    CHANGE COLUMN operation action VARCHAR(50) NOT NULL;

ALTER TABLE operation_logs
    ADD COLUMN target_type VARCHAR(50);

ALTER TABLE operation_logs
    ADD COLUMN target_id BIGINT;

ALTER TABLE operation_logs
    CHANGE COLUMN details details VARCHAR(500);

ALTER TABLE operation_logs
    MODIFY COLUMN ip_address VARCHAR(45);

ALTER TABLE operation_logs
    MODIFY COLUMN created_at DATETIME NOT NULL;