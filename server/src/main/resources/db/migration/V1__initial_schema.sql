-- ============================================================
-- V1: Initial Schema — ZhihuiDangjian 全新 schema
-- 变更概要:
--   - 统一 charset=utf8mb4, row_format=DYNAMIC
--   - 统一 datetime(3) 时间类型
--   - 主键 bigint unsigned, 由 MyBatis-Plus ASSIGN_ID 生成
--   - 索引用 idx_{table}_{column} 命名
--   - user_permission 合并到 user 表
--   - content_block 拆分为 chapter_content_block + article_content_block
--   - 修复 user_quiz_answer 外键引用错误
-- ============================================================

-- -----------------------------------------------------------
-- 1. universities — 学校
-- -----------------------------------------------------------
CREATE TABLE universities
(
    id         VARCHAR(10)  NOT NULL PRIMARY KEY COMMENT '学校编码',
    name       VARCHAR(100) NOT NULL COMMENT '学校名称',
    created_at DATETIME(3)  NOT NULL DEFAULT CURRENT_TIMESTAMP(3) COMMENT '创建时间',
    updated_at DATETIME(3)  NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3) COMMENT '更新时间'
) ENGINE = InnoDB
  CHARSET = utf8mb4
  ROW_FORMAT = DYNAMIC COMMENT '学校表';

-- -----------------------------------------------------------
-- 2. user — 系统用户
-- -----------------------------------------------------------
CREATE TABLE user
(
    id               BIGINT UNSIGNED NOT NULL PRIMARY KEY COMMENT '用户ID',
    university_id    VARCHAR(10)     NULL COMMENT '学校编码',
    username         VARCHAR(50)     NOT NULL COMMENT '用户名',
    password         VARCHAR(255)    NOT NULL COMMENT '加密密码（BCrypt）',
    user_type        ENUM ('学生','学校','管理员') DEFAULT '学生' NOT NULL COMMENT '用户类型',
    real_name        VARCHAR(50)     NOT NULL COMMENT '真实姓名',
    id_card          VARCHAR(18)     NULL COMMENT '身份证号码',
    party_member_id  VARCHAR(30)     NULL COMMENT '党员编号',
    party_status     ENUM ('正式党员','预备党员','发展对象','积极分子','群众') DEFAULT '群众' NOT NULL COMMENT '政治面貌',
    branch_name      VARCHAR(100)    NULL COMMENT '所属党支部名称',
    join_party_date  DATETIME(3)     NULL COMMENT '入党时间',
    status           ENUM ('active','inactive','banned') DEFAULT 'active' NOT NULL COMMENT '账户状态',
    email            VARCHAR(100)    NULL COMMENT '邮箱',
    phone            VARCHAR(20)     NULL COMMENT '手机号',
    permission_level INT             NOT NULL DEFAULT 9 COMMENT '权限等级: 0=最高, 9=最低',
    created_at       DATETIME(3)     NOT NULL DEFAULT CURRENT_TIMESTAMP(3) COMMENT '创建时间',
    updated_at       DATETIME(3)     NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3) COMMENT '更新时间',
    CONSTRAINT uk_user_email UNIQUE (email),
    CONSTRAINT uk_user_id_card UNIQUE (id_card),
    CONSTRAINT uk_user_party_member_id UNIQUE (party_member_id),
    CONSTRAINT uk_user_phone UNIQUE (phone),
    CONSTRAINT uk_user_username UNIQUE (username)
) ENGINE = InnoDB
  CHARSET = utf8mb4
  ROW_FORMAT = DYNAMIC COMMENT '系统用户表';

-- -----------------------------------------------------------
-- 3. category — 分类（树形结构）
-- -----------------------------------------------------------
CREATE TABLE category
(
    id            BIGINT UNSIGNED NOT NULL PRIMARY KEY COMMENT '分类ID',
    university_id VARCHAR(10)     NOT NULL COMMENT '学校编码',
    name          VARCHAR(50)     NOT NULL COMMENT '分类名称',
    level         INT UNSIGNED    NOT NULL DEFAULT 1 COMMENT '目录层级，根目录为1',
    description   VARCHAR(255)    NULL COMMENT '分类描述',
    parent_id     BIGINT UNSIGNED NULL DEFAULT 0 COMMENT '父分类ID',
    sort_order    INT UNSIGNED    NOT NULL DEFAULT 0 COMMENT '排序值，越小越靠前',
    status        TINYINT(1)      NOT NULL DEFAULT 1 COMMENT '状态: 1=正常, 0=禁用',
    created_at    DATETIME(3)     NOT NULL DEFAULT CURRENT_TIMESTAMP(3) COMMENT '创建时间',
    updated_at    DATETIME(3)     NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3) COMMENT '更新时间',
    CONSTRAINT uk_category_parent_name UNIQUE (parent_id, name)
) ENGINE = InnoDB
  CHARSET = utf8mb4
  ROW_FORMAT = DYNAMIC COMMENT '课程分类表（树形结构）';

CREATE INDEX IF NOT EXISTS idx_category_university_parent ON category (university_id, parent_id);
CREATE INDEX IF NOT EXISTS idx_category_parent ON category (parent_id);

-- -----------------------------------------------------------
-- 4. resource_meta — 统一资源元数据
-- -----------------------------------------------------------
CREATE TABLE resource_meta
(
    id            BIGINT UNSIGNED NOT NULL PRIMARY KEY COMMENT '资源ID',
    uploader_id   BIGINT UNSIGNED NULL COMMENT '上传者用户ID',
    original_name VARCHAR(255)    NOT NULL COMMENT '原始文件名',
    hash          VARCHAR(128)    NULL COMMENT '文件内容的SHA-256哈希值',
    object_key    VARCHAR(128)    NOT NULL COMMENT 'COS Object Key',
    resource_type TINYINT         NOT NULL COMMENT '资源类型: 0=图片, 1=视频',
    status        INT             NOT NULL COMMENT '状态: 0=上传中, 1=公开, 2=隐藏',
    created_at    DATETIME(3)     NOT NULL DEFAULT CURRENT_TIMESTAMP(3) COMMENT '创建时间',
    updated_at    DATETIME(3)     NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3) COMMENT '更新时间',
    CONSTRAINT uk_resource_meta_hash UNIQUE (hash),
    CONSTRAINT fk_resource_meta_uploader FOREIGN KEY (uploader_id) REFERENCES user (id)
        ON UPDATE CASCADE ON DELETE SET NULL
) ENGINE = InnoDB
  CHARSET = utf8mb4
  ROW_FORMAT = DYNAMIC COMMENT '统一资源元数据表';

CREATE INDEX IF NOT EXISTS idx_resource_meta_uploader ON resource_meta (uploader_id);

-- -----------------------------------------------------------
-- 5. course — 课程
-- -----------------------------------------------------------
CREATE TABLE course
(
    id                 BIGINT UNSIGNED NOT NULL PRIMARY KEY COMMENT '课程ID',
    title              VARCHAR(100)    NOT NULL COMMENT '课程标题',
    description        TEXT            NULL COMMENT '课程描述',
    cover_image_id     BIGINT UNSIGNED NULL COMMENT '课程封面图资源ID',
    difficulty         ENUM ('beginner','intermediate','advanced') DEFAULT 'beginner' NULL COMMENT '课程难度',
    estimated_duration INT UNSIGNED    NULL DEFAULT 0 COMMENT '课程预估总时长（分钟）',
    creator_id         BIGINT UNSIGNED NOT NULL COMMENT '课程创建者ID',
    enrollment_count   INT UNSIGNED    NOT NULL DEFAULT 0 COMMENT '学习人数',
    average_rating     DECIMAL(3, 2)   NOT NULL DEFAULT 0.00 COMMENT '平均评分',
    is_published       TINYINT(1)      NULL DEFAULT 0 COMMENT '是否已发布: 0=草稿, 1=已发布',
    published_at       DATETIME(3)     NULL COMMENT '发布时间',
    created_at         DATETIME(3)     NOT NULL DEFAULT CURRENT_TIMESTAMP(3) COMMENT '创建时间',
    updated_at         DATETIME(3)     NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3) COMMENT '更新时间',
    CONSTRAINT fk_course_cover FOREIGN KEY (cover_image_id) REFERENCES resource_meta (id)
        ON UPDATE CASCADE ON DELETE SET NULL,
    CONSTRAINT fk_course_creator FOREIGN KEY (creator_id) REFERENCES user (id)
        ON UPDATE CASCADE ON DELETE RESTRICT
) ENGINE = InnoDB
  CHARSET = utf8mb4
  ROW_FORMAT = DYNAMIC COMMENT '课程信息表';

CREATE FULLTEXT INDEX IF NOT EXISTS ft_course_title_desc ON course (title, description);
CREATE INDEX IF NOT EXISTS idx_course_cover ON course (cover_image_id);
CREATE INDEX IF NOT EXISTS idx_course_creator ON course (creator_id);
CREATE INDEX IF NOT EXISTS idx_course_published ON course (is_published, published_at);

-- -----------------------------------------------------------
-- 6. category_course — 分类-课程关联
-- -----------------------------------------------------------
CREATE TABLE category_course
(
    category_id BIGINT UNSIGNED NOT NULL COMMENT '分类ID',
    course_id   BIGINT UNSIGNED NOT NULL PRIMARY KEY COMMENT '课程ID',
    created_at  DATETIME(3)     NOT NULL DEFAULT CURRENT_TIMESTAMP(3) COMMENT '创建时间',
    updated_at  DATETIME(3)     NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3) COMMENT '更新时间',
    CONSTRAINT fk_category_course_category FOREIGN KEY (category_id) REFERENCES category (id)
        ON UPDATE CASCADE ON DELETE CASCADE,
    CONSTRAINT fk_category_course_course FOREIGN KEY (course_id) REFERENCES course (id)
        ON UPDATE CASCADE ON DELETE CASCADE
) ENGINE = InnoDB
  CHARSET = utf8mb4
  ROW_FORMAT = DYNAMIC COMMENT '分类-课程关联表';

CREATE INDEX IF NOT EXISTS idx_category_course_category ON category_course (category_id);

-- -----------------------------------------------------------
-- 7. chapter — 课程章节
-- -----------------------------------------------------------
CREATE TABLE chapter
(
    id             BIGINT UNSIGNED NOT NULL PRIMARY KEY COMMENT '章节ID',
    course_id      BIGINT UNSIGNED NOT NULL COMMENT '所属课程ID',
    title          VARCHAR(100)    NOT NULL COMMENT '章节标题',
    description    TEXT            NULL COMMENT '章节简介',
    duration       INT UNSIGNED    NULL DEFAULT 0 COMMENT '章节预估学习时长（秒）',
    order_index    INT UNSIGNED    NULL DEFAULT 0 COMMENT '排序序号',
    is_optional    TINYINT(1)      NULL DEFAULT 0 COMMENT '是否可选: 0=必学, 1=可选',
    chapter_status ENUM ('draft','published','archived') DEFAULT 'draft' NULL COMMENT '章节状态',
    created_at     DATETIME(3)     NOT NULL DEFAULT CURRENT_TIMESTAMP(3) COMMENT '创建时间',
    updated_at     DATETIME(3)     NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3) COMMENT '更新时间',
    CONSTRAINT fk_chapter_course FOREIGN KEY (course_id) REFERENCES course (id)
        ON UPDATE CASCADE ON DELETE CASCADE
) ENGINE = InnoDB
  CHARSET = utf8mb4
  ROW_FORMAT = DYNAMIC COMMENT '课程章节表';

CREATE FULLTEXT INDEX IF NOT EXISTS ft_chapter_title_desc ON chapter (title, description);
CREATE INDEX IF NOT EXISTS idx_chapter_course ON chapter (course_id);

-- -----------------------------------------------------------
-- 8. chapter_content_block — 章节内容块
-- -----------------------------------------------------------
CREATE TABLE chapter_content_block
(
    id           BIGINT UNSIGNED NOT NULL PRIMARY KEY COMMENT '内容块ID',
    chapter_id   BIGINT UNSIGNED NOT NULL COMMENT '所属章节ID',
    order_index  INT UNSIGNED    NOT NULL DEFAULT 0 COMMENT '排序序号',
    block_type   ENUM ('heading','paragraph','image','video','attachment') NOT NULL COMMENT '内容块类型',
    text_content LONGTEXT        NULL COMMENT '文本内容（heading/paragraph）',
    resource_id  BIGINT UNSIGNED NULL COMMENT '资源ID（image/video/attachment）',
    caption      VARCHAR(255)    NULL COMMENT '图片/视频/附件的标题说明',
    created_at   DATETIME(3)     NOT NULL DEFAULT CURRENT_TIMESTAMP(3) COMMENT '创建时间',
    updated_at   DATETIME(3)     NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3) COMMENT '更新时间',
    CONSTRAINT fk_chapter_content_block_chapter FOREIGN KEY (chapter_id) REFERENCES chapter (id)
        ON UPDATE CASCADE ON DELETE CASCADE,
    CONSTRAINT fk_chapter_content_block_resource FOREIGN KEY (resource_id) REFERENCES resource_meta (id)
        ON UPDATE CASCADE ON DELETE SET NULL
) ENGINE = InnoDB
  CHARSET = utf8mb4
  ROW_FORMAT = DYNAMIC COMMENT '章节内容块表';

CREATE FULLTEXT INDEX IF NOT EXISTS ft_chapter_cb_text ON chapter_content_block (text_content, caption);
CREATE INDEX IF NOT EXISTS idx_chapter_cb_chapter ON chapter_content_block (chapter_id, order_index);

-- -----------------------------------------------------------
-- 9. article — 图文文章
-- -----------------------------------------------------------
CREATE TABLE article
(
    id           BIGINT UNSIGNED NOT NULL PRIMARY KEY COMMENT '文章ID',
    author_id    BIGINT UNSIGNED NULL COMMENT '作者用户ID',
    title        VARCHAR(255)    NOT NULL COMMENT '文章标题',
    summary      TEXT            NULL COMMENT '文章摘要',
    status       ENUM ('draft','published','archived') DEFAULT 'draft' NOT NULL COMMENT '文章状态',
    published_at DATETIME(3)     NULL COMMENT '发布时间',
    created_at   DATETIME(3)     NOT NULL DEFAULT CURRENT_TIMESTAMP(3) COMMENT '创建时间',
    updated_at   DATETIME(3)     NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3) COMMENT '更新时间',
    CONSTRAINT fk_article_author FOREIGN KEY (author_id) REFERENCES user (id)
        ON UPDATE CASCADE ON DELETE SET NULL
) ENGINE = InnoDB
  CHARSET = utf8mb4
  ROW_FORMAT = DYNAMIC COMMENT '独立图文文章表';

CREATE INDEX IF NOT EXISTS idx_article_author ON article (author_id);
CREATE INDEX IF NOT EXISTS idx_article_status ON article (status);

-- -----------------------------------------------------------
-- 10. category_article — 分类-文章关联
-- -----------------------------------------------------------
CREATE TABLE category_article
(
    article_id  BIGINT UNSIGNED NOT NULL PRIMARY KEY COMMENT '文章ID',
    category_id BIGINT UNSIGNED NOT NULL COMMENT '分类ID',
    created_at  DATETIME(3)     NOT NULL DEFAULT CURRENT_TIMESTAMP(3) COMMENT '创建时间',
    updated_at  DATETIME(3)     NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3) COMMENT '更新时间',
    CONSTRAINT fk_category_article_article FOREIGN KEY (article_id) REFERENCES article (id)
        ON UPDATE CASCADE ON DELETE CASCADE,
    CONSTRAINT fk_category_article_category FOREIGN KEY (category_id) REFERENCES category (id)
        ON UPDATE CASCADE ON DELETE CASCADE
) ENGINE = InnoDB
  CHARSET = utf8mb4
  ROW_FORMAT = DYNAMIC COMMENT '分类-文章关联表';

CREATE INDEX IF NOT EXISTS idx_category_article_category ON category_article (category_id);

-- -----------------------------------------------------------
-- 11. article_content_block — 文章内容块
-- -----------------------------------------------------------
CREATE TABLE article_content_block
(
    id           BIGINT UNSIGNED NOT NULL PRIMARY KEY COMMENT '内容块ID',
    article_id   BIGINT UNSIGNED NOT NULL COMMENT '所属文章ID',
    order_index  INT UNSIGNED    NOT NULL DEFAULT 0 COMMENT '排序序号',
    block_type   ENUM ('heading','paragraph','image','video','attachment') NOT NULL COMMENT '内容块类型',
    text_content LONGTEXT        NULL COMMENT '文本内容（heading/paragraph）',
    resource_id  BIGINT UNSIGNED NULL COMMENT '资源ID（image/video/attachment）',
    caption      VARCHAR(255)    NULL COMMENT '图片/视频/附件的标题说明',
    created_at   DATETIME(3)     NOT NULL DEFAULT CURRENT_TIMESTAMP(3) COMMENT '创建时间',
    updated_at   DATETIME(3)     NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3) COMMENT '更新时间',
    CONSTRAINT fk_article_content_block_article FOREIGN KEY (article_id) REFERENCES article (id)
        ON UPDATE CASCADE ON DELETE CASCADE,
    CONSTRAINT fk_article_content_block_resource FOREIGN KEY (resource_id) REFERENCES resource_meta (id)
        ON UPDATE CASCADE ON DELETE SET NULL
) ENGINE = InnoDB
  CHARSET = utf8mb4
  ROW_FORMAT = DYNAMIC COMMENT '文章内容块表';

CREATE FULLTEXT INDEX IF NOT EXISTS ft_article_cb_text ON article_content_block (text_content, caption);
CREATE INDEX IF NOT EXISTS idx_article_cb_article ON article_content_block (article_id, order_index);

-- -----------------------------------------------------------
-- 12. quiz — 章节测试题
-- -----------------------------------------------------------
CREATE TABLE quiz
(
    id            BIGINT UNSIGNED NOT NULL PRIMARY KEY COMMENT '试题ID',
    chapter_id    BIGINT UNSIGNED NOT NULL COMMENT '所属章节ID',
    question      TEXT            NOT NULL COMMENT '问题题干',
    question_type ENUM ('single_choice','multiple_choice','true_false','short_answer') DEFAULT 'single_choice' NULL COMMENT '题目类型',
    score         TINYINT UNSIGNED NULL DEFAULT 1 COMMENT '题目分值',
    difficulty    ENUM ('easy','medium','hard') DEFAULT 'medium' NULL COMMENT '题目难度',
    explanation   TEXT            NULL COMMENT '答案解析',
    is_active     TINYINT(1)      NULL DEFAULT 1 COMMENT '是否启用',
    created_at    DATETIME(3)     NOT NULL DEFAULT CURRENT_TIMESTAMP(3) COMMENT '创建时间',
    updated_at    DATETIME(3)     NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3) COMMENT '更新时间',
    CONSTRAINT fk_quiz_chapter FOREIGN KEY (chapter_id) REFERENCES chapter (id)
        ON UPDATE CASCADE ON DELETE CASCADE
) ENGINE = InnoDB
  CHARSET = utf8mb4
  ROW_FORMAT = DYNAMIC COMMENT '章节测试题表';

CREATE INDEX IF NOT EXISTS idx_quiz_chapter ON quiz (chapter_id);

-- -----------------------------------------------------------
-- 13. quiz_option — 试题选项
-- -----------------------------------------------------------
CREATE TABLE quiz_option
(
    id          BIGINT UNSIGNED NOT NULL PRIMARY KEY COMMENT '选项ID',
    quiz_id     BIGINT UNSIGNED NOT NULL COMMENT '所属试题ID',
    option_text TEXT            NOT NULL COMMENT '选项内容',
    is_correct  TINYINT(1)      NOT NULL DEFAULT 0 COMMENT '是否为正确答案: 0=否, 1=是',
    order_index VARCHAR(10)     NULL COMMENT '选项标签（A, B, C, D...）',
    created_at  DATETIME(3)     NOT NULL DEFAULT CURRENT_TIMESTAMP(3) COMMENT '创建时间',
    updated_at  DATETIME(3)     NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3) COMMENT '更新时间',
    CONSTRAINT fk_quiz_option_quiz FOREIGN KEY (quiz_id) REFERENCES quiz (id)
        ON UPDATE CASCADE ON DELETE CASCADE
) ENGINE = InnoDB
  CHARSET = utf8mb4
  ROW_FORMAT = DYNAMIC COMMENT '试题选项表';

CREATE INDEX IF NOT EXISTS idx_quiz_option_quiz ON quiz_option (quiz_id);

-- -----------------------------------------------------------
-- 14. user_chapter_progress — 用户章节学习进度
-- -----------------------------------------------------------
CREATE TABLE user_chapter_progress
(
    id              BIGINT UNSIGNED NOT NULL PRIMARY KEY COMMENT '进度ID',
    user_id         BIGINT UNSIGNED NOT NULL COMMENT '用户ID',
    chapter_id      BIGINT UNSIGNED NOT NULL COMMENT '章节ID',
    progress        TINYINT UNSIGNED NOT NULL DEFAULT 0 COMMENT '学习进度百分比（0-100）',
    status          ENUM ('not_started','in_progress','completed') DEFAULT 'not_started' NOT NULL COMMENT '完成状态',
    first_viewed_at DATETIME(3)     NULL COMMENT '首次查看时间',
    completed_at    DATETIME(3)     NULL COMMENT '完成时间',
    updated_at      DATETIME(3)     NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3) COMMENT '更新时间',
    CONSTRAINT uk_user_chapter_progress UNIQUE (user_id, chapter_id),
    CONSTRAINT fk_user_chapter_progress_user FOREIGN KEY (user_id) REFERENCES user (id)
        ON UPDATE CASCADE ON DELETE CASCADE,
    CONSTRAINT fk_user_chapter_progress_chapter FOREIGN KEY (chapter_id) REFERENCES chapter (id)
        ON UPDATE CASCADE ON DELETE CASCADE
) ENGINE = InnoDB
  CHARSET = utf8mb4
  ROW_FORMAT = DYNAMIC COMMENT '用户章节学习进度表';

CREATE INDEX IF NOT EXISTS idx_user_chapter_progress_chapter ON user_chapter_progress (chapter_id);

-- -----------------------------------------------------------
-- 15. user_learning_record — 用户学习行为记录
-- -----------------------------------------------------------
CREATE TABLE user_learning_record
(
    id          BIGINT UNSIGNED NOT NULL PRIMARY KEY COMMENT '记录ID',
    user_id     BIGINT UNSIGNED NOT NULL COMMENT '用户ID',
    chapter_id  BIGINT UNSIGNED NOT NULL COMMENT '章节ID',
    start_time  DATETIME(3)     NOT NULL COMMENT '学习开始时间',
    end_time    DATETIME(3)     NULL COMMENT '学习结束时间',
    duration    INT UNSIGNED    NULL DEFAULT 0 COMMENT '学习时长（秒）',
    device_type VARCHAR(50)     NULL COMMENT '学习设备类型（web/android/ios）',
    created_at  DATETIME(3)     NOT NULL DEFAULT CURRENT_TIMESTAMP(3) COMMENT '创建时间',
    CONSTRAINT fk_user_learning_record_user FOREIGN KEY (user_id) REFERENCES user (id)
        ON UPDATE CASCADE ON DELETE CASCADE,
    CONSTRAINT fk_user_learning_record_chapter FOREIGN KEY (chapter_id) REFERENCES chapter (id)
        ON UPDATE CASCADE ON DELETE CASCADE
) ENGINE = InnoDB
  CHARSET = utf8mb4
  ROW_FORMAT = DYNAMIC COMMENT '用户学习行为记录表';

CREATE INDEX IF NOT EXISTS idx_user_learning_record_chapter ON user_learning_record (chapter_id);
CREATE INDEX IF NOT EXISTS idx_user_learning_record_user_chapter ON user_learning_record (user_id, chapter_id);
CREATE INDEX IF NOT EXISTS idx_user_learning_record_user_time ON user_learning_record (user_id, start_time);

-- -----------------------------------------------------------
-- 16. user_quiz_answer — 用户答题记录
--   注意: quiz_id → quiz.id, option_id → quiz_option.id (已修复)
-- -----------------------------------------------------------
CREATE TABLE user_quiz_answer
(
    id             BIGINT UNSIGNED NOT NULL PRIMARY KEY COMMENT '答题记录ID',
    user_id        BIGINT UNSIGNED NOT NULL COMMENT '用户ID',
    quiz_id        BIGINT UNSIGNED NOT NULL COMMENT '试题ID',
    option_id      BIGINT UNSIGNED NOT NULL COMMENT '所选选项ID',
    user_answer    JSON            NULL COMMENT '用户答案（JSON格式）',
    is_correct     TINYINT(1)      NOT NULL DEFAULT 0 COMMENT '是否答对: 0=错, 1=对, 2=部分对',
    score_obtained TINYINT UNSIGNED NULL DEFAULT 0 COMMENT '获得分数',
    time_spent     INT UNSIGNED    NULL DEFAULT 0 COMMENT '答题耗时（秒）',
    session_id     VARCHAR(64)     NULL COMMENT '答题会话ID',
    answer_time    DATETIME(3)     NOT NULL COMMENT '答题时间',
    CONSTRAINT fk_user_quiz_answer_user FOREIGN KEY (user_id) REFERENCES user (id)
        ON UPDATE CASCADE ON DELETE CASCADE,
    CONSTRAINT fk_user_quiz_answer_quiz FOREIGN KEY (quiz_id) REFERENCES quiz (id)
        ON UPDATE CASCADE ON DELETE CASCADE,
    CONSTRAINT fk_user_quiz_answer_option FOREIGN KEY (option_id) REFERENCES quiz_option (id)
        ON UPDATE CASCADE ON DELETE CASCADE
) ENGINE = InnoDB
  CHARSET = utf8mb4
  ROW_FORMAT = DYNAMIC COMMENT '用户答题记录表';

CREATE INDEX IF NOT EXISTS idx_user_quiz_answer_quiz ON user_quiz_answer (quiz_id);
CREATE INDEX IF NOT EXISTS idx_user_quiz_answer_option ON user_quiz_answer (option_id);
CREATE INDEX IF NOT EXISTS idx_user_quiz_answer_session ON user_quiz_answer (session_id);
CREATE INDEX IF NOT EXISTS idx_user_quiz_answer_user_quiz ON user_quiz_answer (user_id, quiz_id);
CREATE INDEX IF NOT EXISTS idx_user_quiz_answer_user_option ON user_quiz_answer (user_id, option_id);

-- -----------------------------------------------------------
-- 17. user_similarity — 用户相似度（协同过滤推荐）
-- -----------------------------------------------------------
CREATE TABLE user_similarity
(
    id                 BIGINT UNSIGNED NOT NULL PRIMARY KEY COMMENT '相似度记录ID',
    user_id1           BIGINT UNSIGNED NOT NULL COMMENT '用户1 ID（较小的ID）',
    user_id2           BIGINT UNSIGNED NOT NULL COMMENT '用户2 ID（较大的ID）',
    similarity_score   DECIMAL(4, 3)   NOT NULL COMMENT '相似度分数（-1.000 到 1.000）',
    similarity_type    ENUM ('learning_behavior','quiz_performance','course_preference','comprehensive') DEFAULT 'learning_behavior' NOT NULL COMMENT '相似度计算类型',
    calculation_params JSON            NULL COMMENT '计算参数快照',
    data_version       VARCHAR(32)     NULL COMMENT '数据版本',
    is_valid           TINYINT(1)      NULL DEFAULT 1 COMMENT '是否有效',
    expires_at         DATETIME(3)     NULL COMMENT '过期时间',
    updated_at         DATETIME(3)     NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3) COMMENT '更新时间',
    CONSTRAINT uk_user_similarity_pair UNIQUE (user_id1, user_id2),
    CONSTRAINT uk_user_similarity_pair_type UNIQUE (user_id1, user_id2, similarity_type),
    CONSTRAINT fk_user_similarity_user1 FOREIGN KEY (user_id1) REFERENCES user (id)
        ON UPDATE CASCADE ON DELETE CASCADE,
    CONSTRAINT fk_user_similarity_user2 FOREIGN KEY (user_id2) REFERENCES user (id)
        ON UPDATE CASCADE ON DELETE CASCADE
) ENGINE = InnoDB
  CHARSET = utf8mb4
  ROW_FORMAT = DYNAMIC COMMENT '用户相似度表（协同过滤推荐）';

CREATE INDEX IF NOT EXISTS idx_user_similarity_user1 ON user_similarity (user_id1, similarity_type, similarity_score DESC);
CREATE INDEX IF NOT EXISTS idx_user_similarity_user2 ON user_similarity (user_id2, similarity_type, similarity_score DESC);
