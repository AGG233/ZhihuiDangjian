package com.rauio.smartdangjian.server.auth.pojo.response;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class LoginResponseTest {

    @Test
    @DisplayName("Builder 模式构建 LoginResponse 字段正确设置")
    void builderSetsFields() {
        LoginResponse response = LoginResponse.builder()
                .accessToken("access-token-abc")
                .build();

        assertThat(response.getAccessToken()).isEqualTo("access-token-abc");
    }

    @Test
    @DisplayName("无参构造创建对象后 setter 设置字段正确")
    void settersAndGettersWork() {
        LoginResponse response = new LoginResponse();

        response.setAccessToken("new-access-token");

        assertThat(response.getAccessToken()).isEqualTo("new-access-token");
    }
}
