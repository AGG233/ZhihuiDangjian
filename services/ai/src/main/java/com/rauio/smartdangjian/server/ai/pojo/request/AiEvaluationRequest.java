package com.rauio.smartdangjian.server.ai.pojo.request;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "AI学习评估请求体")
public record AiEvaluationRequest(

        @Schema(description = "会话ID，可选；不传则自动生成新的评估会话")
        String sessionId,

        @Schema(description = "补充评估要求，可选", example = "请重点分析我最近一周的学习情况")
        String message
) {
}
