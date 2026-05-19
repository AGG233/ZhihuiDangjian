package com.rauio.smartdangjian.server.quiz.pojo.entity;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDateTime;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class QuizTest {

    @Test
    @DisplayName("builder 创建完整 Quiz 实体并验证所有字段")
    void builderCreatesQuizWithAllFields() {
        LocalDateTime now = LocalDateTime.now();

        Quiz quiz = Quiz.builder()
                .id("quiz-123")
                .chapterId("chapter-456")
                .question("什么是党的根本宗旨？")
                .questionType("single_choice")
                .score(5)
                .difficulty("medium")
                .explanation("全心全意为人民服务是中国共产党的根本宗旨")
                .isActive(true)
                .createdAt(now)
                .updatedAt(now)
                .build();

        assertThat(quiz.getId()).isEqualTo("quiz-123");
        assertThat(quiz.getChapterId()).isEqualTo("chapter-456");
        assertThat(quiz.getQuestion()).isEqualTo("什么是党的根本宗旨？");
        assertThat(quiz.getQuestionType()).isEqualTo("single_choice");
        assertThat(quiz.getScore()).isEqualTo(5);
        assertThat(quiz.getDifficulty()).isEqualTo("medium");
        assertThat(quiz.getExplanation()).isEqualTo("全心全意为人民服务是中国共产党的根本宗旨");
        assertThat(quiz.getIsActive()).isTrue();
        assertThat(quiz.getCreatedAt()).isEqualTo(now);
        assertThat(quiz.getUpdatedAt()).isEqualTo(now);
    }

    @Test
    @DisplayName("setter 和 getter 可正常修改和读取字段")
    void settersAndGettersWorkCorrectly() {
        Quiz quiz = Quiz.builder().build();

        quiz.setId("new-id");
        quiz.setChapterId("new-chapter");
        quiz.setQuestion("新问题");
        quiz.setQuestionType("multi_choice");
        quiz.setScore(10);
        quiz.setDifficulty("hard");
        quiz.setExplanation("新的解释");
        quiz.setIsActive(false);

        assertThat(quiz.getId()).isEqualTo("new-id");
        assertThat(quiz.getChapterId()).isEqualTo("new-chapter");
        assertThat(quiz.getQuestion()).isEqualTo("新问题");
        assertThat(quiz.getQuestionType()).isEqualTo("multi_choice");
        assertThat(quiz.getScore()).isEqualTo(10);
        assertThat(quiz.getDifficulty()).isEqualTo("hard");
        assertThat(quiz.getExplanation()).isEqualTo("新的解释");
        assertThat(quiz.getIsActive()).isFalse();
    }

    @Test
    @DisplayName("toString 包含所有字段值")
    void toStringContainsAllFieldValues() {
        Quiz quiz = Quiz.builder()
                .id("qz-1")
                .question("测试题目")
                .chapterId("ch-1")
                .score(5)
                .build();

        String str = quiz.toString();

        assertThat(str).contains("qz-1", "测试题目", "ch-1");
    }

    @Test
    @DisplayName("equals 比较所有字段相同的 Quiz 视为相等")
    void equalsComparesAllFields() {
        Quiz quiz1 = Quiz.builder()
                .id("quiz-1")
                .question("Q1")
                .chapterId("ch-1")
                .score(5)
                .build();
        Quiz quiz2 = Quiz.builder()
                .id("quiz-1")
                .question("Q1")
                .chapterId("ch-1")
                .score(5)
                .build();
        Quiz quiz3 = Quiz.builder()
                .id("quiz-2")
                .question("Q1")
                .chapterId("ch-1")
                .score(5)
                .build();
        Quiz quiz4 = Quiz.builder()
                .id("quiz-1")
                .question("different")
                .chapterId("ch-1")
                .score(5)
                .build();

        assertThat(quiz1).isEqualTo(quiz2);
        assertThat(quiz1).isNotEqualTo(quiz3);
        assertThat(quiz1).isNotEqualTo(quiz4);
    }

    @Test
    @DisplayName("hashCode 所有字段相同时产生相同哈希值")
    void hashCodeBasedOnAllFields() {
        Quiz quiz1 = Quiz.builder()
                .id("qz-1")
                .question("Q1")
                .chapterId("ch-1")
                .score(5)
                .build();
        Quiz quiz2 = Quiz.builder()
                .id("qz-1")
                .question("Q1")
                .chapterId("ch-1")
                .score(5)
                .build();

        assertThat(quiz1.hashCode()).isEqualTo(quiz2.hashCode());
    }

    @Test
    @DisplayName("builder 无参创建时字段为 null")
    void builderWithNoArgsHasNullFields() {
        Quiz quiz = Quiz.builder().build();

        assertThat(quiz.getId()).isNull();
        assertThat(quiz.getChapterId()).isNull();
        assertThat(quiz.getQuestion()).isNull();
        assertThat(quiz.getQuestionType()).isNull();
        assertThat(quiz.getScore()).isNull();
        assertThat(quiz.getDifficulty()).isNull();
        assertThat(quiz.getExplanation()).isNull();
        assertThat(quiz.getIsActive()).isNull();
        assertThat(quiz.getCreatedAt()).isNull();
        assertThat(quiz.getUpdatedAt()).isNull();
    }
}
