package com.rauio.smartdangjian.server.ai.tool;

import static com.rauio.smartdangjian.constants.ErrorConstants.RESOURCE_NOT_EXISTS;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.stereotype.Component;

import com.rauio.smartdangjian.common.utils.IdUtil;
import com.rauio.smartdangjian.exception.BusinessException;
import com.rauio.smartdangjian.server.content.api.ArticleQueryFacade;
import com.rauio.smartdangjian.server.content.api.dto.ArticleSummary;
import com.rauio.smartdangjian.server.content.pojo.response.ContentBlockResponse;
import com.rauio.smartdangjian.server.content.service.ArticleContentBlockService;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class ArticleDetailTool {

    private final ArticleQueryFacade articleQueryFacade;
    private final ArticleContentBlockService articleContentBlockService;

    @Tool(name = "searchArticles", description = "根据关键词搜索文章（匹配标题）")
    public List<Map<String, Object>> searchArticles(@ToolParam(description = "搜索关键词") String keyword) {
        List<ArticleSummary> articles = articleQueryFacade.searchByKeyword(keyword);
        return articles.stream()
                .map(article -> {
                    Map<String, Object> map = new HashMap<>();
                    map.put("id", article.getId());
                    map.put("title", article.getTitle());
                    map.put("summary", article.getSummary());
                    return map;
                })
                .collect(Collectors.toList());
    }

    @Tool(name = "getArticleDetail", description = "获取文章详情及其内容块")
    public Map<String, Object> getArticleDetail(@ToolParam(description = "文章ID") String articleId) {
        ArticleSummary article = articleQueryFacade.getById(IdUtil.parse(articleId));
        if (article == null) {
            throw new BusinessException(RESOURCE_NOT_EXISTS, "文章不存在");
        }
        List<ContentBlockResponse> blocks = articleContentBlockService.getByArticleId(IdUtil.parse(articleId));

        Map<String, Object> result = new HashMap<>();
        result.put("id", article.getId());
        result.put("title", article.getTitle());
        result.put("summary", article.getSummary());
        result.put("contentBlocks", blocks);
        return result;
    }
}
