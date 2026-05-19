package com.rauio.smartdangjian.server.content.pojo.vo;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.rauio.smartdangjian.server.content.pojo.entity.ContentBlock;

class ChapterVOTest {

    @Test
    @DisplayName("builder 构造 ChapterVO 所有字段值正确")
    void builderCreatesChapterVOCorrectly() {
        List<ContentBlock> content = List.of(
                ContentBlock.builder().id("cb-001").textContent("内容1").build(),
                ContentBlock.builder().id("cb-002").textContent("内容2").build());

        ChapterVO vo = ChapterVO.builder()
                .id("ch-001")
                .courseId("course-001")
                .title("第一章")
                .description("章节描述")
                .duration(1800)
                .orderIndex(1)
                .isOptional(false)
                .chapterStatus("published")
                .content(content)
                .build();

        assertThat(vo.getId()).isEqualTo("ch-001");
        assertThat(vo.getCourseId()).isEqualTo("course-001");
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
        ChapterVO vo =
                ChapterVO.builder().id("ch-001").title("无内容章节").content(null).build();

        assertThat(vo.getContent()).isNull();
    }

    @Test
    @DisplayName("isOptional 为 true 时正确返回")
    void isOptionalTrue() {
        ChapterVO vo = ChapterVO.builder().id("ch-001").isOptional(true).build();

        assertThat(vo.getIsOptional()).isTrue();
    }
}
