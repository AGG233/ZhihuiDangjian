-- ============================================================
-- V3: AI agent tables (ai_chat_message, ai_prompts, ai_skill)
-- 从 services/ai/src/main/resources/sql/ai-agent-schema.sql 整合
-- ============================================================

-- -----------------------------------------------------------
-- ai_prompts — AI 提示词模板
-- -----------------------------------------------------------
CREATE TABLE IF NOT EXISTS ai_prompts
(
    id         BIGINT UNSIGNED NOT NULL PRIMARY KEY COMMENT '提示词ID',
    agent_type VARCHAR(32)     NOT NULL DEFAULT 'COMMON' COMMENT 'Agent类型',
    category   VARCHAR(50)     NULL DEFAULT '通用' COMMENT '提示词类别（翻译、周报、绘图等）',
    name       VARCHAR(50)     NOT NULL COMMENT '提示词名称',
    role       ENUM ('system','user') NOT NULL COMMENT '角色: system 或 user',
    content    TEXT            NOT NULL COMMENT '提示词内容',
    enabled    TINYINT(1)      NOT NULL DEFAULT 1 COMMENT '0=禁用, 1=启用',
    sort       INT             NOT NULL DEFAULT 0 COMMENT '排序序号',
    created_at DATETIME(3)     NOT NULL DEFAULT CURRENT_TIMESTAMP(3) COMMENT '创建时间',
    updated_at DATETIME(3)     NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3) COMMENT '更新时间'
) ENGINE = InnoDB
  CHARSET = utf8mb4
  ROW_FORMAT = DYNAMIC COMMENT 'AI提示词模板表';

-- -----------------------------------------------------------
-- ai_skill — AI 技能定义
-- -----------------------------------------------------------
CREATE TABLE IF NOT EXISTS ai_skill
(
    id          VARCHAR(64)     NOT NULL PRIMARY KEY COMMENT '技能ID',
    agent_type  VARCHAR(32)     NOT NULL COMMENT 'Agent类型',
    name        VARCHAR(64)     NOT NULL COMMENT '技能名称',
    description VARCHAR(1024)   NOT NULL COMMENT '技能描述',
    content     TEXT            NOT NULL COMMENT '技能内容',
    enabled     TINYINT(1)      NOT NULL DEFAULT 1 COMMENT '0=禁用, 1=启用',
    sort        INT             NOT NULL DEFAULT 0 COMMENT '排序序号',
    tool_groups JSON            NULL COMMENT '工具组配置',
    created_at  DATETIME(3)     NOT NULL DEFAULT CURRENT_TIMESTAMP(3) COMMENT '创建时间',
    updated_at  DATETIME(3)     NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3) COMMENT '更新时间',
    CONSTRAINT uk_ai_skill_name UNIQUE (name)
) ENGINE = InnoDB
  CHARSET = utf8mb4
  ROW_FORMAT = DYNAMIC COMMENT 'AI技能定义表';

-- -----------------------------------------------------------
-- ai_chat_message — AI 聊天记录
-- -----------------------------------------------------------
CREATE TABLE IF NOT EXISTS ai_chat_message
(
    id           BIGINT UNSIGNED NOT NULL PRIMARY KEY COMMENT '消息ID',
    session_id   VARCHAR(64)     NOT NULL COMMENT '会话ID',
    user_id      BIGINT UNSIGNED NOT NULL COMMENT '用户ID',
    agent_type   VARCHAR(32)     NULL COMMENT 'Agent类型',
    sender_type  ENUM ('user','ai') NOT NULL COMMENT '发送方',
    content      LONGTEXT        NOT NULL COMMENT '消息内容',
    message_type ENUM ('text','kg-card','guide','mixed') DEFAULT 'text' NOT NULL COMMENT '消息类型',
    metadata     JSON            NULL COMMENT '元数据（tokens、模型、用户反馈等）',
    created_at   DATETIME(3)     NOT NULL DEFAULT CURRENT_TIMESTAMP(3) COMMENT '创建时间',
    CONSTRAINT fk_ai_chat_message_user FOREIGN KEY (user_id) REFERENCES user (id)
        ON UPDATE CASCADE ON DELETE CASCADE
) ENGINE = InnoDB
  CHARSET = utf8mb4
  ROW_FORMAT = DYNAMIC COMMENT 'AI智能问答聊天记录表';

CREATE INDEX idx_ai_chat_message_session ON ai_chat_message (session_id);
CREATE INDEX idx_ai_chat_message_user_time ON ai_chat_message (user_id, created_at);
