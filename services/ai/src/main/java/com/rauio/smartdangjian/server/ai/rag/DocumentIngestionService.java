package com.rauio.smartdangjian.server.ai.rag;

import static com.rauio.smartdangjian.server.ai.constants.AiErrorConstants.DOCUMENT_INGEST_FAILED;
import static com.rauio.smartdangjian.server.ai.constants.AiErrorConstants.DOCUMENT_TYPE_INVALID;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import com.rauio.smartdangjian.exception.BusinessException;
import com.rauio.smartdangjian.server.content.pojo.entity.Article;
import com.rauio.smartdangjian.server.content.pojo.entity.Chapter;
import com.rauio.smartdangjian.server.content.pojo.response.ArticleResponse;
import com.rauio.smartdangjian.server.content.pojo.response.ChapterResponse;
import com.rauio.smartdangjian.server.content.pojo.response.ContentBlockResponse;
import com.rauio.smartdangjian.server.content.service.ChapterContentBlockService;
import com.rauio.smartdangjian.server.content.service.article.ArticleService;
import com.rauio.smartdangjian.server.content.service.chapter.ChapterService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * 文档向量化入库服务。
 *
 * <p>将文章/章节内容读取后切分为约 500 字的块，逐块包装为 Spring AI
 * {@link Document}（携带 type/id/title 元数据）后写入 {@link VectorStore}，
 * 供 RAG 检索使用。支持全量入库（{@link #ingestAll()}）与单条增量入库
 * （{@link #ingestById(String, String)}）。</p>
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class DocumentIngestionService {

    /** 内容切分目标块长（按字符，中文按字计） */
    private static final int CHUNK_SIZE = 500;
    /** 切分时允许的超长段落上限（超过则按定长硬切） */
    private static final int MAX_PARAGRAPH_OVERFLOW = 800;

    private final ArticleService articleService;
    private final ChapterService chapterService;
    private final ChapterContentBlockService chapterContentBlockService;
    private final VectorStore vectorStore;

    /**
     * 全量入库：遍历全部文章与章节。
     *
     * @return 本次入库的文档块总数
     */
    public int ingestAll() {
        int total = 0;
        for (Article article : articleService.list()) {
            total += ingestArticle(article);
        }
        for (Chapter chapter : chapterService.list()) {
            total += ingestChapter(chapter.getId(), chapter.getTitle(), chapter.getDescription());
        }
        log.info("RAG ingestAll completed, total chunks: {}", total);
        return total;
    }

    /**
     * 单条增量入库。
     *
     * @param type 文档类型，仅支持 article / chapter
     * @param id   文章或章节 ID
     * @return 本次入库的文档块总数
     */
    public int ingestById(String type, String id) {
        Long docId = parseId(id);
        int count;
        if ("article".equals(type)) {
            Article article = articleService.get(docId);
            count = ingestArticle(article);
        } else if ("chapter".equals(type)) {
            ChapterResponse chapter = chapterService.get(docId);
            count = ingestChapter(chapter.getId(), chapter.getTitle(), chapter.getDescription());
        } else {
            throw new BusinessException(DOCUMENT_TYPE_INVALID, "不支持的文档类型: " + type);
        }
        log.info("RAG ingestById type={} id={} chunks: {}", type, id, count);
        return count;
    }

    /**
     * 入库单篇文章：正文取内容块的文本内容。
     */
    private int ingestArticle(Article article) {
        ArticleResponse detail = articleService.getDetail(article.getId());
        String content = buildArticleContent(detail);
        List<Document> docs = toDocuments(content, "article", String.valueOf(article.getId()), article.getTitle());
        addDocuments(docs);
        return docs.size();
    }

    /**
     * 入库单个章节：正文取章节描述与内容块的文本内容。
     */
    private int ingestChapter(Long chapterId, String title, String description) {
        String content = buildChapterContent(chapterId, title, description);
        List<Document> docs = toDocuments(content, "chapter", String.valueOf(chapterId), title);
        addDocuments(docs);
        return docs.size();
    }

    private String buildArticleContent(ArticleResponse detail) {
        StringBuilder sb = new StringBuilder();
        if (StringUtils.hasText(detail.getTitle())) {
            sb.append(detail.getTitle()).append('\n');
        }
        if (StringUtils.hasText(detail.getSummary())) {
            sb.append(detail.getSummary()).append('\n');
        }
        appendBlockText(sb, detail.getContentBlocks());
        return sb.toString();
    }

    private String buildChapterContent(Long chapterId, String title, String description) {
        StringBuilder sb = new StringBuilder();
        if (StringUtils.hasText(title)) {
            sb.append(title).append('\n');
        }
        if (StringUtils.hasText(description)) {
            sb.append(description).append('\n');
        }
        List<ContentBlockResponse> blocks = chapterContentBlockService.getByChapterId(chapterId);
        appendBlockText(sb, blocks);
        return sb.toString();
    }

    private void appendBlockText(StringBuilder sb, List<ContentBlockResponse> blocks) {
        if (blocks == null) {
            return;
        }
        for (ContentBlockResponse block : blocks) {
            if (StringUtils.hasText(block.getTextContent())) {
                sb.append(block.getTextContent()).append('\n');
            }
        }
    }

    /**
     * 将原始文本切分为约 500 字的块，并包装为带元数据的 {@link Document}。
     *
     * <p>切分策略：优先按换行段落聚合，段落超长时按定长硬切；单个块不超过
     * {@value #CHUNK_SIZE} 字左右，避免切断语义完整段落。</p>
     */
    private List<Document> toDocuments(String raw, String type, String id, String title) {
        List<String> chunks = splitChunks(raw == null ? "" : raw);
        List<Document> docs = new ArrayList<>(chunks.size());
        for (int i = 0; i < chunks.size(); i++) {
            Map<String, Object> metadata = new HashMap<>();
            metadata.put("type", type);
            metadata.put("id", id);
            metadata.put("title", title);
            metadata.put("chunkIndex", i);
            docs.add(new Document(chunks.get(i), metadata));
        }
        return docs;
    }

    /**
     * 手写文本切分：按段落聚合至接近 {@value #CHUNK_SIZE} 字，段落过长则定长硬切。
     */
    static List<String> splitChunks(String text) {
        List<String> chunks = new ArrayList<>();
        if (text == null || text.isEmpty()) {
            return chunks;
        }
        String[] paragraphs = text.split("\n+");
        StringBuilder buffer = new StringBuilder();
        for (String paragraph : paragraphs) {
            String trimmed = paragraph.trim();
            if (trimmed.isEmpty()) {
                continue;
            }
            if (trimmed.length() > MAX_PARAGRAPH_OVERFLOW) {
                flush(buffer, chunks);
                splitFixedLength(trimmed, chunks);
                continue;
            }
            if (buffer.length() + trimmed.length() > CHUNK_SIZE) {
                flush(buffer, chunks);
            }
            if (buffer.length() > 0) {
                buffer.append('\n');
            }
            buffer.append(trimmed);
        }
        flush(buffer, chunks);
        return chunks;
    }

    private static void flush(StringBuilder buffer, List<String> chunks) {
        String chunk = buffer.toString().trim();
        if (!chunk.isEmpty()) {
            chunks.add(chunk);
        }
        buffer.setLength(0);
    }

    private static void splitFixedLength(String text, List<String> chunks) {
        for (int start = 0; start < text.length(); start += CHUNK_SIZE) {
            int end = Math.min(start + CHUNK_SIZE, text.length());
            chunks.add(text.substring(start, end).trim());
        }
    }

    private void addDocuments(List<Document> docs) {
        if (docs.isEmpty()) {
            return;
        }
        try {
            vectorStore.add(docs);
        } catch (Exception e) {
            throw new BusinessException(DOCUMENT_INGEST_FAILED, "文档入库失败: " + e.getMessage());
        }
    }

    private Long parseId(String id) {
        try {
            return Long.valueOf(id);
        } catch (NumberFormatException e) {
            throw new BusinessException(DOCUMENT_TYPE_INVALID, "文档 ID 格式错误: " + id);
        }
    }
}
