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
                .refreshToken("refresh-token-def")
                .build();

        assertThat(response.getAccessToken()).isEqualTo("access-token-abc");
        assertThat(response.getRefreshToken()).isEqualTo("refresh-token-def");
    }

    @Test
    @DisplayName("无参构造创建对象后 setter 设置字段正确")
    void settersAndGettersWork() {
        LoginResponse response = new LoginResponse();

        response.setAccessToken("new-access-token");
        response.setRefreshToken("new-refresh-token");

        assertThat(response.getAccessToken()).isEqualTo("new-access-token");
        assertThat(response.getRefreshToken()).isEqualTo("new-refresh-token");
    }

    @Test
    @DisplayName("refreshToken 标注 @Deprecated 但仍可使用")
    void refreshTokenIsDeprecated() {
        LoginResponse response =
                LoginResponse.builder().refreshToken("deprecated-token").build();

        assertThat(response.getRefreshToken()).isEqualTo("deprecated-token");
    }
}
