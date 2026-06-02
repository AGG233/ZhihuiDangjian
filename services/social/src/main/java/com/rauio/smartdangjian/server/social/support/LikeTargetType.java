package com.rauio.smartdangjian.server.social.support;

import java.util.Locale;

import com.rauio.smartdangjian.exception.BusinessException;
import com.rauio.smartdangjian.server.social.constants.SocialErrorConstants;

public enum LikeTargetType {
    COMMENT("comment"),
    ARTICLE("article"),
    COURSE("course");

    private final String value;

    LikeTargetType(String value) {
        this.value = value;
    }

    public String value() {
        return value;
    }

    public static LikeTargetType parse(String rawValue) {
        if (rawValue == null || rawValue.isBlank()) {
            throw invalidTargetType();
        }
        String normalized = rawValue.toLowerCase(Locale.ROOT);
        for (LikeTargetType type : values()) {
            if (type.value.equals(normalized)) {
                return type;
            }
        }
        throw invalidTargetType();
    }

    private static BusinessException invalidTargetType() {
        return new BusinessException(SocialErrorConstants.LIKE_TARGET_TYPE_INVALID, "点赞目标类型不合法");
    }
}
