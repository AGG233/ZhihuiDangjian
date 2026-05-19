package com.rauio.smartdangjian.server.learning.pojo.vo;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Date;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("UserLearningRecordVO 用户学习记录视图对象")
class UserLearningRecordVOTest {

    @Test
    @DisplayName("使用 builder 构造 VO")
    void buildVO() {
        Date now = new Date();
        UserLearningRecordVO vo = UserLearningRecordVO.builder()
                .id("r-1")
                .userId("user-1")
                .chapterId("ch-1")
                .startTime(now)
                .endTime(now)
                .duration(1800)
                .deviceType("mobile")
                .createdAt(now)
                .build();

        assertThat(vo.getId()).isEqualTo("r-1");
        assertThat(vo.getUserId()).isEqualTo("user-1");
        assertThat(vo.getDuration()).isEqualTo(1800);
        assertThat(vo.getDeviceType()).isEqualTo("mobile");
    }
}
