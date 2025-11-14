package com.rauio.ZhihuiDangjian.controller.learn;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.rauio.ZhihuiDangjian.pojo.Course;
import com.rauio.ZhihuiDangjian.pojo.dto.ChapterDto;
import com.rauio.ZhihuiDangjian.pojo.response.ApiResponse;
import com.rauio.ZhihuiDangjian.pojo.vo.ChapterVO;
import com.rauio.ZhihuiDangjian.service.ChapterService;
import com.rauio.ZhihuiDangjian.service.CourseService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "章节管理接口", description = "提供课程章节的增删改查功能")
@RestController
@RequestMapping("/learning/chapter")
@RequiredArgsConstructor
public class ChapterController {
    private final ChapterService chapterService;
    private final CourseService courseService;
    private final ObjectMapper objectMapper;

    @Operation(summary = "获取章节详情", description = "根据章节ID获取章节详细信息")
    @GetMapping("/{id}")
    public ResponseEntity<String> get(@PathVariable String id) throws JsonProcessingException {
        ChapterVO result = chapterService.get(id);
        String json = objectMapper.writeValueAsString(ApiResponse.builder()
                .data(result)
                .build());
        return ResponseEntity.ok(json);
    }

//    @Operation(summary = "获取分类下的所有课程", description = "根据分类ID获取该分类下的所有课程")
//    @GetMapping("/{id}")
//    public ResponseEntity<String> getAllCoursesOfCategory(@PathVariable String id) throws JsonProcessingException {
//        List<Course> courses = courseService.getAllCoursesOfCategory(id);
//        String json = objectMapper.writeValueAsString(ApiResponse.builder()
//                .data(courses)
//                .build());
//        return ResponseEntity.ok(json);
//    }
    
    @Operation(summary = "获取课程的所有章节", description = "根据课程ID获取该课程下的所有章节")
    @GetMapping("/{courseId}")
    public ResponseEntity<String> getAllChaptersOfCourse(@PathVariable String courseId) throws JsonProcessingException {
        List<ChapterVO> result = chapterService.getAllChaptersOfCourse(courseId);
        String json = objectMapper.writeValueAsString(ApiResponse.builder()
                .data(result)
                .build());
        return ResponseEntity.ok(json);
    }

    @Operation(summary = "创建章节", description = "创建一个新的章节")
    @PostMapping("/")
    public ResponseEntity<String> create(@RequestBody ChapterDto chapter) throws JsonProcessingException {
        Boolean result = chapterService.create(chapter);
        String json = objectMapper.writeValueAsString(ApiResponse.builder()
                .data(result)
                .build());
        return ResponseEntity.ok(json);
    }
}