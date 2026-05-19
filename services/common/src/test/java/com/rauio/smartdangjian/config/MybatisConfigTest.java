package com.rauio.smartdangjian.config;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.baomidou.mybatisplus.extension.plugins.MybatisPlusInterceptor;

class MybatisConfigTest {

    private final MybatisConfig config = new MybatisConfig();

    @Test
    @DisplayName("mybatisPlusInterceptor 创建分页拦截器")
    void mybatisPlusInterceptor() {
        MybatisPlusInterceptor interceptor = config.mybatisPlusInterceptor();

        assertThat(interceptor).isNotNull();
    }
}
