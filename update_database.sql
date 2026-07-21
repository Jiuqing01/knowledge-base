USE knowledge_base;

ALTER TABLE folders MODIFY COLUMN name VARCHAR(255) NOT NULL;
ALTER TABLE folders ADD COLUMN is_deleted TINYINT(1) DEFAULT 0;

ALTER TABLE files ADD COLUMN storage_name VARCHAR(64) NOT NULL UNIQUE AFTER original_name;
ALTER TABLE files ADD COLUMN storage_path VARCHAR(512) NOT NULL AFTER storage_name;
ALTER TABLE files ADD COLUMN file_hash VARCHAR(64) AFTER file_type;
ALTER TABLE files CHANGE COLUMN size file_size BIGINT NOT NULL DEFAULT 0;
ALTER TABLE files CHANGE COLUMN file_type mime_type VARCHAR(128);
ALTER TABLE files MODIFY COLUMN folder_id BIGINT NOT NULL;
ALTER TABLE files DROP COLUMN name, DROP COLUMN file_path, DROP COLUMN is_shared;
ALTER TABLE files CHANGE COLUMN is_deleted is_deleted TINYINT(1) DEFAULT 0;

ALTER TABLE users ADD COLUMN is_deleted TINYINT(1) DEFAULT 0;

UPDATE files SET storage_name = CONCAT(UUID(), '.', SUBSTRING_INDEX(original_name, '.', -1));
UPDATE files SET storage_path = CONCAT('/uploads/', owner_id, '/', storage_name);

UPDATE folders SET is_deleted = 0 WHERE is_deleted IS NULL;
UPDATE users SET is_deleted = 0 WHERE is_deleted IS NULL;

SELECT '数据库表结构更新完成' AS result;