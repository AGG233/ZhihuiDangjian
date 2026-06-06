package com.rauio.smartdangjian.server.content.api;

import java.util.List;

import org.springframework.stereotype.Component;

import com.rauio.smartdangjian.server.content.api.dto.ContentBlockSummary;
import com.rauio.smartdangjian.server.content.pojo.response.ContentBlockResponse;
import com.rauio.smartdangjian.server.content.service.ArticleContentBlockService;
import com.rauio.smartdangjian.server.content.service.ChapterContentBlockService;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class ContentQueryFacadeImpl implements ContentQueryFacade {

    private final ChapterContentBlockService chapterContentBlockService;
    private final ArticleContentBlockService articleContentBlockService;

    @Override
    public List<ContentBlockSummary> getByChapterId(Long chapterId) {
        List<ContentBlockResponse> responses = chapterContentBlockService.getByChapterId(chapterId);
        return responses.stream()
                .map(response -> convertFromResponse(response, null, chapterId, null, null))
                .toList();
    }

    @Override
    public List<ContentBlockSummary> getByArticleId(Long articleId) {
        List<ContentBlockResponse> responses = articleContentBlockService.getByArticleId(articleId);
        return responses.stream()
                .map(response -> convertFromResponse(response, null, null, articleId, null))
                .toList();
    }

    @Override
    public ContentBlockSummary getContentBlockById(Long id) {
        ContentBlockResponse chapterResponse = chapterContentBlockService.get(id);
        if (chapterResponse != null) {
            return convertFromResponse(chapterResponse, id, null, null, null);
        }
        ContentBlockResponse articleResponse = articleContentBlockService.get(id);
        if (articleResponse != null) {
            return convertFromResponse(articleResponse, id, null, null, null);
        }
        return null;
    }

    private ContentBlockSummary convertFromResponse(
            ContentBlockResponse response, Long id, Long chapterId, Long articleId, Integer orderIndex) {
        if (response == null) {
            return null;
        }
        return ContentBlockSummary.builder()
                .id(id)
                .chapterId(chapterId)
                .articleId(articleId)
                .blockType(
                        response.getBlockType() != null
                                ? response.getBlockType().toString()
                                : null)
                .textContent(response.getTextContent())
                .parentId(response.getParentId())
                .resourceId(response.getResourceId())
                .caption(response.getCaption())
                .orderIndex(orderIndex)
                .build();
    }
}
