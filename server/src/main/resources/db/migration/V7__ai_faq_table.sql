CREATE TABLE IF NOT EXISTS ai_faq
(
    id         BIGINT UNSIGNED NOT NULL PRIMARY KEY COMMENT 'FAQ ID',
    keywords   VARCHAR(500)    NOT NULL COMMENT '触发关键词（逗号分隔）',
    question   VARCHAR(255)    NOT NULL COMMENT '问题摘要（可读）',
    answer     TEXT            NOT NULL COMMENT '预定义答案',
    enabled    TINYINT(1)      NOT NULL DEFAULT 1 COMMENT '0=禁用, 1=启用',
    sort       INT             NOT NULL DEFAULT 0 COMMENT '排序序号（越小优先级越高）',
    created_at DATETIME(3)     NOT NULL DEFAULT CURRENT_TIMESTAMP(3) COMMENT '创建时间',
    updated_at DATETIME(3)     NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3) COMMENT '更新时间'
) ENGINE = InnoDB CHARSET = utf8mb4 ROW_FORMAT = DYNAMIC COMMENT 'AI FAQ快速回复规则表';

CREATE INDEX idx_ai_faq_enabled_sort ON ai_faq (enabled, sort);
