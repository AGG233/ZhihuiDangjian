create table zhdj.ai_prompts
(
    id         int auto_increment
        primary key,
    agent_type varchar(32) default 'COMMON'          not null,
    category   varchar(50) default '通用'            null comment '提示词类别，如：翻译、周报、绘图',
    name       varchar(50)                           not null comment '提示词名称',
    role       enum ('system', 'user')               not null comment '角色：system 或 user',
    content    text                                  not null comment '提示词内容',
    updated_at timestamp   default CURRENT_TIMESTAMP null on update CURRENT_TIMESTAMP,
    created_at timestamp   default (now())           not null,
    enabled    tinyint     default 1                 not null comment '0:disabled 1:enabled',
    sort       int         default 0                 not null
)
    charset = utf8mb4;

create table zhdj.ai_skill
(
    id          varchar(64)                          not null
        primary key,
    agent_type  varchar(32)                          not null,
    name        varchar(64)                          not null,
    description varchar(1024)                        not null,
    content     text                                 not null,
    enabled     tinyint(1) default 1                 not null,
    sort        int        default 0                 not null,
    tool_groups json                                 null,
    created_at  datetime   default CURRENT_TIMESTAMP not null,
    updated_at  datetime   default CURRENT_TIMESTAMP not null on update CURRENT_TIMESTAMP,
    constraint uk_ai_skill_name
        unique (name)
);

create table zhdj.category
(
    id            bigint unsigned                           not null comment '分类唯一ID'
        primary key,
    name          varchar(50)                               not null comment '分类名称',
    level         int unsigned    default '1'               not null comment '目录层级，根目录为1',
    description   varchar(255)                              null comment '分类描述',
    parent_id     bigint unsigned default '0'               null comment '父分类ID（用于支持多级分类）',
    sort_order    int unsigned    default '0'               not null comment '同级分类下的排序值，越小越靠前',
    status        tinyint(1)      default 1                 not null comment '状态: 1-正常, 0-禁用',
    created_at    datetime        default CURRENT_TIMESTAMP not null comment '创建时间',
    updated_at    datetime        default CURRENT_TIMESTAMP not null on update CURRENT_TIMESTAMP comment '更新时间',
    university_id bigint                                    not null,
    constraint uk_parent_id_name
        unique (parent_id, name)
)
    comment '课程分类表（树形结构）' row_format = DYNAMIC;

create index ctg_parent_id
    on zhdj.category (university_id, parent_id);

create index idx_parent_id
    on zhdj.category (parent_id);

create table zhdj.universities
(
    id   varchar(10)  not null
        primary key,
    name varchar(100) not null
)
    row_format = DYNAMIC;

create table zhdj.user
(
    id              bigint unsigned auto_increment comment '用户唯一ID'
        primary key,
    university_id   bigint                                                                                  null comment '学校ID',
    username        varchar(50)                                                                             not null comment '用户名（用于登录）',
    password        varchar(255)                                                                            not null comment '加密后的密码（BCrypt）',
    user_type       enum ('学生', '学校', '管理员')                               default '学生'            not null comment '用户类型',
    real_name       varchar(50)                                                                             not null comment '真实姓名',
    id_card         varchar(18)                                                                             null comment '身份证号码',
    party_member_id varchar(30)                                                                             null comment '党员编号',
    party_status    enum ('正式党员', '预备党员', '发展对象', '积极分子', '群众') default '群众'            not null comment '政治面貌',
    branch_name     varchar(100)                                                                            null comment '所属党支部名称',
    join_party_date timestamp                                                                               null comment '入党时间',
    status          enum ('active', 'inactive', 'banned')                         default 'active'          not null comment '账户状态',
    email           varchar(100)                                                                            null comment '邮箱',
    phone           varchar(20)                                                                             null comment '手机号',
    created_at      timestamp                                                     default CURRENT_TIMESTAMP null comment '账户创建时间',
    updated_at      timestamp                                                     default CURRENT_TIMESTAMP null on update CURRENT_TIMESTAMP comment '最后更新时间',
    constraint uk_email
        unique (email),
    constraint uk_id_card
        unique (id_card),
    constraint uk_party_member_id
        unique (party_member_id),
    constraint uk_phone
        unique (phone),
    constraint uk_username
        unique (username)
)
    comment '系统用户表' row_format = DYNAMIC;

create table zhdj.ai_chat_message
(
    id           bigint unsigned                                                      not null comment '消息ID'
        primary key,
    session_id   varchar(64)                                                          not null comment '会话ID，关联同一会话的多轮对话',
    user_id      bigint unsigned                                                      not null comment '用户ID',
    agent_type   varchar(32)                                                          null,
    sender_type  enum ('user', 'ai')                                                  not null comment '发送方',
    content      longtext                                                             not null comment '消息内容',
    message_type enum ('text', 'kg-card', 'guide', 'mixed') default 'text'            not null comment '消息类型',
    metadata     json                                                                 null comment '元数据（JSON，用于存储AI响应耗时、tokens、模型、用户反馈等）',
    created_at   timestamp                                  default CURRENT_TIMESTAMP null comment '消息创建时间',
    constraint fk_chat_user
        foreign key (user_id) references zhdj.user (id)
            on update cascade on delete cascade
)
    comment 'AI智能问答聊天记录表' row_format = DYNAMIC;

create index idx_session
    on zhdj.ai_chat_message (session_id);

create index idx_user_time
    on zhdj.ai_chat_message (user_id, created_at);

create table zhdj.article
(
    id           bigint unsigned                                                   not null comment '文章唯一ID'
        primary key,
    author_id    bigint unsigned                                                   null comment '作者的用户ID',
    title        varchar(255)                                                      not null comment '文章标题',
    summary      text                                                              null comment '文章摘要',
    status       enum ('draft', 'published', 'archived') default 'draft'           not null comment '文章状态',
    published_at timestamp                                                         null comment '发布时间',
    created_at   timestamp                               default CURRENT_TIMESTAMP null,
    updated_at   timestamp                               default CURRENT_TIMESTAMP null on update CURRENT_TIMESTAMP,
    constraint fk_article_author
        foreign key (author_id) references zhdj.user (id)
            on update cascade on delete set null
)
    comment '独立图文文章表' row_format = DYNAMIC;

create index idx_author
    on zhdj.article (author_id);

create index idx_status
    on zhdj.article (status);

create table zhdj.category_article
(
    article_id  bigint unsigned not null
        primary key,
    category_id bigint unsigned not null,
    constraint fk_category_article_article_id
        foreign key (article_id) references zhdj.article (id)
            on update cascade on delete cascade,
    constraint fk_category_article_category_id
        foreign key (category_id) references zhdj.category (id)
            on update cascade on delete cascade
)
    row_format = DYNAMIC;

create table zhdj.resource_meta
(
    id            bigint unsigned auto_increment
        primary key,
    uploader_id   bigint unsigned null comment '上传者用户ID',
    original_name varchar(255)    not null comment '原始文件名',
    hash          varchar(128)    null comment '文件内容的SHA-256哈希值',
    objectkey     varchar(128)    not null comment 'COS Object Key',
    resource_type tinyint         not null comment '0:image,1:video',
    status        int             not null comment '0:upload,1:public,2:hidden',
    constraint uk_hash
        unique (hash),
    constraint fk_resource_uploader
        foreign key (uploader_id) references zhdj.user (id)
            on update cascade on delete set null
)
    comment '统一资源元数据表' row_format = DYNAMIC;

create table zhdj.content_block
(
    id           bigint unsigned auto_increment comment '内容块唯一ID'
        primary key,
    parent_id    bigint unsigned                                               not null comment '所属父实体ID (如 chapter_id 或 article_id)',
    parent_type  enum ('chapter', 'article')                                   not null comment '父实体类型',
    order_index  int unsigned default '0'                                      not null comment '内容块在父实体内的排序',
    block_type   enum ('heading', 'paragraph', 'image', 'video', 'attachment') not null comment '内容块的业务类型',
    text_content longtext                                                      null comment '文本内容 (当 block_type 为 heading 或 paragraph)',
    resource_id  bigint unsigned                                               null comment '资源ID (当 block_type 为 image, video, attachment)',
    caption      varchar(255)                                                  null comment '图片/视频/附件的标题或说明',
    created_at   timestamp    default CURRENT_TIMESTAMP                        null,
    updated_at   timestamp    default CURRENT_TIMESTAMP                        null on update CURRENT_TIMESTAMP,
    constraint fk_content_block_resource
        foreign key (resource_id) references zhdj.resource_meta (id)
            on update cascade on delete set null
)
    comment '通用内容块表 (用于构建章节和文章)' row_format = DYNAMIC;

create fulltext index ft_index
    on zhdj.content_block (text_content, caption);

create index idx_parent
    on zhdj.content_block (parent_id, parent_type, order_index);

create table zhdj.course
(
    id                 bigint unsigned auto_increment comment '课程唯一ID'
        primary key,
    title              varchar(100)                                                            not null comment '课程标题',
    description        text                                                                    null comment '课程描述',
    cover_image_hash   bigint unsigned                                                         null comment '课程封面图资源哈希值',
    difficulty         enum ('beginner', 'intermediate', 'advanced') default 'beginner'        null comment '课程难度',
    estimated_duration int unsigned                                  default '0'               null comment '课程预估总时长（分钟）',
    creator_id         bigint unsigned                                                         not null comment '课程创建者ID (关联user表)',
    enrollment_count   int unsigned                                  default '0'               not null comment '学习人数',
    average_rating     decimal(3, 2)                                 default 0.00              not null comment '平均评分',
    is_published       tinyint(1)                                    default 0                 null comment '是否已发布（0:草稿, 1:已发布）',
    published_at       timestamp                                                               null comment '发布时间',
    created_at         timestamp                                     default CURRENT_TIMESTAMP null,
    updated_at         timestamp                                     default CURRENT_TIMESTAMP null on update CURRENT_TIMESTAMP,
    constraint fk_course_cover
        foreign key (cover_image_hash) references zhdj.resource_meta (id)
            on update cascade,
    constraint fk_course_creator
        foreign key (creator_id) references zhdj.user (id)
            on update cascade
)
    comment '课程信息表' row_format = DYNAMIC;

create table zhdj.category_couse
(
    category_id bigint unsigned not null,
    course_id   bigint unsigned not null
        primary key,
    constraint fk_category_course_category_id
        foreign key (category_id) references zhdj.category (id)
            on update cascade on delete cascade,
    constraint fk_category_course_course_id
        foreign key (course_id) references zhdj.course (id)
            on update cascade on delete cascade
)
    row_format = DYNAMIC;

create table zhdj.chapter
(
    id             bigint unsigned auto_increment comment '章节唯一ID'
        primary key,
    course_id      bigint unsigned                                                   not null comment '章节所属课程',
    title          varchar(100)                                                      not null comment '章节标题',
    description    text                                                              null comment '章节简介',
    duration       int unsigned                            default '0'               null comment '章节预估学习时长（秒）',
    order_index    int unsigned                            default '0'               null comment '章节在课程中的排序序号',
    is_optional    tinyint(1)                              default 0                 null comment '是否可选章节（0:必学, 1:可选）',
    chapter_status enum ('draft', 'published', 'archived') default 'draft'           null comment '章节状态',
    created_at     timestamp                               default CURRENT_TIMESTAMP null,
    updated_at     timestamp                               default CURRENT_TIMESTAMP null on update CURRENT_TIMESTAMP,
    constraint fk_course
        foreign key (course_id) references zhdj.course (id)
            on update cascade on delete cascade
)
    comment '课程章节表 (支持视频/音频/图文)' row_format = DYNAMIC;

create fulltext index ft_index
    on zhdj.chapter (title, description);

create fulltext index ft_index
    on zhdj.course (title, description);

create index idx_cover_image_resource_id
    on zhdj.course (cover_image_hash);

create index idx_creator
    on zhdj.course (creator_id);

create index idx_published_status
    on zhdj.course (is_published, published_at);

create table zhdj.quiz
(
    id            bigint unsigned                                                                                   not null comment '试题唯一ID'
        primary key,
    chapter_id    bigint unsigned                                                                                   not null comment '所属章节ID',
    question      text                                                                                              not null comment '问题题干',
    question_type enum ('single_choice', 'multiple_choice', 'true_false', 'short_answer') default 'single_choice'   null comment '题目类型',
    score         tinyint unsigned                                                        default '1'               null comment '题目分值',
    difficulty    enum ('easy', 'medium', 'hard')                                         default 'medium'          null comment '题目难度',
    explanation   text                                                                                              null comment '答案解析',
    is_active     tinyint(1)                                                              default 1                 null comment '是否启用',
    created_at    timestamp                                                               default CURRENT_TIMESTAMP null,
    updated_at    timestamp                                                               default CURRENT_TIMESTAMP null on update CURRENT_TIMESTAMP,
    constraint fk_quiz_chapter_id
        foreign key (chapter_id) references zhdj.chapter (id)
            on update cascade
)
    comment '章节测试题表' row_format = DYNAMIC;

create index idx_chapter
    on zhdj.quiz (chapter_id);

create table zhdj.quiz_option
(
    id          bigint unsigned auto_increment comment '选项唯一ID'
        primary key,
    quiz_id     bigint unsigned      not null comment '所属试题ID',
    option_text text                 not null comment '选项内容',
    is_correct  tinyint(1) default 0 not null comment '是否为正确答案（0:否, 1:是）',
    order_index char                 null comment '选项标签（A, B, C...）',
    constraint fk_quiz_option_quiz_id
        foreign key (quiz_id) references zhdj.quiz (id)
)
    comment '试题选项表' row_format = DYNAMIC;

create index idx_quiz_id
    on zhdj.quiz_option (quiz_id);

create index idx_uploader_id
    on zhdj.resource_meta (uploader_id);

create table zhdj.user_chapter_progress
(
    id              bigint unsigned auto_increment
        primary key,
    user_id         bigint unsigned                                                            not null comment '用户ID',
    chapter_id      bigint unsigned                                                            not null comment '章节ID',
    progress        tinyint unsigned                                 default '0'               not null comment '学习进度百分比 (0-100)',
    status          enum ('not_started', 'in_progress', 'completed') default 'not_started'     not null comment '完成状态',
    first_viewed_at timestamp                                                                  null comment '首次查看时间',
    completed_at    timestamp                                                                  null comment '完成时间',
    updated_at      timestamp                                        default CURRENT_TIMESTAMP not null on update CURRENT_TIMESTAMP,
    constraint uk_user_chapter
        unique (user_id, chapter_id),
    constraint fk_user_chapter_progress_chapter_id
        foreign key (chapter_id) references zhdj.chapter (id)
            on update cascade,
    constraint fk_user_chapter_progress_user_id
        foreign key (user_id) references zhdj.user (id)
            on update cascade
)
    comment '用户章节学习进度表（状态存储）' row_format = DYNAMIC;

create index fk_progress_chapter
    on zhdj.user_chapter_progress (chapter_id);

create table zhdj.user_learning_record
(
    id          bigint unsigned auto_increment comment '记录唯一ID'
        primary key,
    user_id     bigint unsigned                        not null comment '用户ID',
    chapter_id  bigint unsigned                        not null comment '章节ID',
    start_time  datetime                               not null comment '学习开始时间',
    end_time    datetime                               null comment '学习结束时间',
    duration    int unsigned default '0'               null comment '本次学习实际时长（秒）',
    device_type varchar(50)                            null comment '学习设备类型（如：web, android, ios）',
    created_at  timestamp    default CURRENT_TIMESTAMP null,
    constraint fk_user_learning_record_chapter_id
        foreign key (chapter_id) references zhdj.chapter (id)
            on update cascade on delete cascade,
    constraint fk_user_learning_record_user_id
        foreign key (user_id) references zhdj.user (id)
            on update cascade
)
    comment '用户学习行为记录表（日志）' row_format = DYNAMIC;

create index fk_learning_record_chapter
    on zhdj.user_learning_record (chapter_id);

create index idx_user_chapter
    on zhdj.user_learning_record (user_id, chapter_id);

create index idx_user_time
    on zhdj.user_learning_record (user_id, start_time);

create table zhdj.user_permission
(
    user_id   bigint unsigned                                not null comment '用户id'
        primary key,
    level     int                             default 9      not null comment '用户所属类型的权限等级，0为最高级别，9为最低级',
    user_type enum ('学生', '教师', '管理员') default '学生' null comment '用户类型'
)
    row_format = DYNAMIC;

create table zhdj.user_quiz_answer
(
    id             bigint unsigned auto_increment comment '答题记录ID'
        primary key,
    user_id        bigint unsigned              not null comment '用户ID',
    option_id      bigint unsigned              not null comment '选项ID',
    quiz_id        bigint unsigned              not null comment '所属试题ID',
    user_answer    json                         not null comment '用户答案（JSON格式, 存储选项ID列表）',
    is_correct     tinyint(1)                   not null comment '是否完全答对（0:错, 1:对,2不完全对）',
    score_obtained tinyint unsigned default '0' null comment '获得分数',
    time_spent     int unsigned     default '0' null comment '答题耗时（秒）',
    session_id     varchar(64)                  null comment '答题会话ID（用于关联一次完整的测验）',
    answer_time    datetime                     not null comment '答题时间',
    constraint fk_user_quiz_answer_chapter_id
        foreign key (quiz_id) references zhdj.chapter (id)
            on update cascade,
    constraint fk_user_quiz_answer_quiz_id
        foreign key (option_id) references zhdj.quiz (id)
            on update cascade,
    constraint fk_user_quiz_answer_user_id
        foreign key (user_id) references zhdj.user (id)
            on update cascade
)
    comment '用户答题记录表' row_format = DYNAMIC;

create index fk_answer_chapter
    on zhdj.user_quiz_answer (quiz_id);

create index fk_answer_quiz
    on zhdj.user_quiz_answer (option_id);

create index idx_session
    on zhdj.user_quiz_answer (session_id);

create index idx_user_chapter
    on zhdj.user_quiz_answer (user_id, quiz_id);

create index idx_user_quiz
    on zhdj.user_quiz_answer (user_id, option_id);

create table zhdj.user_similarity
(
    id                 bigint unsigned auto_increment comment '相似度记录ID'
        primary key,
    user_id1           bigint unsigned                                                                                                  not null comment '用户1 ID（较小的ID）',
    user_id2           bigint unsigned                                                                                                  not null comment '用户2 ID（较大的ID）',
    similarity_score   decimal(4, 3)                                                                                                    not null comment '相似度分数（-1.000 到 1.000）',
    similarity_type    enum ('learning_behavior', 'quiz_performance', 'course_preference', 'comprehensive') default 'learning_behavior' not null comment '相似度计算类型',
    calculation_params json                                                                                                             null comment '计算参数快照（如使用的特征权重等）',
    data_version       varchar(32)                                                                                                      null comment '数据版本（如：202401_1）',
    calculated_at      timestamp                                                                            default CURRENT_TIMESTAMP   null comment '计算时间',
    is_valid           tinyint(1)                                                                           default 1                   null comment '是否有效（用于标记过期的相似度）',
    expires_at         timestamp                                                                                                        null comment '过期时间',
    update_time        datetime                                                                             default CURRENT_TIMESTAMP   null,
    constraint idx_user_pair
        unique (user_id1, user_id2),
    constraint uk_user_pair_type
        unique (user_id1, user_id2, similarity_type),
    constraint fk_similarity_user1
        foreign key (user_id1) references zhdj.user (id)
            on update cascade on delete cascade,
    constraint fk_similarity_user2
        foreign key (user_id2) references zhdj.user (id)
            on update cascade on delete cascade
)
    comment '用户相似度表（用于协同过滤推荐）' row_format = DYNAMIC;

create index idx_user1_similarity
    on zhdj.user_similarity (user_id1 asc, similarity_type asc, similarity_score desc);

create index idx_user2_similarity
    on zhdj.user_similarity (user_id2 asc, similarity_type asc, similarity_score desc);

