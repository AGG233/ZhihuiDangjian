package com.rauio.smartdangjian.service.ai.advisor;


import com.rauio.smartdangjian.pojo.AiChatMessage;
import com.rauio.smartdangjian.service.ai.AiChatMessageService;
import lombok.RequiredArgsConstructor;
import org.jspecify.annotations.NonNull;
import org.springframework.ai.chat.client.ChatClientRequest;
import org.springframework.ai.chat.client.ChatClientResponse;
import org.springframework.ai.chat.client.advisor.api.StreamAdvisor;
import org.springframework.ai.chat.client.advisor.api.StreamAdvisorChain;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.content.Content;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import reactor.core.publisher.Flux;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Component
@RequiredArgsConstructor
public class ToDBAdvisor implements StreamAdvisor {

    private final AiChatMessageService aiChatMessageService;

    @Override
    public @NonNull Flux<ChatClientResponse> adviseStream(@NonNull ChatClientRequest request, @NonNull StreamAdvisorChain chain) {
        return chain.nextStream(request)
                .doOnNext(response -> {
                });
    }

    @Override
    public @NonNull String getName() { return "ToDBAdvisor"; }
    @Override
    public int getOrder() { return 0; }
}