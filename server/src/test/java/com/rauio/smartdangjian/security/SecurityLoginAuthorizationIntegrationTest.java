package com.rauio.smartdangjian.security;

import static org.mockito.Mockito.mockStatic;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;

import cn.dev33.satoken.exception.NotLoginException;
import cn.dev33.satoken.stp.StpUtil;

@DisplayName("真实登录鉴权拦截链测试")
class SecurityLoginAuthorizationIntegrationTest extends AbstractSecurityAuthorizationIntegrationTest {

    @Test
    @DisplayName("未登录访问受保护接口返回 401")
    void protectedEndpointWithoutLoginReturns401() throws Exception {
        try (MockedStatic<StpUtil> stpUtil = mockStatic(StpUtil.class)) {
            stpUtil.when(StpUtil::checkLogin)
                    .thenThrow(new NotLoginException("当前会话未登录", "login", NotLoginException.NOT_TOKEN));

            mockMvc.perform(post("/api/auth/logout"))
                    .andExpect(status().isUnauthorized())
                    .andExpect(jsonPath("$.code").value("401"))
                    .andExpect(jsonPath("$.message").value("未登录或登录已过期，请重新登录"));
        }
    }


}
