package com.rauio.smartdangjian.service.ai.advisor;


import com.rauio.smartdangjian.constants.ErrorConstants;
import com.rauio.smartdangjian.exception.BusinessException;
import com.rauio.smartdangjian.pojo.AiChatMessage;
import com.rauio.smartdangjian.service.ai.AiChatMessageService;
import com.rauio.smartdangjian.service.user.UserService;
import lombok.RequiredArgsConstructor;
import org.jspecify.annotations.NonNull;
import org.springframework.ai.chat.client.ChatClientRequest;
import org.springframework.ai.chat.client.ChatClientResponse;
import org.springframework.ai.chat.client.advisor.api.Advisor;
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
public class ToDBAdvisor implements Advisor,StreamAdvisor{

    private final AiChatMessageService aiChatMessageService;
    private final UserService userService;

    @Override
    public @NonNull Flux<ChatClientResponse> adviseStream(@NonNull ChatClientRequest request, @NonNull StreamAdvisorChain chain) {
        String finalInput = request.prompt().getContents();
        StringBuilder contentAggregator = new StringBuilder();
        Long userId = userService.getUserIDFromAuthentication();

        return chain.nextStream(request)
                .doOnNext(response -> {
                    String chunk = null;
                    if (response.chatResponse() != null) {
                        chunk = response.chatResponse().getResult().getOutput().toString();
                    }
                    contentAggregator.append(chunk);
                })
                .publishOn(Schedulers.boundedElastic())
                .doOnComplete(() -> {
                    try{
                        String finalOutput = contentAggregator.toString();
                        aiChatMessageService.create(userId,finalInput, finalOutput);
                    }catch (Exception e){
                        log.error("保存当前会话数据失败",e);
                    }
                });
    }


    @Override
    public @NonNull String getName() { return "ToDBAdvisor"; }
    @Override
    public int getOrder() { return 0; }
}