-- ============================================================
-- V7: SCORM 学习包与学习注册
-- 表：scorm_package（SCORM 学习包）、scorm_registration（学习注册与成绩）
-- 幂等策略：索引/唯一键全部内联在 CREATE TABLE IF NOT EXISTS 中，
--   表已存在时整条语句跳过，避免 CREATE INDEX 非幂等（MySQL 不支持 IF NOT EXISTS）
-- ============================================================

-- -----------------------------------------------------------
-- scorm_package — SCORM 学习包
-- -----------------------------------------------------------
CREATE TABLE IF NOT EXISTS scorm_package
(
    id               BIGINT UNSIGNED NOT NULL COMMENT '主键',
    title            VARCHAR(255)    NOT NULL COMMENT '课程标题',
    version          VARCHAR(32)     NOT NULL COMMENT 'SCORM版本 1.2/2004',
    identifier       VARCHAR(255)    NOT NULL COMMENT 'manifest标识',
    manifest_content LONGTEXT        NULL COMMENT 'imsmanifest.xml原文',
    file_url         VARCHAR(512)    NULL COMMENT '包文件地址',
    created_at       DATETIME(3)     NOT NULL DEFAULT CURRENT_TIMESTAMP(3) COMMENT '创建时间',
    updated_at       DATETIME(3)     NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3) COMMENT '更新时间',
    PRIMARY KEY (id)
) ENGINE = InnoDB
  CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci
  ROW_FORMAT = DYNAMIC COMMENT 'SCORM学习包';

-- -----------------------------------------------------------
-- scorm_registration — SCORM 学习注册与成绩（user_id + package_id 检索）
-- -----------------------------------------------------------
CREATE TABLE IF NOT EXISTS scorm_registration
(
    id                  BIGINT UNSIGNED NOT NULL COMMENT '主键',
    user_id             BIGINT UNSIGNED NOT NULL COMMENT '用户ID',
    package_id          BIGINT UNSIGNED NOT NULL COMMENT '学习包ID',
    sco_identifier      VARCHAR(255)    NOT NULL COMMENT 'SCO标识',
    lesson_status       VARCHAR(32)     NULL COMMENT 'cmi.core.lesson_status',
    score_raw           DECIMAL(5,2)    NULL COMMENT 'cmi.core.score.raw',
    score_min           DECIMAL(5,2)    NULL COMMENT 'cmi.core.score.min',
    score_max           DECIMAL(5,2)    NULL COMMENT 'cmi.core.score.max',
    session_time_seconds INT            NULL COMMENT 'cmi.core.session_time 秒',
    total_time_seconds  INT             NULL COMMENT 'cmi.core.total_time 秒',
    created_at          DATETIME(3)     NOT NULL DEFAULT CURRENT_TIMESTAMP(3) COMMENT '创建时间',
    updated_at          DATETIME(3)     NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3) COMMENT '更新时间',
    PRIMARY KEY (id),
    KEY idx_user_package (user_id, package_id)
) ENGINE = InnoDB
  CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci
  ROW_FORMAT = DYNAMIC COMMENT 'SCORM学习注册与成绩';
