package com.rauio.smartdangjian.server.chapter.service.chapter;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.rauio.smartdangjian.exception.BusinessException;
import com.rauio.smartdangjian.server.chapter.constants.ChapterErrorConstants;
import com.rauio.smartdangjian.server.chapter.mapper.ChapterMapper;
import com.rauio.smartdangjian.server.content.pojo.convertor.ChapterContentBlockConvertor;
import com.rauio.smartdangjian.server.chapter.pojo.convertor.ChapterConvertor;
import com.rauio.smartdangjian.server.chapter.pojo.entity.Chapter;
import com.rauio.smartdangjian.server.content.pojo.entity.ChapterContentBlock;
import com.rauio.smartdangjian.server.chapter.pojo.request.ChapterRequest;
import com.rauio.smartdangjian.server.chapter.pojo.response.ChapterResponse;
import com.rauio.smartdangjian.server.content.service.ChapterContentBlockService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ChapterService extends ServiceImpl<ChapterMapper, Chapter> {

    private final ChapterContentBlockService chapterContentService;
    private final ChapterConvertor chapterConvertor;
    private final ChapterContentBlockConvertor chapterContentBlockConvertor;

    /**
     * 根据章节 ID 获取章节详情。
     *
     * @param chapterId 章节ID
     * @return 章节
     */
    @Transactional(readOnly = true)
    public ChapterResponse get(Long chapterId) {
        Chapter chapter = this.getById(chapterId);
        if (chapter == null) {
            throw new BusinessException(ChapterErrorConstants.CHAPTER_NOT_FOUND, "章节不存在");
        }

        return chapterConvertor.toResponse(chapter);
    }

    /**
     * 创建章节及其内容块。
     *
     * @param dto 创建的新章节
     * @return 创建结果
     */
    @Transactional(rollbackFor = Exception.class)
    public Boolean create(ChapterRequest dto) {
        if (this.getOne(new LambdaQueryWrapper<Chapter>()
                        .eq(Chapter::getCourseId, dto.getCourseId())
                        .eq(Chapter::getTitle, dto.getTitle()))
                != null) {
            throw new BusinessException(ChapterErrorConstants.CHAPTER_ALREADY_EXISTS, "章节已存在");
        }

        Chapter chapter = chapterConvertor.toEntity(dto);

        if (!this.save(chapter)) {
            throw new BusinessException(ChapterErrorConstants.CHAPTER_CREATE_FAILED, "章节无法创建");
        }

        if (dto.getContent() != null && !dto.getContent().isEmpty()) {
            dto.getContent().forEach(blockDto -> {
                ChapterContentBlock block = chapterContentBlockConvertor.toEntity(blockDto);
                block.setChapterId(chapter.getId());
                chapterContentService.create(block);
            });
        } else {
            throw new BusinessException(ChapterErrorConstants.CHAPTER_MIN_REQUIRED, "课程至少需要一个章节");
        }
        return true;
    }

    /**
     * 更新章节信息。
     *
     * @param dto 前端传入的章节
     * @return 修改结果
     */
    @Transactional(rollbackFor = Exception.class)
    public Boolean update(ChapterRequest dto) {
        return this.updateById(chapterConvertor.toEntity(dto));
    }

    /**
     * 查询课程下的全部章节。
     *
     * @param courseId 课程 ID
     * @return 课程所有章节
     */
    @Transactional(readOnly = true)
    public List<ChapterResponse> getByCourseId(Long courseId) {
        List<Chapter> chapters = this.list(new LambdaQueryWrapper<Chapter>().eq(Chapter::getCourseId, courseId));
        return chapterConvertor.toResponseList(chapters);
    }

    @Transactional(readOnly = true)
    public Map<Long, Long> getCourseIdMapByChapterIds(Collection<Long> chapterIds) {
        if (chapterIds == null || chapterIds.isEmpty()) {
            return Collections.emptyMap();
        }
        return this.list(new LambdaQueryWrapper<Chapter>()
                        .in(Chapter::getId, chapterIds)
                        .select(Chapter::getId, Chapter::getCourseId))
                .stream()
                .filter(chapter -> chapter.getId() != null && chapter.getCourseId() != null)
                .collect(Collectors.toMap(Chapter::getId, Chapter::getCourseId, (a, b) -> a));
    }

    @Transactional(readOnly = true)
    public List<Long> listCourseIdsByChapterIds(Collection<Long> chapterIds) {
        return getCourseIdMapByChapterIds(chapterIds).values().stream()
                .filter(Objects::nonNull)
                .distinct()
                .toList();
    }

    /**
     * 根据关键词搜索章节。
     *
     * @param keyword 搜索关键词
     * @return 匹配的章节实体列表
     */
    @Transactional(readOnly = true)
    public List<Chapter> searchByTitle(String keyword) {
        return this.lambdaQuery().like(Chapter::getTitle, keyword).list();
    }

    /**
     * 删除章节。
     *
     * @param chapterId 章节ID
     * @return 删除结果
     */
    @Transactional(rollbackFor = Exception.class)
    public Boolean delete(Long chapterId) {
        return this.removeById(chapterId);
    }
}
