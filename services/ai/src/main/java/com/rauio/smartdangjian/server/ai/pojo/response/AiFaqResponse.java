package com.rauio.smartdangjian.server.ai.pojo.response;

import java.time.LocalDateTime;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "AI FAQ响应")
public class AiFaqResponse {

    @Schema(description = "FAQ ID", example = "1")
    @JsonSerialize(using = ToStringSerializer.class)
    private Long id;

    @Schema(description = "触发关键词（逗号分隔）", example = "入党流程,入党条件,如何入党")
    private String keywords;

    @Schema(description = "问题摘要（可读）", example = "入党需要什么条件？")
    private String question;

    @Schema(description = "预定义答案", example = "根据党章规定...")
    private String answer;

    @Schema(description = "是否启用", example = "true")
    private Boolean enabled;

    @Schema(description = "排序序号（越小优先级越高）", example = "0")
    private Integer sort;

    @Schema(description = "创建时间")
    private LocalDateTime createdAt;

    @Schema(description = "更新时间")
    private LocalDateTime updatedAt;
}
