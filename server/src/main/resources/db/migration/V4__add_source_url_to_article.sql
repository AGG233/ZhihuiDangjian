-- ============================================================
-- V4: Add source_url column to article table
-- 爬虫写入时需要记录原文链接用于去重和溯源
-- ============================================================

ALTER TABLE article
    ADD COLUMN source_url VARCHAR(512) NULL COMMENT '原文链接' AFTER published_at;

CREATE INDEX idx_article_source_url ON article (source_url);
