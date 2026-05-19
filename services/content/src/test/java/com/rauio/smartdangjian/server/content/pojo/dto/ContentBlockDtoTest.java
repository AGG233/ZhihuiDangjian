package com.rauio.smartdangjian.server.content.pojo.dto;

import com.rauio.smartdangjian.server.content.spec.BlockType;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class ContentBlockDtoTest {

    @Test
    @DisplayName("builder 构造 ContentBlockDto 文本类型所有字段正确")
    void builderCreatesTextContentBlockDto() {
        ContentBlockDto dto = ContentBlockDto.builder()
                .blockType(BlockType.Paragraph)
                .textContent("段落文本内容")
                .caption("说明")
                .build();

        assertThat(dto.getBlockType()).isEqualTo(BlockType.Paragraph);
        assertThat(dto.getTextContent()).isEqualTo("段落文本内容");
        assertThat(dto.getCaption()).isEqualTo("说明");
        assertThat(dto.getResourceId()).isNull();
    }

    @Test
    @DisplayName("builder 构造 ContentBlockDto 文件类型含 resourceId")
    void builderCreatesFileContentBlockDto() {
        ContentBlockDto dto = ContentBlockDto.builder()
                .blockType(BlockType.Image)
                .resourceId("res-img-001")
                .caption("图片说明")
                .build();

        assertThat(dto.getBlockType()).isEqualTo(BlockType.Image);
        assertThat(dto.getResourceId()).isEqualTo("res-img-001");
        assertThat(dto.getCaption()).isEqualTo("图片说明");
    }

    @Test
    @DisplayName("builder 构造 ContentBlockDto heading 类型")
    void builderCreatesHeadingContentBlockDto() {
        ContentBlockDto dto = ContentBlockDto.builder()
                .blockType(BlockType.Heading)
                .textContent("一级标题")
                .build();

        assertThat(dto.getBlockType()).isEqualTo(BlockType.Heading);
        assertThat(dto.getTextContent()).isEqualTo("一级标题");
    }

    @Test
    @DisplayName("setter 修改 blockType 后 getter 返回新值")
    void setterAndGetterWorkForBlockType() {
        ContentBlockDto dto = ContentBlockDto.builder()
                .blockType(BlockType.Paragraph)
                .build();

        dto.setBlockType(BlockType.Heading);

        assertThat(dto.getBlockType()).isEqualTo(BlockType.Heading);
    }
}
