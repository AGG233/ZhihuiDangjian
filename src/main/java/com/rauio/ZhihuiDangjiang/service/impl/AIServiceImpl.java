package com.rauio.ZhihuiDangjiang.service.impl;

import com.rauio.ZhihuiDangjiang.pojo.User;
import com.rauio.ZhihuiDangjiang.service.AIService;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;


@Service
public class AIServiceImpl implements AIService {

    private final ChatClient chatClient;
    @Autowired
    public AIServiceImpl(ChatClient.Builder builder) {
        this.chatClient = builder.build();
    }

    @Override
    public Flux<String> chat(String message){
        return chatClient.prompt()
                .user(user ->{
                    user.text(message);
                })
                .stream()
                .content();
    }
    @Override
    public String getEvaluationByUser(User user) {
        return "";
    }
}
