package com.rauio.smartdangjian.server.resource.controller.user;

import com.rauio.smartdangjian.pojo.response.Result;
import com.rauio.smartdangjian.server.resource.service.ResourceService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.web.bind.annotation.*;

import java.net.URL;
import java.util.List;

@Tag(name = "用户资源接口", description = "文件查询下载接口")
@RestController
@RequestMapping("/api/resource/files")
@RequiredArgsConstructor
@ConditionalOnProperty(prefix = "tencent.cloud.cos", name = "enabled", havingValue = "true")

public class UserResourceController {

    private final ResourceService resourceService;

    @Operation(summary = "获取文件链接", description = "根据文件哈希值获取文件链接")
    @GetMapping("/by-hash/{hash}")
    public Result<URL> getByHash(@PathVariable String hash){
        URL url = resourceService.getByHash(hash);
        return Result.ok("200", null, url);
    }

    @Operation(summary = "获取文件链接", description = "通过资源的ID获取文件链接")
    @GetMapping("/by-id/{id}")
    public Result<URL> getById(@PathVariable String id) {
        URL url = resourceService.getById(id);
        return Result.ok("200", null, url);
    }

    @Operation(summary = "批量获取文件访问链接", description = "上传一个列表，列表元素为文件的hash值，根据hash值获取相应的文件访问URL")
    @PostMapping("/batch/hash")
    public Result<List<String>> getBatchByHash(@RequestBody List<String> hashList) {
        List<String> urls = resourceService.getByHashes(hashList);
        return Result.ok("200", null, urls);
    }

    @Operation(summary = "批量获取文件访问链接", description = "上传一个列表，列表元素为资源的ID，根据资源ID获取相应的文件访问URL")
    @PostMapping("/batch/id")
    public Result<List<String>> getBatchById(@RequestBody List<String> idList) {
        List<String> urls = resourceService.getByIds(idList);
        return Result.ok("200", null, urls);
    }
}
