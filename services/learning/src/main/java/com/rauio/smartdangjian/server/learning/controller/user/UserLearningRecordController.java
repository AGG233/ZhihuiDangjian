package com.rauio.smartdangjian.server.learning.controller.user;

import com.rauio.smartdangjian.aop.annotation.DataScopeAccess;
import com.rauio.smartdangjian.aop.annotation.PermissionAccess;
import com.rauio.smartdangjian.aop.annotation.ResourceAccess;
import com.rauio.smartdangjian.aop.support.DataScopeAction;
import com.rauio.smartdangjian.aop.support.DataScopeResources;
import com.rauio.smartdangjian.pojo.response.Result;
import com.rauio.smartdangjian.server.learning.pojo.dto.UserLearningRecordDto;
import com.rauio.smartdangjian.server.learning.pojo.vo.UserLearningRecordVO;
import com.rauio.smartdangjian.server.learning.service.UserLearningRecordService;
import com.rauio.smartdangjian.utils.spec.UserType;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "用户学习记录接口", description = "用户学习记录管理接口")
@RestController
@RequestMapping("/api/learning/records")
@RequiredArgsConstructor
public class UserLearningRecordController {

    private final UserLearningRecordService recordService;

    @Operation(summary = "获取学习记录", description = "根据记录ID获取学习记录详情")
    @GetMapping("/{id}")
    @PermissionAccess(UserType.STUDENT)
    @DataScopeAccess(resource = DataScopeResources.LEARNING_RECORD, action = DataScopeAction.READ, id = "#id")
    public Result<UserLearningRecordVO> get(@Parameter(name = "id", description = "记录ID") @PathVariable String id) {
        UserLearningRecordVO result = recordService.get(id);
        return Result.ok(result);
    }

    @Operation(summary = "获取用户所有学习记录", description = "根据用户ID获取该用户的所有学习记录")
    @GetMapping("/user/{userId}")
    @PermissionAccess(UserType.STUDENT)
    @ResourceAccess(id = "#userId")
    public Result<List<UserLearningRecordVO>> getByUserId(@Parameter(name = "userId", description = "用户ID") @PathVariable String userId) {
        List<UserLearningRecordVO> result = recordService.getByUserId(userId);
        return Result.ok(result);
    }

    @Operation(summary = "获取用户章节学习记录", description = "获取指定用户在指定章节的所有学习记录")
    @GetMapping("/user/{userId}/chapter/{chapterId}")
    @PermissionAccess(UserType.STUDENT)
    @ResourceAccess(id = "#userId")
    public Result<List<UserLearningRecordVO>> getByUserIdAndChapterId(
            @Parameter(name = "userId", description = "用户ID") @PathVariable String userId,
            @Parameter(name = "chapterId", description = "章节ID") @PathVariable String chapterId) {
        List<UserLearningRecordVO> result = recordService.getByUserIdAndChapterId(userId, chapterId);
        return Result.ok(result);
    }

    @Operation(summary = "创建学习记录", description = "创建新的学习记录")
    @PostMapping("/")
    @PermissionAccess(UserType.STUDENT)
    @ResourceAccess(id = "#dto.userId")
    public Result<Boolean> create(@RequestBody @Valid UserLearningRecordDto dto) {
        Boolean result = recordService.create(dto);
        return Result.ok(result);
    }

    @Operation(summary = "更新学习记录", description = "更新学习记录")
    @PutMapping("/")
    @PermissionAccess(UserType.STUDENT)
    @ResourceAccess(id = "#dto.userId")
    public Result<Boolean> update(@RequestBody @Valid UserLearningRecordDto dto) {
        Boolean result = recordService.update(dto);
        return Result.ok(result);
    }

}
