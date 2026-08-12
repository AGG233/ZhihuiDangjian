package com.rauio.smartdangjian.server.task.constants;

import static org.assertj.core.api.Assertions.assertThat;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.HashSet;
import java.util.Set;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class TaskErrorConstantsTest {

    @Test
    @DisplayName("错误码均在 9000-9999 段内且互不重复")
    void allCodesWithinRangeAndUnique() {
        Set<Integer> seen = new HashSet<>();
        for (Field field : TaskErrorConstants.class.getDeclaredFields()) {
            if (Modifier.isStatic(field.getModifiers()) && field.getType() == int.class) {
                int code = readCode(field);
                assertThat(code).as(field.getName() + " 应在 9000-9999 段内").isBetween(9000, 9999);
                assertThat(seen.add(code))
                        .as(field.getName() + " 错误码重复: " + code)
                        .isTrue();
            }
        }
    }

    @Test
    @DisplayName("关键错误码值正确")
    void keyCodesHaveExpectedValues() {
        assertThat(TaskErrorConstants.TASK_NOT_FOUND).isEqualTo(9001);
        assertThat(TaskErrorConstants.TASK_ALREADY_ACCEPTED).isEqualTo(9002);
        assertThat(TaskErrorConstants.TASK_CLOSED).isEqualTo(9003);
        assertThat(TaskErrorConstants.TASK_NOT_PUBLISHED).isEqualTo(9004);
        assertThat(TaskErrorConstants.TASK_ACCEPTANCE_NOT_FOUND).isEqualTo(9005);
        assertThat(TaskErrorConstants.TASK_ALREADY_SUBMITTED).isEqualTo(9006);
        assertThat(TaskErrorConstants.TASK_INVALID_STATUS_TRANSITION).isEqualTo(9007);
        assertThat(TaskErrorConstants.TASK_PROGRESS_INVALID).isEqualTo(9008);
        assertThat(TaskErrorConstants.TASK_SAVE_FAILED).isEqualTo(9009);
        assertThat(TaskErrorConstants.TASK_UPDATE_FAILED).isEqualTo(9010);
        assertThat(TaskErrorConstants.TASK_DELETE_FAILED).isEqualTo(9011);
    }

    private int readCode(Field field) {
        try {
            field.setAccessible(true);
            return field.getInt(null);
        } catch (IllegalAccessException e) {
            throw new IllegalStateException("无法读取错误码: " + field.getName(), e);
        }
    }
}
