package com.rauio.smartdangjian.server.learning.pojo.entity;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDateTime;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("UserChapterProgress 用户章节学习进度实体")
class UserChapterProgressTest {

    @Test
    @DisplayName("使用 builder 构造实体")
    void buildEntity() {
        LocalDateTime now = LocalDateTime.now();
        UserChapterProgress entity = UserChapterProgress.builder()
                .id("p-1")
                .userId("user-1")
                .chapterId("ch-1")
                .progress(75)
                .status("in_progress")
                .firstViewedAt(now)
                .completedAt(null)
                .updatedAt(now)
                .build();

        assertThat(entity.getId()).isEqualTo("p-1");
        assertThat(entity.getUserId()).isEqualTo("user-1");
        assertThat(entity.getChapterId()).isEqualTo("ch-1");
        assertThat(entity.getProgress()).isEqualTo(75);
        assertThat(entity.getStatus()).isEqualTo("in_progress");
        assertThat(entity.getFirstViewedAt()).isEqualTo(now);
        assertThat(entity.getCompletedAt()).isNull();
        assertThat(entity.getUpdatedAt()).isEqualTo(now);
    }
}
