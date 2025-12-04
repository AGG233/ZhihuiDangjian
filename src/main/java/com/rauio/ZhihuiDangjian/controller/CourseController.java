package com.rauio.ZhihuiDangjian.controller;


import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.rauio.ZhihuiDangjian.aop.annotation.PermissionAccess;
import com.rauio.ZhihuiDangjian.pojo.dto.CourseDto;
import com.rauio.ZhihuiDangjian.pojo.request.PageRequest;
import com.rauio.ZhihuiDangjian.pojo.response.ApiResponse;
import com.rauio.ZhihuiDangjian.pojo.vo.CourseVO;
import com.rauio.ZhihuiDangjian.pojo.vo.PageVO;
import com.rauio.ZhihuiDangjian.service.CourseService;
import com.rauio.ZhihuiDangjian.utils.Spec.UserType;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@Tag(name = "课程管理接口", description = "提供课程的增删改查功能")
@RestController
@RequestMapping("/course")
@RequiredArgsConstructor
@PermissionAccess(UserType.TEACHER)
public class CourseController {

    private final CourseService     courseService;
    private final ObjectMapper objectMapper;

    //todo api文档
    @Operation(summary = "获取课程详情", description = "根据课程ID获取课程详细信息")
    @GetMapping("/{id}")
    @PermissionAccess(UserType.STUDENT)
    public ApiResponse<CourseVO> get(@PathVariable Long id) throws JsonProcessingException {
        CourseVO result = courseService.get(id);
        return ApiResponse.ok(result);
    }

    @Operation(summary = "更新课程信息", description = "根据课程ID更新课程信息")
    @PutMapping("/{id}")
    public ApiResponse<Boolean> update(@RequestBody CourseDto course, @PathVariable Long id) throws JsonProcessingException {
        Boolean result = courseService.update(course,id);
        return ApiResponse.ok(result);
    }

    @Operation(summary = "创建课程", description = "创建一个新的课程")
    @PostMapping("/")
    public ApiResponse<Boolean> insert(@RequestBody CourseDto course) throws JsonProcessingException {
        Boolean result = courseService.create(course);
        return ApiResponse.ok(result);
    }

    @Operation(summary = "删除课程", description = "根据课程ID删除指定课程")
    @DeleteMapping("/{id}")
    public ApiResponse<Boolean> delete(@PathVariable Long id) throws JsonProcessingException {
        Boolean result = courseService.delete(id);
        return ApiResponse.ok(result);
    }

//    @Operation(summary = "获取所有课程", description = "获取系统中所有的课程列表")
//    @GetMapping("/all")
//    public ResponseEntity<String> getAll() throws JsonProcessingException {
//        List<Course> result = courseService.getAll();
//        String json = objectMapper.writeValueAsString(ApiResponse.builder()
//                .data(result)
//                .build());
//        return ResponseEntity.ok(json);
//    }
    @Operation(summary = "分页获取课程", description = "根据分页参数获取课程列表")
    @GetMapping("/page/{pageNum}/{pageSize}")
    @PermissionAccess(UserType.STUDENT)
    public ApiResponse<PageVO<Object>> getPage(@RequestBody PageRequest pageRequest) {
        PageVO<Object> result = courseService.getPage(pageRequest.getPageNum(),pageRequest.getPageSize());
        return ApiResponse.ok(result);
    }
}