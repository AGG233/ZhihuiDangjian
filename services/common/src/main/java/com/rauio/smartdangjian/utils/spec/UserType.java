package com.rauio.smartdangjian.utils.spec;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.fasterxml.jackson.annotation.JsonValue;

public enum UserType {
    STUDENT("学生"),
    SCHOOL("学校"),
    MANAGER("管理员");

    @EnumValue
    private final String type;

    UserType(String type) {
        this.type = type;
    }

    @JsonValue
    public String getType() {
        return type;
    }

    @Override
    public String toString() {
        return type;
    }
}
