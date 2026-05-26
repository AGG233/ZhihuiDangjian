package com.rauio.smartdangjian.server.quiz.constants;

import static org.assertj.core.api.Assertions.assertThat;

import java.lang.reflect.Constructor;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class QuizErrorConstantsTest {

    @Test
    @DisplayName("QUIZ_NOT_FOUND 值为 6001")
    void quizNotFoundIs6001() {
        assertThat(QuizErrorConstants.QUIZ_NOT_FOUND).isEqualTo(6001);
    }

    @Test
    @DisplayName("QUIZ_OPTION_NOT_FOUND 值为 6002")
    void quizOptionNotFoundIs6002() {
        assertThat(QuizErrorConstants.QUIZ_OPTION_NOT_FOUND).isEqualTo(6002);
    }

    @Test
    @DisplayName("CHAPTER_NOT_FOUND 值为 6003")
    void chapterNotFoundIs6003() {
        assertThat(QuizErrorConstants.CHAPTER_NOT_FOUND).isEqualTo(6003);
    }

    @Test
    @DisplayName("COURSE_NOT_FOUND 值为 6004")
    void courseNotFoundIs6004() {
        assertThat(QuizErrorConstants.COURSE_NOT_FOUND).isEqualTo(6004);
    }

    @Test
    @DisplayName("private 构造器覆盖")
    void privateConstructor() throws Exception {
        Constructor<QuizErrorConstants> constructor = QuizErrorConstants.class.getDeclaredConstructor();
        constructor.setAccessible(true);
        constructor.newInstance();
    }
}
