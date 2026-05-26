package com.rauio.smartdangjian.server.learning.aop;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

import java.lang.reflect.Method;
import java.util.List;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.reflect.MethodSignature;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.rauio.smartdangjian.aop.annotation.DataScopeAccess;
import com.rauio.smartdangjian.aop.support.DataScopeAction;
import com.rauio.smartdangjian.aop.support.DataScopeContext;
import com.rauio.smartdangjian.aop.support.DataScopeResources;
import com.rauio.smartdangjian.constants.ErrorConstants;
import com.rauio.smartdangjian.exception.BusinessException;
import com.rauio.smartdangjian.pojo.response.Result;
import com.rauio.smartdangjian.security.CurrentUserPrincipal;
import com.rauio.smartdangjian.server.learning.mapper.UserChapterProgressMapper;
import com.rauio.smartdangjian.server.learning.mapper.UserLearningRecordMapper;
import com.rauio.smartdangjian.server.learning.pojo.entity.UserLearningRecord;
import com.rauio.smartdangjian.server.learning.pojo.response.UserChapterProgressResponse;
import com.rauio.smartdangjian.server.learning.pojo.response.UserLearningRecordResponse;
import com.rauio.smartdangjian.server.user.mapper.UserMapper;
import com.rauio.smartdangjian.server.user.pojo.entity.User;
import com.rauio.smartdangjian.utils.spec.UserType;

@ExtendWith(MockitoExtension.class)
class LearningResourceAccessAspectTest {

    @Mock
    private UserLearningRecordMapper learningRecordMapper;

    @Mock
    private UserChapterProgressMapper chapterProgressMapper;

    @Mock
    private UserMapper userMapper;

    @InjectMocks
    private LearningResourceAccessAspect aspect;

    @Test
    @DisplayName("supports returns true for supported resources")
    void supportsTrue() {
        assertThat(aspect.supports(DataScopeResources.LEARNING_RECORD)).isTrue();
        assertThat(aspect.supports(DataScopeResources.CHAPTER_PROGRESS)).isTrue();
    }

    @Test
    @DisplayName("supports returns false for unsupported resource")
    void supportsFalse() {
        assertThat(aspect.supports("UNSUPPORTED")).isFalse();
    }

    // ==================== before - assertSameUniversity targetUser null ====================

    @Test
    @DisplayName("before READ with SCHOOL and target user not found throws unauthorized")
    void beforeReadSchoolUserNotFoundThrows() {
        when(learningRecordMapper.selectById("1")).thenReturn(
                UserLearningRecord.builder().id(1L).userId(99L).build());
        when(userMapper.selectById(99L)).thenReturn(null);

        DataScopeContext context = mockContext(UserType.SCHOOL, 1L, "uni1", DataScopeAction.READ, "'1'", "");

        assertThatThrownBy(() -> aspect.before(context))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("无权访问");
    }

    // ==================== after - belongsToCurrentSchool targetUser null ====================

    @Test
    @DisplayName("after FILTER with SCHOOL filters out items when target user not found")
    void afterFilterSchoolUserNotFoundFiltersOut() {
        CurrentUserPrincipal user = createUser(1L, "uni1", UserType.SCHOOL);
        DataScopeContext context = mockFilterContext(
                DataScopeResources.LEARNING_RECORD, "'1'", user);

        UserLearningRecordResponse item = UserLearningRecordResponse.builder()
                .userId(99L).chapterId(1L).build();
        Result<List<UserLearningRecordResponse>> result = Result.ok(List.of(item));

        Object filtered = aspect.after(context, result);

        assertThat(filtered).isInstanceOf(Result.class);
        Result<?> wrapped = (Result<?>) filtered;
        List<?> data = (List<?>) wrapped.getData();
        assertThat(data).isEmpty();
    }

    @Test
    @DisplayName("after FILTER with CHAPTER_PROGRESS filters items when target user not found")
    void afterFilterChapterProgressUserNotFoundFiltersOut() {
        CurrentUserPrincipal user = createUser(1L, "uni1", UserType.SCHOOL);
        DataScopeContext context = mockFilterContext(
                DataScopeResources.CHAPTER_PROGRESS, "'1'", user);

        UserChapterProgressResponse item = UserChapterProgressResponse.builder()
                .userId(99L).chapterId(1L).build();
        Result<List<UserChapterProgressResponse>> result = Result.ok(List.of(item));

        Object filtered = aspect.after(context, result);

        assertThat(filtered).isInstanceOf(Result.class);
        Result<?> wrapped = (Result<?>) filtered;
        List<?> data = (List<?>) wrapped.getData();
        assertThat(data).isEmpty();
    }

    // ==================== helpers ====================

    private DataScopeContext mockContext(
            UserType userType, Long userId, String universityId,
            DataScopeAction action, String id, String query) {
        CurrentUserPrincipal user = createUser(userId, universityId, userType);
        DataScopeAccess access = createAccess(action, id, query);

        ProceedingJoinPoint jp = mock(ProceedingJoinPoint.class);
        MethodSignature sig = mock(MethodSignature.class);
        lenient().when(sig.getMethod()).thenReturn(mock(Method.class));
        lenient().when(sig.getParameterNames()).thenReturn(new String[0]);
        lenient().when(jp.getSignature()).thenReturn(sig);
        lenient().when(jp.getArgs()).thenReturn(new Object[0]);

        return new DataScopeContext(jp, access, user);
    }

    private DataScopeContext mockFilterContext(
            String resource, String id, CurrentUserPrincipal user) {
        DataScopeAccess access = createAccess(DataScopeAction.FILTER, id, "");

        ProceedingJoinPoint jp = mock(ProceedingJoinPoint.class);
        MethodSignature sig = mock(MethodSignature.class);
        lenient().when(sig.getMethod()).thenReturn(mock(Method.class));
        lenient().when(sig.getParameterNames()).thenReturn(new String[0]);
        lenient().when(jp.getSignature()).thenReturn(sig);
        lenient().when(jp.getArgs()).thenReturn(new Object[0]);

        return new DataScopeContext(jp, access, user);
    }

    private CurrentUserPrincipal createUser(Long id, String universityId, UserType userType) {
        CurrentUserPrincipal user = mock(CurrentUserPrincipal.class);
        lenient().when(user.getId()).thenReturn(id);
        lenient().when(user.getUniversityId()).thenReturn(universityId);
        lenient().when(user.getUserType()).thenReturn(userType);
        return user;
    }

    private DataScopeAccess createAccess(DataScopeAction action, String id, String query) {
        return new DataScopeAccess() {
            @Override
            public String resource() { return DataScopeResources.LEARNING_RECORD; }
            @Override
            public DataScopeAction action() { return action; }
            @Override
            public String id() { return id; }
            @Override
            public String body() { return ""; }
            @Override
            public String query() { return query; }
            @Override
            public Class<? extends java.lang.annotation.Annotation> annotationType() {
                return DataScopeAccess.class;
            }
        };
    }
}
