package com.rauio.smartdangjian.server.ai.tool;

import static com.rauio.smartdangjian.constants.ErrorConstants.RESOURCE_NOT_EXISTS;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.JsonNode;
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

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor
public class AiQuizGeneratorTool {

    private final ObjectProvider<ChatModel> chatModelProvider;
    private final ChapterService chapterService;
    private final ContentBlockService contentBlockService;
    private final QuizService quizService;
    private final QuizOptionService quizOptionService;
    private final ObjectMapper objectMapper;

    @Tool(name = "generateMiniQuiz", description = "根据章节ID或主题，自动生成一道小问答并保存到数据库。AI会提取章节内容或主题要点生成题目、选项和解析。")
    public Map<String, Object> generateMiniQuiz(
            @ToolParam(description = "章节ID，若提供则基于该章节内容生成") String chapterId,
            @ToolParam(description = "主题/知识点，若未提供chapterId则基于主题生成") String topic,
            @ToolParam(description = "题目类型：single_choice / multiple_choice / true_false，默认single_choice")
                    String questionType,
            @ToolParam(description = "难度：easy / medium / hard，默认medium") String difficulty) {
        String effectiveQuestionType =
                (questionType == null || questionType.isBlank()) ? "single_choice" : questionType;
        String effectiveDifficulty = (difficulty == null || difficulty.isBlank()) ? "medium" : difficulty;

        String content;
        String effectiveChapterId = chapterId;

        if (chapterId != null && !chapterId.isBlank()) {
            ChapterVO chapter = chapterService.get(chapterId);
            if (chapter == null) {
                throw new BusinessException(RESOURCE_NOT_EXISTS, "章节不存在");
            }
            List<ContentBlockVO> blocks = contentBlockService.getByParentId(chapterId);
            StringBuilder sb = new StringBuilder();
            sb.append("章节标题：").append(chapter.getTitle()).append("\n");
            if (chapter.getDescription() != null) {
                sb.append("章节描述：").append(chapter.getDescription()).append("\n");
            }
            if (blocks != null && !blocks.isEmpty()) {
                sb.append("章节内容：\n");
                for (ContentBlockVO block : blocks) {
                    String text = getFieldValue(block, "textContent");
                    if (text != null && !text.isBlank()) {
                        sb.append(text).append("\n");
                    }
                }
            }
            content = sb.toString();
        } else if (topic != null && !topic.isBlank()) {
            content = "主题：" + topic;
            effectiveChapterId = null;
        } else {
            throw new BusinessException(RESOURCE_NOT_EXISTS, "必须提供章节ID或主题");
        }

        String prompt = buildPrompt(content, effectiveQuestionType, effectiveDifficulty);
        String llmResponse;
        try {
            llmResponse = chatModelProvider
                    .getObject()
                    .call(new Prompt(prompt))
                    .getResult()
                    .getOutput()
                    .getText();
        } catch (Exception e) {
            log.error("LLM调用失败", e);
            throw new BusinessException(RESOURCE_NOT_EXISTS, "AI生成题目失败：" + e.getMessage());
        }

        JsonNode root;
        try {
            String cleaned = cleanJsonResponse(llmResponse);
            root = objectMapper.readTree(cleaned);
        } catch (Exception e) {
            log.error("LLM返回JSON解析失败，原始响应：{}", llmResponse, e);
            throw new BusinessException(RESOURCE_NOT_EXISTS, "AI返回内容解析失败");
        }

        String question = root.path("question").asText(null);
        String explanation = root.path("explanation").asText(null);
        JsonNode optionsNode = root.path("options");

        if (question == null || question.isBlank()) {
            throw new BusinessException(RESOURCE_NOT_EXISTS, "AI生成的题目内容为空");
        }

        Quiz quiz = Quiz.builder()
                .chapterId(effectiveChapterId)
                .question(question)
                .questionType(effectiveQuestionType)
                .score(5)
                .difficulty(effectiveDifficulty)
                .explanation(explanation)
                .isActive(true)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        Boolean saved = quizService.create(quiz);
        if (!Boolean.TRUE.equals(saved) || quiz.getId() == null) {
            throw new BusinessException(RESOURCE_NOT_EXISTS, "测验保存失败");
        }

        List<Map<String, Object>> optionResults = new ArrayList<>();
        if (optionsNode != null && optionsNode.isArray()) {
            for (JsonNode optNode : optionsNode) {
                String optionText = optNode.path("optionText").asText(null);
                Boolean isCorrect =
                        optNode.has("isCorrect") ? optNode.path("isCorrect").asBoolean() : null;
                String orderIndex = optNode.path("orderIndex").asText(null);

                if (optionText == null || optionText.isBlank()) {
                    continue;
                }

                QuizOption option = QuizOption.builder()
                        .quizId(quiz.getId())
                        .optionText(optionText)
                        .isCorrect(isCorrect != null ? isCorrect : false)
                        .orderIndex(orderIndex)
                        .build();
                Boolean optionSaved = quizOptionService.create(quiz.getId(), option);
                if (!Boolean.TRUE.equals(optionSaved)) {
                    throw new BusinessException(RESOURCE_NOT_EXISTS, "选项创建失败");
                }

                Map<String, Object> optMap = new HashMap<>();
                optMap.put("optionText", optionText);
                optMap.put("isCorrect", isCorrect);
                optMap.put("orderIndex", orderIndex);
                optionResults.add(optMap);
            }
        }

        Map<String, Object> result = new HashMap<>();
        result.put("quizId", quiz.getId());
        result.put("question", question);
        result.put("questionType", effectiveQuestionType);
        result.put("difficulty", effectiveDifficulty);
        result.put("explanation", explanation);
        result.put("options", optionResults);
        return result;
    }

    private String buildPrompt(String content, String questionType, String difficulty) {
        return """
                你是一位专业的教育内容出题专家。请根据以下材料生成一道%s难度的%s题。

                要求：
                1. 题目必须紧扣材料核心知识点。
                2. 选项必须有明确且唯一的正确答案（单选题）或符合题型的正确组合（多选题/判断题）。
                3. 每个选项附带简明解析说明为什么对或错。
                4. 输出必须是严格的JSON格式，不要包含任何Markdown代码块标记或其他额外文本。

                材料：
                %s

                请按以下JSON格式返回：
                {
                  "question": "题目内容",
                  "explanation": "整体解析",
                  "options": [
                    {"optionText": "选项A", "isCorrect": true, "orderIndex": "A"},
                    {"optionText": "选项B", "isCorrect": false, "orderIndex": "B"}
                  ]
                }
                """
                .formatted(difficulty, translateQuestionType(questionType), content);
    }

    private String translateQuestionType(String questionType) {
        return switch (questionType) {
            case "multiple_choice" -> "多选";
            case "true_false" -> "判断";
            default -> "单选";
        };
    }

    private String cleanJsonResponse(String response) {
        if (response == null) {
            return "";
        }
        String cleaned = response.trim();
        if (cleaned.startsWith("```json")) {
            cleaned = cleaned.substring(7);
        } else if (cleaned.startsWith("```")) {
            cleaned = cleaned.substring(3);
        }
        if (cleaned.endsWith("```")) {
            cleaned = cleaned.substring(0, cleaned.length() - 3);
        }
        return cleaned.trim();
    }

    private String getFieldValue(ContentBlockVO block, String fieldName) {
        try {
            java.lang.reflect.Field field = ContentBlockVO.class.getDeclaredField(fieldName);
            field.setAccessible(true);
            Object value = field.get(block);
            return value != null ? value.toString() : null;
        } catch (Exception e) {
            log.warn("无法读取ContentBlockVO字段 {}", fieldName, e);
            return null;
        }
    }
}
