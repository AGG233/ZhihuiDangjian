package com.rauio.smartdangjian.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.rauio.smartdangjian.pojo.response.Result;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;

import java.io.IOException;

@RequiredArgsConstructor
public class LoginEntryPoint implements AuthenticationEntryPoint {

    private final ObjectMapper objectMapper;

    @Override
    public void commence(HttpServletRequest request, HttpServletResponse response, AuthenticationException authException) throws IOException, ServletException {
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.setCharacterEncoding("UTF-8");
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);

        Result result = Result.builder()
                .code(String.valueOf(HttpServletResponse.SC_UNAUTHORIZED))
                .message("Unauthorized")
                .build();
        response.getWriter().write(objectMapper.writeValueAsString(result));
    }
}
