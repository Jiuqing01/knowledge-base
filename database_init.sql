CREATE DATABASE IF NOT EXISTS knowledge_base 
DEFAULT CHARACTER SET utf8mb4 
DEFAULT COLLATE utf8mb4_unicode_ci;

USE knowledge_base;

DROP TABLE IF EXISTS file_tags;
DROP TABLE IF EXISTS files;
DROP TABLE IF EXISTS tags;
DROP TABLE IF EXISTS folders;
DROP TABLE IF EXISTS folder_templates;
DROP TABLE IF EXISTS operation_logs;
DROP TABLE IF EXISTS system_configs;
DROP TABLE IF EXISTS departments;
DROP TABLE IF EXISTS users;

CREATE TABLE users (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    username VARCHAR(50) NOT NULL UNIQUE,
    phone VARCHAR(20),
    email VARCHAR(100),
    department_id BIGINT,
    password VARCHAR(255) NOT NULL,
    role VARCHAR(20) NOT NULL DEFAULT 'USER',
    storage_used BIGINT DEFAULT 0,
    storage_quota BIGINT DEFAULT 1073741824,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_users_username (username),
    INDEX idx_users_role (role)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE folder_templates (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    parent_id BIGINT,
    sort_order INT DEFAULT 0,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_folder_templates_parent (parent_id),
    FOREIGN KEY (parent_id) REFERENCES folder_templates(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE folders (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    parent_id BIGINT,
    owner_id BIGINT NOT NULL,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_folders_owner (owner_id),
    INDEX idx_folders_parent (parent_id),
    FOREIGN KEY (owner_id) REFERENCES users(id) ON DELETE CASCADE,
    FOREIGN KEY (parent_id) REFERENCES folders(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE files (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    original_name VARCHAR(255) NOT NULL,
    file_path VARCHAR(500) NOT NULL,
    file_type VARCHAR(50),
    size BIGINT NOT NULL DEFAULT 0,
    folder_id BIGINT,
    owner_id BIGINT NOT NULL,
    is_shared BOOLEAN DEFAULT FALSE,
    is_deleted BOOLEAN DEFAULT FALSE,
    deleted_at DATETIME,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_files_owner (owner_id),
    INDEX idx_files_folder (folder_id),
    INDEX idx_files_is_deleted (is_deleted),
    FOREIGN KEY (owner_id) REFERENCES users(id) ON DELETE CASCADE,
    FOREIGN KEY (folder_id) REFERENCES folders(id) ON DELETE SET NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE tags (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(50) NOT NULL,
    owner_id BIGINT NOT NULL,
    color VARCHAR(7) DEFAULT '#409eff',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_tags_owner (owner_id),
    UNIQUE KEY uk_tags_name_owner (name, owner_id),
    FOREIGN KEY (owner_id) REFERENCES users(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE file_tags (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    file_id BIGINT NOT NULL,
    tag_id BIGINT NOT NULL,
    INDEX idx_file_tags_file (file_id),
    INDEX idx_file_tags_tag (tag_id),
    UNIQUE KEY uk_file_tags (file_id, tag_id),
    FOREIGN KEY (file_id) REFERENCES files(id) ON DELETE CASCADE,
    FOREIGN KEY (tag_id) REFERENCES tags(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

INSERT INTO folder_templates (name, parent_id, sort_order) VALUES
('文档资料', NULL, 1),
('项目文件', NULL, 2),
('图片素材', NULL, 3),
('个人笔记', NULL, 4),
('报告文档', 1, 1),
('会议记录', 1, 2),
('设计稿', 3, 1),
('截图', 3, 2);

INSERT INTO users (username, phone, email, password, role, storage_used, storage_quota) VALUES
('admin', '13800138000', 'admin@ngd.edu', '$2a$10$ALKb28xNOdTmwfzQGFFgOeHOP3RSSLu6W9cBMNP0sTn1hBdUrf49e', 'ADMIN', 0, 10737418240),
('user', '13900139000', 'user@ngd.edu', '$2a$10$ALKb28xNOdTmwfzQGFFgOeHOP3RSSLu6W9cBMNP0sTn1hBdUrf49e', 'USER', 0, 1073741824);

INSERT INTO folders (name, parent_id, owner_id) VALUES
('文档资料', NULL, 1),
('项目文件', NULL, 1),
('图片素材', NULL, 1),
('个人笔记', NULL, 1),
('报告文档', 1, 1),
('会议记录', 1, 1),
('设计稿', 3, 1),
('截图', 3, 1),
('文档资料', NULL, 2),
('项目文件', NULL, 2),
('图片素材', NULL, 2),
('个人笔记', NULL, 2),
('报告文档', 9, 2),
('会议记录', 9, 2),
('设计稿', 11, 2),
('截图', 11, 2);

INSERT INTO tags (name, owner_id, color) VALUES
('重要', 1, '#e6a23c'),
('待处理', 1, '#f56c6c'),
('已完成', 1, '#67c23a'),
('工作', 1, '#409eff'),
('个人', 1, '#909399'),
('重要', 2, '#e6a23c'),
('待处理', 2, '#f56c6c'),
('已完成', 2, '#67c23a'),
('工作', 2, '#409eff'),
('个人', 2, '#909399');

CREATE TABLE departments (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    parent_id BIGINT,
    sort_order INT DEFAULT 0,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_departments_parent (parent_id),
    FOREIGN KEY (parent_id) REFERENCES departments(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

INSERT INTO departments (name, parent_id, sort_order) VALUES
('研发部', NULL, 1),
('市场部', NULL, 2),
('测试部', NULL, 3),
('财务部', NULL, 4),
('运维部', NULL, 5),
('前端开发组', 1, 1),
('后端开发组', 1, 2),
('产品组', 1, 3);

CREATE TABLE system_configs (
    config_key VARCHAR(100) NOT NULL PRIMARY KEY,
    config_value VARCHAR(1000) NOT NULL,
    description VARCHAR(500),
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

INSERT INTO system_configs (config_key, config_value, description) VALUES
('system_name', '知识库管理系统', '系统名称'),
('version', 'v1.0.0', '系统版本'),
('max_file_size', '524288000', '最大文件大小(字节)'),
('default_storage_quota', '1073741824', '默认存储配额(字节)'),
('allowed_file_extensions', 'pdf,doc,docx,xls,xlsx,ppt,pptx,jpg,jpeg,png,gif,bmp,webp,txt,md,zip,rar', '允许的文件扩展名'),
('login_attempt_limit', '5', '登录失败次数限制'),
('session_timeout', '30', 'Session超时时间(分钟)'),
('enable_captcha', 'true', '是否开启验证码'),
('enable_registration', 'true', '是否开启注册');

CREATE TABLE operation_logs (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT,
    username VARCHAR(50),
    operation VARCHAR(50) NOT NULL,
    target VARCHAR(255),
    detail TEXT,
    ip_address VARCHAR(50),
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_operation_logs_user (user_id),
    INDEX idx_operation_logs_operation (operation),
    INDEX idx_operation_logs_created_at (created_at),
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE SET NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

SELECT '数据库初始化完成' AS result;