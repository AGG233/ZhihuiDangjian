package com.rauio.ZhihuiDangjiang.service.impl;

import com.rauio.ZhihuiDangjiang.dao.CourseDao;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
public class CourseTest {


    @Autowired
    private CourseDao courseDao;

    @Test
    void getAllCourse() {
        System.out.println(courseDao.getAllCourse());
    }
    @Test
    void getAllCoursesOfCategory() {
        System.out.println(courseDao.getAllCoursesOfCategory("1"));
    }
}
