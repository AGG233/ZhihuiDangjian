package com.rauio.smartdangjian.server.ai.util;

import com.alibaba.cloud.ai.graph.agent.tools.ToolContextHelper;
import org.springframework.ai.chat.model.ToolContext;
import com.rauio.smartdangjian.server.user.service.UserService;

public final class ToolContextUtil {

    private ToolContextUtil() {
    }

    public static String getUserId(ToolContext toolContext, UserService userService) {
        return ToolContextHelper.getMetadata(toolContext, "userId", String.class)
                .orElseGet(userService::getCurrentUserId);
    }

    public static String getSessionId(ToolContext toolContext) {
        return ToolContextHelper.getMetadata(toolContext, "sessionId", String.class)
                .orElse(null);
    }
}
