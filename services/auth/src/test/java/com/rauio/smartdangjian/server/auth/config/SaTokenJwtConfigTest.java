package com.rauio.smartdangjian.server.auth.config;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import cn.dev33.satoken.jwt.StpLogicJwtForSimple;

class SaTokenJwtConfigTest {

    private final SaTokenJwtConfig config = new SaTokenJwtConfig();

    @Test
    @DisplayName("getStpLogicJwt 注册 JWT 简单模式 StpLogic")
    void getStpLogicJwt() {
        assertThat(config.getStpLogicJwt()).isInstanceOf(StpLogicJwtForSimple.class);
    }
}
