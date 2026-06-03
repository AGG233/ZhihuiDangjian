package com.rauio.smartdangjian.server.content.api;

import java.util.Collection;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.rauio.smartdangjian.server.content.api.dto.CourseSummary;
import com.rauio.smartdangjian.server.content.pojo.entity.Course;
import com.rauio.smartdangjian.server.content.pojo.response.CourseResponse;
import com.rauio.smartdangjian.server.content.service.course.CourseService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class CourseQueryFacadeImpl implements CourseQueryFacade {

    private final CourseService courseService;

    @Override
    public CourseResponse get(Long courseId) {
        Course entity = courseService.getById(courseId);
        if (entity == null || !Boolean.TRUE.equals(entity.getIsPublished())) {
            return null;
        }
        return courseService.get(courseId);
    }

    @Override
    public CourseSummary getSummary(Long courseId) {
        Course entity = courseService.getById(courseId);
        if (entity == null) {
            return null;
        }
        return CourseSummary.from(entity);
    }

    @Override
    public List<Long> listTopCategoryIdsByCourseIds(Collection<Long> courseIds, int limit) {
        return courseService.listTopCategoryIdsByCourseIds(courseIds, limit);
    }

    @Override
    public Page<Long> recommendPublishedCourseIds(
            Collection<Long> interestCategoryIds,
            Collection<Long> excludedCourseIds,
            String difficulty,
            int pageNum,
            int pageSize) {
        return courseService.recommendPublishedCourseIds(
                interestCategoryIds, excludedCourseIds, difficulty, pageNum, pageSize);
    }

    @Override
    public Page<CourseResponse> searchPublishedCourses(
            String keyword, String categoryId, String difficulty, int pageNum, int pageSize) {
        return courseService.searchPublishedCourses(keyword, categoryId, difficulty, pageNum, pageSize);
    }

    @Override
    public List<CourseResponse> listCourseResponsesByIds(Collection<Long> courseIds) {
        return courseService.listCourseResponsesByIds(courseIds);
    }

    @Override
    public Map<Long, CourseResponse> getCourseResponseMapByIds(Collection<Long> courseIds) {
        return courseService.getCourseResponseMapByIds(courseIds);
    }

    @Override
    public List<CourseSummary> searchByTitle(String keyword, int limit) {
        LambdaQueryWrapper<Course> wrapper = new LambdaQueryWrapper<Course>()
                .like(Course::getTitle, keyword)
                .eq(Course::getIsPublished, true)
                .last("LIMIT " + limit);
        return courseService.list(wrapper).stream().map(CourseSummary::from).toList();
    }
}
