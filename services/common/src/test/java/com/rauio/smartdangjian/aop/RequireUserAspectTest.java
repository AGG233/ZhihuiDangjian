package com.rauio.smartdangjian.aop;

import com.rauio.smartdangjian.aop.annotation.RequireUser;
import com.rauio.smartdangjian.constants.ErrorConstants;
import com.rauio.smartdangjian.exception.BusinessException;
import com.rauio.smartdangjian.security.CurrentUserPrincipal;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.when;

class RequireUserAspectTest {

    private final RequireUserAspect aspect = new RequireUserAspect();
    private MockedStatic<SecurityContextHolder> securityContextMock;
    private SecurityContext securityContext;
    private Authentication authentication;

    @BeforeEach
    void setUp() {
        securityContextMock = mockStatic(SecurityContextHolder.class);
        securityContext = mock(SecurityContext.class);
        authentication = mock(Authentication.class);
        securityContextMock.when(SecurityContextHolder::getContext).thenReturn(securityContext);
    }

    @AfterEach
    void tearDown() {
        securityContextMock.close();
    }

    @Test
    @DisplayName("authentication 为 null 时抛出 BusinessException(USER_NOT_EXISTS)")
    void authenticationNullThrows() {
        when(securityContext.getAuthentication()).thenReturn(null);

        assertThatThrownBy(() -> aspect.requireUser(null))
                .isInstanceOf(BusinessException.class)
                .extracting("code")
                .isEqualTo(ErrorConstants.USER_NOT_EXISTS);
    }

    @Test
    @DisplayName("authentication 未认证时抛出 BusinessException(USER_NOT_EXISTS)")
    void notAuthenticatedThrows() {
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.isAuthenticated()).thenReturn(false);

        assertThatThrownBy(() -> aspect.requireUser(null))
                .isInstanceOf(BusinessException.class)
                .extracting("code")
                .isEqualTo(ErrorConstants.USER_NOT_EXISTS);
    }

    @Test
    @DisplayName("authentication 为 AnonymousAuthenticationToken 时抛出 BusinessException(USER_NOT_EXISTS)")
    void anonymousTokenThrows() {
        AnonymousAuthenticationToken anonymousToken = mock(AnonymousAuthenticationToken.class);
        when(anonymousToken.isAuthenticated()).thenReturn(true);
        when(securityContext.getAuthentication()).thenReturn(anonymousToken);

        assertThatThrownBy(() -> aspect.requireUser(null))
                .isInstanceOf(BusinessException.class)
                .extracting("code")
                .isEqualTo(ErrorConstants.USER_NOT_EXISTS);
    }

    @Test
    @DisplayName("principal 不是 CurrentUserPrincipal 类型时抛出 BusinessException(USER_NOT_EXISTS)")
    void principalNotUserPrincipalThrows() {
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.isAuthenticated()).thenReturn(true);
        when(authentication.getPrincipal()).thenReturn("string-principal");

        assertThatThrownBy(() -> aspect.requireUser(null))
                .isInstanceOf(BusinessException.class)
                .extracting("code")
                .isEqualTo(ErrorConstants.USER_NOT_EXISTS);
    }

    @Test
    @DisplayName("principal 是 CurrentUserPrincipal 时不抛出异常")
    void validPrincipalPasses() {
        CurrentUserPrincipal principal = mock(CurrentUserPrincipal.class);
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.isAuthenticated()).thenReturn(true);
        when(authentication.getPrincipal()).thenReturn(principal);

        assertThatCode(() -> aspect.requireUser(null))
                .doesNotThrowAnyException();
    }
}
