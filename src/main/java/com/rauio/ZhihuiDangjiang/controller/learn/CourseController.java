package com.rauio.ZhihuiDangjiang.controller.learn;


import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.rauio.ZhihuiDangjiang.pojo.Course;
import com.rauio.ZhihuiDangjiang.pojo.dto.CourseDto;
import com.rauio.ZhihuiDangjiang.pojo.request.PageRequest;
import com.rauio.ZhihuiDangjiang.pojo.response.ApiResponse;
import com.rauio.ZhihuiDangjiang.pojo.vo.CourseVO;
import com.rauio.ZhihuiDangjiang.pojo.vo.PageVO;
import com.rauio.ZhihuiDangjiang.service.CourseService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/learning/course")
@RequiredArgsConstructor
public class CourseController {

    private final CourseService     courseService;
    private final ObjectMapper objectMapper;

    //todo api文档
    @GetMapping("/{id}")
    public ResponseEntity<String> get(@PathVariable String id) throws JsonProcessingException {
        CourseVO result = courseService.get(id);
        String json = objectMapper.writeValueAsString(ApiResponse.builder()
                .data(result)
                .build());
        return ResponseEntity.ok(json);
    }

    @PutMapping("/{id}")
    public ResponseEntity<String> update(@RequestBody CourseDto course, @PathVariable String id) throws JsonProcessingException {
        Boolean result = courseService.update(course,id);
        String json = objectMapper.writeValueAsString(ApiResponse.builder()
                .data(result)
                .build());
        return ResponseEntity.ok(json);
    }

    @PostMapping("/")
    public ResponseEntity<String> insert(@RequestBody CourseDto course) throws JsonProcessingException {
        Boolean result = courseService.create(course);
        String json = objectMapper.writeValueAsString(ApiResponse.builder()
                .data(result)
                .build());
        return ResponseEntity.ok(json);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<String> delete(@PathVariable String id) throws JsonProcessingException {
        Boolean result = courseService.delete(id);
        String json = objectMapper.writeValueAsString(ApiResponse.builder()
                .data(result)
                .build());
        return ResponseEntity.ok(json);
    }

    @GetMapping("/all")
    public ResponseEntity<String> getAll() throws JsonProcessingException {
        List<Course> result = courseService.getAll();
        String json = objectMapper.writeValueAsString(ApiResponse.builder()
                .data(result)
                .build());
        return ResponseEntity.ok(json);
    }
    @GetMapping("/page/{pageNum}/{pageSize}")
    public ResponseEntity<String> getPage(@RequestBody PageRequest pageRequest) throws JsonProcessingException {
        PageVO<Object> result = courseService.getPage(pageRequest.getPageNum(),pageRequest.getPageSize());
        String json = objectMapper.writeValueAsString(ApiResponse.builder()
                .data(result)
                .build());
        return ResponseEntity.ok(json);
    }
}