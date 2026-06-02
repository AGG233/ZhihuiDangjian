package com.rauio.smartdangjian.server.learning.controller.user;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.rauio.smartdangjian.pojo.response.Result;
import com.rauio.smartdangjian.security.CurrentUserProvider;
import com.rauio.smartdangjian.security.RoleConstants;
import com.rauio.smartdangjian.server.learning.service.UserLearningRecordService;

import cn.dev33.satoken.annotation.SaCheckRole;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;

@Tag(name = "学习图谱同步接口", description = "同步用户学习记录到知识图谱")
@RestController
@RequestMapping("/api/learning/graph")
@RequiredArgsConstructor
public class UserLearningGraphSyncController {

    private final UserLearningRecordService userLearningRecordService;
    private final CurrentUserProvider currentUserProvider;

    @Operation(summary = "同步用户学习图谱", description = "将用户已学习内容同步到Neo4j图谱")
    @PostMapping("/me/sync")
    @SaCheckRole(RoleConstants.STUDENT)
    public Result<Integer> syncMyGraph() {
        Long userId = Long.valueOf(currentUserProvider.getCurrentUserId());
        return Result.ok(userLearningRecordService.syncUserLearningGraph(userId));
    }
}
