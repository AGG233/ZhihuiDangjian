package com.rauio.smartdangjian.aop;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.lang.reflect.Method;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.reflect.MethodSignature;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.rauio.smartdangjian.aop.annotation.DataScopeAccess;
import com.rauio.smartdangjian.aop.support.DataScopeAction;
import com.rauio.smartdangjian.aop.support.DataScopeContext;
import com.rauio.smartdangjian.aop.support.DataScopeResources;
import com.rauio.smartdangjian.exception.BusinessException;
import com.rauio.smartdangjian.server.content.aop.CourseAdminAccessAspect;
import com.rauio.smartdangjian.server.content.mapper.CourseMapper;
import com.rauio.smartdangjian.server.content.pojo.entity.Course;
import com.rauio.smartdangjian.server.content.pojo.request.CourseRequest;
import com.rauio.smartdangjian.server.user.mapper.UserMapper;
import com.rauio.smartdangjian.server.user.pojo.entity.User;
import com.rauio.smartdangjian.utils.spec.UserType;

@ExtendWith(MockitoExtension.class)
@DisplayName("CourseAdminAccessAspect 单元测试")
class CourseAdminAccessAspectTest {

    @Mock
    private CourseMapper courseMapper;

    @Mock
    private UserMapper userMapper;

    @InjectMocks
    private CourseAdminAccessAspect aspect;

    // ==================== supports ====================

    @Test
    @DisplayName("supports 返回 true 支持 COURSE_ADMIN")
    void supportsTrue() {
        assertThat(aspect.supports(DataScopeResources.COURSE_ADMIN)).isTrue();
    }

    @Test
    @DisplayName("supports 返回 false 不支持其他资源")
    void supportsFalse() {
        assertThat(aspect.supports("OTHER")).isFalse();
    }

    // ==================== before - MANAGER bypass ====================

    @Test
    @DisplayName("管理员直接放行")
    void beforeManagerBypass() {
        DataScopeContext context = mockContext(UserType.MANAGER, DataScopeAction.UPDATE, "", "");
        aspect.before(context);
    }

    // ==================== before - user type checks ====================

    @Test
    @DisplayName("学生无权管理课程")
    void beforeStudentNotAllowed() {
        DataScopeContext context = mockContext(UserType.STUDENT, DataScopeAction.UPDATE, "", "");
        assertThatThrownBy(() -> aspect.before(context))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("无权管理课程");
    }

    @Test
    @DisplayName("学校管理员未绑定学校抛出异常")
    void beforeSchoolNoUniversityId() {
        DataScopeContext context = mockContext(UserType.SCHOOL, 1L, null, DataScopeAction.UPDATE, "'1'", "");
        assertThatThrownBy(() -> aspect.before(context))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("未绑定学校");
    }

    // ==================== before - CREATE ====================

    @Nested
    @DisplayName("CREATE 操作校验")
    class CreateTests {

        @Test
        @DisplayName("学校管理员创建课程通过（仅校验请求体非空）")
        void createPasses() {
            CourseRequest body = CourseRequest.builder().title("新课程").build();
            Method realMethod = findMethod("courseAction", CourseRequest.class);
            ProceedingJoinPoint jp = mockJoinPoint(new String[]{"course"}, new Object[]{body}, realMethod);
            DataScopeContext context = mockContext(jp, DataScopeAction.CREATE, "'0'", "#course");

            aspect.before(context);
        }

        @Test
        @DisplayName("创建课程时请求体为空抛出异常")
        void createBodyNull() {
            ProceedingJoinPoint jp = mockJoinPoint(new String[]{}, new Object[]{});
            DataScopeContext context = mockContext(jp, DataScopeAction.CREATE, "", "#course");
            assertThatThrownBy(() -> aspect.before(context))
                    .isInstanceOf(BusinessException.class)
                    .hasMessageContaining("课程信息不能为空");
        }
    }

    // ==================== before - UPDATE ====================

    @Nested
    @DisplayName("UPDATE 操作校验")
    class UpdateTests {

        @Test
        @DisplayName("更新课程时课程存在且同校通过")
        void updatePasses() {
            Course course = Course.builder().id(1L).creatorId(1L).build();
            User creator = User.builder().id(1L).universityId("uni1").build();
            when(courseMapper.selectById("1")).thenReturn(course);
            when(userMapper.selectById(1L)).thenReturn(creator);

            CourseRequest body = CourseRequest.builder().title("更新标题").build();
            ProceedingJoinPoint jp = mockJoinPoint(new String[]{"course", "id"}, new Object[]{body, 1L});
            DataScopeContext context = mockContext(jp, DataScopeAction.UPDATE, "'1'", "#course");

            aspect.before(context);
        }

        @Test
        @DisplayName("课程不存在时抛出异常")
        void updateCourseNotFound() {
            when(courseMapper.selectById("1")).thenReturn(null);

            CourseRequest body = CourseRequest.builder().title("更新标题").build();
            ProceedingJoinPoint jp = mockJoinPoint(new String[]{"course", "id"}, new Object[]{body, 1L});
            DataScopeContext context = mockContext(jp, DataScopeAction.UPDATE, "'1'", "#course");

            assertThatThrownBy(() -> aspect.before(context))
                    .isInstanceOf(BusinessException.class)
                    .hasMessageContaining("课程不存在");
        }

        @Test
        @DisplayName("创建人不存在时抛出异常")
        void updateCreatorNotFound() {
            Course course = Course.builder().id(1L).creatorId(1L).build();
            when(courseMapper.selectById("1")).thenReturn(course);
            when(userMapper.selectById(1L)).thenReturn(null);

            CourseRequest body = CourseRequest.builder().title("更新标题").build();
            ProceedingJoinPoint jp = mockJoinPoint(new String[]{"course", "id"}, new Object[]{body, 1L});
            DataScopeContext context = mockContext(jp, DataScopeAction.UPDATE, "'1'", "#course");

            assertThatThrownBy(() -> aspect.before(context))
                    .isInstanceOf(BusinessException.class)
                    .hasMessageContaining("无权管理本校外课程");
        }

        @Test
        @DisplayName("创建人与当前用户不同校时抛出异常")
        void updateCreatorDifferentUniversity() {
            Course course = Course.builder().id(1L).creatorId(1L).build();
            User creator = User.builder().id(1L).universityId("uni2").build();
            when(courseMapper.selectById("1")).thenReturn(course);
            when(userMapper.selectById(1L)).thenReturn(creator);

            CourseRequest body = CourseRequest.builder().title("更新标题").build();
            ProceedingJoinPoint jp = mockJoinPoint(new String[]{"course", "id"}, new Object[]{body, 1L});
            DataScopeContext context = mockContext(jp, DataScopeAction.UPDATE, "'1'", "#course");

            assertThatThrownBy(() -> aspect.before(context))
                    .isInstanceOf(BusinessException.class)
                    .hasMessageContaining("无权管理本校外课程");
        }
    }

    // ==================== before - DELETE ====================

    @Nested
    @DisplayName("DELETE 操作校验")
    class DeleteTests {

        @Test
        @DisplayName("删除课程时课程存在且同校通过")
        void deletePasses() {
            Course course = Course.builder().id(1L).creatorId(1L).build();
            User creator = User.builder().id(1L).universityId("uni1").build();
            when(courseMapper.selectById("1")).thenReturn(course);
            when(userMapper.selectById(1L)).thenReturn(creator);

            DataScopeContext context = mockContext(UserType.SCHOOL, 1L, "uni1", DataScopeAction.DELETE, "'1'", "");
            aspect.before(context);
        }

        @Test
        @DisplayName("删除时课程不存在抛出异常")
        void deleteCourseNotFound() {
            when(courseMapper.selectById("1")).thenReturn(null);

            DataScopeContext context = mockContext(UserType.SCHOOL, 1L, "uni1", DataScopeAction.DELETE, "'1'", "");
            assertThatThrownBy(() -> aspect.before(context))
                    .isInstanceOf(BusinessException.class)
                    .hasMessageContaining("课程不存在");
        }

        @Test
        @DisplayName("删除时创建人不存在抛出异常")
        void deleteCreatorNotFound() {
            Course course = Course.builder().id(1L).creatorId(1L).build();
            when(courseMapper.selectById("1")).thenReturn(course);
            when(userMapper.selectById(1L)).thenReturn(null);

            DataScopeContext context = mockContext(UserType.SCHOOL, 1L, "uni1", DataScopeAction.DELETE, "'1'", "");
            assertThatThrownBy(() -> aspect.before(context))
                    .isInstanceOf(BusinessException.class)
                    .hasMessageContaining("无权管理本校外课程");
        }

        @Test
        @DisplayName("删除时创建人不同校抛出异常")
        void deleteCreatorDifferentUniversity() {
            Course course = Course.builder().id(1L).creatorId(1L).build();
            User creator = User.builder().id(1L).universityId("uni2").build();
            when(courseMapper.selectById("1")).thenReturn(course);
            when(userMapper.selectById(1L)).thenReturn(creator);

            DataScopeContext context = mockContext(UserType.SCHOOL, 1L, "uni1", DataScopeAction.DELETE, "'1'", "");
            assertThatThrownBy(() -> aspect.before(context))
                    .isInstanceOf(BusinessException.class)
                    .hasMessageContaining("无权管理本校外课程");
        }
    }

    // ==================== before - courseId empty ====================

    @Test
    @DisplayName("UPDATE 操作课程ID为空抛出异常")
    void updateCourseIdEmpty() {
        ProceedingJoinPoint jp = mockJoinPoint(new String[]{}, new Object[]{});
        DataScopeContext context = mockContext(jp, DataScopeAction.UPDATE, "", "");
        assertThatThrownBy(() -> aspect.before(context))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("课程ID不能为空");
    }

    @Test
    @DisplayName("DELETE 操作课程ID为空抛出异常")
    void deleteCourseIdEmpty() {
        DataScopeContext context = mockContext(UserType.SCHOOL, 1L, "uni1", DataScopeAction.DELETE, "", "");
        assertThatThrownBy(() -> aspect.before(context))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("课程ID不能为空");
    }

    // ==================== dummy methods for SpEL resolution ====================

    void courseAction(CourseRequest course) {}

    // ==================== helpers ====================

    private DataScopeContext mockContext(UserType userType, DataScopeAction action, String id, String query) {
        return mockContext(userType, 1L, "uni1", action, id, query);
    }

    private DataScopeContext mockContext(
            UserType userType, Long userId, String universityId, DataScopeAction action, String id, String query) {
        User user = User.builder()
                .id(userId)
                .userType(userType)
                .universityId(universityId)
                .build();
        DataScopeAccess access = createAccess(action, id, query);

        ProceedingJoinPoint jp = mock(ProceedingJoinPoint.class);
        MethodSignature sig = mock(MethodSignature.class);
        lenient().when(sig.getMethod()).thenReturn(mock(Method.class));
        lenient().when(sig.getParameterNames()).thenReturn(new String[0]);
        lenient().when(jp.getSignature()).thenReturn(sig);
        lenient().when(jp.getArgs()).thenReturn(new Object[0]);

        return new DataScopeContext(jp, access, user);
    }

    private DataScopeContext mockContext(ProceedingJoinPoint jp, DataScopeAction action, String id, String body) {
        User user = User.builder()
                .id(1L)
                .userType(UserType.SCHOOL)
                .universityId("uni1")
                .build();
        DataScopeAccess access = createAccess(action, id, body);
        return new DataScopeContext(jp, access, user);
    }

    private ProceedingJoinPoint mockJoinPoint(String[] paramNames, Object[] args) {
        return mockJoinPoint(paramNames, args, null);
    }

    private ProceedingJoinPoint mockJoinPoint(String[] paramNames, Object[] args, Method realMethod) {
        MethodSignature sig = mock(MethodSignature.class);
        Method method = realMethod != null ? realMethod : mock(Method.class);
        lenient().when(sig.getMethod()).thenReturn(method);
        lenient().when(sig.getParameterNames()).thenReturn(paramNames);

        ProceedingJoinPoint jp = mock(ProceedingJoinPoint.class);
        lenient().when(jp.getSignature()).thenReturn(sig);
        lenient().when(jp.getArgs()).thenReturn(args);
        return jp;
    }

    private Method findMethod(String name, Class<?>... paramTypes) {
        try {
            return getClass().getDeclaredMethod(name, paramTypes);
        } catch (Exception e) {
            throw new AssertionError("Method not found: " + name, e);
        }
    }

    private DataScopeAccess createAccess(DataScopeAction action, String id, String query) {
        return new DataScopeAccess() {
            @Override
            public String resource() {
                return DataScopeResources.COURSE_ADMIN;
            }

            @Override
            public DataScopeAction action() {
                return action;
            }

            @Override
            public String id() {
                return id;
            }

            @Override
            public String body() {
                return query;
            }

            @Override
            public String query() {
                return "";
            }

            @Override
            public Class<? extends java.lang.annotation.Annotation> annotationType() {
                return DataScopeAccess.class;
            }
        };
    }
}
