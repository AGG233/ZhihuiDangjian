package com.rauio.smartdangjian;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * SmartDangjianApplication 的 Smoke Test。
 * 验证主配置类能被加载、annotation 正确、main 方法可调用。
 */
class SmartDangjianApplicationTest {

    @Test
    @DisplayName("主类包含 @SpringBootApplication 注解")
    void hasSpringBootApplicationAnnotation() {
        SpringBootApplication annotation = SmartDangjianApplication.class
                .getAnnotation(SpringBootApplication.class);
        assertThat(annotation).isNotNull();
    }

    @Test
    @DisplayName("主类有 @MapperScan 注解扫描 com.rauio.smartdangjian")
    void hasMapperScanAnnotation() {
        org.mybatis.spring.annotation.MapperScan annotation = SmartDangjianApplication.class
                .getAnnotation(org.mybatis.spring.annotation.MapperScan.class);
        assertThat(annotation).isNotNull();
        assertThat(annotation.basePackages()).contains("com.rauio.smartdangjian");
    }

    @Test
    @DisplayName("@EnableFileStorage 注解存在")
    void hasEnableFileStorageAnnotation() {
        org.dromara.x.file.storage.spring.EnableFileStorage annotation = SmartDangjianApplication.class
                .getAnnotation(org.dromara.x.file.storage.spring.EnableFileStorage.class);
        assertThat(annotation).isNotNull();
    }

    @Test
    @DisplayName("main 方法存在并可反射调用")
    void mainMethodExists() throws Exception {
        var mainMethod = SmartDangjianApplication.class.getDeclaredMethod("main", String[].class);
        assertThat(mainMethod).isNotNull();
        assertThat(java.lang.reflect.Modifier.isPublic(mainMethod.getModifiers())).isTrue();
        assertThat(java.lang.reflect.Modifier.isStatic(mainMethod.getModifiers())).isTrue();
    }
}
