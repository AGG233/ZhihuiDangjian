package com.rauio.smartdangjian.server.ai.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.contains;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.Duration;
import java.time.LocalDateTime;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

import com.rauio.smartdangjian.event.LearningCompletedEvent;
import com.rauio.smartdangjian.server.ai.pojo.request.AiChatRequest;
import com.rauio.smartdangjian.server.ai.pojo.response.AiChatResponse;
import com.rauio.smartdangjian.server.ai.tool.AiQuizGeneratorTool;

import reactor.core.publisher.Flux;

@ExtendWith(MockitoExtension.class)
class LearningCompletionListenerTest {

    @Mock
    private AiQuizGeneratorTool quizGeneratorTool;

    @Mock
    private LLMService llmService;

    @Mock
    private RedisTemplate<String, Object> redisTemplate;

    @Mock
    private ValueOperations<String, Object> valueOps;

    @InjectMocks
    private LearningCompletionListener listener;

    private final LearningCompletedEvent event =
            new LearningCompletedEvent(7L, 88L, LocalDateTime.of(2026, 8, 24, 10, 0));

    @BeforeEach
    void setUp() {
        lenient().when(redisTemplate.opsForValue()).thenReturn(valueOps);
        lenient()
                .when(valueOps.setIfAbsent(anyString(), anyString(), any(Duration.class)))
                .thenReturn(true);
        lenient().doReturn(null).when(quizGeneratorTool).generateMiniQuiz(anyString(), any(), anyString(), anyString());
        lenient()
                .when(llmService.chatForUser(any(AiChatRequest.class), anyString()))
                .thenReturn(Flux.just(new AiChatResponse("end", "s", "done", "node", "agent")));
        setField("enabled", true);
        setField("quizCount", 5);
    }

    private void setField(String name, Object value) {
        org.springframework.test.util.ReflectionTestUtils.setField(listener, name, value);
    }

    @Test
    @DisplayName("学习完成事件触发按配置数量出题并发起评估")
    void onEventGeneratesQuizzesAndEvaluation() {
        listener.onLearningCompleted(event);

        verify(quizGeneratorTool, org.mockito.Mockito.times(5))
                .generateMiniQuiz(eq("88"), eq(null), eq("single_choice"), anyString());
        verify(llmService).chatForUser(any(AiChatRequest.class), eq("7"));
    }

    @Test
    @DisplayName("去抖窗口内重复完成不再重复出题")
    void debounceSkipsRepeatedQuizzes() {
        when(valueOps.setIfAbsent(anyString(), anyString(), any(Duration.class)))
                .thenReturn(false);

        listener.onLearningCompleted(event);

        verify(quizGeneratorTool, never()).generateMiniQuiz(anyString(), any(), anyString(), anyString());
    }

    @Test
    @DisplayName("单题失败不影响剩余题目生成")
    void singleQuizFailureDoesNotStopBatch() {
        doThrow(new RuntimeException("llm down"))
                .doReturn(null)
                .doReturn(null)
                .when(quizGeneratorTool)
                .generateMiniQuiz(anyString(), any(), anyString(), anyString());

        listener.generateQuizzes(event);

        // quizCount=5：单题失败后其余题目继续生成
        verify(quizGeneratorTool, org.mockito.Mockito.times(5))
                .generateMiniQuiz(anyString(), any(), anyString(), anyString());
    }

    @Test
    @DisplayName("全部题目生成失败时回滚去抖键以便重试")
    void allQuizFailureRollsBackDebounceKey() {
        doThrow(new RuntimeException("llm down"))
                .when(quizGeneratorTool)
                .generateMiniQuiz(anyString(), any(), anyString(), anyString());

        listener.generateQuizzes(event);

        verify(redisTemplate)
                .delete(eq(LearningCompletionListener.QUIZ_DEBOUNCE_KEY_PREFIX + event.userId() + ":"
                        + event.chapterId()));
    }

    @Test
    @DisplayName("部分题目成功时保留去抖窗口不回滚")
    void partialQuizSuccessKeepsDebounceKey() {
        doThrow(new RuntimeException("llm down"))
                .doReturn(null)
                .doReturn(null)
                .when(quizGeneratorTool)
                .generateMiniQuiz(anyString(), any(), anyString(), anyString());

        listener.generateQuizzes(event);

        verify(redisTemplate, never()).delete(anyString());
    }

    @Test
    @DisplayName("评估去抖窗口内重复事件不再重复生成评估")
    void evaluationDebounceSkipsDuplicates() {
        when(valueOps.setIfAbsent(
                        contains(LearningCompletionListener.EVALUATION_DEBOUNCE_KEY_PREFIX),
                        anyString(),
                        any(Duration.class)))
                .thenReturn(false);

        listener.generateEvaluation(event);

        verify(llmService, never()).chatForUser(any(AiChatRequest.class), anyString());
    }

    @Test
    @DisplayName("评估生成失败时回滚评估去抖键")
    void evaluationFailureRollsBackDebounceKey() {
        when(llmService.chatForUser(any(AiChatRequest.class), anyString()))
                .thenReturn(Flux.error(new RuntimeException("assessment down")));

        try {
            listener.generateEvaluation(event);
        } catch (RuntimeException expected) {
            // 外层监听器会兜底记录日志，这里仅验证回滚动作
        }

        verify(redisTemplate)
                .delete(eq(LearningCompletionListener.EVALUATION_DEBOUNCE_KEY_PREFIX
                        + event.userId()
                        + ":"
                        + event.chapterId()));
    }

    @Test
    @DisplayName("开关关闭时不做任何动作")
    void disabledListenerDoesNothing() {
        setField("enabled", false);

        listener.onLearningCompleted(event);

        verify(quizGeneratorTool, never()).generateMiniQuiz(anyString(), any(), anyString(), anyString());
        verify(llmService, never()).chatForUser(any(AiChatRequest.class), anyString());
    }

    @Test
    @DisplayName("事件缺少用户或章节时直接忽略")
    void ignoresIncompleteEvents() {
        listener.onLearningCompleted(new LearningCompletedEvent(null, 88L, LocalDateTime.now()));
        listener.onLearningCompleted(new LearningCompletedEvent(7L, null, LocalDateTime.now()));

        assertThat(org.mockito.Mockito.mockingDetails(quizGeneratorTool).getInvocations())
                .isEmpty();
        assertThat(org.mockito.Mockito.mockingDetails(llmService).getInvocations())
                .isEmpty();
    }
}
