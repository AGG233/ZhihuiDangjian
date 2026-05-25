package com.rauio.smartdangjian.server.quiz.pojo.entity;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class QuizOptionTest {

    @Test
    @DisplayName("builder 创建完整 QuizOption 实体并验证所有字段")
    void builderCreatesQuizOptionWithAllFields() {
        QuizOption option = QuizOption.builder()
                .id(1L)
                .quizId(1L)
                .optionText("全心全意为人民服务")
                .isCorrect(true)
                .orderIndex("A")
                .build();

        assertThat(option.getId()).isEqualTo(1L);
        assertThat(option.getQuizId()).isEqualTo(1L);
        assertThat(option.getOptionText()).isEqualTo("全心全意为人民服务");
        assertThat(option.getIsCorrect()).isTrue();
        assertThat(option.getOrderIndex()).isEqualTo("A");
    }

    @Test
    @DisplayName("isCorrect 为 false 表示错误选项")
    void isCorrectFalseRepresentsWrongOption() {
        QuizOption option = QuizOption.builder()
                .id(1L)
                .quizId(1L)
                .optionText("以经济建设为中心")
                .isCorrect(false)
                .orderIndex("B")
                .build();

        assertThat(option.getIsCorrect()).isFalse();
        assertThat(option.getOrderIndex()).isEqualTo("B");
    }

    @Test
    @DisplayName("setter 和 getter 可正常修改和读取字段")
    void settersAndGettersWorkCorrectly() {
        QuizOption option = QuizOption.builder().build();

        option.setId(1L);
        option.setQuizId(1L);
        option.setOptionText("新选项内容");
        option.setIsCorrect(false);
        option.setOrderIndex("D");

        assertThat(option.getId()).isEqualTo(1L);
        assertThat(option.getQuizId()).isEqualTo(1L);
        assertThat(option.getOptionText()).isEqualTo("新选项内容");
        assertThat(option.getIsCorrect()).isFalse();
        assertThat(option.getOrderIndex()).isEqualTo("D");
    }

    @Test
    @DisplayName("toString 包含所有字段值")
    void toStringContainsAllFieldValues() {
        QuizOption option = QuizOption.builder()
                .id(1L)
                .quizId(1L)
                .optionText("选项A")
                .isCorrect(true)
                .orderIndex("A")
                .build();

        String str = option.toString();

        assertThat(str).contains("opt-1", "qz-1", "选项A");
    }

    @Test
    @DisplayName("equals 比较所有字段相同的 QuizOption 视为相等")
    void equalsComparesAllFields() {
        QuizOption opt1 = QuizOption.builder()
                .id(1L)
                .quizId(1L)
                .optionText("选项A")
                .isCorrect(true)
                .orderIndex("A")
                .build();
        QuizOption opt2 = QuizOption.builder()
                .id(1L)
                .quizId(1L)
                .optionText("选项A")
                .isCorrect(true)
                .orderIndex("A")
                .build();
        QuizOption opt3 = QuizOption.builder()
                .id(1L)
                .quizId(1L)
                .optionText("选项A")
                .isCorrect(true)
                .orderIndex("A")
                .build();
        QuizOption opt4 = QuizOption.builder()
                .id(1L)
                .quizId(1L)
                .optionText("选项A")
                .isCorrect(true)
                .orderIndex("A")
                .build();

        assertThat(opt1).isEqualTo(opt2);
        assertThat(opt1).isNotEqualTo(opt3);
        assertThat(opt1).isNotEqualTo(opt4);
    }

    @Test
    @DisplayName("builder 无参创建时字段为 null")
    void builderWithNoArgsHasNullFields() {
        QuizOption option = QuizOption.builder().build();

        assertThat(option.getId()).isNull();
        assertThat(option.getQuizId()).isNull();
        assertThat(option.getOptionText()).isNull();
        assertThat(option.getIsCorrect()).isNull();
        assertThat(option.getOrderIndex()).isNull();
    }
}
