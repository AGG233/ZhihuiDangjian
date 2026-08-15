-- ============================================================
-- V15: 对齐历史 comment/user_like 表结构与新代码期望
-- 背景：数据库早期应用过另一套 V8 social_tables，导致：
--   - comment.status 为 ENUM，而新代码期望 TINYINT(1)
--   - 点赞数据存放在 user_like，而新代码使用 like_record
-- 本迁移幂等，可重复执行。
-- ============================================================

-- -----------------------------------------------------------
-- 1. 将 comment.status 从 ENUM 转换为 TINYINT(1)
--    仅当 status 当前为 enum 时执行转换
-- -----------------------------------------------------------
SET @status_is_enum = (
    SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS
    WHERE TABLE_SCHEMA = DATABASE()
      AND TABLE_NAME = 'comment'
      AND COLUMN_NAME = 'status'
      AND DATA_TYPE = 'enum'
);

-- 1a. 新增临时列 status_new
SET @sql = IF(@status_is_enum > 0,
    'ALTER TABLE comment ADD COLUMN status_new TINYINT(1) NOT NULL DEFAULT 1 COMMENT ''状态: 1=正常, 0=已删除/屏蔽'' AFTER status',
    'SELECT 1');
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- 1b. 迁移旧枚举值到数字状态
SET @sql = IF(@status_is_enum > 0,
    'UPDATE comment SET status_new = 1 WHERE status = ''published''',
    'SELECT 1');
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @sql = IF(@status_is_enum > 0,
    'UPDATE comment SET status_new = 0 WHERE status IN (''pending_review'',''hidden'',''deleted'')',
    'SELECT 1');
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- 1c. 删除旧 status 列
SET @sql = IF(@status_is_enum > 0,
    'ALTER TABLE comment DROP COLUMN status',
    'SELECT 1');
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- 1d. 将 status_new 重命名为 status
SET @sql = IF(@status_is_enum > 0,
    'ALTER TABLE comment CHANGE COLUMN status_new status TINYINT(1) NOT NULL DEFAULT 1 COMMENT ''状态: 1=正常, 0=已删除/屏蔽''',
    'SELECT 1');
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- -----------------------------------------------------------
-- 2. 创建 like_record（与 V12 一致，幂等）
-- -----------------------------------------------------------
CREATE TABLE IF NOT EXISTS like_record
(
    id          BIGINT UNSIGNED NOT NULL PRIMARY KEY COMMENT '点赞记录ID',
    user_id     BIGINT UNSIGNED NOT NULL COMMENT '点赞用户ID',
    target_type ENUM ('course','article') NOT NULL COMMENT '点赞目标类型: course=课程, article=文章',
    target_id   BIGINT UNSIGNED NOT NULL COMMENT '点赞目标ID（课程ID或文章ID）',
    created_at  DATETIME(3)     NOT NULL DEFAULT CURRENT_TIMESTAMP(3) COMMENT '创建时间',
    CONSTRAINT uk_like_user_target UNIQUE (user_id, target_type, target_id),
    INDEX idx_like_target (target_type, target_id)
) ENGINE = InnoDB
  CHARSET = utf8mb4
  ROW_FORMAT = DYNAMIC COMMENT '点赞记录表';

-- -----------------------------------------------------------
-- 3. 将旧 user_like 数据复制到 like_record（仅 course/article）
-- -----------------------------------------------------------
SET @user_like_exists = (
    SELECT COUNT(*) FROM INFORMATION_SCHEMA.TABLES
    WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'user_like'
);

SET @sql = IF(@user_like_exists > 0,
    'INSERT IGNORE INTO like_record (id, user_id, target_type, target_id, created_at)
     SELECT id, user_id, target_type, target_id, created_at
     FROM user_like
     WHERE target_type IN (''course'',''article'')',
    'SELECT 1');
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;
