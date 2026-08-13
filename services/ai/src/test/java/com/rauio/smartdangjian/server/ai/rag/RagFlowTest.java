package com.rauio.smartdangjian.server.ai.rag;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringBootConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Bean;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import com.rauio.smartdangjian.server.ai.tool.RagSearchTool;
import com.rauio.smartdangjian.server.content.pojo.entity.Article;
import com.rauio.smartdangjian.server.content.pojo.response.ArticleResponse;
import com.rauio.smartdangjian.server.content.pojo.response.ChapterResponse;
import com.rauio.smartdangjian.server.content.pojo.response.ContentBlockResponse;
import com.rauio.smartdangjian.server.content.service.ChapterContentBlockService;
import com.rauio.smartdangjian.server.content.service.article.ArticleService;
import com.rauio.smartdangjian.server.content.service.chapter.ChapterService;

/**
 * RAG 端到端流程测试：装配真实 {@link DocumentIngestionService} + {@link RagSearchTool}，
 * {@link VectorStore} 用 Mockito 内存桩记录 add / similaritySearch 调用，验证
 * 「构造内容 → ingestById 入库 → ragSearch 返回该内容」完整链路。
 *
 * <p>参照 VoiceChatFlowTest 的「ai 模块内自建最小 Spring 上下文」模式：不启用
 * {@code @EnableAutoConfiguration}，规避 DataSource / Flyway / Neo4j / Embedding 依赖，
 * 仅装配被测链路所需 bean（ai 模块无法依赖 server 测试类）。</p>
 */
@SpringBootTest(classes = RagFlowTest.TestConfig.class)
class RagFlowTest {

    @Autowired
    private DocumentIngestionService documentIngestionService;

    @Autowired
    private RagSearchTool ragSearchTool;

    /** Mockito 内存桩：add 记录入库文档，similaritySearch 从中按文本包含检索 */
    @MockitoBean
    private VectorStore vectorStore;

    @MockitoBean
    private ArticleService articleService;

    @MockitoBean
    private ChapterService chapterService;

    @MockitoBean
    private ChapterContentBlockService chapterContentBlockService;

    /** 已入库（add）的文档副本，作为内存向量库的检索源 */
    private final List<Document> inMemoryDocuments = new ArrayList<>();

    @BeforeEach
    void setUpInMemoryVectorStore() {
        inMemoryDocuments.clear();
        doAnswer(invocation -> {
                    List<Document> docs = invocation.getArgument(0);
                    inMemoryDocuments.addAll(docs);
                    return null;
                })
                .when(vectorStore)
                .add(anyList());
        when(vectorStore.similaritySearch(any(SearchRequest.class))).thenAnswer(invocation -> {
            String query = ((SearchRequest) invocation.getArgument(0)).getQuery();
            return inMemoryDocuments.stream()
                    .filter(doc -> doc.getText().contains(query))
                    .map(doc -> Document.builder()
                            .text(doc.getText())
                            .metadata(new HashMap<>(doc.getMetadata()))
                            .score(0.9)
                            .build())
                    .toList();
        });
    }

    @SpringBootConfiguration
    static class TestConfig {

        @Bean
        DocumentIngestionService documentIngestionService(
                ArticleService articleService,
                ChapterService chapterService,
                ChapterContentBlockService chapterContentBlockService,
                VectorStore vectorStore) {
            return new DocumentIngestionService(
                    articleService, chapterService, chapterContentBlockService, vectorStore);
        }

        @Bean
        RagSearchTool ragSearchTool(VectorStore vectorStore) {
            return new RagSearchTool(vectorStore);
        }
    }

    @Test
    @DisplayName("文章 ingest 后 ragSearch 命中：返回 content 片段与 metadata.id/score")
    void articleIngestThenRagSearchReturnsContent() {
        Article article = Article.builder().id(1L).title("全面从严治党").build();
        ArticleResponse detail = ArticleResponse.builder()
                .id(1L)
                .title("全面从严治党")
                .summary("党的纪律建设是全面从严治党的治本之策")
                .contentBlocks(List.of(block("党章是最根本的党内法规"), block("党内法规制度建设事关党长期执政")))
                .build();
        when(articleService.get(1L)).thenReturn(article);
        when(articleService.getDetail(1L)).thenReturn(detail);

        int count = documentIngestionService.ingestById("article", "1");

        assertThat(count).isEqualTo(1);
        verify(vectorStore).add(anyList());

        List<Map<String, Object>> result = ragSearchTool.ragSearch("全面从严治党");

        assertThat(result).hasSize(1);
        assertThat(result.get(0))
                .containsEntry("id", "1")
                .containsEntry("title", "全面从严治党")
                .containsEntry("type", "article")
                .containsEntry("score", 0.9)
                .extracting(m -> ((String) m.get("content")).contains("党的纪律建设是全面从严治党的治本之策"));
        verify(vectorStore).similaritySearch(any(SearchRequest.class));
    }

    @Test
    @DisplayName("章节 ingest 后 ragSearch 命中：返回含 contentBlocks 的内容片段")
    void chapterIngestThenRagSearchReturnsContent() {
        ChapterResponse chapter = ChapterResponse.builder()
                .id(10L)
                .title("党的组织生活")
                .description("三会一课是党的组织生活的基本制度")
                .build();
        when(chapterService.get(10L)).thenReturn(chapter);
        when(chapterContentBlockService.getByChapterId(10L)).thenReturn(List.of(block("支部党员大会是党支部的最高领导机关")));

        int count = documentIngestionService.ingestById("chapter", "10");

        assertThat(count).isEqualTo(1);
        verify(vectorStore).add(anyList());

        List<Map<String, Object>> result = ragSearchTool.ragSearch("三会一课");

        assertThat(result).hasSize(1);
        assertThat(result.get(0))
                .containsEntry("id", "10")
                .containsEntry("title", "党的组织生活")
                .containsEntry("type", "chapter")
                .containsEntry("score", 0.9)
                .extracting(m -> ((String) m.get("content")).contains("支部党员大会是党支部的最高领导机关"));
        verify(vectorStore).similaritySearch(any(SearchRequest.class));
    }

    @Test
    @DisplayName("未 ingest 的内容检索返回空；已入库但无关查询同样返回空")
    void unIngestedContentSearchReturnsEmpty() {
        List<Map<String, Object>> emptyResult = ragSearchTool.ragSearch("从未入库的主题");
        assertThat(emptyResult).isEmpty();

        // 入库章节后，检索与内容无关的查询仍返回空
        ChapterResponse chapter = ChapterResponse.builder()
                .id(20L)
                .title("党内监督")
                .description("党内监督是党的建设的重要内容")
                .build();
        when(chapterService.get(20L)).thenReturn(chapter);
        when(chapterContentBlockService.getByChapterId(20L)).thenReturn(List.of(block("巡视巡察是党内监督的重要方式")));
        documentIngestionService.ingestById("chapter", "20");

        List<Map<String, Object>> unrelated = ragSearchTool.ragSearch("量子计算");
        assertThat(unrelated).isEmpty();
    }

    @Test
    @DisplayName("vectorStore 抛异常时 ragSearch 降级返回空列表（不抛出）")
    void vectorStoreFailureDegradesToEmptyList() {
        when(vectorStore.similaritySearch(any(SearchRequest.class)))
                .thenThrow(new IllegalStateException("Neo4j connection refused"));

        List<Map<String, Object>> result = ragSearchTool.ragSearch("全面从严治党");

        assertThat(result).isEmpty();
    }

    private ContentBlockResponse block(String text) {
        ContentBlockResponse block = new ContentBlockResponse();
        block.setTextContent(text);
        return block;
    }
}
