package com.rauio.smartdangjian.server.auth.config;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.core.env.Environment;
import org.springframework.test.util.ReflectionTestUtils;

import cn.dev33.satoken.jwt.StpLogicJwtForSimple;

class SaTokenJwtConfigTest {

    private final Environment environment = mock(Environment.class);

    private final SaTokenJwtConfig config = new SaTokenJwtConfig(environment);

    @BeforeEach
    void setUp() {
        when(environment.getActiveProfiles()).thenReturn(new String[0]);
        when(environment.getDefaultProfiles()).thenReturn(new String[0]);
    }

    @Test
    @DisplayName("getStpLogicJwt 注册 JWT 简单模式 StpLogic")
    void getStpLogicJwt() {
        assertThat(config.getStpLogicJwt()).isInstanceOf(StpLogicJwtForSimple.class);
    }

    @Test
    @DisplayName("dev profile 下占位符密钥可通过校验")
    void validateJwtSecretKeyAllowsDevProfileWithPlaceholder() {
        setJwtSecretKey("CHANGE_ME_IN_PROD");
        when(environment.getActiveProfiles()).thenReturn(new String[] {"dev"});
        config.validateJwtSecretKey();
    }

    @Test
    @DisplayName("active profiles 为空但 default=dev 时（未显式传 --spring.profiles.active）放行占位符")
    void validateJwtSecretKeyAllowsPlaceholderWhenActiveEmptyButDefaultDev() {
        setJwtSecretKey("CHANGE_ME_IN_PROD");
        when(environment.getActiveProfiles()).thenReturn(new String[0]);
        when(environment.getDefaultProfiles()).thenReturn(new String[] {"dev"});
        config.validateJwtSecretKey();
    }

    @Test
    @DisplayName("非 dev/test profile 使用占位符密钥时拒绝启动")
    void validateJwtSecretKeyRejectsPlaceholderInProd() {
        setJwtSecretKey("CHANGE_ME_IN_PROD");
        when(environment.getActiveProfiles()).thenReturn(new String[] {"prod"});
        assertThatThrownBy(config::validateJwtSecretKey).isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("非开发 profile 使用空密钥时拒绝启动")
    void validateJwtSecretKeyRejectsBlankKeyInProd() {
        setJwtSecretKey(" ");
        when(environment.getActiveProfiles()).thenReturn(new String[] {"prod"});
        assertThatThrownBy(config::validateJwtSecretKey).isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("非开发 profile 显式配置真实密钥时可通过校验")
    void validateJwtSecretKeyAllowsRealKeyInProd() {
        setJwtSecretKey("prod-real-secret-key");
        when(environment.getActiveProfiles()).thenReturn(new String[] {"prod"});
        config.validateJwtSecretKey();
    }

    @Test
    @DisplayName("精确切分 profile，devtools 不会误匹配为 dev")
    void doesNotMatchDevtoolsToDev() {
        setJwtSecretKey("CHANGE_ME_IN_PROD");
        when(environment.getActiveProfiles()).thenReturn(new String[] {"devtools"});
        assertThatThrownBy(config::validateJwtSecretKey).isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("逗号多 profile 中存在 test 时放行占位符")
    void allowsPlaceholderWhenCommaSeparatedProfilesContainTest() {
        setJwtSecretKey("CHANGE_ME_IN_PROD");
        when(environment.getActiveProfiles()).thenReturn(new String[] {"prod", "test"});
        config.validateJwtSecretKey();
    }

    private void setJwtSecretKey(String value) {
        ReflectionTestUtils.setField(config, "jwtSecretKey", value);
    }
}
