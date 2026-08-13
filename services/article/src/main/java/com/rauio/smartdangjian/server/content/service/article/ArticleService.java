package com.rauio.smartdangjian.server.content.service.article;

import java.util.Collections;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.rauio.smartdangjian.exception.BusinessException;
import com.rauio.smartdangjian.server.content.constants.ArticleErrorConstants;
import com.rauio.smartdangjian.server.content.mapper.ArticleContentBlockMapper;
import com.rauio.smartdangjian.server.content.mapper.ArticleMapper;
import com.rauio.smartdangjian.server.content.mapper.CategoryArticleMapper;
import com.rauio.smartdangjian.server.content.pojo.convertor.ArticleContentBlockConvertor;
import com.rauio.smartdangjian.server.content.pojo.convertor.ArticleConvertor;
import com.rauio.smartdangjian.server.content.pojo.dto.ContentBlockDto;
import com.rauio.smartdangjian.server.content.pojo.entity.Article;
import com.rauio.smartdangjian.server.content.pojo.entity.ArticleContentBlock;
import com.rauio.smartdangjian.server.content.pojo.entity.CategoryArticle;
import com.rauio.smartdangjian.server.content.pojo.request.ArticleRequest;
import com.rauio.smartdangjian.server.content.pojo.response.ArticleResponse;
import com.rauio.smartdangjian.server.content.pojo.response.ContentBlockResponse;
import com.rauio.smartdangjian.server.user.pojo.entity.User;
import com.rauio.smartdangjian.server.user.service.UserService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ArticleService extends ServiceImpl<ArticleMapper, Article> {

    private final CategoryArticleMapper categoryArticleMapper;
    private final ArticleContentBlockMapper articleContentBlockMapper;
    private final ArticleContentBlockConvertor articleContentBlockConvertor;
    private final UserService userService;
    private final ArticleConvertor convertor;

    public Article get(Long id) {
        Article article = this.getById(id);
        if (article == null) {
            throw new BusinessException(ArticleErrorConstants.ARTICLE_NOT_FOUND, "文章不存在");
        }
        return article;
    }

    /**
     * 获取文章详情，附带分类 ID 与内容块列表。
     *
     * @param id 文章ID
     * @return 文章详情
     */
    public ArticleResponse getDetail(Long id) {
        Article article = get(id);
        ArticleResponse response = convertor.toResponse(article);
        response.setCategoryId(getCategoryIdByArticleId(id));
        response.setContentBlocks(getContentBlocksByArticleId(id));
        return response;
    }

    public List<CategoryArticle> getByCategoryId(Long id) {
        return categoryArticleMapper.selectList(
                new LambdaQueryWrapper<CategoryArticle>().eq(CategoryArticle::getCategoryId, id));
    }

    /**
     * 根据分类 ID 返回该分类下的完整文章列表。
     *
     * @param categoryId 分类ID
     * @return 文章列表
     */
    public List<Article> getArticlesByCategoryId(Long categoryId) {
        List<CategoryArticle> relations = getByCategoryId(categoryId);
        if (relations.isEmpty()) {
            return Collections.emptyList();
        }
        List<Long> articleIds =
                relations.stream().map(CategoryArticle::getArticleId).toList();
        return this.listByIds(articleIds);
    }

    public List<Article> getPage(int pageNum, int pageSize) {
        return this.page(new Page<>(pageNum, pageSize)).getRecords();
    }

    /**
     * 创建文章，事务内同时落 category_article 关联与 article_content_block 内容块。
     *
     * @param request 文章请求体
     */
    @Transactional
    public void create(ArticleRequest request) {
        User user = userService.getCurrentUser();

        Article article = Article.builder()
                .authorId(user.getId())
                .title(request.getTitle())
                .summary(request.getSummary())
                .sourceUrl(request.getSourceUrl())
                .status(request.getStatus())
                .build();
        if (!this.save(article)) {
            throw new BusinessException(ArticleErrorConstants.ARTICLE_SAVE_FAILED, "文章保存失败");
        }
        if (request.getCategoryId() != null) {
            saveCategoryRelation(article.getId(), request.getCategoryId());
        }
        replaceContentBlocks(article.getId(), request.getContentBlocks());
    }

    /**
     * 更新文章，事务内同步维护分类关联与内容块（全量替换）。
     *
     * @param request 文章请求体
     */
    @Transactional
    public void update(ArticleRequest request) {
        if (request.getId() == null) {
            throw new BusinessException(ArticleErrorConstants.ARTICLE_NOT_FOUND, "文章ID不能为空");
        }
        get(request.getId());
        if (!this.updateById(convertor.toEntity(request))) {
            throw new BusinessException(ArticleErrorConstants.ARTICLE_UPDATE_FAILED, "文章更新失败");
        }
        if (request.getCategoryId() != null) {
            categoryArticleMapper.delete(
                    new LambdaQueryWrapper<CategoryArticle>().eq(CategoryArticle::getArticleId, request.getId()));
            saveCategoryRelation(request.getId(), request.getCategoryId());
        }
        if (request.getContentBlocks() != null) {
            replaceContentBlocks(request.getId(), request.getContentBlocks());
        }
    }

    /**
     * 删除文章，级联清理分类关联与内容块。
     *
     * @param id 文章ID
     */
    @Transactional
    public void delete(Long id) {
        categoryArticleMapper.delete(new LambdaQueryWrapper<CategoryArticle>().eq(CategoryArticle::getArticleId, id));
        articleContentBlockMapper.delete(
                new LambdaQueryWrapper<ArticleContentBlock>().eq(ArticleContentBlock::getArticleId, id));
        if (!this.removeById(id)) {
            throw new BusinessException(ArticleErrorConstants.ARTICLE_DELETE_FAILED, "文章删除失败");
        }
    }

    private void saveCategoryRelation(Long articleId, Long categoryId) {
        CategoryArticle relation = new CategoryArticle();
        relation.setArticleId(articleId);
        relation.setCategoryId(categoryId);
        if (categoryArticleMapper.insert(relation) <= 0) {
            throw new BusinessException(ArticleErrorConstants.ARTICLE_SAVE_FAILED, "文章分类关联保存失败");
        }
    }

    /**
     * 全量替换文章内容块：先删后插，orderIndex 按列表顺序。
     */
    private void replaceContentBlocks(Long articleId, List<ContentBlockDto> blocks) {
        articleContentBlockMapper.delete(
                new LambdaQueryWrapper<ArticleContentBlock>().eq(ArticleContentBlock::getArticleId, articleId));
        if (blocks == null || blocks.isEmpty()) {
            return;
        }
        for (int i = 0; i < blocks.size(); i++) {
            ArticleContentBlock block = articleContentBlockConvertor.toEntity(blocks.get(i));
            block.setId(null);
            block.setArticleId(articleId);
            block.setOrderIndex(i);
            if (articleContentBlockMapper.insert(block) <= 0) {
                throw new BusinessException(ArticleErrorConstants.ARTICLE_SAVE_FAILED, "文章内容块保存失败");
            }
        }
    }

    private List<ContentBlockResponse> getContentBlocksByArticleId(Long articleId) {
        List<ArticleContentBlock> blocks = articleContentBlockMapper.selectList(
                new LambdaQueryWrapper<ArticleContentBlock>().eq(ArticleContentBlock::getArticleId, articleId));
        return articleContentBlockConvertor.toResponseList(blocks);
    }

    private Long getCategoryIdByArticleId(Long articleId) {
        CategoryArticle relation = categoryArticleMapper.selectOne(new LambdaQueryWrapper<CategoryArticle>()
                .eq(CategoryArticle::getArticleId, articleId)
                .last("limit 1"));
        return relation == null ? null : relation.getCategoryId();
    }
}
