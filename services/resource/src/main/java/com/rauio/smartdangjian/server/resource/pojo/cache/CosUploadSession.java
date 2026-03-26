package com.rauio.smartdangjian.server.resource.pojo.cache;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
@Schema(description = "COS分片上传会话缓存对象，用于在Redis中保存上传会话、文件信息、分片策略、状态和审计时间")
public class CosUploadSession {

    @Schema(description = "COS分片上传会话ID，对应COS返回的uploadId", example = "upload-id")
    private String uploadId;

    @Schema(description = "对象存储键，即文件在COS中的完整存储路径", example = "resource/8f14e45fceea167a5a36dedd4bea2543.mp4")
    private String objectKey;

    @Schema(description = "文件内容哈希，通常用于秒传判定和去重", example = "8f14e45fceea167a5a36dedd4bea2543")
    private String fileHash;

    @Schema(description = "文件原始名称，通常由前端上传时传入", example = "lesson.mp4")
    private String fileName;

    @Schema(description = "文件后缀名，不含或兼容点号格式，由服务端规范化后保存", example = ".mp4")
    private String suffix;

    @Schema(description = "文件MIME类型", example = "video/mp4")
    private String contentType;

    @Schema(description = "文件总大小，单位为字节", example = "104857600")
    private Long fileSize;

    @Schema(description = "分片大小，单位为字节", example = "5242880")
    private Long partSize;

    @Schema(description = "上传状态，典型值包括UPLOADING、COMPLETED、ABORTED", example = "UPLOADING")
    private String status;

    @Schema(description = "当前上传用户ID；匿名或系统场景下可能为空", example = "1919810")
    private String uploaderId;

    @Schema(description = "上传会话创建时间", example = "2026-03-26T10:30:00")
    private LocalDateTime createdAt;

    @Schema(description = "上传会话最近更新时间，用于续传和超时管理", example = "2026-03-26T10:45:00")
    private LocalDateTime updatedAt;
}
