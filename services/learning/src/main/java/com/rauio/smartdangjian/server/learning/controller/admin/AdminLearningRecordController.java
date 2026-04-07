package com.rauio.smartdangjian.server.learning.controller.admin;

import com.rauio.smartdangjian.aop.annotation.DataScopeAccess;
import com.rauio.smartdangjian.aop.annotation.PermissionAccess;
import com.rauio.smartdangjian.aop.support.DataScopeAction;
import com.rauio.smartdangjian.aop.support.DataScopeResources;
import com.rauio.smartdangjian.pojo.response.Result;
import com.rauio.smartdangjian.server.learning.pojo.vo.UserLearningRecordVO;
import com.rauio.smartdangjian.server.learning.service.UserLearningRecordService;
import com.rauio.smartdangjian.utils.spec.UserType;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "管理员学习记录接口")
@RestController
@RequestMapping("/api/admin/learning/records")
@RequiredArgsConstructor
@PermissionAccess(UserType.SCHOOL)
public class AdminLearningRecordController {

    private final UserLearningRecordService recordService;

    @Operation(summary = "获取章节所有学习记录")
    @GetMapping("/chapter/{chapterId}")
    @DataScopeAccess(resource = DataScopeResources.LEARNING_RECORD, action = DataScopeAction.FILTER, id = "#chapterId")
    public Result<List<UserLearningRecordVO>> getByChapterId(@Parameter(name = "chapterId", description = "章节ID") @PathVariable String chapterId) {
        return Result.ok(recordService.getByChapterId(chapterId));
    }

    @Operation(summary = "删除学习记录")
    @DeleteMapping("/{id}")
    @DataScopeAccess(resource = DataScopeResources.LEARNING_RECORD, action = DataScopeAction.DELETE, id = "#id")
    public Result<Boolean> delete(@Parameter(name = "id", description = "记录ID") @PathVariable String id) {
        return Result.ok(recordService.delete(id));
    }
}
