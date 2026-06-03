package com.rauio.smartdangjian.server.quiz.api;

import java.util.List;
import java.util.Map;

import com.rauio.smartdangjian.server.quiz.pojo.dto.QuizOptionReviewDto;
import com.rauio.smartdangjian.server.quiz.pojo.dto.QuizOptionSummary;
import com.rauio.smartdangjian.server.quiz.pojo.dto.QuizSummary;

/**
 * 测验数据门面 —— 供 AI 模块等业务方调用的稳定接口，封装 Quiz 的读写操作。
 */
public interface QuizDataFacade {

    QuizSummary getQuiz(Long quizId);

    List<QuizSummary> getQuizzesByChapter(Long chapterId);

    Long createQuiz(
            Long chapterId,
            String question,
            String questionType,
            Integer score,
            String difficulty,
            String explanation,
            List<Map<String, Object>> options);

    boolean updateQuiz(
            Long quizId, String question, Integer score, String difficulty, String explanation, Boolean isActive);

    boolean deleteQuiz(Long quizId);

    List<QuizOptionSummary> getOptionsByQuizId(Long quizId);

    /**
     * 获取指定题目的选项列表（审查专用，包含正确答案标记 isCorrect）。
     * <p>与 {@link #getOptionsByQuizId(Long)} 的区别：本方法会暴露 isCorrect 字段，
     * 供 AI 审查工具评估题目质量。</p>
     *
     * @param quizId 题目 ID
     * @return 选项审查 DTO 列表，包含正确答案标记
     */
    List<QuizOptionReviewDto> getOptionsByQuizIdForReview(Long quizId);
}
