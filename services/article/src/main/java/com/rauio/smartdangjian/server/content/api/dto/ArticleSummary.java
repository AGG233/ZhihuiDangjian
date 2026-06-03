package com.rauio.smartdangjian.server.content.api.dto;

import java.time.LocalDateTime;

import lombok.Builder;
import lombok.Data;

/**
 * 文章摘要 DTO。仅包含最小字段集，不暴露 status 等内部状态。
 */
@Data
@Builder
public class ArticleSummary {

    private Long id;

    private String title;

    private String summary;

    private Long authorId;

    private LocalDateTime publishedAt;
}
