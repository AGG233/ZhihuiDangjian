package com.rauio.smartdangjian.server.content.spec;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class BlockTypeTest {

    @Test
    @DisplayName("所有 BlockType 常量 type 字段不为空")
    void allBlockTypesHaveType() {
        for (BlockType bt : BlockType.values()) {
            assertThat(bt.getType()).isNotNull();
        }
    }

    @Test
    @DisplayName("BlockType 枚举值正确")
    void blockTypeValues() {
        assertThat(BlockType.valueOf("Heading")).isEqualTo(BlockType.Heading);
        assertThat(BlockType.valueOf("Paragraph")).isEqualTo(BlockType.Paragraph);
        assertThat(BlockType.valueOf("Image")).isEqualTo(BlockType.Image);
        assertThat(BlockType.valueOf("Video")).isEqualTo(BlockType.Video);
        assertThat(BlockType.valueOf("Attachment")).isEqualTo(BlockType.Attachment);
        assertThat(BlockType.valueOf("Audio")).isEqualTo(BlockType.Audio);
    }
}
