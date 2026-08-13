package com.rauio.smartdangjian.server.content.comment.constants;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("InteractionTargetConstants 目标类型常量")
class InteractionTargetConstantsTest {

    @Test
    @DisplayName("course 为合法目标类型")
    void courseIsValid() {
        assertThat(InteractionTargetConstants.isValid(InteractionTargetConstants.COURSE))
                .isTrue();
    }

    @Test
    @DisplayName("article 为合法目标类型")
    void articleIsValid() {
        assertThat(InteractionTargetConstants.isValid(InteractionTargetConstants.ARTICLE))
                .isTrue();
    }

    @Test
    @DisplayName("未知目标类型非法")
    void unknownTargetIsInvalid() {
        assertThat(InteractionTargetConstants.isValid("video")).isFalse();
    }

    @Test
    @DisplayName("null 目标类型非法")
    void nullTargetIsInvalid() {
        assertThat(InteractionTargetConstants.isValid(null)).isFalse();
    }
}
