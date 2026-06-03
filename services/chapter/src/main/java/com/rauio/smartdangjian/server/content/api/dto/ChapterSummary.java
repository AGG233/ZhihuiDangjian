package com.rauio.smartdangjian.server.content.api.dto;

import lombok.Builder;
import lombok.Data;

/**
 * 章节摘要 DTO。仅包含最小字段集。
 */
@Data
@Builder
public class ChapterSummary {

    private Long id;

    private Long courseId;

    private String title;

    private String description;

    private Integer orderIndex;
}
