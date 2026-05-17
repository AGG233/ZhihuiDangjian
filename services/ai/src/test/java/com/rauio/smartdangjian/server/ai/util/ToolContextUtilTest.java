package com.rauio.smartdangjian.server.ai.util;

import com.rauio.smartdangjian.server.user.service.UserService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.ai.chat.model.ToolContext;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ToolContextUtilTest {

    @Mock
    private UserService userService;

    @Test
    @DisplayName("从 ToolContext metadata 提取 userId")
    void getUserIdFromToolContext() {
        ToolContext context = new ToolContext(Map.of("userId", "user-123", "sessionId", "session-abc"));

        String userId = ToolContextUtil.getUserId(context, userService);

        assertThat(userId).isEqualTo("user-123");
    }

    @Test
    @DisplayName("ToolContext 无 userId 时回退到 SecurityContext")
    void getUserIdFallbackToSecurityContext() {
        ToolContext context = new ToolContext(Map.of());
        when(userService.getCurrentUserId()).thenReturn("fallback-user");

        String userId = ToolContextUtil.getUserId(context, userService);

        assertThat(userId).isEqualTo("fallback-user");
    }

    @Test
    @DisplayName("从 ToolContext metadata 提取 sessionId")
    void getSessionIdFromToolContext() {
        ToolContext context = new ToolContext(Map.of("sessionId", "session-xyz"));

        String sessionId = ToolContextUtil.getSessionId(context);

        assertThat(sessionId).isEqualTo("session-xyz");
    }

    @Test
    @DisplayName("ToolContext 无 sessionId 时返回 null")
    void getSessionIdReturnsNullWhenMissing() {
        ToolContext context = new ToolContext(Map.of());

        String sessionId = ToolContextUtil.getSessionId(context);

        assertThat(sessionId).isNull();
    }
}
