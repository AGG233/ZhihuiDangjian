package com.rauio.smartdangjian.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;

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
class DataScopeServiceTest {

    private final DataScopeService service = new DataScopeService();
    private MockedStatic<SecurityUtils> securityUtilsMock;

    @BeforeEach
    void setUp() {
        securityUtilsMock = mockStatic(SecurityUtils.class);
    }

    @AfterEach
    void tearDown() {
        securityUtilsMock.close();
    }

    private void mockUser(UserType type, String universityId) {
        CurrentUserPrincipal user = mock(CurrentUserPrincipal.class);
        lenient().when(user.getUserType()).thenReturn(type);
        lenient().when(user.getUniversityId()).thenReturn(universityId);
        securityUtilsMock.when(SecurityUtils::getCurrentUser).thenReturn(user);
    }

    // ==================== requireSameUniversity ====================

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
        securityUtilsMock.when(SecurityUtils::getCurrentUser).thenReturn(null);
        assertThatThrownBy(() -> service.requireSameUniversity("uni-1")).isInstanceOf(BusinessException.class);
    }

    // ==================== requireManageable ====================

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

    // ==================== requireUniversityId ====================

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
    @DisplayName("SCHOOL without university id throws")
    void schoolWithoutUniversityIdThrows() {
        mockUser(UserType.SCHOOL, null);
        assertThatThrownBy(() -> service.requireUniversityId())
                .isInstanceOf(BusinessException.class)
                .satisfies(e -> {
                    BusinessException be = (BusinessException) e;
                    assertThat(be.getMessage()).contains("未绑定学校");
                });
    }
}
