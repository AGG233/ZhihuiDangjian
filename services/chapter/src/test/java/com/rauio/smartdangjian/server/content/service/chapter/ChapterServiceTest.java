package com.rauio.smartdangjian.server.content.service.chapter;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Collections;
import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.rauio.smartdangjian.exception.BusinessException;
import com.rauio.smartdangjian.server.content.pojo.convertor.ChapterContentBlockConvertor;
import com.rauio.smartdangjian.server.content.pojo.convertor.ChapterConvertor;
import com.rauio.smartdangjian.server.content.pojo.dto.ContentBlockDto;
import com.rauio.smartdangjian.server.content.pojo.entity.Chapter;
import com.rauio.smartdangjian.server.content.pojo.entity.ChapterContentBlock;
import com.rauio.smartdangjian.server.content.pojo.request.ChapterRequest;
import com.rauio.smartdangjian.server.content.pojo.response.ChapterResponse;
import com.rauio.smartdangjian.server.content.service.ChapterContentBlockService;

@ExtendWith(MockitoExtension.class)
class ChapterServiceTest {

    @Mock
    private ChapterContentBlockService contentService;

    @Mock
    private ChapterConvertor chapterConvertor;

    @Mock
    private ChapterContentBlockConvertor contentBlockConvertor;

    @Spy
    @InjectMocks
    private ChapterService chapterService;

    // ================================================================
    // get
    // ================================================================

    @Test
    @DisplayName("get 根据章节 ID 返回 ChapterResponse")
    void getReturnsChapterResponse() {
        Chapter chapter = Chapter.builder().id(1L).title("第一章").build();
        ChapterResponse vo = ChapterResponse.builder().id(1L).title("第一章").build();
        doReturn(chapter).when(chapterService).getById(1L);
        when(chapterConvertor.toResponse(chapter)).thenReturn(vo);

        ChapterResponse result = chapterService.get(1L);

        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(1L);
        assertThat(result.getTitle()).isEqualTo("第一章");
    }

    @Test
    @DisplayName("get 章节不存在时抛出 BusinessException")
    void getThrowsExceptionWhenChapterNotFound() {
        doReturn(null).when(chapterService).getById(999L);

        assertThatThrownBy(() -> chapterService.get(999L))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("章节不存在");
    }

    // ================================================================
    // create
    // ================================================================

    @Test
    @DisplayName("create 创建章节及其内容块成功返回 true")
    void createChapterSuccessfully() {
        ChapterRequest dto = ChapterRequest.builder()
                .courseId("1")
                .title("新章节")
                .description("描述")
                .orderIndex(1)
                .content(List.of(ContentBlockDto.builder().textContent("文本内容").build()))
                .build();

        Chapter chapter = Chapter.builder().title("新章节").build();
        chapter.setId(1L);

        ChapterContentBlock block =
                ChapterContentBlock.builder().chapterId(1L).textContent("文本内容").build();

        doReturn(null).when(chapterService).getOne(any(LambdaQueryWrapper.class));
        when(chapterConvertor.toEntity(dto)).thenReturn(chapter);
        doReturn(true).when(chapterService).save(chapter);
        when(contentBlockConvertor.toEntity(any(ContentBlockDto.class))).thenReturn(block);
        doReturn(true).when(contentService).create(any(ChapterContentBlock.class));

        Boolean result = chapterService.create(dto);

        assertThat(result).isTrue();
        verify(contentService).create(any(ChapterContentBlock.class));
    }

    @Test
    @DisplayName("create 章节已存在时抛出 BusinessException")
    void createThrowsExceptionWhenChapterExists() {
        ChapterRequest dto = ChapterRequest.builder()
                .courseId("1")
                .title("重复章节")
                .description("描述")
                .orderIndex(1)
                .content(List.of(ContentBlockDto.builder().textContent("内容").build()))
                .build();

        Chapter existing = Chapter.builder().id(1L).title("重复章节").build();
        doReturn(existing).when(chapterService).getOne(any(LambdaQueryWrapper.class));

        assertThatThrownBy(() -> chapterService.create(dto))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("章节已存在");
    }

    @Test
    @DisplayName("create 保存章节失败时抛出 BusinessException")
    void createThrowsExceptionWhenSaveFails() {
        ChapterRequest dto = ChapterRequest.builder()
                .courseId("1")
                .title("失败章节")
                .description("描述")
                .orderIndex(1)
                .content(List.of(ContentBlockDto.builder().textContent("内容").build()))
                .build();

        Chapter chapter = Chapter.builder().title("失败章节").build();
        doReturn(null).when(chapterService).getOne(any(LambdaQueryWrapper.class));
        when(chapterConvertor.toEntity(dto)).thenReturn(chapter);
        doReturn(false).when(chapterService).save(chapter);

        assertThatThrownBy(() -> chapterService.create(dto))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("章节无法创建");
    }

    @Test
    @DisplayName("create content 为 null 时抛出 BusinessException")
    void createThrowsExceptionWhenContentIsNull() {
        ChapterRequest dto = ChapterRequest.builder()
                .courseId("1")
                .title("无内容章节")
                .description("描述")
                .orderIndex(1)
                .content(null)
                .build();

        Chapter chapter = Chapter.builder().title("无内容章节").build();
        doReturn(null).when(chapterService).getOne(any(LambdaQueryWrapper.class));
        when(chapterConvertor.toEntity(dto)).thenReturn(chapter);
        doReturn(true).when(chapterService).save(chapter);

        assertThatThrownBy(() -> chapterService.create(dto))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("课程至少需要一个章节");
    }

    @Test
    @DisplayName("create content 为空列表时抛出 BusinessException")
    void createThrowsExceptionWhenContentIsEmpty() {
        ChapterRequest dto = ChapterRequest.builder()
                .courseId("1")
                .title("空内容章节")
                .description("描述")
                .orderIndex(1)
                .content(Collections.emptyList())
                .build();

        Chapter chapter = Chapter.builder().title("空内容章节").build();
        doReturn(null).when(chapterService).getOne(any(LambdaQueryWrapper.class));
        when(chapterConvertor.toEntity(dto)).thenReturn(chapter);
        doReturn(true).when(chapterService).save(chapter);

        assertThatThrownBy(() -> chapterService.create(dto))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("课程至少需要一个章节");
    }

    // ================================================================
    // update
    // ================================================================

    @Test
    @DisplayName("update 更新章节成功时调用 updateById 且 id 已设置")
    void updateChapterSuccessfully() {
        ChapterRequest dto =
                ChapterRequest.builder().title("更新章节").description("更新描述").build();
        Chapter entity = Chapter.builder().title("更新章节").build();
        Chapter existing = Chapter.builder().id(1L).title("旧章节").build();
        doReturn(existing).when(chapterService).getById(1L);
        when(chapterConvertor.toEntity(dto)).thenReturn(entity);
        doReturn(true).when(chapterService).updateById(any(Chapter.class));

        chapterService.update(dto, 1L);

        assertThat(entity.getId()).isEqualTo(1L);
        verify(chapterService).updateById(entity);
    }

    @Test
    @DisplayName("update 章节不存在时抛出 BusinessException")
    void updateThrowsExceptionWhenChapterNotFound() {
        ChapterRequest dto = ChapterRequest.builder().title("更新章节").build();
        doReturn(null).when(chapterService).getById(999L);

        assertThatThrownBy(() -> chapterService.update(dto, 999L))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("章节不存在");
    }

    // ================================================================
    // getByCourseId
    // ================================================================

    @Test
    @DisplayName("getByCourseId 根据课程 ID 返回章节列表")
    void getByCourseIdReturnsChapterResponseList() {
        List<Chapter> chapters = List.of(
                Chapter.builder().id(1L).courseId(1L).title("第一章").build(),
                Chapter.builder().id(1L).courseId(1L).title("第二章").build());
        List<ChapterResponse> vos = List.of(
                ChapterResponse.builder().id(1L).title("第一章").build(),
                ChapterResponse.builder().id(2L).title("第二章").build());
        doReturn(chapters).when(chapterService).list(any(LambdaQueryWrapper.class));
        when(chapterConvertor.toResponseList(chapters)).thenReturn(vos);

        List<ChapterResponse> result = chapterService.getByCourseId(1L);

        assertThat(result).hasSize(2);
        assertThat(result.get(0).getTitle()).isEqualTo("第一章");
    }

    @Test
    @DisplayName("getByCourseId 课程无章节时返回空列表")
    void getByCourseIdReturnsEmptyListWhenNoChapters() {
        doReturn(Collections.emptyList()).when(chapterService).list(any(LambdaQueryWrapper.class));
        when(chapterConvertor.toResponseList(Collections.emptyList())).thenReturn(Collections.emptyList());

        List<ChapterResponse> result = chapterService.getByCourseId(1L);

        assertThat(result).isEmpty();
    }

    // ================================================================
    // delete
    // ================================================================

    @Test
    @DisplayName("delete 删除存在的章节时调用 removeById")
    void deleteChapterSuccessfully() {
        Chapter existing = Chapter.builder().id(1L).title("第一章").build();
        doReturn(existing).when(chapterService).getById(1L);
        doReturn(true).when(chapterService).removeById(1L);

        chapterService.delete(1L);

        verify(chapterService).removeById(1L);
    }

    @Test
    @DisplayName("delete 删除不存在的章节时抛出 BusinessException")
    void deleteThrowsExceptionWhenChapterNotFound() {
        doReturn(null).when(chapterService).getById(999L);

        assertThatThrownBy(() -> chapterService.delete(999L))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("章节不存在");
    }
}
