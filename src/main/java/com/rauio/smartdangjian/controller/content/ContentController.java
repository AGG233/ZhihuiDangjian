package com.rauio.smartdangjian.controller.content;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.rauio.smartdangjian.aop.annotation.PermissionAccess;
import com.rauio.smartdangjian.content.pojo.ContentBlock;
import com.rauio.smartdangjian.pojo.response.Result;
import com.rauio.smartdangjian.content.pojo.vo.ContentBlockVO;
import com.rauio.smartdangjian.content.service.ContentBlockService;
import com.rauio.smartdangjian.search.service.SearchService;
import com.rauio.smartdangjian.utils.spec.UserType;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/")
@RequiredArgsConstructor
@Tag(name = "内容接口", description = "ID字段无需填写，会自动生成一个ID")
public class ContentController {

    private final ContentBlockService contentBlockService;
//    private final SearchService searchService;

    @GetMapping("/carousel")
    @Operation(summary = "获取轮播图列表")
    public Result<List<ContentBlockVO>> getCarousel() {
        var carousel = contentBlockService.getByParentId("1145141919810");
        return Result.ok(carousel);
    }

    @PutMapping("/carousel")
    @PermissionAccess(UserType.MANAGER)
    @Operation(summary = "更新轮播图")
    public Result<Boolean> updateCarousel(@RequestBody ContentBlock contentBlock) {
        var result = contentBlockService.update(contentBlock);
        return Result.ok(result);
    }

    @PostMapping("/carousel")
    @PermissionAccess(UserType.MANAGER)
    @Operation(summary = "添加轮播图",description = "不用填写ID字段，即便是填写了也不会有任何效果")
    public Result<Boolean> addCarousel(List<ContentBlock> contentBlocks) {
        for (ContentBlock contentBlock : contentBlocks){
            contentBlock.setParentId("1145141919810");
        }
        var result = contentBlockService.saveBatch(contentBlocks);
        return Result.ok(result);
    }

    @DeleteMapping("/carousel")
    @PermissionAccess(UserType.MANAGER)
    @Operation(summary = "删除轮播图")
    public Result<Boolean> deleteCarousel(String id) {
        var result = contentBlockService.delete(id);
        return  Result.ok(result);
    }

//    @GetMapping("/search/course/{keyword}")
//    @Operation(summary = "搜索")
//    public Result<Page<CourseVO>> search(
//            @PathVariable String keyword,
//            @Parameter(description = "页码")  @RequestParam(defaultValue = "1") int pageNum,
//            @Parameter(description = "页大小") @RequestParam(defaultValue = "10") int pageSize)
//    {
//        var result = searchService.getHybridSearchResult(keyword, pageNum, pageSize);
//        return Result.ok(result);
//    }

}
