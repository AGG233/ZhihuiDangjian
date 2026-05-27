package com.rauio.smartdangjian.server.ai.tool;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.lang.reflect.Field;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.model.Generation;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.beans.factory.ObjectProvider;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.rauio.smartdangjian.exception.BusinessException;
import com.rauio.smartdangjian.server.content.pojo.response.ChapterResponse;
import com.rauio.smartdangjian.server.content.pojo.response.ContentBlockResponse;
import com.rauio.smartdangjian.server.content.service.ChapterContentBlockService;
import com.rauio.smartdangjian.server.content.service.chapter.ChapterService;
import com.rauio.smartdangjian.server.quiz.pojo.entity.Quiz;
import com.rauio.smartdangjian.server.quiz.pojo.entity.QuizOption;
import com.rauio.smartdangjian.server.quiz.service.QuizOptionService;
import com.rauio.smartdangjian.server.quiz.service.QuizService;

@ExtendWith(MockitoExtension.class)
class AiQuizGeneratorToolTest {

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Mock
    private ObjectProvider<ChatModel> chatModelProvider;

    @Mock
    private ChapterService chapterService;

    @Mock
    private ChapterContentBlockService contentBlockService;

    @Mock
    private QuizService quizService;

    @Mock
    private QuizOptionService quizOptionService;

    private AiQuizGeneratorTool aiQuizGeneratorTool;

    @Captor
    private ArgumentCaptor<Quiz> quizCaptor;

    @Captor
    private ArgumentCaptor<Prompt> promptCaptor;

    @BeforeEach
    void setUp() {
        aiQuizGeneratorTool = new AiQuizGeneratorTool(
                chatModelProvider, chapterService, contentBlockService, quizService, quizOptionService, objectMapper);
    }

    @Test
    @DisplayName("generateMiniQuiz 基于主题生成题目并保存")
    void generateMiniQuizWithTopic() {
        ChatModel chatModel = mock(ChatModel.class);
        ChatResponse chatResponse = mock(ChatResponse.class);
        Generation generation = mock(Generation.class);
        AssistantMessage assistantMessage = new AssistantMessage(
                """
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
            q.setId(1L);
            return true;
        });
        when(quizOptionService.create(anyLong(), any(QuizOption.class))).thenReturn(true);

        Map<String, Object> result = aiQuizGeneratorTool.generateMiniQuiz(null, "共产党的初心使命", "single_choice", "easy");

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
        assertThatThrownBy(() -> aiQuizGeneratorTool.generateMiniQuiz(null, null, "single_choice", "easy"))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("必须提供章节ID或主题");
    }

    @Test
    @DisplayName("generateMiniQuiz 基于章节内容生成题目")
    void generateMiniQuizWithChapter() throws Exception {
        ChapterResponse chapter =
                ChapterResponse.builder().id(1L).title("第一章").description("章节描述").build();

        ContentBlockResponse block = new ContentBlockResponse();
        Field textField = ContentBlockResponse.class.getDeclaredField("textContent");
        textField.setAccessible(true); // NOSONAR
        textField.set(block, "章节内容文本");

        ChatModel chatModel = mock(ChatModel.class);
        ChatResponse chatResponse = mock(ChatResponse.class);
        Generation generation = mock(Generation.class);
        AssistantMessage assistantMessage = new AssistantMessage(
                """
                {"question": "测试题", "explanation": "解析", "options": []}
                """);

        when(chapterService.get(1L)).thenReturn(chapter);
        when(contentBlockService.getByChapterId(1L)).thenReturn(List.of(block));
        when(chatModelProvider.getObject()).thenReturn(chatModel);
        when(chatModel.call(any(Prompt.class))).thenReturn(chatResponse);
        doReturn(generation).when(chatResponse).getResult();
        doReturn(assistantMessage).when(generation).getOutput();
        when(quizService.create(any(Quiz.class))).thenAnswer(inv -> {
            Quiz q = inv.getArgument(0);
            q.setId(1L);
            return true;
        });

        Map<String, Object> result = aiQuizGeneratorTool.generateMiniQuiz("1", null, "multiple_choice", "medium");

        assertThat(result).containsEntry("question", "测试题");
        assertThat(result).containsEntry("questionType", "multiple_choice");
        assertThat(result).containsEntry("difficulty", "medium");
    }

    @Test
    @DisplayName("generateMiniQuiz 章节不存在时抛出 BusinessException")
    void generateMiniQuizChapterNotFound() {
        when(chapterService.get(1L)).thenReturn(null);

        assertThatThrownBy(() -> aiQuizGeneratorTool.generateMiniQuiz("1", null, "single_choice", "easy"))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("章节不存在");
    }

    @Test
    @DisplayName("generateMiniQuiz AI 返回空题目时抛出 BusinessException")
    void generateMiniQuizEmptyQuestion() {
        ChatModel chatModel = mock(ChatModel.class);
        ChatResponse chatResponse = mock(ChatResponse.class);
        Generation generation = mock(Generation.class);
        AssistantMessage assistantMessage = new AssistantMessage(
                """
                {"question": "", "explanation": "", "options": []}
                """);

        when(chatModelProvider.getObject()).thenReturn(chatModel);
        when(chatModel.call(any(Prompt.class))).thenReturn(chatResponse);
        doReturn(generation).when(chatResponse).getResult();
        doReturn(assistantMessage).when(generation).getOutput();

        assertThatThrownBy(() -> aiQuizGeneratorTool.generateMiniQuiz(null, "主题", "single_choice", "easy"))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("AI生成的题目内容为空");
    }

    @Test
    @DisplayName("generateMiniQuiz 默认使用 single_choice 和 medium 难度")
    void generateMiniQuizDefaults() {
        ChatModel chatModel = mock(ChatModel.class);
        ChatResponse chatResponse = mock(ChatResponse.class);
        Generation generation = mock(Generation.class);
        AssistantMessage assistantMessage = new AssistantMessage(
                """
                {"question": "题", "explanation": "解析", "options": []}
                """);

        when(chatModelProvider.getObject()).thenReturn(chatModel);
        when(chatModel.call(any(Prompt.class))).thenReturn(chatResponse);
        doReturn(generation).when(chatResponse).getResult();
        doReturn(assistantMessage).when(generation).getOutput();
        when(quizService.create(any(Quiz.class))).thenAnswer(inv -> {
            Quiz q = inv.getArgument(0);
            q.setId(1L);
            return true;
        });

        Map<String, Object> result = aiQuizGeneratorTool.generateMiniQuiz(null, "主题", null, null);

        assertThat(result).containsEntry("questionType", "single_choice");
        assertThat(result).containsEntry("difficulty", "medium");
    }

    @Test
    @DisplayName("generateMiniQuiz LLM 调用失败时抛出 BusinessException")
    void generateMiniQuizLlmCallFails() {
        when(chatModelProvider.getObject()).thenThrow(new RuntimeException("API error"));

        assertThatThrownBy(() -> aiQuizGeneratorTool.generateMiniQuiz(null, "主题", "single_choice", "easy"))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("AI生成题目失败");
    }

    @Test
    @DisplayName("generateMiniQuiz AI 返回 JSON 解析失败时抛出 BusinessException")
    void generateMiniQuizJsonParseFails() {
        ChatModel chatModel = mock(ChatModel.class);
        ChatResponse chatResponse = mock(ChatResponse.class);
        Generation generation = mock(Generation.class);
        AssistantMessage assistantMessage = new AssistantMessage("这不是JSON，完全无法解析");

        when(chatModelProvider.getObject()).thenReturn(chatModel);
        when(chatModel.call(any(Prompt.class))).thenReturn(chatResponse);
        doReturn(generation).when(chatResponse).getResult();
        doReturn(assistantMessage).when(generation).getOutput();

        assertThatThrownBy(() -> aiQuizGeneratorTool.generateMiniQuiz(null, "主题", "single_choice", "easy"))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("AI返回内容解析失败");
    }

    @Test
    @DisplayName("generateMiniQuiz AI 返回带 Markdown 代码块的 JSON 时能正确解析")
    void generateMiniQuizWithMarkdownCodeBlock() {
        ChatModel chatModel = mock(ChatModel.class);
        ChatResponse chatResponse = mock(ChatResponse.class);
        Generation generation = mock(Generation.class);
        AssistantMessage assistantMessage = new AssistantMessage(
                """
                ```json
                {"question": "Markdown题", "explanation": "解析", "options": []}
                ```
                """);

        when(chatModelProvider.getObject()).thenReturn(chatModel);
        when(chatModel.call(any(Prompt.class))).thenReturn(chatResponse);
        doReturn(generation).when(chatResponse).getResult();
        doReturn(assistantMessage).when(generation).getOutput();
        when(quizService.create(any(Quiz.class))).thenAnswer(inv -> {
            Quiz q = inv.getArgument(0);
            q.setId(1L);
            return true;
        });

        Map<String, Object> result = aiQuizGeneratorTool.generateMiniQuiz(null, "主题", "single_choice", "medium");

        assertThat(result).containsEntry("question", "Markdown题");
    }

    @Test
    @DisplayName("generateMiniQuiz 测验保存失败时抛出 BusinessException")
    void generateMiniQuizSaveFails() {
        ChatModel chatModel = mock(ChatModel.class);
        ChatResponse chatResponse = mock(ChatResponse.class);
        Generation generation = mock(Generation.class);
        AssistantMessage assistantMessage = new AssistantMessage(
                """
                {"question": "保存失败题", "explanation": "解析", "options": []}
                """);

        when(chatModelProvider.getObject()).thenReturn(chatModel);
        when(chatModel.call(any(Prompt.class))).thenReturn(chatResponse);
        doReturn(generation).when(chatResponse).getResult();
        doReturn(assistantMessage).when(generation).getOutput();
        when(quizService.create(any(Quiz.class))).thenReturn(false);

        assertThatThrownBy(() -> aiQuizGeneratorTool.generateMiniQuiz(null, "主题", "single_choice", "easy"))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("测验保存失败");
    }

    @Test
    @DisplayName("generateMiniQuiz 测验保存后 ID 为 null 时抛出 BusinessException")
    void generateMiniQuizSaveIdNull() {
        ChatModel chatModel = mock(ChatModel.class);
        ChatResponse chatResponse = mock(ChatResponse.class);
        Generation generation = mock(Generation.class);
        AssistantMessage assistantMessage = new AssistantMessage(
                """
                {"question": "ID为null", "explanation": "", "options": []}
                """);

        when(chatModelProvider.getObject()).thenReturn(chatModel);
        when(chatModel.call(any(Prompt.class))).thenReturn(chatResponse);
        doReturn(generation).when(chatResponse).getResult();
        doReturn(assistantMessage).when(generation).getOutput();
        when(quizService.create(any(Quiz.class))).thenReturn(true);

        assertThatThrownBy(() -> aiQuizGeneratorTool.generateMiniQuiz(null, "主题", "single_choice", "easy"))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("测验保存失败");
    }

    @Test
    @DisplayName("generateMiniQuiz 选项创建失败时抛出 BusinessException")
    void generateMiniQuizOptionSaveFails() {
        ChatModel chatModel = mock(ChatModel.class);
        ChatResponse chatResponse = mock(ChatResponse.class);
        Generation generation = mock(Generation.class);
        AssistantMessage assistantMessage = new AssistantMessage(
                """
                {
                  "question": "选项创建失败题",
                  "explanation": "解析",
                  "options": [
                    {"optionText": "正确选项", "isCorrect": true, "orderIndex": "A"},
                    {"optionText": "错误选项", "isCorrect": false, "orderIndex": "B"}
                  ]
                }
                """);

        when(chatModelProvider.getObject()).thenReturn(chatModel);
        when(chatModel.call(any(Prompt.class))).thenReturn(chatResponse);
        doReturn(generation).when(chatResponse).getResult();
        doReturn(assistantMessage).when(generation).getOutput();
        when(quizService.create(any(Quiz.class))).thenAnswer(inv -> {
            Quiz q = inv.getArgument(0);
            q.setId(1L);
            return true;
        });
        when(quizOptionService.create(anyLong(), any(QuizOption.class))).thenReturn(false);

        assertThatThrownBy(() -> aiQuizGeneratorTool.generateMiniQuiz(null, "主题", "single_choice", "easy"))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("选项创建失败");
    }

    @Test
    @DisplayName("generateMiniQuiz 选项节点为 null 时跳过选项创建")
    void generateMiniQuizOptionsNull() {
        ChatModel chatModel = mock(ChatModel.class);
        ChatResponse chatResponse = mock(ChatResponse.class);
        Generation generation = mock(Generation.class);
        AssistantMessage assistantMessage = new AssistantMessage(
                """
                {"question": "无选项题", "explanation": "解析"}
                """);

        when(chatModelProvider.getObject()).thenReturn(chatModel);
        when(chatModel.call(any(Prompt.class))).thenReturn(chatResponse);
        doReturn(generation).when(chatResponse).getResult();
        doReturn(assistantMessage).when(generation).getOutput();
        when(quizService.create(any(Quiz.class))).thenAnswer(inv -> {
            Quiz q = inv.getArgument(0);
            q.setId(1L);
            return true;
        });

        Map<String, Object> result = aiQuizGeneratorTool.generateMiniQuiz(null, "主题", "single_choice", "easy");

        assertThat(result).containsEntry("question", "无选项题");
        assertThat(result.get("options")).isNotNull();
        assertThat((List<?>) result.get("options")).isEmpty();
    }

    @Test
    @DisplayName("generateMiniQuiz 选项文本为空时跳过该选项")
    void generateMiniQuizSkipsBlankOptionText() {
        ChatModel chatModel = mock(ChatModel.class);
        ChatResponse chatResponse = mock(ChatResponse.class);
        Generation generation = mock(Generation.class);
        AssistantMessage assistantMessage = new AssistantMessage(
                """
                {
                  "question": "跳过空选项",
                  "explanation": "解析",
                  "options": [
                    {"optionText": "有效选项", "isCorrect": true, "orderIndex": "A"},
                    {"optionText": "", "isCorrect": true, "orderIndex": "B"},
                    {"optionText": "   ", "isCorrect": false, "orderIndex": "C"}
                  ]
                }
                """);

        when(chatModelProvider.getObject()).thenReturn(chatModel);
        when(chatModel.call(any(Prompt.class))).thenReturn(chatResponse);
        doReturn(generation).when(chatResponse).getResult();
        doReturn(assistantMessage).when(generation).getOutput();
        when(quizService.create(any(Quiz.class))).thenAnswer(inv -> {
            Quiz q = inv.getArgument(0);
            q.setId(1L);
            return true;
        });
        when(quizOptionService.create(anyLong(), any(QuizOption.class))).thenReturn(true);

        Map<String, Object> result = aiQuizGeneratorTool.generateMiniQuiz(null, "主题", "single_choice", "easy");

        assertThat(result).containsEntry("question", "跳过空选项");
        @SuppressWarnings("unchecked")
        List<Map<String, Object>> options = (List<Map<String, Object>>) result.get("options");
        assertThat(options).hasSize(1);
        assertThat(options.get(0)).containsEntry("optionText", "有效选项");
    }

    @Test
    @DisplayName("generateMiniQuiz 章节有空白描述时仍可正常生成")
    void generateMiniQuizWithChapterNullDescription() throws Exception {
        ChapterResponse chapter = ChapterResponse.builder()
                .id(1L).title("无描述章节").description(null).build();

        ContentBlockResponse block = new ContentBlockResponse();
        block.setTextContent("章节内容");

        ChatModel chatModel = mock(ChatModel.class);
        ChatResponse chatResponse = mock(ChatResponse.class);
        Generation generation = mock(Generation.class);
        AssistantMessage assistantMessage = new AssistantMessage(
                """
                {"question": "测试题", "explanation": "解析", "options": []}
                """);

        when(chapterService.get(1L)).thenReturn(chapter);
        when(contentBlockService.getByChapterId(1L)).thenReturn(List.of(block));
        when(chatModelProvider.getObject()).thenReturn(chatModel);
        when(chatModel.call(any(Prompt.class))).thenReturn(chatResponse);
        doReturn(generation).when(chatResponse).getResult();
        doReturn(assistantMessage).when(generation).getOutput();
        when(quizService.create(any(Quiz.class))).thenAnswer(inv -> {
            Quiz q = inv.getArgument(0);
            q.setId(1L);
            return true;
        });

        Map<String, Object> result = aiQuizGeneratorTool.generateMiniQuiz("1", null, "single_choice", "easy");

        assertThat(result).containsEntry("question", "测试题");
    }

    @Test
    @DisplayName("generateMiniQuiz 章节内容块为空列表时仍可正常生成")
    void generateMiniQuizWithEmptyBlocks() throws Exception {
        ChapterResponse chapter = ChapterResponse.builder()
                .id(1L).title("空内容章节").description("描述").build();

        when(chapterService.get(1L)).thenReturn(chapter);
        when(contentBlockService.getByChapterId(1L)).thenReturn(List.of());

        ChatModel chatModel = mock(ChatModel.class);
        ChatResponse chatResponse = mock(ChatResponse.class);
        Generation generation = mock(Generation.class);
        AssistantMessage assistantMessage = new AssistantMessage(
                """
                {"question": "空块题", "explanation": "解析", "options": []}
                """);

        when(chatModelProvider.getObject()).thenReturn(chatModel);
        when(chatModel.call(any(Prompt.class))).thenReturn(chatResponse);
        doReturn(generation).when(chatResponse).getResult();
        doReturn(assistantMessage).when(generation).getOutput();
        when(quizService.create(any(Quiz.class))).thenAnswer(inv -> {
            Quiz q = inv.getArgument(0);
            q.setId(1L);
            return true;
        });

        Map<String, Object> result = aiQuizGeneratorTool.generateMiniQuiz("1", null, "true_false", "hard");

        assertThat(result).containsEntry("question", "空块题");
        assertThat(result).containsEntry("questionType", "true_false");
        assertThat(result).containsEntry("difficulty", "hard");
    }

    @Test
    @DisplayName("generateMiniQuiz 内容块 textContent 为空时跳过该块")
    void generateMiniQuizWithBlankBlockText() throws Exception {
        ChapterResponse chapter = ChapterResponse.builder()
                .id(1L).title("空文本块章节").description("描述").build();
        ContentBlockResponse block = new ContentBlockResponse();
        block.setTextContent("");

        when(chapterService.get(1L)).thenReturn(chapter);
        when(contentBlockService.getByChapterId(1L)).thenReturn(List.of(block));

        ChatModel chatModel = mock(ChatModel.class);
        ChatResponse chatResponse = mock(ChatResponse.class);
        Generation generation = mock(Generation.class);
        AssistantMessage assistantMessage = new AssistantMessage(
                """
                {"question": "空文本题", "explanation": "解析", "options": []}
                """);

        when(chatModelProvider.getObject()).thenReturn(chatModel);
        when(chatModel.call(any(Prompt.class))).thenReturn(chatResponse);
        doReturn(generation).when(chatResponse).getResult();
        doReturn(assistantMessage).when(generation).getOutput();
        when(quizService.create(any(Quiz.class))).thenAnswer(inv -> {
            Quiz q = inv.getArgument(0);
            q.setId(1L);
            return true;
        });

        Map<String, Object> result = aiQuizGeneratorTool.generateMiniQuiz("1", null, "single_choice", "easy");

        assertThat(result).containsEntry("question", "空文本题");
    }

    @Test
    @DisplayName("generateMiniQuiz AI 返回纯 ``` 代码块时能正确清理并解析")
    void generateMiniQuizWithPlainCodeBlock() {
        ChatModel chatModel = mock(ChatModel.class);
        ChatResponse chatResponse = mock(ChatResponse.class);
        Generation generation = mock(Generation.class);
        AssistantMessage assistantMessage = new AssistantMessage(
                """
                ```
                {"question": "纯代码块题", "explanation": "解析", "options": []}
                ```
                """);

        when(chatModelProvider.getObject()).thenReturn(chatModel);
        when(chatModel.call(any(Prompt.class))).thenReturn(chatResponse);
        doReturn(generation).when(chatResponse).getResult();
        doReturn(assistantMessage).when(generation).getOutput();
        when(quizService.create(any(Quiz.class))).thenAnswer(inv -> {
            Quiz q = inv.getArgument(0);
            q.setId(1L);
            return true;
        });

        Map<String, Object> result = aiQuizGeneratorTool.generateMiniQuiz(null, "主题", "single_choice", "easy");

        assertThat(result).containsEntry("question", "纯代码块题");
    }

    @Test
    @DisplayName("generateMiniQuiz AI 返回 JSON 中缺少 question 字段时抛出 BusinessException")
    void generateMiniQuizMissingQuestionField() {
        ChatModel chatModel = mock(ChatModel.class);
        ChatResponse chatResponse = mock(ChatResponse.class);
        Generation generation = mock(Generation.class);
        AssistantMessage assistantMessage = new AssistantMessage(
                """
                {"explanation": "解析", "options": []}
                """);

        when(chatModelProvider.getObject()).thenReturn(chatModel);
        when(chatModel.call(any(Prompt.class))).thenReturn(chatResponse);
        doReturn(generation).when(chatResponse).getResult();
        doReturn(assistantMessage).when(generation).getOutput();

        assertThatThrownBy(() -> aiQuizGeneratorTool.generateMiniQuiz(null, "主题", "single_choice", "easy"))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("AI生成的题目内容为空");
    }

    @Test
    @DisplayName("generateMiniQuiz 选项中缺少 isCorrect 字段时默认为 false")
    void generateMiniQuizOptionWithoutIsCorrect() {
        ChatModel chatModel = mock(ChatModel.class);
        ChatResponse chatResponse = mock(ChatResponse.class);
        Generation generation = mock(Generation.class);
        AssistantMessage assistantMessage = new AssistantMessage(
                """
                {
                  "question": "选项缺isCorrect",
                  "explanation": "解析",
                  "options": [
                    {"optionText": "选项A", "orderIndex": "A"},
                    {"optionText": "选项B", "orderIndex": "B"}
                  ]
                }
                """);

        when(chatModelProvider.getObject()).thenReturn(chatModel);
        when(chatModel.call(any(Prompt.class))).thenReturn(chatResponse);
        doReturn(generation).when(chatResponse).getResult();
        doReturn(assistantMessage).when(generation).getOutput();
        when(quizService.create(any(Quiz.class))).thenAnswer(inv -> {
            Quiz q = inv.getArgument(0);
            q.setId(1L);
            return true;
        });
        when(quizOptionService.create(anyLong(), any(QuizOption.class))).thenReturn(true);

        Map<String, Object> result = aiQuizGeneratorTool.generateMiniQuiz(null, "主题", "single_choice", "easy");

        assertThat(result).containsEntry("question", "选项缺isCorrect");
        @SuppressWarnings("unchecked")
        List<Map<String, Object>> options = (List<Map<String, Object>>) result.get("options");
        assertThat(options).hasSize(2);
    }

    @Test
    @DisplayName("cleanJsonResponse 处理空响应时抛出 BusinessException")
    void cleanJsonResponseHandlesEmptyResponse() throws Exception {
        ChatModel chatModel = mock(ChatModel.class);
        ChatResponse chatResponse = mock(ChatResponse.class);
        Generation generation = mock(Generation.class);
        // Empty string response: cleanJsonResponse returns "", readTree("") fails
        AssistantMessage assistantMessage = new AssistantMessage("");

        when(chatModelProvider.getObject()).thenReturn(chatModel);
        when(chatModel.call(any(Prompt.class))).thenReturn(chatResponse);
        doReturn(generation).when(chatResponse).getResult();
        doReturn(assistantMessage).when(generation).getOutput();

        assertThatThrownBy(() -> aiQuizGeneratorTool.generateMiniQuiz(null, "主题", "single_choice", "easy"))
                .isInstanceOf(BusinessException.class);
    }

    @Test
    @DisplayName("generateMiniQuiz 内容块列表为 null 时跳过内容拼装")
    void generateMiniQuizWithNullBlocks() throws Exception {
        ChapterResponse chapter = ChapterResponse.builder()
                .id(1L).title("null块章节").description("描述").build();

        when(chapterService.get(1L)).thenReturn(chapter);
        when(contentBlockService.getByChapterId(1L)).thenReturn(null);

        ChatModel chatModel = mock(ChatModel.class);
        ChatResponse chatResponse = mock(ChatResponse.class);
        Generation generation = mock(Generation.class);
        AssistantMessage assistantMessage = new AssistantMessage(
                """
                {"question": "null块题", "explanation": "解析", "options": []}
                """);

        when(chatModelProvider.getObject()).thenReturn(chatModel);
        when(chatModel.call(any(Prompt.class))).thenReturn(chatResponse);
        doReturn(generation).when(chatResponse).getResult();
        doReturn(assistantMessage).when(generation).getOutput();
        when(quizService.create(any(Quiz.class))).thenAnswer(inv -> {
            Quiz q = inv.getArgument(0);
            q.setId(1L);
            return true;
        });

        Map<String, Object> result = aiQuizGeneratorTool.generateMiniQuiz("1", null, "single_choice", "easy");

        assertThat(result).containsEntry("question", "null块题");
    }

    @Test
    @DisplayName("generateMiniQuiz getFieldValue 返回 null 时跳过该文本块")
    void generateMiniQuizWithNullFieldValue() throws Exception {
        ChapterResponse chapter = ChapterResponse.builder()
                .id(1L).title("null字段值章节").description("描述").build();

        // ContentBlockResponse 中 textContent 字段不存在 → getFieldValue 返回 null
        ContentBlockResponse block = new ContentBlockResponse();

        when(chapterService.get(1L)).thenReturn(chapter);
        when(contentBlockService.getByChapterId(1L)).thenReturn(List.of(block));

        ChatModel chatModel = mock(ChatModel.class);
        ChatResponse chatResponse = mock(ChatResponse.class);
        Generation generation = mock(Generation.class);
        AssistantMessage assistantMessage = new AssistantMessage(
                """
                {"question": "null字段题", "explanation": "解析", "options": []}
                """);

        when(chatModelProvider.getObject()).thenReturn(chatModel);
        when(chatModel.call(any(Prompt.class))).thenReturn(chatResponse);
        doReturn(generation).when(chatResponse).getResult();
        doReturn(assistantMessage).when(generation).getOutput();
        when(quizService.create(any(Quiz.class))).thenAnswer(inv -> {
            Quiz q = inv.getArgument(0);
            q.setId(1L);
            return true;
        });

        Map<String, Object> result = aiQuizGeneratorTool.generateMiniQuiz("1", null, "single_choice", "easy");

        assertThat(result).containsEntry("question", "null字段题");
    }

    @Test
    @DisplayName("generateMiniQuiz AI 返回带多余换行和空白的 JSON 能正常解析")
    void generateMiniQuizWithExtraWhitespace() {
        ChatModel chatModel = mock(ChatModel.class);
        ChatResponse chatResponse = mock(ChatResponse.class);
        Generation generation = mock(Generation.class);
        AssistantMessage assistantMessage = new AssistantMessage(
                """
                   {"question": "空白题", "explanation": "解析", "options": []}
                """);

        when(chatModelProvider.getObject()).thenReturn(chatModel);
        when(chatModel.call(any(Prompt.class))).thenReturn(chatResponse);
        doReturn(generation).when(chatResponse).getResult();
        doReturn(assistantMessage).when(generation).getOutput();
        when(quizService.create(any(Quiz.class))).thenAnswer(inv -> {
            Quiz q = inv.getArgument(0);
            q.setId(1L);
            return true;
        });

        Map<String, Object> result = aiQuizGeneratorTool.generateMiniQuiz(null, "主题", "true_false", "hard");

        assertThat(result).containsEntry("question", "空白题");
        assertThat(result).containsEntry("questionType", "true_false");
        assertThat(result).containsEntry("difficulty", "hard");
    }

    @Test
    @DisplayName("generateMiniQuiz AI 返回 null 文本时抛出 BusinessException")
    void generateMiniQuizNullTextResponse() {
        ChatModel chatModel = mock(ChatModel.class);
        ChatResponse chatResponse = mock(ChatResponse.class);
        Generation generation = mock(Generation.class);
        AssistantMessage assistantMessage = mock(AssistantMessage.class);
        when(assistantMessage.getText()).thenReturn(null);

        when(chatModelProvider.getObject()).thenReturn(chatModel);
        when(chatModel.call(any(Prompt.class))).thenReturn(chatResponse);
        doReturn(generation).when(chatResponse).getResult();
        doReturn(assistantMessage).when(generation).getOutput();

        assertThatThrownBy(() -> aiQuizGeneratorTool.generateMiniQuiz(null, "主题", "single_choice", "easy"))
                .isInstanceOf(BusinessException.class);
    }

    @Test
    @DisplayName("generateMiniQuiz 空白 questionType 时默认 single_choice")
    void generateMiniQuizWithBlankQuestionType() {
        ChatModel chatModel = mock(ChatModel.class);
        ChatResponse chatResponse = mock(ChatResponse.class);
        Generation generation = mock(Generation.class);
        AssistantMessage assistantMessage = new AssistantMessage(
                """
                {"question": "测试题", "explanation": "解析", "options": []}
                """);

        when(chatModelProvider.getObject()).thenReturn(chatModel);
        when(chatModel.call(any(Prompt.class))).thenReturn(chatResponse);
        doReturn(generation).when(chatResponse).getResult();
        doReturn(assistantMessage).when(generation).getOutput();
        when(quizService.create(any(Quiz.class))).thenAnswer(inv -> {
            Quiz q = inv.getArgument(0);
            q.setId(1L);
            return true;
        });

        Map<String, Object> result = aiQuizGeneratorTool.generateMiniQuiz(null, "主题", "", "medium");

        assertThat(result).containsEntry("questionType", "single_choice");
        assertThat(result).containsEntry("difficulty", "medium");
    }

    @Test
    @DisplayName("generateMiniQuiz 空白 difficulty 时默认 medium")
    void generateMiniQuizWithBlankDifficulty() {
        ChatModel chatModel = mock(ChatModel.class);
        ChatResponse chatResponse = mock(ChatResponse.class);
        Generation generation = mock(Generation.class);
        AssistantMessage assistantMessage = new AssistantMessage(
                """
                {"question": "测试题", "explanation": "解析", "options": []}
                """);

        when(chatModelProvider.getObject()).thenReturn(chatModel);
        when(chatModel.call(any(Prompt.class))).thenReturn(chatResponse);
        doReturn(generation).when(chatResponse).getResult();
        doReturn(assistantMessage).when(generation).getOutput();
        when(quizService.create(any(Quiz.class))).thenAnswer(inv -> {
            Quiz q = inv.getArgument(0);
            q.setId(1L);
            return true;
        });

        Map<String, Object> result = aiQuizGeneratorTool.generateMiniQuiz(null, "主题", "single_choice", "");

        assertThat(result).containsEntry("questionType", "single_choice");
        assertThat(result).containsEntry("difficulty", "medium");
    }

    @Test
    @DisplayName("generateMiniQuiz 选项 isCorrect 为 false 时传递 false")
    void generateMiniQuizOptionIsCorrectFalse() {
        ChatModel chatModel = mock(ChatModel.class);
        ChatResponse chatResponse = mock(ChatResponse.class);
        Generation generation = mock(Generation.class);
        AssistantMessage assistantMessage = new AssistantMessage(
                """
                {
                  "question": "isCorrect为false的选项",
                  "explanation": "解析",
                  "options": [
                    {"optionText": "选项A", "isCorrect": false, "orderIndex": "A"}
                  ]
                }
                """);

        when(chatModelProvider.getObject()).thenReturn(chatModel);
        when(chatModel.call(any(Prompt.class))).thenReturn(chatResponse);
        doReturn(generation).when(chatResponse).getResult();
        doReturn(assistantMessage).when(generation).getOutput();
        when(quizService.create(any(Quiz.class))).thenAnswer(inv -> {
            Quiz q = inv.getArgument(0);
            q.setId(1L);
            return true;
        });
        when(quizOptionService.create(anyLong(), any(QuizOption.class))).thenReturn(true);

        Map<String, Object> result = aiQuizGeneratorTool.generateMiniQuiz(null, "主题", "single_choice", "easy");

        assertThat(result).containsEntry("question", "isCorrect为false的选项");
        @SuppressWarnings("unchecked")
        List<Map<String, Object>> options = (List<Map<String, Object>>) result.get("options");
        assertThat(options).hasSize(1);
        assertThat(options.get(0)).containsEntry("isCorrect", false);
    }

    @Test
    @DisplayName("generateMiniQuiz translateQuestionType multiple_choice 时 prompt 包含多选")
    void translateQuestionTypeMultipleChoice() {
        ChatModel chatModel = mock(ChatModel.class);
        ChatResponse chatResponse = mock(ChatResponse.class);
        Generation generation = mock(Generation.class);
        AssistantMessage assistantMessage = new AssistantMessage(
                """
                {"question": "题", "explanation": "解析", "options": []}
                """);

        when(chatModelProvider.getObject()).thenReturn(chatModel);
        when(chatModel.call(any(Prompt.class))).thenReturn(chatResponse);
        doReturn(generation).when(chatResponse).getResult();
        doReturn(assistantMessage).when(generation).getOutput();
        when(quizService.create(any(Quiz.class))).thenAnswer(inv -> {
            Quiz q = inv.getArgument(0);
            q.setId(1L);
            return true;
        });

        aiQuizGeneratorTool.generateMiniQuiz(null, "主题", "multiple_choice", "easy");

        verify(chatModel).call(promptCaptor.capture());
        assertThat(promptCaptor.getValue().getContents()).contains("多选");
    }

    @Test
    @DisplayName("generateMiniQuiz translateQuestionType true_false 时 prompt 包含判断")
    void translateQuestionTypeTrueFalse() {
        ChatModel chatModel = mock(ChatModel.class);
        ChatResponse chatResponse = mock(ChatResponse.class);
        Generation generation = mock(Generation.class);
        AssistantMessage assistantMessage = new AssistantMessage(
                """
                {"question": "题", "explanation": "解析", "options": []}
                """);

        when(chatModelProvider.getObject()).thenReturn(chatModel);
        when(chatModel.call(any(Prompt.class))).thenReturn(chatResponse);
        doReturn(generation).when(chatResponse).getResult();
        doReturn(assistantMessage).when(generation).getOutput();
        when(quizService.create(any(Quiz.class))).thenAnswer(inv -> {
            Quiz q = inv.getArgument(0);
            q.setId(1L);
            return true;
        });

        aiQuizGeneratorTool.generateMiniQuiz(null, "主题", "true_false", "easy");

        verify(chatModel).call(promptCaptor.capture());
        assertThat(promptCaptor.getValue().getContents()).contains("判断");
    }

    @Test
    @DisplayName("generateMiniQuiz options 为非数组值时跳过选项创建")
    void generateMiniQuizNonArrayOptions() {
        ChatModel chatModel = mock(ChatModel.class);
        ChatResponse chatResponse = mock(ChatResponse.class);
        Generation generation = mock(Generation.class);
        AssistantMessage assistantMessage = new AssistantMessage(
                """
                {"question": "选项非数组", "explanation": "解析", "options": "not-an-array"}
                """);

        when(chatModelProvider.getObject()).thenReturn(chatModel);
        when(chatModel.call(any(Prompt.class))).thenReturn(chatResponse);
        doReturn(generation).when(chatResponse).getResult();
        doReturn(assistantMessage).when(generation).getOutput();
        when(quizService.create(any(Quiz.class))).thenAnswer(inv -> {
            Quiz q = inv.getArgument(0);
            q.setId(1L);
            return true;
        });

        Map<String, Object> result = aiQuizGeneratorTool.generateMiniQuiz(null, "主题", "single_choice", "easy");

        assertThat(result).containsEntry("question", "选项非数组");
        assertThat((List<?>) result.get("options")).isEmpty();
    }

    @Test
    @DisplayName("getFieldValue with non-existent field catches exception and returns null")
    void getFieldValueExceptionCaught() throws Exception {
        ContentBlockResponse block = new ContentBlockResponse();
        java.lang.reflect.Method method = AiQuizGeneratorTool.class.getDeclaredMethod(
                "getFieldValue", ContentBlockResponse.class, String.class);
        method.setAccessible(true); // NOSONAR
        Object result = method.invoke(aiQuizGeneratorTool, block, "nonExistentField");
        assertThat(result).isNull();
    }

    @Test
    @DisplayName("generateMiniQuiz 空白 chapterId 和有效 topic 时走 topic 路径")
    void generateMiniQuizWithBlankChapterIdAndValidTopic() {
        ChatModel chatModel = mock(ChatModel.class);
        ChatResponse chatResponse = mock(ChatResponse.class);
        Generation generation = mock(Generation.class);
        AssistantMessage assistantMessage = new AssistantMessage(
                """
                {"question": "空白章节题", "explanation": "解析", "options": []}
                """);

        when(chatModelProvider.getObject()).thenReturn(chatModel);
        when(chatModel.call(any(Prompt.class))).thenReturn(chatResponse);
        doReturn(generation).when(chatResponse).getResult();
        doReturn(assistantMessage).when(generation).getOutput();
        when(quizService.create(any(Quiz.class))).thenAnswer(inv -> {
            Quiz q = inv.getArgument(0);
            q.setId(1L);
            return true;
        });

        Map<String, Object> result = aiQuizGeneratorTool.generateMiniQuiz("", "有效主题", "single_choice", "easy");

        assertThat(result).containsEntry("question", "空白章节题");
    }

    @Test
    @DisplayName("generateMiniQuiz 空白 topic 且无 chapterId 时抛出 BusinessException")
    void generateMiniQuizWithBlankTopic() {
        assertThatThrownBy(() -> aiQuizGeneratorTool.generateMiniQuiz(null, "", "single_choice", "easy"))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("必须提供章节ID或主题");
    }

    @Test
    @DisplayName("generateMiniQuiz 含 null optionText 的选项被跳过")
    void generateMiniQuizSkipsNullOptionText() {
        ChatModel chatModel = mock(ChatModel.class);
        ChatResponse chatResponse = mock(ChatResponse.class);
        Generation generation = mock(Generation.class);
        AssistantMessage assistantMessage = new AssistantMessage(
                """
                {
                  "question": "跳过错题",
                  "explanation": "解析",
                  "options": [
                    {"optionText": null, "isCorrect": true, "orderIndex": "A"},
                    {"optionText": "有效选项", "isCorrect": false, "orderIndex": "B"}
                  ]
                }
                """);

        when(chatModelProvider.getObject()).thenReturn(chatModel);
        when(chatModel.call(any(Prompt.class))).thenReturn(chatResponse);
        doReturn(generation).when(chatResponse).getResult();
        doReturn(assistantMessage).when(generation).getOutput();
        when(quizService.create(any(Quiz.class))).thenAnswer(inv -> {
            Quiz q = inv.getArgument(0);
            q.setId(1L);
            return true;
        });
        when(quizOptionService.create(anyLong(), any(QuizOption.class))).thenReturn(true);

        Map<String, Object> result = aiQuizGeneratorTool.generateMiniQuiz(null, "主题", "single_choice", "easy");

        assertThat(result).containsEntry("question", "跳过错题");
        @SuppressWarnings("unchecked")
        List<Map<String, Object>> options = (List<Map<String, Object>>) result.get("options");
        assertThat(options).hasSize(1);
    }

    @Test
    @DisplayName("generateMiniQuiz options isCorrect true passed correctly")
    void generateMiniQuizOptionIsCorrectTrue() {
        ChatModel chatModel = mock(ChatModel.class);
        ChatResponse chatResponse = mock(ChatResponse.class);
        Generation generation = mock(Generation.class);
        AssistantMessage assistantMessage = new AssistantMessage(
                """
                {
                  "question": "isCorrect correct option",
                  "explanation": "explanation",
                  "options": [
                    {"optionText": "correct", "isCorrect": true, "orderIndex": "A"},
                    {"optionText": "wrong", "isCorrect": false, "orderIndex": "B"}
                  ]
                }
                """);

        when(chatModelProvider.getObject()).thenReturn(chatModel);
        when(chatModel.call(any(Prompt.class))).thenReturn(chatResponse);
        doReturn(generation).when(chatResponse).getResult();
        doReturn(assistantMessage).when(generation).getOutput();
        when(quizService.create(any(Quiz.class))).thenAnswer(inv -> {
            Quiz q = inv.getArgument(0);
            q.setId(1L);
            return true;
        });
        when(quizOptionService.create(anyLong(), any(QuizOption.class))).thenReturn(true);

        Map<String, Object> result = aiQuizGeneratorTool.generateMiniQuiz(null, "topic", "single_choice", "easy");

        assertThat(result).containsEntry("question", "isCorrect correct option");
        @SuppressWarnings("unchecked")
        List<Map<String, Object>> options = (List<Map<String, Object>>) result.get("options");
        assertThat(options).hasSize(2);
        assertThat(options.get(0)).containsEntry("isCorrect", true);
        assertThat(options.get(1)).containsEntry("isCorrect", false);
    }
}
