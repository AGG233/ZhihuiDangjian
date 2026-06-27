-- ============================================================
-- V11: 扩展 ai_prompts.role 枚举以对齐 PromptRoleEnum
-- 将 ENUM('system','user') 扩展为 ENUM('system','user','developer')
-- 使用 INFORMATION_SCHEMA 做幂等检查
-- ============================================================

SET @dbname = DATABASE();
SET @exists = (
    SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS
    WHERE TABLE_SCHEMA = @dbname
      AND TABLE_NAME = 'ai_prompts'
      AND COLUMN_NAME = 'role'
      AND COLUMN_TYPE LIKE '%developer%'
);

SET @sql = IF(@exists = 0,
    'ALTER TABLE ai_prompts MODIFY COLUMN role ENUM(''system'',''user'',''developer'') NOT NULL COMMENT ''角色: system, user 或 developer''',
    'SELECT 1 AS migration_already_applied'
);

PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;
