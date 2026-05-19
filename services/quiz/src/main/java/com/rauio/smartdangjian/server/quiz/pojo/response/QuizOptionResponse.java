package com.rauio.smartdangjian.server.quiz.pojo.response;

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
    private String id;

    @Schema(description = "题目ID")
    private String quizId;

    @Schema(description = "选项内容")
    private String optionText;

    @Schema(description = "选项标签，例如 A、B、C、D")
    private String orderIndex;
}
