package com.rauio.smartdangjian.server.learning.pojo.response;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Date;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("UserChapterProgressResponse 用户章节进度视图对象")
class UserChapterProgressResponseTest {

    @Test
    @DisplayName("使用 builder 构造 Response")
    void buildResponse() {
        Date now = new Date();
        UserChapterProgressResponse response = UserChapterProgressResponse.builder()
                .id("p-1")
                .userId("user-1")
                .chapterId("ch-1")
                .progress(80)
                .status("completed")
                .completedAt(now)
                .build();

        assertThat(response.getId()).isEqualTo("p-1");
        assertThat(response.getUserId()).isEqualTo("user-1");
        assertThat(response.getProgress()).isEqualTo(80);
        assertThat(response.getStatus()).isEqualTo("completed");
        assertThat(response.getCompletedAt()).isEqualTo(now);
    }
}
