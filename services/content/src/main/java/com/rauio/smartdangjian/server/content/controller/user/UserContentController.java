package com.rauio.smartdangjian.server.content.controller.user;

import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.rauio.smartdangjian.pojo.response.Result;
import com.rauio.smartdangjian.server.content.pojo.response.ContentBlockResponse;
import com.rauio.smartdangjian.server.content.service.ChapterContentBlockService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;

@Tag(name = "用户内容接口")
@RestController
@RequestMapping("/api/content/content-blocks")
@RequiredArgsConstructor
public class UserContentController {

    private static final Long CAROUSEL_PARENT_ID = 1145141919810L;

    private final ChapterContentBlockService chapterContentBlockService;

    @Operation(summary = "获取轮播图列表")
    @GetMapping("/carousel")
    public Result<List<ContentBlockResponse>> getCarousel() {
        return Result.ok(chapterContentBlockService.getByChapterId(CAROUSEL_PARENT_ID));
    }
}
