package com.rauio.ZhihuiDangjiang.service.impl;

import com.rauio.ZhihuiDangjiang.dao.ChapterDao;
import com.rauio.ZhihuiDangjiang.exception.BusinessException;
import com.rauio.ZhihuiDangjiang.pojo.Chapter;
import com.rauio.ZhihuiDangjiang.pojo.ContentBlock;
import com.rauio.ZhihuiDangjiang.pojo.convertor.ChapterConvertor;
import com.rauio.ZhihuiDangjiang.pojo.convertor.ContentBlockConvertor;
import com.rauio.ZhihuiDangjiang.pojo.dto.ChapterDto;
import com.rauio.ZhihuiDangjiang.pojo.dto.ContentBlockDto;
import com.rauio.ZhihuiDangjiang.service.ContentBlockService;
import com.rauio.ZhihuiDangjiang.service.ResourceService;
import com.rauio.ZhihuiDangjiang.utils.Spec.ParentType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class ChapterServiceImplTest {

    @Mock
    private ChapterDao chapterDao;

    @Mock
    private ResourceService resourceService;

    @Mock
    private ContentBlockService contentService;

    @Mock
    private ChapterConvertor chapterConvertor;

    @Mock
    private ContentBlockConvertor contentBlockConvertor;

    @InjectMocks
    private ChapterServiceImpl chapterService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void create_ShouldThrowBusinessException_WhenChapterAlreadyExists() {
        // Given
        ChapterDto chapterDto = ChapterDto.builder()
                .courseId("course1")
                .title("Chapter 1")
                .build();

        Chapter existingChapter = Chapter.builder().build();
        when(chapterDao.getByCourseAndTitle("course1", "Chapter 1")).thenReturn(existingChapter);

        // When & Then
        BusinessException exception = assertThrows(BusinessException.class, () -> {
            chapterService.create(chapterDto);
        });

        assertEquals(4000, exception.getCode());
        assertEquals("章节已存在", exception.getMessage());
        verify(chapterDao, times(1)).getByCourseAndTitle("course1", "Chapter 1");
        verify(chapterDao, never()).insert(any());
    }

    @Test
    void create_ShouldThrowBusinessException_WhenChapterHasNoContent() {
        // Given
        ChapterDto chapterDto = ChapterDto.builder()
                .courseId("course1")
                .title("Chapter 1")
                .content(null) // 明确设置content为null
                .build();

        Chapter chapterEntity = Chapter.builder().id("chapter1").build();

        when(chapterDao.getByCourseAndTitle("course1", "Chapter 1"))
                .thenReturn(null)  // 第一次检查
                .thenReturn(chapterEntity); // 插入后再次检查
        when(chapterConvertor.toEntity(chapterDto)).thenReturn(chapterEntity);

        // When & Then
        BusinessException exception = assertThrows(BusinessException.class, () -> {
            chapterService.create(chapterDto);
        });

        assertEquals(4000, exception.getCode());
        assertEquals("课程至少需要一个章节", exception.getMessage());
        verify(chapterDao, times(1)).getByCourseAndTitle("course1", "Chapter 1");
        verify(chapterDao, times(1)).insert(chapterEntity);
        // 由于content为null，contentBlockConvertor.toEntity不会被调用
        verify(contentBlockConvertor, never()).toEntity(any());
        verify(contentService, never()).save(any(ContentBlock.class));
    }

    @Test
    void create_ShouldCreateChapterAndContentBlocks_WhenChapterHasContent() {
        // Given
        ContentBlockDto blockDto1 = ContentBlockDto.builder().build();
        ContentBlockDto blockDto2 = ContentBlockDto.builder().build();

        ChapterDto chapterDto = ChapterDto.builder()
                .courseId("course1")
                .title("Chapter 1")
                .content(List.of(blockDto1, blockDto2))
                .build();

        Chapter chapterEntity = Chapter.builder().id("chapter1").build();

        ContentBlock block1 = ContentBlock.builder().build();
        ContentBlock block2 = ContentBlock.builder().build();

        when(chapterDao.getByCourseAndTitle("course1", "Chapter 1"))
                .thenReturn(null)  // 第一次检查
                .thenReturn(chapterEntity); // 插入后再次检查
        when(chapterConvertor.toEntity(chapterDto)).thenReturn(chapterEntity);
        when(contentBlockConvertor.toEntity(any(ContentBlockDto.class)))
                .thenReturn(block1)  // 第一次调用返回block1
                .thenReturn(block2); // 第二次调用返回block2;
        when(contentService.save(any(ContentBlock.class))).thenReturn(true);

        // When
        Boolean result = chapterService.create(chapterDto);

        // Then
        assertTrue(result);
        verify(chapterDao, times(2)).getByCourseAndTitle("course1", "Chapter 1");
        verify(chapterDao, times(1)).insert(chapterEntity);
        verify(contentBlockConvertor, times(2)).toEntity(any(ContentBlockDto.class));
        verify(contentService, times(2)).save(any(ContentBlock.class));

        // Verify content blocks are properly configured
        assertEquals("chapter1", block1.getParentId());
        assertEquals(ParentType.chapter, block1.getParentType());
        assertEquals("chapter1", block2.getParentId());
        assertEquals(ParentType.chapter, block2.getParentType());
    }
}