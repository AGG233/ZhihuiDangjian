package com.rauio.smartdangjian.server.content.controller.admin;

import com.rauio.smartdangjian.aop.annotation.PermissionAccess;
import com.rauio.smartdangjian.pojo.response.Result;
import com.rauio.smartdangjian.server.content.pojo.entity.ContentBlock;
import com.rauio.smartdangjian.server.content.service.ContentBlockService;
import com.rauio.smartdangjian.utils.spec.UserType;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Tag(name = "管理员内容接口", description = "提供轮播图等内容块管理能力")
@RestController
@RequestMapping("/api/admin/content/content-blocks")
@RequiredArgsConstructor
@PermissionAccess(UserType.MANAGER)
public class AdminContentController {

    private static final String CAROUSEL_PARENT_ID = "1145141919810";

    private final ContentBlockService contentBlockService;

    @Operation(summary = "更新轮播图")
    @PutMapping("/carousel")
    public Result<Boolean> updateCarousel(@RequestBody ContentBlock contentBlock) {
        return Result.ok(contentBlockService.update(contentBlock));
    }

    @Operation(summary = "添加轮播图")
    @PostMapping("/carousel")
    public Result<Boolean> addCarousel(@RequestBody List<ContentBlock> contentBlocks) {
        for (ContentBlock contentBlock : contentBlocks) {
            contentBlock.setParentId(CAROUSEL_PARENT_ID);
        }
        return Result.ok(contentBlockService.saveBatch(contentBlocks));
    }

    @Operation(summary = "删除轮播图")
    @DeleteMapping("/carousel/{id}")
    public Result<Boolean> deleteCarousel(@PathVariable String id) {
        return Result.ok(contentBlockService.delete(id));
    }
}
