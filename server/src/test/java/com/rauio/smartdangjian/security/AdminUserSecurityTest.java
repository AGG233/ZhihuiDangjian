package com.rauio.smartdangjian.security;

import com.rauio.smartdangjian.aop.UserAspect;
import com.rauio.smartdangjian.aop.annotation.DataScopeAccess;
import com.rauio.smartdangjian.aop.annotation.PermissionAccess;
import com.rauio.smartdangjian.aop.support.DataScopeAction;
import com.rauio.smartdangjian.aop.support.DataScopeContext;
import com.rauio.smartdangjian.aop.support.DataScopeResources;
import com.rauio.smartdangjian.constants.ErrorConstants;
import com.rauio.smartdangjian.exception.BusinessException;
import com.rauio.smartdangjian.server.user.aop.UserManagementAspect;
import com.rauio.smartdangjian.server.user.mapper.UserMapper;
import com.rauio.smartdangjian.server.user.pojo.dto.UserDto;
import com.rauio.smartdangjian.utils.spec.UserType;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.reflect.MethodSignature;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

import java.lang.reflect.Method;
import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("管理端用户安全切面单元测试")
class AdminUserSecurityTest {

    private final UserAspect userAspect = new UserAspect();

    @Mock
    private UserMapper userMapper;

    private UserManagementAspect userManagementAspect;

    @BeforeEach
    void setUp() {
        userManagementAspect = new UserManagementAspect(userMapper);
    }

    @AfterEach
    void clearContext() {
        SecurityContextHolder.clearContext();
    }

    // ── Helpers ───────────────────────────────────────────────────

    private void setSecurityContext(String id, UserType userType, String universityId) {
        CurrentUserPrincipal principal = new CurrentUserPrincipal() {
            @Override public String getId() { return id; }
            @Override public UserType getUserType() { return userType; }
            @Override public String getUniversityId() { return universityId; }
        };
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(principal, null, Collections.emptyList()));
    }

    // ═══════════════════════════════════════════════════════════════
    // Test interfaces with real annotations for reflection
    // ═══════════════════════════════════════════════════════════════

    @PermissionAccess(UserType.SCHOOL)
    interface TestAdminController {
        @PermissionAccess(UserType.SCHOOL)
        void searchEndpoint();

        @PermissionAccess(UserType.MANAGER)
        void manageEndpoint();

        // Method with parameters matching SpEL expressions used in annotations
        @DataScopeAccess(resource = DataScopeResources.USER_MANAGEMENT, action = DataScopeAction.SEARCH, query = "#userDto")
        Object searchUsers(UserDto userDto, int pageNum, int pageSize);

        @DataScopeAccess(resource = DataScopeResources.USER_MANAGEMENT, action = DataScopeAction.CREATE, body = "#user")
        Object createUser(com.rauio.smartdangjian.server.user.pojo.entity.User user);
    }

    private static Method findMethod(Class<?> clazz, String methodName) {
        for (Method m : clazz.getDeclaredMethods()) {
            if (m.getName().equals(methodName)) {
                return m;
            }
        }
        throw new RuntimeException("Method not found: " + methodName);
    }

    private JoinPoint mockJoinPointForControllerMethod(Class<?> controllerClass, String methodName) {
        Method method = findMethod(controllerClass, methodName);
        MethodSignature signature = mock(MethodSignature.class);
        lenient().when(signature.getMethod()).thenReturn(method);

        JoinPoint joinPoint = mock(JoinPoint.class);
        lenient().when(joinPoint.getSignature()).thenReturn(signature);
        lenient().when(joinPoint.getTarget()).thenReturn(mock(controllerClass));
        return joinPoint;
    }

    private ProceedingJoinPoint mockProceedingJoinPointWithArgs(Class<?> controllerClass, String methodName, Object[] args) {
        Method method = findMethod(controllerClass, methodName);
        MethodSignature signature = mock(MethodSignature.class);
        lenient().when(signature.getMethod()).thenReturn(method);

        ProceedingJoinPoint joinPoint = mock(ProceedingJoinPoint.class);
        lenient().when(joinPoint.getSignature()).thenReturn(signature);
        lenient().when(joinPoint.getTarget()).thenReturn(mock(controllerClass));
        lenient().when(joinPoint.getArgs()).thenReturn(args);
        return joinPoint;
    }

    private DataScopeAccess getDataScopeAnnotation(Class<?> clazz, String methodName) {
        return findMethod(clazz, methodName).getAnnotation(DataScopeAccess.class);
    }

    // ═══════════════════════════════════════════════════════════════
    // PermissionAccess (UserAspect) tests
    // ═══════════════════════════════════════════════════════════════

    @Nested
    @DisplayName("PermissionAccess 权限级别检查")
    class PermissionAccessTests {

        @Test
        @DisplayName("STUDENT(1级) 访问 SCHOOL(2级) 接口被拒绝 → 4003")
        void studentRejectedBySchoolLevel() throws Exception {
            setSecurityContext("stu-001", UserType.STUDENT, "uni-001");
            JoinPoint jp = mockJoinPointForControllerMethod(TestAdminController.class, "searchEndpoint");

            assertThatThrownBy(() -> userAspect.checkPermissionAccess(jp))
                    .isInstanceOf(BusinessException.class)
                    .extracting("code")
                    .isEqualTo(ErrorConstants.RESOURCE_NOT_AUTHORIZED);
        }

        @Test
        @DisplayName("SCHOOL(2级) 访问 MANAGER(3级) 接口被拒绝 → 4003")
        void schoolRejectedByManagerLevel() throws Exception {
            setSecurityContext("sch-001", UserType.SCHOOL, "uni-001");
            JoinPoint jp = mockJoinPointForControllerMethod(TestAdminController.class, "manageEndpoint");

            assertThatThrownBy(() -> userAspect.checkPermissionAccess(jp))
                    .isInstanceOf(BusinessException.class)
                    .extracting("code")
                    .isEqualTo(ErrorConstants.RESOURCE_NOT_AUTHORIZED);
        }

        @Test
        @DisplayName("无认证用户访问被拒绝 → 4003")
        void unauthenticatedRejected() throws Exception {
            SecurityContextHolder.clearContext();
            JoinPoint jp = mockJoinPointForControllerMethod(TestAdminController.class, "searchEndpoint");

            assertThatThrownBy(() -> userAspect.checkPermissionAccess(jp))
                    .isInstanceOf(BusinessException.class)
                    .extracting("code")
                    .isEqualTo(ErrorConstants.RESOURCE_NOT_AUTHORIZED);
        }

        @Test
        @DisplayName("MANAGER(3级) 可通过 SCHOOL(2级) 权限检查")
        void managerPassesSchoolLevel() throws Exception {
            setSecurityContext("mgr-001", UserType.MANAGER, null);
            JoinPoint jp = mockJoinPointForControllerMethod(TestAdminController.class, "searchEndpoint");

            assertThatCode(() -> userAspect.checkPermissionAccess(jp))
                    .doesNotThrowAnyException();
        }

        @Test
        @DisplayName("SCHOOL(2级) 可通过 SCHOOL(2级) 权限检查")
        void schoolPassesSchoolLevel() throws Exception {
            setSecurityContext("sch-001", UserType.SCHOOL, "uni-001");
            JoinPoint jp = mockJoinPointForControllerMethod(TestAdminController.class, "searchEndpoint");

            assertThatCode(() -> userAspect.checkPermissionAccess(jp))
                    .doesNotThrowAnyException();
        }
    }

    // ═══════════════════════════════════════════════════════════════
    // DataScope (UserManagementAspect) tests via resolver contract
    // ═══════════════════════════════════════════════════════════════

    @Nested
    @DisplayName("DataScope 用户管理数据范围")
    class DataScopeTests {

        @Test
        @DisplayName("handleSearch: SCHOOL 管理员自动注入本学校 ID，限制只看本校用户")
        void schoolSearchInjectsUniversityId() throws Exception {
            setSecurityContext("sch-001", UserType.SCHOOL, "uni-sch-001");

            UserDto queryDto = new UserDto();
            queryDto.setUsername("test");

            ProceedingJoinPoint jp = mockProceedingJoinPointWithArgs(
                    TestAdminController.class, "searchUsers",
                    new Object[]{queryDto, 1, 10});

            DataScopeContext ctx = new DataScopeContext(jp, getDataScopeAnnotation(
                    TestAdminController.class, "searchUsers"),
                    (CurrentUserPrincipal) SecurityContextHolder.getContext()
                            .getAuthentication().getPrincipal());

            // Resolver.before → handleSearch injects universityId
            userManagementAspect.before(ctx);

            org.assertj.core.api.Assertions.assertThat(queryDto.getUniversityId())
                    .isEqualTo("uni-sch-001");
        }

        @Test
        @DisplayName("handleSearch: MANAGER 搜索不受数据范围限制")
        void managerSearchHasNoRestrictions() throws Exception {
            setSecurityContext("mgr-001", UserType.MANAGER, null);

            UserDto queryDto = new UserDto();
            queryDto.setUserType(UserType.MANAGER);

            ProceedingJoinPoint jp = mockProceedingJoinPointWithArgs(
                    TestAdminController.class, "searchUsers",
                    new Object[]{queryDto, 1, 10});

            DataScopeContext ctx = new DataScopeContext(jp, getDataScopeAnnotation(
                    TestAdminController.class, "searchUsers"),
                    (CurrentUserPrincipal) SecurityContextHolder.getContext()
                            .getAuthentication().getPrincipal());

            // MANAGER: returns early, no modifications
            assertThatCode(() -> userManagementAspect.before(ctx))
                    .doesNotThrowAnyException();
        }

        @Test
        @DisplayName("handleSearch: SCHOOL 管理员搜索 MANAGER 类型用户被拒绝 → 4003")
        void schoolCannotSearchManagerType() throws Exception {
            setSecurityContext("sch-001", UserType.SCHOOL, "uni-sch-001");

            UserDto queryDto = new UserDto();
            queryDto.setUserType(UserType.MANAGER);

            ProceedingJoinPoint jp = mockProceedingJoinPointWithArgs(
                    TestAdminController.class, "searchUsers",
                    new Object[]{queryDto, 1, 10});

            DataScopeContext ctx = new DataScopeContext(jp, getDataScopeAnnotation(
                    TestAdminController.class, "searchUsers"),
                    (CurrentUserPrincipal) SecurityContextHolder.getContext()
                            .getAuthentication().getPrincipal());

            assertThatThrownBy(() -> userManagementAspect.before(ctx))
                    .isInstanceOf(BusinessException.class)
                    .extracting("code")
                    .isEqualTo(ErrorConstants.RESOURCE_NOT_AUTHORIZED);
        }

        @Test
        @DisplayName("handleSearch: SCHOOL 管理员必须绑定学校ID")
        void schoolWithoutUniversityIdRejected() throws Exception {
            setSecurityContext("sch-001", UserType.SCHOOL, null);

            UserDto queryDto = new UserDto();

            ProceedingJoinPoint jp = mockProceedingJoinPointWithArgs(
                    TestAdminController.class, "searchUsers",
                    new Object[]{queryDto, 1, 10});

            DataScopeContext ctx = new DataScopeContext(jp, getDataScopeAnnotation(
                    TestAdminController.class, "searchUsers"),
                    (CurrentUserPrincipal) SecurityContextHolder.getContext()
                            .getAuthentication().getPrincipal());

            assertThatThrownBy(() -> userManagementAspect.before(ctx))
                    .isInstanceOf(BusinessException.class)
                    .extracting("code")
                    .isEqualTo(ErrorConstants.RESOURCE_NOT_AUTHORIZED);
        }

        @Test
        @DisplayName("handleSearch: STUDENT 用户搜索仅可见自身")
        void studentSearchLimitedToOwnData() throws Exception {
            setSecurityContext("stu-001", UserType.STUDENT, "uni-001");

            UserDto queryDto = new UserDto();

            ProceedingJoinPoint jp = mockProceedingJoinPointWithArgs(
                    TestAdminController.class, "searchUsers",
                    new Object[]{queryDto, 1, 10});

            DataScopeContext ctx = new DataScopeContext(jp, getDataScopeAnnotation(
                    TestAdminController.class, "searchUsers"),
                    (CurrentUserPrincipal) SecurityContextHolder.getContext()
                            .getAuthentication().getPrincipal());

            userManagementAspect.before(ctx);

            org.assertj.core.api.Assertions.assertThat(queryDto.getUserId())
                    .isEqualTo("stu-001");
            org.assertj.core.api.Assertions.assertThat(queryDto.getUniversityId())
                    .isEqualTo("uni-001");
        }

        @Test
        @DisplayName("supports: 仅响应 USER_MANAGEMENT 资源类型")
        void supportsOnlyUserManagement() {
            org.assertj.core.api.Assertions.assertThat(userManagementAspect.supports(DataScopeResources.USER_MANAGEMENT))
                    .isTrue();
            org.assertj.core.api.Assertions.assertThat(userManagementAspect.supports("OTHER_RESOURCE"))
                    .isFalse();
        }
    }

    // ═══════════════════════════════════════════════════════════════
    // handleCreate (UserManagementAspect) tests
    // ═══════════════════════════════════════════════════════════════

    @Nested
    @DisplayName("DataScope 用户创建权限")
    class CreatePermissionTests {

        @Test
        @DisplayName("STUDENT 无权创建用户 → 4003")
        void studentCannotCreateUser() throws Exception {
            setSecurityContext("stu-001", UserType.STUDENT, "uni-001");

            com.rauio.smartdangjian.server.user.pojo.entity.User newUser =
                    com.rauio.smartdangjian.server.user.pojo.entity.User.builder()
                            .username("newuser")
                            .userType(UserType.STUDENT)
                            .build();

            ProceedingJoinPoint jp = mockProceedingJoinPointWithArgs(
                    TestAdminController.class, "createUser",
                    new Object[]{newUser});

            DataScopeContext ctx = new DataScopeContext(jp, getDataScopeAnnotation(
                    TestAdminController.class, "createUser"),
                    (CurrentUserPrincipal) SecurityContextHolder.getContext()
                            .getAuthentication().getPrincipal());

            assertThatThrownBy(() -> userManagementAspect.before(ctx))
                    .isInstanceOf(BusinessException.class)
                    .extracting("code")
                    .isEqualTo(ErrorConstants.RESOURCE_NOT_AUTHORIZED);
        }

        @Test
        @DisplayName("SCHOOL 管理员只能创建 STUDENT 类型用户")
        void schoolCanOnlyCreateStudentType() throws Exception {
            setSecurityContext("sch-001", UserType.SCHOOL, "uni-sch-001");

            com.rauio.smartdangjian.server.user.pojo.entity.User newUser =
                    com.rauio.smartdangjian.server.user.pojo.entity.User.builder()
                            .username("newmanager")
                            .userType(UserType.MANAGER)
                            .build();

            ProceedingJoinPoint jp = mockProceedingJoinPointWithArgs(
                    TestAdminController.class, "createUser",
                    new Object[]{newUser});

            DataScopeContext ctx = new DataScopeContext(jp, getDataScopeAnnotation(
                    TestAdminController.class, "createUser"),
                    (CurrentUserPrincipal) SecurityContextHolder.getContext()
                            .getAuthentication().getPrincipal());

            assertThatThrownBy(() -> userManagementAspect.before(ctx))
                    .isInstanceOf(BusinessException.class)
                    .extracting("code")
                    .isEqualTo(ErrorConstants.RESOURCE_NOT_AUTHORIZED);
        }
    }
}
