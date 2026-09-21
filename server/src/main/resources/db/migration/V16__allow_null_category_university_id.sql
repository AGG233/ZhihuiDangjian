-- ============================================================
-- V16: 允许分类的 university_id 为空（公共分类）
-- 背景：CategoryService.create 对 MANAGER 角色刻意不设置 universityId，
--       代码注释声明其为「公共分类（DB 中为 NULL）」；但 V1 建表时该列是
--       VARCHAR(10) NOT NULL 且无默认值，导致 MANAGER 创建根分类时
--       报 "Field 'university_id' doesn't have a default value"（HTTP 500）。
--       此缺陷此前被认证故障（所有接口 401）掩盖，修复 token 头后暴露。
-- 本迁移幂等，可重复执行。
-- ============================================================

SET @uni_is_not_null = (
    SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS
    WHERE TABLE_SCHEMA = DATABASE()
      AND TABLE_NAME = 'category'
      AND COLUMN_NAME = 'university_id'
      AND IS_NULLABLE = 'NO'
);

SET @sql = IF(@uni_is_not_null > 0,
    'ALTER TABLE category MODIFY COLUMN university_id VARCHAR(10) NULL COMMENT ''学校编码，NULL 表示公共分类''',
    'SELECT 1');
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;
