package com.rauio.smartdangjian.controller.factory;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.rauio.smartdangjian.server.quiz.pojo.entity.Quiz;
import com.rauio.smartdangjian.server.quiz.pojo.entity.QuizOption;
import com.rauio.smartdangjian.server.quiz.pojo.entity.UserQuizAnswer;

import java.time.LocalDateTime;

/**
 * Static factory for quiz test data — produces Quiz, QuizOption, UserQuizAnswer
 * entities and a JSON helper. All IDs are deterministic so jsonPath
 * assertions are predictable.
 */
public final class QuizTestDataFactory {

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper()
            .registerModule(new JavaTimeModule());

    private QuizTestDataFactory() {
    }

    // ── Quiz ────────────────────────────────────────────────────────

    public static Quiz createQuiz() {
        return Quiz.builder()
                .id("quiz-1")
                .chapterId("chapter-1")
                .question("党的最高理想和最终目标是什么？")
                .questionType("single_choice")
                .score(5)
                .difficulty("medium")
                .explanation("根据党章规定...")
                .isActive(true)
                .build();
    }

    public static Quiz createQuiz(String id) {
        Quiz q = createQuiz();
        q.setId(id);
        return q;
    }

    // ── QuizOption ──────────────────────────────────────────────────

    public static QuizOption createQuizOption() {
        return QuizOption.builder()
                .id("opt-1")
                .quizId("quiz-1")
                .optionText("实现共产主义")
                .isCorrect(true)
                .orderIndex("A")
                .build();
    }

    public static QuizOption createQuizOption(String id, String quizId, String optionText, Boolean isCorrect, String orderIndex) {
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
                .id("answer-1")
                .userId("user-1")
                .quizId("quiz-1")
                .optionId("opt-1")
                .userAnswer("A")
                .isCorrect(1)
                .scoreObtained(5)
                .timeSpent(30)
                .sessionId("session-1")
                .answerTime(LocalDateTime.now())
                .build();
    }

    public static UserQuizAnswer createUserQuizAnswer(String id, String userId, String quizId, String optionId) {
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
