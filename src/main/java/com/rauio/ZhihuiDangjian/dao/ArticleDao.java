package com.rauio.ZhihuiDangjian.dao;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.rauio.ZhihuiDangjian.mapper.ArticleMapper;
import com.rauio.ZhihuiDangjian.pojo.Article;
import com.rauio.ZhihuiDangjian.pojo.CategoryArticle;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
@RequiredArgsConstructor
public class ArticleDao {
    private final ArticleMapper articleMapper;
    private final CategoryArticleDao categoryArticleDao;

    public Article get(String id) {
        return articleMapper.selectById(id);
    }

    public List<CategoryArticle> getAllArticlesOfCategory(String id) {
        return categoryArticleDao.getAllArticlesOfCategory(id);
    }
    public List<Article> getPage(int pageNum, int pageSize) {
        return articleMapper.selectPage(new Page<>(pageNum,pageSize),null).getRecords();
    }
    public Boolean create(Article article) {
        return articleMapper.insert(article) > 0;
    }
    public Boolean update(Article article) {
        return articleMapper.updateById(article) > 0;
    }
    public Boolean delete(String id) {
        return articleMapper.deleteById(id) > 0;
    }

}
