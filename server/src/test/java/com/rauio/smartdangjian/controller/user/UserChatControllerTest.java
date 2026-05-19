package com.rauio.smartdangjian.controller.user;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.lang.reflect.Method;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.boot.SpringBootConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Bean;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.web.bind.annotation.PostMapping;

import com.rauio.smartdangjian.BaseControllerTest;
import com.rauio.smartdangjian.server.ai.constants.AiChatResponseType;
import com.rauio.smartdangjian.server.ai.controller.user.UserChatController;
import com.rauio.smartdangjian.server.ai.pojo.entity.AiChatMessage;
import com.rauio.smartdangjian.server.ai.pojo.request.AiChatRequest;
import com.rauio.smartdangjian.server.ai.pojo.response.AiChatResponse;
import com.rauio.smartdangjian.server.ai.service.AiMemoryService;
import com.rauio.smartdangjian.server.ai.service.LLMService;
import com.rauio.smartdangjian.server.user.service.UserService;

import reactor.core.publisher.Flux;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK, classes = UserChatControllerTest.TestConfig.class)
@DisplayName("用户AI聊天接口测试")
class UserChatControllerTest extends BaseControllerTest {

    @SpringBootConfiguration
    static class TestConfig extends CommonTestConfig {
        @Bean
        public UserChatController userChatController(
                LLMService llmService, AiMemoryService aiMemoryService, UserService userService) {
            return new UserChatController(llmService, aiMemoryService, userService);
        }
    }

    @MockitoBean
    private LLMService llmService;

    @MockitoBean
    private AiMemoryService aiMemoryService;

    @MockitoBean
    private UserService userService;

    @Nested
    @DisplayName("正常场景")
    class NormalTests {

        @Test
        @DisplayName("POST / - SSE 聊天成功")
        void chatSuccess() throws Exception {
            when(llmService.chat(any(AiChatRequest.class)))
                    .thenReturn(
                            Flux.just(new AiChatResponse("assistant", "回复内容", null, AiChatResponseType.TEXT, null)));

            mockMvc.perform(post("/api/ai/chat")
                            .contentType(MediaType.APPLICATION_JSON)
                            .accept(MediaType.TEXT_EVENT_STREAM)
                            .content("{\"message\":\"hello\"}"))
                    .andExpect(status().isOk());
        }

        @Test
        @DisplayName("POST /evaluation - SSE 学习评估成功（兼容垫片）")
        void evaluateSuccess() throws Exception {
            when(llmService.chat(any(AiChatRequest.class)))
                    .thenReturn(
                            Flux.just(new AiChatResponse("assistant", "评估结果", null, AiChatResponseType.TEXT, null)));

            mockMvc.perform(post("/api/ai/chat/evaluation")
                            .contentType(MediaType.APPLICATION_JSON)
                            .accept(MediaType.TEXT_EVENT_STREAM)
                            .content("{}"))
                    .andExpect(status().isOk());
        }

        @Test
        @DisplayName("POST /quiz - SSE 测试小题成功（兼容垫片）")
        void quizSuccess() throws Exception {
            when(llmService.chat(any(AiChatRequest.class)))
                    .thenReturn(Flux.just(new AiChatResponse("assistant", "测试题", null, AiChatResponseType.TEXT, null)));

            mockMvc.perform(post("/api/ai/chat/quiz")
                            .contentType(MediaType.APPLICATION_JSON)
                            .accept(MediaType.TEXT_EVENT_STREAM)
                            .content("{\"topic\":\"党的纪律建设\"}"))
                    .andExpect(status().isOk());
        }

        @Test
        @DisplayName("GET /{sessionId}/messages - 查询会话消息成功")
        void listMessagesSuccess() throws Exception {
            when(userService.getCurrentUserId()).thenReturn("stu-001");
            List<AiChatMessage> messages = List.of(AiChatMessage.builder()
                    .id("msg-1")
                    .sessionId("session-1")
                    .userId("stu-001")
                    .agentType("CHAT")
                    .senderType("USER")
                    .content("你好")
                    .messageType("TEXT")
                    .createdAt(LocalDateTime.now())
                    .build());
            when(aiMemoryService.listSessionMessages("stu-001", "session-1")).thenReturn(messages);

            mockMvc.perform(get("/api/ai/chat/session-1/messages"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value("200"))
                    .andExpect(jsonPath("$.data.length()").value(1))
                    .andExpect(jsonPath("$.data[0].id").value("msg-1"));
        }

        @Test
        @DisplayName("流式接口声明 SSE 响应类型")
        void streamingEndpointsDeclareSseMediaType() throws Exception {
            assertPostMappingProducesSse("chat", AiChatRequest.class);
            assertPostMappingProducesSse(
                    "evaluate", com.rauio.smartdangjian.server.ai.pojo.request.AiEvaluationRequest.class);
            assertPostMappingProducesSse("quiz", com.rauio.smartdangjian.server.ai.pojo.request.AiQuizRequest.class);
        }
    }

    @Nested
    @DisplayName("异常处理场景")
    class ErrorTests {

        @Test
        @DisplayName("POST / - message 为空返回 400")
        void chatWithEmptyMessage() throws Exception {
            mockMvc.perform(post("/api/ai/chat")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("{\"message\":\"\"}"))
                    .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("POST / - 请求体为空返回 400")
        void chatWithEmptyBody() throws Exception {
            mockMvc.perform(post("/api/ai/chat")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(""))
                    .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("POST /quiz - topic 为空返回 400")
        void quizWithEmptyTopic() throws Exception {
            mockMvc.perform(post("/api/ai/chat/quiz")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("{\"topic\":\"\"}"))
                    .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("POST / - Service 抛出 RuntimeException 时 SSE 仍然返回 200 (Flux 内部处理)")
        void chatWithRuntimeException() throws Exception {
            // LLMService handles errors internally via onErrorResume, so the HTTP status is still 200
            when(llmService.chat(any(AiChatRequest.class))).thenReturn(Flux.error(new RuntimeException("AI 服务异常")));

            mockMvc.perform(post("/api/ai/chat")
                            .contentType(MediaType.APPLICATION_JSON)
                            .accept(MediaType.TEXT_EVENT_STREAM)
                            .content("{\"message\":\"hello\"}"))
                    .andExpect(status().isOk());
        }

        @Test
        @DisplayName("POST / - 非法 JSON 返回 400")
        void malformedJson() throws Exception {
            mockMvc.perform(post("/api/ai/chat")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("{invalid json"))
                    .andExpect(status().isBadRequest());
        }
    }

    @Nested
    @DisplayName("边界场景")
    class BoundaryTests {

        @Test
        @DisplayName("GET /{sessionId}/messages - 空消息列表")
        void listMessagesEmpty() throws Exception {
            when(userService.getCurrentUserId()).thenReturn("stu-001");
            when(aiMemoryService.listSessionMessages("stu-001", "session-empty"))
                    .thenReturn(List.of());

            mockMvc.perform(get("/api/ai/chat/session-empty/messages"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value("200"))
                    .andExpect(jsonPath("$.data.length()").value(0));
        }

        @Test
        @DisplayName("POST / - 超长消息内容")
        void chatWithLongMessage() throws Exception {
            when(llmService.chat(any(AiChatRequest.class))).thenReturn(Flux.empty());

            String longMsg = "a".repeat(10000);
            mockMvc.perform(post("/api/ai/chat")
                            .contentType(MediaType.APPLICATION_JSON)
                            .accept(MediaType.TEXT_EVENT_STREAM)
                            .content("{\"message\":\"" + longMsg + "\"}"))
                    .andExpect(status().isOk());
        }

        @Test
        @DisplayName("POST / - 中文消息内容")
        void chatWithChineseMessage() throws Exception {
            when(llmService.chat(any(AiChatRequest.class))).thenReturn(Flux.empty());

            mockMvc.perform(post("/api/ai/chat")
                            .contentType(MediaType.APPLICATION_JSON)
                            .accept(MediaType.TEXT_EVENT_STREAM)
                            .content("{\"message\":\"请用一句话介绍党的性质\"}"))
                    .andExpect(status().isOk());
        }
    }

    @Nested
    @DisplayName("安全场景")
    class SecurityTests {

        @Test
        @DisplayName("XSS 注入在 message 字段")
        void xssInMessage() throws Exception {
            when(llmService.chat(any(AiChatRequest.class))).thenReturn(Flux.empty());

            mockMvc.perform(post("/api/ai/chat")
                            .contentType(MediaType.APPLICATION_JSON)
                            .accept(MediaType.TEXT_EVENT_STREAM)
                            .content("{\"message\":\"<script>alert('xss')</script>\"}"))
                    .andExpect(status().isOk());
        }

        @Test
        @DisplayName("SQL 注入在 message 字段")
        void sqlInjectionInMessage() throws Exception {
            when(llmService.chat(any(AiChatRequest.class))).thenReturn(Flux.empty());

            mockMvc.perform(post("/api/ai/chat")
                            .contentType(MediaType.APPLICATION_JSON)
                            .accept(MediaType.TEXT_EVENT_STREAM)
                            .content("{\"message\":\"' OR '1'='1\"}"))
                    .andExpect(status().isOk());
        }

        @Test
        @DisplayName("GET 请求聊天接口返回 405")
        void chatWithWrongMethod() throws Exception {
            mockMvc.perform(get("/api/ai/chat")).andExpect(status().isMethodNotAllowed());
        }

        @Test
        @DisplayName("POST 请求消息查询接口返回 405")
        void listMessagesWithWrongMethod() throws Exception {
            mockMvc.perform(post("/api/ai/chat/session-1/messages")).andExpect(status().isMethodNotAllowed());
        }
    }

    private void assertPostMappingProducesSse(String methodName, Class<?> parameterType) throws NoSuchMethodException {
        Method method = UserChatController.class.getDeclaredMethod(methodName, parameterType);
        PostMapping postMapping = method.getAnnotation(PostMapping.class);

        assertThat(postMapping).as("%s must declare @PostMapping", methodName).isNotNull();
        assertThat(Arrays.asList(postMapping.produces())).contains(MediaType.TEXT_EVENT_STREAM_VALUE);
    }
}
