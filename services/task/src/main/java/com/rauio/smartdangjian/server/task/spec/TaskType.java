package com.rauio.smartdangjian.server.task.spec;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.fasterxml.jackson.annotation.JsonValue;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 任务类型。
 *
 * <p>DB 存储与 JSON 传输均使用小写 value（与 user_type 等既有枚举一致，
 * 含 @EnumValue + @JsonValue，避免按 enum name 序列化）。
 */
@Getter
@AllArgsConstructor
public enum TaskType {
    LEARNING("learning"),
    QUIZ("quiz"),
    SOCIAL("social"),
    CUSTOM("custom");

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
