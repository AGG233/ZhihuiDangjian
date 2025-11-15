package com.rauio.ZhihuiDangjian.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.rauio.ZhihuiDangjian.pojo.response.ApiResponse;
import com.rauio.ZhihuiDangjian.service.ResourceService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
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

@Tag(name = "资源管理接口", description = "提供文件上传、下载和删除等资源管理功能")
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

    @Operation(summary = "批量上传文件", description = "支持同时上传多个文件")
    @PostMapping(value = "/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<String> uploadBatch(@RequestParam("files") List<MultipartFile> files) throws IOException, NoSuchAlgorithmException, JsonProcessingException {
        Map<String, String> result = resourceService.saveFileBatch(files);
        return ApiResponse.buildResponse("200", "上传成功", result);
    }

    @Operation(summary = "获取文件访问链接", description = "根据文件哈希值获取文件的访问URL")
    @GetMapping("/{hash}")
    public ResponseEntity<String> get(@PathVariable String hash) throws JsonProcessingException {
        URL url = resourceService.get(hash);
        return ApiResponse.buildResponse("200", null, url);
    }

    @Operation(summary = "批量获取文件访问链接", description = "根据多个文件哈希值批量获取文件访问URL")
    @GetMapping("/batch")
    public ResponseEntity<String> getBatch(@RequestBody List<String> objectKeys) throws JsonProcessingException {
        List<String> urls = resourceService.getBatch(objectKeys);
        return ApiResponse.buildResponse("200", null, urls);
    }

    @Operation(summary = "删除单个文件", description = "根据文件key删除指定文件")
    @DeleteMapping("/{key}")
    public ResponseEntity<String> delete(@PathVariable String key) throws JsonProcessingException {
        boolean result = resourceService.delete(key);
        return ApiResponse.buildResponse(result);
    }
    
    @Operation(summary = "批量删除文件", description = "根据多个文件key批量删除文件")
    @DeleteMapping("/")
    public ResponseEntity<String> delete(@RequestParam String[] keys) throws JsonProcessingException {
        boolean result = resourceService.delete(keys);
        return ApiResponse.buildResponse(result);
    }
}