package com.rauio.smartdangjian.aop;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

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
import com.rauio.smartdangjian.server.content.aop.ArticleAdminAccessAspect;
import com.rauio.smartdangjian.server.content.mapper.ArticleMapper;
import com.rauio.smartdangjian.server.content.pojo.entity.Article;
import com.rauio.smartdangjian.server.user.mapper.UserMapper;
import com.rauio.smartdangjian.server.user.pojo.entity.User;
import com.rauio.smartdangjian.utils.spec.UserType;

@ExtendWith(MockitoExtension.class)
@DisplayName("ArticleAdminAccessAspect 单元测试")
class ArticleAdminAccessAspectTest {

    @Mock
    private ArticleMapper articleMapper;

    @Mock
    private UserMapper userMapper;

    @InjectMocks
    private ArticleAdminAccessAspect aspect;

    // ==================== supports ====================

    @Test
    @DisplayName("supports 返回 true 支持 ARTICLE_ADMIN")
    void supportsTrue() {
        assertThat(aspect.supports(DataScopeResources.ARTICLE_ADMIN)).isTrue();
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
        DataScopeContext context = mockContext(UserType.MANAGER, 1L, null, "'1'");
        aspect.before(context);
    }

    // ==================== before - user type checks ====================

    @Test
    @DisplayName("学生无权管理文章")
    void beforeStudentNotAllowed() {
        DataScopeContext context = mockContext(UserType.STUDENT, 1L, "uni1", "'1'");
        assertThatThrownBy(() -> aspect.before(context))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("无权管理文章");
    }

    @Test
    @DisplayName("学校管理员未绑定学校抛出异常")
    void beforeSchoolNoUniversityId() {
        DataScopeContext context = mockContext(UserType.SCHOOL, 1L, null, "'1'");
        assertThatThrownBy(() -> aspect.before(context))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("未绑定学校");
    }

    // ==================== before - articleId empty ====================

    @Test
    @DisplayName("文章ID为空抛出异常")
    void beforeArticleIdEmpty() {
        DataScopeContext context = mockContext(UserType.SCHOOL, 1L, "uni1", "");
        assertThatThrownBy(() -> aspect.before(context))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("文章ID不能为空");
    }

    // ==================== before - 同校校验 ====================

    @Test
    @DisplayName("文章不存在时抛出异常")
    void beforeArticleNotFound() {
        when(articleMapper.selectById("999")).thenReturn(null);

        DataScopeContext context = mockContext(UserType.SCHOOL, 1L, "uni1", "'999'");
        assertThatThrownBy(() -> aspect.before(context))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("文章不存在");
    }

    @Test
    @DisplayName("文章作者不存在时抛出异常")
    void beforeAuthorNotFound() {
        when(articleMapper.selectById("1"))
                .thenReturn(Article.builder().id(1L).authorId(1L).build());
        when(userMapper.selectById(1L)).thenReturn(null);

        DataScopeContext context = mockContext(UserType.SCHOOL, 1L, "uni1", "'1'");
        assertThatThrownBy(() -> aspect.before(context))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("无权管理本校外文章");
    }

    @Test
    @DisplayName("作者与当前用户不同校时抛出异常")
    void beforeAuthorDifferentUniversity() {
        when(articleMapper.selectById("1"))
                .thenReturn(Article.builder().id(1L).authorId(1L).build());
        when(userMapper.selectById(1L))
                .thenReturn(User.builder().id(1L).universityId("uni2").build());

        DataScopeContext context = mockContext(UserType.SCHOOL, 1L, "uni1", "'1'");
        assertThatThrownBy(() -> aspect.before(context))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("无权管理本校外文章");
    }

    @Test
    @DisplayName("学校管理员管理本校作者文章通过")
    void beforeSameUniversityPasses() {
        when(articleMapper.selectById("1"))
                .thenReturn(Article.builder().id(1L).authorId(1L).build());
        when(userMapper.selectById(1L))
                .thenReturn(User.builder().id(1L).universityId("uni1").build());

        DataScopeContext context = mockContext(UserType.SCHOOL, 1L, "uni1", "'1'");
        assertThatCode(() -> aspect.before(context)).doesNotThrowAnyException();
    }

    // ==================== helpers ====================

    private DataScopeContext mockContext(UserType userType, Long userId, String universityId, String id) {
        User user = User.builder()
                .id(userId)
                .userType(userType)
                .universityId(universityId)
                .build();
        DataScopeAccess access = createAccess(id);

        ProceedingJoinPoint jp = mock(ProceedingJoinPoint.class);
        MethodSignature sig = mock(MethodSignature.class);
        lenient().when(sig.getMethod()).thenReturn(mock(java.lang.reflect.Method.class));
        lenient().when(sig.getParameterNames()).thenReturn(new String[0]);
        lenient().when(jp.getSignature()).thenReturn(sig);
        lenient().when(jp.getArgs()).thenReturn(new Object[0]);

        return new DataScopeContext(jp, access, user);
    }

    private DataScopeAccess createAccess(String id) {
        return new DataScopeAccess() {
            @Override
            public String resource() {
                return DataScopeResources.ARTICLE_ADMIN;
            }

            @Override
            public DataScopeAction action() {
                return DataScopeAction.UPDATE;
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
                return "";
            }

            @Override
            public Class<? extends java.lang.annotation.Annotation> annotationType() {
                return DataScopeAccess.class;
            }
        };
    }
}
