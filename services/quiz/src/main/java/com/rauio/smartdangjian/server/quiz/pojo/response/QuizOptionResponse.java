package com.rauio.smartdangjian.server.quiz.pojo.response;

import com.rauio.smartdangjian.server.quiz.pojo.entity.QuizOption;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "测验选项响应")
public class QuizOptionResponse {

    @Schema(description = "选项ID")
    private Long id;

    @Schema(description = "题目ID")
    private Long quizId;

    @Schema(description = "选项内容")
    private String optionText;

    @Schema(description = "选项标签，例如 A、B、C、D")
    private String orderIndex;

    public static QuizOptionResponse from(QuizOption option) {
        if (option == null) {
            return null;
        }
        return QuizOptionResponse.builder()
                .id(option.getId())
                .quizId(option.getQuizId())
                .optionText(option.getOptionText())
                .orderIndex(option.getOrderIndex())
                .build();
    }
}
