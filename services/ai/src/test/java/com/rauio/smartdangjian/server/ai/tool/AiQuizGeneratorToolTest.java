package com.rauio.smartdangjian.server.ai.tool;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

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
import com.rauio.smartdangjian.server.chapter.api.ChapterQueryFacade;
import com.rauio.smartdangjian.server.chapter.pojo.response.ChapterResponse;
import com.rauio.smartdangjian.server.content.api.ContentQueryFacade;
import com.rauio.smartdangjian.server.content.api.dto.ContentBlockSummary;
import com.rauio.smartdangjian.server.quiz.api.QuizDataFacade;

@ExtendWith(MockitoExtension.class)
class AiQuizGeneratorToolTest {

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Mock
    private ObjectProvider<ChatModel> chatModelProvider;

    @Mock
    private ChapterQueryFacade chapterQueryFacade;

    @Mock
    private ContentQueryFacade contentQueryFacade;

    @Mock
    private QuizDataFacade quizDataFacade;

    @Captor
    private ArgumentCaptor<Prompt> promptCaptor;

    @Captor
    private ArgumentCaptor<List<Map<String, Object>>> optionsCaptor;

    private AiQuizGeneratorTool tool;

    @BeforeEach
    void setUp() {
        tool = new AiQuizGeneratorTool(
                chatModelProvider, chapterQueryFacade, contentQueryFacade, quizDataFacade, objectMapper);
    }

    @Test
    @DisplayName("generateMiniQuiz 基于主题生成题目并通过 facade 保存")
    void generateMiniQuizWithTopic() {
        mockLlmResponse(
                """
                {
                  "question": "中国共产党的初心和使命是什么？",
                  "explanation": "为中国人民谋幸福，为中华民族谋复兴。",
                  "options": [
                    {"optionText": "为中国人民谋幸福", "isCorrect": true, "orderIndex": "A"},
                    {"optionText": "实现共同富裕", "isCorrect": false, "orderIndex": "B"}
                  ]
                }
                """);
        when(quizDataFacade.createQuiz(any(), any(), any(), any(), any(), any(), any()))
                .thenReturn(1L);

        Map<String, Object> result = tool.generateMiniQuiz(null, "共产党的初心使命", "single_choice", "easy");

        assertThat(result)
                .containsEntry("quizId", "1")
                .containsEntry("question", "中国共产党的初心和使命是什么？")
                .containsEntry("questionType", "single_choice")
                .containsEntry("difficulty", "easy");
        verify(quizDataFacade)
                .createQuiz(
                        eq(null),
                        eq("中国共产党的初心和使命是什么？"),
                        eq("single_choice"),
                        eq(5),
                        eq("easy"),
                        eq("为中国人民谋幸福，为中华民族谋复兴。"),
                        any());
    }

    @Test
    @DisplayName("generateMiniQuiz 基于章节内容生成题目")
    void generateMiniQuizWithChapter() {
        ChapterResponse chapter = ChapterResponse.builder()
                .id(1L)
                .title("第一章")
                .description("章节描述")
                .build();
        ContentBlockSummary block =
                ContentBlockSummary.builder().textContent("章节内容文本").build();
        when(chapterQueryFacade.get(1L)).thenReturn(chapter);
        when(contentQueryFacade.getByChapterId(1L)).thenReturn(List.of(block));
        mockLlmResponse(
                """
                {"question": "测试题", "explanation": "解析", "options": []}
                """);
        when(quizDataFacade.createQuiz(any(), any(), any(), any(), any(), any(), any()))
                .thenReturn(2L);

        Map<String, Object> result = tool.generateMiniQuiz("1", null, "multiple_choice", "medium");

        assertThat(result)
                .containsEntry("quizId", "2")
                .containsEntry("question", "测试题")
                .containsEntry("questionType", "multiple_choice")
                .containsEntry("difficulty", "medium");
        verify(quizDataFacade)
                .createQuiz(eq(1L), eq("测试题"), eq("multiple_choice"), eq(10), eq("medium"), eq("解析"), any());
    }

    @Test
    @DisplayName("generateMiniQuiz 默认使用 single_choice 和 medium")
    void generateMiniQuizDefaults() {
        mockLlmResponse(
                """
                ```json
                {"question": "默认题", "explanation": "解析", "options": []}
                ```
                """);
        when(quizDataFacade.createQuiz(any(), any(), any(), any(), any(), any(), any()))
                .thenReturn(3L);

        Map<String, Object> result = tool.generateMiniQuiz(null, "主题", "", "");

        assertThat(result)
                .containsEntry("question", "默认题")
                .containsEntry("questionType", "single_choice")
                .containsEntry("difficulty", "medium");
    }

    @Test
    @DisplayName("generateMiniQuiz 无 chapterId 和 topic 时抛出 BusinessException")
    void generateMiniQuizNoInput() {
        assertThatThrownBy(() -> tool.generateMiniQuiz(null, " ", "single_choice", "easy"))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("必须提供章节ID或主题");
    }

    @Test
    @DisplayName("generateMiniQuiz 章节不存在时抛出 BusinessException")
    void generateMiniQuizChapterNotFound() {
        when(chapterQueryFacade.get(1L)).thenReturn(null);

        assertThatThrownBy(() -> tool.generateMiniQuiz("1", null, "single_choice", "easy"))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("章节不存在");
    }

    @Test
    @DisplayName("generateMiniQuiz AI 返回空题目时抛出 BusinessException")
    void generateMiniQuizEmptyQuestion() {
        mockLlmResponse("{\"question\": \"\", \"explanation\": \"\", \"options\": []}");

        assertThatThrownBy(() -> tool.generateMiniQuiz(null, "主题", "single_choice", "easy"))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("AI生成的题目内容为空");
    }

    @Test
    @DisplayName("generateMiniQuiz AI 返回非 JSON 时抛出 BusinessException")
    void generateMiniQuizJsonParseFails() {
        mockLlmResponse("这不是JSON，完全无法解析");

        assertThatThrownBy(() -> tool.generateMiniQuiz(null, "主题", "single_choice", "easy"))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("AI返回内容解析失败");
    }

    @Test
    @DisplayName("generateMiniQuiz LLM 调用失败时抛出 BusinessException")
    void generateMiniQuizLlmCallFails() {
        when(chatModelProvider.getObject()).thenThrow(new RuntimeException("API error"));

        assertThatThrownBy(() -> tool.generateMiniQuiz(null, "主题", "single_choice", "easy"))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("AI生成题目失败");
    }

    @Test
    @DisplayName("generateMiniQuiz AI 返回纯代码块时能正确清理并解析")
    void generateMiniQuizWithPlainCodeBlock() {
        mockLlmResponse(
                """
                ```
                {"question": "代码块题", "explanation": "解析", "options": []}
                ```
                """);
        when(quizDataFacade.createQuiz(any(), any(), any(), any(), any(), any(), any()))
                .thenReturn(5L);

        Map<String, Object> result = tool.generateMiniQuiz(null, "主题", "single_choice", "medium");

        assertThat(result).containsEntry("question", "代码块题");
    }

    @Test
    @DisplayName("generateMiniQuiz 选项文本为空时跳过该选项")
    void generateMiniQuizSkipsBlankOptionText() {
        mockLlmResponse(
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
        when(quizDataFacade.createQuiz(any(), any(), any(), any(), any(), any(), any()))
                .thenReturn(6L);

        Map<String, Object> result = tool.generateMiniQuiz(null, "主题", "single_choice", "easy");

        assertThat(result).containsEntry("question", "跳过空选项");
        @SuppressWarnings("unchecked")
        List<Map<String, Object>> resultOptions = (List<Map<String, Object>>) result.get("options");
        assertThat(resultOptions).hasSize(1);
        assertThat(resultOptions.getFirst())
                .containsEntry("optionText", "有效选项")
                .containsEntry("isCorrect", true)
                .containsEntry("orderIndex", "A");
    }

    @Test
    @DisplayName("generateMiniQuiz 选项缺少 isCorrect 时默认为 false")
    void generateMiniQuizOptionWithoutIsCorrectDefaultsFalse() {
        mockLlmResponse(
                """
                {
                  "question": "缺省正确性",
                  "explanation": "解析",
                  "options": [
                    {"optionText": "选项A", "orderIndex": "A"}
                  ]
                }
                """);
        when(quizDataFacade.createQuiz(any(), any(), any(), any(), any(), any(), any()))
                .thenReturn(7L);

        tool.generateMiniQuiz(null, "主题", "single_choice", "easy");

        verify(quizDataFacade).createQuiz(any(), any(), any(), any(), any(), any(), optionsCaptor.capture());
        assertThat(optionsCaptor.getValue()).singleElement().satisfies(option -> assertThat(option)
                .containsEntry("optionText", "选项A")
                .containsEntry("isCorrect", false)
                .containsEntry("orderIndex", "A"));
    }

    @Test
    @DisplayName("generateMiniQuiz options 为非数组值时跳过选项")
    void generateMiniQuizNonArrayOptions() {
        mockLlmResponse("{\"question\": \"非数组选项\", \"explanation\": \"解析\", \"options\": {}}");
        when(quizDataFacade.createQuiz(any(), any(), any(), any(), any(), any(), any()))
                .thenReturn(8L);

        Map<String, Object> result = tool.generateMiniQuiz(null, "主题", "single_choice", "medium");

        assertThat(result).containsEntry("question", "非数组选项");
        assertThat((List<?>) result.get("options")).isEmpty();
    }

    @Test
    @DisplayName("generateMiniQuiz prompt 根据题型翻译中文题型")
    void generateMiniQuizPromptTranslatesQuestionType() {
        ChatModel chatModel = mockLlmResponse("{\"question\": \"判断题\", \"explanation\": \"解析\", \"options\": []}");
        when(quizDataFacade.createQuiz(any(), any(), any(), any(), any(), any(), any()))
                .thenReturn(4L);

        tool.generateMiniQuiz(null, "主题", "true_false", "hard");

        verify(chatModel).call(promptCaptor.capture());
        assertThat(promptCaptor.getValue().getContents()).contains("hard难度的判断题");
    }

    private ChatModel mockLlmResponse(String responseText) {
        ChatModel chatModel = mock(ChatModel.class);
        ChatResponse chatResponse = mock(ChatResponse.class);
        Generation generation = mock(Generation.class);
        AssistantMessage assistantMessage = new AssistantMessage(responseText);
        when(chatModelProvider.getObject()).thenReturn(chatModel);
        when(chatModel.call(any(Prompt.class))).thenReturn(chatResponse);
        doReturn(generation).when(chatResponse).getResult();
        doReturn(assistantMessage).when(generation).getOutput();
        return chatModel;
    }
}
