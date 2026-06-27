package com.rauio.smartdangjian.server.course.service.course;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.rauio.smartdangjian.exception.BusinessException;
import com.rauio.smartdangjian.server.course.constants.CourseErrorConstants;
import com.rauio.smartdangjian.server.course.mapper.CategoryCourseMapper;
import com.rauio.smartdangjian.server.course.mapper.CourseMapper;
import com.rauio.smartdangjian.server.course.pojo.convertor.CourseConvertor;
import com.rauio.smartdangjian.server.course.pojo.entity.CategoryCourse;
import com.rauio.smartdangjian.server.course.pojo.entity.Course;
import com.rauio.smartdangjian.server.course.pojo.request.CourseRequest;
import com.rauio.smartdangjian.server.course.pojo.response.CourseResponse;
import com.rauio.smartdangjian.server.course.pojo.response.PageResponse;
import com.rauio.smartdangjian.server.user.mapper.UserMapper;
import com.rauio.smartdangjian.server.user.pojo.entity.User;
import com.rauio.smartdangjian.server.user.service.UserService;
import com.rauio.smartdangjian.service.DataScopeService;
import com.rauio.smartdangjian.service.PermissionValidator;
import com.rauio.smartdangjian.utils.spec.UserType;

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
    private final DataScopeService dataScopeService;
    private final PermissionValidator permissionValidator;
    private final UserMapper userMapper;

    private void normalizeCourseFields(Course course) {
        if (course.getCoverImageId() != null && course.getCoverImageId() <= 0) {
            course.setCoverImageId(null);
        }
        if (course.getDifficulty() != null && DIFFICULTY_MAP.containsKey(course.getDifficulty())) {
            course.setDifficulty(DIFFICULTY_MAP.get(course.getDifficulty()));
        }
    }

    public CourseResponse get(Long courseId) {
        Course entity = this.getById(courseId);
        if (entity == null) {
            throw new BusinessException(CourseErrorConstants.COURSE_NOT_FOUND, "课程不存在");
        }
        CourseResponse vo = courseConvertor.toResponse(entity);
        vo.setCategoryId(getCategoryIdByCourseId(courseId));
        return vo;
    }

    public void create(CourseRequest courseRequest) {
        dataScopeService.requireUniversityId();
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

    public void update(CourseRequest courseRequest, Long id) {
        if (id == null) {
            throw new BusinessException(CourseErrorConstants.COURSE_NOT_FOUND, "课程ID不能为空");
        }
        Course target = this.getById(id);
        if (target == null) {
            throw new BusinessException(CourseErrorConstants.COURSE_NOT_FOUND, "课程不存在");
        }
        String universityId = null;
        if (target.getCreatorId() != null) {
            User creator = userMapper.selectById(target.getCreatorId());
            if (creator != null) {
                universityId = creator.getUniversityId();
            }
        }
        dataScopeService.requireManageable(universityId);
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

    public void delete(Long courseId) {
        Course target = this.getById(courseId);
        if (target == null) {
            throw new BusinessException(CourseErrorConstants.COURSE_NOT_FOUND, "课程不存在");
        }
        String universityId = null;
        if (target.getCreatorId() != null) {
            User creator = userMapper.selectById(target.getCreatorId());
            if (creator != null) {
                universityId = creator.getUniversityId();
            }
        }
        dataScopeService.requireManageable(universityId);
        categoryCourseMapper.delete(new LambdaQueryWrapper<CategoryCourse>().eq(CategoryCourse::getCourseId, courseId));
        if (!this.removeById(courseId)) {
            throw new BusinessException(CourseErrorConstants.COURSE_DELETE_FAILED, "课程删除失败");
        }
    }

    public List<Course> getList() {
        return this.list();
    }

    public List<CategoryCourse> getByCategoryId(Long categoryId) {
        return categoryCourseMapper.selectList(
                new LambdaQueryWrapper<CategoryCourse>().eq(CategoryCourse::getCategoryId, categoryId));
    }

    public List<Course> getByUserId(Long userId) {
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
        Map<Long, Long> categoryIdMap = getCategoryIdMapByCourseIds(
                courses.stream().map(Course::getId).filter(Objects::nonNull).toList());
        for (CourseResponse courseVO : courseVOList) {
            Long catId = categoryIdMap.get(courseVO.getId());
            if (catId != null) {
                courseVO.setCategoryId(catId);
            }
        }
        return courseVOList;
    }

    public Map<Long, Long> getCategoryIdMapByCourseIds(List<Long> courseIds) {
        if (courseIds == null || courseIds.isEmpty()) {
            return Collections.emptyMap();
        }
        List<CategoryCourse> relations = categoryCourseMapper.selectList(
                new LambdaQueryWrapper<CategoryCourse>().in(CategoryCourse::getCourseId, courseIds));
        Map<Long, Long> categoryIdMap = new HashMap<>();
        for (CategoryCourse relation : relations) {
            categoryIdMap.putIfAbsent(relation.getCourseId(), relation.getCategoryId());
        }
        return categoryIdMap;
    }

    public Long getCategoryIdByCourseId(Long courseId) {
        if (courseId == null) {
            return null;
        }
        CategoryCourse relation = categoryCourseMapper.selectOne(
                new LambdaQueryWrapper<CategoryCourse>().eq(CategoryCourse::getCourseId, courseId));
        return relation == null ? null : relation.getCategoryId();
    }

    public List<Long> getCourseIdsByCategoryIds(List<Long> categoryIds) {
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

    public List<Long> listTopCategoryIdsByCourseIds(Collection<Long> courseIds, int limit) {
        if (courseIds == null || courseIds.isEmpty() || limit <= 0) {
            return Collections.emptyList();
        }
        return categoryCourseMapper
                .selectList(new LambdaQueryWrapper<CategoryCourse>().in(CategoryCourse::getCourseId, courseIds))
                .stream()
                .map(CategoryCourse::getCategoryId)
                .filter(Objects::nonNull)
                .collect(Collectors.groupingBy(categoryId -> categoryId, Collectors.counting()))
                .entrySet()
                .stream()
                .sorted(Map.Entry.<Long, Long>comparingByValue().reversed())
                .map(Map.Entry::getKey)
                .limit(limit)
                .toList();
    }

    public Page<CourseResponse> searchPublishedCourses(
            String keyword, String categoryId, String difficulty, int pageNum, int pageSize) {
        LambdaQueryWrapper<Course> wrapper = new LambdaQueryWrapper<Course>()
                .eq(Course::getIsPublished, true)
                .select(
                        Course::getId,
                        Course::getTitle,
                        Course::getDescription,
                        Course::getDifficulty,
                        Course::getCoverImageId,
                        Course::getEnrollmentCount,
                        Course::getAverageRating);

        if (StringUtils.isNotBlank(keyword)) {
            wrapper.apply("MATCH(title, description) AGAINST({0} IN BOOLEAN MODE)", keyword);
        }
        if (StringUtils.isNotBlank(categoryId)) {
            List<Long> matchedCourseIds = categoryCourseMapper
                    .selectList(new LambdaQueryWrapper<CategoryCourse>().eq(CategoryCourse::getCategoryId, categoryId))
                    .stream()
                    .map(CategoryCourse::getCourseId)
                    .filter(Objects::nonNull)
                    .distinct()
                    .toList();
            if (matchedCourseIds.isEmpty()) {
                return new Page<>(pageNum, pageSize, 0);
            }
            wrapper.in(Course::getId, matchedCourseIds);
        }
        if (StringUtils.isNotBlank(difficulty)) {
            wrapper.eq(Course::getDifficulty, difficulty);
        }

        wrapper.orderByDesc(Course::getEnrollmentCount);

        Page<Course> coursePage = this.page(new Page<>(pageNum, pageSize), wrapper);
        Page<CourseResponse> result = new Page<>(coursePage.getCurrent(), coursePage.getSize(), coursePage.getTotal());
        result.setRecords(toCourseResponseList(coursePage.getRecords()));
        return result;
    }

    /**
     * 管理员分页查询课程，支持关键字/分类/难度/发布状态筛选。
     * 系统管理员（MANAGER）可查看全量；高校管理员（SCHOOL）仅能看到本校课程。
     *
     * @param keyword     关键字（标题/描述 LIKE 匹配）
     * @param categoryId  分类ID
     * @param difficulty  难度
     * @param isPublished 发布状态（null 时不限制）
     * @param pageNum     页码
     * @param pageSize    每页大小
     * @return 分页结果
     */
    public Page<CourseResponse> searchAdminCourses(
            String keyword, Long categoryId, String difficulty, Boolean isPublished, int pageNum, int pageSize) {
        LambdaQueryWrapper<Course> wrapper = new LambdaQueryWrapper<Course>()
                .select(
                        Course::getId,
                        Course::getTitle,
                        Course::getDescription,
                        Course::getDifficulty,
                        Course::getCoverImageId,
                        Course::getEnrollmentCount,
                        Course::getAverageRating,
                        Course::getIsPublished,
                        Course::getPublishedAt,
                        Course::getCreatorId,
                        Course::getEstimatedDuration,
                        Course::getCreatedAt,
                        Course::getUpdatedAt,
                        Course::getLikeCount);

        // 大学范围隔离：非系统管理员只能看到本校课程
        User currentUser = userService.getCurrentUser();
        if (currentUser != null && currentUser.getUserType() != UserType.MANAGER) {
            String universityId = currentUser.getUniversityId();
            if (StringUtils.isNotBlank(universityId)) {
                List<Long> universityUserIds = userMapper
                        .selectList(new LambdaQueryWrapper<User>()
                                .select(User::getId)
                                .eq(User::getUniversityId, universityId))
                        .stream()
                        .map(User::getId)
                        .filter(Objects::nonNull)
                        .toList();
                if (universityUserIds.isEmpty()) {
                    Page<CourseResponse> emptyPage = new Page<>(pageNum, pageSize, 0);
                    emptyPage.setRecords(Collections.emptyList());
                    return emptyPage;
                }
                wrapper.in(Course::getCreatorId, universityUserIds);
            }
        }

        // 关键字搜索
        if (StringUtils.isNotBlank(keyword)) {
            wrapper.and(w -> w.like(Course::getTitle, keyword).or(w2 -> w2.like(Course::getDescription, keyword)));
        }

        // 分类筛选
        if (categoryId != null) {
            List<Long> matchedCourseIds = categoryCourseMapper
                    .selectList(new LambdaQueryWrapper<CategoryCourse>().eq(CategoryCourse::getCategoryId, categoryId))
                    .stream()
                    .map(CategoryCourse::getCourseId)
                    .filter(Objects::nonNull)
                    .distinct()
                    .toList();
            if (matchedCourseIds.isEmpty()) {
                Page<CourseResponse> emptyPage = new Page<>(pageNum, pageSize, 0);
                emptyPage.setRecords(Collections.emptyList());
                return emptyPage;
            }
            wrapper.in(Course::getId, matchedCourseIds);
        }

        // 难度筛选
        if (StringUtils.isNotBlank(difficulty)) {
            wrapper.eq(Course::getDifficulty, difficulty);
        }

        // 发布状态筛选
        if (isPublished != null) {
            wrapper.eq(Course::getIsPublished, isPublished);
        }

        wrapper.orderByDesc(Course::getCreatedAt);

        Page<Course> coursePage = this.page(new Page<>(pageNum, pageSize), wrapper);
        Page<CourseResponse> resultPage =
                new Page<>(coursePage.getCurrent(), coursePage.getSize(), coursePage.getTotal());
        resultPage.setRecords(toCourseResponseList(coursePage.getRecords()));
        return resultPage;
    }

    public Page<Long> recommendPublishedCourseIds(
            Collection<Long> interestCategoryIds,
            Collection<Long> excludedCourseIds,
            String difficulty,
            int pageNum,
            int pageSize) {
        LambdaQueryWrapper<Course> wrapper = new LambdaQueryWrapper<Course>()
                .eq(Course::getIsPublished, true)
                .notIn(excludedCourseIds != null && !excludedCourseIds.isEmpty(), Course::getId, excludedCourseIds);

        if (interestCategoryIds != null && !interestCategoryIds.isEmpty()) {
            List<Long> matchedCourseIds = getCourseIdsByCategoryIds(new ArrayList<>(interestCategoryIds));
            if (matchedCourseIds.isEmpty()) {
                return new Page<>(pageNum, pageSize, 0);
            }
            wrapper.in(Course::getId, matchedCourseIds);
        }
        if (StringUtils.isNotBlank(difficulty)) {
            wrapper.eq(Course::getDifficulty, difficulty);
        }

        wrapper.orderByDesc(Course::getEnrollmentCount);

        Page<Course> coursePage = this.page(new Page<>(pageNum, pageSize), wrapper);
        Page<Long> result = new Page<>(pageNum, pageSize);
        result.setTotal(coursePage.getTotal());
        result.setRecords(coursePage.getRecords().stream().map(Course::getId).toList());
        return result;
    }

    public List<CourseResponse> listCourseResponsesByIds(Collection<Long> courseIds) {
        if (courseIds == null || courseIds.isEmpty()) {
            return Collections.emptyList();
        }
        return toCourseResponseList(this.listByIds(courseIds));
    }

    public Map<Long, CourseResponse> getCourseResponseMapByIds(Collection<Long> courseIds) {
        return listCourseResponsesByIds(courseIds).stream()
                .collect(Collectors.toMap(CourseResponse::getId, course -> course, (a, b) -> a));
    }
}
