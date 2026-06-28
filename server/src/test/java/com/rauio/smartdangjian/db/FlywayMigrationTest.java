package com.rauio.smartdangjian.db;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * 验证 Flyway migration 文件存在性及基本结构。
 * 确保新增 migration 包含预期的 SQL 变更。
 */
class FlywayMigrationTest {

    private static final Path MIGRATION_DIR = Paths.get("src/main/resources/db/migration");

    @Test
    @DisplayName("V10 migration 文件存在")
    void v10MigrationFileExists() {
        Path v10File = MIGRATION_DIR.resolve("V10__add_composite_indexes_and_fix_option_id.sql");
        assertThat(v10File).exists();
    }

    @Test
    @DisplayName("V10 包含 article(status, published_at) 复合索引")
    void v10ContainsArticleStatusPublishedAtIndex() throws IOException {
        String content = Files.readString(MIGRATION_DIR.resolve("V10__add_composite_indexes_and_fix_option_id.sql"));
        assertThat(content).contains("idx_article_status_published_at").contains("article (status, published_at)");
    }

    @Test
    @DisplayName("V10 包含 comment(parent_id, target_type, target_id) 联合索引")
    void v10ContainsCommentParentTargetIndex() throws IOException {
        String content = Files.readString(MIGRATION_DIR.resolve("V10__add_composite_indexes_and_fix_option_id.sql"));
        assertThat(content)
                .contains("idx_comment_parent_target")
                .contains("comment (parent_id, target_type, target_id)");
    }

    @Test
    @DisplayName("V10 将 user_quiz_answer.option_id 改为 NULLABLE")
    void v10MakesOptionIdNullable() throws IOException {
        String content = Files.readString(MIGRATION_DIR.resolve("V10__add_composite_indexes_and_fix_option_id.sql"));
        assertThat(content)
                .contains("DROP FOREIGN KEY fk_user_quiz_answer_option")
                .contains("MODIFY COLUMN option_id BIGINT UNSIGNED NULL");
    }
}
