package com.rauio.smartdangjian.server.learning.pojo.vo;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Date;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("UserChapterProgressVO 用户章节进度视图对象")
class UserChapterProgressVOTest {

    @Test
    @DisplayName("使用 builder 构造 VO")
    void buildVO() {
        Date now = new Date();
        UserChapterProgressVO vo = UserChapterProgressVO.builder()
                .id("p-1")
                .userId("user-1")
                .chapterId("ch-1")
                .progress(80)
                .status("completed")
                .completedAt(now)
                .build();

        assertThat(vo.getId()).isEqualTo("p-1");
        assertThat(vo.getUserId()).isEqualTo("user-1");
        assertThat(vo.getProgress()).isEqualTo(80);
        assertThat(vo.getStatus()).isEqualTo("completed");
        assertThat(vo.getCompletedAt()).isEqualTo(now);
    }
}
