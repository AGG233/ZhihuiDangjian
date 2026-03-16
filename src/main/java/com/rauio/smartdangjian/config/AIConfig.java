package com.rauio.smartdangjian.config;

import com.rauio.smartdangjian.service.ai.advisor.ToDBAdvisor;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class AIConfig {
    @Bean
    public ChatClient chatClient(ChatClient.Builder builder, ToDBAdvisor toDBAdvisor) {
        return builder.defaultAdvisors(toDBAdvisor).build();
    }
}
