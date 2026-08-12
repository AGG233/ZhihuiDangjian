package com.rauio.smartdangjian.server.ai.config;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

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

@Slf4j
@Configuration
@RequiredArgsConstructor
public class RateLimitConfig implements WebMvcConfigurer {

    private final UserService userService;
    private final ObjectMapper objectMapper;

    @Value("${ai.rate-limit.enabled:true}")
    private boolean enabled;

    @Value("${ai.rate-limit.requests-per-minute:10}")
    private int requestsPerMinute;

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        if (!enabled) {
            return;
        }
        registry.addInterceptor(new RateLimitInterceptor()).addPathPatterns("/api/ai/chat/**");
    }

    private class RateLimitInterceptor implements HandlerInterceptor {

        private final Map<String, ConcurrentHashMap<Long, AtomicInteger>> userCounters = new ConcurrentHashMap<>();

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

            long windowKey = System.currentTimeMillis() / 60_000;
            ConcurrentHashMap<Long, AtomicInteger> counters =
                    userCounters.computeIfAbsent(userId, k -> new ConcurrentHashMap<>());

            counters.entrySet().removeIf(e -> e.getKey() < windowKey - 1);

            AtomicInteger counter = counters.computeIfAbsent(windowKey, k -> new AtomicInteger(0));
            int count = counter.incrementAndGet();

            if (count > requestsPerMinute) {
                log.warn("AI请求限流触发 userId={} count={}", userId, count);
                response.setStatus(HttpStatus.TOO_MANY_REQUESTS.value());
                response.setContentType("application/json;charset=UTF-8");
                response.getWriter().write(objectMapper.writeValueAsString(Result.error("429", "请求过于频繁，请稍后重试")));
                return false;
            }
            return true;
        }
    }
}
