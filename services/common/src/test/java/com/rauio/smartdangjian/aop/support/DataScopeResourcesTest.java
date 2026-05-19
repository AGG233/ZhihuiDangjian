package com.rauio.smartdangjian.aop.support;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class DataScopeResourcesTest {

    @Test
    @DisplayName("USER_MANAGEMENT 常量为 USER_MANAGEMENT")
    void userManagement() {
        assertThat(DataScopeResources.USER_MANAGEMENT).isEqualTo("USER_MANAGEMENT");
    }

    @Test
    @DisplayName("LEARNING_RECORD 常量为 LEARNING_RECORD")
    void learningRecord() {
        assertThat(DataScopeResources.LEARNING_RECORD).isEqualTo("LEARNING_RECORD");
    }

    @Test
    @DisplayName("CHAPTER_PROGRESS 常量为 CHAPTER_PROGRESS")
    void chapterProgress() {
        assertThat(DataScopeResources.CHAPTER_PROGRESS).isEqualTo("CHAPTER_PROGRESS");
    }

    @Test
    @DisplayName("COURSE_ADMIN 常量为 COURSE_ADMIN")
    void courseAdmin() {
        assertThat(DataScopeResources.COURSE_ADMIN).isEqualTo("COURSE_ADMIN");
    }

    @Test
    @DisplayName("CHAPTER_ADMIN 常量为 CHAPTER_ADMIN")
    void chapterAdmin() {
        assertThat(DataScopeResources.CHAPTER_ADMIN).isEqualTo("CHAPTER_ADMIN");
    }

    @Test
    @DisplayName("QUIZ_ADMIN 常量为 QUIZ_ADMIN")
    void quizAdmin() {
        assertThat(DataScopeResources.QUIZ_ADMIN).isEqualTo("QUIZ_ADMIN");
    }

    @Test
    @DisplayName("RESOURCE_META_ADMIN 常量为 RESOURCE_META_ADMIN")
    void resourceMetaAdmin() {
        assertThat(DataScopeResources.RESOURCE_META_ADMIN).isEqualTo("RESOURCE_META_ADMIN");
    }

    @Test
    @DisplayName("CATEGORY 常量为 CATEGORY")
    void category() {
        assertThat(DataScopeResources.CATEGORY).isEqualTo("CATEGORY");
    }
}
