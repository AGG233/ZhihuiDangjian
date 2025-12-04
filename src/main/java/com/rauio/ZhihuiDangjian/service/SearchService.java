package com.rauio.ZhihuiDangjian.service;

import com.rauio.ZhihuiDangjian.mapper.CourseMapper;
import com.rauio.ZhihuiDangjian.pojo.Course;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class SearchService {

    private final CourseMapper courseMapper;

    public List<Course> getCourse(String keyword) {
        return courseMapper.getCourse(keyword);
    }


}
