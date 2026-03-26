package com.rauio.smartdangjian.server.ai.advisor;


import com.rauio.smartdangjian.server.ai.service.AiChatMessageService;
import com.rauio.smartdangjian.server.ai.service.support.CurrentUserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jspecify.annotations.NonNull;
import org.springframework.ai.chat.client.ChatClientRequest;
import org.springframework.ai.chat.client.ChatClientResponse;
import org.springframework.ai.chat.client.advisor.api.Advisor;
import org.springframework.ai.chat.client.advisor.api.StreamAdvisor;
import org.springframework.ai.chat.client.advisor.api.StreamAdvisorChain;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import reactor.core.scheduler.Schedulers;

@Slf4j
@Component
@RequiredArgsConstructor
public class ToDBAdvisor implements Advisor,StreamAdvisor{

    private final AiChatMessageService aiChatMessageService;
    private final CurrentUserService currentUserService;

    /**
     * 在流式响应结束后将本轮问答写入数据库。
     *
     * @param request 当前请求
     * @param chain Advisor 调用链
     * @return 流式响应
     */
    @Override
    public @NonNull Flux<ChatClientResponse> adviseStream(@NonNull ChatClientRequest request, @NonNull StreamAdvisorChain chain) {
        String finalInput = request.prompt().getContents();
        StringBuilder contentAggregator = new StringBuilder();
        String userId = currentUserService.getCurrentUserId();

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


    /**
     * 返回 Advisor 名称。
     *
     * @return Advisor 名称
     */
    @Override
    public @NonNull String getName() { return "ToDBAdvisor"; }
    @Override
    public int getOrder() { return 0; }
}
