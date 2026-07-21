USE knowledge_base;

ALTER TABLE tags MODIFY COLUMN name VARCHAR(100) NOT NULL;
ALTER TABLE tags CHANGE COLUMN owner_id user_id BIGINT NOT NULL;

ALTER TABLE file_tags ADD COLUMN created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP;

ALTER TABLE folder_templates MODIFY COLUMN name VARCHAR(255) NOT NULL;

ALTER TABLE tags ADD CONSTRAINT uk_tags_name_user UNIQUE (user_id, name);
ALTER TABLE file_tags ADD CONSTRAINT uk_file_tags UNIQUE (file_id, tag_id);

SELECT '数据库表结构更新完成 (v2)' AS result;