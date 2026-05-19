package com.rauio.smartdangjian.server.learning.pojo.request;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDateTime;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("UserLearningRecordRequest 用户学习记录请求体")
class UserLearningRecordRequestTest {

    @Test
    @DisplayName("使用 builder 构造 Request")
    void buildRequest() {
        LocalDateTime now = LocalDateTime.now();
        UserLearningRecordRequest request = UserLearningRecordRequest.builder()
                .id("r-1")
                .userId("user-1")
                .chapterId("ch-1")
                .startTime(now)
                .endTime(now.plusSeconds(3600))
                .duration(3600)
                .deviceType("web")
                .createdAt(now)
                .build();

        assertThat(request.getId()).isEqualTo("r-1");
        assertThat(request.getUserId()).isEqualTo("user-1");
        assertThat(request.getDuration()).isEqualTo(3600);
        assertThat(request.getDeviceType()).isEqualTo("web");
    }
}
