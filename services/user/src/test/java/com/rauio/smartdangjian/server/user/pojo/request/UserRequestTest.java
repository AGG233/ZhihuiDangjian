package com.rauio.smartdangjian.server.user.pojo.request;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDateTime;
import java.util.UUID;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.rauio.smartdangjian.server.user.utils.spec.AccountStatus;
import com.rauio.smartdangjian.server.user.utils.spec.PartyStatus;
import com.rauio.smartdangjian.utils.spec.UserType;

class UserRequestTest {

    @Test
    @DisplayName("UserRequest setter 和 getter 正确工作")
    void settersAndGetters() {
        LocalDateTime joinDate = LocalDateTime.of(2025, 3, 15, 9, 30);
        UserRequest request = new UserRequest();
        String testPassword = UUID.randomUUID().toString();

        request.setUserId("user-1");
        request.setUsername("testuser");
        request.setRealName("张三");
        request.setPassword(testPassword);
        request.setPartyMemberId("pm-001");
        request.setPartyStatus(PartyStatus.FORMAL_MEMBER);
        request.setBranchName("党支部A");
        request.setUserType(UserType.STUDENT);
        request.setStatus(AccountStatus.ACTIVE);
        request.setUniversityId("univ-1");
        request.setJoinPartyDate(joinDate);
        request.setEmail("test@example.com");
        request.setPhone("13800138000");
        request.setIdCard("110101199001011234");

        assertThat(request.getUserId()).isEqualTo("user-1");
        assertThat(request.getUsername()).isEqualTo("testuser");
        assertThat(request.getRealName()).isEqualTo("张三");
        assertThat(request.getPassword()).isEqualTo(testPassword);
        assertThat(request.getPartyMemberId()).isEqualTo("pm-001");
        assertThat(request.getPartyStatus()).isEqualTo(PartyStatus.FORMAL_MEMBER);
        assertThat(request.getBranchName()).isEqualTo("党支部A");
        assertThat(request.getUserType()).isEqualTo(UserType.STUDENT);
        assertThat(request.getStatus()).isEqualTo(AccountStatus.ACTIVE);
        assertThat(request.getUniversityId()).isEqualTo("univ-1");
        assertThat(request.getJoinPartyDate()).isEqualTo(joinDate);
        assertThat(request.getEmail()).isEqualTo("test@example.com");
        assertThat(request.getPhone()).isEqualTo("13800138000");
        assertThat(request.getIdCard()).isEqualTo("110101199001011234");
    }

    @Test
    @DisplayName("UserRequest 默认字段为 null")
    void defaultValuesAreNull() {
        UserRequest request = new UserRequest();

        assertThat(request.getUserId()).isNull();
        assertThat(request.getUsername()).isNull();
        assertThat(request.getRealName()).isNull();
        assertThat(request.getPassword()).isNull();
        assertThat(request.getPartyMemberId()).isNull();
        assertThat(request.getPartyStatus()).isNull();
        assertThat(request.getBranchName()).isNull();
        assertThat(request.getUserType()).isNull();
        assertThat(request.getStatus()).isNull();
        assertThat(request.getUniversityId()).isNull();
        assertThat(request.getJoinPartyDate()).isNull();
        assertThat(request.getEmail()).isNull();
        assertThat(request.getPhone()).isNull();
        assertThat(request.getIdCard()).isNull();
    }
}
