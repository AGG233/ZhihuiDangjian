-- ============================================================
-- V5: 评论与点赞互动模块（4.5 互动维度）
-- 表：comment（评论）、like_record（点赞记录）
-- 幂等策略：索引/唯一键全部内联在 CREATE TABLE IF NOT EXISTS 中，
--   表已存在时整条语句跳过，避免 CREATE INDEX 非幂等（MySQL 不支持 IF NOT EXISTS）
-- ============================================================

-- -----------------------------------------------------------
-- comment — 评论表
-- -----------------------------------------------------------
CREATE TABLE IF NOT EXISTS comment
(
    id          BIGINT UNSIGNED NOT NULL PRIMARY KEY COMMENT '评论ID',
    user_id     BIGINT UNSIGNED NOT NULL COMMENT '评论用户ID',
    target_type ENUM ('course','article') NOT NULL COMMENT '评论目标类型: course=课程, article=文章',
    target_id   BIGINT UNSIGNED NOT NULL COMMENT '评论目标ID（课程ID或文章ID）',
    content     VARCHAR(1000)   NOT NULL COMMENT '评论内容',
    parent_id   BIGINT UNSIGNED NULL COMMENT '父评论ID，回复时使用，根评论为空',
    status      TINYINT(1)      NOT NULL DEFAULT 1 COMMENT '状态: 1=正常, 0=已删除/屏蔽',
    created_at  DATETIME(3)     NOT NULL DEFAULT CURRENT_TIMESTAMP(3) COMMENT '创建时间',
    updated_at  DATETIME(3)     NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3) COMMENT '更新时间',
    INDEX idx_comment_target (target_type, target_id, created_at),
    INDEX idx_comment_user (user_id),
    INDEX idx_comment_parent (parent_id)
) ENGINE = InnoDB
  CHARSET = utf8mb4
  ROW_FORMAT = DYNAMIC COMMENT '评论表';

-- -----------------------------------------------------------
-- like_record — 点赞记录表（user_id+target_type+target_id 唯一）
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
