package com.rauio.smartdangjian.aop.annotation;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class ResourceAccessAnnotationTest {

    @Test
    @DisplayName("ResourceAccess 注解包含 id 属性")
    void hasIdAttribute() throws Exception {
        ResourceAccess annotation = TestClass.class.getMethod("method").getAnnotation(ResourceAccess.class);
        assertThat(annotation.id()).isEqualTo("#userId");
    }

    @Test
    @DisplayName("ResourceAccess 注解包含 type 属性且默认为 USER")
    void hasTypeAttribute() throws Exception {
        ResourceAccess annotation = TestClass.class.getMethod("method").getAnnotation(ResourceAccess.class);
        assertThat(annotation.type()).isEqualTo("USER");
    }

    @Test
    @DisplayName("ResourceAccess 注解 type 可自定义")
    void customType() throws Exception {
        ResourceAccess annotation =
                TestClass.class.getMethod("customTypeMethod").getAnnotation(ResourceAccess.class);
        assertThat(annotation.id()).isEqualTo("#courseId");
        assertThat(annotation.type()).isEqualTo("COURSE");
    }

    static class TestClass {
        @ResourceAccess(id = "#userId")
        public void method() {}

        @ResourceAccess(id = "#courseId", type = "COURSE")
        public void customTypeMethod() {}
    }
}
