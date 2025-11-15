package com.rauio.ZhihuiDangjian.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.rauio.ZhihuiDangjian.pojo.Article;
import com.rauio.ZhihuiDangjian.pojo.CategoryArticle;

import java.util.List;

public interface ArticleService {
    Article get(String id);
    List<CategoryArticle> getAllArticlesOfCategory(String id);
    Page<Article> getPage(int pageNum, int pageSize);

    Boolean create(String courseId, String title, String content);
    Boolean update(String id, String title, String content);
    Boolean delete(String id);
}
