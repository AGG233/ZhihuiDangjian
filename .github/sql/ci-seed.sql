INSERT IGNORE INTO universities (id, name)
VALUES ('1001', 'CI测试学校');

INSERT IGNORE INTO user (
    id,
    university_id,
    username,
    password,
    user_type,
    real_name,
    id_card,
    party_member_id,
    party_status,
    branch_name,
    join_party_date,
    status,
    email,
    phone,
    permission_level
) VALUES (
    10001,
    '1001',
    'ci_smoke_user',
    '$2a$10$DhmonCqt7Ek.10jGN8ZO8O09.JB1N3GO4axf2KwngKBitHDxre3lO',
    '学生',
    '测试用户',
    '110101199001011234',
    'CI202604080000000001',
    '群众',
    'CI测试支部',
    '2024-01-01 00:00:00',
    'active',
    'ci_smoke_user@example.com',
    '13800138000',
    9
);

INSERT IGNORE INTO user (
    id,
    university_id,
    username,
    password,
    user_type,
    real_name,
    id_card,
    party_member_id,
    party_status,
    branch_name,
    join_party_date,
    status,
    email,
    phone,
    permission_level
) VALUES (
    10002,
    '1001',
    'ci_smoke_school',
    '$2a$10$DhmonCqt7Ek.10jGN8ZO8O09.JB1N3GO4axf2KwngKBitHDxre3lO',
    '学校',
    'CI学校管理员',
    '110101199001011235',
    'CI202604080000000002',
    '群众',
    'CI测试支部',
    '2024-01-01 00:00:00',
    'active',
    'ci_smoke_school@example.com',
    '13800138001',
    4
);

INSERT IGNORE INTO user (
    id,
    university_id,
    username,
    password,
    user_type,
    real_name,
    id_card,
    party_member_id,
    party_status,
    branch_name,
    join_party_date,
    status,
    email,
    phone,
    permission_level
) VALUES (
    10003,
    '1001',
    'ci_smoke_manager',
    '$2a$10$DhmonCqt7Ek.10jGN8ZO8O09.JB1N3GO4axf2KwngKBitHDxre3lO',
    '管理员',
    'CI平台管理员',
    '110101199001011236',
    'CI202604080000000003',
    '群众',
    'CI测试支部',
    '2024-01-01 00:00:00',
    'active',
    'ci_smoke_manager@example.com',
    '13800138002',
    0
);

INSERT IGNORE INTO category (id, name, level, university_id)
VALUES (1, 'ci-category', 1, 10001);

INSERT IGNORE INTO course (id, title, creator_id)
VALUES (1, 'ci-course', 10001);

INSERT IGNORE INTO chapter (id, course_id, title)
VALUES (1, 1, 'ci-chapter');

INSERT IGNORE INTO resource_meta (id, hash, uploader_id, original_name, object_key, resource_type, status)
VALUES (1, 'ci-hash', 10001, 'ci-test-file.png', 'ci/test/file.png', 0, 1);

-- AI 提示词：覆盖 role 枚举映射（DB 小写 'system' ↔ PromptRoleEnum @EnumValue）。
-- 此前该表为空，GET /api/admin/ai/prompts 不触发枚举转换，枚举不匹配问题在 CI 中不可见。
INSERT IGNORE INTO ai_prompts (id, agent_type, category, name, role, content, enabled, sort)
VALUES (90001, 'COMMON', '通用', 'CI系统提示词', 'system', 'CI 烟雾测试用系统提示词。', 1, 0);
