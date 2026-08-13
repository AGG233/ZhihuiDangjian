package com.rauio.smartdangjian.crosslayer.quiz;

import static org.assertj.core.api.Assertions.assertThat;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.rauio.smartdangjian.server.quiz.mapper.UserQuizAnswerMapper;

/**
 * 按章节准确率聚合 SQL 的真实执行验证。
 *
 * <p>直接引用 {@link UserQuizAnswerMapper#CHAPTER_ACCURACY_SQL}（非复制副本），
 * 在 H2 内存库执行，断言 GROUP BY quiz.chapter_id 的分组结果与答对口径
 * （仅 isCorrect=1 计答对，isCorrect=2 部分正确 / isCorrect=0 错误不计）。
 */
class ChapterAccuracyAggregationSqlTest {

    private Connection connection;

    @BeforeEach
    void setUp() throws Exception {
        connection = DriverManager.getConnection("jdbc:h2:mem:accuracy_sql_test;DB_CLOSE_DELAY=-1", "sa", "");
        try (Statement stmt = connection.createStatement()) {
            stmt.execute("DROP TABLE IF EXISTS user_quiz_answer");
            stmt.execute("DROP TABLE IF EXISTS quiz");
            stmt.execute("CREATE TABLE quiz (id BIGINT PRIMARY KEY, chapter_id BIGINT)");
            stmt.execute("CREATE TABLE user_quiz_answer ("
                    + "id BIGINT PRIMARY KEY, user_id BIGINT, quiz_id BIGINT, is_correct INTEGER)");
        }
    }

    @AfterEach
    void tearDown() throws Exception {
        if (connection != null) {
            connection.close();
        }
    }

    @Test
    @DisplayName("聚合 SQL：按章节分组统计记录数与答对数，isCorrect=1 计答对、isCorrect=2/0 不计")
    void groupsByChapterWithCorrectOnlyCounting() throws Exception {
        // 章节 10：两道题共 4 条记录，其中 2 条 isCorrect=1、1 条 isCorrect=2、1 条 isCorrect=0 → 答对 2
        // 章节 20：一道题 2 条记录全 isCorrect=1 → 答对 2
        insertQuiz(1L, 10L);
        insertQuiz(2L, 10L);
        insertQuiz(3L, 20L);
        insertAnswer(1L, 1L, 1L, 1);
        insertAnswer(2L, 1L, 1L, 1);
        insertAnswer(3L, 1L, 2L, 2);
        insertAnswer(4L, 1L, 2L, 0);
        insertAnswer(5L, 1L, 3L, 1);
        insertAnswer(6L, 1L, 3L, 1);

        List<long[]> rows = runAggregation(1L);

        assertThat(rows).hasSize(2);
        assertThat(rows).anyMatch(r -> r[0] == 10L && r[1] == 4 && r[2] == 2);
        assertThat(rows).anyMatch(r -> r[0] == 20L && r[1] == 2 && r[2] == 2);
    }

    @Test
    @DisplayName("聚合 SQL：按 userId 过滤，其他用户记录不影响结果")
    void filtersByUserId() throws Exception {
        insertQuiz(1L, 10L);
        insertQuiz(2L, 20L);
        insertAnswer(1L, 1L, 1L, 1);
        insertAnswer(2L, 1L, 1L, 1);
        insertAnswer(3L, 2L, 2L, 1);

        List<long[]> rows = runAggregation(1L);

        assertThat(rows).hasSize(1);
        assertThat(rows).anyMatch(r -> r[0] == 10L && r[1] == 2 && r[2] == 2);
    }

    @Test
    @DisplayName("聚合 SQL：无答题记录返回空结果")
    void returnsNoRowsWhenNoAnswers() throws Exception {
        insertQuiz(1L, 10L);

        List<long[]> rows = runAggregation(1L);

        assertThat(rows).isEmpty();
    }

    private void insertQuiz(long id, long chapterId) throws Exception {
        try (PreparedStatement ps = connection.prepareStatement("INSERT INTO quiz (id, chapter_id) VALUES (?, ?)")) {
            ps.setLong(1, id);
            ps.setLong(2, chapterId);
            ps.executeUpdate();
        }
    }

    private void insertAnswer(long id, long userId, long quizId, Integer isCorrect) throws Exception {
        try (PreparedStatement ps = connection.prepareStatement(
                "INSERT INTO user_quiz_answer (id, user_id, quiz_id, is_correct) VALUES (?, ?, ?, ?)")) {
            ps.setLong(1, id);
            ps.setLong(2, userId);
            ps.setLong(3, quizId);
            if (isCorrect == null) {
                ps.setObject(4, null);
            } else {
                ps.setInt(4, isCorrect);
            }
            ps.executeUpdate();
        }
    }

    private List<long[]> runAggregation(long userId) throws Exception {
        List<long[]> rows = new ArrayList<>();
        String sql = UserQuizAnswerMapper.CHAPTER_ACCURACY_SQL.replace("#{userId}", "?");
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setLong(1, userId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    rows.add(
                            new long[] {rs.getLong("chapterId"), rs.getLong("questionCount"), rs.getLong("correctCount")
                            });
                }
            }
        }
        return rows;
    }
}
