package com.rauio.smartdangjian.server.ai.tool;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;

@ExtendWith(MockitoExtension.class)
class RagSearchToolTest {

    @Mock
    private VectorStore vectorStore;

    @InjectMocks
    private RagSearchTool ragSearchTool;

    @Nested
    @DisplayName("ragSearch 方法")
    class RagSearchTest {

        @Test
        @DisplayName("向量检索命中时返回结构化 Map 列表（content/id/title/type/score）")
        void returnsStructuredResults() {
            Document doc = Document.builder()
                    .text("习近平强调，全面从严治党永远在路上，必须持之以恒推进党的建设新的伟大工程。")
                    .metadata(Map.of("id", "article-1", "title", "全面从严治党论述", "type", "article"))
                    .score(0.92)
                    .build();
            when(vectorStore.similaritySearch(any(SearchRequest.class))).thenReturn(List.of(doc));

            List<Map<String, Object>> result = ragSearchTool.ragSearch("从严治党的要求是什么");

            assertThat(result).hasSize(1);
            assertThat(result.get(0))
                    .containsEntry("id", "article-1")
                    .containsEntry("title", "全面从严治党论述")
                    .containsEntry("type", "article")
                    .containsEntry("score", 0.92)
                    .containsKey("content")
                    .extracting(m -> ((String) m.get("content")).contains("全面从严治党"));
        }

        @Test
        @DisplayName("content 超过 500 字符时截断")
        void truncatesLongContent() {
            Document doc = Document.builder()
                    .text("A".repeat(800))
                    .metadata(Map.of("id", "1", "title", "t", "type", "article"))
                    .build();
            when(vectorStore.similaritySearch(any(SearchRequest.class))).thenReturn(List.of(doc));

            List<Map<String, Object>> result = ragSearchTool.ragSearch("问题");

            assertThat(result).hasSize(1);
            assertThat((String) result.get(0).get("content")).hasSize(500);
        }

        @Test
        @DisplayName("metadata 缺失 id 时兜底使用 Document id")
        void fallsBackToDocumentId() {
            Document doc = Document.builder()
                    .id("doc-42")
                    .text("正文")
                    .metadata(Map.of())
                    .build();
            when(vectorStore.similaritySearch(any(SearchRequest.class))).thenReturn(List.of(doc));

            List<Map<String, Object>> result = ragSearchTool.ragSearch("问题");

            assertThat(result).hasSize(1);
            assertThat(result.get(0))
                    .containsEntry("id", "doc-42")
                    .containsEntry("title", "")
                    .containsEntry("type", "");
        }

        @Test
        @DisplayName("无命中结果时返回空列表")
        void returnsEmptyListWhenNoMatch() {
            when(vectorStore.similaritySearch(any(SearchRequest.class))).thenReturn(List.of());

            List<Map<String, Object>> result = ragSearchTool.ragSearch("不存在的内容");

            assertThat(result).isEmpty();
        }

        @Test
        @DisplayName("向量库抛异常时降级返回空列表（不抛出）")
        void returnsEmptyListWhenVectorStoreFails() {
            when(vectorStore.similaritySearch(any(SearchRequest.class)))
                    .thenThrow(new IllegalStateException("Neo4j connection refused"));

            List<Map<String, Object>> result = ragSearchTool.ragSearch("问题");

            assertThat(result).isEmpty();
        }
    }
}
