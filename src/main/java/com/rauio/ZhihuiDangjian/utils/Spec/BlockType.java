package com.rauio.ZhihuiDangjian.utils.Spec;

import lombok.Getter;

@Getter
public enum BlockType {
    Heading("heading"),
    Paragraph("paragraph"),
    Image("image"),
    Video("video"),
    Attachment("attachment"),
    Audio("audio");

    private final String type;
    BlockType(String type) {
        this.type = type;
    }
}
