package com.rauio.ZhihuiDangjian.service.impl;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.rauio.ZhihuiDangjian.dao.CourseDao;
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
import java.util.Date;
import java.util.List;

@Service
@RequiredArgsConstructor
public class CourseServiceImpl implements CourseService {

    private final CourseDao         courseDao;
    private final UserService       userService;
    private final CourseConvertor   courseConvertor;

    @Override
    public CourseVO get(String courseId) {
        Course entity = courseDao.get(courseId);
        return courseConvertor.CourseToCourseVO(entity);
    }

    @Override
    public Boolean create(CourseDto courseDto) {
        User    user        = userService.getUserFromAuthentication();
        Course  course      = courseConvertor.CourseDtoToCourse(courseDto);

        course.setCreatorId(user.getId());
        course.setCreatedAt(new Date());
        course.setUpdatedAt(new Date());
        return courseDao.insert(course);
    }

    @Override
    public Boolean update(CourseDto courseDto,String id) {
        if (id == null || id.isEmpty()){
            return false;
        }

        Course  course = courseConvertor.CourseDtoToCourse(courseDto);
        Course  target = courseDao.get(id);
        if(target == null && course.getId().equals(id)){
            return false;
        }
        return courseDao.update(course);
    }


    @Override
    public Boolean delete(String courseId) {
        return courseDao.delete(courseId);
    }

    @Override
    public List<Course> getAll() {
        return courseDao.getAllCourse();
    }

    @Override
    public List<Course> getAllCoursesOfCategory(String categoryId) {
        return courseDao.getAllCoursesOfCategory(categoryId);
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
