package com.rauio.smartdangjian.server.user.utils.spec;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.Getter;

@Getter
public enum AccountStatus {
    ACTIVE("active"),
    INACTIVE("inactive"),
    BANNED("banned");

    @EnumValue
    private final String value;

    AccountStatus(String value) {
        this.value = value;
    }

    @JsonValue
    public String getValue() {
        return value;
    }

    @Override
    public String toString() {
        return value;
    }
}
