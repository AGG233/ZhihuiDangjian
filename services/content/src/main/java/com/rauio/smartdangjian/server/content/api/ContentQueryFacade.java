package com.rauio.smartdangjian.server.content.api;

import java.util.List;

import com.rauio.smartdangjian.server.content.api.dto.ContentBlockSummary;

public interface ContentQueryFacade {

    List<ContentBlockSummary> getByChapterId(Long chapterId);

    List<ContentBlockSummary> getByArticleId(Long articleId);

    ContentBlockSummary getContentBlockById(Long id);
}
