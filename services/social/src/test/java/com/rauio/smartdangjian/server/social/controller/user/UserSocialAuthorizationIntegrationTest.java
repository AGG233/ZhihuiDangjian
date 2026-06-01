package com.rauio.smartdangjian.server.social.controller.user;

import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.lang.annotation.Annotation;
import java.lang.reflect.AnnotatedElement;
import java.util.LinkedHashMap;
import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringBootConfiguration;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.boot.autoconfigure.orm.jpa.HibernateJpaAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.rauio.smartdangjian.exception.GlobalExceptionHandler;
import com.rauio.smartdangjian.server.social.pojo.response.CommentResponse;
import com.rauio.smartdangjian.server.social.service.CommentService;
import com.rauio.smartdangjian.server.social.service.LikeService;
import com.rauio.smartdangjian.server.user.service.UserService;

import cn.dev33.satoken.annotation.SaCheckLogin;
import cn.dev33.satoken.annotation.SaCheckPermission;
import cn.dev33.satoken.annotation.SaCheckRole;
import cn.dev33.satoken.annotation.handler.SaAnnotationHandlerInterface;
import cn.dev33.satoken.exception.NotRoleException;
import cn.dev33.satoken.interceptor.SaInterceptor;
import cn.dev33.satoken.stp.StpUtil;
import cn.dev33.satoken.strategy.SaAnnotationStrategy;

@SpringBootTest(
        webEnvironment = SpringBootTest.WebEnvironment.MOCK,
        classes = UserSocialAuthorizationIntegrationTest.TestConfig.class)
@AutoConfigureMockMvc(addFilters = true)
@DisplayName("社交接口真实角色鉴权测试")
class UserSocialAuthorizationIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private CommentService commentService;

    @MockitoBean
    private LikeService likeService;

    @MockitoBean
    private UserService userService;

    @BeforeEach
    void resetMocks() {
        org.mockito.Mockito.reset(commentService, likeService, userService);
    }

    @Test
    @DisplayName("低权限角色访问社交接口返回 403")
    void forbiddenRoleReturns403() throws Exception {
        try (MockedStatic<StpUtil> stpUtil = mockStatic(StpUtil.class);
                AnnotationHandlerScope ignored = annotationHandlerScope(
                        allowingLoginHandler(), rejectingRoleHandler("STUDENT"), allowingPermissionHandler(null))) {
            stpUtil.when(StpUtil::checkLogin).thenAnswer(invocation -> null);

            mockMvc.perform(get("/api/social/course/1/comments"))
                    .andExpect(status().isForbidden())
                    .andExpect(jsonPath("$.code").value("403"));
        }
    }

    @Test
    @DisplayName("正确角色访问社交接口返回 200")
    void allowedRoleReturns200() throws Exception {
        Page<CommentResponse> page = new Page<>(1, 20);
        page.setRecords(java.util.List.of());
        when(commentService.getPage("course", 1L, null, 1, 20, "latest")).thenReturn(page);

        try (MockedStatic<StpUtil> stpUtil = mockStatic(StpUtil.class);
                AnnotationHandlerScope ignored = annotationHandlerScope(
                        allowingLoginHandler(), allowingRoleHandler("STUDENT"), allowingPermissionHandler(null))) {
            stpUtil.when(StpUtil::checkLogin).thenAnswer(invocation -> null);
            stpUtil.when(StpUtil::isLogin).thenReturn(true);

            mockMvc.perform(get("/api/social/course/1/comments"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value("200"));
        }
    }

    private static SaAnnotationHandlerInterface<SaCheckLogin> allowingLoginHandler() {
        return new SaAnnotationHandlerInterface<>() {
            @Override
            public Class<SaCheckLogin> getHandlerAnnotationClass() {
                return SaCheckLogin.class;
            }

            @Override
            public void checkMethod(SaCheckLogin at, AnnotatedElement element) {}
        };
    }

    private static SaAnnotationHandlerInterface<SaCheckRole> rejectingRoleHandler(String expectedRole) {
        return new SaAnnotationHandlerInterface<>() {
            @Override
            public Class<SaCheckRole> getHandlerAnnotationClass() {
                return SaCheckRole.class;
            }

            @Override
            public void checkMethod(SaCheckRole at, AnnotatedElement element) {
                org.assertj.core.api.Assertions.assertThat(at.value()).containsExactly(expectedRole);
                throw new NotRoleException(expectedRole);
            }
        };
    }

    private static SaAnnotationHandlerInterface<SaCheckRole> allowingRoleHandler(String expectedRole) {
        return new SaAnnotationHandlerInterface<>() {
            @Override
            public Class<SaCheckRole> getHandlerAnnotationClass() {
                return SaCheckRole.class;
            }

            @Override
            public void checkMethod(SaCheckRole at, AnnotatedElement element) {
                org.assertj.core.api.Assertions.assertThat(at.value()).containsExactly(expectedRole);
            }
        };
    }

    private static SaAnnotationHandlerInterface<SaCheckPermission> allowingPermissionHandler(
            String expectedPermission) {
        return new SaAnnotationHandlerInterface<>() {
            @Override
            public Class<SaCheckPermission> getHandlerAnnotationClass() {
                return SaCheckPermission.class;
            }

            @Override
            public void checkMethod(SaCheckPermission at, AnnotatedElement element) {}
        };
    }

    @SafeVarargs
    private static AnnotationHandlerScope annotationHandlerScope(
            SaAnnotationHandlerInterface<? extends Annotation>... handlers) {
        return new AnnotationHandlerScope(handlers);
    }

    private static final class AnnotationHandlerScope implements AutoCloseable {
        private final Map<Class<?>, SaAnnotationHandlerInterface<?>> originals = new LinkedHashMap<>();

        @SafeVarargs
        private AnnotationHandlerScope(SaAnnotationHandlerInterface<? extends Annotation>... handlers) {
            for (SaAnnotationHandlerInterface<? extends Annotation> handler : handlers) {
                Class<?> annotationClass = handler.getHandlerAnnotationClass();
                originals.put(annotationClass, SaAnnotationStrategy.instance.annotationHandlerMap.get(annotationClass));
                SaAnnotationStrategy.instance.annotationHandlerMap.put(annotationClass, handler);
            }
        }

        @Override
        public void close() {
            originals.forEach((annotationClass, handler) -> {
                if (handler == null) {
                    SaAnnotationStrategy.instance.annotationHandlerMap.remove(annotationClass);
                } else {
                    SaAnnotationStrategy.instance.annotationHandlerMap.put(annotationClass, handler);
                }
            });
        }
    }

    @SpringBootConfiguration
    @EnableWebMvc
    @EnableAutoConfiguration(
            exclude = {
                DataSourceAutoConfiguration.class,
                HibernateJpaAutoConfiguration.class,
                org.redisson.spring.starter.RedissonAutoConfigurationV2.class,
                org.springframework.boot.autoconfigure.cache.CacheAutoConfiguration.class,
                org.springframework.boot.autoconfigure.data.redis.RedisRepositoriesAutoConfiguration.class,
                org.springframework.boot.autoconfigure.data.redis.RedisReactiveAutoConfiguration.class,
                org.springframework.boot.autoconfigure.data.redis.RedisAutoConfiguration.class,
                cn.dev33.satoken.dao.SaTokenDaoForRedisTemplate.class,
                com.rauio.smartdangjian.config.RedisConfig.class,
                com.rauio.smartdangjian.config.TransactionConfig.class,
                com.rauio.smartdangjian.config.AsyncConfig.class
            })
    @Import(GlobalExceptionHandler.class)
    static class TestConfig implements WebMvcConfigurer {

        @Override
        public void addInterceptors(InterceptorRegistry registry) {
            registry.addInterceptor(new SaInterceptor(handle -> StpUtil.checkLogin()))
                    .addPathPatterns("/**");
        }

        @Bean
        UserSocialController userSocialController(
                CommentService commentService, LikeService likeService, UserService userService) {
            return new UserSocialController(commentService, likeService, userService);
        }
    }
}
