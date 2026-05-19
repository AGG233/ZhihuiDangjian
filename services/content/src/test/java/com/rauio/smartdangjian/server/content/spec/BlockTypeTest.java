package com.rauio.smartdangjian.server.content.spec;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class BlockTypeTest {

    @Test
    @DisplayName("Heading 类型值为 heading")
    void headingTypeValue() {
        assertThat(BlockType.Heading.getType()).isEqualTo("heading");
    }

    @Test
    @DisplayName("Paragraph 类型值为 paragraph")
    void paragraphTypeValue() {
        assertThat(BlockType.Paragraph.getType()).isEqualTo("paragraph");
    }

    @Test
    @DisplayName("Image 类型值为 image")
    void imageTypeValue() {
        assertThat(BlockType.Image.getType()).isEqualTo("image");
    }

    @Test
    @DisplayName("Video 类型值为 video")
    void videoTypeValue() {
        assertThat(BlockType.Video.getType()).isEqualTo("video");
    }

    @Test
    @DisplayName("Attachment 类型值为 attachment")
    void attachmentTypeValue() {
        assertThat(BlockType.Attachment.getType()).isEqualTo("attachment");
    }

    @Test
    @DisplayName("Audio 类型值为 audio")
    void audioTypeValue() {
        assertThat(BlockType.Audio.getType()).isEqualTo("audio");
    }

    @Test
    @DisplayName("枚举 values 长度为 6")
    void enumValuesLength() {
        assertThat(BlockType.values()).hasSize(6);
    }

    @Test
    @DisplayName("valueOf Heading 返回正确的枚举实例")
    void valueOfHeading() {
        assertThat(BlockType.valueOf("Heading")).isEqualTo(BlockType.Heading);
    }
}
