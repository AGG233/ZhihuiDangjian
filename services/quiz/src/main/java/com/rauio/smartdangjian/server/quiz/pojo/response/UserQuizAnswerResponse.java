package com.rauio.smartdangjian.server.quiz.pojo.response;

import java.time.LocalDateTime;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "用户答题记录响应")
public class UserQuizAnswerResponse {

    @Schema(description = "答题记录ID")
    private String id;

    @Schema(description = "用户ID")
    private String userId;

    @Schema(description = "选项ID")
    private String optionId;

    @Schema(description = "题目ID")
    private String quizId;

    @Schema(description = "用户答案")
    private String userAnswer;

    @Schema(description = "答题结果：0表示错误，1表示完全正确，2表示部分正确", example = "1")
    private Integer isCorrect;

    @Schema(description = "获得分数", example = "5")
    private Integer scoreObtained;

    @Schema(description = "答题耗时（秒）", example = "30")
    private Integer timeSpent;

    @Schema(description = "答题会话ID")
    private String sessionId;

    @Schema(description = "答题时间")
    private LocalDateTime answerTime;
}
