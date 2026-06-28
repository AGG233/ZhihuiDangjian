package com.rauio.smartdangjian.db;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.stream.Stream;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

/**
 * 验证 Flyway migration 文件存在性、基本结构及幂等性模式。
 * 确保所有迁移文件均存在且包含预期的 SQL 变更。
 * 增量 DDL 必须幂等（IF NOT EXISTS / INFORMATION_SCHEMA 检查）。
 */
class FlywayMigrationTest {

    private static final Path MIGRATION_DIR = Paths.get("src/main/resources/db/migration");

    // ==================== 文件存在性 ====================

    @Test
    @DisplayName("所有 11 个迁移文件存在")
    void allMigrationFilesExist() throws IOException {
        try (Stream<Path> files = Files.list(MIGRATION_DIR)) {
            List<String> names = files.map(p -> p.getFileName().toString())
                    .filter(n -> n.startsWith("V") && n.endsWith(".sql"))
                    .sorted()
                    .toList();

            assertThat(names)
                    .contains(
                            "V1__initial_schema.sql",
                            "V2__seed_universities.sql",
                            "V3__ai_agent_tables.sql",
                            "V4__add_source_url_to_article.sql",
                            "V5__seed_test_users.sql",
                            "V6__category_university_id_nullable.sql",
                            "V7__ai_faq_table.sql",
                            "V8__social_tables.sql",
                            "V9__add_like_count_to_article_and_course.sql",
                            "V10__add_composite_indexes_and_fix_option_id.sql",
                            "V11__alter_ai_prompts_role_enum.sql");
        }
    }

    @ParameterizedTest(name = "{0} 迁移文件存在")
    @ValueSource(
            strings = {
                "V1__initial_schema.sql",
                "V2__seed_universities.sql",
                "V3__ai_agent_tables.sql",
                "V4__add_source_url_to_article.sql",
                "V5__seed_test_users.sql",
                "V6__category_university_id_nullable.sql",
                "V7__ai_faq_table.sql",
                "V8__social_tables.sql",
                "V9__add_like_count_to_article_and_course.sql",
                "V10__add_composite_indexes_and_fix_option_id.sql",
                "V11__alter_ai_prompts_role_enum.sql"
            })
    @DisplayName("每个迁移文件单独验证存在性")
    void migrationFileExists(String fileName) {
        Path file = MIGRATION_DIR.resolve(fileName);
        assertThat(file).exists().isRegularFile();
    }

    // ==================== V1: 初始 schema ====================

    @Test
    @DisplayName("V1 包含 user 表定义")
    void v1ContainsUserTable() throws IOException {
        String content = readMigration("V1__initial_schema.sql");
        assertThat(content)
                .contains("CREATE TABLE user")
                .contains("username")
                .contains("password")
                .contains("user_type")
                .contains("party_status")
                .contains("permission_level");
    }

    @Test
    @DisplayName("V1 包含 category 树形结构表")
    void v1ContainsCategoryTable() throws IOException {
        String content = readMigration("V1__initial_schema.sql");
        assertThat(content)
                .contains("CREATE TABLE category")
                .contains("parent_id")
                .contains("sort_order");
    }

    @Test
    @DisplayName("V1 包含 course 和 chapter 表")
    void v1ContainsCourseAndChapter() throws IOException {
        String content = readMigration("V1__initial_schema.sql");
        assertThat(content)
                .contains("CREATE TABLE course")
                .contains("CREATE TABLE chapter")
                .contains("enrollment_count")
                .contains("chapter_status");
    }

    @Test
    @DisplayName("V1 包含 content_block 拆分（chapter_content_block + article_content_block）")
    void v1ContainsContentBlocks() throws IOException {
        String content = readMigration("V1__initial_schema.sql");
        assertThat(content)
                .contains("chapter_content_block")
                .contains("article_content_block")
                .contains("block_type");
    }

    @Test
    @DisplayName("V1 包含 quiz 和 quiz_option 表")
    void v1ContainsQuizTables() throws IOException {
        String content = readMigration("V1__initial_schema.sql");
        assertThat(content)
                .contains("CREATE TABLE quiz")
                .contains("CREATE TABLE quiz_option")
                .contains("question_type")
                .contains("is_correct");
    }

    @Test
    @DisplayName("V1 包含 user_learning_record 和 user_chapter_progress 表")
    void v1ContainsLearningTables() throws IOException {
        String content = readMigration("V1__initial_schema.sql");
        assertThat(content)
                .contains("user_chapter_progress")
                .contains("user_learning_record")
                .contains("device_type");
    }

    @Test
    @DisplayName("V1 包含 user_quiz_answer 表及外键引用")
    void v1ContainsUserQuizAnswer() throws IOException {
        String content = readMigration("V1__initial_schema.sql");
        assertThat(content).contains("user_quiz_answer").contains("fk_user_quiz_answer_option");
    }

    @Test
    @DisplayName("V1 包含 user_similarity 表（协同过滤推荐）")
    void v1ContainsUserSimilarity() throws IOException {
        String content = readMigration("V1__initial_schema.sql");
        assertThat(content).contains("user_similarity").contains("similarity_score");
    }

    // ==================== V2: 种子数据 ====================

    @Test
    @DisplayName("V2 使用 INSERT IGNORE 插入学校种子数据")
    void v2SeedsUniversities() throws IOException {
        String content = readMigration("V2__seed_universities.sql");
        assertThat(content).contains("INSERT IGNORE INTO universities").contains("CI测试学校");
    }

    // ==================== V3: AI Agent 表 ====================

    @Test
    @DisplayName("V3 ai_prompts 表使用 CREATE TABLE IF NOT EXISTS（幂等）")
    void v3CreatesAiPromptsTable() throws IOException {
        String content = readMigration("V3__ai_agent_tables.sql");
        assertThat(content).contains("CREATE TABLE IF NOT EXISTS ai_prompts").contains("ENUM ('system','user')");
    }

    @Test
    @DisplayName("V3 ai_skill 和 ai_chat_message 表使用 CREATE TABLE IF NOT EXISTS（幂等）")
    void v3CreatesAiSkillAndChatMessage() throws IOException {
        String content = readMigration("V3__ai_agent_tables.sql");
        assertThat(content)
                .contains("CREATE TABLE IF NOT EXISTS ai_skill")
                .contains("CREATE TABLE IF NOT EXISTS ai_chat_message")
                .contains("sender_type");
    }

    // ==================== V4: source_url 列 ====================

    @Test
    @DisplayName("V4 使用 INFORMATION_SCHEMA 做幂等检查")
    void v4IdempotentCheck() throws IOException {
        String content = readMigration("V4__add_source_url_to_article.sql");
        assertThat(content).contains("INFORMATION_SCHEMA.COLUMNS").contains("source_url");
    }

    @Test
    @DisplayName("V4 创建 source_url 索引")
    void v4CreatesSourceUrlIndex() throws IOException {
        String content = readMigration("V4__add_source_url_to_article.sql");
        assertThat(content).contains("CREATE INDEX idx_article_source_url");
    }

    // ==================== V5: 测试种子用户 ====================

    @Test
    @DisplayName("V5 使用 INSERT IGNORE 插入 10 个压力测试用户")
    void v5SeedsTestUsers() throws IOException {
        String content = readMigration("V5__seed_test_users.sql");
        assertThat(content)
                .contains("INSERT IGNORE INTO user")
                .contains("loadtest01")
                .contains("loadtest10");
    }

    @Test
    @DisplayName("V5 包含 BCrypt 加密密码")
    void v5ContainsBcryptPassword() throws IOException {
        String content = readMigration("V5__seed_test_users.sql");
        assertThat(content).contains("$2b$12$");
    }

    // ==================== V6: category.university_id 改为可空 ====================

    @Test
    @DisplayName("V6 将 category.university_id 改为可空")
    void v6MakesUniversityIdNullable() throws IOException {
        String content = readMigration("V6__category_university_id_nullable.sql");
        assertThat(content)
                .contains("ALTER TABLE category")
                .contains("MODIFY university_id")
                .contains("NULL");
    }

    // ==================== V7: AI FAQ 表 ====================

    @Test
    @DisplayName("V7 使用 CREATE TABLE IF NOT EXISTS 创建 ai_faq 表（幂等）")
    void v7CreatesAiFaqTable() throws IOException {
        String content = readMigration("V7__ai_faq_table.sql");
        assertThat(content)
                .contains("CREATE TABLE IF NOT EXISTS ai_faq")
                .contains("keywords")
                .contains("answer");
    }

    @Test
    @DisplayName("V7 创建 ai_faq 索引")
    void v7CreatesAiFaqIndex() throws IOException {
        String content = readMigration("V7__ai_faq_table.sql");
        assertThat(content).contains("CREATE INDEX idx_ai_faq_enabled_sort");
    }

    // ==================== V8: 社交模块 ====================

    @Test
    @DisplayName("V8 comment 表使用 CREATE TABLE IF NOT EXISTS（幂等）")
    void v8CreatesCommentTable() throws IOException {
        String content = readMigration("V8__social_tables.sql");
        assertThat(content)
                .contains("CREATE TABLE IF NOT EXISTS comment")
                .contains("target_type")
                .contains("parent_id");
    }

    @Test
    @DisplayName("V8 user_like 表使用 CREATE TABLE IF NOT EXISTS（幂等）")
    void v8CreatesUserLikeTable() throws IOException {
        String content = readMigration("V8__social_tables.sql");
        assertThat(content).contains("CREATE TABLE IF NOT EXISTS user_like").contains("uk_user_like");
    }

    @Test
    @DisplayName("V8 comment 表创建 target/user/parent/created 复合索引")
    void v8CreatesCommentIndexes() throws IOException {
        String content = readMigration("V8__social_tables.sql");
        assertThat(content)
                .contains("idx_comment_target")
                .contains("idx_comment_user")
                .contains("idx_comment_parent")
                .contains("idx_comment_created");
    }

    // ==================== V9: 点赞数字段 ====================

    @Test
    @DisplayName("V9 使用 INFORMATION_SCHEMA 做幂等检查添加 like_count")
    void v9IdempotentCheck() throws IOException {
        String content = readMigration("V9__add_like_count_to_article_and_course.sql");
        assertThat(content).contains("INFORMATION_SCHEMA.COLUMNS").contains("like_count");
    }

    @Test
    @DisplayName("V9 为 article 和 course 添加 like_count 列")
    void v9AddsLikeCountToArticleAndCourse() throws IOException {
        String content = readMigration("V9__add_like_count_to_article_and_course.sql");
        assertThat(content)
                .contains("ALTER TABLE article ADD COLUMN like_count")
                .contains("ALTER TABLE course ADD COLUMN like_count");
    }

    // ==================== V10: 复合索引 + option_id 修复 ====================

    @Test
    @DisplayName("V10 migration 文件存在")
    void v10MigrationFileExists() {
        Path v10File = MIGRATION_DIR.resolve("V10__add_composite_indexes_and_fix_option_id.sql");
        assertThat(v10File).exists();
    }

    @Test
    @DisplayName("V10 包含 article(status, published_at) 复合索引")
    void v10ContainsArticleStatusPublishedAtIndex() throws IOException {
        String content = readMigration("V10__add_composite_indexes_and_fix_option_id.sql");
        assertThat(content).contains("idx_article_status_published_at").contains("article (status, published_at)");
    }

    @Test
    @DisplayName("V10 包含 comment(parent_id, target_type, target_id) 联合索引")
    void v10ContainsCommentParentTargetIndex() throws IOException {
        String content = readMigration("V10__add_composite_indexes_and_fix_option_id.sql");
        assertThat(content)
                .contains("idx_comment_parent_target")
                .contains("comment (parent_id, target_type, target_id)");
    }

    @Test
    @DisplayName("V10 将 user_quiz_answer.option_id 改为 NULLABLE")
    void v10MakesOptionIdNullable() throws IOException {
        String content = readMigration("V10__add_composite_indexes_and_fix_option_id.sql");
        assertThat(content)
                .contains("DROP FOREIGN KEY fk_user_quiz_answer_option")
                .contains("MODIFY COLUMN option_id BIGINT UNSIGNED NULL");
    }

    @Test
    @DisplayName("V10 使用 INFORMATION_SCHEMA 做幂等检查")
    void v10UsesIdempotentPattern() throws IOException {
        String content = readMigration("V10__add_composite_indexes_and_fix_option_id.sql");
        assertThat(content)
                .contains("INFORMATION_SCHEMA.STATISTICS")
                .contains("INFORMATION_SCHEMA.TABLE_CONSTRAINTS")
                .contains("INFORMATION_SCHEMA.COLUMNS");
    }

    // ==================== V11: 扩展 ai_prompts.role 枚举 ====================

    @Test
    @DisplayName("V11 migration 文件存在")
    void v11MigrationFileExists() {
        Path v11File = MIGRATION_DIR.resolve("V11__alter_ai_prompts_role_enum.sql");
        assertThat(v11File).exists();
    }

    @Test
    @DisplayName("V11 使用 INFORMATION_SCHEMA 做幂等检查")
    void v11UsesIdempotentCheck() throws IOException {
        String content = readMigration("V11__alter_ai_prompts_role_enum.sql");
        assertThat(content).contains("INFORMATION_SCHEMA.COLUMNS").contains("COLUMN_TYPE LIKE '%developer%'");
    }

    @Test
    @DisplayName("V11 将 ai_prompts.role ENUM 扩展为包含 developer")
    void v11ExtendsRoleEnum() throws IOException {
        String content = readMigration("V11__alter_ai_prompts_role_enum.sql");
        assertThat(content)
                .contains("ENUM(''system'',''user'',''developer'')")
                .contains("MODIFY COLUMN role")
                .contains("ai_prompts");
    }

    // ==================== 幂等性模式全局检查 ====================

    @ParameterizedTest(name = "{0} 满足幂等性模式")
    @ValueSource(strings = {"V3__ai_agent_tables.sql", "V7__ai_faq_table.sql", "V8__social_tables.sql"})
    @DisplayName("CREATE TABLE 类迁移使用 IF NOT EXISTS 幂等模式")
    void idempotentCreateTablePattern(String fileName) throws IOException {
        String content = readMigration(fileName);
        assertThat(content).containsPattern("CREATE TABLE IF NOT EXISTS");
    }

    @ParameterizedTest(name = "{0} 满足幂等性模式")
    @ValueSource(
            strings = {
                "V4__add_source_url_to_article.sql",
                "V9__add_like_count_to_article_and_course.sql",
                "V10__add_composite_indexes_and_fix_option_id.sql",
                "V11__alter_ai_prompts_role_enum.sql"
            })
    @DisplayName("增量 DDL 类迁移使用 INFORMATION_SCHEMA 幂等检查模式")
    void idempotentInformationSchemaPattern(String fileName) throws IOException {
        String content = readMigration(fileName);
        assertThat(content).contains("INFORMATION_SCHEMA");
    }

    @ParameterizedTest(name = "{0} 使用 INSERT IGNORE 幂等插入")
    @ValueSource(strings = {"V2__seed_universities.sql", "V5__seed_test_users.sql"})
    @DisplayName("数据种子迁移使用 INSERT IGNORE 幂等模式")
    void idempotentInsertPattern(String fileName) throws IOException {
        String content = readMigration(fileName);
        assertThat(content).contains("INSERT IGNORE");
    }

    // ==================== 辅助方法 ====================

    private String readMigration(String fileName) throws IOException {
        return Files.readString(MIGRATION_DIR.resolve(fileName));
    }
}
