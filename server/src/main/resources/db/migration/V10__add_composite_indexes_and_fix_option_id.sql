-- ============================================================
-- V10: Add composite indexes and fix option_id NOT NULL
-- 变更概要:
--   - article(status, published_at) 复合索引
--   - comment(parent_id, target_type, target_id) 联合索引
--   - user_quiz_answer.option_id 改为 NULLABLE（支持 short_answer 题型）
--
-- ADR: course.is_published 前导布尔索引
--   评价: 已有 idx_course_published(is_published, published_at)
--   复合索引，TINYINT + DATETIME(3) 组合查询效率足够，无需调整。
--
-- ADR: source_url VARCHAR(512) 索引前缀
--   评价: MySQL 8.4 InnoDB DYNAMIC 行格式下最大索引键长 3072 字节，
--   utf8mb4: 512*4=2048 < 3072，现有全列索引安全，无需改为前缀索引。
--
-- ADR: category_course / category_article 主键设计
--   评价: 当前 PRIMARY KEY(course_id)/PRIMARY KEY(article_id) 限制
--   一个课程/文章只能归属一个分类。若业务需要多分类，需改为复合主键
--   (category_id, course_id)/(category_id, article_id)。此变更涉及
--   大量关联代码和迁移，暂不修，待确认业务需求。
--
-- ADR: quiz_option.order_index 类型
--   评价: 列注释为"选项标签（A, B, C, D...）"，VARCHAR(10) 存储
--   字母标签完全正确。非数值排序，无需改为整数类型。业务允许。不修。
-- ============================================================

-- -----------------------------------------------------------
-- 1. article(status, published_at) 复合索引
--    查询模式: WHERE status='published' ORDER BY published_at DESC
-- -----------------------------------------------------------
SET @idx_exists = (SELECT COUNT(*) FROM INFORMATION_SCHEMA.STATISTICS
    WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'article' AND INDEX_NAME = 'idx_article_status_published_at');

SET @sql = IF(@idx_exists = 0,
    'CREATE INDEX idx_article_status_published_at ON article (status, published_at)',
    'SELECT 1');
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- -----------------------------------------------------------
-- 2. comment(parent_id, target_type, target_id) 联合索引
--    查询模式: 查找某个父评论下指定类型的回复
-- -----------------------------------------------------------
SET @idx_exists = (SELECT COUNT(*) FROM INFORMATION_SCHEMA.STATISTICS
    WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'comment' AND INDEX_NAME = 'idx_comment_parent_target');

SET @sql = IF(@idx_exists = 0,
    'CREATE INDEX idx_comment_parent_target ON comment (parent_id, target_type, target_id)',
    'SELECT 1');
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- -----------------------------------------------------------
-- 3. user_quiz_answer.option_id 改为 NULLABLE
--    short_answer 题型无选项，option_id 应为 NULL
-- -----------------------------------------------------------
SET @fk_exists = (SELECT COUNT(*) FROM INFORMATION_SCHEMA.TABLE_CONSTRAINTS
    WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'user_quiz_answer'
    AND CONSTRAINT_NAME = 'fk_user_quiz_answer_option' AND CONSTRAINT_TYPE = 'FOREIGN KEY');

SET @sql_drop_fk = IF(@fk_exists > 0,
    'ALTER TABLE user_quiz_answer DROP FOREIGN KEY fk_user_quiz_answer_option',
    'SELECT 1');
PREPARE stmt FROM @sql_drop_fk;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @col_nullable = (SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS
    WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'user_quiz_answer'
    AND COLUMN_NAME = 'option_id' AND IS_NULLABLE = 'YES');

SET @sql_alter_null = IF(@col_nullable = 0,
    'ALTER TABLE user_quiz_answer MODIFY COLUMN option_id BIGINT UNSIGNED NULL COMMENT ''所选选项ID（选择题适用，简答题为NULL）''',
    'SELECT 1');
PREPARE stmt FROM @sql_alter_null;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;
