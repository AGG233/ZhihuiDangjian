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

@Tag(name = "资源管理接口", description = "文件上传下载接口")
@RestController
@RequestMapping("/res")
@RequiredArgsConstructor
public class ResourceController {

    private final ResourceService resourceService;
    private final ObjectMapper objectMapper;


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

    @Operation(summary = "获取文件链接", description = "根据文件哈希值获取文件链接")
    @GetMapping("/{hash}")
    public ResponseEntity<String> getByHash(@PathVariable String hash) throws JsonProcessingException {
        URL url = resourceService.getByHash(hash);
        return ApiResponse.buildResponse("200", null, url);
    }

    @Operation(summary = "获取文件链接", description = "通过资源的ID获取文件链接")
    @GetMapping("/{id}")
    public ResponseEntity<String> getById(@PathVariable String id) throws JsonProcessingException {
        URL url = resourceService.getById(id);
        return ApiResponse.buildResponse("200", null, url);
    }

    @Operation(summary = "批量获取文件访问链接", description = "上传一个列表，列表元素为文件的hash值，根据hash值获取相应的文件访问URL")
    @GetMapping("/batch/hash")
    public ResponseEntity<String> getBatchByHash(@RequestBody List<String> hashList) throws JsonProcessingException {
        List<String> urls = resourceService.getBatchWithHash(hashList);
        return ApiResponse.buildResponse("200", null, urls);
    }

    @Operation(summary = "批量获取文件访问链接", description = "上传一个列表，列表元素为资源的ID，根据资源ID获取相应的文件访问URL")
    @GetMapping("/batch/id")
    public ResponseEntity<String> getBatchById(@RequestBody List<String> idList) throws JsonProcessingException {
        List<String> urls = resourceService.getBatchWithHash(idList);
        return ApiResponse.buildResponse("200", null, urls);
    }

    @Operation(summary = "删除单个文件", description = "根据文件hash值删除")
    @DeleteMapping("/{hash}")
    public ResponseEntity<String> delete(@PathVariable String hash) throws JsonProcessingException {
        boolean result = resourceService.delete(hash);
        return ApiResponse.buildResponse(result);
    }
    
    @Operation(summary = "批量删除文件", description = "根据hash值批量删除文件")
    @DeleteMapping("/")
    public ResponseEntity<String> delete(@RequestParam String[] hash) throws JsonProcessingException {
        boolean result = resourceService.delete(hash);
        return ApiResponse.buildResponse(result);
    }
}