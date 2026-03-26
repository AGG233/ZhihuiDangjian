package com.rauio.smartdangjian.config;

import com.rauio.smartdangjian.aop.ResourceAccessAspect;
import com.rauio.smartdangjian.aop.UserAspect;
import com.rauio.smartdangjian.exception.GlobalExceptionHandler;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.context.annotation.Bean;

@AutoConfiguration
public class WebSupportAutoConfiguration {

    @Bean
    public UserAspect userAspect() {
        return new UserAspect();
    }

    @Bean
    public ResourceAccessAspect resourceAccessAspect() {
        return new ResourceAccessAspect();
    }

    @Bean
    public GlobalExceptionHandler globalExceptionHandler() {
        return new GlobalExceptionHandler();
    }
}
