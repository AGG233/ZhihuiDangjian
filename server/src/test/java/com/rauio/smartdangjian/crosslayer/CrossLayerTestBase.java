package com.rauio.smartdangjian.crosslayer;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.when;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.boot.autoconfigure.orm.jpa.HibernateJpaAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;

import com.rauio.smartdangjian.security.CurrentUserPrincipal;
import com.rauio.smartdangjian.security.CurrentUserProvider;
import com.rauio.smartdangjian.security.LoginUser;
import com.rauio.smartdangjian.utils.spec.UserType;

import cn.dev33.satoken.session.SaSession;
import cn.dev33.satoken.stp.StpUtil;

@AutoConfigureMockMvc(addFilters = false)
@TestPropertySource(
        locations = "classpath:application-test.yaml",
        properties = {"REDIS_HOST=localhost", "REDIS_PORT=6379", "REDIS_DATABASE=0"})
public abstract class CrossLayerTestBase {

    @Autowired
    protected MockMvc mockMvc;

    private MockedStatic<StpUtil> stpUtilMock;

    @BeforeEach
    void clearSecurityContext() {
        resetMockitoBeans();
        closeMock();
    }

    @AfterEach
    void tearDown() {
        closeMock();
    }

    private void closeMock() {
        if (stpUtilMock != null) {
            stpUtilMock.close();
            stpUtilMock = null;
        }
    }

    protected void setSecurityContext(UserType userType, Long userId, String universityId) {
        closeMock();
        stpUtilMock = mockStatic(StpUtil.class);
        stpUtilMock.when(StpUtil::isLogin).thenReturn(true);
        stpUtilMock.when(StpUtil::getLoginIdAsString).thenReturn(String.valueOf(userId));
        CurrentUserPrincipal principal = new CurrentUserPrincipal() {
            @Override
            public Long getId() {
                return userId;
            }

            @Override
            public UserType getUserType() {
                return userType;
            }

            @Override
            public String getUniversityId() {
                return universityId;
            }
        };
        SaSession session = mock(SaSession.class);
        when(session.get("user")).thenReturn(principal);
        stpUtilMock.when(StpUtil::getSession).thenReturn(session);
    }

    protected void setStudentContext(Long userId, String universityId) {
        setSecurityContext(UserType.STUDENT, userId, universityId);
    }

    protected void setSchoolContext(Long userId, String universityId) {
        setSecurityContext(UserType.SCHOOL, userId, universityId);
    }

    protected void setManagerContext(Long userId, String universityId) {
        setSecurityContext(UserType.MANAGER, userId, universityId);
    }

    protected void setAnonymousContext() {
        closeMock();
    }

    private void resetMockitoBeans() {
        Class<?> current = getClass();
        while (current != null && current != CrossLayerTestBase.class) {
            for (Field field : current.getDeclaredFields()) {
                if (Modifier.isStatic(field.getModifiers()) || field.getAnnotation(MockitoBean.class) == null) {
                    continue;
                }
                field.setAccessible(true);
                try {
                    Object candidate = field.get(this);
                    if (candidate != null && Mockito.mockingDetails(candidate).isMock()) {
                        Mockito.reset(candidate);
                    }
                } catch (IllegalAccessException e) {
                    throw new IllegalStateException("Failed to reset @MockitoBean field: " + field.getName(), e);
                }
            }
            current = current.getSuperclass();
        }
    }

    @EnableWebMvc
    @EnableAutoConfiguration(
            exclude = {
                DataSourceAutoConfiguration.class,
                HibernateJpaAutoConfiguration.class,
                org.redisson.spring.starter.RedissonAutoConfigurationV2.class,
                org.springframework.boot.autoconfigure.cache.CacheAutoConfiguration.class,
                org.springframework.boot.autoconfigure.data.redis.RedisRepositoriesAutoConfiguration.class,
                org.springframework.boot.autoconfigure.neo4j.Neo4jAutoConfiguration.class,
                org.springframework.boot.autoconfigure.data.redis.RedisReactiveAutoConfiguration.class,
                org.springframework.boot.autoconfigure.data.neo4j.Neo4jDataAutoConfiguration.class,
                org.springframework.boot.autoconfigure.data.redis.RedisAutoConfiguration.class,
                cn.dev33.satoken.dao.SaTokenDaoForRedisTemplate.class,
                com.rauio.smartdangjian.config.RedisConfig.class,
                com.rauio.smartdangjian.config.TransactionConfig.class
            })
    protected static class CrossLayerTestConfig {
        @org.springframework.context.annotation.Bean
        CurrentUserProvider currentUserProvider() {
            return new CurrentUserProvider() {
                @Override
                public String getCurrentUserId() {
                    return StpUtil.getLoginIdAsString();
                }

                @Override
                public String getCurrentUserRole() {
                    return null;
                }

                @Override
                public boolean hasRole(String role) {
                    return StpUtil.hasRole(role);
                }

                @Override
                public LoginUser getCurrentUser() {
                    return null;
                }
            };
        }
    }
}
