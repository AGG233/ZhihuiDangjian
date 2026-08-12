package com.rauio.smartdangjian.server.quiz.pojo.entity;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDateTime;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class UserQuizAnswerTest {

    @Test
    @DisplayName("builder 创建完整 UserQuizAnswer 实体并验证所有字段")
    void builderCreatesUserQuizAnswerWithAllFields() {
        LocalDateTime answerTime = LocalDateTime.now();

        UserQuizAnswer answer = UserQuizAnswer.builder()
                .id(1L)
                .userId(1L)
                .optionId(1L)
                .quizId(1L)
                .userAnswer("A")
                .isCorrect(1)
                .scoreObtained(5)
                .timeSpent(30)
                .sessionId("session-1")
                .answerTime(answerTime)
                .build();

        assertThat(answer.getId()).isEqualTo(1L);
        assertThat(answer.getUserId()).isEqualTo(1L);
        assertThat(answer.getOptionId()).isEqualTo(1L);
        assertThat(answer.getQuizId()).isEqualTo(1L);
        assertThat(answer.getUserAnswer()).isEqualTo("A");
        assertThat(answer.getIsCorrect()).isEqualTo(1);
        assertThat(answer.getScoreObtained()).isEqualTo(5);
        assertThat(answer.getTimeSpent()).isEqualTo(30);
        assertThat(answer.getSessionId()).isEqualTo("session-1");
        assertThat(answer.getAnswerTime()).isEqualTo(answerTime);
    }

    @Test
    @DisplayName("isCorrect 为 0 表示回答错误")
    void isCorrectZeroMeansWrongAnswer() {
        UserQuizAnswer answer = UserQuizAnswer.builder()
                .id(1L)
                .userId(1L)
                .quizId(1L)
                .isCorrect(0)
                .build();

        assertThat(answer.getIsCorrect()).isZero();
    }

    @Test
    @DisplayName("isCorrect 为 2 表示部分正确")
    void isCorrectTwoMeansPartiallyCorrect() {
        UserQuizAnswer answer = UserQuizAnswer.builder()
                .id(1L)
                .userId(1L)
                .quizId(1L)
                .isCorrect(2)
                .build();

        assertThat(answer.getIsCorrect()).isEqualTo(2);
    }

    @Test
    @DisplayName("setter 和 getter 可正常修改和读取字段")
    void settersAndGettersWorkCorrectly() {
        LocalDateTime newTime = LocalDateTime.now();
        UserQuizAnswer answer = UserQuizAnswer.builder().build();

        answer.setId(1L);
        answer.setUserId(1L);
        answer.setOptionId(1L);
        answer.setQuizId(1L);
        answer.setUserAnswer("B");
        answer.setIsCorrect(0);
        answer.setScoreObtained(0);
        answer.setTimeSpent(60);
        answer.setSessionId("session-1");
        answer.setAnswerTime(newTime);

        assertThat(answer.getId()).isEqualTo(1L);
        assertThat(answer.getUserId()).isEqualTo(1L);
        assertThat(answer.getOptionId()).isEqualTo(1L);
        assertThat(answer.getQuizId()).isEqualTo(1L);
        assertThat(answer.getUserAnswer()).isEqualTo("B");
        assertThat(answer.getIsCorrect()).isZero();
        assertThat(answer.getScoreObtained()).isZero();
        assertThat(answer.getTimeSpent()).isEqualTo(60);
        assertThat(answer.getSessionId()).isEqualTo("session-1");
        assertThat(answer.getAnswerTime()).isEqualTo(newTime);
    }

    @Test
    @DisplayName("toString 包含所有字段值")
    void toStringContainsAllFieldValues() {
        UserQuizAnswer answer = UserQuizAnswer.builder()
                .id(1L)
                .userId(1L)
                .quizId(1L)
                .isCorrect(1)
                .build();

        String str = answer.toString();

        assertThat(str).contains("id=1", "userId=1", "quizId=1");
    }

    @Test
    @DisplayName("equals 比较所有字段相同的 UserQuizAnswer 视为相等")
    void equalsComparesAllFields() {
        UserQuizAnswer a1 = UserQuizAnswer.builder()
                .id(1L)
                .quizId(1L)
                .userId(1L)
                .isCorrect(1)
                .build();
        UserQuizAnswer a2 = UserQuizAnswer.builder()
                .id(1L)
                .quizId(1L)
                .userId(1L)
                .isCorrect(1)
                .build();
        UserQuizAnswer a3 = UserQuizAnswer.builder()
                .id(2L)
                .quizId(1L)
                .userId(1L)
                .isCorrect(1)
                .build();
        UserQuizAnswer a4 = UserQuizAnswer.builder()
                .id(1L)
                .quizId(1L)
                .userId(1L)
                .isCorrect(0)
                .build();

        assertThat(a1).isEqualTo(a2);
        assertThat(a1).isNotEqualTo(a3);
        assertThat(a1).isNotEqualTo(a4);
    }

    @Test
    @DisplayName("builder 无参创建时字段为 null")
    void builderWithNoArgsHasNullFields() {
        UserQuizAnswer answer = UserQuizAnswer.builder().build();

        assertThat(answer.getId()).isNull();
        assertThat(answer.getUserId()).isNull();
        assertThat(answer.getOptionId()).isNull();
        assertThat(answer.getQuizId()).isNull();
        assertThat(answer.getUserAnswer()).isNull();
        assertThat(answer.getIsCorrect()).isNull();
        assertThat(answer.getScoreObtained()).isNull();
        assertThat(answer.getTimeSpent()).isNull();
        assertThat(answer.getSessionId()).isNull();
        assertThat(answer.getAnswerTime()).isNull();
    }
}
