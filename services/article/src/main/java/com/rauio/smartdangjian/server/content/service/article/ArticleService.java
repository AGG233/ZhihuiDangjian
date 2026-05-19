package com.rauio.smartdangjian.server.content.service.article;

import java.util.List;

import org.springframework.stereotype.Service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.rauio.smartdangjian.exception.BusinessException;
import com.rauio.smartdangjian.server.content.constants.ArticleErrorConstants;
import com.rauio.smartdangjian.server.content.mapper.ArticleMapper;
import com.rauio.smartdangjian.server.content.mapper.CategoryArticleMapper;
import com.rauio.smartdangjian.server.content.pojo.convertor.ArticleConvertor;
import com.rauio.smartdangjian.server.content.pojo.entity.Article;
import com.rauio.smartdangjian.server.content.pojo.entity.CategoryArticle;
import com.rauio.smartdangjian.server.content.pojo.request.ArticleRequest;
import com.rauio.smartdangjian.server.user.pojo.entity.User;
import com.rauio.smartdangjian.server.user.service.UserService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ArticleService extends ServiceImpl<ArticleMapper, Article> {

    private final CategoryArticleMapper categoryArticleMapper;
    private final UserService userService;
    private final ArticleConvertor convertor;

    public Article get(String id) {
        Article article = this.getById(id);
        if (article == null) {
            throw new BusinessException(ArticleErrorConstants.ARTICLE_NOT_FOUND, "文章不存在");
        }
        return article;
    }

    public List<CategoryArticle> getByCategoryId(String id) {
        return categoryArticleMapper.selectList(
                new LambdaQueryWrapper<CategoryArticle>().eq(CategoryArticle::getCategoryId, id));
    }

    public List<Article> getPage(int pageNum, int pageSize) {
        return this.page(new com.baomidou.mybatisplus.extension.plugins.pagination.Page<>(pageNum, pageSize))
                .getRecords();
    }

    public void create(ArticleRequest request) {
        User user = userService.getCurrentUser();

        Article article = Article.builder()
                .authorId(user.getId())
                .title(request.getTitle())
                .summary(request.getSummary())
                .status(request.getStatus())
                .build();
        if (!this.save(article)) {
            throw new BusinessException(ArticleErrorConstants.ARTICLE_SAVE_FAILED, "文章保存失败");
        }
    }

    public void update(ArticleRequest request) {
        if (!this.updateById(convertor.toEntity(request))) {
            throw new BusinessException(ArticleErrorConstants.ARTICLE_UPDATE_FAILED, "文章更新失败");
        }
    }

    public void delete(String id) {
        if (!this.removeById(id)) {
            throw new BusinessException(ArticleErrorConstants.ARTICLE_DELETE_FAILED, "文章删除失败");
        }
    }
}
