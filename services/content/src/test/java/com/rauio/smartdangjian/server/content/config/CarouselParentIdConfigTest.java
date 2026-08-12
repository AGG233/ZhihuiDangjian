package com.rauio.smartdangjian.server.content.config;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@DisplayName("轮播图父章节 ID 配置绑定测试")
class CarouselParentIdConfigTest {

    /** 与两个 Controller 相同的绑定方式：@Value + 默认值兜底 */
    private final ApplicationContextRunner contextRunner =
            new ApplicationContextRunner().withUserConfiguration(BindingConfig.class);

    @Configuration
    static class BindingConfig {

        @Value("${app.content.carousel.chapter-id:1145141919810}")
        private Long carouselParentId;

        @Bean
        Long carouselParentId() {
            return carouselParentId;
        }
    }

    @Test
    @DisplayName("未配置时回退默认值（兼容既有数据）")
    void fallbackToDefaultWhenAbsent() {
        contextRunner.run(context -> {
            assertThat(context).hasNotFailed();
            assertThat(context.getBean("carouselParentId", Long.class)).isEqualTo(Long.parseLong("1145141919810"));
        });
    }

    @Test
    @DisplayName("配置为合法数字时使用配置值")
    void useConfiguredValue() {
        contextRunner.withPropertyValues("app.content.carousel.chapter-id=100").run(context -> {
            assertThat(context).hasNotFailed();
            assertThat(context.getBean("carouselParentId", Long.class)).isEqualTo(100L);
        });
    }

    @Test
    @DisplayName("配置为非法值（非数字）时启动失败")
    void invalidValueFailsStartup() {
        contextRunner
                .withPropertyValues("app.content.carousel.chapter-id=not-a-number")
                .run(context -> assertThat(context).hasFailed());
    }
}
