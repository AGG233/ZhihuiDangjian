package com.rauio.smartdangjian.aop.annotation;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.rauio.smartdangjian.utils.spec.UserType;

class PermissionAccessAnnotationTest {

    @Test
    @DisplayName("PermissionAccess 注解 value 属性默认值为 STUDENT")
    void defaultValue() throws Exception {
        PermissionAccess annotation = TestClass.class.getMethod("defaultMethod").getAnnotation(PermissionAccess.class);

        assertThat(annotation.value()).isEqualTo(UserType.STUDENT);
    }

    @Test
    @DisplayName("PermissionAccess 注解可设置 SCHOOL 值")
    void schoolValue() throws Exception {
        PermissionAccess annotation = TestClass.class.getMethod("schoolMethod").getAnnotation(PermissionAccess.class);

        assertThat(annotation.value()).isEqualTo(UserType.SCHOOL);
    }

    @Test
    @DisplayName("PermissionAccess 注解可设置 MANAGER 值")
    void managerValue() throws Exception {
        PermissionAccess annotation = TestClass.class.getMethod("managerMethod").getAnnotation(PermissionAccess.class);

        assertThat(annotation.value()).isEqualTo(UserType.MANAGER);
    }

    @Test
    @DisplayName("PermissionAccess 注解可用于类级别")
    void classLevel() {
        PermissionAccess annotation = TestClass.class.getAnnotation(PermissionAccess.class);
        assertThat(annotation).isNotNull();
        assertThat(annotation.value()).isEqualTo(UserType.STUDENT);
    }

    @PermissionAccess(UserType.STUDENT)
    static class TestClass {
        @PermissionAccess
        public void defaultMethod() {}

        @PermissionAccess(UserType.SCHOOL)
        public void schoolMethod() {}

        @PermissionAccess(UserType.MANAGER)
        public void managerMethod() {}
    }
}
