USE knowledge_base;

SELECT '=== 用户表数据 ===' AS section;
SELECT id, username, role, storage_used, storage_quota FROM users;

SELECT '=== 文件夹表数据 ===' AS section;
SELECT id, name, parent_id, owner_id FROM folders LIMIT 10;

SELECT '=== 文件表数据 ===' AS section;
SELECT id, original_name, file_type, size, owner_id, is_deleted FROM files;

SELECT '=== 标签表数据 ===' AS section;
SELECT id, name, owner_id, color FROM tags;

SELECT '=== 文件标签关联 ===' AS section;
SELECT ft.file_id, ft.tag_id, f.original_name, t.name as tag_name FROM file_tags ft
JOIN files f ON ft.file_id = f.id
JOIN tags t ON ft.tag_id = t.id;
