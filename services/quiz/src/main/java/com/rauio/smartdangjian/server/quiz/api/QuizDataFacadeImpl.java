package com.rauio.smartdangjian.server.quiz.api;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import com.rauio.smartdangjian.server.quiz.pojo.dto.QuizOptionReviewDto;
import com.rauio.smartdangjian.server.quiz.pojo.dto.QuizOptionSummary;
import com.rauio.smartdangjian.server.quiz.pojo.dto.QuizSummary;
import com.rauio.smartdangjian.server.quiz.pojo.entity.Quiz;
import com.rauio.smartdangjian.server.quiz.pojo.entity.QuizOption;
import com.rauio.smartdangjian.server.quiz.service.QuizOptionService;
import com.rauio.smartdangjian.server.quiz.service.QuizService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class QuizDataFacadeImpl implements QuizDataFacade {

    private final QuizService quizService;
    private final QuizOptionService quizOptionService;

    @Override
    public QuizSummary getQuiz(Long quizId) {
        Quiz quiz = quizService.getById(quizId);
        if (quiz == null) {
            return null;
        }
        return toQuizSummary(quiz);
    }

    @Override
    public List<QuizSummary> getQuizzesByChapter(Long chapterId) {
        List<Quiz> quizzes = quizService.getByChapterId(chapterId);
        if (quizzes == null || quizzes.isEmpty()) {
            return Collections.emptyList();
        }
        return quizzes.stream()
                .filter(Objects::nonNull)
                .map(this::toQuizSummary)
                .collect(Collectors.toList());
    }

    @Override
    public Long createQuiz(
            Long chapterId,
            String question,
            String questionType,
            Integer score,
            String difficulty,
            String explanation,
            List<Map<String, Object>> options) {
        Quiz quiz = Quiz.builder()
                .chapterId(chapterId)
                .question(question)
                .questionType(questionType)
                .score(score)
                .difficulty(difficulty)
                .explanation(explanation)
                .isActive(true)
                .build();
        quizService.save(quiz);
        return quiz.getId();
    }

    @Override
    public boolean updateQuiz(
            Long quizId, String question, Integer score, String difficulty, String explanation, Boolean isActive) {
        Quiz quiz = quizService.getById(quizId);
        if (quiz == null) {
            return false;
        }
        if (question != null) {
            quiz.setQuestion(question);
        }
        if (score != null) {
            quiz.setScore(score);
        }
        if (difficulty != null) {
            quiz.setDifficulty(difficulty);
        }
        if (explanation != null) {
            quiz.setExplanation(explanation);
        }
        if (isActive != null) {
            quiz.setIsActive(isActive);
        }
        return quizService.updateById(quiz);
    }

    @Override
    public boolean deleteQuiz(Long quizId) {
        return quizService.removeById(quizId);
    }

    @Override
    public List<QuizOptionSummary> getOptionsByQuizId(Long quizId) {
        List<QuizOption> options = quizOptionService.getByQuizId(quizId);
        if (options == null || options.isEmpty()) {
            return Collections.emptyList();
        }
        return options.stream()
                .filter(Objects::nonNull)
                .map(QuizOptionSummary::from)
                .collect(Collectors.toList());
    }

    @Override
    public List<QuizOptionReviewDto> getOptionsByQuizIdForReview(Long quizId) {
        List<QuizOption> options = quizOptionService.getByQuizId(quizId);
        if (options == null || options.isEmpty()) {
            return Collections.emptyList();
        }
        return options.stream()
                .filter(Objects::nonNull)
                .map(QuizOptionReviewDto::from)
                .collect(Collectors.toList());
    }

    private QuizSummary toQuizSummary(Quiz quiz) {
        return QuizSummary.builder()
                .id(quiz.getId())
                .chapterId(quiz.getChapterId())
                .question(quiz.getQuestion())
                .questionType(quiz.getQuestionType())
                .score(quiz.getScore())
                .difficulty(quiz.getDifficulty())
                .explanation(quiz.getExplanation())
                .isActive(quiz.getIsActive())
                .createdAt(quiz.getCreatedAt())
                .updatedAt(quiz.getUpdatedAt())
                .build();
    }
}
