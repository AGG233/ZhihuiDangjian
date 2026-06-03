package com.rauio.smartdangjian.server.content.api;

import java.util.Collection;
import java.util.List;
import java.util.Map;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.rauio.smartdangjian.server.content.api.dto.CourseSummary;
import com.rauio.smartdangjian.server.content.pojo.response.CourseResponse;

/**
 * 课程查询门面 —— 供搜索模块等业务方调用的稳定接口。
 */
public interface CourseQueryFacade {

    CourseResponse get(Long courseId);

    /**
     * 根据 ID 获取课程摘要（不过滤 isPublished 状态）。
     * 适用于需要看到未发布课程的场景，如内容审查。
     *
     * @param courseId 课程 ID
     * @return 课程摘要 DTO，课程不存在返回 null
     */
    CourseSummary getSummary(Long courseId);

    List<Long> listTopCategoryIdsByCourseIds(Collection<Long> courseIds, int limit);

    Page<Long> recommendPublishedCourseIds(
            Collection<Long> interestCategoryIds,
            Collection<Long> excludedCourseIds,
            String difficulty,
            int pageNum,
            int pageSize);

    Page<CourseResponse> searchPublishedCourses(
            String keyword, String categoryId, String difficulty, int pageNum, int pageSize);

    List<CourseResponse> listCourseResponsesByIds(Collection<Long> courseIds);

    Map<Long, CourseResponse> getCourseResponseMapByIds(Collection<Long> courseIds);

    /**
     * 根据标题关键词搜索已发布课程。
     *
     * @param keyword 标题关键词
     * @param limit   返回数量上限
     * @return 课程摘要列表
     */
    List<CourseSummary> searchByTitle(String keyword, int limit);
}
