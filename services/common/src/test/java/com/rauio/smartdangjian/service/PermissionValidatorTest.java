package com.rauio.smartdangjian.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;

import com.rauio.smartdangjian.constants.ErrorConstants;
import com.rauio.smartdangjian.exception.BusinessException;
import com.rauio.smartdangjian.security.CurrentUserPrincipal;
import com.rauio.smartdangjian.utils.SecurityUtils;
import com.rauio.smartdangjian.utils.spec.UserType;

@ExtendWith(MockitoExtension.class)
class PermissionValidatorTest {

    private final PermissionValidator validator = new PermissionValidator();
    private MockedStatic<SecurityUtils> securityUtilsMock;

    @BeforeEach
    void setUp() {
        securityUtilsMock = mockStatic(SecurityUtils.class);
    }

    @AfterEach
    void tearDown() {
        securityUtilsMock.close();
    }

    private void mockUser(UserType type, Long id) {
        CurrentUserPrincipal user = mock(CurrentUserPrincipal.class);
        lenient().when(user.getUserType()).thenReturn(type);
        lenient().when(user.getId()).thenReturn(id);
        securityUtilsMock.when(SecurityUtils::getCurrentUser).thenReturn(user);
    }

    @Test
    @DisplayName("MANAGER bypasses all resource access checks")
    void managerBypassesChecks() {
        mockUser(UserType.MANAGER, 1L);
        validator.requireResourceAccess(999L);
        assertThat(validator.isResourceOwner(999L)).isTrue();
    }

    @Test
    @DisplayName("STUDENT owner can access own resource")
    void studentOwnerCanAccess() {
        mockUser(UserType.STUDENT, 42L);
        validator.requireResourceAccess("42");
        assertThat(validator.isResourceOwner("42")).isTrue();
    }

    @Test
    @DisplayName("STUDENT cannot access others resource")
    void studentCannotAccessOthers() {
        mockUser(UserType.STUDENT, 42L);
        assertThatThrownBy(() -> validator.requireResourceAccess("99"))
                .isInstanceOf(BusinessException.class)
                .satisfies(e -> {
                    BusinessException be = (BusinessException) e;
                    assertThat(be.getCode()).isEqualTo(ErrorConstants.RESOURCE_NOT_AUTHORIZED);
                });
    }

    @Test
    @DisplayName("Null user throws exception")
    void nullUserThrowsException() {
        securityUtilsMock.when(SecurityUtils::getCurrentUser).thenReturn(null);
        assertThatThrownBy(() -> validator.requireResourceAccess(1L))
                .isInstanceOf(BusinessException.class);
    }

    @Test
    @DisplayName("isResourceOwner returns false for null user")
    void isResourceOwnerFalseForNullUser() {
        securityUtilsMock.when(SecurityUtils::getCurrentUser).thenReturn(null);
        assertThat(validator.isResourceOwner(1L)).isFalse();
    }

    @Test
    @DisplayName("Null resourceOwnerId throws exception for non-manager")
    void nullResourceOwnerIdThrows() {
        mockUser(UserType.STUDENT, 1L);
        assertThatThrownBy(() -> validator.requireResourceAccess(null))
                .isInstanceOf(BusinessException.class);
    }
}
