package com.rauio.smartdangjian.server.content.controller.admin;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import java.util.Collections;
import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.rauio.smartdangjian.pojo.response.Result;
import com.rauio.smartdangjian.server.content.pojo.dto.ChapterDto;
import com.rauio.smartdangjian.server.content.pojo.vo.ChapterVO;
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
    @DisplayName("get 根据章节 ID 返回 ChapterVO")
    void getShouldReturnChapterVO() {
        ChapterVO vo = ChapterVO.builder()
                .id("ch-001")
                .courseId("course-001")
                .title("第一章")
                .description("第一章描述")
                .duration(1800)
                .orderIndex(1)
                .isOptional(false)
                .chapterStatus("published")
                .build();
        when(chapterService.get("ch-001")).thenReturn(vo);

        Result<ChapterVO> result = controller.get("ch-001");

        assertThat(result).isNotNull();
        assertThat(result.getData()).isNotNull();
        assertThat(result.getData().getId()).isEqualTo("ch-001");
        assertThat(result.getData().getTitle()).isEqualTo("第一章");
    }

    @Test
    @DisplayName("get 返回的 Result 包含成功状态码")
    void getShouldReturnSuccessResult() {
        ChapterVO vo = ChapterVO.builder()
                .id("ch-001")
                .courseId("course-001")
                .title("第一章")
                .build();
        when(chapterService.get("ch-001")).thenReturn(vo);

        Result<ChapterVO> result = controller.get("ch-001");

        assertThat(result.getCode()).isEqualTo("200");
        assertThat(result.getMessage()).isEqualTo("OK");
    }

    // ================================================================
    // GET /by-course/{courseId}
    // ================================================================

    @Test
    @DisplayName("getByCourseId 根据课程 ID 返回章节列表")
    void getByCourseIdShouldReturnChapterVOList() {
        List<ChapterVO> vos = List.of(
                ChapterVO.builder().id("ch-001").title("第一章").build(),
                ChapterVO.builder().id("ch-002").title("第二章").build());
        when(chapterService.getByCourseId("course-001")).thenReturn(vos);

        Result<List<ChapterVO>> result = controller.getByCourseId("course-001");

        assertThat(result).isNotNull();
        assertThat(result.getData()).hasSize(2);
        assertThat(result.getData().get(0).getTitle()).isEqualTo("第一章");
        assertThat(result.getData().get(1).getTitle()).isEqualTo("第二章");
    }

    @Test
    @DisplayName("getByCourseId 课程无章节时返回空列表")
    void getByCourseIdShouldReturnEmptyListWhenNoChapters() {
        when(chapterService.getByCourseId("empty-course")).thenReturn(Collections.emptyList());

        Result<List<ChapterVO>> result = controller.getByCourseId("empty-course");

        assertThat(result).isNotNull();
        assertThat(result.getData()).isEmpty();
    }

    @Test
    @DisplayName("getByCourseId 返回的 Result 包含成功状态码")
    void getByCourseIdShouldReturnSuccessResult() {
        when(chapterService.getByCourseId("course-001")).thenReturn(Collections.emptyList());

        Result<List<ChapterVO>> result = controller.getByCourseId("course-001");

        assertThat(result.getCode()).isEqualTo("200");
        assertThat(result.getMessage()).isEqualTo("OK");
    }

    // ================================================================
    // POST
    // ================================================================

    @Test
    @DisplayName("create 创建章节成功时返回 true")
    void createShouldReturnTrue() {
        ChapterDto dto = ChapterDto.builder()
                .courseId("course-001")
                .title("新章节")
                .description("描述")
                .duration(1800)
                .orderIndex(1)
                .build();
        when(chapterService.create(any(ChapterDto.class))).thenReturn(true);

        Result<Boolean> result = controller.create(dto);

        assertThat(result).isNotNull();
        assertThat(result.getData()).isTrue();
        assertThat(result.getCode()).isEqualTo("200");
    }

    @Test
    @DisplayName("create 创建章节失败时返回 false")
    void createShouldReturnFalseWhenServiceFails() {
        ChapterDto dto = ChapterDto.builder()
                .courseId("course-001")
                .title("失败章节")
                .description("描述")
                .duration(1800)
                .orderIndex(1)
                .build();
        when(chapterService.create(any(ChapterDto.class))).thenReturn(false);

        Result<Boolean> result = controller.create(dto);

        assertThat(result).isNotNull();
        assertThat(result.getData()).isFalse();
        assertThat(result.getCode()).isEqualTo("200");
    }

    // ================================================================
    // PUT
    // ================================================================

    @Test
    @DisplayName("update 更新章节成功时返回 true")
    void updateShouldReturnTrue() {
        ChapterDto dto = ChapterDto.builder().title("更新章节").description("更新描述").build();
        when(chapterService.update(any(ChapterDto.class))).thenReturn(true);

        Result<Boolean> result = controller.update(dto);

        assertThat(result).isNotNull();
        assertThat(result.getData()).isTrue();
        assertThat(result.getCode()).isEqualTo("200");
    }

    @Test
    @DisplayName("update 更新章节失败时返回 false")
    void updateShouldReturnFalseWhenServiceFails() {
        ChapterDto dto = ChapterDto.builder().title("失败更新").description("描述").build();
        when(chapterService.update(any(ChapterDto.class))).thenReturn(false);

        Result<Boolean> result = controller.update(dto);

        assertThat(result).isNotNull();
        assertThat(result.getData()).isFalse();
        assertThat(result.getCode()).isEqualTo("200");
    }

    // ================================================================
    // DELETE /{id}
    // ================================================================

    @Test
    @DisplayName("delete 删除章节成功时返回 true")
    void deleteShouldReturnTrue() {
        when(chapterService.delete("ch-001")).thenReturn(true);

        Result<Boolean> result = controller.delete("ch-001");

        assertThat(result).isNotNull();
        assertThat(result.getData()).isTrue();
        assertThat(result.getCode()).isEqualTo("200");
    }

    @Test
    @DisplayName("delete 删除不存在的章节时返回 false")
    void deleteShouldReturnFalseWhenChapterNotFound() {
        when(chapterService.delete("non-existent")).thenReturn(false);

        Result<Boolean> result = controller.delete("non-existent");

        assertThat(result).isNotNull();
        assertThat(result.getData()).isFalse();
        assertThat(result.getCode()).isEqualTo("200");
    }
}
