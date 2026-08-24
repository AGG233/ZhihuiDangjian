package com.rauio.smartdangjian.server.auth.security;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mockStatic;

import java.util.List;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;

import cn.dev33.satoken.stp.StpUtil;

@ExtendWith(MockitoExtension.class)
class SaTokenPermissionImplTest {

    private final SaTokenPermissionImpl permission = new SaTokenPermissionImpl();

    private MockedStatic<StpUtil> stpUtil;

    @BeforeEach
    void openMock() {
        stpUtil = mockStatic(StpUtil.class);
        // 未 stub 的 getExtra 默认返回 null，即「令牌未携带角色 claim」
        stpUtil.when(() -> StpUtil.getExtra("role")).thenReturn(null);
    }

    @AfterEach
    void closeMock() {
        stpUtil.close();
    }

    @Test
    @DisplayName("MANAGER 角色声明返回 STUDENT、SCHOOL、MANAGER 三个角色")
    void managerRoles() {
        stpUtil.when(() -> StpUtil.getExtra("role")).thenReturn("MANAGER");

        List<String> roles = permission.getRoleList("1", "login");

        assertThat(roles).containsExactly("STUDENT", "SCHOOL", "MANAGER");
    }

    @Test
    @DisplayName("SCHOOL 角色声明返回 STUDENT、SCHOOL 两个角色")
    void schoolRoles() {
        stpUtil.when(() -> StpUtil.getExtra("role")).thenReturn("SCHOOL");

        List<String> roles = permission.getRoleList("1", "login");

        assertThat(roles).containsExactly("STUDENT", "SCHOOL");
    }

    @Test
    @DisplayName("STUDENT 角色声明返回 STUDENT 一个角色")
    void studentRoles() {
        stpUtil.when(() -> StpUtil.getExtra("role")).thenReturn("STUDENT");

        List<String> roles = permission.getRoleList("1", "login");

        assertThat(roles).containsExactly("STUDENT");
    }

    @Test
    @DisplayName("令牌未携带角色 claim 时返回空列表")
    void emptyRolesWhenNoRoleClaim() {
        List<String> roles = permission.getRoleList("1", "login");

        assertThat(roles).isEmpty();
    }

    @Test
    @DisplayName("未知角色声明的令牌返回空列表（不提权）")
    void unknownRoleClaimReturnsEmptyRoles() {
        stpUtil.when(() -> StpUtil.getExtra("role")).thenReturn("SUPER_ADMIN");

        List<String> roles = permission.getRoleList("1", "login");

        assertThat(roles).isEmpty();
    }

    @Test
    @DisplayName("getPermissionList always returns empty list")
    void permissionListIsAlwaysEmpty() {
        List<String> permissions = permission.getPermissionList("1", "login");

        assertThat(permissions).isEmpty();
    }
}
