package com.rauio.smartdangjian.server.learning.controller.user;

import com.rauio.smartdangjian.aop.annotation.PermissionAccess;
import com.rauio.smartdangjian.server.learning.service.UserLearningRecordService;
import com.rauio.smartdangjian.pojo.response.Result;
import com.rauio.smartdangjian.utils.spec.UserType;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "学习图谱同步接口", description = "同步用户学习记录到知识图谱")
@RestController
@RequestMapping("/api/learning/graph")
@RequiredArgsConstructor
public class UserLearningGraphSyncController {

    private final UserLearningRecordService userLearningRecordService;

    @Operation(summary = "同步用户学习图谱", description = "将用户已学习内容同步到Neo4j图谱")
    @PostMapping("/user/{userId}/sync")
    @PermissionAccess(UserType.STUDENT)
    public Result<Integer> syncUserGraph(@Parameter(description = "用户ID") @PathVariable String userId) {
        return Result.ok(userLearningRecordService.syncUserLearningGraph(userId));
    }
}
