package com.rauio.ZhihuiDangjian.controller.learn;


import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.rauio.ZhihuiDangjian.pojo.Course;
import com.rauio.ZhihuiDangjian.pojo.dto.CourseDto;
import com.rauio.ZhihuiDangjian.pojo.request.PageRequest;
import com.rauio.ZhihuiDangjian.pojo.response.ApiResponse;
import com.rauio.ZhihuiDangjian.pojo.vo.CourseVO;
import com.rauio.ZhihuiDangjian.pojo.vo.PageVO;
import com.rauio.ZhihuiDangjian.service.CourseService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "课程管理接口", description = "提供课程的增删改查功能")
@RestController
@RequestMapping("/learning/course")
@RequiredArgsConstructor
public class CourseController {

    private final CourseService     courseService;
    private final ObjectMapper objectMapper;

    //todo api文档
    @Operation(summary = "获取课程详情", description = "根据课程ID获取课程详细信息")
    @GetMapping("/{id}")
    public ResponseEntity<String> get(@PathVariable String id) throws JsonProcessingException {
        CourseVO result = courseService.get(id);
        String json = objectMapper.writeValueAsString(ApiResponse.builder()
                .data(result)
                .build());
        return ResponseEntity.ok(json);
    }

    @Operation(summary = "更新课程信息", description = "根据课程ID更新课程信息")
    @PutMapping("/{id}")
    public ResponseEntity<String> update(@RequestBody CourseDto course, @PathVariable String id) throws JsonProcessingException {
        Boolean result = courseService.update(course,id);
        String json = objectMapper.writeValueAsString(ApiResponse.builder()
                .data(result)
                .build());
        return ResponseEntity.ok(json);
    }

    @Operation(summary = "创建课程", description = "创建一个新的课程")
    @PostMapping("/")
    public ResponseEntity<String> insert(@RequestBody CourseDto course) throws JsonProcessingException {
        Boolean result = courseService.create(course);
        String json = objectMapper.writeValueAsString(ApiResponse.builder()
                .data(result)
                .build());
        return ResponseEntity.ok(json);
    }

    @Operation(summary = "删除课程", description = "根据课程ID删除指定课程")
    @DeleteMapping("/{id}")
    public ResponseEntity<String> delete(@PathVariable String id) throws JsonProcessingException {
        Boolean result = courseService.delete(id);
        String json = objectMapper.writeValueAsString(ApiResponse.builder()
                .data(result)
                .build());
        return ResponseEntity.ok(json);
    }

    @Operation(summary = "获取所有课程", description = "获取系统中所有的课程列表")
    @GetMapping("/all")
    public ResponseEntity<String> getAll() throws JsonProcessingException {
        List<Course> result = courseService.getAll();
        String json = objectMapper.writeValueAsString(ApiResponse.builder()
                .data(result)
                .build());
        return ResponseEntity.ok(json);
    }
    
    @Operation(summary = "分页获取课程", description = "根据分页参数获取课程列表")
    @GetMapping("/page/{pageNum}/{pageSize}")
    public ResponseEntity<String> getPage(@RequestBody PageRequest pageRequest) throws JsonProcessingException {
        PageVO<Object> result = courseService.getPage(pageRequest.getPageNum(),pageRequest.getPageSize());
        String json = objectMapper.writeValueAsString(ApiResponse.builder()
                .data(result)
                .build());
        return ResponseEntity.ok(json);
    }
}