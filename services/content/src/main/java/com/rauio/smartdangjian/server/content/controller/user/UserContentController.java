package com.rauio.smartdangjian.server.content.controller.user;

import com.rauio.smartdangjian.pojo.response.Result;
import com.rauio.smartdangjian.server.content.pojo.vo.ContentBlockVO;
import com.rauio.smartdangjian.server.content.service.ContentBlockService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Tag(name = "用户内容接口")
@RestController
@RequestMapping("/api/content/content-blocks")
@RequiredArgsConstructor
public class UserContentController {

    private static final String CAROUSEL_PARENT_ID = "1145141919810";

    private final ContentBlockService contentBlockService;

    @Operation(summary = "获取轮播图列表")
    @GetMapping("/carousel")
    public Result<List<ContentBlockVO>> getCarousel() {
        return Result.ok(contentBlockService.getByParentId(CAROUSEL_PARENT_ID));
    }
}
