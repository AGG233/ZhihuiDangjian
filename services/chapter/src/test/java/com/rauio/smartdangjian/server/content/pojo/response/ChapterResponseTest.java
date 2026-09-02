package com.rauio.smartdangjian.server.content.pojo.response;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.rauio.smartdangjian.server.content.pojo.entity.ChapterContentBlock;

class ChapterResponseTest {

    @Test
    @DisplayName("builder 构造 ChapterResponse 所有字段值正确")
    void builderCreatesChapterResponseCorrectly() {
        List<ChapterContentBlock> content = List.of(
                ChapterContentBlock.builder().id(1L).textContent("内容1").build(),
                ChapterContentBlock.builder().id(1L).textContent("内容2").build());

        ChapterResponse vo = ChapterResponse.builder()
                .id(1L)
                .courseId(1L)
                .title("第一章")
                .description("章节描述")
                .duration(1800)
                .orderIndex(1)
                .isOptional(false)
                .chapterStatus("published")
                .content(content)
                .build();

        assertThat(vo.getId()).isEqualTo(1L);
        assertThat(vo.getCourseId()).isEqualTo(1L);
        assertThat(vo.getTitle()).isEqualTo("第一章");
        assertThat(vo.getDescription()).isEqualTo("章节描述");
        assertThat(vo.getDuration()).isEqualTo(1800);
        assertThat(vo.getOrderIndex()).isEqualTo(1);
        assertThat(vo.getIsOptional()).isFalse();
        assertThat(vo.getChapterStatus()).isEqualTo("published");
        assertThat(vo.getContent()).hasSize(2);
        assertThat(vo.getContent().get(0).getTextContent()).isEqualTo("内容1");
    }

    @Test
    @DisplayName("builder content 为 null 时正常工作")
    void builderWithNullContent() {
        ChapterResponse vo =
                ChapterResponse.builder().id(1L).title("无内容章节").content(null).build();

        assertThat(vo.getContent()).isNull();
    }

    @Test
    @DisplayName("isOptional 为 true 时正确返回")
    void isOptionalTrue() {
        ChapterResponse vo = ChapterResponse.builder().id(1L).isOptional(true).build();

        assertThat(vo.getIsOptional()).isTrue();
    }
}
