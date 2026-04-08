package com.rauio.smartdangjian.server.quiz.pojo.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
@TableName("user_quiz_answer")
@Schema(description = "用户答题记录")
public class UserQuizAnswer {

    @TableId
    @JsonSerialize(using = ToStringSerializer.class)
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

    @Schema(description = "是否回答正确")
    private Boolean isCorrect;

    @Schema(description = "获得分数", example = "5")
    private Integer scoreObtained;

    @Schema(description = "答题耗时（秒）", example = "30")
    private Integer timeSpent;

    @Schema(description = "答题会话ID")
    private String sessionId;

    @Schema(description = "答题时间")
    private LocalDateTime answerTime;

}
