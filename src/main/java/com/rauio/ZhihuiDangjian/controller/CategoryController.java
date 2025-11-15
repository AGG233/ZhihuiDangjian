package com.rauio.ZhihuiDangjian.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.rauio.ZhihuiDangjian.pojo.dto.CategoryDto;
import com.rauio.ZhihuiDangjian.pojo.response.ApiResponse;
import com.rauio.ZhihuiDangjian.pojo.vo.CategoryVO;
import com.rauio.ZhihuiDangjian.service.CategoryService;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/category")
@RequiredArgsConstructor
public class CategoryController {
    private final CategoryService categoryService;
    private final ObjectMapper objectMapper;


    @Operation(
            summary = "获取目录",
            description = "根据目录id获取目录"
    )
    @GetMapping("/{id}")
    public ResponseEntity<String> get(@PathVariable String id) throws JsonProcessingException {
        CategoryVO result = categoryService.getById(id);
        String json = objectMapper.writeValueAsString(ApiResponse.builder()
                .data(result)
                .build());
        return ResponseEntity.ok(json);
    }

    @Operation(
            summary = "获取所有目录",
            description = "获取所有目录"
    )
    @GetMapping("/all")
    public ResponseEntity<String> getAll() throws JsonProcessingException {
        List<CategoryVO> result = categoryService.getRootNodes();
        String json = objectMapper.writeValueAsString(ApiResponse.builder()
                .data(result)
                .build());
        return ResponseEntity.ok(json);
    }

    @Operation(
            summary = "获取子目录",
            description = "根据目录id获取这个目录的子目录"
    )
    @GetMapping("/{id}/children")
    public ResponseEntity<String> getChildren(@PathVariable String id) throws JsonProcessingException {
        List<CategoryVO> result = categoryService.getChildren(id);
        String json = objectMapper.writeValueAsString(ApiResponse.builder()
                .data(result)
                .build());
        return ResponseEntity.ok(json);
    }

    @Operation(
            summary = "获取所有根目录",
            description = "获取所有根目录"
    )
    @GetMapping("/rootNodes")
    public ResponseEntity<String> getRootNodes() throws JsonProcessingException {
        List<CategoryVO> result = categoryService.getRootNodes();
        String json = objectMapper.writeValueAsString(ApiResponse.builder()
                .data(result)
                .build());
        return ResponseEntity.ok(json);
    }
    @Operation(
            summary = "添加根目录",
            description = "添加根目录"
    )
    @PostMapping("/rootNode")
    public ResponseEntity<String> add(CategoryDto dto) throws JsonProcessingException {
        Boolean result = categoryService.add(dto);
        String json = objectMapper.writeValueAsString(ApiResponse.builder()
                .data(result)
                .build());
        return ResponseEntity.ok(json);
    }
    @Operation(
            summary = "添加子目录",
            description = "向根目录id为路径参数的id添加子目录"
    )
    @PostMapping("/{id}/addChildren")
    public ResponseEntity<String> addChildren(List<CategoryDto> children, @PathVariable String id) throws JsonProcessingException {
        Boolean result = categoryService.addChildren(children,id);
        String json = objectMapper.writeValueAsString(ApiResponse.builder()
                .data(result)
                .build());
        return ResponseEntity.ok(json);
    }
    @Operation(
            summary = "修改目录",
            description = "修改目录"
    )
    @PostMapping("/{id}")
    public ResponseEntity<String> update(CategoryDto dto, @PathVariable String id) throws JsonProcessingException {
        Boolean result = categoryService.update(dto,id);
        String json = objectMapper.writeValueAsString(ApiResponse.builder()
                .data(result)
                .build());
        return ResponseEntity.ok(json);
    }
    @Operation(
            summary = "删除目录",
            description = "如果删除的目录没有子目录，则删除成功，有子目录则无法删除"
    )
    @DeleteMapping("/{id}")
    public ResponseEntity<String> delete(@PathVariable String id) throws JsonProcessingException {
        Boolean result = categoryService.delete(id);
        String json = objectMapper.writeValueAsString(ApiResponse.builder()
                .data(result)
                .build());
        return ResponseEntity.ok(json);
    }
    @Operation(
            summary = "删除目录和它的子目录",
            description = "删除目录的同时也将删除它的子目录"
    )
    @DeleteMapping("/{id}/all")
    public ResponseEntity<String> deleteAll(@PathVariable String id) throws JsonProcessingException {
        Boolean result = categoryService.deleteAll(id);
        String json = objectMapper.writeValueAsString(ApiResponse.builder()
                .data(result)
                .build());
        return ResponseEntity.ok(json);
    }
}