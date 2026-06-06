package com.rauio.smartdangjian.server.ai.tool;

import static com.rauio.smartdangjian.constants.ErrorConstants.RESOURCE_NOT_EXISTS;

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
import com.rauio.smartdangjian.common.utils.IdUtil;
import com.rauio.smartdangjian.exception.BusinessException;
import com.rauio.smartdangjian.server.chapter.api.ChapterQueryFacade;
import com.rauio.smartdangjian.server.chapter.pojo.response.ChapterResponse;
import com.rauio.smartdangjian.server.content.api.ContentQueryFacade;
import com.rauio.smartdangjian.server.content.api.dto.ContentBlockSummary;
import com.rauio.smartdangjian.server.quiz.api.QuizDataFacade;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor
public class AiQuizGeneratorTool {

    private final ObjectProvider<ChatModel> chatModelProvider;
    private final ChapterQueryFacade chapterQueryFacade;
    private final ContentQueryFacade contentQueryFacade;
    private final QuizDataFacade quizDataFacade;
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
            ChapterResponse chapter = chapterQueryFacade.get(IdUtil.parse(chapterId));
            if (chapter == null) {
                throw new BusinessException(RESOURCE_NOT_EXISTS, "章节不存在");
            }
            List<ContentBlockSummary> blocks = contentQueryFacade.getByChapterId(IdUtil.parse(chapterId));
            StringBuilder sb = new StringBuilder();
            sb.append("章节标题：").append(chapter.getTitle()).append("\n");
            if (chapter.getDescription() != null) {
                sb.append("章节描述：").append(chapter.getDescription()).append("\n");
            }
            if (blocks != null && !blocks.isEmpty()) {
                sb.append("章节内容：\n");
                for (ContentBlockSummary block : blocks) {
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

        List<Map<String, Object>> optList = new ArrayList<>();
        if (optionsNode != null && optionsNode.isArray()) {
            for (JsonNode optNode : optionsNode) {
                String optionText = optNode.path("optionText").asText(null);
                if (optionText == null || optionText.isBlank()) {
                    continue;
                }
                boolean isCorrect = optNode.has("isCorrect") ? optNode.path("isCorrect").asBoolean() : false;
                Map<String, Object> optData = new HashMap<>();
                optData.put("optionText", optionText);
                optData.put("isCorrect", isCorrect);
                optData.put("orderIndex", optNode.path("orderIndex").asText(null));
                optList.add(optData);
            }
        }

        int score = switch (effectiveDifficulty) {
            case "easy" -> 5;
            case "hard" -> 15;
            default -> 10;
        };

        Long quizId = quizDataFacade.createQuiz(
                effectiveChapterId != null ? IdUtil.parse(effectiveChapterId) : null,
                question,
                effectiveQuestionType,
                score,
                effectiveDifficulty,
                explanation,
                optList);

        Map<String, Object> result = new HashMap<>();
        result.put("quizId", quizId.toString());
        result.put("question", question);
        result.put("questionType", effectiveQuestionType);
        result.put("difficulty", effectiveDifficulty);
        result.put("explanation", explanation);
        result.put("options", optList);
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

    private String getFieldValue(ContentBlockSummary block, String fieldName) {
        return switch (fieldName) {
            case "textContent" -> block.getTextContent();
            case "blockType" -> block.getBlockType();
            case "parentId" -> block.getParentId() != null ? block.getParentId().toString() : null;
            case "resourceId" ->
                block.getResourceId() != null ? block.getResourceId().toString() : null;
            case "caption" -> block.getCaption();
            default -> {
                log.warn("未知字段: {}", fieldName);
                yield null;
            }
        };
    }
}
