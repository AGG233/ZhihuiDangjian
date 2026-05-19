package com.rauio.smartdangjian.server.resource.aop;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

import java.lang.reflect.Method;

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
import com.rauio.smartdangjian.exception.BusinessException;
import com.rauio.smartdangjian.server.resource.service.ResourceMetaService;
import com.rauio.smartdangjian.server.user.mapper.UserMapper;
import com.rauio.smartdangjian.server.user.pojo.entity.User;
import com.rauio.smartdangjian.utils.spec.UserType;

@ExtendWith(MockitoExtension.class)
class ResourceMetaAccessAspectTest {

    @Mock
    private ResourceMetaService resourceMetaService;

    @Mock
    private UserMapper userMapper;

    @InjectMocks
    private ResourceMetaAccessAspect aspect;

    // ==================== supports ====================

    @Test
    @DisplayName("supports 返回 true 支持 RESOURCE_META_ADMIN")
    void supportsTrue() {
        assertThat(aspect.supports(DataScopeResources.RESOURCE_META_ADMIN)).isTrue();
    }

    @Test
    @DisplayName("supports 返回 false 不支持其他资源")
    void supportsFalse() {
        assertThat(aspect.supports("OTHER")).isFalse();
    }

    // ==================== before - MANAGER bypass ====================

    @Test
    @DisplayName("before 管理员直接放行")
    void beforeManagerBypass() {
        DataScopeContext context = mockContext(UserType.MANAGER, DataScopeAction.CREATE, "", "");
        aspect.before(context);
    }

    // ==================== before - non-SCHOOL reject ====================

    @Test
    @DisplayName("before 学生无权管理资源")
    void beforeStudentNotAllowed() {
        DataScopeContext context = mockContext(UserType.STUDENT, DataScopeAction.CREATE, "", "");
        assertThatThrownBy(() -> aspect.before(context))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("无权管理资源");
    }

    // ==================== before - CREATE ====================

    @Test
    @DisplayName("before CREATE 学校管理员未绑定学校抛出异常")
    void beforeCreateNoUniversityId() {
        DataScopeContext context = mockContext(UserType.SCHOOL, "school-1", null, DataScopeAction.CREATE, "", "");
        assertThatThrownBy(() -> aspect.before(context))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("未绑定学校");
    }

    // ==================== helpers ====================

    private DataScopeContext mockContext(UserType userType, DataScopeAction action, String id, String query) {
        return mockContext(userType, "test-user", "uni-1", action, id, query);
    }

    private DataScopeContext mockContext(
            UserType userType, String userId, String universityId, DataScopeAction action, String id, String query) {
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
                return DataScopeResources.RESOURCE_META_ADMIN;
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
