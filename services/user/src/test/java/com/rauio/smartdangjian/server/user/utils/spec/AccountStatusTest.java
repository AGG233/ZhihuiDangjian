package com.rauio.smartdangjian.server.user.utils.spec;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class AccountStatusTest {

    @Test
    @DisplayName("AccountStatus 包含 ACTIVE、INACTIVE、BANNED 三个枚举值")
    void enumValues() {
        assertThat(AccountStatus.values())
                .containsExactly(AccountStatus.ACTIVE, AccountStatus.INACTIVE, AccountStatus.BANNED);
    }

    @Test
    @DisplayName("valueOf ACTIVE 返回正确的枚举")
    void valueOfActive() {
        assertThat(AccountStatus.valueOf("ACTIVE")).isEqualTo(AccountStatus.ACTIVE);
    }

    @Test
    @DisplayName("valueOf INACTIVE 返回正确的枚举")
    void valueOfInactive() {
        assertThat(AccountStatus.valueOf("INACTIVE")).isEqualTo(AccountStatus.INACTIVE);
    }

    @Test
    @DisplayName("valueOf BANNED 返回正确的枚举")
    void valueOfBanned() {
        assertThat(AccountStatus.valueOf("BANNED")).isEqualTo(AccountStatus.BANNED);
    }

    @Test
    @DisplayName("ACTIVE getValue 返回 active")
    void activeGetValue() {
        assertThat(AccountStatus.ACTIVE.getValue()).isEqualTo("active");
    }

    @Test
    @DisplayName("INACTIVE getValue 返回 inactive")
    void inactiveGetValue() {
        assertThat(AccountStatus.INACTIVE.getValue()).isEqualTo("inactive");
    }

    @Test
    @DisplayName("BANNED getValue 返回 banned")
    void bannedGetValue() {
        assertThat(AccountStatus.BANNED.getValue()).isEqualTo("banned");
    }

    @Test
    @DisplayName("toString 返回 value 对应的字符串")
    void toStringReturnsValue() {
        assertThat(AccountStatus.ACTIVE.toString()).isEqualTo("active");
        assertThat(AccountStatus.INACTIVE.toString()).isEqualTo("inactive");
        assertThat(AccountStatus.BANNED.toString()).isEqualTo("banned");
    }
}
