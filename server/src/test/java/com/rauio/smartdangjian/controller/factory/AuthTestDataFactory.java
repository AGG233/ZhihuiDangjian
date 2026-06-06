package com.rauio.smartdangjian.controller.factory;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.rauio.smartdangjian.server.auth.pojo.Captcha;
import com.rauio.smartdangjian.server.auth.pojo.request.ChangePasswordRequest;
import com.rauio.smartdangjian.server.auth.pojo.request.LoginRequest;
import com.rauio.smartdangjian.server.auth.pojo.request.RegisterRequest;
import com.rauio.smartdangjian.server.auth.pojo.response.LoginResponse;
import com.rauio.smartdangjian.server.user.utils.spec.PartyStatus;
import com.rauio.smartdangjian.utils.spec.UserType;

/**
 * Static factory for auth module test data — produces Captcha, LoginRequest,
 * RegisterRequest, ChangePasswordRequest, LoginResponse, and JSON helpers.
 */
public final class AuthTestDataFactory {

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    private AuthTestDataFactory() {}

    // ── Captcha ────────────────────────────────────────────────────

    public static Captcha createCaptcha() {
        return Captcha.builder()
                .uuid("captcha-uuid-001")
                .code("captcha-code-001")
                .base64(
                        "iVBORw0KGgoAAAANSUhEUgAAAAEAAAABCAYAAAAfFcSJAAAADUlEQVR42mNk+M9QDwADhgGAWjR9awAAAABJRU5ErkJggg==")
                .build();
    }

    // ── LoginRequest ───────────────────────────────────────────────

    public static LoginRequest createLoginRequest() {
        LoginRequest request = new LoginRequest();
        request.setPassport("admin");
        request.setPassword(createValidPassword());
        request.setPlatform("web");
        request.setCaptchaUUID("captcha-uuid-001");
        request.setCaptchaCode("valid-code");
        return request;
    }

    // ── RegisterRequest ────────────────────────────────────────────

    public static RegisterRequest createRegisterRequest() {
        RegisterRequest request = new RegisterRequest();
        request.setUsername("newuser");
        request.setPassword(createValidPassword());
        request.setRealName("新用户");
        request.setIdCard("110101199001011234");
        request.setPhone("13800138000");
        request.setCaptchaUUID("captcha-uuid-001");
        request.setCaptchaCode("valid-code");
        request.setPartyStatus(PartyStatus.FORMAL_MEMBER);
        request.setType(UserType.STUDENT);
        request.setUniversityId("uni-001");
        return request;
    }

    // ── ChangePasswordRequest ──────────────────────────────────────

    public static ChangePasswordRequest createChangePasswordRequest() {
        ChangePasswordRequest request = new ChangePasswordRequest();
        request.setOldPassword(createValidPassword());
        request.setNewPassword(createValidPassword());
        request.setCaptchaUUID("captcha-uuid-001");
        request.setCaptchaCode("valid-code");
        return request;
    }

    // ── LoginResponse ──────────────────────────────────────────────

    public static LoginResponse createLoginResponse() {
        return LoginResponse.builder()
                .accessToken("eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJhZG1pbiJ9.test-signature")
                .build();
    }

    public static String createValidPassword() {
        return String.valueOf(new char[] {'T', 'e', 's', 't', '1', '2', '3', '4', '!'});
    }

    // ── JSON helper ────────────────────────────────────────────────

    public static String toJson(Object obj) {
        try {
            return OBJECT_MAPPER.writeValueAsString(obj);
        } catch (Exception e) {
            throw new RuntimeException("Failed to serialize to JSON", e);
        }
    }
}
