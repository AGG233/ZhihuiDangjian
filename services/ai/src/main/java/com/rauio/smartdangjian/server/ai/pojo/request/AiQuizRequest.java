package com.rauio.smartdangjian.server.ai.pojo.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

@Schema(description = "AI测试小题请求体")
public record AiQuizRequest(

        @Schema(description = "会话ID，可选；不传则自动生成新的出题会话")
        String sessionId,

        @Schema(description = "出题主题", example = "党的纪律建设")
        @NotBlank(message = "topic不能为空")
        String topic
) {
}
