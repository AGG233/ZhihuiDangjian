package com.rauio.smartdangjian.server.content.pojo.entity;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDateTime;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.rauio.smartdangjian.server.content.spec.BlockType;
import com.rauio.smartdangjian.server.content.spec.ParentType;

class ContentBlockTest {

    @Test
    @DisplayName("builder 构造 ContentBlock 所有字段值正确")
    void builderCreatesContentBlockCorrectly() {
        LocalDateTime now = LocalDateTime.of(2025, 6, 1, 12, 0);
        ContentBlock block = ContentBlock.builder()
                .id("cb-001")
                .parentId("ch-001")
                .orderIndex(1)
                .parentType(ParentType.chapter)
                .blockType(BlockType.Heading)
                .textContent("标题文本")
                .resourceId(null)
                .caption("说明文字")
                .createdAt(now)
                .updatedAt(now)
                .build();

        assertThat(block.getId()).isEqualTo("cb-001");
        assertThat(block.getParentId()).isEqualTo("ch-001");
        assertThat(block.getOrderIndex()).isEqualTo(1);
        assertThat(block.getParentType()).isEqualTo(ParentType.chapter);
        assertThat(block.getBlockType()).isEqualTo(BlockType.Heading);
        assertThat(block.getTextContent()).isEqualTo("标题文本");
        assertThat(block.getResourceId()).isNull();
        assertThat(block.getCaption()).isEqualTo("说明文字");
        assertThat(block.getCreatedAt()).isEqualTo(now);
        assertThat(block.getUpdatedAt()).isEqualTo(now);
    }

    @Test
    @DisplayName("builder 构造 ContentBlock 父类型为 article")
    void builderCreatesContentBlockWithArticleParent() {
        ContentBlock block = ContentBlock.builder()
                .id("cb-002")
                .parentType(ParentType.article)
                .blockType(BlockType.Paragraph)
                .textContent("段落文本")
                .build();

        assertThat(block.getParentType()).isEqualTo(ParentType.article);
        assertThat(block.getBlockType()).isEqualTo(BlockType.Paragraph);
    }

    @Test
    @DisplayName("builder 构造 ContentBlock 类型为 Image 含 resourceId")
    void builderCreatesImageContentBlock() {
        ContentBlock block = ContentBlock.builder()
                .id("cb-003")
                .blockType(BlockType.Image)
                .resourceId("res-img-001")
                .build();

        assertThat(block.getBlockType()).isEqualTo(BlockType.Image);
        assertThat(block.getResourceId()).isEqualTo("res-img-001");
    }

    @Test
    @DisplayName("builder 构造 ContentBlock 类型为 Video")
    void builderCreatesVideoContentBlock() {
        ContentBlock block = ContentBlock.builder()
                .id("cb-004")
                .blockType(BlockType.Video)
                .resourceId("res-video-001")
                .build();

        assertThat(block.getBlockType()).isEqualTo(BlockType.Video);
    }

    @Test
    @DisplayName("builder 构造 ContentBlock 类型为 Attachment")
    void builderCreatesAttachmentContentBlock() {
        ContentBlock block = ContentBlock.builder()
                .id("cb-005")
                .blockType(BlockType.Attachment)
                .resourceId("res-file-001")
                .build();

        assertThat(block.getBlockType()).isEqualTo(BlockType.Attachment);
    }

    @Test
    @DisplayName("builder 构造 ContentBlock 类型为 Audio")
    void builderCreatesAudioContentBlock() {
        ContentBlock block = ContentBlock.builder()
                .id("cb-006")
                .blockType(BlockType.Audio)
                .resourceId("res-audio-001")
                .build();

        assertThat(block.getBlockType()).isEqualTo(BlockType.Audio);
    }

    @Test
    @DisplayName("setter 修改 textContent 后 getter 返回新值")
    void setterAndGetterWorkForTextContent() {
        ContentBlock block =
                ContentBlock.builder().id("cb-001").textContent("旧内容").build();

        block.setTextContent("新内容");

        assertThat(block.getTextContent()).isEqualTo("新内容");
    }

    @Test
    @DisplayName("两个相同字段的 ContentBlock equals 和 hashCode 行为符合 @Data 预期")
    void equalsAndHashCodeBehavior() {
        ContentBlock b1 = ContentBlock.builder()
                .id("cb-001")
                .textContent("内容")
                .blockType(BlockType.Paragraph)
                .build();
        ContentBlock b2 = ContentBlock.builder()
                .id("cb-001")
                .textContent("内容")
                .blockType(BlockType.Paragraph)
                .build();

        assertThat(b1).isEqualTo(b2);
        assertThat(b1.hashCode()).isEqualTo(b2.hashCode());
    }

    @Test
    @DisplayName("toString 包含主要字段")
    void toStringContainsKeyFields() {
        ContentBlock block = ContentBlock.builder()
                .id("cb-001")
                .textContent("测试内容")
                .blockType(BlockType.Heading)
                .build();

        String str = block.toString();

        assertThat(str).contains("cb-001", "测试内容", "Heading");
    }
}
