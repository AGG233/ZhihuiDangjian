package com.rauio.smartdangjian.controller.factory;

import java.time.LocalDateTime;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.rauio.smartdangjian.server.quiz.pojo.entity.Quiz;
import com.rauio.smartdangjian.server.quiz.pojo.entity.QuizOption;
import com.rauio.smartdangjian.server.quiz.pojo.entity.UserQuizAnswer;

/**
 * Static factory for quiz test data — produces Quiz, QuizOption, UserQuizAnswer
 * entities and a JSON helper. All IDs are deterministic so jsonPath
 * assertions are predictable.
 */
public final class QuizTestDataFactory {

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper().registerModule(new JavaTimeModule());

    private QuizTestDataFactory() {}

    // ── Quiz ────────────────────────────────────────────────────────

    public static Quiz createQuiz() {
        return Quiz.builder()
                .id(1L)
                .chapterId(1L)
                .question("党的最高理想和最终目标是什么？")
                .questionType("single_choice")
                .score(5)
                .difficulty("medium")
                .explanation("根据党章规定...")
                .isActive(true)
                .build();
    }

    public static Quiz createQuiz(Long id) {
        Quiz q = createQuiz();
        q.setId(id);
        return q;
    }

    // ── QuizOption ──────────────────────────────────────────────────

    public static QuizOption createQuizOption() {
        return QuizOption.builder()
                .id(1L)
                .quizId(1L)
                .optionText("实现共产主义")
                .isCorrect(true)
                .orderIndex("A")
                .build();
    }

    public static QuizOption createQuizOption(
            Long id, Long quizId, String optionText, Boolean isCorrect, String orderIndex) {
        return QuizOption.builder()
                .id(id)
                .quizId(quizId)
                .optionText(optionText)
                .isCorrect(isCorrect)
                .orderIndex(orderIndex)
                .build();
    }

    // ── UserQuizAnswer ──────────────────────────────────────────────

    public static UserQuizAnswer createUserQuizAnswer() {
        return UserQuizAnswer.builder()
                .id(1L)
                .userId(1L)
                .quizId(1L)
                .optionId(1L)
                .userAnswer("A")
                .isCorrect(1)
                .scoreObtained(5)
                .timeSpent(30)
                .sessionId("1")
                .answerTime(LocalDateTime.now())
                .build();
    }

    public static UserQuizAnswer createUserQuizAnswer(Long id, Long userId, Long quizId, Long optionId) {
        return UserQuizAnswer.builder()
                .id(id)
                .userId(userId)
                .quizId(quizId)
                .optionId(optionId)
                .userAnswer("A")
                .isCorrect(1)
                .scoreObtained(5)
                .timeSpent(30)
                .build();
    }

    // ── JSON helper ─────────────────────────────────────────────────

    public static String toJson(Object obj) {
        try {
            return OBJECT_MAPPER.writeValueAsString(obj);
        } catch (Exception e) {
            throw new RuntimeException("Failed to serialize to JSON", e);
        }
    }
}
