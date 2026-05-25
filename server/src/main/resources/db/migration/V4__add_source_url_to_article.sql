-- ============================================================
-- V4: Add source_url column to article table
-- 爬虫写入时需要记录原文链接用于去重和溯源
-- ============================================================

SET @col_exists = (SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS
    WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'article' AND COLUMN_NAME = 'source_url');

SET @sql = IF(@col_exists = 0,
    'ALTER TABLE article ADD COLUMN source_url VARCHAR(512) NULL COMMENT ''原文链接'' AFTER published_at',
    'SELECT 1 AS info');

PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

CREATE INDEX IF NOT EXISTS idx_article_source_url ON article (source_url);
