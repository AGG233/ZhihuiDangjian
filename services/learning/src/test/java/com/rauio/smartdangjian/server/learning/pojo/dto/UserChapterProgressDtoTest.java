package com.rauio.smartdangjian.server.learning.pojo.dto;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("UserChapterProgressDto 用户章节进度请求体")
class UserChapterProgressDtoTest {

    @Test
    @DisplayName("使用 builder 构造 DTO")
    void buildDto() {
        LocalDateTime now = LocalDateTime.now();
        UserChapterProgressDto dto = UserChapterProgressDto.builder()
                .id("p-1")
                .userId("user-1")
                .chapterId("ch-1")
                .progress(50)
                .status("in_progress")
                .firstViewedAt(now)
                .build();

        assertThat(dto.getId()).isEqualTo("p-1");
        assertThat(dto.getUserId()).isEqualTo("user-1");
        assertThat(dto.getChapterId()).isEqualTo("ch-1");
        assertThat(dto.getProgress()).isEqualTo(50);
        assertThat(dto.getStatus()).isEqualTo("in_progress");
    }
}
