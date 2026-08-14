package com.rauio.smartdangjian.aop;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.lang.reflect.Method;
import java.util.List;

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
import com.rauio.smartdangjian.pojo.response.Result;
import com.rauio.smartdangjian.server.learning.aop.LearningResourceAccessAspect;
import com.rauio.smartdangjian.server.learning.mapper.UserChapterProgressMapper;
import com.rauio.smartdangjian.server.learning.mapper.UserLearningRecordMapper;
import com.rauio.smartdangjian.server.learning.pojo.entity.UserChapterProgress;
import com.rauio.smartdangjian.server.learning.pojo.entity.UserLearningRecord;
import com.rauio.smartdangjian.server.learning.pojo.response.UserChapterProgressResponse;
import com.rauio.smartdangjian.server.learning.pojo.response.UserLearningRecordResponse;
import com.rauio.smartdangjian.server.user.mapper.UserMapper;
import com.rauio.smartdangjian.server.user.pojo.entity.User;
import com.rauio.smartdangjian.utils.spec.UserType;

@ExtendWith(MockitoExtension.class)
@DisplayName("LearningResourceAccessAspect 单元测试")
class LearningResourceAccessAspectTest {

    @Mock
    private UserLearningRecordMapper learningRecordMapper;

    @Mock
    private UserChapterProgressMapper chapterProgressMapper;

    @Mock
    private UserMapper userMapper;

    @InjectMocks
    private LearningResourceAccessAspect aspect;

    // ==================== supports ====================

    @Test
    @DisplayName("supports 返回 true 支持 LEARNING_RECORD")
    void supportsLearningRecord() {
        assertThat(aspect.supports(DataScopeResources.LEARNING_RECORD)).isTrue();
    }

    @Test
    @DisplayName("supports 返回 true 支持 CHAPTER_PROGRESS")
    void supportsChapterProgress() {
        assertThat(aspect.supports(DataScopeResources.CHAPTER_PROGRESS)).isTrue();
    }

    @Test
    @DisplayName("supports 返回 false 不支持其他资源")
    void supportsFalse() {
        assertThat(aspect.supports("OTHER")).isFalse();
    }

    // ==================== before - READ with LEARNING_RECORD ====================

    @Nested
    @DisplayName("READ 学习记录校验")
    class ReadLearningRecordTests {

        @Test
        @DisplayName("管理员读取学习记录通过")
        void readManagerBypass() {
            when(learningRecordMapper.selectById("1"))
                    .thenReturn(UserLearningRecord.builder().id(1L).userId(1L).build());

            DataScopeContext context = mockContext(
                    UserType.MANAGER, 1L, "uni1", DataScopeResources.LEARNING_RECORD, DataScopeAction.READ, "'1'");
            aspect.before(context);
        }

        @Test
        @DisplayName("学生读取自己的学习记录通过")
        void readOwnRecord() {
            when(learningRecordMapper.selectById("1"))
                    .thenReturn(UserLearningRecord.builder().id(1L).userId(1L).build());

            DataScopeContext context = mockContext(
                    UserType.STUDENT, 1L, "uni1", DataScopeResources.LEARNING_RECORD, DataScopeAction.READ, "'1'");
            aspect.before(context);
        }

        @Test
        @DisplayName("学生读取他人学习记录拒绝")
        void readOthersRecord() {
            when(learningRecordMapper.selectById("1"))
                    .thenReturn(UserLearningRecord.builder().id(1L).userId(2L).build());

            DataScopeContext context = mockContext(
                    UserType.STUDENT, 1L, "uni1", DataScopeResources.LEARNING_RECORD, DataScopeAction.READ, "'1'");
            assertThatThrownBy(() -> aspect.before(context))
                    .isInstanceOf(BusinessException.class)
                    .hasMessageContaining("无权访问该资源");
        }

        @Test
        @DisplayName("学校管理员读取本校学习记录通过")
        void readSameUniversityRecord() {
            when(learningRecordMapper.selectById("1"))
                    .thenReturn(UserLearningRecord.builder().id(1L).userId(2L).build());
            when(userMapper.selectById(2L))
                    .thenReturn(User.builder().id(2L).universityId("uni1").build());

            DataScopeContext context = mockContext(
                    UserType.SCHOOL, 1L, "uni1", DataScopeResources.LEARNING_RECORD, DataScopeAction.READ, "'1'");
            aspect.before(context);
        }

        @Test
        @DisplayName("学校管理员读取外校学习记录拒绝")
        void readOtherUniversityRecord() {
            when(learningRecordMapper.selectById("1"))
                    .thenReturn(UserLearningRecord.builder().id(1L).userId(2L).build());
            when(userMapper.selectById(2L))
                    .thenReturn(User.builder().id(2L).universityId("uni2").build());

            DataScopeContext context = mockContext(
                    UserType.SCHOOL, 1L, "uni1", DataScopeResources.LEARNING_RECORD, DataScopeAction.READ, "'1'");
            assertThatThrownBy(() -> aspect.before(context))
                    .isInstanceOf(BusinessException.class)
                    .hasMessageContaining("无权访问本校外的数据");
        }

        @Test
        @DisplayName("学校管理员未绑定学校时读取抛出异常")
        void readSchoolWithoutUniversityId() {
            when(learningRecordMapper.selectById("1"))
                    .thenReturn(UserLearningRecord.builder().id(1L).userId(2L).build());
            DataScopeContext context = mockContext(
                    UserType.SCHOOL, 1L, null, DataScopeResources.LEARNING_RECORD, DataScopeAction.READ, "'1'");
            assertThatThrownBy(() -> aspect.before(context))
                    .isInstanceOf(BusinessException.class)
                    .hasMessageContaining("未绑定学校");
        }

        @Test
        @DisplayName("读取不存在的学习记录抛出异常")
        void readRecordNotFound() {
            when(learningRecordMapper.selectById("1")).thenReturn(null);

            DataScopeContext context = mockContext(
                    UserType.MANAGER, 1L, "uni1", DataScopeResources.LEARNING_RECORD, DataScopeAction.READ, "'1'");
            assertThatThrownBy(() -> aspect.before(context))
                    .isInstanceOf(BusinessException.class)
                    .hasMessageContaining("学习记录不存在");
        }
    }

    // ==================== before - READ with CHAPTER_PROGRESS ====================

    @Nested
    @DisplayName("READ 章节进度校验")
    class ReadChapterProgressTests {

        @Test
        @DisplayName("学生读取自己的进度通过")
        void readOwnProgress() {
            when(chapterProgressMapper.selectById("1"))
                    .thenReturn(UserChapterProgress.builder().id(1L).userId(1L).build());

            DataScopeContext context = mockContext(
                    UserType.STUDENT, 1L, "uni1", DataScopeResources.CHAPTER_PROGRESS, DataScopeAction.READ, "'1'");
            aspect.before(context);
        }

        @Test
        @DisplayName("学生读取他人进度拒绝")
        void readOthersProgress() {
            when(chapterProgressMapper.selectById("1"))
                    .thenReturn(UserChapterProgress.builder().id(1L).userId(2L).build());

            DataScopeContext context = mockContext(
                    UserType.STUDENT, 1L, "uni1", DataScopeResources.CHAPTER_PROGRESS, DataScopeAction.READ, "'1'");
            assertThatThrownBy(() -> aspect.before(context))
                    .isInstanceOf(BusinessException.class)
                    .hasMessageContaining("无权访问该资源");
        }

        @Test
        @DisplayName("读取不存在的进度记录抛出异常")
        void readProgressNotFound() {
            when(chapterProgressMapper.selectById("1")).thenReturn(null);

            DataScopeContext context = mockContext(
                    UserType.MANAGER, 1L, "uni1", DataScopeResources.CHAPTER_PROGRESS, DataScopeAction.READ, "'1'");
            assertThatThrownBy(() -> aspect.before(context))
                    .isInstanceOf(BusinessException.class)
                    .hasMessageContaining("进度记录不存在");
        }
    }

    // ==================== before - DELETE with LEARNING_RECORD ====================

    @Nested
    @DisplayName("DELETE 学习记录校验")
    class DeleteLearningRecordTests {

        @Test
        @DisplayName("管理员删除学习记录通过")
        void deleteManagerBypass() {
            when(learningRecordMapper.selectById("1"))
                    .thenReturn(UserLearningRecord.builder().id(1L).userId(2L).build());
            DataScopeContext context = mockContext(
                    UserType.MANAGER, 1L, "uni1", DataScopeResources.LEARNING_RECORD, DataScopeAction.DELETE, "'1'");
            aspect.before(context);
        }

        @Test
        @DisplayName("学校管理员删除本校学习记录通过")
        void deleteSameUniversityRecord() {
            when(learningRecordMapper.selectById("1"))
                    .thenReturn(UserLearningRecord.builder().id(1L).userId(2L).build());
            when(userMapper.selectById(2L))
                    .thenReturn(User.builder().id(2L).universityId("uni1").build());

            DataScopeContext context = mockContext(
                    UserType.SCHOOL, 1L, "uni1", DataScopeResources.LEARNING_RECORD, DataScopeAction.DELETE, "'1'");
            aspect.before(context);
        }

        @Test
        @DisplayName("学校管理员删除外校学习记录拒绝")
        void deleteOtherUniversityRecord() {
            when(learningRecordMapper.selectById("1"))
                    .thenReturn(UserLearningRecord.builder().id(1L).userId(2L).build());
            when(userMapper.selectById(2L))
                    .thenReturn(User.builder().id(2L).universityId("uni2").build());

            DataScopeContext context = mockContext(
                    UserType.SCHOOL, 1L, "uni1", DataScopeResources.LEARNING_RECORD, DataScopeAction.DELETE, "'1'");
            assertThatThrownBy(() -> aspect.before(context))
                    .isInstanceOf(BusinessException.class)
                    .hasMessageContaining("无权访问本校外的数据");
        }

        @Test
        @DisplayName("学生无权删除学习记录")
        void deleteStudentNotAllowed() {
            when(learningRecordMapper.selectById("1"))
                    .thenReturn(UserLearningRecord.builder().id(1L).userId(2L).build());
            DataScopeContext context = mockContext(
                    UserType.STUDENT, 1L, "uni1", DataScopeResources.LEARNING_RECORD, DataScopeAction.DELETE, "'1'");
            assertThatThrownBy(() -> aspect.before(context))
                    .isInstanceOf(BusinessException.class)
                    .hasMessageContaining("无权删除该资源");
        }

        @Test
        @DisplayName("学校管理员未绑定学校时删除抛出异常")
        void deleteSchoolWithoutUniversityId() {
            when(learningRecordMapper.selectById("1"))
                    .thenReturn(UserLearningRecord.builder().id(1L).userId(2L).build());
            DataScopeContext context = mockContext(
                    UserType.SCHOOL, 1L, null, DataScopeResources.LEARNING_RECORD, DataScopeAction.DELETE, "'1'");
            assertThatThrownBy(() -> aspect.before(context))
                    .isInstanceOf(BusinessException.class)
                    .hasMessageContaining("未绑定学校");
        }

        @Test
        @DisplayName("删除不存在的学习记录抛出异常")
        void deleteRecordNotFound() {
            when(learningRecordMapper.selectById("1")).thenReturn(null);

            DataScopeContext context = mockContext(
                    UserType.MANAGER, 1L, "uni1", DataScopeResources.LEARNING_RECORD, DataScopeAction.DELETE, "'1'");
            assertThatThrownBy(() -> aspect.before(context))
                    .isInstanceOf(BusinessException.class)
                    .hasMessageContaining("学习记录不存在");
        }
    }

    // ==================== before - DELETE with CHAPTER_PROGRESS ====================

    @Nested
    @DisplayName("DELETE 章节进度校验")
    class DeleteChapterProgressTests {

        @Test
        @DisplayName("管理员删除进度通过")
        void deleteManagerBypass() {
            when(chapterProgressMapper.selectById("1"))
                    .thenReturn(UserChapterProgress.builder().id(1L).userId(2L).build());
            DataScopeContext context = mockContext(
                    UserType.MANAGER, 1L, "uni1", DataScopeResources.CHAPTER_PROGRESS, DataScopeAction.DELETE, "'1'");
            aspect.before(context);
        }

        @Test
        @DisplayName("删除不存在的进度记录抛出异常")
        void deleteProgressNotFound() {
            when(chapterProgressMapper.selectById("1")).thenReturn(null);

            DataScopeContext context = mockContext(
                    UserType.MANAGER, 1L, "uni1", DataScopeResources.CHAPTER_PROGRESS, DataScopeAction.DELETE, "'1'");
            assertThatThrownBy(() -> aspect.before(context))
                    .isInstanceOf(BusinessException.class)
                    .hasMessageContaining("进度记录不存在");
        }
    }

    // ==================== before - unsupported resource ====================

    @Test
    @DisplayName("不支持的资源类型抛出异常")
    void unsupportedResource() {
        User user = User.builder()
                .id(1L)
                .userType(UserType.MANAGER)
                .universityId("uni1")
                .build();
        DataScopeAccess access = new DataScopeAccess() {
            @Override
            public String resource() {
                return "INVALID";
            }

            @Override
            public DataScopeAction action() {
                return DataScopeAction.READ;
            }

            @Override
            public String id() {
                return "'1'";
            }

            @Override
            public String body() {
                return "";
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

        ProceedingJoinPoint jp = mock(ProceedingJoinPoint.class);
        MethodSignature sig = mock(MethodSignature.class);
        lenient().when(sig.getMethod()).thenReturn(findMethod("dummyLearningAction"));
        lenient().when(jp.getSignature()).thenReturn(sig);
        lenient().when(jp.getArgs()).thenReturn(new Object[0]);

        DataScopeContext context = new DataScopeContext(jp, access, user);

        assertThatThrownBy(() -> aspect.before(context))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("不支持的学习资源");
    }

    // ==================== after - FILTER ====================

    @Nested
    @DisplayName("after 后置处理校验")
    class AfterTests {

        @Test
        @DisplayName("非 FILTER 操作直接返回原结果")
        void afterNonFilterReturnsOriginal() {
            Object result = Result.ok("any");
            DataScopeContext context = mockContext(
                    UserType.SCHOOL, 1L, "uni1", DataScopeResources.LEARNING_RECORD, DataScopeAction.READ, "");
            assertThat(aspect.after(context, result)).isSameAs(result);
        }

        @Test
        @DisplayName("管理员 FILTER 直接返回原结果")
        void afterManagerReturnsOriginal() {
            Object result = Result.ok("any");
            DataScopeContext context = mockContext(
                    UserType.MANAGER, 1L, "uni1", DataScopeResources.LEARNING_RECORD, DataScopeAction.FILTER, "'1'");
            assertThat(aspect.after(context, result)).isSameAs(result);
        }

        @Test
        @DisplayName("学生 FILTER 直接返回原结果")
        void afterStudentReturnsOriginal() {
            Object result = Result.ok("any");
            DataScopeContext context = mockContext(
                    UserType.STUDENT, 1L, "uni1", DataScopeResources.LEARNING_RECORD, DataScopeAction.FILTER, "'1'");
            assertThat(aspect.after(context, result)).isSameAs(result);
        }

        @Test
        @DisplayName("FILTER 结果不是 Result 类型时直接返回")
        void afterNotResultTypeReturnsOriginal() {
            Object result = "plain string";
            DataScopeContext context = mockContext(
                    UserType.SCHOOL, 1L, "uni1", DataScopeResources.LEARNING_RECORD, DataScopeAction.FILTER, "'1'");
            assertThat(aspect.after(context, result)).isSameAs(result);
        }

        @Test
        @DisplayName("学校管理员未绑定学校时 FILTER 抛出异常")
        void afterSchoolWithoutUniversityId() {
            Object result = Result.ok(List.of());
            ProceedingJoinPoint jp = mockJoinPoint(new String[] {"chapterId"}, new Object[] {1L});
            DataScopeContext context = mockContext(
                    jp, UserType.SCHOOL, 1L, null, DataScopeResources.LEARNING_RECORD, DataScopeAction.FILTER, "'1'");
            assertThatThrownBy(() -> aspect.after(context, result))
                    .isInstanceOf(BusinessException.class)
                    .hasMessageContaining("未绑定学校");
        }

        @Test
        @DisplayName("FILTER 学习记录按学校过滤")
        void afterFiltersLearningRecordByUniversity() {
            UserLearningRecordResponse ownRecord =
                    UserLearningRecordResponse.builder().id(1L).userId(2L).build();
            UserLearningRecordResponse otherRecord =
                    UserLearningRecordResponse.builder().id(2L).userId(3L).build();
            Object result = Result.ok(List.of(ownRecord, otherRecord));

            when(userMapper.selectById(2L))
                    .thenReturn(User.builder().id(2L).universityId("uni1").build());
            when(userMapper.selectById(3L))
                    .thenReturn(User.builder().id(3L).universityId("uni2").build());

            ProceedingJoinPoint jp = mockJoinPoint(new String[] {"chapterId"}, new Object[] {1L});
            DataScopeContext context = mockContext(
                    jp, UserType.SCHOOL, 1L, "uni1", DataScopeResources.LEARNING_RECORD, DataScopeAction.FILTER, "'1'");

            Object after = aspect.after(context, result);
            assertThat(after).isInstanceOf(Result.class);
            @SuppressWarnings("unchecked")
            List<UserLearningRecordResponse> data = (List<UserLearningRecordResponse>) ((Result<?>) after).getData();
            assertThat(data).hasSize(1);
            assertThat(data.get(0).getId()).isEqualTo(1L);
        }

        @Test
        @DisplayName("FILTER 章节进度按学校过滤")
        void afterFiltersChapterProgressByUniversity() {
            UserChapterProgressResponse ownProgress =
                    UserChapterProgressResponse.builder().id(1L).userId(2L).build();
            UserChapterProgressResponse otherProgress =
                    UserChapterProgressResponse.builder().id(2L).userId(3L).build();
            Object result = Result.ok(List.of(ownProgress, otherProgress));

            when(userMapper.selectById(2L))
                    .thenReturn(User.builder().id(2L).universityId("uni1").build());
            when(userMapper.selectById(3L))
                    .thenReturn(User.builder().id(3L).universityId("uni2").build());

            ProceedingJoinPoint jp = mockJoinPoint(new String[] {"chapterId"}, new Object[] {1L});
            DataScopeContext context = mockContext(
                    jp,
                    UserType.SCHOOL,
                    1L,
                    "uni1",
                    DataScopeResources.CHAPTER_PROGRESS,
                    DataScopeAction.FILTER,
                    "'1'");

            Object after = aspect.after(context, result);
            assertThat(after).isInstanceOf(Result.class);
            @SuppressWarnings("unchecked")
            List<UserChapterProgressResponse> data = (List<UserChapterProgressResponse>) ((Result<?>) after).getData();
            assertThat(data).hasSize(1);
            assertThat(data.get(0).getId()).isEqualTo(1L);
        }

        @Test
        @DisplayName("FILTER data 不是 List 时直接返回原结果")
        void afterFilterDataNotList() {
            Object result = Result.ok("single item");
            ProceedingJoinPoint jp = mockJoinPoint(new String[] {"chapterId"}, new Object[] {1L});
            DataScopeContext context = mockContext(
                    jp, UserType.SCHOOL, 1L, "uni1", DataScopeResources.LEARNING_RECORD, DataScopeAction.FILTER, "'1'");
            assertThat(aspect.after(context, result)).isSameAs(result);
        }
    }

    // ==================== helpers ====================

    private DataScopeContext mockContext(
            UserType userType, Long userId, String universityId, String resource, DataScopeAction action, String id) {
        UserStub user = new UserStub(userId, userType, universityId);
        DataScopeAccess access = createAccess(resource, action, id, "");

        ProceedingJoinPoint jp = mock(ProceedingJoinPoint.class);
        MethodSignature sig = mock(MethodSignature.class);
        lenient().when(sig.getMethod()).thenReturn(findMethod("dummyLearningAction"));
        lenient().when(sig.getParameterNames()).thenReturn(new String[0]);
        lenient().when(jp.getSignature()).thenReturn(sig);
        lenient().when(jp.getArgs()).thenReturn(new Object[0]);

        return new DataScopeContext(jp, access, user);
    }

    private DataScopeContext mockContext(
            ProceedingJoinPoint jp,
            UserType userType,
            Long userId,
            String universityId,
            String resource,
            DataScopeAction action,
            String id) {
        UserStub user = new UserStub(userId, userType, universityId);
        DataScopeAccess access = createAccess(resource, action, id, "");
        return new DataScopeContext(jp, access, user);
    }

    private ProceedingJoinPoint mockJoinPoint(String[] paramNames, Object[] args) {
        MethodSignature sig = mock(MethodSignature.class);
        lenient().when(sig.getMethod()).thenReturn(findMethod("dummyFilterAction"));
        lenient().when(sig.getParameterNames()).thenReturn(paramNames);

        ProceedingJoinPoint jp = mock(ProceedingJoinPoint.class);
        lenient().when(jp.getSignature()).thenReturn(sig);
        lenient().when(jp.getArgs()).thenReturn(args);
        return jp;
    }

    private String dummyLearningAction() {
        return null;
    }

    private String dummyFilterAction(Long id) {
        return null;
    }

    private Method findMethod(String methodName) {
        for (Method method : getClass().getDeclaredMethods()) {
            if (method.getName().equals(methodName)) {
                return method;
            }
        }
        throw new AssertionError("Method not found: " + methodName);
    }

    private DataScopeAccess createAccess(String resource, DataScopeAction action, String id, String body) {
        return new DataScopeAccess() {
            @Override
            public String resource() {
                return resource;
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
                return body;
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

    /**
     * Simple stub implementing CurrentUserPrincipal for test convenience.
     */
    private static class UserStub implements com.rauio.smartdangjian.security.CurrentUserPrincipal {
        private final Long id;
        private final UserType userType;
        private final String universityId;

        UserStub(Long id, UserType userType, String universityId) {
            this.id = id;
            this.userType = userType;
            this.universityId = universityId;
        }

        @Override
        public Long getId() {
            return id;
        }

        @Override
        public UserType getUserType() {
            return userType;
        }

        @Override
        public String getUniversityId() {
            return universityId;
        }
    }
}
