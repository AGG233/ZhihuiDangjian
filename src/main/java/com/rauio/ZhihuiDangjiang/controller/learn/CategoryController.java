package com.rauio.ZhihuiDangjiang.controller.learn;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.rauio.ZhihuiDangjiang.pojo.dto.CourseCategoryDto;
import com.rauio.ZhihuiDangjiang.pojo.response.ApiResponse;
import com.rauio.ZhihuiDangjiang.pojo.vo.CourseCategoryVO;
import com.rauio.ZhihuiDangjiang.service.CourseCategoryService;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/learning/category")
@RequiredArgsConstructor
public class CategoryController {
    private final CourseCategoryService courseCategoryService;
    private final ObjectMapper objectMapper;


    @Operation(
            summary = "获取目录",
            description = "根据目录id获取目录"
    )
    @GetMapping("/{id}")
    public ResponseEntity<String> get(@PathVariable String id) throws JsonProcessingException {
        CourseCategoryVO result = courseCategoryService.getById(id);
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
        List<CourseCategoryVO> result = courseCategoryService.getRootNodes();
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
        List<CourseCategoryVO> result = courseCategoryService.getChildren(id);
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
        List<CourseCategoryVO> result = courseCategoryService.getRootNodes();
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
    public ResponseEntity<String> add(CourseCategoryDto dto) throws JsonProcessingException {
        Boolean result = courseCategoryService.add(dto);
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
    public ResponseEntity<String> addChildren(List<CourseCategoryDto> children,@PathVariable String id) throws JsonProcessingException {
        Boolean result = courseCategoryService.addChildren(children,id);
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
    public ResponseEntity<String> update(CourseCategoryDto dto,@PathVariable String id) throws JsonProcessingException {
        Boolean result = courseCategoryService.update(dto,id);
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
        Boolean result = courseCategoryService.delete(id);
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
        Boolean result = courseCategoryService.deleteAll(id);
        String json = objectMapper.writeValueAsString(ApiResponse.builder()
                .data(result)
                .build());
        return ResponseEntity.ok(json);
    }
}