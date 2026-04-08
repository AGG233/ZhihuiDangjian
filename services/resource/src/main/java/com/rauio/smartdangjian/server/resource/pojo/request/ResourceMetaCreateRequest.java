package com.rauio.smartdangjian.server.resource.pojo.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
@Schema(description = "资源元数据创建请求。仅创建数据库中的资源记录，不直接接收或上传文件内容")
public class ResourceMetaCreateRequest {

    @Schema(description = "上传人ID，用于标识资源归属", example = "1919810")
    @NotBlank(message = "uploaderId不能为空")
    private String uploaderId;

    @Schema(description = "文件原始名称，保留用户上传时的文件名", example = "党课封面.png")
    @NotBlank(message = "originalName不能为空")
    private String originalName;

    @Schema(description = "文件内容哈希，用于去重和检索", example = "8f14e45fceea167a5a36dedd4bea2543")
    @NotBlank(message = "hash不能为空")
    private String hash;

    @Schema(description = "对象存储键，即文件在COS中的完整存储路径", example = "image/8f14e45fceea167a5a36dedd4bea2543.png")
    @NotBlank(message = "objectKey不能为空")
    private String objectKey;

    @Schema(description = "资源类型，一般为MIME类型", example = "image/png")
    @NotBlank(message = "resourceType不能为空")
    private String resourceType;

    @Schema(description = "资源状态：0表示禁用，1表示可用，2表示已删除；未传时服务端默认置为1", example = "1")
    private Integer status;
}
