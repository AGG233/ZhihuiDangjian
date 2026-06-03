package com.rauio.smartdangjian.server.quiz.pojo.dto;

import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 测验摘要 —— 不暴露 Quiz 实体。
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class QuizSummary {
    private Long id;
    private Long chapterId;
    private String question;
    private String questionType;
    private Integer score;
    private String difficulty;
    private String explanation;
    private Boolean isActive;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
