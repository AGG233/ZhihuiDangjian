package com.rauio.smartdangjian.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.PrintWriter;
import java.io.StringWriter;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class LoginEntryPointTest {

    @Test
    @DisplayName("commence 设置 401 状态和 JSON 响应")
    void commenceReturnsUnauthorizedJson() throws Exception {
        ObjectMapper objectMapper = new ObjectMapper();
        LoginEntryPoint entryPoint = new LoginEntryPoint(objectMapper);

        HttpServletRequest request = mock(HttpServletRequest.class);
        HttpServletResponse response = mock(HttpServletResponse.class);
        StringWriter stringWriter = new StringWriter();
        PrintWriter writer = new PrintWriter(stringWriter);

        when(response.getWriter()).thenReturn(writer);

        entryPoint.commence(request, response, null);

        verify(response).setStatus(401);
        verify(response).setCharacterEncoding("UTF-8");
        verify(response).setContentType("application/json");

        writer.flush();
        String json = stringWriter.toString();
        assertThat(json).contains("\"code\":\"401\"");
        assertThat(json).contains("\"message\":\"Unauthorized\"");
    }
}
