package com.rauio.smartdangjian.server.content.spec;

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
