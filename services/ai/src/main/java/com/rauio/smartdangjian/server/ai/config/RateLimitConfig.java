package com.rauio.smartdangjian.server.ai.config;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatus;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.rauio.smartdangjian.pojo.response.Result;
import com.rauio.smartdangjian.server.user.service.UserService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * AI 对话接口限流配置。
 *
 * <p>计数走 {@link AiRateLimiter}（Redisson 分布式令牌桶），多实例部署时限流
 * 额度全局共享；主体优先取当前用户 ID，取不到时回退客户端 IP。
 */
@Slf4j
@Configuration
@RequiredArgsConstructor
public class RateLimitConfig implements WebMvcConfigurer {

    private final UserService userService;
    private final ObjectMapper objectMapper;
    private final AiRateLimiter rateLimiter;

    @Value("${ai.rate-limit.enabled:true}")
    private boolean enabled;

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        if (!enabled) {
            return;
        }
        registry.addInterceptor(new RateLimitInterceptor()).addPathPatterns("/api/ai/chat/**");
    }

    private class RateLimitInterceptor implements HandlerInterceptor {

        @Override
        public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler)
                throws Exception {
            String userId;
            try {
                userId = userService.getCurrentUserId();
            } catch (Exception e) {
                userId = request.getRemoteAddr();
            }
            if (userId == null) {
                userId = request.getRemoteAddr();
            }

            if (!rateLimiter.tryAcquire(userId)) {
                log.warn("AI请求限流触发 subject={}", userId);
                response.setStatus(HttpStatus.TOO_MANY_REQUESTS.value());
                response.setContentType("application/json;charset=UTF-8");
                response.getWriter().write(objectMapper.writeValueAsString(Result.error("429", "请求过于频繁，请稍后重试")));
                return false;
            }
            return true;
        }
    }
}
