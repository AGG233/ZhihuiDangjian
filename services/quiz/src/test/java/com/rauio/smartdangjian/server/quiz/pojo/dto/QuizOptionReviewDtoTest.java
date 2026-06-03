package com.rauio.smartdangjian.server.quiz.pojo.dto;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.rauio.smartdangjian.server.quiz.pojo.entity.QuizOption;

@DisplayName("QuizOptionReviewDto")
class QuizOptionReviewDtoTest {

    @Test
    @DisplayName("from 方法将 QuizOption 转换为 DTO，包含 isCorrect")
    void fromQuizOptionIncludesIsCorrect() {
        QuizOption option = QuizOption.builder()
                .id(1L)
                .quizId(10L)
                .optionText("测试选项A")
                .isCorrect(true)
                .orderIndex("A")
                .build();

        QuizOptionReviewDto dto = QuizOptionReviewDto.from(option);

        assertThat(dto).isNotNull();
        assertThat(dto.getId()).isEqualTo(1L);
        assertThat(dto.getQuizId()).isEqualTo(10L);
        assertThat(dto.getOptionText()).isEqualTo("测试选项A");
        assertThat(dto.getIsCorrect()).isTrue();
        assertThat(dto.getOrderIndex()).isEqualTo("A");
    }

    @Test
    @DisplayName("from 方法处理 isCorrect 为 false")
    void fromQuizOptionWithIsCorrectFalse() {
        QuizOption option = QuizOption.builder()
                .id(2L)
                .quizId(10L)
                .optionText("错误选项")
                .isCorrect(false)
                .orderIndex("B")
                .build();

        QuizOptionReviewDto dto = QuizOptionReviewDto.from(option);

        assertThat(dto.getIsCorrect()).isFalse();
    }

    @Test
    @DisplayName("from 方法入参为 null 时返回 null")
    void fromNullReturnsNull() {
        assertThat(QuizOptionReviewDto.from(null)).isNull();
    }

    @Test
    @DisplayName("无参构造器和 setter 正常工作")
    void noArgsConstructorAndSettersWork() {
        QuizOptionReviewDto dto = new QuizOptionReviewDto();
        dto.setId(3L);
        dto.setQuizId(10L);
        dto.setOptionText("选项C");
        dto.setIsCorrect(true);
        dto.setOrderIndex("C");

        assertThat(dto.getId()).isEqualTo(3L);
        assertThat(dto.getIsCorrect()).isTrue();
    }

    @Test
    @DisplayName("全参构造器正常工作")
    void allArgsConstructorWorks() {
        QuizOptionReviewDto dto = new QuizOptionReviewDto(4L, 10L, "选项D", false, "D");

        assertThat(dto.getId()).isEqualTo(4L);
        assertThat(dto.getIsCorrect()).isFalse();
        assertThat(dto.getOptionText()).isEqualTo("选项D");
    }

    @Test
    @DisplayName("builder 正常工作")
    void builderWorks() {
        QuizOptionReviewDto dto = QuizOptionReviewDto.builder()
                .id(5L)
                .quizId(10L)
                .optionText("选项E")
                .isCorrect(true)
                .orderIndex("E")
                .build();

        assertThat(dto.getId()).isEqualTo(5L);
        assertThat(dto.getIsCorrect()).isTrue();
    }
}
