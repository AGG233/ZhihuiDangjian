package com.rauio.smartdangjian.server.quiz.pojo.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import com.rauio.smartdangjian.server.quiz.pojo.entity.QuizOption;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "测验选项请求")
public record QuizOptionRequest(
        @NotBlank @Schema(description = "选项内容") String optionText,
        @NotNull @Schema(description = "是否为正确答案") Boolean isCorrect,
        @NotBlank @Schema(description = "选项标签，例如 A、B、C、D") String orderIndex) {

    public QuizOption toEntity() {
        return QuizOption.builder()
                .optionText(optionText)
                .isCorrect(isCorrect)
                .orderIndex(orderIndex)
                .build();
    }
}
