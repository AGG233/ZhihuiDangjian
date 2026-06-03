package com.rauio.smartdangjian.server.quiz.pojo.dto;

import com.rauio.smartdangjian.server.quiz.pojo.entity.QuizOption;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 选项审查 DTO —— 包含正确答案标记，仅用于 AI 内容审查场景。</br>
 * 注意：与 {@link QuizOptionSummary} 不同，本 DTO 会暴露 isCorrect 字段，
 * 仅限于受信任的审查方（如 AI 审核工具）使用。
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class QuizOptionReviewDto {

    private Long id;
    private Long quizId;
    private String optionText;
    private Boolean isCorrect;
    private String orderIndex;

    /**
     * 将 QuizOption 实体转换为审查 DTO（包含 isCorrect）。
     *
     * @param option QuizOption 实体
     * @return 审查 DTO，若入参为 null 则返回 null
     */
    public static QuizOptionReviewDto from(QuizOption option) {
        if (option == null) {
            return null;
        }
        return QuizOptionReviewDto.builder()
                .id(option.getId())
                .quizId(option.getQuizId())
                .optionText(option.getOptionText())
                .isCorrect(option.getIsCorrect())
                .orderIndex(option.getOrderIndex())
                .build();
    }
}
