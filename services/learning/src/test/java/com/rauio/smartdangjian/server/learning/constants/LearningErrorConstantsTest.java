package com.rauio.smartdangjian.server.learning.constants;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("LearningErrorConstants 学习模块错误码常量")
class LearningErrorConstantsTest {

    @Test
    @DisplayName("学习记录错误码在 4000-4010 范围内")
    void recordErrorCodesInRange() {
        assertThat(LearningErrorConstants.RECORD_NOT_FOUND).isEqualTo(4001);
        assertThat(LearningErrorConstants.RECORD_CREATE_FAILED).isEqualTo(4002);
        assertThat(LearningErrorConstants.RECORD_ID_REQUIRED).isEqualTo(4003);
        assertThat(LearningErrorConstants.RECORD_UPDATE_FAILED).isEqualTo(4004);
        assertThat(LearningErrorConstants.RECORD_DELETE_FAILED).isEqualTo(4005);

        assertThat(LearningErrorConstants.RECORD_NOT_FOUND).isBetween(4000, 4999);
        assertThat(LearningErrorConstants.RECORD_CREATE_FAILED).isBetween(4000, 4999);
        assertThat(LearningErrorConstants.RECORD_ID_REQUIRED).isBetween(4000, 4999);
        assertThat(LearningErrorConstants.RECORD_UPDATE_FAILED).isBetween(4000, 4999);
        assertThat(LearningErrorConstants.RECORD_DELETE_FAILED).isBetween(4000, 4999);
    }

    @Test
    @DisplayName("学习进度错误码在 4011-4020 范围内")
    void progressErrorCodesInRange() {
        assertThat(LearningErrorConstants.PROGRESS_NOT_FOUND).isEqualTo(4011);
        assertThat(LearningErrorConstants.PROGRESS_ALREADY_EXISTS).isEqualTo(4012);
        assertThat(LearningErrorConstants.PROGRESS_CREATE_FAILED).isEqualTo(4013);
        assertThat(LearningErrorConstants.PROGRESS_ID_REQUIRED).isEqualTo(4014);
        assertThat(LearningErrorConstants.PROGRESS_UPDATE_FAILED).isEqualTo(4015);
        assertThat(LearningErrorConstants.PROGRESS_DELETE_FAILED).isEqualTo(4016);

        assertThat(LearningErrorConstants.PROGRESS_NOT_FOUND).isBetween(4011, 4016);
    }
}
