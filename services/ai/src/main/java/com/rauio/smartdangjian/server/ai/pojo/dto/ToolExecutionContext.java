package com.rauio.smartdangjian.server.ai.pojo.dto;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * AI 工具执行上下文，封装用户身份与会话信息，替代已废弃的 {@code ToolContext} API。
 * <p>业务 Tool 只依赖此上下文，不直接访问底层框架 API。</p>
 */
@Schema(description = "AI工具执行上下文")
public record ToolExecutionContext(
        @Schema(description = "用户ID") String userId, @Schema(description = "会话ID") String sessionId) {}
