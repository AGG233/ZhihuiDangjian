package com.rauio.smartdangjian.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.rauio.smartdangjian.constants.ErrorConstants;
import com.rauio.smartdangjian.exception.BusinessException;
import com.rauio.smartdangjian.security.CurrentUserProvider;
import com.rauio.smartdangjian.security.LoginUser;
import com.rauio.smartdangjian.utils.spec.UserType;

import cn.dev33.satoken.exception.NotLoginException;

@ExtendWith(MockitoExtension.class)
class DataScopeServiceTest {

    @Mock
    private CurrentUserProvider currentUserProvider;

    private DataScopeService service;

    @BeforeEach
    void setUp() {
        service = new DataScopeService(currentUserProvider);
    }

    private void mockUser(UserType type, String universityId) {
        LoginUser user = LoginUser.builder()
                .id("1")
                .userType(type)
                .universityId(universityId)
                .build();
        when(currentUserProvider.getCurrentUser()).thenReturn(user);
    }

    @Test
    @DisplayName("MANAGER bypasses university check")
    void managerBypassesSameUniversity() {
        mockUser(UserType.MANAGER, null);
        service.requireSameUniversity("other");
        assertThat(service.isSameUniversity("other")).isTrue();
    }

    @Test
    @DisplayName("SCHOOL user with matching university passes")
    void schoolMatchingUniversityPasses() {
        mockUser(UserType.SCHOOL, "uni-1");
        service.requireSameUniversity("uni-1");
        assertThat(service.isSameUniversity("uni-1")).isTrue();
    }

    @Test
    @DisplayName("SCHOOL user with different university throws")
    void schoolDifferentUniversityThrows() {
        mockUser(UserType.SCHOOL, "uni-1");
        assertThatThrownBy(() -> service.requireSameUniversity("uni-2"))
                .isInstanceOf(BusinessException.class)
                .satisfies(e -> {
                    BusinessException be = (BusinessException) e;
                    assertThat(be.getCode()).isEqualTo(ErrorConstants.RESOURCE_NOT_AUTHORIZED);
                });
    }

    @Test
    @DisplayName("Blank entity university id allows access")
    void blankEntityUniversityAllowsAccess() {
        mockUser(UserType.SCHOOL, "uni-1");
        service.requireSameUniversity("");
        service.requireSameUniversity(null);
        assertThat(service.isSameUniversity("")).isTrue();
        assertThat(service.isSameUniversity(null)).isTrue();
    }

    @Test
    @DisplayName("Null user throws exception")
    void nullUserThrowsForSameUniversity() {
        when(currentUserProvider.getCurrentUser()).thenReturn(null);
        assertThatThrownBy(() -> service.requireSameUniversity("uni-1")).isInstanceOf(BusinessException.class);
    }

    @Test
    @DisplayName("MANAGER bypasses manageable check")
    void managerBypassesManageable() {
        mockUser(UserType.MANAGER, null);
        service.requireManageable("other");
    }

    @Test
    @DisplayName("STUDENT cannot manage resources")
    void studentCannotManage() {
        mockUser(UserType.STUDENT, "uni-1");
        assertThatThrownBy(() -> service.requireManageable("uni-1"))
                .isInstanceOf(BusinessException.class)
                .satisfies(e -> {
                    BusinessException be = (BusinessException) e;
                    assertThat(be.getMessage()).contains("无权管理");
                });
    }

    @Test
    @DisplayName("SCHOOL user can manage own university resources")
    void schoolCanManageOwn() {
        mockUser(UserType.SCHOOL, "uni-1");
        service.requireManageable("uni-1");
    }

    @Test
    @DisplayName("SCHOOL user cannot manage other university resources")
    void schoolCannotManageOther() {
        mockUser(UserType.SCHOOL, "uni-1");
        assertThatThrownBy(() -> service.requireManageable("uni-2")).isInstanceOf(BusinessException.class);
    }

    @Test
    @DisplayName("Blank entity university id throws for SCHOOL")
    void blankEntityThrowsForSchool() {
        mockUser(UserType.SCHOOL, "uni-1");
        assertThatThrownBy(() -> service.requireManageable(""))
                .isInstanceOf(BusinessException.class)
                .satisfies(e -> {
                    BusinessException be = (BusinessException) e;
                    assertThat(be.getMessage()).contains("公共");
                });
    }

    @Test
    @DisplayName("MANAGER bypasses university id requirement")
    void managerBypassesUniversityIdReq() {
        mockUser(UserType.MANAGER, null);
        service.requireUniversityId();
    }

    @Test
    @DisplayName("SCHOOL with university id passes")
    void schoolWithUniversityIdPasses() {
        mockUser(UserType.SCHOOL, "uni-1");
        service.requireUniversityId();
    }

    @Test
    @DisplayName("SCHOOL without university id throws for requireUniversityId")
    void schoolWithoutUniversityIdThrows() {
        mockUser(UserType.SCHOOL, null);
        assertThatThrownBy(() -> service.requireUniversityId())
                .isInstanceOf(BusinessException.class)
                .satisfies(e -> {
                    BusinessException be = (BusinessException) e;
                    assertThat(be.getMessage()).contains("未绑定学校");
                });
    }

    @Test
    @DisplayName("isSameUniversity null user returns false")
    void isSameUniversityNullUserReturnsFalse() {
        when(currentUserProvider.getCurrentUser()).thenReturn(null);
        assertThat(service.isSameUniversity("uni-1")).isFalse();
    }

    @Test
    @DisplayName("isSameUniversity non-matching university id returns false")
    void isSameUniversityNonMatchingReturnsFalse() {
        mockUser(UserType.SCHOOL, "uni-1");
        assertThat(service.isSameUniversity("uni-2")).isFalse();
    }

    @Test
    @DisplayName("requireManageable null user throws exception")
    void requireManageableNullUserThrows() {
        when(currentUserProvider.getCurrentUser()).thenReturn(null);
        assertThatThrownBy(() -> service.requireManageable("uni-1"))
                .isInstanceOf(BusinessException.class)
                .satisfies(e -> {
                    BusinessException be = (BusinessException) e;
                    assertThat(be.getCode()).isEqualTo(ErrorConstants.RESOURCE_NOT_AUTHORIZED);
                    assertThat(be.getMessage()).contains("用户未登录");
                });
    }

    @Test
    @DisplayName("requireUniversityId null user throws exception")
    void requireUniversityIdNullUserThrows() {
        when(currentUserProvider.getCurrentUser()).thenReturn(null);
        assertThatThrownBy(() -> service.requireUniversityId())
                .isInstanceOf(BusinessException.class)
                .satisfies(e -> {
                    BusinessException be = (BusinessException) e;
                    assertThat(be.getMessage()).contains("用户未登录");
                });
    }

    @Test
    @DisplayName("requireSameUniversity user universityId blank with non-blank entity throws")
    void requireSameUniversityUserBlankUniWithEntityThrows() {
        mockUser(UserType.SCHOOL, null);
        assertThatThrownBy(() -> service.requireSameUniversity("uni-1"))
                .isInstanceOf(BusinessException.class)
                .satisfies(e -> {
                    BusinessException be = (BusinessException) e;
                    assertThat(be.getCode()).isEqualTo(ErrorConstants.RESOURCE_NOT_AUTHORIZED);
                });
    }

    @Test
    @DisplayName("requireManageable user universityId blank throws")
    void requireManageableUserBlankUniWithEntityThrows() {
        mockUser(UserType.SCHOOL, null);
        assertThatThrownBy(() -> service.requireManageable("uni-1"))
                .isInstanceOf(BusinessException.class)
                .satisfies(e -> {
                    BusinessException be = (BusinessException) e;
                    assertThat(be.getCode()).isEqualTo(ErrorConstants.RESOURCE_NOT_AUTHORIZED);
                });
    }

    @Test
    @DisplayName("requireSameUniversity student user matching university passes")
    void studentMatchingUniversityPasses() {
        mockUser(UserType.STUDENT, "uni-1");
        service.requireSameUniversity("uni-1");
        assertThat(service.isSameUniversity("uni-1")).isTrue();
    }

    @Test
    @DisplayName("NotLoginException is caught and treated as null user")
    void notLoginExceptionReturnsNull() {
        when(currentUserProvider.getCurrentUser()).thenThrow(NotLoginException.class);
        assertThatThrownBy(() -> service.requireSameUniversity("uni-1")).isInstanceOf(BusinessException.class);
        assertThat(service.isSameUniversity("uni-1")).isFalse();
    }
}
