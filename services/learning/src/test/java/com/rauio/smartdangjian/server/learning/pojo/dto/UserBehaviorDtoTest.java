package com.rauio.smartdangjian.server.learning.pojo.dto;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("UserBehaviorDto 用户行为请求体")
class UserBehaviorDtoTest {

    @Test
    @DisplayName("构造 UserBehaviorDto")
    void buildDto() {
        UserBehaviorDto dto = new UserBehaviorDto();
        dto.setUserId("user-1");
        dto.setChapterId("ch-1");

        assertThat(dto.getUserId()).isEqualTo("user-1");
        assertThat(dto.getChapterId()).isEqualTo("ch-1");
    }
}
