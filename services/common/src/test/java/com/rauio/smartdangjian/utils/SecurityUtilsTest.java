package com.rauio.smartdangjian.utils;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

import com.rauio.smartdangjian.security.CurrentUserPrincipal;
import com.rauio.smartdangjian.utils.spec.UserType;

class SecurityUtilsTest {

    private MockedStatic<SecurityContextHolder> securityContextMock;
    private SecurityContext securityContext;
    private Authentication authentication;

    @BeforeEach
    void setUp() {
        securityContextMock = mockStatic(SecurityContextHolder.class);
        securityContext = mock(SecurityContext.class);
        authentication = mock(Authentication.class);
        securityContextMock.when(SecurityContextHolder::getContext).thenReturn(securityContext);
        when(securityContext.getAuthentication()).thenReturn(authentication);
    }

    @AfterEach
    void tearDown() {
        securityContextMock.close();
    }

    // ================================================================
    // getCurrentUser
    // ================================================================

    @Test
    @DisplayName("getCurrentUser authentication 为 null 时返回 null")
    void getCurrentUserReturnsNullWhenAuthenticationNull() {
        when(securityContext.getAuthentication()).thenReturn(null);

        CurrentUserPrincipal result = SecurityUtils.getCurrentUser();

        assertThat(result).isNull();
    }

    @Test
    @DisplayName("getCurrentUser 未认证时返回 null")
    void getCurrentUserReturnsNullWhenNotAuthenticated() {
        when(authentication.isAuthenticated()).thenReturn(false);

        CurrentUserPrincipal result = SecurityUtils.getCurrentUser();

        assertThat(result).isNull();
    }

    @Test
    @DisplayName("getCurrentUser principal 为 anonymousUser 字符串时返回 null")
    void getCurrentUserReturnsNullForAnonymousUser() {
        when(authentication.isAuthenticated()).thenReturn(true);
        when(authentication.getPrincipal()).thenReturn("anonymousUser");

        CurrentUserPrincipal result = SecurityUtils.getCurrentUser();

        assertThat(result).isNull();
    }

    @Test
    @DisplayName("getCurrentUser principal 为 CurrentUserPrincipal 实例时返回该实例")
    void getCurrentUserReturnsPrincipalWhenValid() {
        CurrentUserPrincipal mockPrincipal = mock(CurrentUserPrincipal.class);
        when(authentication.isAuthenticated()).thenReturn(true);
        when(authentication.getPrincipal()).thenReturn(mockPrincipal);

        CurrentUserPrincipal result = SecurityUtils.getCurrentUser();

        assertThat(result).isEqualTo(mockPrincipal);
    }

    @Test
    @DisplayName("getCurrentUser principal 不是 CurrentUserPrincipal 类型时返回 null")
    void getCurrentUserReturnsNullWhenPrincipalNotUserPrincipal() {
        when(authentication.isAuthenticated()).thenReturn(true);
        when(authentication.getPrincipal()).thenReturn("some-other-principal");

        CurrentUserPrincipal result = SecurityUtils.getCurrentUser();

        assertThat(result).isNull();
    }

    // ================================================================
    // getCurrentUserId
    // ================================================================

    @Test
    @DisplayName("getCurrentUserId 用户已认证时返回用户 ID")
    void getCurrentUserIdReturnsIdWhenAuthenticated() {
        CurrentUserPrincipal mockPrincipal = mock(CurrentUserPrincipal.class);
        when(mockPrincipal.getId()).thenReturn("user-id-123");
        when(authentication.isAuthenticated()).thenReturn(true);
        when(authentication.getPrincipal()).thenReturn(mockPrincipal);

        String result = SecurityUtils.getCurrentUserId();

        assertThat(result).isEqualTo("user-id-123");
    }

    @Test
    @DisplayName("getCurrentUserId 用户未认证时返回 null")
    void getCurrentUserIdReturnsNullWhenNotAuthenticated() {
        when(authentication.isAuthenticated()).thenReturn(false);

        String result = SecurityUtils.getCurrentUserId();

        assertThat(result).isNull();
    }

    // ================================================================
    // getCurrentUserType
    // ================================================================

    @Test
    @DisplayName("getCurrentUserType 用户已认证时返回用户类型")
    void getCurrentUserTypeReturnsTypeWhenAuthenticated() {
        CurrentUserPrincipal mockPrincipal = mock(CurrentUserPrincipal.class);
        when(mockPrincipal.getUserType()).thenReturn(UserType.MANAGER);
        when(authentication.isAuthenticated()).thenReturn(true);
        when(authentication.getPrincipal()).thenReturn(mockPrincipal);

        UserType result = SecurityUtils.getCurrentUserType();

        assertThat(result).isEqualTo(UserType.MANAGER);
    }

    @Test
    @DisplayName("getCurrentUserType 用户未认证时返回 null")
    void getCurrentUserTypeReturnsNullWhenNotAuthenticated() {
        when(authentication.isAuthenticated()).thenReturn(false);

        UserType result = SecurityUtils.getCurrentUserType();

        assertThat(result).isNull();
    }
}
