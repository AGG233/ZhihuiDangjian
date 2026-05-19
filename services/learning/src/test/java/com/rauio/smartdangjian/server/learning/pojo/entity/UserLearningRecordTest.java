package com.rauio.smartdangjian.server.learning.pojo.entity;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("UserLearningRecord 用户学习记录实体")
class UserLearningRecordTest {

    @Test
    @DisplayName("使用 builder 构造实体")
    void buildEntity() {
        LocalDateTime now = LocalDateTime.now();
        UserLearningRecord entity = UserLearningRecord.builder()
                .id("r-1")
                .userId("user-1")
                .chapterId("ch-1")
                .startTime(now)
                .endTime(now.plusSeconds(1800))
                .duration(1800)
                .deviceType("web")
                .createdAt(now)
                .build();

        assertThat(entity.getId()).isEqualTo("r-1");
        assertThat(entity.getUserId()).isEqualTo("user-1");
        assertThat(entity.getChapterId()).isEqualTo("ch-1");
        assertThat(entity.getDuration()).isEqualTo(1800);
        assertThat(entity.getDeviceType()).isEqualTo("web");
    }
}
