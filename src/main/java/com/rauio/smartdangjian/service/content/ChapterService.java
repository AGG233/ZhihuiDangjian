package com.rauio.smartdangjian.service.content;

import com.rauio.smartdangjian.dao.ChapterDao;
import com.rauio.smartdangjian.exception.BusinessException;
import com.rauio.smartdangjian.pojo.Chapter;
import com.rauio.smartdangjian.pojo.ContentBlock;
import com.rauio.smartdangjian.pojo.convertor.ChapterConvertor;
import com.rauio.smartdangjian.pojo.convertor.ContentBlockConvertor;
import com.rauio.smartdangjian.pojo.dto.ChapterDto;
import com.rauio.smartdangjian.pojo.vo.ChapterVO;
import com.rauio.smartdangjian.service.ResourceService;
import com.rauio.smartdangjian.utils.spec.ParentType;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(rollbackFor = Exception.class, propagation = Propagation.REQUIRES_NEW)
public class ChapterService {

    private final ChapterDao chapterDao;
    private final ResourceService resourceService;
    private final ContentBlockService contentService;
    private final ChapterConvertor chapterConvertor;
    private final ContentBlockConvertor contentBlockConvertor;
    /**
     * @param chapterId 章节ID
     * @return 章节
     */
    public ChapterVO get(Long chapterId) {
        Chapter chapter = chapterDao.getById(chapterId);
        if (chapter == null) {
            throw new BusinessException(4000, "章节不存在");
        }


        return chapterConvertor.toVO(chapter);
    }
    /**
     * @param dto 创建的新章节
     * @return 创建结果
     */
    public Boolean create(ChapterDto dto) {
        if (chapterDao.getByCourseAndTitle(dto.getCourseId(), dto.getTitle()) != null) {
            throw new BusinessException(4000, "章节已存在");
        }

        Chapter chapter = chapterConvertor.toEntity(dto);

        if (!chapterDao.insert(chapter)){
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
     * @param dto 前端传入的章节
     * @return 修改结果
     */
    public Boolean update(ChapterDto dto) {
        return chapterDao.update(chapterConvertor.toEntity(dto));
    }
    /**
     * @return 课程所有章节
     */
    public List<ChapterVO> getAllChaptersOfCourse(String courseId) {
        List<Chapter> chapters = chapterDao.getAllChapterOfCourse(courseId);
        return chapterConvertor.toVOList(chapters);
    }
    /**
     * @param chapterId 章节ID
     * @return 删除结果
     */
    public Boolean delete(Long chapterId) {
        return chapterDao.delete(chapterId);
    }
}
