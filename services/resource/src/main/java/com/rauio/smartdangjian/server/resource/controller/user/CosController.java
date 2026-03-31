package com.rauio.smartdangjian.server.resource.controller.user;


import com.rauio.smartdangjian.pojo.response.Result;
import com.rauio.smartdangjian.server.resource.pojo.entity.ResourceMeta;
import com.rauio.smartdangjian.server.resource.pojo.request.GeneratePartUploadUrlRequest;
import com.rauio.smartdangjian.server.resource.pojo.request.InitMultipartUploadRequest;
import com.rauio.smartdangjian.server.resource.pojo.request.MultipartUploadSessionRequest;
import com.rauio.smartdangjian.server.resource.pojo.request.RecordUploadedPartRequest;
import com.rauio.smartdangjian.server.resource.pojo.response.MultipartInitResponse;
import com.rauio.smartdangjian.server.resource.pojo.response.MultipartPartUploadUrlResponse;
import com.rauio.smartdangjian.server.resource.pojo.response.MultipartProgressResponse;
import com.rauio.smartdangjian.server.resource.service.CosService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.web.bind.annotation.*;

import java.net.URL;

@Tag(
        name = "COS上传接口（更加详细的文档详见腾讯云对象存储SDK）",
        description = "提供统一分片上传与断点续传能力。前端调用顺序为：1. 先调用初始化接口获取uploadId、objectKey、partSize和已上传分片；2. 对每个未上传分片调用分片地址接口获取预签名URL；3. 前端使用HTTP PUT直接把该分片上传到返回的URL，而不是把文件流再传回业务服务；4. 每个分片上传成功后，从响应头中取ETag并调用记录分片接口；5. 可随时调用进度接口恢复断点；6. 全部分片完成后调用完成接口，由服务端通知COS合并分片并写入resource_meta。"
)
@RestController
@RequestMapping("/api/cos")
@RequiredArgsConstructor
@ConditionalOnProperty(prefix = "tencent.cloud.cos", name = "enabled", havingValue = "true")
public class CosController {

    private final CosService cosService;

    @Operation(
            summary = "获取文件下载链接",
            description = "根据资源ID下发COS预签名下载链接。前端拿到返回的URL后，可直接发起GET请求下载或预览文件；链接具有时效性，过期后需重新调用本接口获取。"
    )
    @GetMapping("/download/{resourceId}")
    public Result<URL> generateDownloadUrl(@PathVariable String resourceId) {
        return Result.ok(cosService.generateDownloadUrl(resourceId));
    }

    @Operation(
            summary = "初始化分片上传",
            description = "前端在真正上传文件前先调用本接口。服务端会做三件事：1. 判断该文件是否已存在，如果已存在则返回instantUpload=true，前端无需继续上传；2. 判断是否已有未完成上传会话，如果有则直接返回原uploadId和已上传分片列表，前端据此继续断点续传；3. 如果是新上传，则创建新的COS分片上传会话并返回uploadId、objectKey、partSize。前端收到响应后，应把文件按partSize切片，仅对uploadedParts中不存在的分片继续上传。"
    )
    @PostMapping("/multipart/init")
    public Result<MultipartInitResponse> initMultipartUpload(@RequestBody @Valid InitMultipartUploadRequest request) {
        return Result.ok(cosService.initMultipartUpload(request));
    }

    @Operation(
            summary = "生成分片上传地址",
            description = "前端对每一个待上传分片调用本接口，传入uploadId和partNumber，服务端返回该分片对应的COS预签名上传地址。前端拿到uploadUrl后，应直接对该地址发起HTTP PUT请求，并把当前分片的二进制内容作为请求体上传到COS。这个接口只负责生成地址，不负责真正接收文件内容。"
    )
    @PostMapping("/multipart/part-url")
    public Result<MultipartPartUploadUrlResponse> generatePartUploadUrl(@RequestBody @Valid GeneratePartUploadUrlRequest request) {
        return Result.ok(cosService.generatePartUploadUrl(request));
    }

    @Operation(
            summary = "记录已上传分片",
            description = "前端每上传成功一个分片后，都应立即调用本接口回传partNumber和ETag。ETag应从上传COS成功后的响应头中提取。服务端会把已完成分片写入Redis，用于后续断点续传和最终分片合并。如果前端不回传ETag，服务端将无法准确恢复进度或完成分片合并。"
    )
    @PostMapping("/multipart/part")
    public Result<Boolean> recordUploadedPart(@RequestBody @Valid RecordUploadedPartRequest request) {
        return Result.ok(cosService.recordUploadedPart(request));
    }

    @Operation(
            summary = "获取分片上传进度",
            description = "前端在页面刷新、应用重启或网络中断后，可调用本接口恢复当前上传状态。接口会返回uploadId、objectKey、status以及已上传分片列表。前端应据此跳过已上传分片，只重新请求未完成分片的上传地址并继续上传。"
    )
    @GetMapping("/multipart/progress")
    public Result<MultipartProgressResponse> getUploadProgress(@Valid MultipartUploadSessionRequest request) {
        return Result.ok(cosService.getUploadProgress(request));
    }

    @Operation(
            summary = "完成分片上传",
            description = "当前端确认所有分片都已上传并且都已成功回传ETag后，调用本接口。服务端会读取Redis中的分片记录，通知COS执行分片合并，并在合并成功后写入resource_meta表。前端只有在本接口成功返回后，才能认为整个文件上传完成并已经成为系统内可用资源。"
    )
    @PostMapping("/multipart/complete")
    public Result<ResourceMeta> completeMultipartUpload(@RequestBody @Valid MultipartUploadSessionRequest request) {
        return Result.ok(cosService.completeMultipartUpload(request));
    }

    @Operation(
            summary = "取消分片上传",
            description = "当前端主动取消上传，或需要放弃当前uploadId重新开始时，调用本接口。服务端会通知COS终止该分片上传会话，并清理Redis中的上传会话和分片记录。调用成功后，该uploadId将失效，后续若要继续上传，前端必须重新调用初始化接口。"
    )
    @DeleteMapping("/multipart")
    public Result<Boolean> abortMultipartUpload(@RequestBody @Valid MultipartUploadSessionRequest request) {
        return Result.ok(cosService.abortMultipartUpload(request));
    }
}
