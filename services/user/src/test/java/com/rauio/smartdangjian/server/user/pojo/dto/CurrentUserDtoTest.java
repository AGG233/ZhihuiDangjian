package com.rauio.smartdangjian.server.user.pojo.dto;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.rauio.smartdangjian.utils.spec.UserType;

class CurrentUserDtoTest {

    @Test
    @DisplayName("CurrentUserDto 无参构造器字段为 null")
    void noArgsConstructor() {
        CurrentUserDto dto = new CurrentUserDto();

        assertThat(dto.getId()).isNull();
        assertThat(dto.getUserType()).isNull();
        assertThat(dto.getUniversityId()).isNull();
    }

    @Test
    @DisplayName("CurrentUserDto 全参构造器正确设置字段")
    void allArgsConstructor() {
        CurrentUserDto dto = new CurrentUserDto("user-1", UserType.STUDENT, "univ-1");

        assertThat(dto.getId()).isEqualTo("user-1");
        assertThat(dto.getUserType()).isEqualTo(UserType.STUDENT);
        assertThat(dto.getUniversityId()).isEqualTo("univ-1");
    }

    @Test
    @DisplayName("CurrentUserDto setter 和 getter 正确工作")
    void settersAndGetters() {
        CurrentUserDto dto = new CurrentUserDto();

        dto.setId("user-2");
        dto.setUserType(UserType.MANAGER);
        dto.setUniversityId("univ-2");

        assertThat(dto.getId()).isEqualTo("user-2");
        assertThat(dto.getUserType()).isEqualTo(UserType.MANAGER);
        assertThat(dto.getUniversityId()).isEqualTo("univ-2");
    }
}
