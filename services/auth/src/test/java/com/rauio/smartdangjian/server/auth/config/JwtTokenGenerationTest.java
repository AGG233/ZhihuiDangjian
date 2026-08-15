package com.rauio.smartdangjian.server.auth.config;

import static org.assertj.core.api.Assertions.assertThat;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Map;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import cn.dev33.satoken.SaManager;
import cn.dev33.satoken.context.mock.SaTokenContextMockUtil;
import cn.dev33.satoken.jwt.StpLogicJwtForSimple;
import cn.dev33.satoken.stp.StpLogic;

/**
 * 验证 sa-token-jwt「简单模式」签发 JWT 格式 token（三段式、可解码、可反解 loginId）。
 *
 * <p>不依赖 Spring 上下文与 Redis：使用官方 mock 上下文 + 默认内存会话存储，
 * 仅验证 JWT 插件的 token 生成行为与密钥配置路径。
 */
class JwtTokenGenerationTest {

    private static final String TEST_SECRET_KEY = "test-jwt-secret-key-0123456789abcdef";

    private final ObjectMapper objectMapper = new ObjectMapper();

    @BeforeEach
    void setUp() {
        SaTokenContextMockUtil.setMockContext();
    }

    @AfterEach
    void tearDown() {
        SaManager.getConfig().setJwtSecretKey(null);
    }

    @Test
    @DisplayName("login 后签发三段式 HS256 JWT token，header 与 payload 均可解析且含 loginId")
    void loginIssuesJwtToken() throws Exception {
        SaManager.getConfig().setJwtSecretKey(TEST_SECRET_KEY);
        StpLogic stpLogic = new StpLogicJwtForSimple();
        stpLogic.login(10001L);

        String token = stpLogic.getTokenValue();
        String[] parts = token.split("\\.");
        assertThat(parts).hasSize(3);

        Map<String, Object> header = decodeJson(parts[0]);
        assertThat(header).containsEntry("typ", "JWT").containsEntry("alg", "HS256");

        Map<String, Object> payload = decodeJson(parts[1]);
        assertThat(String.valueOf(payload.get("loginId"))).isEqualTo("10001");
    }

    @Test
    @DisplayName("JWT 校验通过后可反解登录用户")
    void tokenParsesBackToLoginId() {
        SaManager.getConfig().setJwtSecretKey(TEST_SECRET_KEY);
        StpLogic stpLogic = new StpLogicJwtForSimple();
        stpLogic.login(10002L);

        String token = stpLogic.getTokenValue();
        Object loginId = stpLogic.getLoginIdByToken(token);
        assertThat(String.valueOf(loginId)).isEqualTo("10002");
    }

    private Map<String, Object> decodeJson(String segment) throws Exception {
        String json = new String(Base64.getUrlDecoder().decode(segment), StandardCharsets.UTF_8);
        return objectMapper.readValue(json, new TypeReference<Map<String, Object>>() {});
    }
}
