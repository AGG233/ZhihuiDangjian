package com.rauio.smartdangjian.common.utils;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;

@ExtendWith(MockitoExtension.class)
class MediaTypeUtilTest {

    @Test
    @DisplayName("detect 从 InputStream 检测图片类型")
    void detectFromInputStream() throws IOException {
        byte[] pngHeader = new byte[] {(byte) 0x89, 'P', 'N', 'G', 0x0D, 0x0A, 0x1A, 0x0A};
        InputStream inputStream = new ByteArrayInputStream(pngHeader);

        String result = MediaTypeUtil.detect(inputStream);

        assertThat(result).isEqualTo("image/png");
    }

    @Test
    @DisplayName("detect 从 InputStream 检测 JPEG 图片类型")
    void detectJpegFromInputStream() throws IOException {
        byte[] jpegHeader = new byte[] {(byte) 0xFF, (byte) 0xD8, (byte) 0xFF, (byte) 0xE0};
        InputStream inputStream = new ByteArrayInputStream(jpegHeader);

        String result = MediaTypeUtil.detect(inputStream);

        assertThat(result).isEqualTo("image/jpeg");
    }

    @Test
    @DisplayName("detect 从 InputStream 检测 GIF 图片类型")
    void detectGifFromInputStream() throws IOException {
        byte[] gifHeader = new byte[] {'G', 'I', 'F', '8', '9', 'a'};
        InputStream inputStream = new ByteArrayInputStream(gifHeader);

        String result = MediaTypeUtil.detect(inputStream);

        assertThat(result).isEqualTo("image/gif");
    }

    @Test
    @DisplayName("detect 从 InputStream 检测 WEBP 图片类型")
    void detectWebpFromInputStream() throws IOException {
        // WEBP header: RIFF....WEBP
        byte[] webpHeader = new byte[] {
                'R', 'I', 'F', 'F', 0x00, 0x00, 0x00, 0x00,
                'W', 'E', 'B', 'P'
        };
        InputStream inputStream = new ByteArrayInputStream(webpHeader);

        String result = MediaTypeUtil.detect(inputStream);

        assertThat(result).isEqualTo("image/webp");
    }

    @Test
    @DisplayName("detect 从 InputStream 检测 TIFF 图片类型")
    void detectTiffFromInputStream() throws IOException {
        // TIFF magic bytes (little-endian)
        byte[] tiffHeader = new byte[] {0x49, 0x49, 0x2A, 0x00};
        InputStream inputStream = new ByteArrayInputStream(tiffHeader);

        String result = MediaTypeUtil.detect(inputStream);

        assertThat(result).isEqualTo("image/tiff");
    }

    @Test
    @DisplayName("detect 从 MultipartFile 检测 JPEG 图片类型")
    void detectJpegFromMultipartFile() throws IOException {
        byte[] jpegHeader = new byte[] {(byte) 0xFF, (byte) 0xD8, (byte) 0xFF, (byte) 0xE0};
        MockMultipartFile file = new MockMultipartFile("file", "test.jpg", "image/jpeg", jpegHeader);

        String result = MediaTypeUtil.detect(file);

        assertThat(result).isEqualTo("image/jpeg");
    }

    @Test
    @DisplayName("detect 从未知格式的 InputStream 返回 application/octet-stream")
    void detectUnknownFromInputStream() throws IOException {
        byte[] unknownHeader = new byte[] {0x00, 0x01, 0x02, 0x03};
        InputStream inputStream = new ByteArrayInputStream(unknownHeader);

        String result = MediaTypeUtil.detect(inputStream);

        assertThat(result).isEqualTo("application/octet-stream");
    }
}
