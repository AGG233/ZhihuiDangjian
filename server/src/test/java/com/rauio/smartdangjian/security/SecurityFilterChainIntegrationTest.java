package com.rauio.smartdangjian.security;

import com.rauio.smartdangjian.exception.BusinessException;
import com.rauio.smartdangjian.exception.GlobalExceptionHandler;
import com.rauio.smartdangjian.config.SecurityCoreAutoConfiguration;
import com.rauio.smartdangjian.config.SecuritySupportAutoConfiguration;
import com.rauio.smartdangjian.pojo.response.Result;
import com.rauio.smartdangjian.server.auth.config.SecurityConfig;
import com.rauio.smartdangjian.server.auth.constants.AuthErrorConstants;
import com.rauio.smartdangjian.server.auth.service.JwtService;
import com.rauio.smartdangjian.server.user.pojo.entity.User;
import com.rauio.smartdangjian.utils.spec.UserType;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.boot.SpringBootConfiguration;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;
import org.springframework.boot.autoconfigure.security.servlet.UserDetailsServiceAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.beans.factory.annotation.Autowired;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(
        webEnvironment = SpringBootTest.WebEnvironment.MOCK,
        classes = SecurityFilterChainIntegrationTest.TestConfig.class
)
@AutoConfigureMockMvc
@TestPropertySource(properties = {
        "app.security.enabled=true",
        "REDIS_HOST=localhost",
        "REDIS_PORT=6379",
        "REDIS_DATABASE=0",
        "DATABASE_URL=jdbc:h2:mem:security-filter-chain;DB_CLOSE_DELAY=-1",
        "DATABASE_USERNAME=sa",
        "DATABASE_PASSWORD="
})
@DisplayName("真实 Security Filter Chain 集成测试")
class SecurityFilterChainIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private JwtService jwtService;

    @Nested
    @DisplayName("公开接口")
    class PublicEndpointTests {

        @Test
        @DisplayName("permitAll 接口无需 token")
        void publicEndpointAllowsAnonymousAccess() throws Exception {
            mockMvc.perform(get("/auth/captcha"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value("200"))
                    .andExpect(jsonPath("$.data").value("captcha-ok"));
        }
    }

    @Nested
    @DisplayName("受保护接口")
    class ProtectedEndpointTests {

        @Test
        @DisplayName("缺少 token 被 Spring Security 拒绝")
        void protectedEndpointRejectsMissingToken() throws Exception {
            mockMvc.perform(get("/api/security-test/protected"))
                    .andExpect(status().isForbidden());
        }

        @Test
        @DisplayName("伪造 token 被 JwtAuthenticationFilter 拒绝")
        void protectedEndpointRejectsInvalidToken() throws Exception {
            when(jwtService.validateToken("forged-token"))
                    .thenThrow(new BusinessException(
                            AuthErrorConstants.TOKEN_VERIFICATION_FAILED,
                            "身份验证失败，请重新登录"
                    ));

            mockMvc.perform(get("/api/security-test/protected")
                            .header("Authorization", "Bearer forged-token"))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.code").value(String.valueOf(AuthErrorConstants.TOKEN_VERIFICATION_FAILED)));
        }

        @Test
        @DisplayName("黑名单 token 被 JwtAuthenticationFilter 拒绝")
        void protectedEndpointRejectsBlacklistedToken() throws Exception {
            when(jwtService.validateToken("blacklisted-token"))
                    .thenThrow(new BusinessException(
                            AuthErrorConstants.TOKEN_VERIFICATION_FAILED,
                            "令牌已失效，请重新登录"
                    ));

            mockMvc.perform(get("/api/security-test/protected")
                            .header("Authorization", "Bearer blacklisted-token"))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.code").value(String.valueOf(AuthErrorConstants.TOKEN_VERIFICATION_FAILED)))
                    .andExpect(jsonPath("$.message").value("令牌已失效，请重新登录"));
        }

        @Test
        @DisplayName("有效 token 可进入控制器并注入认证对象")
        void protectedEndpointAcceptsValidToken() throws Exception {
            when(jwtService.validateToken("valid-token")).thenReturn(testUser());

            mockMvc.perform(get("/api/security-test/protected")
                            .header("Authorization", "Bearer valid-token"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value("200"))
                    .andExpect(jsonPath("$.data").value("protected-ok"));
        }
    }

    private static User testUser() {
        return User.builder()
                .id("security-user-1")
                .username("security-user")
                .userType(UserType.STUDENT)
                .universityId("security-university-1")
                .build();
    }

    @SpringBootConfiguration
    @EnableAutoConfiguration(exclude = {
            SecurityAutoConfiguration.class,
            UserDetailsServiceAutoConfiguration.class,
            SecurityCoreAutoConfiguration.class,
            SecuritySupportAutoConfiguration.class
    })
    @EnableMethodSecurity
    @Import({
            SecurityConfig.class,
            GlobalExceptionHandler.class,
            TestController.class
    })
    static class TestConfig {

        @Bean
        SecurityTestAuthController securityTestAuthController() {
            return new SecurityTestAuthController();
        }
    }

    @RestController
    @RequestMapping("/api/security-test")
    static class TestController {

        @GetMapping("/protected")
        Result<String> protectedEndpoint() {
            return Result.ok("protected-ok");
        }
    }

    @RestController
    @RequestMapping("/auth")
    static class SecurityTestAuthController {

        @GetMapping(value = "/captcha", produces = MediaType.APPLICATION_JSON_VALUE)
        Result<String> captcha() {
            return Result.ok("captcha-ok");
        }
    }
}
