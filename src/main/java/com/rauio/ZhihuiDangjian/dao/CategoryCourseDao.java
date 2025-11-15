package com.rauio.ZhihuiDangjian.dao;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.rauio.ZhihuiDangjian.mapper.CategoryCourseMapper;
import com.rauio.ZhihuiDangjian.pojo.CategoryCourse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
@RequiredArgsConstructor
public class CategoryCourseDao {
    private final CategoryCourseMapper categoryCourseMapper;


    public int insert(CategoryCourse categoryCourse) {
        return categoryCourseMapper.insert(categoryCourse);
    }
    public int delete(CategoryCourse courseId) {
        return categoryCourseMapper.deleteById(courseId);
    }
    public int update(CategoryCourse categoryCourse){
        return categoryCourseMapper.updateById(categoryCourse);
    }
    public CategoryCourse get(String courseId) {
        return categoryCourseMapper.selectById(courseId);
    }

    public List<CategoryCourse> getAllCoursesOfCategory(String categoryId) {
        LambdaQueryWrapper<CategoryCourse> queryWrapper = new LambdaQueryWrapper<>();
        return categoryCourseMapper.selectList(
                queryWrapper.eq(CategoryCourse::getCategoryId,categoryId
                )
        );
    }
}
