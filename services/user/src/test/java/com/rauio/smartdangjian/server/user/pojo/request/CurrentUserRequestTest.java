package com.rauio.smartdangjian.server.user.pojo.request;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.rauio.smartdangjian.utils.spec.UserType;

class CurrentUserRequestTest {

    @Test
    @DisplayName("CurrentUserRequest 无参构造器字段为 null")
    void noArgsConstructor() {
        CurrentUserRequest request = new CurrentUserRequest();

        assertThat(request.getId()).isNull();
        assertThat(request.getUserType()).isNull();
        assertThat(request.getUniversityId()).isNull();
    }

    @Test
    @DisplayName("CurrentUserRequest 全参构造器正确设置字段")
    void allArgsConstructor() {
        CurrentUserRequest request = new CurrentUserRequest("user-1", UserType.STUDENT, "univ-1");

        assertThat(request.getId()).isEqualTo("user-1");
        assertThat(request.getUserType()).isEqualTo(UserType.STUDENT);
        assertThat(request.getUniversityId()).isEqualTo("univ-1");
    }

    @Test
    @DisplayName("CurrentUserRequest setter 和 getter 正确工作")
    void settersAndGetters() {
        CurrentUserRequest request = new CurrentUserRequest();

        request.setId("user-2");
        request.setUserType(UserType.MANAGER);
        request.setUniversityId("univ-2");

        assertThat(request.getId()).isEqualTo("user-2");
        assertThat(request.getUserType()).isEqualTo(UserType.MANAGER);
        assertThat(request.getUniversityId()).isEqualTo("univ-2");
    }
}
