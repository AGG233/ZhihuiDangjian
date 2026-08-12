package com.rauio.smartdangjian.server.learning.pojo.entity;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDateTime;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("UserLearningRecord 用户学习记录实体")
class UserLearningRecordTest {

    @Test
    @DisplayName("使用 builder 构造实体")
    void buildEntity() {
        LocalDateTime now = LocalDateTime.now();
        UserLearningRecord entity = UserLearningRecord.builder()
                .id(1L)
                .userId(1L)
                .chapterId(1L)
                .startTime(now)
                .endTime(now.plusSeconds(1800))
                .duration(1800)
                .deviceType("web")
                .createdAt(now)
                .build();

        assertThat(entity.getId()).isEqualTo(1L);
        assertThat(entity.getUserId()).isEqualTo(1L);
        assertThat(entity.getChapterId()).isEqualTo(1L);
        assertThat(entity.getDuration()).isEqualTo(1800);
        assertThat(entity.getDeviceType()).isEqualTo("web");
    }
}
