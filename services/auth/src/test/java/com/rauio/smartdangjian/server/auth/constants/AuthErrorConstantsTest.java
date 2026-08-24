package com.rauio.smartdangjian.server.auth.constants;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class AuthErrorConstantsTest {

    @Test
    @DisplayName("TOKEN_INVALID_SUBJECT 常数值为 1001")
    void tokenInvalidSubject() {
        assertThat(AuthErrorConstants.TOKEN_INVALID_SUBJECT).isEqualTo(1001);
    }

    @Test
    @DisplayName("TOKEN_USER_NOT_FOUND 常数值为 1002")
    void tokenUserNotFound() {
        assertThat(AuthErrorConstants.TOKEN_USER_NOT_FOUND).isEqualTo(1002);
    }

    @Test
    @DisplayName("TOKEN_EXPIRED 常数值为 1003")
    void tokenExpired() {
        assertThat(AuthErrorConstants.TOKEN_EXPIRED).isEqualTo(1003);
    }

    @Test
    @DisplayName("TOKEN_VERIFICATION_FAILED 常数值为 1004")
    void tokenVerificationFailed() {
        assertThat(AuthErrorConstants.TOKEN_VERIFICATION_FAILED).isEqualTo(1004);
    }

    @Test
    @DisplayName("TOKEN_SERVER_ERROR 常数值为 1005")
    void tokenServerError() {
        assertThat(AuthErrorConstants.TOKEN_SERVER_ERROR).isEqualTo(1005);
    }

    @Test
    @DisplayName("TOKEN_DECODE_ERROR 常数值为 1006")
    void tokenDecodeError() {
        assertThat(AuthErrorConstants.TOKEN_DECODE_ERROR).isEqualTo(1006);
    }

    @Test
    @DisplayName("CAPTCHA_ERROR 常数值为 1010")
    void captchaError() {
        assertThat(AuthErrorConstants.CAPTCHA_ERROR).isEqualTo(1010);
    }

    @Test
    @DisplayName("PASSWORD_ERROR 常数值为 1011")
    void passwordError() {
        assertThat(AuthErrorConstants.PASSWORD_ERROR).isEqualTo(1011);
    }

    @Test
    @DisplayName("UNAUTHORIZED 常数值为 1012")
    void unauthorized() {
        assertThat(AuthErrorConstants.UNAUTHORIZED).isEqualTo(1012);
    }

    @Test
    @DisplayName("USER_NOT_FOUND 常数值为 1013")
    void userNotFound() {
        assertThat(AuthErrorConstants.USER_NOT_FOUND).isEqualTo(1013);
    }

    @Test
    @DisplayName("OLD_PASSWORD_ERROR 常数值为 1014")
    void oldPasswordError() {
        assertThat(AuthErrorConstants.OLD_PASSWORD_ERROR).isEqualTo(1014);
    }

    @Test
    @DisplayName("认证模块错误码范围在 1000-1999 之间")
    void errorCodeRange() {
        assertThat(AuthErrorConstants.TOKEN_INVALID_SUBJECT).isBetween(1000, 1999);
        assertThat(AuthErrorConstants.TOKEN_USER_NOT_FOUND).isBetween(1000, 1999);
        assertThat(AuthErrorConstants.TOKEN_EXPIRED).isBetween(1000, 1999);
        assertThat(AuthErrorConstants.TOKEN_VERIFICATION_FAILED).isBetween(1000, 1999);
        assertThat(AuthErrorConstants.TOKEN_SERVER_ERROR).isBetween(1000, 1999);
        assertThat(AuthErrorConstants.TOKEN_DECODE_ERROR).isBetween(1000, 1999);
        assertThat(AuthErrorConstants.CAPTCHA_ERROR).isBetween(1000, 1999);
        assertThat(AuthErrorConstants.PASSWORD_ERROR).isBetween(1000, 1999);
        assertThat(AuthErrorConstants.UNAUTHORIZED).isBetween(1000, 1999);
        assertThat(AuthErrorConstants.USER_NOT_FOUND).isBetween(1000, 1999);
        assertThat(AuthErrorConstants.OLD_PASSWORD_ERROR).isBetween(1000, 1999);
    }
}
