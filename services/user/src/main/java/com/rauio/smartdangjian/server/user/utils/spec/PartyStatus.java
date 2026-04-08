package com.rauio.smartdangjian.server.user.utils.spec;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.Getter;

@Getter
public enum PartyStatus {
    FORMAL_MEMBER("正式党员"),
    PROBATIONARY_MEMBER("预备党员"),
    DEVELOPMENT_TARGET("发展对象"),
    PARTY_ACTIVIST("积极分子"),
    GENERAL_PUBLIC("群众");

    @EnumValue
    private final String description;

    PartyStatus(String description) {
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
}
