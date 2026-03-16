package com.rauio.smartdangjian.pojo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "AI系统提示词")
public class AiSystemPrompt {

    @Schema(description = "提示词ID", example = "1")
    private String id;

    @Schema(description = "提示词类型", example = "COMMON")
    private String type;

    @Schema(description = "提示词内容", example = "你是党务学习助手，回答需严谨、客观、简洁。")
    private String content;

    @Schema(description = "是否启用", example = "true")
    private Boolean enabled;

    @Schema(description = "排序号(升序)", example = "10")
    private Integer sort;

    @Schema(description = "创建时间")
    private LocalDateTime createdAt;

    @Schema(description = "更新时间")
    private LocalDateTime updatedAt;
}
