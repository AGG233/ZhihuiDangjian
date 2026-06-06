package com.rauio.smartdangjian.server.quiz.pojo.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import com.rauio.smartdangjian.server.quiz.pojo.entity.Quiz;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "测验题目请求")
public record QuizRequest(
        @NotNull @Positive @Schema(description = "章节ID") Long chapterId,
        @NotBlank @Schema(description = "题目内容") String question,
        @NotBlank @Schema(description = "题目类型", example = "single_choice") String questionType,
        @NotNull @Positive @Schema(description = "题目分值", example = "5") Integer score,
        @Schema(description = "难度", example = "medium") String difficulty,
        @Schema(description = "题目解析") String explanation,
        @Schema(description = "是否启用") Boolean isActive) {

    public Quiz toEntity() {
        return Quiz.builder()
                .chapterId(chapterId)
                .question(question)
                .questionType(questionType)
                .score(score)
                .difficulty(difficulty)
                .explanation(explanation)
                .isActive(isActive)
                .build();
    }
}
