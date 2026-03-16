package com.rauio.smartdangjian.service.ai;

import org.junit.jupiter.api.Test;
import org.springframework.ai.tool.ToolCallback;
import org.springframework.ai.tool.resolution.SpringBeanToolCallbackResolver;
import org.springframework.ai.util.json.schema.SchemaType;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.support.GenericApplicationContext;

import static org.junit.jupiter.api.Assertions.assertNotNull;

@SpringBootTest
class SpringAiToolInjectionTests {

    private final GenericApplicationContext applicationContext;

    SpringAiToolInjectionTests(GenericApplicationContext applicationContext) {
        this.applicationContext = applicationContext;
    }

    @Test
    void toolFunctionsShouldBeResolvable() {
        SpringBeanToolCallbackResolver resolver = SpringBeanToolCallbackResolver.builder()
                .applicationContext(applicationContext)
                .schemaType(SchemaType.JSON_SCHEMA)
                .build();

        ToolCallback currentUserInfo = resolver.resolve("getCurrentUserInfo");
        ToolCallback recentLearningRecords = resolver.resolve("getRecentLearningRecords");
        ToolCallback learnedCourseIds = resolver.resolve("getLearnedCourseIds");

        assertNotNull(currentUserInfo);
        assertNotNull(recentLearningRecords);
        assertNotNull(learnedCourseIds);
    }
}
