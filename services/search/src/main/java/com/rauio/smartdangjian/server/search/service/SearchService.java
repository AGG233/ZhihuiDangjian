package com.rauio.smartdangjian.server.search.service;

import java.util.*;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.rauio.smartdangjian.common.utils.IdUtil;
import com.rauio.smartdangjian.server.content.pojo.response.CourseResponse;
import com.rauio.smartdangjian.server.content.service.course.CourseService;
import com.rauio.smartdangjian.server.user.service.UserService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class SearchService {

    private final CourseService courseService;
    private final UserService userService;
    private final RecommendService recommendService;

    /**
     * 全文检索课程，支持关键词、分类、难度过滤
     */
    public Page<CourseResponse> searchCourses(
            String keyword, String categoryId, String difficulty, int pageNum, int pageSize) {
        return courseService.searchPublishedCourses(keyword, categoryId, difficulty, pageNum, pageSize);
    }

    /**
     * 混合搜索：全文检索 + 个性化推荐补充
     */
    public Page<CourseResponse> hybridSearch(String keyword, int pageNum, int pageSize) {
        // 先做全文搜索
        Page<CourseResponse> searchPage = searchCourses(keyword, null, null, pageNum, pageSize);
        List<CourseResponse> records = new ArrayList<>(searchPage.getRecords());

        // 搜索结果不足时，用推荐补充
        if (records.size() < pageSize) {
            Set<Long> existingIds = records.stream().map(CourseResponse::getId).collect(Collectors.toSet());

            String userIdStr = userService.getCurrentUserId();
            Long userId = IdUtil.parse(userIdStr);
            Page<Long> cfIds = recommendService.recommend(userId, 1, pageSize);

            Set<Long> idsToFetch = cfIds.getRecords().stream()
                    .filter(id -> !existingIds.contains(id))
                    .limit(pageSize - records.size())
                    .collect(Collectors.toSet());

            if (!idsToFetch.isEmpty()) {
                records.addAll(courseService.listCourseResponsesByIds(idsToFetch));
            }
        }

        searchPage.setRecords(records);
        return searchPage;
    }
}
