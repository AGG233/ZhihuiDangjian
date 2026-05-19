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
@Schema(description = "测验题目响应")
public class QuizResponse {

    @Schema(description = "题目ID")
    private String id;

    @Schema(description = "章节ID")
    private String chapterId;

    @Schema(description = "题目内容")
    private String question;

    @Schema(description = "题目类型", example = "single_choice")
    private String questionType;

    @Schema(description = "题目分值", example = "5")
    private Integer score;

    @Schema(description = "难度", example = "medium")
    private String difficulty;

    @Schema(description = "题目解析")
    private String explanation;

    @Schema(description = "是否启用")
    private Boolean isActive;

    @Schema(description = "创建时间")
    private LocalDateTime createdAt;

    @Schema(description = "更新时间")
    private LocalDateTime updatedAt;
}
