package com.rauio.smartdangjian.config;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;

import jakarta.servlet.FilterChain;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

@DisplayName("SecurityHeadersFilter 安全响应头测试")
class SecurityHeadersFilterTest {

    private final SecurityHeadersFilter filter = new SecurityHeadersFilter();

    @Test
    @DisplayName("普通请求写入基础安全响应头")
    void writesSecurityHeaders() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/api/auth/captcha");
        MockHttpServletResponse response = new MockHttpServletResponse();
        FilterChain chain = org.mockito.Mockito.mock(FilterChain.class);

        filter.doFilterInternal(request, response, chain);

        assertThat(response.getHeader("Content-Security-Policy"))
                .contains("default-src 'self'")
                .contains("frame-ancestors 'none'")
                .contains("object-src 'none'");
        assertThat(response.getHeader("X-Content-Type-Options")).isEqualTo("nosniff");
        assertThat(response.getHeader("X-Frame-Options")).isEqualTo("DENY");
        assertThat(response.getHeader("Referrer-Policy")).isEqualTo("strict-origin-when-cross-origin");
        assertThat(response.getHeader("Permissions-Policy")).contains("camera=()");
        assertThat(response.getHeader("Strict-Transport-Security")).isNull();
        verify(chain).doFilter(request, response);
    }

    @Test
    @DisplayName("HTTPS 请求额外写入 HSTS")
    void writesHstsForSecureRequests() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/api/auth/captcha");
        request.setSecure(true);
        MockHttpServletResponse response = new MockHttpServletResponse();
        FilterChain chain = org.mockito.Mockito.mock(FilterChain.class);

        filter.doFilterInternal(request, response, chain);

        assertThat(response.getHeader("Strict-Transport-Security")).isEqualTo("max-age=31536000; includeSubDomains");
        verify(chain).doFilter(request, response);
    }
}
