package com.rauio.smartdangjian.server.content.constants;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class ChapterErrorConstantsTest {

    @Test
    @DisplayName("CHAPTER_NOT_FOUND 常数值为 3101")
    void chapterNotFound() {
        assertThat(ChapterErrorConstants.CHAPTER_NOT_FOUND).isEqualTo(3101);
    }

    @Test
    @DisplayName("CHAPTER_ALREADY_EXISTS 常数值为 3102")
    void chapterAlreadyExists() {
        assertThat(ChapterErrorConstants.CHAPTER_ALREADY_EXISTS).isEqualTo(3102);
    }

    @Test
    @DisplayName("CHAPTER_CREATE_FAILED 常数值为 3103")
    void chapterCreateFailed() {
        assertThat(ChapterErrorConstants.CHAPTER_CREATE_FAILED).isEqualTo(3103);
    }

    @Test
    @DisplayName("CHAPTER_MIN_REQUIRED 常数值为 3104")
    void chapterMinRequired() {
        assertThat(ChapterErrorConstants.CHAPTER_MIN_REQUIRED).isEqualTo(3104);
    }
}
