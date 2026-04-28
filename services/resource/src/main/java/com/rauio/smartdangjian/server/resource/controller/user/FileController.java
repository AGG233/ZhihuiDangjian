package com.rauio.smartdangjian.server.resource.controller.user;

import com.rauio.smartdangjian.pojo.response.Result;
import com.rauio.smartdangjian.server.resource.pojo.entity.ResourceMeta;
import com.rauio.smartdangjian.server.resource.pojo.request.UploadFileRequest;
import com.rauio.smartdangjian.server.resource.pojo.response.FileInfoResponse;
import com.rauio.smartdangjian.server.resource.pojo.response.FileUploadResponse;
import com.rauio.smartdangjian.server.resource.service.FileService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "文件资源接口", description = "基于xfile的文件上传、下载、删除接口。上传流程：1) 调用upload获取预签名PUT URL；2) 前端直接用PUT上传文件到该URL；3) 调用confirm确认上传完成。")
@RestController
@RequestMapping("/api/resource/files")
@RequiredArgsConstructor
public class FileController {

    private final FileService fileService;

    @Operation(
            summary = "获取文件上传预签名URL",
            description = "前端调用本接口获取预签名PUT上传地址。服务端会创建ResourceMeta记录（状态为UPLOADING）并返回预签名URL。前端拿到uploadUrl后，应直接对该地址发起HTTP PUT请求，把文件二进制内容作为请求体上传。上传完成后，需调用confirm接口确认。"
    )
    @PostMapping("/upload")
    public Result<FileUploadResponse> upload(@RequestBody @Valid UploadFileRequest request) {
        return Result.ok(fileService.upload(request));
    }

    @Operation(
            summary = "确认文件上传完成",
            description = "前端通过预签名URL成功上传文件到COS后，调用本接口通知服务端。服务端会将ResourceMeta状态从UPLOADING更新为PUBLIC，此后文件即为可用资源。"
    )
    @PostMapping("/confirm/{resourceId}")
    public Result<ResourceMeta> confirmUpload(@PathVariable String resourceId) {
        return Result.ok(fileService.confirmUpload(resourceId));
    }

    @Operation(
            summary = "根据资源ID获取文件信息",
            description = "根据资源ID查询文件元数据，并返回包含预签名下载链接的文件信息。下载链接具有时效性，过期后需重新调用。"
    )
    @GetMapping("/by-id/{id}")
    public Result<FileInfoResponse> getById(@PathVariable String id) {
        return Result.ok(fileService.getFileInfo(id));
    }

    @Operation(
            summary = "根据文件哈希获取文件信息",
            description = "根据文件内容哈希查询文件元数据，并返回包含预签名下载链接的文件信息。"
    )
    @GetMapping("/by-hash/{hash}")
    public Result<FileInfoResponse> getByHash(@PathVariable String hash) {
        return Result.ok(fileService.getFileInfoByHash(hash));
    }

    @Operation(
            summary = "获取文件下载链接",
            description = "根据资源ID生成COS预签名下载链接。前端拿到返回的URL后，可直接发起GET请求下载或预览文件；链接具有时效性，过期后需重新调用本接口获取。"
    )
    @GetMapping("/{id}/download")
    public Result<String> getDownloadUrl(@PathVariable String id) {
        return Result.ok(fileService.getDownloadUrl(id));
    }

    @Operation(
            summary = "批量根据资源ID获取下载链接",
            description = "上传一个资源ID列表，返回对应的预签名下载链接列表。顺序与输入列表一致。"
    )
    @PostMapping("/batch/id")
    public Result<List<String>> getBatchById(@RequestBody @Valid List<String> ids) {
        return Result.ok(fileService.getBatchByIds(ids));
    }

    @Operation(
            summary = "批量根据文件哈希获取下载链接",
            description = "上传一个文件哈希列表，返回对应的预签名下载链接列表。顺序与输入列表一致。"
    )
    @PostMapping("/batch/hash")
    public Result<List<String>> getBatchByHash(@RequestBody @Valid List<String> hashes) {
        return Result.ok(fileService.getBatchByHashes(hashes));
    }

    @Operation(
            summary = "删除文件",
            description = "根据资源ID删除文件。服务端会先从COS删除文件对象，再删除resource_meta数据库记录。"
    )
    @DeleteMapping("/{id}")
    public Result<Boolean> delete(@PathVariable String id) {
        fileService.delete(id);
        return Result.ok(true);
    }
}
