package com.rauio.smartdangjian.controller.user;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.lang.reflect.Method;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.boot.SpringBootConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Bean;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.web.bind.annotation.PostMapping;

import com.rauio.smartdangjian.BaseControllerTest;
import com.rauio.smartdangjian.exception.BusinessException;
import com.rauio.smartdangjian.server.ai.constants.AiErrorConstants;
import com.rauio.smartdangjian.server.ai.controller.user.VoiceChatController;
import com.rauio.smartdangjian.server.ai.pojo.request.AiChatRequest;
import com.rauio.smartdangjian.server.ai.pojo.response.AiChatResponse;
import com.rauio.smartdangjian.server.ai.service.LLMService;
import com.rauio.smartdangjian.server.ai.service.SpeechService;

import cn.dev33.satoken.annotation.SaCheckRole;
import reactor.core.publisher.Flux;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK, classes = VoiceChatControllerTest.TestConfig.class)
@DisplayName("AI语音问答接口测试")
class VoiceChatControllerTest extends BaseControllerTest {

    @SpringBootConfiguration
    static class TestConfig extends CommonTestConfig {
        @Bean
        public VoiceChatController voiceChatController(SpeechService speechService, LLMService llmService) {
            return new VoiceChatController(speechService, llmService);
        }
    }

    @MockitoBean
    private SpeechService speechService;

    @MockitoBean
    private LLMService llmService;

    @Nested
    @DisplayName("正常场景")
    class NormalTests {

        @Test
        @DisplayName("POST /chat - 语音问答 SSE 成功")
        void chatSuccess() throws Exception {
            when(speechService.transcribe(any())).thenReturn("请介绍党的性质");
            when(llmService.chat(any(AiChatRequest.class)))
                    .thenReturn(Flux.just(new AiChatResponse("TEXT", "session-1", "回复内容", "chat-agent", "chat-agent")));

            MockMultipartFile file = new MockMultipartFile("file", "voice.wav", "audio/wav", new byte[] {1, 2, 3});

            mockMvc.perform(multipart("/api/ai/voice/chat").file(file)).andExpect(status().isOk());
        }

        @Test
        @DisplayName("POST /chat - 携带可选 sessionId 成功")
        void chatWithSessionId() throws Exception {
            when(speechService.transcribe(any())).thenReturn("党的纪律要求");
            when(llmService.chat(any(AiChatRequest.class))).thenReturn(Flux.empty());

            MockMultipartFile file = new MockMultipartFile("file", "voice.wav", "audio/wav", new byte[] {1, 2, 3});
            MockMultipartFile sessionId = new MockMultipartFile(
                    "sessionId", "sessionId", "text/plain", "session-1".getBytes(StandardCharsets.UTF_8));

            mockMvc.perform(multipart("/api/ai/voice/chat").file(file).file(sessionId))
                    .andExpect(status().isOk());
        }

        @Test
        @DisplayName("流式接口声明 SSE 响应类型")
        void streamingEndpointDeclaresSseMediaType() throws Exception {
            Method method = VoiceChatController.class.getDeclaredMethod(
                    "chat", org.springframework.web.multipart.MultipartFile.class, String.class);
            PostMapping postMapping = method.getAnnotation(PostMapping.class);

            assertThat(postMapping).as("chat must declare @PostMapping").isNotNull();
            assertThat(Arrays.asList(postMapping.produces())).contains(MediaType.TEXT_EVENT_STREAM_VALUE);
        }

        @Test
        @DisplayName("控制器要求 STUDENT 角色")
        void controllerRequiresStudentRole() {
            SaCheckRole role = VoiceChatController.class.getAnnotation(SaCheckRole.class);
            assertThat(role).as("VoiceChatController must declare @SaCheckRole").isNotNull();
            assertThat(Arrays.asList(role.value())).contains("STUDENT");
        }
    }

    @Nested
    @DisplayName("异常处理场景")
    class ErrorTests {

        @Test
        @DisplayName("POST /chat - 缺少 file part 返回 400")
        void chatWithoutFile() throws Exception {
            mockMvc.perform(multipart("/api/ai/voice/chat")).andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("POST /chat - 转写失败返回 400 业务错误码")
        void chatWhenTranscribeFails() throws Exception {
            when(speechService.transcribe(any()))
                    .thenThrow(new BusinessException(AiErrorConstants.VOICE_TRANSCRIBE_FAILED, "语音转写失败"));

            MockMultipartFile file = new MockMultipartFile("file", "voice.wav", "audio/wav", new byte[] {1, 2, 3});

            mockMvc.perform(multipart("/api/ai/voice/chat").file(file))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.code").value(String.valueOf(AiErrorConstants.VOICE_TRANSCRIBE_FAILED)));
        }
    }
}
