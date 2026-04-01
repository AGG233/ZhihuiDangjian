package com.rauio.smartdangjian.search.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.rauio.smartdangjian.server.content.mapper.CourseMapper;
import com.rauio.smartdangjian.server.content.pojo.convertor.CourseConvertor;
import com.rauio.smartdangjian.server.content.pojo.entity.Course;
import com.rauio.smartdangjian.server.content.pojo.vo.CourseVO;
import com.rauio.smartdangjian.server.user.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class SearchService {

    private final CourseMapper courseMapper;
    private final UserService userService;
    private final CourseConvertor courseConvertor;
    private final RecommendService recommendService;
    private final UserProfileService userProfileService;

    /**
     * 全文检索课程，支持关键词、分类、难度过滤
     */
    public Page<CourseVO> searchCourses(String keyword, String categoryId, String difficulty,
                                         int pageNum, int pageSize) {
        LambdaQueryWrapper<Course> wrapper = new LambdaQueryWrapper<Course>()
                .eq(Course::getIsPublished, true)
                .select(Course::getId, Course::getTitle, Course::getDescription,
                        Course::getCategoryId, Course::getDifficulty, Course::getCoverImageHash,
                        Course::getEnrollmentCount, Course::getAverageRating);

        if (StringUtils.isNotBlank(keyword)) {
            wrapper.apply("MATCH(title, description) AGAINST({0} IN BOOLEAN MODE)", keyword);
        }
        if (StringUtils.isNotBlank(categoryId)) {
            wrapper.eq(Course::getCategoryId, categoryId);
        }
        if (StringUtils.isNotBlank(difficulty)) {
            wrapper.eq(Course::getDifficulty, difficulty);
        }

        // 个性化排序：用户兴趣分类的课程排名提升
        wrapper.orderByDesc(Course::getEnrollmentCount);

        Page<Course> coursePage = courseMapper.selectPage(new Page<>(pageNum, pageSize), wrapper);

        Page<CourseVO> result = new Page<>(coursePage.getCurrent(), coursePage.getSize(), coursePage.getTotal());
        result.setRecords(courseConvertor.toVOList(coursePage.getRecords()));
        return result;
    }

    /**
     * 混合搜索：全文检索 + 个性化推荐补充
     */
    public Page<CourseVO> hybridSearch(String keyword, int pageNum, int pageSize) {
        // 先做全文搜索
        Page<CourseVO> searchPage = searchCourses(keyword, null, null, pageNum, pageSize);
        List<CourseVO> records = new ArrayList<>(searchPage.getRecords());

        // 搜索结果不足时，用推荐补充
        if (records.size() < pageSize) {
            Set<String> existingIds = records.stream()
                    .map(CourseVO::getId)
                    .collect(Collectors.toSet());

            String userId = userService.getCurrentUserId();
            Page<String> cfIds = recommendService.recommend(userId, 1, pageSize);

            Set<String> idsToFetch = cfIds.getRecords().stream()
                    .filter(id -> !existingIds.contains(id))
                    .limit(pageSize - records.size())
                    .collect(Collectors.toSet());

            if (!idsToFetch.isEmpty()) {
                List<CourseVO> recommended = courseConvertor.toVOList(
                        courseMapper.selectBatchIds(idsToFetch));
                records.addAll(recommended);
            }
        }

        searchPage.setRecords(records);
        return searchPage;
    }
}
