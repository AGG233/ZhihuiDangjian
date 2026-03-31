package com.rauio.smartdangjian.common.controller.publicapi;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.rauio.smartdangjian.common.pojo.Universities;
import com.rauio.smartdangjian.common.service.UniversitiesService;
import com.rauio.smartdangjian.pojo.response.Result;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Tag(name = "公共API接口", description = "提供公共数据访问接口")
@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class ApiController {

    private final UniversitiesService universitiesService;

    @Operation(summary = "获取所有学校列表", description = "返回系统中所有学校的列表信息")
    @GetMapping("/school/all")
    public Result<List<Universities>> school() throws JsonProcessingException {
        List<Universities> universities = universitiesService.getList();
        return Result.ok(universities);
    }
}
