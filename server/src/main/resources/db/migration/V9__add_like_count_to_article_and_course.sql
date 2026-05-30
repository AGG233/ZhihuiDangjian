-- V9: 为 article 和 course 表添加点赞数字段

SET @col_exists = (SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS
    WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'article' AND COLUMN_NAME = 'like_count');

SET @sql = IF(@col_exists = 0,
    'ALTER TABLE article ADD COLUMN like_count INT UNSIGNED NOT NULL DEFAULT 0 COMMENT ''点赞数''',
    'SELECT 1 AS info');
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @col_exists = (SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS
    WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'course' AND COLUMN_NAME = 'like_count');

SET @sql = IF(@col_exists = 0,
    'ALTER TABLE course ADD COLUMN like_count INT UNSIGNED NOT NULL DEFAULT 0 COMMENT ''点赞数''',
    'SELECT 1 AS info');
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;
