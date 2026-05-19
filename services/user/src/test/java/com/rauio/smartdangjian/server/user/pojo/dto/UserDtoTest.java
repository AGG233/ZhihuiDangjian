package com.rauio.smartdangjian.server.user.pojo.dto;

import com.rauio.smartdangjian.server.user.utils.spec.AccountStatus;
import com.rauio.smartdangjian.server.user.utils.spec.PartyStatus;
import com.rauio.smartdangjian.utils.spec.UserType;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

class UserDtoTest {

    @Test
    @DisplayName("UserDto setter 和 getter 正确工作")
    void settersAndGetters() {
        LocalDateTime joinDate = LocalDateTime.of(2025, 3, 15, 9, 30);
        UserDto dto = new UserDto();

        dto.setUserId("user-1");
        dto.setUsername("testuser");
        dto.setRealName("张三");
        dto.setPassword("secret123");
        dto.setPartyMemberId("pm-001");
        dto.setPartyStatus(PartyStatus.FORMAL_MEMBER);
        dto.setBranchName("党支部A");
        dto.setUserType(UserType.STUDENT);
        dto.setStatus(AccountStatus.ACTIVE);
        dto.setUniversityId("univ-1");
        dto.setJoinPartyDate(joinDate);
        dto.setEmail("test@example.com");
        dto.setPhone("13800138000");
        dto.setIdCard("110101199001011234");

        assertThat(dto.getUserId()).isEqualTo("user-1");
        assertThat(dto.getUsername()).isEqualTo("testuser");
        assertThat(dto.getRealName()).isEqualTo("张三");
        assertThat(dto.getPassword()).isEqualTo("secret123");
        assertThat(dto.getPartyMemberId()).isEqualTo("pm-001");
        assertThat(dto.getPartyStatus()).isEqualTo(PartyStatus.FORMAL_MEMBER);
        assertThat(dto.getBranchName()).isEqualTo("党支部A");
        assertThat(dto.getUserType()).isEqualTo(UserType.STUDENT);
        assertThat(dto.getStatus()).isEqualTo(AccountStatus.ACTIVE);
        assertThat(dto.getUniversityId()).isEqualTo("univ-1");
        assertThat(dto.getJoinPartyDate()).isEqualTo(joinDate);
        assertThat(dto.getEmail()).isEqualTo("test@example.com");
        assertThat(dto.getPhone()).isEqualTo("13800138000");
        assertThat(dto.getIdCard()).isEqualTo("110101199001011234");
    }

    @Test
    @DisplayName("UserDto 默认字段为 null")
    void defaultValuesAreNull() {
        UserDto dto = new UserDto();

        assertThat(dto.getUserId()).isNull();
        assertThat(dto.getUsername()).isNull();
        assertThat(dto.getRealName()).isNull();
        assertThat(dto.getPassword()).isNull();
        assertThat(dto.getPartyMemberId()).isNull();
        assertThat(dto.getPartyStatus()).isNull();
        assertThat(dto.getBranchName()).isNull();
        assertThat(dto.getUserType()).isNull();
        assertThat(dto.getStatus()).isNull();
        assertThat(dto.getUniversityId()).isNull();
        assertThat(dto.getJoinPartyDate()).isNull();
        assertThat(dto.getEmail()).isNull();
        assertThat(dto.getPhone()).isNull();
        assertThat(dto.getIdCard()).isNull();
    }
}
