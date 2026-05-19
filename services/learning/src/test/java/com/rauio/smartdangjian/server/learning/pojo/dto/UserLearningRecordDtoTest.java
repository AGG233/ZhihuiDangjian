package com.rauio.smartdangjian.server.learning.pojo.dto;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDateTime;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("UserLearningRecordDto 用户学习记录请求体")
class UserLearningRecordDtoTest {

    @Test
    @DisplayName("使用 builder 构造 DTO")
    void buildDto() {
        LocalDateTime now = LocalDateTime.now();
        UserLearningRecordDto dto = UserLearningRecordDto.builder()
                .id("r-1")
                .userId("user-1")
                .chapterId("ch-1")
                .startTime(now)
                .endTime(now.plusSeconds(3600))
                .duration(3600)
                .deviceType("web")
                .createdAt(now)
                .build();

        assertThat(dto.getId()).isEqualTo("r-1");
        assertThat(dto.getUserId()).isEqualTo("user-1");
        assertThat(dto.getDuration()).isEqualTo(3600);
        assertThat(dto.getDeviceType()).isEqualTo("web");
    }
}
