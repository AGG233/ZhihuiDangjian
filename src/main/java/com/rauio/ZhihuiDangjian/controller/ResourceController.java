package com.rauio.ZhihuiDangjian.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.rauio.ZhihuiDangjian.pojo.response.ApiResponse;
import com.rauio.ZhihuiDangjian.service.ResourceService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.net.URL;
import java.security.NoSuchAlgorithmException;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/res")
@RequiredArgsConstructor
public class ResourceController {

    private final ResourceService resourceService;
    private final ObjectMapper objectMapper;

    //todo api文档
//    @PostMapping(value =  "/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
//    public ResponseEntity<String> upload(@RequestPart("file") MultipartFile file) throws IOException, NoSuchAlgorithmException, JsonProcessingException {
//        CompletableFuture<Map<String, String>> result = resourceService.saveFile(file);
//        String json = objectMapper.writeValueAsString(ApiResponse.builder()
//                .code("200")
//                .message("上传成功")
//                .data(result)
//                .build());
//        return ResponseEntity.ok(json);
//    }

    @PostMapping(value = "/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<String> uploadBatch(@RequestParam("files") List<MultipartFile> files) throws IOException, NoSuchAlgorithmException, JsonProcessingException {
        Map<String, String> result = resourceService.saveFileBatch(files);
        String json = objectMapper.writeValueAsString(ApiResponse.builder()
                .code("200")
                .message("上传成功")
                .data(result)
                .build());
        return ResponseEntity.ok(json);
    }

    @GetMapping("/{hash}")
    public ResponseEntity<String> get(@PathVariable String hash) throws JsonProcessingException {
        URL url = resourceService.get(hash);
        String json = objectMapper.writeValueAsString(ApiResponse.builder()
                .code("200")
                .data(url)
                .build());
        return ResponseEntity.ok(json);
    }

    @GetMapping("/batch")
    public ResponseEntity<String> getBatch(@RequestBody List<String> objectKeys) throws JsonProcessingException {
        List<String> urls = resourceService.getBatch(objectKeys);
        String json = objectMapper.writeValueAsString(ApiResponse.builder()
                .code("200")
                .data(urls)
                .build());
        return ResponseEntity.ok(json);
    }

    @DeleteMapping("/{key}")
    public ResponseEntity<String> delete(@PathVariable String key) throws JsonProcessingException {
        boolean result = resourceService.delete(key);
        String json = objectMapper.writeValueAsString(ApiResponse.builder()
                .data(result)
                .build());
        return ResponseEntity.ok(json);
    }
    
    @DeleteMapping("/")
    public ResponseEntity<String> delete(String[] keys) throws JsonProcessingException {
        boolean result = resourceService.delete(keys);
        String json = objectMapper.writeValueAsString(ApiResponse.builder()
                .data(result)
                .build());
        return ResponseEntity.ok(json);
    }
}