package com.rauio.ZhihuiDangjiang.utils.Spec;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.fasterxml.jackson.annotation.JsonValue;

public enum UserType {
    STUDENT("学生"),
    TEACHER("教师"),
    MANAGER("管理员");

    @EnumValue
    private final String Type;
    UserType(String type) {
        this.Type = type;
    }
    @JsonValue
    public String getType() {
        return Type;
    }
    @Override
    public String toString() {
        return Type;
    }

}
