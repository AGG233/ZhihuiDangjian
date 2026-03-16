package com.rauio.smartdangjian.service.content;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.rauio.smartdangjian.dao.CategoryCourseDao;
import com.rauio.smartdangjian.dao.CourseDao;
import com.rauio.smartdangjian.pojo.CategoryCourse;
import com.rauio.smartdangjian.pojo.Course;
import com.rauio.smartdangjian.pojo.User;
import com.rauio.smartdangjian.pojo.convertor.CourseConvertor;
import com.rauio.smartdangjian.pojo.dto.CourseDto;
import com.rauio.smartdangjian.pojo.vo.CourseVO;
import com.rauio.smartdangjian.pojo.vo.PageVO;
import com.rauio.smartdangjian.service.user.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;

@Service
@RequiredArgsConstructor
public class CourseService {

    private final CourseDao         courseDao;
    private final UserService       userService;
    private final CourseConvertor   courseConvertor;
    private final CategoryCourseDao categoryCourseDao;
    public CourseVO get(Long courseId) {
        Course entity = courseDao.get(courseId);
        return courseConvertor.toVO(entity);
    }
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
    public Boolean delete(Long courseId) {
        return courseDao.delete(courseId);
    }
    public List<Course> getAll() {
        return courseDao.getAllCourse();
    }
    public List<CategoryCourse> getAllCoursesOfCategory(String categoryId) {
        return categoryCourseDao.getAllCoursesOfCategory(categoryId);
    }
    public List<Course> getAllCoursesOfUser(String userId) {
        return courseDao.getAllCoursesOfUser(userId);
    }
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
