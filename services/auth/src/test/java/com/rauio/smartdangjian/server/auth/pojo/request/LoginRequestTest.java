package com.rauio.smartdangjian.server.auth.pojo.request;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class LoginRequestTest {

    @Test
    @DisplayName("无参构造后 platform 默认值为 web")
    void defaultPlatformIsWeb() {
        LoginRequest request = new LoginRequest();

        assertThat(request.getPlatform()).isEqualTo("web");
    }

    @Test
    @DisplayName("全参构造设置所有字段")
    void allArgsConstructorSetsAllFields() {
        LoginRequest request = new LoginRequest(
                "admin",
                "password123",
                "app",
                "captcha-uuid-123",
                "ABCD"
        );

        assertThat(request.getPassport()).isEqualTo("admin");
        assertThat(request.getPassword()).isEqualTo("password123");
        assertThat(request.getPlatform()).isEqualTo("app");
        assertThat(request.getCaptchaUUID()).isEqualTo("captcha-uuid-123");
        assertThat(request.getCaptchaCode()).isEqualTo("ABCD");
    }

    @Test
    @DisplayName("setter 设置字段后 getter 正确返回")
    void settersAndGettersWork() {
        LoginRequest request = new LoginRequest();

        request.setPassport("user@example.com");
        request.setPassword("secret");
        request.setPlatform("app");
        request.setCaptchaUUID("uuid-abc");
        request.setCaptchaCode("1234");

        assertThat(request.getPassport()).isEqualTo("user@example.com");
        assertThat(request.getPassword()).isEqualTo("secret");
        assertThat(request.getPlatform()).isEqualTo("app");
        assertThat(request.getCaptchaUUID()).isEqualTo("uuid-abc");
        assertThat(request.getCaptchaCode()).isEqualTo("1234");
    }
}
