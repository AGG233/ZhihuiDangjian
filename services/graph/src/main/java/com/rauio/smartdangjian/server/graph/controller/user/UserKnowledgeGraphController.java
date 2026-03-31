package com.rauio.smartdangjian.server.graph.controller.user;

import com.rauio.smartdangjian.aop.annotation.PermissionAccess;
import com.rauio.smartdangjian.pojo.response.Result;
import com.rauio.smartdangjian.server.graph.pojo.vo.KnowledgeGraphVO;
import com.rauio.smartdangjian.server.graph.service.KnowledgeGraphService;
import com.rauio.smartdangjian.utils.spec.UserType;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "知识图谱接口", description = "基于Neo4j的用户学习图谱接口")
@RestController
@RequestMapping("/api/graph/knowledge-graphs")
@RequiredArgsConstructor
public class UserKnowledgeGraphController {

    private final KnowledgeGraphService knowledgeGraphService;

    @Operation(summary = "获取用户学习图谱", description = "返回用户学习课程和章节的图谱结构")
    @GetMapping("/users/{userId}")
    @PermissionAccess(UserType.STUDENT)
    public Result<KnowledgeGraphVO> getUserGraph(@Parameter(description = "用户ID") @PathVariable String userId) {
        return Result.ok(knowledgeGraphService.getUserGraph(userId));
    }

    @Operation(summary = "获取课程图谱", description = "返回课程关联的学习用户和章节结构")
    @GetMapping("/courses/{courseId}")
    @PermissionAccess(UserType.STUDENT)
    public Result<KnowledgeGraphVO> getCourseGraph(@Parameter(description = "课程ID") @PathVariable String courseId) {
        return Result.ok(knowledgeGraphService.getCourseGraph(courseId));
    }
}
