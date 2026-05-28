package com.rauio.smartdangjian.server.auth.security;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.when;

import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;

import com.rauio.smartdangjian.server.user.pojo.entity.User;
import com.rauio.smartdangjian.utils.spec.UserType;

import cn.dev33.satoken.session.SaSession;
import cn.dev33.satoken.stp.StpUtil;

@ExtendWith(MockitoExtension.class)
class SaTokenPermissionImplTest {

    private final SaTokenPermissionImpl permission = new SaTokenPermissionImpl();

    @Mock
    private SaSession session;

    @Test
    @DisplayName("MANAGER 用户返回 STUDENT、SCHOOL、MANAGER 三个角色")
    void managerRoles() {
        var user = new User();
        user.setUserType(UserType.MANAGER);

        try (MockedStatic<StpUtil> stpUtil = mockStatic(StpUtil.class)) {
            stpUtil.when(StpUtil::getSession).thenReturn(session);
            when(session.get("user")).thenReturn(user);

            List<String> roles = permission.getRoleList("1", "login");

            assertThat(roles).containsExactly("STUDENT", "SCHOOL", "MANAGER");
        }
    }

    @Test
    @DisplayName("SCHOOL 用户返回 STUDENT、SCHOOL 两个角色")
    void schoolRoles() {
        var user = new User();
        user.setUserType(UserType.SCHOOL);

        try (MockedStatic<StpUtil> stpUtil = mockStatic(StpUtil.class)) {
            stpUtil.when(StpUtil::getSession).thenReturn(session);
            when(session.get("user")).thenReturn(user);

            List<String> roles = permission.getRoleList("1", "login");

            assertThat(roles).containsExactly("STUDENT", "SCHOOL");
        }
    }

    @Test
    @DisplayName("STUDENT 用户返回 STUDENT 一个角色")
    void studentRoles() {
        var user = new User();
        user.setUserType(UserType.STUDENT);

        try (MockedStatic<StpUtil> stpUtil = mockStatic(StpUtil.class)) {
            stpUtil.when(StpUtil::getSession).thenReturn(session);
            when(session.get("user")).thenReturn(user);

            List<String> roles = permission.getRoleList("1", "login");

            assertThat(roles).containsExactly("STUDENT");
        }
    }

    @Test
    @DisplayName("Session 中无用户时返回空列表")
    void emptyRolesWhenNoUser() {
        try (MockedStatic<StpUtil> stpUtil = mockStatic(StpUtil.class)) {
            stpUtil.when(StpUtil::getSession).thenReturn(session);
            when(session.get("user")).thenReturn(null);

            List<String> roles = permission.getRoleList("1", "login");

            assertThat(roles).isEmpty();
        }
    }

    @Test
    @DisplayName("User has null userType returns empty role list")
    void nullUserTypeReturnsEmptyRoles() {
        var user = new User();
        user.setUserType(null);

        try (MockedStatic<StpUtil> stpUtil = mockStatic(StpUtil.class)) {
            stpUtil.when(StpUtil::getSession).thenReturn(session);
            when(session.get("user")).thenReturn(user);

            List<String> roles = permission.getRoleList("1", "login");

            assertThat(roles).isEmpty();
        }
    }

    @Test
    @DisplayName("MANAGER 用户返回通配权限 *")
    void managerPermissions() {
        var user = new User();
        user.setUserType(UserType.MANAGER);

        try (MockedStatic<StpUtil> stpUtil = mockStatic(StpUtil.class)) {
            stpUtil.when(StpUtil::getSession).thenReturn(session);
            when(session.get("user")).thenReturn(user);

            List<String> permissions = permission.getPermissionList("1", "login");

            assertThat(permissions).containsExactly("*");
        }
    }

    @Test
    @DisplayName("SCHOOL 用户返回模块管理权限")
    void schoolPermissions() {
        var user = new User();
        user.setUserType(UserType.SCHOOL);

        try (MockedStatic<StpUtil> stpUtil = mockStatic(StpUtil.class)) {
            stpUtil.when(StpUtil::getSession).thenReturn(session);
            when(session.get("user")).thenReturn(user);

            List<String> permissions = permission.getPermissionList("1", "login");

            assertThat(permissions).contains("category:*", "chapter:*", "course:*", "quiz:*", "resource:*");
        }
    }

    @Test
    @DisplayName("STUDENT 用户返回只读权限")
    void studentPermissions() {
        var user = new User();
        user.setUserType(UserType.STUDENT);

        try (MockedStatic<StpUtil> stpUtil = mockStatic(StpUtil.class)) {
            stpUtil.when(StpUtil::getSession).thenReturn(session);
            when(session.get("user")).thenReturn(user);

            List<String> permissions = permission.getPermissionList("1", "login");

            assertThat(permissions).contains("content:read", "quiz:answer", "learning:*", "file:read");
        }
    }

    @Test
    @DisplayName("Session 中无用户时返回空权限列表")
    void emptyPermissionsWhenNoUser() {
        try (MockedStatic<StpUtil> stpUtil = mockStatic(StpUtil.class)) {
            stpUtil.when(StpUtil::getSession).thenReturn(session);
            when(session.get("user")).thenReturn(null);

            List<String> permissions = permission.getPermissionList("1", "login");

            assertThat(permissions).isEmpty();
        }
    }
}
