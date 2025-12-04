package com.rauio.ZhihuiDangjian.controller;

import com.rauio.ZhihuiDangjian.aop.annotation.PermissionAccess;
import com.rauio.ZhihuiDangjian.pojo.ContentBlock;
import com.rauio.ZhihuiDangjian.pojo.response.ApiResponse;
import com.rauio.ZhihuiDangjian.pojo.vo.ContentBlockVO;
import com.rauio.ZhihuiDangjian.service.ContentBlockService;
import com.rauio.ZhihuiDangjian.service.SearchService;
import com.rauio.ZhihuiDangjian.utils.Spec.UserType;
import io.swagger.v3.oas.annotations.Operation;
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
    private final SearchService searchService;

    @GetMapping("/carousel")
    @Operation(summary = "获取轮播图列表")
    public ApiResponse<List<ContentBlockVO>> getCarousel() {
        List<ContentBlockVO> carousel = contentBlockService.getAllByParentId(1145141919810L);
        return ApiResponse.ok(carousel);
    }

    @PutMapping("/carousel")
    @PermissionAccess(UserType.MANAGER)
    @Operation(summary = "更新轮播图")
    public ApiResponse<Boolean> updateCarousel(@RequestBody ContentBlock contentBlock) {
        Boolean result = contentBlockService.update(contentBlock);
        return ApiResponse.ok(result);
    }

    @PostMapping("/carousel")
    @PermissionAccess(UserType.MANAGER)
    @Operation(summary = "添加轮播图",description = "不用填写ID字段，即便是填写了也不会有任何效果")
    public ApiResponse<Boolean> addCarousel(List<ContentBlock> contentBlocks) {
        for (ContentBlock contentBlock : contentBlocks){
            contentBlock.setParentId(1145141919810L);
        }
        Boolean result = contentBlockService.saveBatch(contentBlocks);
        return ApiResponse.ok(result);
    }

    @DeleteMapping("/carousel")
    @PermissionAccess(UserType.MANAGER)
    @Operation(summary = "删除轮播图")
    public ApiResponse<Boolean> deleteCarousel(Long id) {
        Boolean result = contentBlockService.delete(id);
        return  ApiResponse.ok(result);
    }

    @GetMapping("/search")
    @Operation(summary = "搜索")
    public ApiResponse<String> search(@RequestParam("keyword") String keyword) {
        return ApiResponse.ok("null");
    }

}
