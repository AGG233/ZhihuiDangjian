package com.rauio.smartdangjian.server.content.api.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ContentBlockSummary {

    private Long id;
    private Long chapterId;
    private Long articleId;
    private String blockType;
    private String textContent;
    private Long parentId;
    private Long resourceId;
    private String caption;
    private Integer orderIndex;
}
