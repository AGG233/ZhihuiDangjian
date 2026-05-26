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
import com.rauio.smartdangjian.server.content.mapper.ChapterMapper;
import com.rauio.smartdangjian.server.content.mapper.CourseMapper;
import com.rauio.smartdangjian.server.content.pojo.entity.Chapter;
import com.rauio.smartdangjian.server.content.pojo.entity.Course;
import com.rauio.smartdangjian.server.quiz.aop.QuizAdminAccessAspect;
import com.rauio.smartdangjian.server.quiz.mapper.QuizMapper;
import com.rauio.smartdangjian.server.quiz.mapper.QuizOptionMapper;
import com.rauio.smartdangjian.server.quiz.pojo.entity.Quiz;
import com.rauio.smartdangjian.server.quiz.pojo.entity.QuizOption;
import com.rauio.smartdangjian.server.user.mapper.UserMapper;
import com.rauio.smartdangjian.server.user.pojo.entity.User;
import com.rauio.smartdangjian.utils.spec.UserType;

@ExtendWith(MockitoExtension.class)
@DisplayName("QuizAdminAccessAspect 单元测试")
class QuizAdminAccessAspectTest {

    @Mock
    private QuizMapper quizMapper;

    @Mock
    private QuizOptionMapper quizOptionMapper;

    @Mock
    private ChapterMapper chapterMapper;

    @Mock
    private CourseMapper courseMapper;

    @Mock
    private UserMapper userMapper;

    @InjectMocks
    private QuizAdminAccessAspect aspect;

    // ==================== supports ====================

    @Test
    @DisplayName("supports 返回 true 支持 QUIZ_ADMIN")
    void supportsTrue() {
        assertThat(aspect.supports(DataScopeResources.QUIZ_ADMIN)).isTrue();
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
    @DisplayName("学生无权管理题目")
    void beforeStudentNotAllowed() {
        DataScopeContext context = mockContext(UserType.STUDENT, DataScopeAction.UPDATE, "", "");
        assertThatThrownBy(() -> aspect.before(context))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("无权管理题目");
    }

    @Test
    @DisplayName("学校管理员未绑定学校抛出异常")
    void beforeSchoolNoUniversityId() {
        DataScopeContext context = mockContext(UserType.SCHOOL, 1L, null, DataScopeAction.UPDATE, "'1'", "'QUIZ'");
        assertThatThrownBy(() -> aspect.before(context))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("未绑定学校");
    }

    // ==================== before - assertSameUniversity QUIZ type ====================

    @Nested
    @DisplayName("QUIZ 资源类型校验")
    class QuizResourceTests {

        @Test
        @DisplayName("QUIZ 资源完整链路通过")
        void quizResourcePasses() {
            DataScopeContext context = mockContext(UserType.SCHOOL, 1L, "uni1", DataScopeAction.UPDATE, "'1'", "'QUIZ'");
            when(quizMapper.selectById(1L)).thenReturn(Quiz.builder().id(1L).chapterId(1L).build());
            when(chapterMapper.selectById(1L)).thenReturn(Chapter.builder().id(1L).courseId(1L).build());
            when(courseMapper.selectById(1L)).thenReturn(Course.builder().id(1L).creatorId(1L).build());
            when(userMapper.selectById(1L)).thenReturn(User.builder().id(1L).universityId("uni1").build());

            aspect.before(context);
        }

        @Test
        @DisplayName("题目不存在时抛出异常")
        void quizNotFound() {
            DataScopeContext context = mockContext(UserType.SCHOOL, 1L, "uni1", DataScopeAction.UPDATE, "'1'", "'QUIZ'");
            when(quizMapper.selectById(1L)).thenReturn(null);

            assertThatThrownBy(() -> aspect.before(context))
                    .isInstanceOf(BusinessException.class)
                    .hasMessageContaining("题目不存在");
        }

        @Test
        @DisplayName("章节不存在时抛出异常")
        void chapterNotFound() {
            DataScopeContext context = mockContext(UserType.SCHOOL, 1L, "uni1", DataScopeAction.UPDATE, "'1'", "'QUIZ'");
            when(quizMapper.selectById(1L)).thenReturn(Quiz.builder().id(1L).chapterId(1L).build());
            when(chapterMapper.selectById(1L)).thenReturn(null);

            assertThatThrownBy(() -> aspect.before(context))
                    .isInstanceOf(BusinessException.class)
                    .hasMessageContaining("章节不存在");
        }

        @Test
        @DisplayName("课程不存在时抛出异常")
        void courseNotFound() {
            DataScopeContext context = mockContext(UserType.SCHOOL, 1L, "uni1", DataScopeAction.UPDATE, "'1'", "'QUIZ'");
            when(quizMapper.selectById(1L)).thenReturn(Quiz.builder().id(1L).chapterId(1L).build());
            when(chapterMapper.selectById(1L)).thenReturn(Chapter.builder().id(1L).courseId(1L).build());
            when(courseMapper.selectById(1L)).thenReturn(null);

            assertThatThrownBy(() -> aspect.before(context))
                    .isInstanceOf(BusinessException.class)
                    .hasMessageContaining("课程不存在");
        }

        @Test
        @DisplayName("创建人不存在时抛出异常")
        void creatorNotFound() {
            DataScopeContext context = mockContext(UserType.SCHOOL, 1L, "uni1", DataScopeAction.UPDATE, "'1'", "'QUIZ'");
            when(quizMapper.selectById(1L)).thenReturn(Quiz.builder().id(1L).chapterId(1L).build());
            when(chapterMapper.selectById(1L)).thenReturn(Chapter.builder().id(1L).courseId(1L).build());
            when(courseMapper.selectById(1L)).thenReturn(Course.builder().id(1L).creatorId(1L).build());
            when(userMapper.selectById(1L)).thenReturn(null);

            assertThatThrownBy(() -> aspect.before(context))
                    .isInstanceOf(BusinessException.class)
                    .hasMessageContaining("无权管理本校外题目");
        }

        @Test
        @DisplayName("创建人与当前用户不同校时抛出异常")
        void creatorDifferentUniversity() {
            DataScopeContext context = mockContext(UserType.SCHOOL, 1L, "uni1", DataScopeAction.UPDATE, "'1'", "'QUIZ'");
            when(quizMapper.selectById(1L)).thenReturn(Quiz.builder().id(1L).chapterId(1L).build());
            when(chapterMapper.selectById(1L)).thenReturn(Chapter.builder().id(1L).courseId(1L).build());
            when(courseMapper.selectById(1L)).thenReturn(Course.builder().id(1L).creatorId(1L).build());
            when(userMapper.selectById(1L)).thenReturn(User.builder().id(1L).universityId("uni2").build());

            assertThatThrownBy(() -> aspect.before(context))
                    .isInstanceOf(BusinessException.class)
                    .hasMessageContaining("无权管理本校外题目");
        }
    }

    // ==================== before - assertSameUniversity OPTION type ====================

    @Nested
    @DisplayName("OPTION 资源类型校验")
    class OptionResourceTests {

        @Test
        @DisplayName("OPTION 资源完整链路通过")
        void optionResourcePasses() {
            DataScopeContext context = mockContext(UserType.SCHOOL, 1L, "uni1", DataScopeAction.UPDATE, "'1'", "'OPTION'");
            when(quizOptionMapper.selectById("1")).thenReturn(QuizOption.builder().id(1L).quizId(1L).build());
            when(quizMapper.selectById(1L)).thenReturn(Quiz.builder().id(1L).chapterId(1L).build());
            when(chapterMapper.selectById(1L)).thenReturn(Chapter.builder().id(1L).courseId(1L).build());
            when(courseMapper.selectById(1L)).thenReturn(Course.builder().id(1L).creatorId(1L).build());
            when(userMapper.selectById(1L)).thenReturn(User.builder().id(1L).universityId("uni1").build());

            aspect.before(context);
        }

        @Test
        @DisplayName("选项不存在时抛出异常")
        void optionNotFound() {
            DataScopeContext context = mockContext(UserType.SCHOOL, 1L, "uni1", DataScopeAction.UPDATE, "'1'", "'OPTION'");
            when(quizOptionMapper.selectById("1")).thenReturn(null);

            assertThatThrownBy(() -> aspect.before(context))
                    .isInstanceOf(BusinessException.class)
                    .hasMessageContaining("题目选项不存在");
        }
    }

    // ==================== before - unsupported resource type ====================

    @Test
    @DisplayName("不支持的资源类型抛出异常")
    void unsupportedResourceType() {
        DataScopeContext context = mockContext(UserType.SCHOOL, 1L, "uni1", DataScopeAction.UPDATE, "'1'", "'INVALID'");
        assertThatThrownBy(() -> aspect.before(context))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("不支持的题目资源");
    }

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

    private DataScopeAccess createAccess(DataScopeAction action, String id, String query) {
        return new DataScopeAccess() {
            @Override
            public String resource() {
                return DataScopeResources.QUIZ_ADMIN;
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
                return "";
            }

            @Override
            public String query() {
                return query;
            }

            @Override
            public Class<? extends java.lang.annotation.Annotation> annotationType() {
                return DataScopeAccess.class;
            }
        };
    }
}
