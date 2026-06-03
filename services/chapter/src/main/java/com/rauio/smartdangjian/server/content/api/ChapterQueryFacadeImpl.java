package com.rauio.smartdangjian.server.content.api;

import java.util.Collection;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;

import com.rauio.smartdangjian.server.content.api.dto.ChapterSummary;
import com.rauio.smartdangjian.server.content.pojo.entity.Chapter;
import com.rauio.smartdangjian.server.content.pojo.response.ChapterResponse;
import com.rauio.smartdangjian.server.content.service.chapter.ChapterService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ChapterQueryFacadeImpl implements ChapterQueryFacade {

    private final ChapterService chapterService;

    @Override
    public ChapterResponse get(Long chapterId) {
        return chapterService.get(chapterId);
    }

    @Override
    public List<ChapterResponse> getByCourseId(Long courseId) {
        return chapterService.getByCourseId(courseId);
    }

    @Override
    public List<Long> listCourseIdsByChapterIds(Collection<Long> chapterIds) {
        return chapterService.listCourseIdsByChapterIds(chapterIds);
    }

    @Override
    public Map<Long, Long> getCourseIdMapByChapterIds(Collection<Long> chapterIds) {
        return chapterService.getCourseIdMapByChapterIds(chapterIds);
    }

    @Override
    public List<ChapterSummary> searchByTitle(String keyword) {
        return chapterService.searchByTitle(keyword).stream()
                .map(ChapterQueryFacadeImpl::toSummary)
                .toList();
    }

    private static ChapterSummary toSummary(Chapter chapter) {
        return ChapterSummary.builder()
                .id(chapter.getId())
                .courseId(chapter.getCourseId())
                .title(chapter.getTitle())
                .description(chapter.getDescription())
                .orderIndex(chapter.getOrderIndex())
                .build();
    }
}
