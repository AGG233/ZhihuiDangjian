package com.rauio.smartdangjian.server.user.constants;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class UserErrorConstantsTest {

    @Test
    @DisplayName("PHONE_EXISTS 常数值为 2001")
    void phoneExists() {
        assertThat(UserErrorConstants.PHONE_EXISTS).isEqualTo(2001);
    }

    @Test
    @DisplayName("EMAIL_EXISTS 常数值为 2002")
    void emailExists() {
        assertThat(UserErrorConstants.EMAIL_EXISTS).isEqualTo(2002);
    }

    @Test
    @DisplayName("USERNAME_EXISTS 常数值为 2003")
    void usernameExists() {
        assertThat(UserErrorConstants.USERNAME_EXISTS).isEqualTo(2003);
    }

    @Test
    @DisplayName("PARTY_MEMBER_ID_EXISTS 常数值为 2004")
    void partyMemberIdExists() {
        assertThat(UserErrorConstants.PARTY_MEMBER_ID_EXISTS).isEqualTo(2004);
    }

    @Test
    @DisplayName("USER_NOT_EXISTS 常数值为 2005")
    void userNotExists() {
        assertThat(UserErrorConstants.USER_NOT_EXISTS).isEqualTo(2005);
    }

    @Test
    @DisplayName("EMPTY_ARGS 常数值为 2006")
    void emptyArgs() {
        assertThat(UserErrorConstants.EMPTY_ARGS).isEqualTo(2006);
    }

    @Test
    @DisplayName("PASSWORD_CHANGE_ERROR 常数值为 2007")
    void passwordChangeError() {
        assertThat(UserErrorConstants.PASSWORD_CHANGE_ERROR).isEqualTo(2007);
    }
}
