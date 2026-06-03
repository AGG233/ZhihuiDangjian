package com.rauio.smartdangjian.server.content.api;

import java.util.List;

import com.rauio.smartdangjian.server.content.api.dto.ArticleSummary;

/**
 * 文章只读查询接口。供跨模块使用，隐藏实体和内部 Service 实现细节。
 *
 * <p>只返回已发布文章（status=PUBLISHED），不暴露草稿等内部状态。
 */
public interface ArticleQueryFacade {

    /**
     * 按ID获取已发布的文章摘要。
     *
     * @param id 文章ID
     * @return 文章摘要，不存在或未发布时返回 null
     */
    ArticleSummary getById(Long id);

    /**
     * 按关键词搜索已发布文章（匹配标题）。
     *
     * @param keyword 搜索关键词
     * @return 已发布文章的摘要列表
     */
    List<ArticleSummary> searchByKeyword(String keyword);
}
