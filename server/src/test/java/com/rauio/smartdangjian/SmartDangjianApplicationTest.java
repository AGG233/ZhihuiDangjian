package com.rauio.smartdangjian;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mockStatic;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

class SmartDangjianApplicationTest {

    @Test
    @DisplayName("主类包含 @SpringBootApplication 注解")
    void hasSpringBootApplicationAnnotation() {
        SpringBootApplication annotation = SmartDangjianApplication.class.getAnnotation(SpringBootApplication.class);
        assertThat(annotation).isNotNull();
    }

    @Test
    @DisplayName("主类有 @MapperScan 注解扫描 com.rauio.smartdangjian")
    void hasMapperScanAnnotation() {
        org.mybatis.spring.annotation.MapperScan annotation =
                SmartDangjianApplication.class.getAnnotation(org.mybatis.spring.annotation.MapperScan.class);
        assertThat(annotation).isNotNull();
        assertThat(annotation.basePackages()).contains("com.rauio.smartdangjian");
    }

    @Test
    @DisplayName("@EnableFileStorage 注解存在")
    void hasEnableFileStorageAnnotation() {
        org.dromara.x.file.storage.spring.EnableFileStorage annotation =
                SmartDangjianApplication.class.getAnnotation(org.dromara.x.file.storage.spring.EnableFileStorage.class);
        assertThat(annotation).isNotNull();
    }

    @Test
    @DisplayName("main 方法存在并可反射调用（不启动完整 Spring 上下文）")
    void mainMethodExists() throws Exception {
        var mainMethod = SmartDangjianApplication.class.getDeclaredMethod("main", String[].class);
        assertThat(mainMethod).isNotNull();
        assertThat(java.lang.reflect.Modifier.isPublic(mainMethod.getModifiers()))
                .isTrue();
        assertThat(java.lang.reflect.Modifier.isStatic(mainMethod.getModifiers()))
                .isTrue();
    }

    @Test
    @DisplayName("main 方法可执行（不启动 Spring 上下文）")
    void mainMethodInvocation() {
        try (MockedStatic<SpringApplication> springAppMock = mockStatic(SpringApplication.class)) {
            springAppMock
                    .when(() -> SpringApplication.run(any(Class.class), any(String[].class)))
                    .thenReturn(null);

            SmartDangjianApplication.main(new String[0]);

            springAppMock.verify(() -> SpringApplication.run(SmartDangjianApplication.class, new String[0]));
        }
    }
}
