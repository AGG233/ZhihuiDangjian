package com.rauio.smartdangjian.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.rauio.smartdangjian.constants.ErrorConstants;
import com.rauio.smartdangjian.exception.BusinessException;
import com.rauio.smartdangjian.security.CurrentUserProvider;
import com.rauio.smartdangjian.security.LoginUser;
import com.rauio.smartdangjian.utils.spec.UserType;

@ExtendWith(MockitoExtension.class)
class PermissionValidatorTest {

    @Mock
    private CurrentUserProvider currentUserProvider;

    @InjectMocks
    private PermissionValidator validator;

    private void mockUser(UserType type, String id) {
        LoginUser user = LoginUser.builder().id(id).userType(type).build();
        when(currentUserProvider.getCurrentUser()).thenReturn(user);
    }

    @Test
    @DisplayName("MANAGER bypasses all resource access checks")
    void managerBypassesChecks() {
        mockUser(UserType.MANAGER, "1");
        validator.requireResourceAccess(999L);
        assertThat(validator.isResourceOwner(999L)).isTrue();
    }

    @Test
    @DisplayName("STUDENT owner can access own resource")
    void studentOwnerCanAccess() {
        mockUser(UserType.STUDENT, "42");
        validator.requireResourceAccess("42");
        assertThat(validator.isResourceOwner("42")).isTrue();
    }

    @Test
    @DisplayName("STUDENT cannot access others resource")
    void studentCannotAccessOthers() {
        mockUser(UserType.STUDENT, "42");
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
        when(currentUserProvider.getCurrentUser()).thenReturn(null);
        assertThatThrownBy(() -> validator.requireResourceAccess(1L)).isInstanceOf(BusinessException.class);
    }

    @Test
    @DisplayName("isResourceOwner returns false for null user")
    void isResourceOwnerFalseForNullUser() {
        when(currentUserProvider.getCurrentUser()).thenReturn(null);
        assertThat(validator.isResourceOwner(1L)).isFalse();
    }

    @Test
    @DisplayName("Null resourceOwnerId throws exception for non-manager")
    void nullResourceOwnerIdThrows() {
        mockUser(UserType.STUDENT, "1");
        assertThatThrownBy(() -> validator.requireResourceAccess(null)).isInstanceOf(BusinessException.class);
    }
}
