package com.rauio.smartdangjian;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;

import java.util.List;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.mockito.MockedStatic;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.boot.autoconfigure.orm.jpa.HibernateJpaAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.xml.MappingJackson2XmlHttpMessageConverter;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

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
            // 测试环境禁用向量库与 embedding 选择，避免 dashscope/openai 双 EmbeddingModel 冲突及真实 API 调用
            "spring.ai.model.embedding=dashscope",
            "spring.ai.vectorstore.type=none"
        })
public abstract class BaseControllerTest {

    @Autowired
    protected MockMvc mockMvc;

    private MockedStatic<StpUtil> stpUtilMock;

    @BeforeEach
    void defaultSecurityContext() {
        stpUtilMock = mockStatic(StpUtil.class);
        setSecurityContext(UserType.SCHOOL, 1L, "uni1");
    }

    @AfterEach
    void clearSecurityContext() {
        if (stpUtilMock != null) {
            stpUtilMock.close();
        }
    }

    protected void setSecurityContext(UserType userType, Long userId, String universityId) {
        if (stpUtilMock == null) {
            stpUtilMock = mockStatic(StpUtil.class);
        }
        stpUtilMock.when(StpUtil::isLogin).thenReturn(true);
        stpUtilMock.when(StpUtil::getLoginIdAsString).thenReturn(String.valueOf(userId));
        SaSession session = mock(SaSession.class);
        stpUtilMock.when(StpUtil::getSession).thenReturn(session);
    }

    protected void setAnonymousContext() {
        if (stpUtilMock != null) {
            stpUtilMock.close();
        }
        stpUtilMock = mockStatic(StpUtil.class);
        stpUtilMock.when(StpUtil::isLogin).thenReturn(false);
    }

    @EnableWebMvc
    @EnableAutoConfiguration(
            exclude = {
                DataSourceAutoConfiguration.class,
                HibernateJpaAutoConfiguration.class,
                com.rauio.smartdangjian.config.TransactionConfig.class
            })
    protected static class CommonTestConfig implements WebMvcConfigurer {

        @Override
        public void extendMessageConverters(List<HttpMessageConverter<?>> converters) {
            // elearning-module-parser 传递引入 jackson-dataformat-xml，移除 XML converter 保证默认 JSON 响应
            converters.removeIf(converter -> converter instanceof MappingJackson2XmlHttpMessageConverter);
        }
    }
}
