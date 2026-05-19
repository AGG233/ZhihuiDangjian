package com.rauio.smartdangjian.server.user.aop;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.rauio.smartdangjian.aop.annotation.DataScopeAccess;
import com.rauio.smartdangjian.aop.support.DataScopeAction;
import com.rauio.smartdangjian.aop.support.DataScopeContext;
import com.rauio.smartdangjian.constants.ErrorConstants;
import com.rauio.smartdangjian.exception.BusinessException;
import com.rauio.smartdangjian.security.CurrentUserPrincipal;
import com.rauio.smartdangjian.server.user.constants.UserErrorConstants;
import com.rauio.smartdangjian.server.user.mapper.UserMapper;
import com.rauio.smartdangjian.server.user.pojo.entity.User;
import com.rauio.smartdangjian.server.user.pojo.request.UserRequest;
import com.rauio.smartdangjian.utils.spec.UserType;

@ExtendWith(MockitoExtension.class)
class UserManagementAspectTest {

    @Mock
    private UserMapper userMapper;

    @Mock
    private DataScopeContext context;

    @Mock
    private DataScopeAccess access;

    @Mock
    private CurrentUserPrincipal currentUser;

    @InjectMocks
    private UserManagementAspect aspect;

    private void stubAction(DataScopeAction action) {
        when(context.getAccess()).thenReturn(access);
        when(context.getCurrentUser()).thenReturn(currentUser);
        when(access.action()).thenReturn(action);
    }

    private void stubSchoolUser(String universityId) {
        when(currentUser.getUserType()).thenReturn(UserType.SCHOOL);
        when(currentUser.getUniversityId()).thenReturn(universityId);
    }

    private void stubStudentUser(String userId) {
        when(currentUser.getUserType()).thenReturn(UserType.STUDENT);
        when(currentUser.getId()).thenReturn(userId);
    }

    // ================================================================
    // supports
    // ================================================================

    @Test
    @DisplayName("supports USER_MANAGEMENT 返回 true")
    void supportsUserManagement() {
        assertThat(aspect.supports("USER_MANAGEMENT")).isTrue();
    }

    @Test
    @DisplayName("supports 其他资源返回 false")
    void supportsOtherResource() {
        assertThat(aspect.supports("LEARNING_RECORD")).isFalse();
    }

    // ================================================================
    // SEARCH
    // ================================================================

    @Test
    @DisplayName("SEARCH MANAGER 权限放行")
    void searchManagerPasses() {
        stubAction(DataScopeAction.SEARCH);
        when(currentUser.getUserType()).thenReturn(UserType.MANAGER);
        UserRequest query = new UserRequest();
        when(access.query()).thenReturn("#query");
        when(context.require("#query", UserRequest.class, "查询参数不能为空")).thenReturn(query);

        assertThatCode(() -> aspect.before(context)).doesNotThrowAnyException();
    }

    @Test
    @DisplayName("SEARCH SCHOOL 绑定学校时自动限制查询范围")
    void searchSchoolSetsUniversityId() {
        stubAction(DataScopeAction.SEARCH);
        stubSchoolUser("univ-1");
        UserRequest query = new UserRequest();
        when(access.query()).thenReturn("#query");
        when(context.require("#query", UserRequest.class, "查询参数不能为空")).thenReturn(query);

        aspect.before(context);

        assertThat(query.getUniversityId()).isEqualTo("univ-1");
    }

    @Test
    @DisplayName("SEARCH SCHOOL 未绑定学校时抛出异常")
    void searchSchoolNoUniversityThrows() {
        stubAction(DataScopeAction.SEARCH);
        stubSchoolUser(null);
        UserRequest query = new UserRequest();
        when(access.query()).thenReturn("#query");
        when(context.require("#query", UserRequest.class, "查询参数不能为空")).thenReturn(query);

        assertThatThrownBy(() -> aspect.before(context))
                .isInstanceOf(BusinessException.class)
                .extracting("code")
                .isEqualTo(ErrorConstants.RESOURCE_NOT_AUTHORIZED);
    }

    @Test
    @DisplayName("SEARCH STUDENT 自动设置 userId 和 universityId")
    void searchStudentSetsIds() {
        stubAction(DataScopeAction.SEARCH);
        stubStudentUser("student-1");
        when(currentUser.getUniversityId()).thenReturn("univ-1");
        UserRequest query = new UserRequest();
        when(access.query()).thenReturn("#query");
        when(context.require("#query", UserRequest.class, "查询参数不能为空")).thenReturn(query);

        aspect.before(context);

        assertThat(query.getUserId()).isEqualTo("student-1");
        assertThat(query.getUniversityId()).isEqualTo("univ-1");
    }

    // ================================================================
    // READ
    // ================================================================

    @Test
    @DisplayName("READ MANAGER 放行")
    void readManagerPasses() {
        stubAction(DataScopeAction.READ);
        when(currentUser.getUserType()).thenReturn(UserType.MANAGER);
        User target = User.builder().id("target-id").build();
        when(access.id()).thenReturn("#id");
        when(context.require("#id", String.class, "用户ID不能为空")).thenReturn("target-id");
        when(userMapper.selectById("target-id")).thenReturn(target);

        assertThatCode(() -> aspect.before(context)).doesNotThrowAnyException();
    }

    @Test
    @DisplayName("READ SCHOOL 同校用户放行")
    void readSchoolSameUniversityPasses() {
        stubAction(DataScopeAction.READ);
        stubSchoolUser("univ-1");
        User target = User.builder()
                .id("target-id")
                .userType(UserType.STUDENT)
                .universityId("univ-1")
                .build();
        when(access.id()).thenReturn("#id");
        when(context.require("#id", String.class, "用户ID不能为空")).thenReturn("target-id");
        when(userMapper.selectById("target-id")).thenReturn(target);

        assertThatCode(() -> aspect.before(context)).doesNotThrowAnyException();
    }

    @Test
    @DisplayName("READ SCHOOL 不同校用户抛出异常")
    void readSchoolDifferentUniversityThrows() {
        stubAction(DataScopeAction.READ);
        stubSchoolUser("univ-1");
        User target = User.builder()
                .id("target-id")
                .userType(UserType.STUDENT)
                .universityId("univ-2")
                .build();
        when(access.id()).thenReturn("#id");
        when(context.require("#id", String.class, "用户ID不能为空")).thenReturn("target-id");
        when(userMapper.selectById("target-id")).thenReturn(target);

        assertThatThrownBy(() -> aspect.before(context))
                .isInstanceOf(BusinessException.class)
                .extracting("code")
                .isEqualTo(ErrorConstants.RESOURCE_NOT_AUTHORIZED);
    }

    @Test
    @DisplayName("READ 目标用户不存在时抛出异常")
    void readUserNotFoundThrows() {
        stubAction(DataScopeAction.READ);
        when(access.id()).thenReturn("#id");
        when(context.require("#id", String.class, "用户ID不能为空")).thenReturn("nonexistent");
        when(userMapper.selectById("nonexistent")).thenReturn(null);

        assertThatThrownBy(() -> aspect.before(context))
                .isInstanceOf(BusinessException.class)
                .extracting("code")
                .isEqualTo(UserErrorConstants.USER_NOT_EXISTS);
    }

    // ================================================================
    // CREATE
    // ================================================================

    @Test
    @DisplayName("CREATE MANAGER 放行")
    void createManagerPasses() {
        stubAction(DataScopeAction.CREATE);
        when(currentUser.getUserType()).thenReturn(UserType.MANAGER);
        User payload = User.builder().userType(UserType.STUDENT).build();
        when(access.body()).thenReturn("#user");
        when(context.require("#user", User.class, "用户信息不能为空")).thenReturn(payload);

        assertThatCode(() -> aspect.before(context)).doesNotThrowAnyException();
    }

    @Test
    @DisplayName("CREATE payload 缺少 userType 时抛出异常")
    void createNoUserTypeThrows() {
        stubAction(DataScopeAction.CREATE);
        User payload = User.builder().build();
        when(access.body()).thenReturn("#user");
        when(context.require("#user", User.class, "用户信息不能为空")).thenReturn(payload);

        assertThatThrownBy(() -> aspect.before(context))
                .isInstanceOf(BusinessException.class)
                .extracting("code")
                .isEqualTo(ErrorConstants.ARGS_ERROR);
    }

    @Test
    @DisplayName("CREATE SCHOOL 创建本校学生成功")
    void createSchoolOwnStudentPasses() {
        stubAction(DataScopeAction.CREATE);
        stubSchoolUser("univ-1");
        User payload =
                User.builder().userType(UserType.STUDENT).universityId("univ-1").build();
        when(access.body()).thenReturn("#user");
        when(context.require("#user", User.class, "用户信息不能为空")).thenReturn(payload);

        assertThatCode(() -> aspect.before(context)).doesNotThrowAnyException();
    }

    @Test
    @DisplayName("CREATE SCHOOL 创建学生时自动补全学校ID")
    void createSchoolAutoFillsUniversity() {
        stubAction(DataScopeAction.CREATE);
        stubSchoolUser("univ-1");
        User payload = User.builder().userType(UserType.STUDENT).build();
        when(access.body()).thenReturn("#user");
        when(context.require("#user", User.class, "用户信息不能为空")).thenReturn(payload);

        aspect.before(context);

        assertThat(payload.getUniversityId()).isEqualTo("univ-1");
    }

    @Test
    @DisplayName("CREATE SCHOOL 不能创建其他学校的用户")
    void createSchoolOtherUniversityThrows() {
        stubAction(DataScopeAction.CREATE);
        stubSchoolUser("univ-1");
        User payload =
                User.builder().userType(UserType.STUDENT).universityId("univ-2").build();
        when(access.body()).thenReturn("#user");
        when(context.require("#user", User.class, "用户信息不能为空")).thenReturn(payload);

        assertThatThrownBy(() -> aspect.before(context))
                .isInstanceOf(BusinessException.class)
                .extracting("code")
                .isEqualTo(ErrorConstants.RESOURCE_NOT_AUTHORIZED);
    }

    @Test
    @DisplayName("CREATE SCHOOL 不能创建管理员")
    void createSchoolCannotCreateManager() {
        stubAction(DataScopeAction.CREATE);
        stubSchoolUser("univ-1");
        User payload = User.builder().userType(UserType.MANAGER).build();
        when(access.body()).thenReturn("#user");
        when(context.require("#user", User.class, "用户信息不能为空")).thenReturn(payload);

        assertThatThrownBy(() -> aspect.before(context))
                .isInstanceOf(BusinessException.class)
                .extracting("code")
                .isEqualTo(ErrorConstants.RESOURCE_NOT_AUTHORIZED);
    }

    @Test
    @DisplayName("CREATE STUDENT 无权创建用户")
    void createStudentThrows() {
        stubAction(DataScopeAction.CREATE);
        when(currentUser.getUserType()).thenReturn(UserType.STUDENT);
        User payload = User.builder().userType(UserType.STUDENT).build();
        when(access.body()).thenReturn("#user");
        when(context.require("#user", User.class, "用户信息不能为空")).thenReturn(payload);

        assertThatThrownBy(() -> aspect.before(context))
                .isInstanceOf(BusinessException.class)
                .extracting("code")
                .isEqualTo(ErrorConstants.RESOURCE_NOT_AUTHORIZED);
    }

    // ================================================================
    // UPDATE
    // ================================================================

    @Test
    @DisplayName("UPDATE MANAGER 放行")
    void updateManagerPasses() {
        stubAction(DataScopeAction.UPDATE);
        when(currentUser.getUserType()).thenReturn(UserType.MANAGER);
        User target = User.builder().id("target-id").build();
        User payload = User.builder().build();
        when(access.id()).thenReturn("#id");
        when(access.body()).thenReturn("#user");
        when(context.require("#id", String.class, "用户ID不能为空")).thenReturn("target-id");
        when(context.require("#user", User.class, "用户信息不能为空")).thenReturn(payload);
        when(userMapper.selectById("target-id")).thenReturn(target);

        assertThatCode(() -> aspect.before(context)).doesNotThrowAnyException();
    }

    @Test
    @DisplayName("UPDATE STUDENT 更新自己成功")
    void updateStudentSelfPasses() {
        stubAction(DataScopeAction.UPDATE);
        stubStudentUser("student-1");
        User target = User.builder().id("student-1").build();
        User payload = User.builder().build();
        when(access.id()).thenReturn("#id");
        when(access.body()).thenReturn("#user");
        when(context.require("#id", String.class, "用户ID不能为空")).thenReturn("student-1");
        when(context.require("#user", User.class, "用户信息不能为空")).thenReturn(payload);
        when(userMapper.selectById("student-1")).thenReturn(target);

        assertThatCode(() -> aspect.before(context)).doesNotThrowAnyException();
    }

    @Test
    @DisplayName("UPDATE STUDENT 更新他人信息抛出异常")
    void updateStudentOtherThrows() {
        stubAction(DataScopeAction.UPDATE);
        stubStudentUser("student-1");
        User target = User.builder().id("other-user").build();
        User payload = User.builder().build();
        when(access.id()).thenReturn("#id");
        when(access.body()).thenReturn("#user");
        when(context.require("#id", String.class, "用户ID不能为空")).thenReturn("other-user");
        when(context.require("#user", User.class, "用户信息不能为空")).thenReturn(payload);
        when(userMapper.selectById("other-user")).thenReturn(target);

        assertThatThrownBy(() -> aspect.before(context))
                .isInstanceOf(BusinessException.class)
                .extracting("code")
                .isEqualTo(ErrorConstants.RESOURCE_NOT_AUTHORIZED);
    }

    @Test
    @DisplayName("UPDATE STUDENT 试图修改角色时抛出异常")
    void updateStudentChangeRoleThrows() {
        stubAction(DataScopeAction.UPDATE);
        stubStudentUser("student-1");
        User target = User.builder().id("student-1").build();
        User payload = User.builder().userType(UserType.SCHOOL).build();
        when(access.id()).thenReturn("#id");
        when(access.body()).thenReturn("#user");
        when(context.require("#id", String.class, "用户ID不能为空")).thenReturn("student-1");
        when(context.require("#user", User.class, "用户信息不能为空")).thenReturn(payload);
        when(userMapper.selectById("student-1")).thenReturn(target);

        assertThatThrownBy(() -> aspect.before(context))
                .isInstanceOf(BusinessException.class)
                .extracting("code")
                .isEqualTo(ErrorConstants.RESOURCE_NOT_AUTHORIZED);
    }

    // ================================================================
    // DELETE
    // ================================================================

    @Test
    @DisplayName("DELETE MANAGER 放行")
    void deleteManagerPasses() {
        stubAction(DataScopeAction.DELETE);
        when(currentUser.getUserType()).thenReturn(UserType.MANAGER);
        User target = User.builder().id("target-id").build();
        when(access.id()).thenReturn("#id");
        when(context.require("#id", String.class, "用户ID不能为空")).thenReturn("target-id");
        when(userMapper.selectById("target-id")).thenReturn(target);

        assertThatCode(() -> aspect.before(context)).doesNotThrowAnyException();
    }

    @Test
    @DisplayName("DELETE STUDENT 抛出异常")
    void deleteStudentThrows() {
        stubAction(DataScopeAction.DELETE);
        when(currentUser.getUserType()).thenReturn(UserType.STUDENT);
        User target = User.builder().id("target-id").build();
        when(access.id()).thenReturn("#id");
        when(context.require("#id", String.class, "用户ID不能为空")).thenReturn("target-id");
        when(userMapper.selectById("target-id")).thenReturn(target);

        assertThatThrownBy(() -> aspect.before(context))
                .isInstanceOf(BusinessException.class)
                .extracting("code")
                .isEqualTo(ErrorConstants.RESOURCE_NOT_AUTHORIZED);
    }

    @Test
    @DisplayName("DELETE SCHOOL 删除本校学生成功")
    void deleteSchoolOwnStudentPasses() {
        stubAction(DataScopeAction.DELETE);
        stubSchoolUser("univ-1");
        User target = User.builder()
                .id("target-id")
                .userType(UserType.STUDENT)
                .universityId("univ-1")
                .build();
        when(access.id()).thenReturn("#id");
        when(context.require("#id", String.class, "用户ID不能为空")).thenReturn("target-id");
        when(userMapper.selectById("target-id")).thenReturn(target);

        assertThatCode(() -> aspect.before(context)).doesNotThrowAnyException();
    }

    @Test
    @DisplayName("DELETE SCHOOL 删除其他学校用户抛出异常")
    void deleteSchoolOtherUniversityThrows() {
        stubAction(DataScopeAction.DELETE);
        stubSchoolUser("univ-1");
        User target = User.builder()
                .id("target-id")
                .userType(UserType.STUDENT)
                .universityId("univ-2")
                .build();
        when(access.id()).thenReturn("#id");
        when(context.require("#id", String.class, "用户ID不能为空")).thenReturn("target-id");
        when(userMapper.selectById("target-id")).thenReturn(target);

        assertThatThrownBy(() -> aspect.before(context))
                .isInstanceOf(BusinessException.class)
                .extracting("code")
                .isEqualTo(ErrorConstants.RESOURCE_NOT_AUTHORIZED);
    }

    // ================================================================
    // after
    // ================================================================

    @Test
    @DisplayName("after 默认实现直接返回 result")
    void afterReturnsResult() {
        Object result = new Object();
        assertThat(aspect.after(context, result)).isSameAs(result);
    }
}
