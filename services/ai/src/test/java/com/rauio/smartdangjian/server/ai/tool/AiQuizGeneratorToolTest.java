package com.rauio.smartdangjian.server.ai.tool;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.rauio.smartdangjian.exception.BusinessException;
import com.rauio.smartdangjian.server.content.pojo.vo.ChapterVO;
import com.rauio.smartdangjian.server.content.pojo.vo.ContentBlockVO;
import com.rauio.smartdangjian.server.content.service.ContentBlockService;
import com.rauio.smartdangjian.server.content.service.chapter.ChapterService;
import com.rauio.smartdangjian.server.quiz.pojo.entity.Quiz;
import com.rauio.smartdangjian.server.quiz.pojo.entity.QuizOption;
import com.rauio.smartdangjian.server.quiz.service.QuizOptionService;
import com.rauio.smartdangjian.server.quiz.service.QuizService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.model.Generation;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.ai.chat.messages.AssistantMessage;

import java.lang.reflect.Field;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AiQuizGeneratorToolTest {

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Mock
    private ObjectProvider<ChatModel> chatModelProvider;

    @Mock
    private ChapterService chapterService;

    @Mock
    private ContentBlockService contentBlockService;

    @Mock
    private QuizService quizService;

    @Mock
    private QuizOptionService quizOptionService;

    private AiQuizGeneratorTool aiQuizGeneratorTool;

    @Captor
    private ArgumentCaptor<Quiz> quizCaptor;

    @BeforeEach
    void setUp() {
        aiQuizGeneratorTool = new AiQuizGeneratorTool(
                chatModelProvider, chapterService, contentBlockService,
                quizService, quizOptionService, objectMapper);
    }

    @Test
    @DisplayName("generateMiniQuiz 基于主题生成题目并保存")
    void generateMiniQuizWithTopic() {
        ChatModel chatModel = mock(ChatModel.class);
        ChatResponse chatResponse = mock(ChatResponse.class);
        Generation generation = mock(Generation.class);
        AssistantMessage assistantMessage = new AssistantMessage("""
                {
                  "question": "中国共产党的初心和使命是什么？",
                  "explanation": "为中国人民谋幸福，为中华民族谋复兴是党的初心和使命。",
                  "options": [
                    {"optionText": "为中国人民谋幸福", "isCorrect": true, "orderIndex": "A"},
                    {"optionText": "实现共同富裕", "isCorrect": false, "orderIndex": "B"}
                  ]
                }
                """);

        when(chatModelProvider.getObject()).thenReturn(chatModel);
        when(chatModel.call(any(Prompt.class))).thenReturn(chatResponse);
        doReturn(generation).when(chatResponse).getResult();
        doReturn(assistantMessage).when(generation).getOutput();
        when(quizService.create(any(Quiz.class))).thenAnswer(inv -> {
            Quiz q = inv.getArgument(0);
            q.setId("quiz-new-id");
            return true;
        });
        when(quizOptionService.create(anyString(), any(QuizOption.class))).thenReturn(true);

        Map<String, Object> result = aiQuizGeneratorTool.generateMiniQuiz(
                null, "共产党的初心使命", "single_choice", "easy");

        assertThat(result).containsEntry("question", "中国共产党的初心和使命是什么？");
        assertThat(result).containsEntry("questionType", "single_choice");
        assertThat(result).containsEntry("difficulty", "easy");
        assertThat(result).containsKey("quizId");
        assertThat(result).containsKey("options");

        verify(quizService).create(quizCaptor.capture());
        Quiz savedQuiz = quizCaptor.getValue();
        assertThat(savedQuiz.getQuestion()).isEqualTo("中国共产党的初心和使命是什么？");
        assertThat(savedQuiz.getQuestionType()).isEqualTo("single_choice");
        assertThat(savedQuiz.getDifficulty()).isEqualTo("easy");
        assertThat(savedQuiz.getIsActive()).isTrue();
    }

    @Test
    @DisplayName("generateMiniQuiz 无 chapterId 和 topic 时抛出 BusinessException")
    void generateMiniQuizNoInput() {
        assertThatThrownBy(() -> aiQuizGeneratorTool.generateMiniQuiz(
                null, null, "single_choice", "easy"))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("必须提供章节ID或主题");
    }

    @Test
    @DisplayName("generateMiniQuiz 基于章节内容生成题目")
    void generateMiniQuizWithChapter() throws Exception {
        ChapterVO chapter = ChapterVO.builder()
                .id("ch-1")
                .title("第一章")
                .description("章节描述")
                .build();

        ContentBlockVO block = new ContentBlockVO();
        Field textField = ContentBlockVO.class.getDeclaredField("textContent");
        textField.setAccessible(true);
        textField.set(block, "章节内容文本");

        ChatModel chatModel = mock(ChatModel.class);
        ChatResponse chatResponse = mock(ChatResponse.class);
        Generation generation = mock(Generation.class);
        AssistantMessage assistantMessage = new AssistantMessage("""
                {"question": "测试题", "explanation": "解析", "options": []}
                """);

        when(chapterService.get("ch-1")).thenReturn(chapter);
        when(contentBlockService.getByParentId("ch-1")).thenReturn(List.of(block));
        when(chatModelProvider.getObject()).thenReturn(chatModel);
        when(chatModel.call(any(Prompt.class))).thenReturn(chatResponse);
        doReturn(generation).when(chatResponse).getResult();
        doReturn(assistantMessage).when(generation).getOutput();
        when(quizService.create(any(Quiz.class))).thenAnswer(inv -> {
            Quiz q = inv.getArgument(0);
            q.setId("quiz-ch-1");
            return true;
        });

        Map<String, Object> result = aiQuizGeneratorTool.generateMiniQuiz(
                "ch-1", null, "multiple_choice", "medium");

        assertThat(result).containsEntry("question", "测试题");
        assertThat(result).containsEntry("questionType", "multiple_choice");
        assertThat(result).containsEntry("difficulty", "medium");
    }

    @Test
    @DisplayName("generateMiniQuiz 章节不存在时抛出 BusinessException")
    void generateMiniQuizChapterNotFound() {
        when(chapterService.get("ch-1")).thenReturn(null);

        assertThatThrownBy(() -> aiQuizGeneratorTool.generateMiniQuiz(
                "ch-1", null, "single_choice", "easy"))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("章节不存在");
    }

    @Test
    @DisplayName("generateMiniQuiz AI 返回空题目时抛出 BusinessException")
    void generateMiniQuizEmptyQuestion() {
        ChatModel chatModel = mock(ChatModel.class);
        ChatResponse chatResponse = mock(ChatResponse.class);
        Generation generation = mock(Generation.class);
        AssistantMessage assistantMessage = new AssistantMessage("""
                {"question": "", "explanation": "", "options": []}
                """);

        when(chatModelProvider.getObject()).thenReturn(chatModel);
        when(chatModel.call(any(Prompt.class))).thenReturn(chatResponse);
        doReturn(generation).when(chatResponse).getResult();
        doReturn(assistantMessage).when(generation).getOutput();

        assertThatThrownBy(() -> aiQuizGeneratorTool.generateMiniQuiz(
                null, "主题", "single_choice", "easy"))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("AI生成的题目内容为空");
    }

    @Test
    @DisplayName("generateMiniQuiz 默认使用 single_choice 和 medium 难度")
    void generateMiniQuizDefaults() {
        ChatModel chatModel = mock(ChatModel.class);
        ChatResponse chatResponse = mock(ChatResponse.class);
        Generation generation = mock(Generation.class);
        AssistantMessage assistantMessage = new AssistantMessage("""
                {"question": "题", "explanation": "解析", "options": []}
                """);

        when(chatModelProvider.getObject()).thenReturn(chatModel);
        when(chatModel.call(any(Prompt.class))).thenReturn(chatResponse);
        doReturn(generation).when(chatResponse).getResult();
        doReturn(assistantMessage).when(generation).getOutput();
        when(quizService.create(any(Quiz.class))).thenAnswer(inv -> {
            Quiz q = inv.getArgument(0);
            q.setId("quiz-id");
            return true;
        });

        Map<String, Object> result = aiQuizGeneratorTool.generateMiniQuiz(
                null, "主题", null, null);

        assertThat(result).containsEntry("questionType", "single_choice");
        assertThat(result).containsEntry("difficulty", "medium");
    }
}
