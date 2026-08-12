package com.rauio.smartdangjian.server.learning.pojo.dto;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("UserBehaviorDto 用户行为请求体")
class UserBehaviorDtoTest {

    @Test
    @DisplayName("构造 UserBehaviorDto")
    void buildDto() {
        UserBehaviorDto dto = new UserBehaviorDto();
        dto.setUserId(1L);
        dto.setChapterId(1L);

        assertThat(dto.getUserId()).isEqualTo(1L);
        assertThat(dto.getChapterId()).isEqualTo(1L);
    }
}
