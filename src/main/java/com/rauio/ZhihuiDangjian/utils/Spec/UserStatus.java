package com.rauio.ZhihuiDangjian.utils.Spec;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.Getter;

import java.util.Objects;
import java.util.stream.Stream;

@Getter
public enum UserStatus {
    formalMEMBER("正式党员"),
    probationaryMember("预备党员"),
    developmentTarget("发展对象"),
    partyActivist("积极分子"),
    generalPublic("群众"),
    banned("banned"),
    active("active"),
    inactive("inactive");

    @EnumValue
    private final String description;
    UserStatus(String description) {
        this.description = description;
    }

    @JsonValue
    public String getDescription() {
        return description;
    }
    @Override
    public String toString() {
        return description;
    }
    public static UserStatus fromDescription(String description) {

        return Stream.of(UserStatus.values())
                .filter(status -> Objects.equals(status.getDescription(), description))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("未知的状态描述: " + description));
    }
}
