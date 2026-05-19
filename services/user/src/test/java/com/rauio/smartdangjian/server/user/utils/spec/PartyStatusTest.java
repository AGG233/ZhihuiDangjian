package com.rauio.smartdangjian.server.user.utils.spec;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class PartyStatusTest {

    @Test
    @DisplayName("PartyStatus 包含 5 个枚举常量")
    void constants() {
        assertThat(PartyStatus.values()).hasSize(5);
    }

    @Test
    @DisplayName("FORMAL_MEMBER 描述为 正式党员")
    void formalMember() {
        assertThat(PartyStatus.FORMAL_MEMBER.getDescription()).isEqualTo("正式党员");
        assertThat(PartyStatus.FORMAL_MEMBER.toString()).isEqualTo("正式党员");
    }

    @Test
    @DisplayName("PROBATIONARY_MEMBER 描述为 预备党员")
    void probationaryMember() {
        assertThat(PartyStatus.PROBATIONARY_MEMBER.getDescription()).isEqualTo("预备党员");
    }

    @Test
    @DisplayName("DEVELOPMENT_TARGET 描述为 发展对象")
    void developmentTarget() {
        assertThat(PartyStatus.DEVELOPMENT_TARGET.getDescription()).isEqualTo("发展对象");
    }

    @Test
    @DisplayName("PARTY_ACTIVIST 描述为 积极分子")
    void partyActivist() {
        assertThat(PartyStatus.PARTY_ACTIVIST.getDescription()).isEqualTo("积极分子");
    }

    @Test
    @DisplayName("GENERAL_PUBLIC 描述为 群众")
    void generalPublic() {
        assertThat(PartyStatus.GENERAL_PUBLIC.getDescription()).isEqualTo("群众");
    }

    @Test
    @DisplayName("valueOf 正确反序列化")
    void valueOf() {
        assertThat(PartyStatus.valueOf("FORMAL_MEMBER")).isEqualTo(PartyStatus.FORMAL_MEMBER);
        assertThat(PartyStatus.valueOf("GENERAL_PUBLIC")).isEqualTo(PartyStatus.GENERAL_PUBLIC);
    }
}
