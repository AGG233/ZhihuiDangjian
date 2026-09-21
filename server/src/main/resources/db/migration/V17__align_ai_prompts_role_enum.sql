-- ============================================================
-- V17: ai_prompts.role 枚举与 PromptRoleEnum 对齐
-- 背景：V3 建表时 role 为 ENUM('system','user')（小写），而 Java 侧
--       PromptRoleEnum 是 SYSTEM/DEVELOPER 且无 @EnumValue 映射。
--       MySQL 的 ENUM 比较不区分大小写，写入 'SYSTEM' 会被存成 'system'，
--       读取时 valueOf("system") 大小写敏感而失败 →
--       GET /api/admin/ai/prompts 返回 500；且 DEVELOPER 在旧枚举中无对应值。
-- 处理：枚举值改为 ('system','developer')，与 Java 枚举的 @EnumValue 一致；
--       历史值 'user' 迁移为 'developer'。本迁移幂等，可重复执行。
-- ============================================================

SET @role_legacy = (
    SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS
    WHERE TABLE_SCHEMA = DATABASE()
      AND TABLE_NAME = 'ai_prompts'
      AND COLUMN_NAME = 'role'
      AND COLUMN_TYPE LIKE '%user%'
);

-- 1. 先转 VARCHAR，避免直接改 ENUM 定义时截断既有值
SET @sql = IF(@role_legacy > 0,
    'ALTER TABLE ai_prompts MODIFY COLUMN role VARCHAR(16) NOT NULL COMMENT ''提示词角色''',
    'SELECT 1');
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- 2. 历史值归一：转小写，并把遗留的 'user' 迁移为 'developer'
SET @sql = IF(@role_legacy > 0,
    'UPDATE ai_prompts SET role = CASE LOWER(role) WHEN ''user'' THEN ''developer'' ELSE LOWER(role) END',
    'SELECT 1');
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- 3. 收敛为与 Java 枚举 @EnumValue 一致的新枚举
SET @sql = IF(@role_legacy > 0,
    'ALTER TABLE ai_prompts MODIFY COLUMN role ENUM(''system'',''developer'') NOT NULL COMMENT ''角色: system 或 developer''',
    'SELECT 1');
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;
