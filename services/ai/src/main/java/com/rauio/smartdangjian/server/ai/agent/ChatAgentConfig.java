package com.rauio.smartdangjian.server.ai.agent;


import com.alibaba.cloud.ai.graph.agent.ReactAgent;
import com.alibaba.cloud.ai.graph.checkpoint.savers.MemorySaver;
import com.rauio.smartdangjian.server.ai.pojo.response.AiChatResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.tool.ToolCallback;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ChatAgentConfig {

    private final ChatModel chatModel;

//    private final ToolCallback getLearningRecordTool;
//    private final ToolCallback getGetLearningRecordTool;

    public ChatAgentConfig(
            ChatModel chatModel
//            @Qualifier("learningRecordCallBack") ToolCallback getLearningRecordTool,
//            @Qualifier("learnedCourseCallBack")ToolCallback getGetLearningRecordTool
    ) {
        this.chatModel = chatModel;
//        this.getLearningRecordTool = getLearningRecordTool;
//        this.getGetLearningRecordTool = getGetLearningRecordTool;
    }


    @Bean
    public ReactAgent chatAgent() {
        return ReactAgent.builder()
                .name("chat-agent")
                .model(chatModel)
//                .tools(getGetLearningRecordTool,getLearningRecordTool)
                .systemPrompt("You are a helpful assistant")
                .outputType(AiChatResponse.class)
                .saver(new MemorySaver())
                .build();
    }
}
