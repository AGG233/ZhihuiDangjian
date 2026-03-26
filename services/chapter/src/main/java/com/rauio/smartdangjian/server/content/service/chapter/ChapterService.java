package com.rauio.smartdangjian.server.content.service.chapter;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.rauio.smartdangjian.exception.BusinessException;
import com.rauio.smartdangjian.server.content.mapper.ChapterMapper;
import com.rauio.smartdangjian.server.content.pojo.entity.Chapter;
import com.rauio.smartdangjian.server.content.pojo.entity.ContentBlock;
import com.rauio.smartdangjian.server.content.pojo.convertor.ChapterConvertor;
import com.rauio.smartdangjian.server.content.pojo.convertor.ContentBlockConvertor;
import com.rauio.smartdangjian.server.content.pojo.dto.ChapterDto;
import com.rauio.smartdangjian.server.content.pojo.vo.ChapterVO;
import com.rauio.smartdangjian.server.content.service.ContentBlockService;
import com.rauio.smartdangjian.server.content.spec.ParentType;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class ChapterService extends ServiceImpl<ChapterMapper, Chapter> {

    private final ContentBlockService contentService;
    private final ChapterConvertor chapterConvertor;
    private final ContentBlockConvertor contentBlockConvertor;

    /**
     * 根据章节 ID 获取章节详情。
     *
     * @param chapterId 章节ID
     * @return 章节
     */
    public ChapterVO get(String chapterId) {
        Chapter chapter = this.getById(chapterId);
        if (chapter == null) {
            throw new BusinessException(4000, "章节不存在");
        }


        return chapterConvertor.toVO(chapter);
    }

    /**
     * 创建章节及其内容块。
     *
     * @param dto 创建的新章节
     * @return 创建结果
     */
    public Boolean create(ChapterDto dto) {
        if (this.getOne(new LambdaQueryWrapper<Chapter>()
                .eq(Chapter::getCourseId, dto.getCourseId())
                .eq(Chapter::getTitle, dto.getTitle())) != null) {
            throw new BusinessException(4000, "章节已存在");
        }

        Chapter chapter = chapterConvertor.toEntity(dto);

        if (!this.save(chapter)){
            throw new BusinessException(4000, "章节无法创建");
        }

        if (dto.getContent() != null && !dto.getContent().isEmpty()) {
            dto.getContent().forEach(blockDto -> {
                ContentBlock block = contentBlockConvertor.toEntity(blockDto);
                block.setParentId(chapter.getId());
                block.setParentType(ParentType.chapter);
                contentService.save(block);
            });
        } else {
            throw new BusinessException(4000, "课程至少需要一个章节");
        }
        return true;
    }

    /**
     * 更新章节信息。
     *
     * @param dto 前端传入的章节
     * @return 修改结果
     */
    public Boolean update(ChapterDto dto) {
        return this.updateById(chapterConvertor.toEntity(dto));
    }

    /**
     * 查询课程下的全部章节。
     *
     * @param courseId 课程 ID
     * @return 课程所有章节
     */
    public List<ChapterVO> getByCourseId(String courseId) {
        List<Chapter> chapters = this.list(new LambdaQueryWrapper<Chapter>()
                .eq(Chapter::getCourseId, courseId));
        return chapterConvertor.toVOList(chapters);
    }

    /**
     * 删除章节。
     *
     * @param chapterId 章节ID
     * @return 删除结果
     */
    public Boolean delete(String chapterId) {
        return this.removeById(chapterId);
    }
}
