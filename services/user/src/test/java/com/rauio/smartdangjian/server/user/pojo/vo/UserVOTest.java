package com.rauio.smartdangjian.server.user.pojo.vo;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDateTime;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.rauio.smartdangjian.server.user.utils.spec.AccountStatus;
import com.rauio.smartdangjian.server.user.utils.spec.PartyStatus;
import com.rauio.smartdangjian.utils.spec.UserType;

class UserVOTest {

    @Test
    @DisplayName("UserVO setter 和 getter 正确工作")
    void settersAndGetters() {
        LocalDateTime joinDate = LocalDateTime.of(2025, 3, 15, 9, 30);
        UserVO vo = new UserVO();

        vo.setId("user-1");
        vo.setUsername("testuser");
        vo.setRealName("张三");
        vo.setPartyMemberId("pm-001");
        vo.setPartyStatus(PartyStatus.FORMAL_MEMBER);
        vo.setBranchName("党支部A");
        vo.setUserType(UserType.STUDENT);
        vo.setStatus(AccountStatus.ACTIVE);
        vo.setUniversityId("univ-1");
        vo.setJoinPartyDate(joinDate);
        vo.setEmail("test@example.com");
        vo.setPhone("13800138000");

        assertThat(vo.getId()).isEqualTo("user-1");
        assertThat(vo.getUsername()).isEqualTo("testuser");
        assertThat(vo.getRealName()).isEqualTo("张三");
        assertThat(vo.getPartyMemberId()).isEqualTo("pm-001");
        assertThat(vo.getPartyStatus()).isEqualTo(PartyStatus.FORMAL_MEMBER);
        assertThat(vo.getBranchName()).isEqualTo("党支部A");
        assertThat(vo.getUserType()).isEqualTo(UserType.STUDENT);
        assertThat(vo.getStatus()).isEqualTo(AccountStatus.ACTIVE);
        assertThat(vo.getUniversityId()).isEqualTo("univ-1");
        assertThat(vo.getJoinPartyDate()).isEqualTo(joinDate);
        assertThat(vo.getEmail()).isEqualTo("test@example.com");
        assertThat(vo.getPhone()).isEqualTo("13800138000");
    }

    @Test
    @DisplayName("UserVO 默认字段为 null")
    void defaultValuesAreNull() {
        UserVO vo = new UserVO();

        assertThat(vo.getId()).isNull();
        assertThat(vo.getUsername()).isNull();
        assertThat(vo.getRealName()).isNull();
        assertThat(vo.getPartyMemberId()).isNull();
        assertThat(vo.getPartyStatus()).isNull();
        assertThat(vo.getBranchName()).isNull();
        assertThat(vo.getUserType()).isNull();
        assertThat(vo.getStatus()).isNull();
        assertThat(vo.getUniversityId()).isNull();
        assertThat(vo.getJoinPartyDate()).isNull();
        assertThat(vo.getEmail()).isNull();
        assertThat(vo.getPhone()).isNull();
    }
}
