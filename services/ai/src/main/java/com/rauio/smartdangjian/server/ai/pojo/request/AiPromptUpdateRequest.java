package com.rauio.smartdangjian.server.ai.pojo.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

@Schema(description = "AI系统提示词更新请求")
@Data
public class AiPromptUpdateRequest {

    @Schema(description = "提示词类型", example = "COMMON")
    @Pattern(regexp = "COMMON|EVALUATION|QUIZ", message = "type只支持COMMON、EVALUATION或QUIZ")
    private String type;

    @Schema(description = "提示词内容", example = "你是党务学习助手，回答需严谨、客观、简洁。")
    @Pattern(regexp = ".*\\S.*", message = "content不能为空")
    private String content;

    @Schema(description = "是否启用", example = "true")
    private Boolean enabled;

    @Schema(description = "排序号(升序)", example = "10")
    private Integer sort;
}
