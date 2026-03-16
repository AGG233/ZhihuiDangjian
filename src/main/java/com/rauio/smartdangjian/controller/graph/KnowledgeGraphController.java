package com.rauio.smartdangjian.controller.graph;

import com.rauio.smartdangjian.aop.annotation.PermissionAccess;
import com.rauio.smartdangjian.pojo.response.Result;
import com.rauio.smartdangjian.pojo.vo.KnowledgeGraphVO;
import com.rauio.smartdangjian.service.graph.KnowledgeGraphService;
import com.rauio.smartdangjian.utils.spec.UserType;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "知识图谱接口", description = "基于Neo4j的用户学习图谱接口")
@RestController
@RequestMapping("/api/graph")
@RequiredArgsConstructor
public class KnowledgeGraphController {

    private final KnowledgeGraphService knowledgeGraphService;

    @Operation(summary = "同步用户学习图谱", description = "将用户已学习内容同步到Neo4j图谱")
    @PostMapping("/user/{userId}/sync")
    @PermissionAccess(UserType.STUDENT)
    public Result<Integer> syncUserGraph(@Parameter(description = "用户ID") @PathVariable Long userId) {
        int count = knowledgeGraphService.syncUserLearningGraph(userId);
        return Result.ok(count);
    }

    @Operation(summary = "获取用户学习图谱", description = "返回用户学习课程和章节的图谱结构")
    @GetMapping("/user/{userId}")
    @PermissionAccess(UserType.STUDENT)
    public Result<KnowledgeGraphVO> getUserGraph(@Parameter(description = "用户ID") @PathVariable Long userId) {
        return Result.ok(knowledgeGraphService.getUserGraph(userId));
    }

    @Operation(summary = "获取课程图谱", description = "返回课程关联的学习用户和章节结构")
    @GetMapping("/course/{courseId}")
    @PermissionAccess(UserType.STUDENT)
    public Result<KnowledgeGraphVO> getCourseGraph(@Parameter(description = "课程ID") @PathVariable Long courseId) {
        return Result.ok(knowledgeGraphService.getCourseGraph(courseId));
    }
}
