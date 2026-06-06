package com.rauio.smartdangjian.server.quiz.pojo.dto;

import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 用户答题记录摘要 —— 不暴露 UserQuizAnswer 实体。
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserQuizAnswerDto {
    private Long id;
    private Long userId;
    private Long quizId;
    private Long optionId;
    private Integer isCorrect;
    private Integer timeSpent;
    private LocalDateTime answerTime;
}
