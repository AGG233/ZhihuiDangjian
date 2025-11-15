package com.rauio.ZhihuiDangjian.service.impl;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.rauio.ZhihuiDangjian.pojo.Article;
import com.rauio.ZhihuiDangjian.pojo.CategoryArticle;
import com.rauio.ZhihuiDangjian.service.ArticleService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ArticleServiceImpl implements ArticleService {
    @Override
    public Article get(String id) {
        return null;
    }

    @Override
    public List<CategoryArticle> getAllArticlesOfCategory(String id) {
        return List.of();
    }

    @Override
    public Page<Article> getPage(int pageNum, int pageSize) {
        return null;
    }

    @Override
    public Boolean create(String courseId, String title, String content) {
        return null;
    }

    @Override
    public Boolean update(String id, String title, String content) {
        return null;
    }

    @Override
    public Boolean delete(String id) {
        return null;
    }
}
