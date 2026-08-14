package com.rauio.smartdangjian.server.ai.controller.user;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringBootConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Bean;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.bean.override.mockito.MockitoSpyBean;

import com.alibaba.cloud.ai.graph.RunnableConfig;
import com.alibaba.cloud.ai.graph.agent.flow.agent.LlmRoutingAgent;
import com.alibaba.cloud.ai.graph.exception.GraphRunnerException;
import com.alibaba.cloud.ai.graph.streaming.OutputType;
import com.alibaba.cloud.ai.graph.streaming.StreamingOutput;
import com.rauio.smartdangjian.exception.BusinessException;
import com.rauio.smartdangjian.server.ai.agent.AiAgentRegistry;
import com.rauio.smartdangjian.server.ai.constants.AiChatResponseType;
import com.rauio.smartdangjian.server.ai.constants.AiErrorConstants;
import com.rauio.smartdangjian.server.ai.pojo.response.AiChatResponse;
import com.rauio.smartdangjian.server.ai.service.AiMemoryService;
import com.rauio.smartdangjian.server.ai.service.LLMService;
import com.rauio.smartdangjian.server.ai.service.SpeechService;
import com.rauio.smartdangjian.server.user.service.UserService;

import reactor.core.publisher.Flux;
import reactor.test.StepVerifier;

/**
 * 语音问答跨层流程测试：真实 VoiceChatController + LLMService 链路，mock SpeechService 与 LLM 依赖边界
 * （AiAgentRegistry / UserService / AiMemoryService）。模拟仓库 CrossLayerTestBase 的跨层风格：禁用
 * DataSource/Flyway（不启用任何自动配置），仅装配被测链路所需 bean。
 */
@SpringBootTest(classes = VoiceChatFlowTest.TestConfig.class)
class VoiceChatFlowTest {

    @Autowired
    private VoiceChatController voiceChatController;

    /** 真实 LLMService 的 spy：委托真实实现，同时允许 verify 断言是否被调用 */
    @MockitoSpyBean
    private LLMService llmService;

    @MockitoBean
    private SpeechService speechService;

    @MockitoBean
    private AiAgentRegistry aiAgentRegistry;

    @MockitoBean
    private UserService userService;

    @MockitoBean
    private AiMemoryService aiMemoryService;

    @SpringBootConfiguration
    static class TestConfig {

        @Bean
        LLMService llmService(
                AiAgentRegistry aiAgentRegistry, UserService userService, AiMemoryService aiMemoryService) {
            return new LLMService(aiAgentRegistry, userService, aiMemoryService);
        }

        @Bean
        VoiceChatController voiceChatController(SpeechService speechService, LLMService llmService) {
            return new VoiceChatController(speechService, llmService);
        }
    }

    private static MockMultipartFile voiceFile() {
        return new MockMultipartFile("file", "voice.wav", "audio/wav", new byte[] {1, 2, 3});
    }

    private LlmRoutingAgent mockCoordinator(StreamingOutput... outputs) throws GraphRunnerException {
        LlmRoutingAgent coordinator = mock(LlmRoutingAgent.class);
        when(aiAgentRegistry.getCoordinator()).thenReturn(coordinator);
        when(coordinator.stream(anyString(), any(RunnableConfig.class))).thenReturn(Flux.just(outputs));
        return coordinator;
    }

    private static StreamingOutput streamingTextOutput(String text) {
        StreamingOutput output = mock(StreamingOutput.class);
        when(output.getOutputType()).thenReturn(OutputType.AGENT_MODEL_STREAMING);
        when(output.message()).thenReturn(new AssistantMessage(text));
        when(output.node()).thenReturn("chat-agent");
        return output;
    }

    @Test
    @DisplayName("转写成功后链路输出 START/TEXT/END 完整 SSE 序列")
    void chatEmitsStartTextEnd() throws GraphRunnerException {
        when(speechService.transcribe(any())).thenReturn("党的纪律建设是党的生命线");
        when(userService.getCurrentUserId()).thenReturn("user-1");
        mockCoordinator(streamingTextOutput("正在解析您的问题..."));

        MockMultipartFile audio = voiceFile();
        Flux<AiChatResponse> result = voiceChatController.chat(audio, "session-1");

        StepVerifier.create(result)
                .assertNext(r -> {
                    assertThat(r.type()).isEqualTo(AiChatResponseType.START);
                    assertThat(r.sessionId()).isEqualTo("session-1");
                })
                .assertNext(r -> {
                    assertThat(r.type()).isEqualTo(AiChatResponseType.TEXT);
                    assertThat(r.output()).isEqualTo("正在解析您的问题...");
                })
                .assertNext(r -> assertThat(r.type()).isEqualTo(AiChatResponseType.END))
                .verifyComplete();

        verify(speechService).transcribe(audio);
    }

    @Test
    @DisplayName("空转写文本时返回业务错误而非空 SSE")
    void emptyTranscriptReturnsBusinessErrorNotEmptySse() {
        when(speechService.transcribe(any())).thenReturn("");

        MockMultipartFile audio = voiceFile();

        assertThatThrownBy(() -> voiceChatController.chat(audio, "session-1"))
                .isInstanceOf(BusinessException.class)
                .satisfies(e -> assertThat(((BusinessException) e).getCode())
                        .isEqualTo(AiErrorConstants.VOICE_TRANSCRIBE_FAILED));

        verify(llmService, never()).chat(any());
    }

    @Test
    @DisplayName("转写失败（如空音频）时业务错误向上传播，不产生 SSE")
    void transcribeFailurePropagatesBusinessError() {
        when(speechService.transcribe(any()))
                .thenThrow(new BusinessException(AiErrorConstants.VOICE_TRANSCRIBE_FAILED, "音频文件内容为空"));

        MockMultipartFile audio = voiceFile();

        assertThatThrownBy(() -> voiceChatController.chat(audio, "session-1"))
                .isInstanceOf(BusinessException.class)
                .satisfies(e -> assertThat(((BusinessException) e).getCode())
                        .isEqualTo(AiErrorConstants.VOICE_TRANSCRIBE_FAILED));

        verify(llmService, never()).chat(any());
    }
}
