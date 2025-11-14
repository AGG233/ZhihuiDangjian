package com.rauio.ZhihuiDangjiang.dao;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.github.yulichang.toolkit.JoinWrappers;
import com.github.yulichang.wrapper.MPJLambdaWrapper;
import com.rauio.ZhihuiDangjiang.mapper.CourseMapper;
import com.rauio.ZhihuiDangjiang.pojo.Course;
import com.rauio.ZhihuiDangjiang.pojo.User;
import com.rauio.ZhihuiDangjiang.pojo.UserChapterProgress;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class CourseDao {

    private final CourseMapper courseMapper;

    @Autowired
    public CourseDao(CourseMapper courseMapper) {
        this.courseMapper = courseMapper;
    }

    public Course get(String courseId) {
        return courseMapper.selectById(courseId);
    }

    public Boolean update(Course course) {
        return courseMapper.updateById(course) > 0;
    }

    public Boolean insert(Course course) {
        return courseMapper.insert(course) > 0;
    }

    public Boolean delete(String courseId) {
        return courseMapper.deleteById(courseId) > 0;
    }

    public List<Course> getAllCourse() {
        return courseMapper.selectList(null);
    }

    public List<Course> getAllCoursesOfCategory(String categoryId) {

        MPJLambdaWrapper<Course> wrapper = JoinWrappers.lambda(Course.class)
                .selectAll(Course.class)
                .select(User::getId)
                .leftJoin(User.class, User::getId, Course::getCreatorId)
                .eq(User::getId, categoryId)
                .distinct();

        return courseMapper.selectJoinList(Course.class, wrapper);
    }
    public List<Course> getAllCoursesOfUser(String userId) {

        MPJLambdaWrapper<Course> wrapper = JoinWrappers.lambda(Course.class)
                .selectAll(Course.class)
                .select(User::getId)
                .leftJoin(User.class, User::getId, Course::getCreatorId)
                .leftJoin(UserChapterProgress.class, UserChapterProgress::getUserId, User::getId)
                .eq(User::getId, userId)
                .eq(UserChapterProgress::getUserId, userId)
                .distinct();

        return courseMapper.selectJoinList(Course.class, wrapper);
    }

    public Page<Course> selectPage(int pageNum,int pageSize) {
        Page<Course> page   = new Page<>(pageNum, pageSize);
        return courseMapper.selectPage(page,null);
    }
}