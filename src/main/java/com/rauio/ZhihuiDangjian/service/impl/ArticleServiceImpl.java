package com.rauio.ZhihuiDangjian.service.impl;

import com.rauio.ZhihuiDangjian.dao.ArticleDao;
import com.rauio.ZhihuiDangjian.pojo.Article;
import com.rauio.ZhihuiDangjian.pojo.CategoryArticle;
import com.rauio.ZhihuiDangjian.pojo.User;
import com.rauio.ZhihuiDangjian.pojo.convertor.ArticleConvertor;
import com.rauio.ZhihuiDangjian.pojo.dto.ArticleDto;
import com.rauio.ZhihuiDangjian.service.ArticleService;
import com.rauio.ZhihuiDangjian.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ArticleServiceImpl implements ArticleService {

    private final ArticleDao articleDao;
    private final UserService userService;
    private final ArticleConvertor convertor;

    @Override
    public Article get(Long id) {
        return articleDao.get(id);
    }

    @Override
    public List<CategoryArticle> getAllArticlesOfCategory(String id) {
        return articleDao.getAllArticlesOfCategory(id);
    }

    @Override
    public List<Article> getPage(int pageNum, int pageSize) {
        return articleDao.getPage(pageNum, pageSize);
    }

    @Override
    public Boolean create(ArticleDto dto) {
        User user = userService.getUserFromAuthentication();

        Article article = Article.builder()
                .authorId(user.getId())
                .title(dto.getTitle())
                .summary(dto.getSummary())
                .status(dto.getStatus())
                .build();
        return articleDao.create(article);
    }

    @Override
    public Boolean update(ArticleDto dto) {
        return articleDao.update(convertor.toEntity(dto));
    }

    @Override
    public Boolean delete(Long id) {
        return articleDao.delete(id);
    }
}
