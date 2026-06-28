package com.rauio.smartdangjian.server.content.api;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.doReturn;

import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.rauio.smartdangjian.server.content.api.dto.ContentBlockSummary;
import com.rauio.smartdangjian.server.content.pojo.response.ContentBlockResponse;
import com.rauio.smartdangjian.server.content.service.ArticleContentBlockService;
import com.rauio.smartdangjian.server.content.service.ChapterContentBlockService;
import com.rauio.smartdangjian.server.content.spec.BlockType;

@ExtendWith(MockitoExtension.class)
class ContentQueryFacadeImplTest {

    @Mock
    private ChapterContentBlockService chapterContentBlockService;

    @Mock
    private ArticleContentBlockService articleContentBlockService;

    @InjectMocks
    private ContentQueryFacadeImpl facade;

    private static ContentBlockResponse createResponse(Long id, BlockType blockType) {
        ContentBlockResponse r = new ContentBlockResponse();
        r.setParentId(id);
        r.setBlockType(blockType);
        r.setTextContent("text-" + id);
        r.setResourceId(100L + id);
        r.setCaption("caption-" + id);
        return r;
    }

    // ==================== getByChapterId ====================

    @Nested
    @DisplayName("getByChapterId 根据章节ID查询")
    class GetByChapterId {

        @Test
        @DisplayName("返回章节的内容块摘要列表")
        void returnsChapterContentBlocks() {
            ContentBlockResponse r1 = createResponse(1L, BlockType.Paragraph);
            ContentBlockResponse r2 = createResponse(2L, BlockType.Image);
            doReturn(List.of(r1, r2)).when(chapterContentBlockService).getByChapterId(10L);

            List<ContentBlockSummary> result = facade.getByChapterId(10L);

            assertThat(result).hasSize(2);
            assertThat(result).allMatch(s -> s.getChapterId().equals(10L));
            assertThat(result.get(0).getTextContent()).isEqualTo("text-1");
            assertThat(result.get(1).getBlockType()).isEqualTo("Image");
        }

        @Test
        @DisplayName("章节下无内容块时返回空列表")
        void returnsEmptyWhenNoBlocks() {
            doReturn(List.of()).when(chapterContentBlockService).getByChapterId(10L);

            List<ContentBlockSummary> result = facade.getByChapterId(10L);

            assertThat(result).isEmpty();
        }
    }

    // ==================== getByArticleId ====================

    @Nested
    @DisplayName("getByArticleId 根据文章ID查询")
    class GetByArticleId {

        @Test
        @DisplayName("返回文章的内容块摘要列表")
        void returnsArticleContentBlocks() {
            ContentBlockResponse r1 = createResponse(1L, BlockType.Paragraph);
            doReturn(List.of(r1)).when(articleContentBlockService).getByArticleId(20L);

            List<ContentBlockSummary> result = facade.getByArticleId(20L);

            assertThat(result).hasSize(1);
            assertThat(result.get(0).getArticleId()).isEqualTo(20L);
        }

        @Test
        @DisplayName("文章下无内容块时返回空列表")
        void returnsEmptyWhenNoBlocks() {
            doReturn(List.of()).when(articleContentBlockService).getByArticleId(20L);

            List<ContentBlockSummary> result = facade.getByArticleId(20L);

            assertThat(result).isEmpty();
        }
    }

    // ==================== getContentBlockById ====================

    @Nested
    @DisplayName("getContentBlockById 根据ID查询（先查章节再查文章）")
    class GetContentBlockById {

        @Test
        @DisplayName("先找到章节内容块时直接返回")
        void returnsChapterBlockWhenFound() {
            ContentBlockResponse chapterBlock = createResponse(1L, BlockType.Paragraph);
            doReturn(chapterBlock).when(chapterContentBlockService).get(1L);

            ContentBlockSummary result = facade.getContentBlockById(1L);

            assertThat(result).isNotNull();
            assertThat(result.getId()).isEqualTo(1L);
        }

        @Test
        @DisplayName("章节内容块不存在时尝试文章内容块")
        void fallsBackToArticleBlockWhenChapterNotFound() {
            doReturn(null).when(chapterContentBlockService).get(1L);
            ContentBlockResponse articleBlock = createResponse(1L, BlockType.Video);
            doReturn(articleBlock).when(articleContentBlockService).get(1L);

            ContentBlockSummary result = facade.getContentBlockById(1L);

            assertThat(result).isNotNull();
        }

        @Test
        @DisplayName("两者都不存在时返回 null")
        void returnsNullWhenNotFoundInBoth() {
            doReturn(null).when(chapterContentBlockService).get(1L);
            doReturn(null).when(articleContentBlockService).get(1L);

            ContentBlockSummary result = facade.getContentBlockById(1L);

            assertThat(result).isNull();
        }
    }
}
