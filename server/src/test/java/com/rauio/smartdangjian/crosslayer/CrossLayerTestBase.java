package com.rauio.smartdangjian.crosslayer;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.mockito.MockedStatic;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.boot.autoconfigure.orm.jpa.HibernateJpaAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;

import com.rauio.smartdangjian.security.CurrentUserPrincipal;
import com.rauio.smartdangjian.utils.spec.UserType;

import cn.dev33.satoken.session.SaSession;
import cn.dev33.satoken.stp.StpUtil;

@AutoConfigureMockMvc(addFilters = false)
@TestPropertySource(
        locations = "classpath:application-test.yaml",
        properties = {
            "REDIS_HOST=localhost",
            "REDIS_PORT=6379",
            "REDIS_DATABASE=0",
            "DATABASE_URL=jdbc:h2:mem:testdb;DB_CLOSE_DELAY=-1;DB_CLOSE_ON_EXIT=FALSE",
            "DATABASE_USERNAME=sa",
            "DATABASE_PASSWORD=",
            "NEO4J_URI=bolt://localhost:7687",
            "NEO4J_USERNAME=neo4j",
            "NEO4J_PASSWORD=password",
            "app.security.enabled=false"
        })
public abstract class CrossLayerTestBase {

    @Autowired
    protected MockMvc mockMvc;

    private MockedStatic<StpUtil> stpUtilMock;

    @BeforeEach
    void clearSecurityContext() {
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

    protected void setSecurityContext(UserType userType, String userId, String universityId) {
        closeMock();
        stpUtilMock = mockStatic(StpUtil.class);
        stpUtilMock.when(StpUtil::isLogin).thenReturn(true);
        stpUtilMock.when(StpUtil::getLoginIdAsString).thenReturn(userId);
        CurrentUserPrincipal principal = new CurrentUserPrincipal() {
            @Override public String getId() { return userId; }
            @Override public UserType getUserType() { return userType; }
            @Override public String getUniversityId() { return universityId; }
        };
        SaSession session = mock(SaSession.class);
        when(session.get("user")).thenReturn(principal);
        stpUtilMock.when(StpUtil::getSession).thenReturn(session);
    }

    protected void setStudentContext(String userId, String universityId) {
        setSecurityContext(UserType.STUDENT, userId, universityId);
    }

    protected void setSchoolContext(String userId, String universityId) {
        setSecurityContext(UserType.SCHOOL, userId, universityId);
    }

    protected void setManagerContext(String userId, String universityId) {
        setSecurityContext(UserType.MANAGER, userId, universityId);
    }

    protected void setAnonymousContext() {
        closeMock();
    }

    @EnableWebMvc
    @EnableAutoConfiguration(
            exclude = {
                DataSourceAutoConfiguration.class,
                HibernateJpaAutoConfiguration.class,
                com.rauio.smartdangjian.config.TransactionConfig.class
            })
    protected static class CrossLayerTestConfig {}
}
