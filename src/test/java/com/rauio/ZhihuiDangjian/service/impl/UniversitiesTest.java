package com.rauio.ZhihuiDangjian.service.impl;

import com.rauio.ZhihuiDangjian.dao.UniversitiesDao;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
public class UniversitiesTest {
    private final UniversitiesDao universitiesDao;

    @Autowired
    UniversitiesTest(UniversitiesDao universitiesDao) {
        this.universitiesDao = universitiesDao;
    }
    @Test
    void testGetIdByName() {
        System.out.println(universitiesDao.getIdByName("广西民族师范学院"));
    }

    @Test
    void testGetAll() {
        System.out.println(universitiesDao.getAll());
    }
}
