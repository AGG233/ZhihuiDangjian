package com.rauio.smartdangjian.server.content.controller.admin;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Collections;
import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.rauio.smartdangjian.exception.BusinessException;
import com.rauio.smartdangjian.pojo.response.Result;
import com.rauio.smartdangjian.server.content.constants.ChapterErrorConstants;
import com.rauio.smartdangjian.server.content.pojo.request.ChapterRequest;
import com.rauio.smartdangjian.server.content.pojo.response.ChapterResponse;
import com.rauio.smartdangjian.server.content.service.chapter.ChapterService;

@ExtendWith(MockitoExtension.class)
@DisplayName("AdminChapterController 单元测试")
class AdminChapterControllerTest {

    @Mock
    private ChapterService chapterService;

    @InjectMocks
    private AdminChapterController controller;

    // ================================================================
    // GET /{id}
    // ================================================================

    @Test
    @DisplayName("get 根据章节 ID 返回 ChapterResponse")
    void getShouldReturnChapterResponse() {
        ChapterResponse vo = ChapterResponse.builder()
                .id(1L)
                .courseId(1L)
                .title("第一章")
                .description("第一章描述")
                .duration(1800)
                .orderIndex(1)
                .isOptional(false)
                .chapterStatus("published")
                .build();
        when(chapterService.get(1L)).thenReturn(vo);

        Result<ChapterResponse> result = controller.get(1L);

        assertThat(result).isNotNull();
        assertThat(result.getData()).isNotNull();
        assertThat(result.getData().getId()).isEqualTo(1L);
        assertThat(result.getData().getTitle()).isEqualTo("第一章");
    }

    @Test
    @DisplayName("get 返回的 Result 包含成功状态码")
    void getShouldReturnSuccessResult() {
        ChapterResponse vo =
                ChapterResponse.builder().id(1L).courseId(1L).title("第一章").build();
        when(chapterService.get(1L)).thenReturn(vo);

        Result<ChapterResponse> result = controller.get(1L);

        assertThat(result.getCode()).isEqualTo("200");
        assertThat(result.getMessage()).isEqualTo("OK");
    }

    // ================================================================
    // GET /by-course/{courseId}
    // ================================================================

    @Test
    @DisplayName("getByCourseId 根据课程 ID 返回章节列表")
    void getByCourseIdShouldReturnChapterResponseList() {
        List<ChapterResponse> vos = List.of(
                ChapterResponse.builder().id(1L).title("第一章").build(),
                ChapterResponse.builder().id(2L).title("第二章").build());
        when(chapterService.getByCourseId(1L)).thenReturn(vos);

        Result<List<ChapterResponse>> result = controller.getByCourseId(1L);

        assertThat(result).isNotNull();
        assertThat(result.getData()).hasSize(2);
        assertThat(result.getData().get(0).getTitle()).isEqualTo("第一章");
        assertThat(result.getData().get(1).getTitle()).isEqualTo("第二章");
    }

    @Test
    @DisplayName("getByCourseId 课程无章节时返回空列表")
    void getByCourseIdShouldReturnEmptyListWhenNoChapters() {
        when(chapterService.getByCourseId(1L)).thenReturn(Collections.emptyList());

        Result<List<ChapterResponse>> result = controller.getByCourseId(1L);

        assertThat(result).isNotNull();
        assertThat(result.getData()).isEmpty();
    }

    @Test
    @DisplayName("getByCourseId 返回的 Result 包含成功状态码")
    void getByCourseIdShouldReturnSuccessResult() {
        when(chapterService.getByCourseId(1L)).thenReturn(Collections.emptyList());

        Result<List<ChapterResponse>> result = controller.getByCourseId(1L);

        assertThat(result.getCode()).isEqualTo("200");
        assertThat(result.getMessage()).isEqualTo("OK");
    }

    // ================================================================
    // POST
    // ================================================================

    @Test
    @DisplayName("create 创建章节成功时返回空 Result")
    void createShouldReturnSuccess() {
        ChapterRequest dto = ChapterRequest.builder()
                .courseId("1")
                .title("新章节")
                .description("描述")
                .duration(1800)
                .orderIndex(1)
                .build();
        doNothing().when(chapterService).create(any(ChapterRequest.class));

        Result<Void> result = controller.create(dto);

        assertThat(result).isNotNull();
        assertThat(result.getData()).isNull();
        assertThat(result.getCode()).isEqualTo("200");
        verify(chapterService).create(dto);
    }

    @Test
    @DisplayName("create Service 抛出 BusinessException 时异常向上传播")
    void createShouldThrowWhenServiceThrows() {
        ChapterRequest dto = ChapterRequest.builder()
                .courseId("1")
                .title("失败章节")
                .description("描述")
                .duration(1800)
                .orderIndex(1)
                .build();
        doThrow(new BusinessException(3103, "章节无法创建")).when(chapterService).create(any(ChapterRequest.class));

        assertThatThrownBy(() -> controller.create(dto))
                .isInstanceOf(BusinessException.class)
                .extracting("code")
                .isEqualTo(ChapterErrorConstants.CHAPTER_CREATE_FAILED);
    }

    // ================================================================
    // PUT
    // ================================================================

    @Test
    @DisplayName("update 更新章节成功时返回空 Result")
    void updateShouldReturnSuccess() {
        ChapterRequest dto =
                ChapterRequest.builder().title("更新章节").description("更新描述").build();
        doNothing().when(chapterService).update(dto, 1L);

        Result<Void> result = controller.update(1L, dto);

        assertThat(result).isNotNull();
        assertThat(result.getData()).isNull();
        assertThat(result.getCode()).isEqualTo("200");
        verify(chapterService).update(dto, 1L);
    }

    @Test
    @DisplayName("update 章节不存在时抛出 BusinessException")
    void updateShouldThrowWhenServiceThrows() {
        ChapterRequest dto =
                ChapterRequest.builder().title("失败更新").description("描述").build();
        doThrow(new BusinessException(3101, "章节不存在")).when(chapterService).update(dto, 999L);

        assertThatThrownBy(() -> controller.update(999L, dto))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("章节不存在");
    }

    // ================================================================
    // DELETE /{id}
    // ================================================================

    @Test
    @DisplayName("delete 删除章节成功时返回空 Result")
    void deleteShouldReturnSuccess() {
        doNothing().when(chapterService).delete(1L);

        Result<Void> result = controller.delete(1L);

        assertThat(result).isNotNull();
        assertThat(result.getData()).isNull();
        assertThat(result.getCode()).isEqualTo("200");
        verify(chapterService).delete(1L);
    }

    @Test
    @DisplayName("delete 删除不存在的章节时抛出 BusinessException")
    void deleteShouldThrowWhenServiceThrows() {
        doThrow(new BusinessException(3101, "章节不存在")).when(chapterService).delete(999L);

        assertThatThrownBy(() -> controller.delete(999L))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("章节不存在");
    }
}
