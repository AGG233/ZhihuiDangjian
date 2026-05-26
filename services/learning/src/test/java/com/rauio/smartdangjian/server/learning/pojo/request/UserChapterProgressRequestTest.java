package com.rauio.smartdangjian.server.learning.pojo.request;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDateTime;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("UserChapterProgressRequest 用户章节进度请求体")
class UserChapterProgressRequestTest {

    @Test
    @DisplayName("使用 builder 构造 Request")
    void buildRequest() {
        LocalDateTime now = LocalDateTime.now();
        UserChapterProgressRequest request = UserChapterProgressRequest.builder()
                .id(1L)
                .userId(1L)
                .chapterId(1L)
                .progress(50)
                .status("in_progress")
                .firstViewedAt(now)
                .build();

        assertThat(request.getId()).isEqualTo(1L);
        assertThat(request.getUserId()).isEqualTo(1L);
        assertThat(request.getChapterId()).isEqualTo(1L);
        assertThat(request.getProgress()).isEqualTo(50);
        assertThat(request.getStatus()).isEqualTo("in_progress");
    }
}
