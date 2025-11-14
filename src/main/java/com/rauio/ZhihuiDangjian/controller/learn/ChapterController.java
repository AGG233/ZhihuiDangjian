package com.rauio.ZhihuiDangjian.controller.learn;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.rauio.ZhihuiDangjian.pojo.Course;
import com.rauio.ZhihuiDangjian.pojo.dto.ChapterDto;
import com.rauio.ZhihuiDangjian.pojo.response.ApiResponse;
import com.rauio.ZhihuiDangjian.pojo.vo.ChapterVO;
import com.rauio.ZhihuiDangjian.service.ChapterService;
import com.rauio.ZhihuiDangjian.service.CourseService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Service
@RequestMapping("/learning/chapter")
@RequiredArgsConstructor
public class ChapterController {
    private final ChapterService chapterService;
    private final CourseService courseService;
    private final ObjectMapper objectMapper;

    @GetMapping("/{id}")
    public ResponseEntity<String> get(@PathVariable String id) throws JsonProcessingException {
        ChapterVO result = chapterService.get(id);
        String json = objectMapper.writeValueAsString(ApiResponse.builder()
                .data(result)
                .build());
        return ResponseEntity.ok(json);
    }

    @GetMapping("/{id}")
    public ResponseEntity<String> getAllCoursesOfCategory(@PathVariable String id) throws JsonProcessingException {
        List<Course> courses = courseService.getAllCoursesOfCategory(id);
        String json = objectMapper.writeValueAsString(ApiResponse.builder()
                .data(courses)
                .build());
        return ResponseEntity.ok(json);
    }
    @GetMapping("/{courseId}")
    public ResponseEntity<String> getAllChaptersOfCourse(@PathVariable String courseId) throws JsonProcessingException {
        List<ChapterVO> result = chapterService.getAllChaptersOfCourse(courseId);
        String json = objectMapper.writeValueAsString(ApiResponse.builder()
                .data(result)
                .build());
        return ResponseEntity.ok(json);
    }

    @PostMapping("/")
    public ResponseEntity<String> create(@RequestBody ChapterDto chapter) throws JsonProcessingException {
        Boolean result = chapterService.create(chapter);
        String json = objectMapper.writeValueAsString(ApiResponse.builder()
                .data(result)
                .build());
        return ResponseEntity.ok(json);
    }
}
