package com.rauio.ZhihuiDangjiang.utils.Spec;

import lombok.Getter;

@Getter
public enum ParentType {
    chapter("chapter"),
    article("article");

    private final String type;
    ParentType(String type) {
        this.type = type;
    }
}
