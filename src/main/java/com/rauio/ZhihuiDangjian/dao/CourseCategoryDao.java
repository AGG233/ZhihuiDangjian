package com.rauio.ZhihuiDangjian.dao;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.rauio.ZhihuiDangjian.mapper.CourseCategoryMapper;
import com.rauio.ZhihuiDangjian.pojo.CourseCategory;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
@RequiredArgsConstructor
public class CourseCategoryDao {

    private final CourseCategoryMapper courseCategoryMapper;

    public CourseCategory get(String categoryId) {
        return courseCategoryMapper.selectById(categoryId);
    }

    /**
    * @param categoryId 父目录Id
    * @return 父目录下的子目录
    * */
    public List<CourseCategory> getChildren(String categoryId){
        QueryWrapper<CourseCategory> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("parent_id", categoryId);
        return courseCategoryMapper.selectList(queryWrapper);
    }
    public List<CourseCategory> getRootNodes(){
        QueryWrapper<CourseCategory> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("level", 0);
        return courseCategoryMapper.selectList(queryWrapper);
    }
    public Boolean update(CourseCategory courseCategory) {
        return courseCategoryMapper.updateById(courseCategory) > 0;
    }
    public Boolean insert(CourseCategory courseCategory) {
        return courseCategoryMapper.insert(courseCategory) > 0;
    }
    public Boolean delete(String categoryId) {
        return courseCategoryMapper.deleteById(categoryId) > 0;
    }
    public Boolean deleteAll(String categoryId){
        return courseCategoryMapper.delete(new QueryWrapper<CourseCategory>().eq("parent_id", categoryId)) > 0;
    }

}