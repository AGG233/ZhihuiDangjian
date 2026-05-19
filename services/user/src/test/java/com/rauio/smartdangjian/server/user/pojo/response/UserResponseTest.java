package com.rauio.smartdangjian.server.user.pojo.response;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDateTime;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.rauio.smartdangjian.server.user.utils.spec.AccountStatus;
import com.rauio.smartdangjian.server.user.utils.spec.PartyStatus;
import com.rauio.smartdangjian.utils.spec.UserType;

class UserResponseTest {

    @Test
    @DisplayName("UserResponse setter 和 getter 正确工作")
    void settersAndGetters() {
        LocalDateTime joinDate = LocalDateTime.of(2025, 3, 15, 9, 30);
        UserResponse response = new UserResponse();

        response.setId("user-1");
        response.setUsername("testuser");
        response.setRealName("张三");
        response.setPartyMemberId("pm-001");
        response.setPartyStatus(PartyStatus.FORMAL_MEMBER);
        response.setBranchName("党支部A");
        response.setUserType(UserType.STUDENT);
        response.setStatus(AccountStatus.ACTIVE);
        response.setUniversityId("univ-1");
        response.setJoinPartyDate(joinDate);
        response.setEmail("test@example.com");
        response.setPhone("13800138000");

        assertThat(response.getId()).isEqualTo("user-1");
        assertThat(response.getUsername()).isEqualTo("testuser");
        assertThat(response.getRealName()).isEqualTo("张三");
        assertThat(response.getPartyMemberId()).isEqualTo("pm-001");
        assertThat(response.getPartyStatus()).isEqualTo(PartyStatus.FORMAL_MEMBER);
        assertThat(response.getBranchName()).isEqualTo("党支部A");
        assertThat(response.getUserType()).isEqualTo(UserType.STUDENT);
        assertThat(response.getStatus()).isEqualTo(AccountStatus.ACTIVE);
        assertThat(response.getUniversityId()).isEqualTo("univ-1");
        assertThat(response.getJoinPartyDate()).isEqualTo(joinDate);
        assertThat(response.getEmail()).isEqualTo("test@example.com");
        assertThat(response.getPhone()).isEqualTo("13800138000");
    }

    @Test
    @DisplayName("UserResponse 默认字段为 null")
    void defaultValuesAreNull() {
        UserResponse response = new UserResponse();

        assertThat(response.getId()).isNull();
        assertThat(response.getUsername()).isNull();
        assertThat(response.getRealName()).isNull();
        assertThat(response.getPartyMemberId()).isNull();
        assertThat(response.getPartyStatus()).isNull();
        assertThat(response.getBranchName()).isNull();
        assertThat(response.getUserType()).isNull();
        assertThat(response.getStatus()).isNull();
        assertThat(response.getUniversityId()).isNull();
        assertThat(response.getJoinPartyDate()).isNull();
        assertThat(response.getEmail()).isNull();
        assertThat(response.getPhone()).isNull();
    }
}
