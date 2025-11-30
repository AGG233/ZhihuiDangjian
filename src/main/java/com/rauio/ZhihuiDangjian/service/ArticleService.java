package com.rauio.ZhihuiDangjian.service;

import com.rauio.ZhihuiDangjian.pojo.Article;
import com.rauio.ZhihuiDangjian.pojo.CategoryArticle;
import com.rauio.ZhihuiDangjian.pojo.dto.ArticleDto;

import java.util.List;

public interface ArticleService {
    Article get(String id);
    List<CategoryArticle> getAllArticlesOfCategory(String id);
    List<Article> getPage(int pageNum, int pageSize);

    Boolean create(ArticleDto dto);
    Boolean update(ArticleDto dto);
    Boolean delete(String id);
}
