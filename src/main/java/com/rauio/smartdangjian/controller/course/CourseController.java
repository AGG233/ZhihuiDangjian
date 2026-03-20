package com.rauio.smartdangjian.controller.course;


import com.fasterxml.jackson.core.JsonProcessingException;
import com.rauio.smartdangjian.aop.annotation.PermissionAccess;
import com.rauio.smartdangjian.pojo.dto.CourseDto;
import com.rauio.smartdangjian.pojo.request.PageRequest;
import com.rauio.smartdangjian.pojo.response.Result;
import com.rauio.smartdangjian.pojo.vo.CourseVO;
import com.rauio.smartdangjian.pojo.vo.PageVO;
import com.rauio.smartdangjian.service.content.CourseService;
import com.rauio.smartdangjian.utils.spec.UserType;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@Tag(name = "课程管理接口", description = "提供课程的增删改查功能")
@RestController
@RequestMapping("/course")
@RequiredArgsConstructor
@PermissionAccess(UserType.SCHOOL)
public class CourseController {

    private final CourseService     courseService;

    //todo api文档
    @Operation(summary = "获取课程详情", description = "根据课程ID获取课程详细信息")
    @GetMapping("/{id}")
    @PermissionAccess(UserType.STUDENT)
    public Result<CourseVO> get(@PathVariable String id) throws JsonProcessingException {
        CourseVO result = courseService.get(id);
        return Result.ok(result);
    }

    @Operation(summary = "更新课程信息", description = "根据课程ID更新课程信息")
    @PutMapping("/{id}")
    public Result<Boolean> update(@RequestBody CourseDto course, @PathVariable String id) throws JsonProcessingException {
        Boolean result = courseService.update(course,id);
        return Result.ok(result);
    }

    @Operation(summary = "创建课程", description = "创建一个新的课程")
    @PostMapping("/")
    public Result<Boolean> insert(@RequestBody CourseDto course) throws JsonProcessingException {
        Boolean result = courseService.create(course);
        return Result.ok(result);
    }

    @Operation(summary = "删除课程", description = "根据课程ID删除指定课程")
    @DeleteMapping("/{id}")
    public Result<Boolean> delete(@PathVariable String id) throws JsonProcessingException {
        Boolean result = courseService.delete(id);
        return Result.ok(result);
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
    @GetMapping("/page")
    @PermissionAccess(UserType.STUDENT)
    public Result<PageVO<Object>> getPage(
            @ModelAttribute PageRequest pageRequest,
            @Parameter(description = "页码") @RequestParam(defaultValue = "1") int pageNum,
            @Parameter(description = "页的大小") @RequestParam(defaultValue = "10") int pageSize) {
        PageVO<Object> result = courseService.getPage(pageNum, pageSize);
        return Result.ok(result);
    }
}
