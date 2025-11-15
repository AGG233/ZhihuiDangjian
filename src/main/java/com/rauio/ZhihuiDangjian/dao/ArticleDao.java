package com.rauio.ZhihuiDangjian.dao;

import com.rauio.ZhihuiDangjian.mapper.ArticleMapper;
import com.rauio.ZhihuiDangjian.pojo.Article;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
@RequiredArgsConstructor
public class ArticleDao {
    private final ArticleMapper articleMapper;

    public Article get(String id) {
        return articleMapper.selectById(id);
    }

    public List<Article> getAllArticlesOfCategory(String id) {
    }
}
