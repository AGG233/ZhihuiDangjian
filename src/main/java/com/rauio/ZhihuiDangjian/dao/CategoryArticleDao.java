package com.rauio.ZhihuiDangjian.dao;


import com.rauio.ZhihuiDangjian.mapper.CategoryArticleMapper;
import com.rauio.ZhihuiDangjian.pojo.CategoryArticle;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
@RequiredArgsConstructor
public class CategoryArticleDao {
    private final CategoryArticleMapper categoryArticleMapper;

    public int insert(CategoryArticle categoryArticle) {
        return categoryArticleMapper.insert(categoryArticle);
    }
    public int delete(CategoryArticle categoryArticle) {
        return categoryArticleMapper.deleteById(categoryArticle);
    }
    public int update(CategoryArticle categoryArticle){
        return categoryArticleMapper.updateById(categoryArticle);
    }
    public CategoryArticle get(String categoryArticle) {
        return categoryArticleMapper.selectById(categoryArticle);
    }
    public List<CategoryArticle> getAllArticlesOfCategory(String categoryId) {
        LambdaQueryWrapper<CategoryArticle> queryWrapper = new LambdaQueryWrapper<>();
        return categoryArticleMapper.selectList(queryWrapper.eq(CategoryArticle::getCategoryId,categoryId));
    }
}
