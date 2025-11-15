package com.rauio.ZhihuiDangjian.dao;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.rauio.ZhihuiDangjian.mapper.CategoryMapper;
import com.rauio.ZhihuiDangjian.pojo.Category;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
@RequiredArgsConstructor
public class CategoryDao {

    private final CategoryMapper categoryMapper;

    public Category get(String categoryId) {
        return categoryMapper.selectById(categoryId);
    }

    /**
    * @param categoryId 父目录Id
    * @return 父目录下的子目录
    * */
    public List<Category> getChildren(String categoryId){
        QueryWrapper<Category> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("parent_id", categoryId);
        return categoryMapper.selectList(queryWrapper);
    }
    public List<Category> getRootNodes(){
        QueryWrapper<Category> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("level", 0);
        return categoryMapper.selectList(queryWrapper);
    }
    public Boolean update(Category category) {
        return categoryMapper.updateById(category) > 0;
    }
    public Boolean insert(Category category) {
        return categoryMapper.insert(category) > 0;
    }
    public Boolean delete(String categoryId) {
        return categoryMapper.deleteById(categoryId) > 0;
    }
    public Boolean deleteAll(String categoryId){
        return categoryMapper.delete(new QueryWrapper<Category>().eq("parent_id", categoryId)) > 0;
    }

}