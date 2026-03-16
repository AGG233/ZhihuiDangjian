package com.rauio.smartdangjian.utils.spec;

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
