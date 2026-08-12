package com.rauio.smartdangjian.server.task.spec;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.fasterxml.jackson.annotation.JsonValue;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 任务领取状态：accepted → in_progress → submitted → completed/rejected。
 */
@Getter
@AllArgsConstructor
public enum TaskAcceptanceStatus {
    ACCEPTED("accepted"),
    IN_PROGRESS("in_progress"),
    SUBMITTED("submitted"),
    COMPLETED("completed"),
    REJECTED("rejected");

    @EnumValue
    private final String value;

    @JsonValue
    public String getValue() {
        return value;
    }

    @Override
    public String toString() {
        return value;
    }
}
