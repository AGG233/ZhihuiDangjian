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
    @DisplayName("detect 从 MultipartFile 检测文件类型")
    void detectFromMultipartFile() throws IOException {
        byte[] pngHeader = new byte[] {(byte) 0x89, 'P', 'N', 'G', 0x0D, 0x0A, 0x1A, 0x0A};
        MockMultipartFile file = new MockMultipartFile("file", "test.png", "image/png", pngHeader);

        String result = MediaTypeUtil.detect(file);

        assertThat(result).isEqualTo("image/png");
    }
}
