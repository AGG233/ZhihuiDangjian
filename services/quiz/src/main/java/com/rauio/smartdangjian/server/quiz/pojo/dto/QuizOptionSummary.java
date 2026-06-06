package com.rauio.smartdangjian.server.quiz.pojo.dto;

import com.rauio.smartdangjian.server.quiz.pojo.entity.QuizOption;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 选项摘要 —— 不暴露 QuizOption 实体，不含正确答案标记。
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class QuizOptionSummary {
    private Long id;
    private Long quizId;
    private String optionText;
    private String orderIndex;

    public static QuizOptionSummary from(QuizOption option) {
        if (option == null) return null;
        return QuizOptionSummary.builder()
                .id(option.getId())
                .quizId(option.getQuizId())
                .optionText(option.getOptionText())
                .orderIndex(option.getOrderIndex())
                .build();
    }
}
