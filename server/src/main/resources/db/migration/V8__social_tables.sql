-- V8: 社交模块 - 评论 + 点赞
CREATE TABLE IF NOT EXISTS comment (
    id BIGINT UNSIGNED NOT NULL PRIMARY KEY COMMENT '评论ID',
    target_type ENUM('article', 'course', 'chapter') NOT NULL,
    target_id BIGINT UNSIGNED NOT NULL,
    user_id BIGINT UNSIGNED NOT NULL,
    parent_id BIGINT UNSIGNED NULL,
    content TEXT NOT NULL,
    status ENUM('published', 'pending_review', 'hidden', 'deleted') DEFAULT 'published' NOT NULL,
    like_count INT UNSIGNED NOT NULL DEFAULT 0,
    reply_count INT UNSIGNED NOT NULL DEFAULT 0,
    created_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
    updated_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3),
    CONSTRAINT fk_comment_user FOREIGN KEY (user_id) REFERENCES user(id) ON DELETE CASCADE,
    CONSTRAINT fk_comment_parent FOREIGN KEY (parent_id) REFERENCES comment(id) ON DELETE CASCADE
) ENGINE=InnoDB CHARSET=utf8mb4 ROW_FORMAT=DYNAMIC;

CREATE INDEX idx_comment_target ON comment (target_type, target_id, status);
CREATE INDEX idx_comment_user ON comment (user_id);
CREATE INDEX idx_comment_parent ON comment (parent_id);
CREATE INDEX idx_comment_created ON comment (target_type, target_id, created_at DESC);

CREATE TABLE IF NOT EXISTS user_like (
    id BIGINT UNSIGNED NOT NULL PRIMARY KEY COMMENT '点赞ID',
    user_id BIGINT UNSIGNED NOT NULL,
    target_type ENUM('comment', 'article', 'course') NOT NULL,
    target_id BIGINT UNSIGNED NOT NULL,
    created_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
    CONSTRAINT fk_like_user FOREIGN KEY (user_id) REFERENCES user(id) ON DELETE CASCADE,
    CONSTRAINT uk_user_like UNIQUE (user_id, target_type, target_id)
) ENGINE=InnoDB CHARSET=utf8mb4 ROW_FORMAT=DYNAMIC;

CREATE INDEX idx_like_target ON user_like (target_type, target_id);
