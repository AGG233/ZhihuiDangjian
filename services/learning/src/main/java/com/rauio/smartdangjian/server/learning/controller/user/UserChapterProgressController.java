package com.rauio.smartdangjian.server.learning.controller.user;

import com.rauio.smartdangjian.aop.annotation.DataScopeAccess;
import com.rauio.smartdangjian.aop.annotation.PermissionAccess;
import com.rauio.smartdangjian.aop.annotation.ResourceAccess;
import com.rauio.smartdangjian.aop.support.DataScopeAction;
import com.rauio.smartdangjian.aop.support.DataScopeResources;
import com.rauio.smartdangjian.pojo.response.Result;
import com.rauio.smartdangjian.server.learning.pojo.dto.UserChapterProgressDto;
import com.rauio.smartdangjian.server.learning.pojo.vo.UserChapterProgressVO;
import com.rauio.smartdangjian.server.learning.service.UserChapterProgressService;
import com.rauio.smartdangjian.utils.spec.UserType;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "用户章节进度接口", description = "用户章节学习进度管理接口")
@RestController
@RequestMapping("/api/learning/progress")
@RequiredArgsConstructor
public class UserChapterProgressController {

    private final UserChapterProgressService progressService;

    @Operation(summary = "获取进度记录", description = "根据进度ID获取用户章节进度记录")
    @GetMapping("/{id}")
    @PermissionAccess(UserType.STUDENT)
    @DataScopeAccess(resource = DataScopeResources.CHAPTER_PROGRESS, action = DataScopeAction.READ, id = "#id")
    public Result<UserChapterProgressVO> get(@Parameter(name = "id", description = "进度ID") @PathVariable String id) {
        UserChapterProgressVO result = progressService.get(id);
        return Result.ok(result);
    }

    @Operation(summary = "获取用户所有进度", description = "根据用户ID获取该用户的所有章节进度")
    @GetMapping("/user/{userId}")
    @PermissionAccess(UserType.STUDENT)
    @ResourceAccess(id = "#userId")
    public Result<List<UserChapterProgressVO>> getByUserId(@Parameter(name = "userId", description = "用户ID") @PathVariable String userId) {
        List<UserChapterProgressVO> result = progressService.getByUserId(userId);
        return Result.ok(result);
    }

    @Operation(summary = "获取用户章节进度", description = "获取指定用户在指定章节的学习进度")
    @GetMapping("/user/{userId}/chapter/{chapterId}")
    @PermissionAccess(UserType.STUDENT)
    @ResourceAccess(id = "#userId")
    public Result<UserChapterProgressVO> getByUserIdAndChapterId(
            @Parameter(name = "userId", description = "用户ID") @PathVariable String userId,
            @Parameter(name = "chapterId", description = "章节ID") @PathVariable String chapterId) {
        UserChapterProgressVO result = progressService.getByUserIdAndChapterId(userId, chapterId);
        return Result.ok(result);
    }

    @Operation(summary = "创建进度记录", description = "创建新的用户章节进度记录")
    @PostMapping("/")
    @PermissionAccess(UserType.STUDENT)
    @ResourceAccess(id = "#dto.userId")
    public Result<Boolean> create(@RequestBody @Valid UserChapterProgressDto dto) {
        Boolean result = progressService.create(dto);
        return Result.ok(result);
    }

    @Operation(summary = "更新进度记录", description = "更新用户章节进度记录")
    @PutMapping("/")
    @PermissionAccess(UserType.STUDENT)
    @ResourceAccess(id = "#dto.userId")
    public Result<Boolean> update(@RequestBody @Valid UserChapterProgressDto dto) {
        Boolean result = progressService.update(dto);
        return Result.ok(result);
    }

}
