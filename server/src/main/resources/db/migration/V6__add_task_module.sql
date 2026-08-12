-- ============================================================
-- V6: 任务发布模块
-- 表：task（任务）、task_acceptance（任务领取记录）
-- 幂等策略：索引/唯一键全部内联在 CREATE TABLE IF NOT EXISTS 中，
--   表已存在时整条语句跳过，避免 CREATE INDEX 非幂等（MySQL 不支持 IF NOT EXISTS）
-- ============================================================

-- -----------------------------------------------------------
-- task — 任务表
-- -----------------------------------------------------------
CREATE TABLE IF NOT EXISTS task
(
    id          BIGINT UNSIGNED NOT NULL PRIMARY KEY COMMENT '任务ID',
    title       VARCHAR(200)    NOT NULL COMMENT '任务标题',
    description TEXT            NULL COMMENT '任务描述',
    task_type   ENUM ('learning','quiz','social','custom') NOT NULL COMMENT '任务类型: learning=学习, quiz=测验, social=社会实践, custom=自定义',
    points      INT             NOT NULL DEFAULT 0 COMMENT '任务积分',
    deadline    DATETIME(3)     NULL COMMENT '任务截止时间',
    creator_id  BIGINT UNSIGNED NOT NULL COMMENT '创建者（学校/管理员）用户ID',
    status      ENUM ('draft','published','closed') NOT NULL DEFAULT 'draft' COMMENT '任务状态: draft=草稿, published=已发布, closed=已关闭',
    created_at  DATETIME(3)     NOT NULL DEFAULT CURRENT_TIMESTAMP(3) COMMENT '创建时间',
    updated_at  DATETIME(3)     NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3) COMMENT '更新时间',
    INDEX idx_task_status (status),
    INDEX idx_task_creator (creator_id)
) ENGINE = InnoDB
  CHARSET = utf8mb4
  ROW_FORMAT = DYNAMIC COMMENT '任务表';

-- -----------------------------------------------------------
-- task_acceptance — 任务领取记录表（task_id + user_id 唯一）
-- -----------------------------------------------------------
CREATE TABLE IF NOT EXISTS task_acceptance
(
    id           BIGINT UNSIGNED NOT NULL PRIMARY KEY COMMENT '领取记录ID',
    task_id      BIGINT UNSIGNED NOT NULL COMMENT '任务ID',
    user_id      BIGINT UNSIGNED NOT NULL COMMENT '领取用户ID',
    progress     INT             NOT NULL DEFAULT 0 COMMENT '完成进度 0-100',
    status       ENUM ('accepted','in_progress','submitted','completed','rejected') NOT NULL DEFAULT 'accepted' COMMENT '领取状态: accepted=已领取, in_progress=进行中, submitted=已提交, completed=已完成, rejected=已驳回',
    accepted_at  DATETIME(3)     NOT NULL DEFAULT CURRENT_TIMESTAMP(3) COMMENT '领取时间',
    completed_at DATETIME(3)     NULL COMMENT '完成时间',
    CONSTRAINT uk_task_acceptance_task_user UNIQUE (task_id, user_id),
    INDEX idx_task_acceptance_user (user_id)
) ENGINE = InnoDB
  CHARSET = utf8mb4
  ROW_FORMAT = DYNAMIC COMMENT '任务领取记录表';
