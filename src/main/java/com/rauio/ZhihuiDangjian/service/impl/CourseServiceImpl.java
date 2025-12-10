package com.rauio.ZhihuiDangjian.service.impl;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.rauio.ZhihuiDangjian.dao.CategoryCourseDao;
import com.rauio.ZhihuiDangjian.dao.CourseDao;
import com.rauio.ZhihuiDangjian.pojo.CategoryCourse;
import com.rauio.ZhihuiDangjian.pojo.Course;
import com.rauio.ZhihuiDangjian.pojo.User;
import com.rauio.ZhihuiDangjian.pojo.convertor.CourseConvertor;
import com.rauio.ZhihuiDangjian.pojo.dto.CourseDto;
import com.rauio.ZhihuiDangjian.pojo.vo.CourseVO;
import com.rauio.ZhihuiDangjian.pojo.vo.PageVO;
import com.rauio.ZhihuiDangjian.service.CourseService;
import com.rauio.ZhihuiDangjian.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;

@Service
@RequiredArgsConstructor
public class CourseServiceImpl implements CourseService {

    private final CourseDao         courseDao;
    private final UserService       userService;
    private final CourseConvertor   courseConvertor;
    private final CategoryCourseDao categoryCourseDao;


    @Override
    public CourseVO get(Long courseId) {
        Course entity = courseDao.get(courseId);
        return courseConvertor.toVO(entity);
    }

    @Override
    public Boolean create(CourseDto courseDto) {
        User    user        = userService.getUserFromAuthentication();
        Course  course      = courseConvertor.toCourse(courseDto);

        course.setCreatorId(user.getId());

        courseDao.insert(course);
        return categoryCourseDao.insert(CategoryCourse.builder()
                .courseId(course.getId())
                .categoryId(courseDto.getCategoryId())
                .build()
        ) > 0;

    }

    @Override
    public Boolean update(CourseDto courseDto,Long id) {
        if (id == null){
            return false;
        }

        Course  course = courseConvertor.toCourse(courseDto);
        Course  target = courseDao.get(id);
        if(target == null && course.getId().equals(id)){
            return false;
        }
        return courseDao.update(course);
    }


    @Override
    public Boolean delete(Long courseId) {
        return courseDao.delete(courseId);
    }

    @Override
    public List<Course> getAll() {
        return courseDao.getAllCourse();
    }

    @Override
    public List<CategoryCourse> getAllCoursesOfCategory(String categoryId) {
        return categoryCourseDao.getAllCoursesOfCategory(categoryId);
    }


    @Override
    public List<Course> getAllCoursesOfUser(String userId) {
        return courseDao.getAllCoursesOfUser(userId);
    }

    @Override
    public PageVO<Object> getPage(int pageNum, int pageSize) {
        Page<Course> page = courseDao.selectPage(pageNum,pageSize);
        return PageVO.builder()
                .total(page.getTotal())
                .size(page.getSize())
                .current(page.getCurrent())
                .list(Collections.singletonList(page.getRecords()))
                .build();
    }
}
