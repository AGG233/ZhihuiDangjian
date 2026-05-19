package com.rauio.smartdangjian.server.graph.constants;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("GraphErrorConstants 知识图谱错误码常量")
class GraphErrorConstantsTest {

    @Test
    @DisplayName("错误码在 7000-7999 范围内")
    void errorCodesAreInRange() {
        assertThat(GraphErrorConstants.USER_NOT_FOUND).isEqualTo(7001);
        assertThat(GraphErrorConstants.CHAPTER_NOT_FOUND).isEqualTo(7002);
        assertThat(GraphErrorConstants.COURSE_NOT_FOUND).isEqualTo(7003);

        assertThat(GraphErrorConstants.USER_NOT_FOUND).isBetween(7000, 7999);
        assertThat(GraphErrorConstants.CHAPTER_NOT_FOUND).isBetween(7000, 7999);
        assertThat(GraphErrorConstants.COURSE_NOT_FOUND).isBetween(7000, 7999);
    }
}
