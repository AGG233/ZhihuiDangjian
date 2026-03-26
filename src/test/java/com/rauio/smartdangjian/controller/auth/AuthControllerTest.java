package com.rauio.smartdangjian.controller.auth;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.rauio.smartdangjian.exception.GlobalExceptionHandler;
import com.rauio.smartdangjian.pojo.Captcha;
import com.rauio.smartdangjian.pojo.request.LoginRequest;
import com.rauio.smartdangjian.pojo.response.LoginResponse;
import com.rauio.smartdangjian.service.auth.AuthService;
import com.rauio.smartdangjian.service.auth.CaptchaService;
import com.rauio.smartdangjian.user.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.http.MediaType;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class AuthControllerTest {

    private MockMvc mockMvc;

    private ObjectMapper objectMapper;

    @Mock
    private AuthService authService;

    @Mock
    private CaptchaService captchaService;

    @Mock
    private UserService userService;

    @InjectMocks
    private AuthController authController;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper().findAndRegisterModules();
        LocalValidatorFactoryBean validator = new LocalValidatorFactoryBean();
        validator.afterPropertiesSet();
        mockMvc = MockMvcBuilders.standaloneSetup(authController)
                .setControllerAdvice(new GlobalExceptionHandler())
                .setMessageConverters(new MappingJackson2HttpMessageConverter(objectMapper))
                .setValidator(validator)
                .build();
    }

    @Test
    void getCaptchaReturnsWrappedCaptcha() throws Exception {
        Captcha captcha = Captcha.builder()
                .uuid("captcha-uuid")
                .code("1234")
                .base64("base64-image")
                .build();
        given(captchaService.get()).willReturn(captcha);

        mockMvc.perform(get("/auth/captcha"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value("200"))
                .andExpect(jsonPath("$.message").value("OK"))
                .andExpect(jsonPath("$.data.uuid").value("captcha-uuid"))
                .andExpect(jsonPath("$.data.base64").value("base64-image"));
    }

    @Test
    void validateCaptchaReturnsServiceResult() throws Exception {
        given(captchaService.validate("captcha-uuid", "1234")).willReturn(true);

        mockMvc.perform(post("/auth/captcha")
                        .param("uuid", "captcha-uuid")
                        .param("code", "1234"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value("200"))
                .andExpect(jsonPath("$.data").value(true));
    }

    @Test
    void loginReturnsAccessToken() throws Exception {
        LoginRequest request = new LoginRequest("admin", "Password!1", "web", "captcha-uuid", "1234");
        LoginResponse response = LoginResponse.builder()
                .accessToken("access-token")
                .build();
        given(authService.login(any(LoginRequest.class))).willReturn(response);

        mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value("200"))
                .andExpect(jsonPath("$.data.accessToken").value("access-token"));

        then(authService).should().login(any(LoginRequest.class));
    }

    @Test
    void loginWithBlankPassportReturnsBadRequest() throws Exception {
        LoginRequest request = new LoginRequest("", "Password!1", "web", "captcha-uuid", "1234");

        mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("400"))
                .andExpect(jsonPath("$.message").value("请填写用户名/手机号码/身份证号码"));
    }

    @Test
    void changePasswordReturnsWrappedBoolean() throws Exception {
        given(userService.changePassword("old-pass", "new-pass")).willReturn(true);

        mockMvc.perform(post("/auth/changePassword")
                        .param("oldPassword", "old-pass")
                        .param("newPassword", "new-pass"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value("200"))
                .andExpect(jsonPath("$.data").value(true));

        then(userService).should().changePassword(eq("old-pass"), eq("new-pass"));
    }
}
