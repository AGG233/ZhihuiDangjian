package com.rauio.smartdangjian.server.user.service;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class UserSimilarityServiceTest {

    @Test
    @DisplayName("创建 UserSimilarityService 实例")
    void create() {
        var service = new UserSimilarityService();
        assertThat(service).isNotNull();
    }
}
