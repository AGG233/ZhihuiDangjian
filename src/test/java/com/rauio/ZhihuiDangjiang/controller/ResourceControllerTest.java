package com.rauio.ZhihuiDangjiang.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.rauio.ZhihuiDangjiang.pojo.response.ApiResponse;
import com.rauio.ZhihuiDangjiang.service.ResourceService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

public class ResourceControllerTest {

    @Mock
    private ResourceService resourceService;

    @Mock
    private ObjectMapper objectMapper;

    @InjectMocks
    private ResourceController resourceController;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }


    @Test
    void get_Success() throws MalformedURLException, JsonProcessingException {
        // Given
        String hash = "abc123";
        URL expectedUrl = new URL("http://example.com/resource/" + hash);
        when(resourceService.get(hash)).thenReturn(expectedUrl);

        ApiResponse apiResponse = ApiResponse.builder()
                .code("200")
                .data(expectedUrl)
                .build();
        when(objectMapper.writeValueAsString(any())).thenReturn("{\"code\":\"200\"}");

        // When
        ResponseEntity<String> result = resourceController.get(hash);

        // Then
        assertNotNull(result);
        assertEquals(HttpStatus.OK, result.getStatusCode());
        verify(resourceService, times(1)).get(hash);
        verify(objectMapper, times(1)).writeValueAsString(any(ApiResponse.class));
    }

    @Test
    void getBatch_Success() throws MalformedURLException, JsonProcessingException {
        // Given
        List<String> hashes = List.of("hash1", "hash2");
        List<String> expectedUrls = List.of(
                "http://example.com/resource/hash1",
                "http://example.com/resource/hash2"
        );
        when(resourceService.getBatch(hashes)).thenReturn(expectedUrls);

        ApiResponse apiResponse = ApiResponse.builder()
                .code("200")
                .data(expectedUrls)
                .build();
        when(objectMapper.writeValueAsString(any())).thenReturn("{\"code\":\"200\"}");

        // When
        ResponseEntity<String> result = resourceController.getBatch(hashes);

        // Then
        assertNotNull(result);
        assertEquals(HttpStatus.OK, result.getStatusCode());
        verify(resourceService, times(1)).getBatch(hashes);
        verify(objectMapper, times(1)).writeValueAsString(any(ApiResponse.class));
    }

    @Test
    void deleteBySingleKey_Success() throws JsonProcessingException {
        // Given
        String key = "test-key";
        when(resourceService.delete(key)).thenReturn(true);

        ApiResponse apiResponse = ApiResponse.builder()
                .data(true)
                .build();
        when(objectMapper.writeValueAsString(any())).thenReturn("{\"data\":true}");

        // When
        ResponseEntity<String> result = resourceController.delete(key);

        // Then
        assertNotNull(result);
        assertEquals(HttpStatus.OK, result.getStatusCode());
        verify(resourceService, times(1)).delete(key);
        verify(objectMapper, times(1)).writeValueAsString(any(ApiResponse.class));
    }

    @Test
    void deleteByMultipleKeys_Success() throws JsonProcessingException {
        // Given
        String[] keys = {"key1", "key2"};
        when(resourceService.delete(keys)).thenReturn(true);

        ApiResponse apiResponse = ApiResponse.builder()
                .data(true)
                .build();
        when(objectMapper.writeValueAsString(any())).thenReturn("{\"data\":true}");

        // When
        ResponseEntity<String> result = resourceController.delete(keys);

        // Then
        assertNotNull(result);
        assertEquals(HttpStatus.OK, result.getStatusCode());
        verify(resourceService, times(1)).delete(keys);
        verify(objectMapper, times(1)).writeValueAsString(any(ApiResponse.class));
    }
}