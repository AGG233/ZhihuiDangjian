package com.rauio.smartdangjian.migration;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;

import org.h2.tools.RunScript;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * V7 SCORM 表迁移独立验证。
 *
 * <p>Flyway 在测试环境禁用（application-test.yaml），跨层测试基建亦不建真实表，
 * 故直接以 H2 内存库（MODE=MySQL，兼容 bigint unsigned / LONGTEXT / 内联索引语法）
 * 执行 V7 迁移脚本两次，验证：1) 建表成功；2) 重复执行幂等（表结构与索引不重复创建）。
 */
class ScormMigrationTest {

    private static final String H2_URL =
            "jdbc:h2:mem:scorm_migration;MODE=MySQL;DATABASE_TO_LOWER=TRUE;DB_CLOSE_DELAY=-1;DB_CLOSE_ON_EXIT=FALSE";

    private static Connection connection;

    @BeforeAll
    static void setUp() throws Exception {
        connection = DriverManager.getConnection(H2_URL, "sa", "");
    }

    @AfterAll
    static void tearDown() throws Exception {
        if (connection != null && !connection.isClosed()) {
            connection.close();
        }
    }

    @Test
    @DisplayName("V7 首次执行：scorm_package 与 scorm_registration 建表成功")
    void firstExecutionCreatesTables() throws Exception {
        executeMigration();

        assertThat(tableExists("scorm_package")).isTrue();
        assertThat(tableExists("scorm_registration")).isTrue();
        assertThat(indexExists("scorm_registration", "idx_user_package")).isTrue();
        assertThat(tableExists("scorm_package")).isTrue();
    }

    @Test
    @DisplayName("V14 重复执行：幂等，不抛异常且表结构未被破坏")
    void secondExecutionIsIdempotent() throws Exception {
        executeMigration();
        executeMigration();

        assertThat(tableExists("scorm_package")).isTrue();
        assertThat(tableExists("scorm_registration")).isTrue();
        assertThat(indexExists("scorm_registration", "idx_user_package")).isTrue();
        assertThat(countTables()).isEqualTo(2);
    }

    private void executeMigration() throws Exception {
        try (InputStreamReader reader = new InputStreamReader(
                ScormMigrationTest.class.getResourceAsStream("/db/migration/V14__scorm_tables.sql"),
                StandardCharsets.UTF_8)) {
            RunScript.execute(connection, reader);
        }
    }

    private boolean tableExists(String tableName) throws Exception {
        try (Statement stmt = connection.createStatement();
                ResultSet rs = stmt.executeQuery(
                        "SELECT COUNT(*) FROM INFORMATION_SCHEMA.TABLES WHERE TABLE_NAME = '" + tableName + "'")) {
            rs.next();
            return rs.getInt(1) > 0;
        }
    }

    private boolean indexExists(String tableName, String indexName) throws Exception {
        try (Statement stmt = connection.createStatement();
                ResultSet rs = stmt.executeQuery("SELECT COUNT(*) FROM INFORMATION_SCHEMA.INDEXES"
                        + " WHERE TABLE_NAME = '" + tableName + "' AND INDEX_NAME = '" + indexName + "'")) {
            rs.next();
            return rs.getInt(1) > 0;
        }
    }

    private int countTables() throws Exception {
        try (Statement stmt = connection.createStatement();
                ResultSet rs = stmt.executeQuery("SELECT COUNT(*) FROM INFORMATION_SCHEMA.TABLES"
                        + " WHERE TABLE_NAME IN ('scorm_package', 'scorm_registration')")) {
            rs.next();
            return rs.getInt(1);
        }
    }
}
