package com.rauio.smartdangjian;

import java.util.Collections;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.boot.autoconfigure.orm.jpa.HibernateJpaAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;

import com.rauio.smartdangjian.security.CurrentUserPrincipal;
import com.rauio.smartdangjian.utils.spec.UserType;

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
            "NEO4J_PASSWORD=password"
        })
public abstract class BaseControllerTest {

    @Autowired
    protected MockMvc mockMvc;

    @BeforeEach
    void defaultSecurityContext() {
        setSecurityContext(UserType.SCHOOL, "admin1", "uni1");
    }

    @AfterEach
    void clearSecurityContext() {
        SecurityContextHolder.clearContext();
    }

    protected void setSecurityContext(UserType userType, String userId, String universityId) {
        CurrentUserPrincipal principal = new CurrentUserPrincipal() {
            @Override
            public String getId() {
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
        SecurityContextHolder.getContext()
                .setAuthentication(new UsernamePasswordAuthenticationToken(principal, null, Collections.emptyList()));
    }

    @EnableWebMvc
    @EnableAutoConfiguration(
            exclude = {
                DataSourceAutoConfiguration.class,
                HibernateJpaAutoConfiguration.class,
                org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration.class,
                org.springframework.boot.autoconfigure.security.servlet.UserDetailsServiceAutoConfiguration.class,
                com.rauio.smartdangjian.config.SecurityCoreAutoConfiguration.class,
                com.rauio.smartdangjian.config.SecuritySupportAutoConfiguration.class,
                com.rauio.smartdangjian.config.TransactionConfig.class
            })
    protected static class CommonTestConfig {}
}
