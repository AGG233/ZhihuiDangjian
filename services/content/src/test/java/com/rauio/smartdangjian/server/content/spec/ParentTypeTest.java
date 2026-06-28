package com.rauio.smartdangjian.server.content.spec;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class ParentTypeTest {

    @Test
    @DisplayName("所有 ParentType 常量 type 字段不为空")
    void allParentTypesHaveType() {
        for (ParentType pt : ParentType.values()) {
            assertThat(pt.getType()).isNotNull();
        }
    }

    @Test
    @DisplayName("ParentType 枚举值正确")
    void parentTypeValues() {
        assertThat(ParentType.valueOf("chapter")).isEqualTo(ParentType.chapter);
        assertThat(ParentType.valueOf("article")).isEqualTo(ParentType.article);
    }
}
