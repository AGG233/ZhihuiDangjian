-- ============================================================
-- V5: Seed test users for load testing
-- 密码统一为 LoadTest123!（BCrypt 加密）
-- 使用 INSERT IGNORE，可在已有数据的库上安全执行
-- ============================================================

INSERT IGNORE INTO user (id, university_id, username, password, user_type, real_name, id_card, status, created_at, updated_at)
VALUES
(10000001, '001', 'loadtest01', '$2b$12$RtlUldVi9il0ZvreP8j3GOq1DKlGEmfWKnXim25U6aj8qfayeZ/fu', '学生', '压力测试用户1', '110001199001010001', 'active', NOW(3), NOW(3)),
(10000002, '001', 'loadtest02', '$2b$12$RtlUldVi9il0ZvreP8j3GOq1DKlGEmfWKnXim25U6aj8qfayeZ/fu', '学生', '压力测试用户2', '110001199001010002', 'active', NOW(3), NOW(3)),
(10000003, '001', 'loadtest03', '$2b$12$RtlUldVi9il0ZvreP8j3GOq1DKlGEmfWKnXim25U6aj8qfayeZ/fu', '学生', '压力测试用户3', '110001199001010003', 'active', NOW(3), NOW(3)),
(10000004, '001', 'loadtest04', '$2b$12$RtlUldVi9il0ZvreP8j3GOq1DKlGEmfWKnXim25U6aj8qfayeZ/fu', '学生', '压力测试用户4', '110001199001010004', 'active', NOW(3), NOW(3)),
(10000005, '001', 'loadtest05', '$2b$12$RtlUldVi9il0ZvreP8j3GOq1DKlGEmfWKnXim25U6aj8qfayeZ/fu', '学生', '压力测试用户5', '110001199001010005', 'active', NOW(3), NOW(3)),
(10000006, '001', 'loadtest06', '$2b$12$RtlUldVi9il0ZvreP8j3GOq1DKlGEmfWKnXim25U6aj8qfayeZ/fu', '学生', '压力测试用户6', '110001199001010006', 'active', NOW(3), NOW(3)),
(10000007, '001', 'loadtest07', '$2b$12$RtlUldVi9il0ZvreP8j3GOq1DKlGEmfWKnXim25U6aj8qfayeZ/fu', '学生', '压力测试用户7', '110001199001010007', 'active', NOW(3), NOW(3)),
(10000008, '001', 'loadtest08', '$2b$12$RtlUldVi9il0ZvreP8j3GOq1DKlGEmfWKnXim25U6aj8qfayeZ/fu', '学生', '压力测试用户8', '110001199001010008', 'active', NOW(3), NOW(3)),
(10000009, '001', 'loadtest09', '$2b$12$RtlUldVi9il0ZvreP8j3GOq1DKlGEmfWKnXim25U6aj8qfayeZ/fu', '学生', '压力测试用户9', '110001199001010009', 'active', NOW(3), NOW(3)),
(10000010, '001', 'loadtest10', '$2b$12$RtlUldVi9il0ZvreP8j3GOq1DKlGEmfWKnXim25U6aj8qfayeZ/fu', '学生', '压力测试用户10', '110001199001010010', 'active', NOW(3), NOW(3));
