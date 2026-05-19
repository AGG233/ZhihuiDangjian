package com.rauio.smartdangjian.server.user.pojo.response;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.rauio.smartdangjian.server.user.utils.spec.PartyStatus;

class UserPublicResponseTest {

    @Test
    @DisplayName("UserPublicResponse setter 和 getter 正确工作")
    void settersAndGetters() {
        UserPublicResponse response = new UserPublicResponse();

        response.setId("user-1");
        response.setUsername("testuser");
        response.setRealName("张三");
        response.setPartyMemberId("pm-001");
        response.setPartyStatus(PartyStatus.FORMAL_MEMBER);
        response.setBranchName("党支部A");

        assertThat(response.getId()).isEqualTo("user-1");
        assertThat(response.getUsername()).isEqualTo("testuser");
        assertThat(response.getRealName()).isEqualTo("张三");
        assertThat(response.getPartyMemberId()).isEqualTo("pm-001");
        assertThat(response.getPartyStatus()).isEqualTo(PartyStatus.FORMAL_MEMBER);
        assertThat(response.getBranchName()).isEqualTo("党支部A");
    }

    @Test
    @DisplayName("UserPublicResponse 默认字段为 null")
    void defaultValuesAreNull() {
        UserPublicResponse response = new UserPublicResponse();

        assertThat(response.getId()).isNull();
        assertThat(response.getUsername()).isNull();
        assertThat(response.getRealName()).isNull();
        assertThat(response.getPartyMemberId()).isNull();
        assertThat(response.getPartyStatus()).isNull();
        assertThat(response.getBranchName()).isNull();
    }
}
