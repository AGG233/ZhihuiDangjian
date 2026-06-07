package com.rauio.smartdangjian.crosslayer.auth;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
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
import org.springframework.http.MediaType;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;

import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.rauio.smartdangjian.exception.GlobalExceptionHandler;
import com.rauio.smartdangjian.security.SessionUserPrincipal;
import com.rauio.smartdangjian.server.auth.constants.AuthErrorConstants;
import com.rauio.smartdangjian.server.auth.controller.AuthController;
import com.rauio.smartdangjian.server.auth.pojo.request.LoginRequest;
import com.rauio.smartdangjian.server.auth.pojo.request.RegisterRequest;
import com.rauio.smartdangjian.server.auth.security.SessionPrincipalFactory;
import com.rauio.smartdangjian.server.auth.service.AuthService;
import com.rauio.smartdangjian.server.auth.service.CaptchaService;
import com.rauio.smartdangjian.server.user.mapper.UserMapper;
import com.rauio.smartdangjian.server.user.pojo.entity.User;
import com.rauio.smartdangjian.server.user.service.UserService;
import com.rauio.smartdangjian.server.user.utils.spec.AccountStatus;
import com.rauio.smartdangjian.server.user.utils.spec.PartyStatus;
import com.rauio.smartdangjian.utils.spec.UserType;

import cn.dev33.satoken.session.SaSession;
import cn.dev33.satoken.stp.StpUtil;
import cn.dev33.satoken.stp.parameter.SaLoginParameter;
import cn.hutool.crypto.digest.BCrypt;

@SpringBootTest(
        webEnvironment = SpringBootTest.WebEnvironment.MOCK,
        classes = AuthControllerRealServiceIntegrationTest.TestConfig.class)
@AutoConfigureMockMvc(addFilters = false)
@TestPropertySource(
        locations = "classpath:application-test.yaml",
        properties = {"REDIS_HOST=localhost", "REDIS_PORT=6379", "REDIS_DATABASE=0"})
@DisplayName("认证控制层真实服务集成测试")
class AuthControllerRealServiceIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private CaptchaService captchaService;

    @MockitoBean
    private UserMapper userMapper;

    @MockitoBean
    private UserService userService;

    @BeforeEach
    void resetMocks() {
        reset(captchaService, userMapper, userService);
    }

    @Test
    @DisplayName("POST /auth/login 使用真实 AuthService 完成登录成功链路")
    void loginUsesRealAuthServiceAndReturnsToken() throws Exception {
        LoginRequest request = new LoginRequest("admin", strongPassword(), "web", "captcha-uuid", "1234");
        User user = User.builder()
                .id(1L)
                .username("admin")
                .password("encoded-password")
                .status(AccountStatus.ACTIVE)
                .build();
        SaSession session = org.mockito.Mockito.mock(SaSession.class);

        when(captchaService.validate("captcha-uuid", "1234")).thenReturn(true);
        when(userService.getByPassport("admin")).thenReturn(user);

        try (MockedStatic<BCrypt> bcrypt = mockStatic(BCrypt.class);
                MockedStatic<StpUtil> stpUtil = mockStatic(StpUtil.class)) {
            bcrypt.when(() -> BCrypt.checkpw(strongPassword(), "encoded-password"))
                    .thenReturn(true);
            stpUtil.when(StpUtil::getSession).thenReturn(session);
            stpUtil.when(StpUtil::getTokenValue).thenReturn("sa-token-real-service");

            mockMvc.perform(post("/api/auth/login")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value("200"))
                    .andExpect(jsonPath("$.data.accessToken").value("sa-token-real-service"));

            stpUtil.verify(() -> StpUtil.login(org.mockito.ArgumentMatchers.eq(1L), any(SaLoginParameter.class)));
            ArgumentCaptor<Object> userCaptor = ArgumentCaptor.forClass(Object.class);
            verify(session).set(org.mockito.ArgumentMatchers.eq("user"), userCaptor.capture());
            assertThat(userCaptor.getValue()).isInstanceOf(SessionUserPrincipal.class);
            SessionUserPrincipal principal = (SessionUserPrincipal) userCaptor.getValue();
            assertThat(principal.getId()).isEqualTo(user.getId());
            assertThat(principal.getUserType()).isEqualTo(user.getUserType());
        }
    }

    @Test
    @DisplayName("POST /auth/login 字段校验失败时不进入真实 AuthService 依赖")
    void loginValidationFailureStopsBeforeServiceDependencies() throws Exception {
        LoginRequest request = new LoginRequest("", strongPassword(), "web", "captcha-uuid", "1234");

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("400"))
                .andExpect(jsonPath("$.message").value("请填写用户名/手机号码/身份证号码"));

        verify(captchaService, org.mockito.Mockito.never()).validate(any(), any());
        verify(userMapper, org.mockito.Mockito.never()).selectOne(any(Wrapper.class));
    }

    @Test
    @DisplayName("POST /auth/login 验证码错误由真实 AuthService 映射为业务异常")
    void loginCaptchaErrorComesFromRealAuthService() throws Exception {
        LoginRequest request = new LoginRequest("admin", strongPassword(), "web", "captcha-uuid", "wrong");
        when(captchaService.validate("captcha-uuid", "wrong")).thenReturn(false);

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value(String.valueOf(AuthErrorConstants.CAPTCHA_ERROR)))
                .andExpect(jsonPath("$.message").value("验证码错误"));

        verify(userMapper, org.mockito.Mockito.never()).selectOne(any(Wrapper.class));
    }

    @Test
    @DisplayName("POST /auth/register 使用真实 AuthService 完成注册成功链路")
    void registerUsesRealAuthServiceAndPersistsUser() throws Exception {
        RegisterRequest request = validRegisterRequest();
        when(captchaService.validate("captcha-uuid", "1234")).thenReturn(true);
        when(userMapper.exists(any(Wrapper.class))).thenReturn(false);
        when(userMapper.insert(any(User.class))).thenReturn(1);

        try (MockedStatic<BCrypt> bcrypt = mockStatic(BCrypt.class)) {
            bcrypt.when(() -> BCrypt.hashpw(strongPassword())).thenReturn("encoded-new-password");

            mockMvc.perform(post("/api/auth/register")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value("200"))
                    .andExpect(jsonPath("$.data").value("注册成功！"));

            ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
            verify(userMapper).insert(userCaptor.capture());
            assertThat(userCaptor.getValue().getUsername()).isEqualTo("new-user");
            assertThat(userCaptor.getValue().getPassword()).isEqualTo("encoded-new-password");
            assertThat(userCaptor.getValue().getStatus()).isEqualTo(AccountStatus.ACTIVE);
        }
    }

    @Test
    @DisplayName("POST /auth/register 字段约束失败时不查询重复用户")
    void registerValidationFailureStopsBeforeDuplicateChecks() throws Exception {
        RegisterRequest request = validRegisterRequest();
        request.setPhone("12345678901");

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("400"))
                .andExpect(jsonPath("$.message").value("手机号格式错误"));

        verify(captchaService, org.mockito.Mockito.never()).validate(any(), any());
        verify(userMapper, org.mockito.Mockito.never()).exists(any(Wrapper.class));
    }

    private static RegisterRequest validRegisterRequest() {
        RegisterRequest request = new RegisterRequest();
        request.setType(UserType.STUDENT);
        request.setUsername("new-user");
        request.setPassword(strongPassword());
        request.setRealName("张三");
        request.setIdCard("11010119900307123X");
        request.setPartyStatus(PartyStatus.FORMAL_MEMBER);
        request.setBranchName("第一党支部");
        request.setEmail("new-user@example.com");
        request.setPhone("13800138000");
        request.setCaptchaUUID("captcha-uuid");
        request.setCaptchaCode("1234");
        request.setUniversityId("uni-1");
        return request;
    }

    private static String strongPassword() {
        return "Aa1!" + "aaaa";
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
                org.springframework.boot.autoconfigure.neo4j.Neo4jAutoConfiguration.class,
                org.springframework.boot.autoconfigure.data.redis.RedisReactiveAutoConfiguration.class,
                org.springframework.boot.autoconfigure.data.neo4j.Neo4jDataAutoConfiguration.class,
                org.springframework.boot.autoconfigure.data.redis.RedisAutoConfiguration.class,
                cn.dev33.satoken.dao.SaTokenDaoForRedisTemplate.class,
                com.rauio.smartdangjian.config.RedisConfig.class,
                com.rauio.smartdangjian.config.TransactionConfig.class,
                com.rauio.smartdangjian.config.SensitiveWordConfig.class
            })
    @Import({AuthController.class, AuthService.class, SessionPrincipalFactory.class, GlobalExceptionHandler.class})
    static class TestConfig {

        @Bean
        ObjectMapper objectMapper() {
            return new ObjectMapper();
        }
    }
}
