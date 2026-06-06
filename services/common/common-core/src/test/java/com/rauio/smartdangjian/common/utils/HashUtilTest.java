package com.rauio.smartdangjian.common.utils;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.NoSuchAlgorithmException;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class HashUtilTest {

    @Test
    @DisplayName("calculateSHA256 对已知内容产生确定的哈希值")
    void calculateSha256ReturnsDeterministicHash() throws IOException, NoSuchAlgorithmException {
        byte[] content = "Hello, World!".getBytes();
        InputStream inputStream = new ByteArrayInputStream(content);

        String hash1 = HashUtil.calculateSHA256(inputStream);
        InputStream inputStream2 = new ByteArrayInputStream(content);
        String hash2 = HashUtil.calculateSHA256(inputStream2);

        assertThat(hash1).isEqualTo(hash2);
        assertThat(hash1).hasSize(64); // SHA-256 produces 64 hex characters
    }

    @Test
    @DisplayName("calculateSHA256 不同内容产生不同的哈希值")
    void calculateSha256DifferentContentDifferentHash() throws IOException, NoSuchAlgorithmException {
        String hash1 = HashUtil.calculateSHA256(new ByteArrayInputStream("abc".getBytes()));
        String hash2 = HashUtil.calculateSHA256(new ByteArrayInputStream("xyz".getBytes()));

        assertThat(hash1).isNotEqualTo(hash2);
    }

    @Test
    @DisplayName("calculateSHA256 空输入流仍然产生有效的哈希值")
    void calculateSha256EmptyInputStreamProducesValidHash() throws IOException, NoSuchAlgorithmException {
        String hash = HashUtil.calculateSHA256(new ByteArrayInputStream(new byte[0]));

        assertThat(hash).isNotNull();
        assertThat(hash).hasSize(64);
    }

    @Test
    @DisplayName("calculateSHA256 大内容仍然正确计算")
    void calculateSha256LargeContent() throws IOException, NoSuchAlgorithmException {
        byte[] largeContent = new byte[100_000];
        for (int i = 0; i < largeContent.length; i++) {
            largeContent[i] = (byte) (i % 256);
        }

        String hash = HashUtil.calculateSHA256(new ByteArrayInputStream(largeContent));

        assertThat(hash).isNotNull();
        assertThat(hash).hasSize(64);
    }

    @Test
    @DisplayName("calculateSHA256 哈希值只包含十六进制字符")
    void calculateSha256HashContainsOnlyHexChars() throws IOException, NoSuchAlgorithmException {
        String hash = HashUtil.calculateSHA256(new ByteArrayInputStream("test".getBytes()));

        assertThat(hash).matches("^[0-9a-f]{64}$");
    }
}
