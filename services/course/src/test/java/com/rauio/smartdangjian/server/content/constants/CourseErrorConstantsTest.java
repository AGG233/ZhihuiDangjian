package com.rauio.smartdangjian.server.content.constants;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class CourseErrorConstantsTest {

    @Test
    @DisplayName("COURSE_NOT_FOUND 常数值为 3201")
    void courseNotFound() {
        assertThat(CourseErrorConstants.COURSE_NOT_FOUND).isEqualTo(3201);
    }
}
