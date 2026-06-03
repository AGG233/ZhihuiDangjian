package com.rauio.smartdangjian.server.user.api;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;

import java.time.LocalDateTime;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.rauio.smartdangjian.server.user.pojo.response.UserResponse;
import com.rauio.smartdangjian.server.user.service.UserService;
import com.rauio.smartdangjian.server.user.utils.spec.AccountStatus;
import com.rauio.smartdangjian.server.user.utils.spec.PartyStatus;
import com.rauio.smartdangjian.utils.spec.UserType;

@ExtendWith(MockitoExtension.class)
class UserProfileQueryFacadeImplTest {

    @Mock
    private UserService userService;

    @InjectMocks
    private UserProfileQueryFacadeImpl facade;

    @Nested
    @DisplayName("getCurrentUserId()")
    class GetCurrentUserId {

        @Test
        @DisplayName("应委托 userService.getCurrentUserId() 并返回结果")
        void shouldDelegateAndReturn() {
            given(userService.getCurrentUserId()).willReturn("user-123");

            String result = facade.getCurrentUserId();

            assertThat(result).isEqualTo("user-123");
            then(userService).should().getCurrentUserId();
        }

        @Test
        @DisplayName("当 Service 返回 null 时应返回 null")
        void shouldReturnNullWhenServiceReturnsNull() {
            given(userService.getCurrentUserId()).willReturn(null);

            String result = facade.getCurrentUserId();

            assertThat(result).isNull();
        }
    }

    @Nested
    @DisplayName("getUserById(String)")
    class GetUserById {

        @Test
        @DisplayName("应委托 userService.get(id) 并返回用户信息")
        void shouldDelegateAndReturnUser() {
            UserResponse expected = new UserResponse();
            expected.setId(1L);
            expected.setUsername("testuser");
            expected.setRealName("张三");
            expected.setStatus(AccountStatus.ACTIVE);
            expected.setUserType(UserType.STUDENT);
            given(userService.get(1L)).willReturn(expected);

            UserResponse result = facade.getUserById("1");

            assertThat(result).isNotNull();
            assertThat(result.getId()).isEqualTo(1L);
            assertThat(result.getUsername()).isEqualTo("testuser");
            assertThat(result.getRealName()).isEqualTo("张三");
            then(userService).should().get(1L);
        }

        @Test
        @DisplayName("当用户不存在时应返回 null")
        void shouldReturnNullWhenUserNotFound() {
            given(userService.get(999L)).willReturn(null);

            UserResponse result = facade.getUserById("999");

            assertThat(result).isNull();
        }

        @Test
        @DisplayName("当 userId 为 null 时应返回 null")
        void shouldReturnNullForNullUserId() {
            UserResponse result = facade.getUserById(null);

            assertThat(result).isNull();
            then(userService).shouldHaveNoInteractions();
        }

        @Test
        @DisplayName("当 userId 为空字符串时应返回 null")
        void shouldReturnNullForEmptyUserId() {
            UserResponse result = facade.getUserById("");

            assertThat(result).isNull();
            then(userService).shouldHaveNoInteractions();
        }
    }
}
