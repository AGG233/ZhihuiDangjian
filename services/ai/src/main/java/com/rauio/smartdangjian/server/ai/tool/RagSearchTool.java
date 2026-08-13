package com.rauio.smartdangjian.server.ai.tool;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.ai.document.Document;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * 基于知识库向量的语义检索工具。
 *
 * <p>与 ContentSearchTool 的关键词检索互补：当向量库不可用或检索抛异常时，
 * 返回空列表降级，由 Agent 继续走 ContentSearchTool 的关键词检索兜底。</p>
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class RagSearchTool {

    /** 返回给 LLM 的 content 片段最大长度，避免上下文超长 */
    private static final int CONTENT_MAX_LENGTH = 500;

    private final VectorStore vectorStore;

    @Tool(name = "ragSearch", description = "基于知识库向量的语义检索，返回与问题语义最相关的内容片段（权威知识库/课程章节/文章）。当用户问题涉及具体知识点、政策原文、理论出处时优先使用。")
    public List<Map<String, Object>> ragSearch(@ToolParam(description = "用户问题或检索查询") String question) {
        try {
            List<Document> documents = vectorStore.similaritySearch(SearchRequest.builder()
                    .query(question)
                    .topK(5)
                    .similarityThreshold(0.5)
                    .build());
            return documents.stream().map(this::toResultMap).collect(Collectors.toList());
        } catch (Exception e) {
            // 向量库不可用时降级返回空列表，交由 ContentSearchTool 关键词检索兜底
            log.warn("RAG 语义检索失败，降级返回空列表: {}", e.getMessage());
            return Collections.emptyList();
        }
    }

    private Map<String, Object> toResultMap(Document document) {
        Map<String, Object> map = new HashMap<>();
        String content = document.getText();
        map.put("content", content.length() > CONTENT_MAX_LENGTH ? content.substring(0, CONTENT_MAX_LENGTH) : content);
        Map<String, Object> metadata = document.getMetadata();
        map.put("id", metadata.getOrDefault("id", document.getId()));
        map.put("title", metadata.getOrDefault("title", ""));
        map.put("type", metadata.getOrDefault("type", ""));
        map.put("score", document.getScore());
        return map;
    }
}
