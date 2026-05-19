package com.rauio.smartdangjian.aop.annotation;

import com.rauio.smartdangjian.aop.support.DataScopeAction;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class DataScopeAccessAnnotationTest {

    @Test
    @DisplayName("DataScopeAccess 注解包含所有必要属性")
    void allAttributes() throws Exception {
        DataScopeAccess annotation = TestClass.class.getMethod("method").getAnnotation(DataScopeAccess.class);

        assertThat(annotation.resource()).isEqualTo("USER_MANAGEMENT");
        assertThat(annotation.action()).isEqualTo(DataScopeAction.READ);
        assertThat(annotation.id()).isEqualTo("#id");
        assertThat(annotation.body()).isEqualTo("#user");
        assertThat(annotation.query()).isEqualTo("#query");
    }

    @Test
    @DisplayName("DataScopeAccess 注解默认值为空字符串")
    void defaultValues() throws Exception {
        DataScopeAccess annotation = TestClass.class.getMethod("minimalMethod").getAnnotation(DataScopeAccess.class);

        assertThat(annotation.resource()).isEqualTo("USER_MANAGEMENT");
        assertThat(annotation.action()).isEqualTo(DataScopeAction.READ);
        assertThat(annotation.id()).isEmpty();
        assertThat(annotation.body()).isEmpty();
        assertThat(annotation.query()).isEmpty();
    }

    static class TestClass {
        @DataScopeAccess(
                resource = "USER_MANAGEMENT",
                action = DataScopeAction.READ,
                id = "#id",
                body = "#user",
                query = "#query"
        )
        public void method() {}

        @DataScopeAccess(resource = "USER_MANAGEMENT", action = DataScopeAction.READ)
        public void minimalMethod() {}
    }
}
