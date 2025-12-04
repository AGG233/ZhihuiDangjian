package com.rauio.ZhihuiDangjian.service;

import com.rauio.ZhihuiDangjian.pojo.CategoryCourse;
import com.rauio.ZhihuiDangjian.pojo.Course;
import com.rauio.ZhihuiDangjian.pojo.dto.CourseDto;
import com.rauio.ZhihuiDangjian.pojo.vo.CourseVO;
import com.rauio.ZhihuiDangjian.pojo.vo.PageVO;

import java.util.List;

public interface CourseService {
    CourseVO        get(Long courseId);
    Boolean             create(CourseDto course);
    Boolean         update(CourseDto course,Long id);
    Boolean         delete(Long courseId);

    List<Course>    getAll();
    List<CategoryCourse>    getAllCoursesOfCategory(String categoryId);
    List<Course>    getAllCoursesOfUser(String userId);
    PageVO<Object>  getPage(int pageNum, int pageSize);
}