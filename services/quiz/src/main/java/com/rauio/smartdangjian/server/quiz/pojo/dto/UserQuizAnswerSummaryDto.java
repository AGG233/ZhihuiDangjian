package com.rauio.smartdangjian.server.quiz.pojo.dto;

public record UserQuizAnswerSummaryDto(Long userId, Long quizId, Integer isCorrect, Integer timeSpent) {}
