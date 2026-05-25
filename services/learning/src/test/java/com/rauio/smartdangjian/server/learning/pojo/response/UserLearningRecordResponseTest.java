package com.rauio.smartdangjian.server.learning.pojo.response;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Date;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("UserLearningRecordResponse 用户学习记录视图对象")
class UserLearningRecordResponseTest {

    @Test
    @DisplayName("使用 builder 构造 Response")
    void buildResponse() {
        Date now = new Date();
        UserLearningRecordResponse response = UserLearningRecordResponse.builder()
                .id(1L)
                .userId(1L)
                .chapterId(1L)
                .startTime(now)
                .endTime(now)
                .duration(1800)
                .deviceType("mobile")
                .createdAt(now)
                .build();

        assertThat(response.getId()).isEqualTo(1L);
        assertThat(response.getUserId()).isEqualTo(1L);
        assertThat(response.getDuration()).isEqualTo(1800);
        assertThat(response.getDeviceType()).isEqualTo("mobile");
    }
}
