package com.rauio.smartdangjian.server.auth.pojo.request;

import com.rauio.smartdangjian.server.user.utils.spec.PartyStatus;
import com.rauio.smartdangjian.utils.spec.UserType;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

class RegisterRequestTest {

    @Test
    @DisplayName("无参构造创建对象后 setter 设置字段正确")
    void settersAndGettersWork() {
        RegisterRequest request = new RegisterRequest();
        LocalDateTime joinDate = LocalDateTime.of(2024, 1, 1, 0, 0);

        request.setType(UserType.STUDENT);
        request.setUsername("testuser");
        request.setPassword("Test@1234");
        request.setRealName("张三");
        request.setIdCard("110101199001011234");
        request.setPartyMemberId("12345678901234567890");
        request.setPartyStatus(PartyStatus.FORMAL_MEMBER);
        request.setBranchName("某某党支部");
        request.setEmail("test@example.com");
        request.setPhone("13800138000");
        request.setCaptchaUUID("uuid-123");
        request.setCaptchaCode("ABCD");
        request.setUniversityId("univ-1");
        request.setJoinPartyDate(joinDate);

        assertThat(request.getType()).isEqualTo(UserType.STUDENT);
        assertThat(request.getUsername()).isEqualTo("testuser");
        assertThat(request.getPassword()).isEqualTo("Test@1234");
        assertThat(request.getRealName()).isEqualTo("张三");
        assertThat(request.getIdCard()).isEqualTo("110101199001011234");
        assertThat(request.getPartyMemberId()).isEqualTo("12345678901234567890");
        assertThat(request.getPartyStatus()).isEqualTo(PartyStatus.FORMAL_MEMBER);
        assertThat(request.getBranchName()).isEqualTo("某某党支部");
        assertThat(request.getEmail()).isEqualTo("test@example.com");
        assertThat(request.getPhone()).isEqualTo("13800138000");
        assertThat(request.getCaptchaUUID()).isEqualTo("uuid-123");
        assertThat(request.getCaptchaCode()).isEqualTo("ABCD");
        assertThat(request.getUniversityId()).isEqualTo("univ-1");
        assertThat(request.getJoinPartyDate()).isEqualTo(joinDate);
    }

    @Test
    @DisplayName("创建后字段默认为 null")
    void defaultFieldsAreNull() {
        RegisterRequest request = new RegisterRequest();

        assertThat(request.getUsername()).isNull();
        assertThat(request.getPassword()).isNull();
        assertThat(request.getEmail()).isNull();
        assertThat(request.getPhone()).isNull();
    }
}
