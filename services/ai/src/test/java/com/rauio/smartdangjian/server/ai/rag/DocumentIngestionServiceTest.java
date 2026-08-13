package com.rauio.smartdangjian.server.ai.rag;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.VectorStore;

import com.rauio.smartdangjian.exception.BusinessException;
import com.rauio.smartdangjian.server.content.pojo.entity.Article;
import com.rauio.smartdangjian.server.content.pojo.response.ArticleResponse;
import com.rauio.smartdangjian.server.content.pojo.response.ChapterResponse;
import com.rauio.smartdangjian.server.content.pojo.response.ContentBlockResponse;
import com.rauio.smartdangjian.server.content.service.ChapterContentBlockService;
import com.rauio.smartdangjian.server.content.service.article.ArticleService;
import com.rauio.smartdangjian.server.content.service.chapter.ChapterService;

@ExtendWith(MockitoExtension.class)
class DocumentIngestionServiceTest {

    @Mock
    private ArticleService articleService;

    @Mock
    private ChapterService chapterService;

    @Mock
    private ChapterContentBlockService chapterContentBlockService;

    @Mock
    private VectorStore vectorStore;

    @InjectMocks
    private DocumentIngestionService documentIngestionService;

    @Nested
    @DisplayName("ingestById 方法")
    class IngestByIdTest {

        @Test
        @DisplayName("article 入库：add() 收到至少 1 条 Document 且 metadata 含 type/id/title")
        void articleIngestAddsDocumentsWithMetadata() {
            Article article = Article.builder().id(1L).title("文章标题").build();
            ArticleResponse detail = ArticleResponse.builder()
                    .id(1L)
                    .title("文章标题")
                    .summary("摘要")
                    .contentBlocks(List.of(block("正文第一段内容"), block("正文第二段内容")))
                    .build();
            when(articleService.get(1L)).thenReturn(article);
            when(articleService.getDetail(1L)).thenReturn(detail);

            int count = documentIngestionService.ingestById("article", "1");

            assertThat(count).isEqualTo(1);
            ArgumentCaptor<List<Document>> captor = ArgumentCaptor.forClass(List.class);
            verify(vectorStore).add(captor.capture());
            List<Document> docs = captor.getValue();
            assertThat(docs).isNotEmpty();
            assertThat(docs.get(0).getMetadata())
                    .containsEntry("type", "article")
                    .containsEntry("id", "1")
                    .containsEntry("title", "文章标题");
            assertThat(docs.get(0).getText()).contains("正文第一段内容");
        }

        @Test
        @DisplayName("article 不存在时抛出 BusinessException 且不调用 add")
        void articleNotFoundThrows() {
            when(articleService.get(999L)).thenThrow(new BusinessException(7001, "文章不存在"));

            assertThatThrownBy(() -> documentIngestionService.ingestById("article", "999"))
                    .isInstanceOf(BusinessException.class)
                    .hasMessageContaining("文章不存在");
            verify(vectorStore, never()).add(anyList());
        }

        @Test
        @DisplayName("chapter 入库：add() 收到 Document 且 metadata 含 type/id/title")
        void chapterIngestAddsDocumentsWithMetadata() {
            ChapterResponse chapter = ChapterResponse.builder()
                    .id(10L)
                    .title("章节标题")
                    .description("章节描述")
                    .build();
            when(chapterService.get(10L)).thenReturn(chapter);
            when(chapterContentBlockService.getByChapterId(10L)).thenReturn(List.of(block("章节正文内容")));

            int count = documentIngestionService.ingestById("chapter", "10");

            assertThat(count).isEqualTo(1);
            ArgumentCaptor<List<Document>> captor = ArgumentCaptor.forClass(List.class);
            verify(vectorStore).add(captor.capture());
            List<Document> docs = captor.getValue();
            assertThat(docs).isNotEmpty();
            assertThat(docs.get(0).getMetadata())
                    .containsEntry("type", "chapter")
                    .containsEntry("id", "10")
                    .containsEntry("title", "章节标题");
            assertThat(docs.get(0).getText()).contains("章节正文内容");
        }

        @Test
        @DisplayName("type 非法时抛出 BusinessException 且不调用 add")
        void invalidTypeThrows() {
            assertThatThrownBy(() -> documentIngestionService.ingestById("course", "1"))
                    .isInstanceOf(BusinessException.class)
                    .hasMessageContaining("不支持的文档类型");
            verify(vectorStore, never()).add(anyList());
        }
    }

    @Nested
    @DisplayName("ingestAll 方法")
    class IngestAllTest {

        @Test
        @DisplayName("全量入库：遍历文章与章节并调用 add")
        void ingestAllIngestsArticlesAndChapters() {
            Article article = Article.builder().id(1L).title("文章").build();
            when(articleService.list()).thenReturn(List.of(article));
            when(articleService.getDetail(1L))
                    .thenReturn(ArticleResponse.builder().id(1L).title("文章").build());
            when(chapterService.list()).thenReturn(List.of());

            int count = documentIngestionService.ingestAll();

            assertThat(count).isEqualTo(1);
            ArgumentCaptor<List<Document>> captor = ArgumentCaptor.forClass(List.class);
            verify(vectorStore).add(captor.capture());
            assertThat(captor.getValue()).hasSize(1);
            assertThat(captor.getValue().get(0).getMetadata()).containsEntry("type", "article");
        }
    }

    @Nested
    @DisplayName("splitChunks 静态切分")
    class SplitChunksTest {

        @Test
        @DisplayName("短文不切分")
        void shortTextSingleChunk() {
            assertThat(DocumentIngestionService.splitChunks("短文本内容")).hasSize(1);
        }

        @Test
        @DisplayName("超长文本按段落聚合切块且每块不超过 500 字")
        void longTextSplitsByParagraphs() {
            String text = "第一段内容。\n" + "a".repeat(1200);
            List<String> chunks = DocumentIngestionService.splitChunks(text);
            assertThat(chunks).isNotEmpty();
            for (String chunk : chunks) {
                assertThat(chunk.length()).isLessThanOrEqualTo(500);
            }
        }

        @Test
        @DisplayName("空文本返回空列表")
        void emptyTextReturnsEmpty() {
            assertThat(DocumentIngestionService.splitChunks(null)).isEmpty();
            assertThat(DocumentIngestionService.splitChunks("")).isEmpty();
            assertThat(DocumentIngestionService.splitChunks("   \n  ")).isEmpty();
        }
    }

    private ContentBlockResponse block(String text) {
        ContentBlockResponse block = new ContentBlockResponse();
        block.setTextContent(text);
        return block;
    }
}
