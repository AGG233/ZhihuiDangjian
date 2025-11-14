package com.rauio.ZhihuiDangjiang.utils.Spec;

import lombok.Data;
import lombok.Getter;

@Getter
public enum ArticleStatus {
    Draft("draft"),
    Published("published"),
    Deleted("archived");

    private final String status;
    ArticleStatus(String status) {
        this.status = status;
    }
}
