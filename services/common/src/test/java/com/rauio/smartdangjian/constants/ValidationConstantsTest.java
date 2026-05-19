package com.rauio.smartdangjian.constants;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class ValidationConstantsTest {

    @Test
    @DisplayName("EMAIL_PATTERN 匹配合法邮箱格式")
    void emailPatternMatchesValidEmails() {
        assertThat(ValidationConstants.EMAIL_PATTERN.matcher("test@example.com").matches()).isTrue();
        assertThat(ValidationConstants.EMAIL_PATTERN.matcher("user.name+tag@domain.co.jp").matches()).isTrue();
    }

    @Test
    @DisplayName("EMAIL_PATTERN 不匹配非法邮箱格式")
    void emailPatternRejectsInvalidEmails() {
        assertThat(ValidationConstants.EMAIL_PATTERN.matcher("notanemail").matches()).isFalse();
        assertThat(ValidationConstants.EMAIL_PATTERN.matcher("@missinguser.com").matches()).isFalse();
        assertThat(ValidationConstants.EMAIL_PATTERN.matcher("missingdomain@").matches()).isFalse();
        assertThat(ValidationConstants.EMAIL_PATTERN.matcher("").matches()).isFalse();
    }

    @Test
    @DisplayName("PHONE_PATTERN 匹配合法手机号")
    void phonePatternMatchesValidPhones() {
        assertThat(ValidationConstants.PHONE_PATTERN.matcher("13800138000").matches()).isTrue();
        assertThat(ValidationConstants.PHONE_PATTERN.matcher("15912345678").matches()).isTrue();
        assertThat(ValidationConstants.PHONE_PATTERN.matcher("18800001111").matches()).isTrue();
    }

    @Test
    @DisplayName("PHONE_PATTERN 拒绝非法手机号")
    void phonePatternRejectsInvalidPhones() {
        assertThat(ValidationConstants.PHONE_PATTERN.matcher("12345678901").matches()).isFalse();
        assertThat(ValidationConstants.PHONE_PATTERN.matcher("1380013800").matches()).isFalse();
        assertThat(ValidationConstants.PHONE_PATTERN.matcher("138001380000").matches()).isFalse();
        assertThat(ValidationConstants.PHONE_PATTERN.matcher("").matches()).isFalse();
    }

    @Test
    @DisplayName("PASSWORD_PATTERN 匹配包含字母数字和特殊字符的密码")
    void passwordPatternMatchesValidPasswords() {
        assertThat(ValidationConstants.PASSWORD_PATTERN.matcher("Password1!").matches()).isTrue();
        assertThat(ValidationConstants.PASSWORD_PATTERN.matcher("abc123@#$%").matches()).isTrue();
        assertThat(ValidationConstants.PASSWORD_PATTERN.matcher("Test@1234").matches()).isTrue();
    }

    @Test
    @DisplayName("PASSWORD_PATTERN 拒绝不包含特殊字符的密码")
    void passwordPatternRejectsWithoutSpecialChar() {
        assertThat(ValidationConstants.PASSWORD_PATTERN.matcher("Password1").matches()).isFalse();
        assertThat(ValidationConstants.PASSWORD_PATTERN.matcher("abcdefgh").matches()).isFalse();
    }

    @Test
    @DisplayName("PASSWORD_PATTERN 拒绝短于 8 位的密码")
    void passwordPatternRejectsShortPasswords() {
        assertThat(ValidationConstants.PASSWORD_PATTERN.matcher("Abc1!").matches()).isFalse();
    }
}
