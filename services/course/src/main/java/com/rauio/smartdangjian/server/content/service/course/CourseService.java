package com.rauio.smartdangjian.server.content.service.course;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.rauio.smartdangjian.exception.BusinessException;
import com.rauio.smartdangjian.server.content.constants.CourseErrorConstants;
import com.rauio.smartdangjian.server.content.mapper.CategoryCourseMapper;
import com.rauio.smartdangjian.server.content.mapper.CourseMapper;
import com.rauio.smartdangjian.server.content.pojo.convertor.CourseConvertor;
import com.rauio.smartdangjian.server.content.pojo.entity.CategoryCourse;
import com.rauio.smartdangjian.server.content.pojo.entity.Course;
import com.rauio.smartdangjian.server.content.pojo.request.CourseRequest;
import com.rauio.smartdangjian.server.content.pojo.response.CourseResponse;
import com.rauio.smartdangjian.server.content.pojo.response.PageResponse;
import com.rauio.smartdangjian.server.user.pojo.entity.User;
import com.rauio.smartdangjian.server.user.service.UserService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class CourseService extends ServiceImpl<CourseMapper, Course> {

    private static final Map<String, String> DIFFICULTY_MAP = Map.of(
            "入门", "beginner",
            "中级", "intermediate",
            "高级", "advanced");

    private final UserService userService;
    private final CourseConvertor courseConvertor;
    private final CategoryCourseMapper categoryCourseMapper;

    private void normalizeCourseFields(Course course) {
        if (course.getCoverImageId() != null && course.getCoverImageId().isBlank()) {
            course.setCoverImageId(null);
        }
        if (course.getDifficulty() != null && DIFFICULTY_MAP.containsKey(course.getDifficulty())) {
            course.setDifficulty(DIFFICULTY_MAP.get(course.getDifficulty()));
        }
    }

    public CourseResponse get(String courseId) {
        Course entity = this.getById(courseId);
        if (entity == null) {
            throw new BusinessException(CourseErrorConstants.COURSE_NOT_FOUND, "课程不存在");
        }
        CourseResponse vo = courseConvertor.toResponse(entity);
        vo.setCategoryId(getCategoryIdByCourseId(courseId));
        return vo;
    }

    public void create(CourseRequest courseRequest) {
        User user = userService.getCurrentUser();
        Course course = courseConvertor.toCourse(courseRequest);
        course.setCreatorId(user.getId());
        normalizeCourseFields(course);
        if (!this.save(course)) {
            throw new BusinessException(CourseErrorConstants.COURSE_SAVE_FAILED, "课程保存失败");
        }
        int insertResult = categoryCourseMapper.insert(CategoryCourse.builder()
                .courseId(course.getId())
                .categoryId(courseRequest.getCategoryId())
                .build());
        if (insertResult <= 0) {
            throw new BusinessException(CourseErrorConstants.COURSE_SAVE_FAILED, "课程分类关联保存失败");
        }
    }

    public void update(CourseRequest courseRequest, String id) {
        if (id == null) {
            throw new BusinessException(CourseErrorConstants.COURSE_NOT_FOUND, "课程ID不能为空");
        }
        Course target = this.getById(id);
        if (target == null) {
            throw new BusinessException(CourseErrorConstants.COURSE_NOT_FOUND, "课程不存在");
        }
        Course course = courseConvertor.toCourse(courseRequest);
        course.setId(id);
        normalizeCourseFields(course);
        if (!this.updateById(course)) {
            throw new BusinessException(CourseErrorConstants.COURSE_UPDATE_FAILED, "课程更新失败");
        }
        if (courseRequest.getCategoryId() != null) {
            categoryCourseMapper.delete(new LambdaQueryWrapper<CategoryCourse>().eq(CategoryCourse::getCourseId, id));
            int insertResult = categoryCourseMapper.insert(CategoryCourse.builder()
                    .courseId(id)
                    .categoryId(courseRequest.getCategoryId())
                    .build());
            if (insertResult <= 0) {
                throw new BusinessException(CourseErrorConstants.COURSE_UPDATE_FAILED, "课程分类关联更新失败");
            }
        }
    }

    public void delete(String courseId) {
        categoryCourseMapper.delete(new LambdaQueryWrapper<CategoryCourse>().eq(CategoryCourse::getCourseId, courseId));
        if (!this.removeById(courseId)) {
            throw new BusinessException(CourseErrorConstants.COURSE_DELETE_FAILED, "课程删除失败");
        }
    }

    public List<Course> getList() {
        return this.list();
    }

    public List<CategoryCourse> getByCategoryId(String categoryId) {
        return categoryCourseMapper.selectList(
                new LambdaQueryWrapper<CategoryCourse>().eq(CategoryCourse::getCategoryId, categoryId));
    }

    public List<Course> getByUserId(String userId) {
        return this.baseMapper.selectLearnedCoursesByUserId(userId);
    }

    public PageResponse<Object> getPage(int pageNum, int pageSize) {
        Page<Course> page = this.page(new Page<>(pageNum, pageSize));
        List<CourseResponse> courseVOList = toCourseResponseList(page.getRecords());
        return PageResponse.builder()
                .total(page.getTotal())
                .size(page.getSize())
                .current(page.getCurrent())
                .list(Collections.singletonList(courseVOList))
                .build();
    }

    public List<CourseResponse> toCourseResponseList(List<Course> courses) {
        if (courses == null || courses.isEmpty()) {
            return Collections.emptyList();
        }
        List<CourseResponse> courseVOList = new ArrayList<>(courseConvertor.toResponseList(courses));
        Map<String, String> categoryIdMap = getCategoryIdMapByCourseIds(
                courses.stream().map(Course::getId).filter(Objects::nonNull).toList());
        for (CourseResponse courseVO : courseVOList) {
            courseVO.setCategoryId(categoryIdMap.get(courseVO.getId()));
        }
        return courseVOList;
    }

    public Map<String, String> getCategoryIdMapByCourseIds(List<String> courseIds) {
        if (courseIds == null || courseIds.isEmpty()) {
            return Collections.emptyMap();
        }
        List<CategoryCourse> relations = categoryCourseMapper.selectList(
                new LambdaQueryWrapper<CategoryCourse>().in(CategoryCourse::getCourseId, courseIds));
        Map<String, String> categoryIdMap = new HashMap<>();
        for (CategoryCourse relation : relations) {
            categoryIdMap.putIfAbsent(relation.getCourseId(), relation.getCategoryId());
        }
        return categoryIdMap;
    }

    public String getCategoryIdByCourseId(String courseId) {
        if (courseId == null) {
            return null;
        }
        CategoryCourse relation = categoryCourseMapper.selectOne(new LambdaQueryWrapper<CategoryCourse>()
                .eq(CategoryCourse::getCourseId, courseId)
                .last("limit 1"));
        return relation == null ? null : relation.getCategoryId();
    }

    public List<String> getCourseIdsByCategoryIds(List<String> categoryIds) {
        if (categoryIds == null || categoryIds.isEmpty()) {
            return Collections.emptyList();
        }
        return categoryCourseMapper
                .selectList(new LambdaQueryWrapper<CategoryCourse>().in(CategoryCourse::getCategoryId, categoryIds))
                .stream()
                .map(CategoryCourse::getCourseId)
                .filter(Objects::nonNull)
                .distinct()
                .collect(Collectors.toList());
    }
}
