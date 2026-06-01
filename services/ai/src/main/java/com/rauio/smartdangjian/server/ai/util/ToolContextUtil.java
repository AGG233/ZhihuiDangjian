package com.rauio.smartdangjian.server.ai.util;

import org.springframework.ai.chat.model.ToolContext;

import com.alibaba.cloud.ai.graph.agent.tools.ToolContextHelper;
import com.rauio.smartdangjian.server.ai.pojo.dto.ToolExecutionContext;
import com.rauio.smartdangjian.server.user.service.UserService;

/**
 * AI Tool 上下文工具。作为兼容适配层，业务 Tool 不应直接依赖 {@link ToolContext}。
 * <p>新代码应使用 {@link #resolveUserId(UserService)} 替代已废弃方法。</p>
 */
public final class ToolContextUtil {

    private ToolContextUtil() {}

    /**
     * 从当前安全上下文解析用户ID。
     */
    public static String resolveUserId(UserService userService) {
        return userService.getCurrentUserId();
    }

    /**
     * 创建工具执行上下文（基于当前安全上下文）。
     */
    public static ToolExecutionContext createContext(UserService userService) {
        return new ToolExecutionContext(resolveUserId(userService), null);
    }

    // ========== 以下为适配层，保留对已废弃 ToolContext 的兼容 ==========

    /**
     * 从 ToolContext 获取 userId，回退到 SecurityUtils。
     * @deprecated 请使用 {@link #resolveUserId(UserService)}
     */
    @Deprecated(since = "0.9.2", forRemoval = true)
    public static String getUserId(ToolContext toolContext, UserService userService) {
        return ToolContextHelper.getMetadata(toolContext, "userId", String.class)
                .orElseGet(userService::getCurrentUserId);
    }

    /**
     * 从 ToolContext 获取 sessionId。
     * @deprecated 会话ID应由 LLM 通过 {@code @ToolParam} 显式传递
     */
    @Deprecated(since = "0.9.2", forRemoval = true)
    public static String getSessionId(ToolContext toolContext) {
        return ToolContextHelper.getMetadata(toolContext, "sessionId", String.class)
                .orElse(null);
    }
}
