package com.rauio.smartdangjian.server.ai.tool;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Map;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.extension.conditions.query.LambdaQueryChainWrapper;
import com.rauio.smartdangjian.server.ai.pojo.entity.AiChatMessage;
import com.rauio.smartdangjian.server.ai.service.AiChatMessageService;
import com.rauio.smartdangjian.server.user.service.UserService;

@ExtendWith(MockitoExtension.class)
class QuizToolTest {

    @Mock
    private AiChatMessageService messageService;

    @Mock
    private UserService userService;

    @InjectMocks
    private QuizTool quizTool;

    @Nested
    @DisplayName("getQuizReasoning 方法")
    class GetQuizReasoningTest {

        @SuppressWarnings("unchecked")
        private LambdaQueryChainWrapper<AiChatMessage> realChain(AiChatMessage result) {
            BaseMapper<AiChatMessage> mockMapper = mock(BaseMapper.class);
            when(mockMapper.selectOne(any())).thenReturn(result);
            LambdaQueryChainWrapper<AiChatMessage> wrapper = new LambdaQueryChainWrapper<>(mockMapper);
            doReturn(wrapper).when(messageService).lambdaQuery();
            return wrapper;
        }

        @Test
        @DisplayName("提供 sessionId 参数时使用该值查询并返回 metadata")
        void usesProvidedSessionIdAndReturnsMetadata() {
            Map<String, Object> metadata = Map.of("reasoning", "some reasoning");
            AiChatMessage message = AiChatMessage.builder()
                    .sessionId("session-1")
                    .userId(1L)
                    .metadata(metadata)
                    .build();

            realChain(message);

            Object result = quizTool.getQuizReasoning("session-1");

            assertThat(result).isEqualTo(metadata);
        }

        @Test
        @DisplayName("sessionId 为 null 时返回 null")
        void usesSessionIdFromToolContextWhenParamIsNull() {
            Object result = quizTool.getQuizReasoning(null);

            assertThat(result).isNull();
        }

        @Test
        @DisplayName("sessionId 为空白字符串时返回 null")
        void usesSessionIdFromToolContextWhenParamIsBlank() {
            Object result = quizTool.getQuizReasoning("   ");

            assertThat(result).isNull();
        }

        @Test
        @DisplayName("未找到消息时返回 null")
        void returnsNullWhenMessageNotFound() {
            realChain(null);

            Object result = quizTool.getQuizReasoning("nonexistent-session");

            assertThat(result).isNull();
        }
    }
}
