package com.rauio.smartdangjian.server.content.api;

import java.util.Collection;
import java.util.List;
import java.util.Map;

import com.rauio.smartdangjian.server.content.api.dto.ChapterSummary;
import com.rauio.smartdangjian.server.content.pojo.response.ChapterResponse;

/**
 * 章节查询门面 —— 供搜索模块等业务方调用的稳定接口。
 */
public interface ChapterQueryFacade {

    ChapterResponse get(Long chapterId);

    List<ChapterResponse> getByCourseId(Long courseId);

    List<Long> listCourseIdsByChapterIds(Collection<Long> chapterIds);

    Map<Long, Long> getCourseIdMapByChapterIds(Collection<Long> chapterIds);

    /**
     * 根据标题关键词搜索章节。
     *
     * @param keyword 搜索关键词
     * @return 匹配的章节摘要列表
     */
    List<ChapterSummary> searchByTitle(String keyword);
}
