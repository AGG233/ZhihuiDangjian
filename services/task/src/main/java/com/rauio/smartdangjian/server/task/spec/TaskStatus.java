package com.rauio.smartdangjian.server.task.spec;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.fasterxml.jackson.annotation.JsonValue;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 任务状态：draft → published → closed 单向流转。
 */
@Getter
@AllArgsConstructor
public enum TaskStatus {
    DRAFT("draft"),
    PUBLISHED("published"),
    CLOSED("closed");

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
