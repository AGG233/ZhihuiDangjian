package com.rauio.smartdangjian.server.user.pojo.vo;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.rauio.smartdangjian.server.user.utils.spec.PartyStatus;

class UserPublicVOTest {

    @Test
    @DisplayName("UserPublicVO setter 和 getter 正确工作")
    void settersAndGetters() {
        UserPublicVO vo = new UserPublicVO();

        vo.setId("user-1");
        vo.setUsername("testuser");
        vo.setRealName("张三");
        vo.setPartyMemberId("pm-001");
        vo.setPartyStatus(PartyStatus.FORMAL_MEMBER);
        vo.setBranchName("党支部A");

        assertThat(vo.getId()).isEqualTo("user-1");
        assertThat(vo.getUsername()).isEqualTo("testuser");
        assertThat(vo.getRealName()).isEqualTo("张三");
        assertThat(vo.getPartyMemberId()).isEqualTo("pm-001");
        assertThat(vo.getPartyStatus()).isEqualTo(PartyStatus.FORMAL_MEMBER);
        assertThat(vo.getBranchName()).isEqualTo("党支部A");
    }

    @Test
    @DisplayName("UserPublicVO 默认字段为 null")
    void defaultValuesAreNull() {
        UserPublicVO vo = new UserPublicVO();

        assertThat(vo.getId()).isNull();
        assertThat(vo.getUsername()).isNull();
        assertThat(vo.getRealName()).isNull();
        assertThat(vo.getPartyMemberId()).isNull();
        assertThat(vo.getPartyStatus()).isNull();
        assertThat(vo.getBranchName()).isNull();
    }
}
